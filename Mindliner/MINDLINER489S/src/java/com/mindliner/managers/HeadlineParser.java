package com.mindliner.managers;

import com.mindliner.analysis.MindlinerStripTag;
import com.mindliner.analysis.TagParser;
import com.mindliner.categories.MlsEventType;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import com.mindliner.contentfilter.Timed;
import com.mindliner.entities.EntityRefresher;
import com.mindliner.entities.MlsLink;
import com.mindliner.entities.mlsContact;
import com.mindliner.entities.mlsLog;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsObjectCollection;
import com.mindliner.entities.mlsTask;
import com.mindliner.entities.mlsUser;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.enums.ObjectReviewStatus;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.managers.MlMessageHandler.MessageEventType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * This class searches for and interprets dot tags and hash tags that appear in
 * the headline of an object.
 *
 * @author Marius Messerli
 */
@Stateless
@DeclareRoles(value = {"Admin", "User"})
@RolesAllowed(value = {"Admin", "User"})
public class HeadlineParser implements HeadlineParserRemote {

    private mlsObject editedObject = null;
    private String headline = "";
    private TagParser newHeadlineParser;

    private mlsUser owner = null;
    private Date date = null;
    private mlsPriority priority = null;
    private mlsConfidentiality confidentiality = null;

    private Map<mlsObject, Boolean> linkCandidateMap = new HashMap<>();
    private List<mlsObject> unlinkCandidates = new ArrayList<>();

    private boolean privateAccess = false;
//    private mlsUser currentUser = null;
    private final String contactFirstName = "";
    private final String contactMiddleName = "";
    private final String contactLastName = "";
    private final String contactEmail = "";
    public static final String nameSeparator = "-";
    @EJB
    private CategoryManagerRemote catman;
    @EJB
    private ObjectManagerRemote objectManager;
    @EJB
    private UserManagerLocal userManager;
    @PersistenceContext
    private EntityManager em;
    @EJB
    private SearchManagerLocal searchManager;
    @EJB
    private SolrServerBean solrServer;
    @EJB
    LogManagerLocal logManager;

    @Override
    public int updateHeadline(int objectId, String headline) throws mlModifiedException, NonExistingObjectException {
        this.headline = headline;
        editedObject = (mlsObject) em.find(mlsObject.class, objectId);
        if (editedObject == null) {
            throw new NonExistingObjectException("The object no longer exists on the server.");
        }
        if (editedObject instanceof mlsContact) {
            // we don't care for headlines in the case of contacts
            MlMessageHandler mh = new MlMessageHandler();
            mh.sendMessage(userManager.getCurrentUser(), editedObject, MessageEventType.OBJECT_UPDATE_EVENT, "description");
            mh.closeConnection();
        } else {
            privateAccess = editedObject.getPrivateAccess();
            linkCandidateMap = new HashMap<>();
            unlinkCandidates = new ArrayList<>();
            date = null;
            priority = null;
            owner = null;
            analyze();
            persistCurrentObject();
            logManager.log(editedObject.getClient(), MlsEventType.EventType.ObjectUpdated, editedObject, 0, "headline update to: " + headline, "updateHeadline", mlsLog.Type.Modify);
        }
        solrServer.addObject(editedObject, true);
        return editedObject.getVersion();
    }

    private void analyze() {
        newHeadlineParser = new TagParser(headline);
        determineOwner();
        determineDate();
        determinePrivacyFlag();
        processIdTags();
        processObjectTag();
        headline = concatenatePlainWords();
    }

    private void persistCurrentObject() throws mlModifiedException {
        mlsUser currUser = userManager.getCurrentUser();
        editedObject.setHeadline(concatenatePlainWords());
        editedObject.setPrivateAccess(privateAccess);
        if (confidentiality != null) {
            editedObject.setConfidentiality(confidentiality);
        }
        editedObject.setModificationDate(new Date());

        if (priority != null && editedObject instanceof mlsTask) {
            ((mlsTask) editedObject).setPriority(priority);
        }

        if (date != null && editedObject instanceof Timed) {
            ((Timed) editedObject).setDueDate(date);
        }

        if (editedObject instanceof mlsContact) {
            mlsContact c = (mlsContact) editedObject;
            c.setFirstName(contactFirstName);
            c.setMiddleName(contactMiddleName);
            c.setLastName(contactLastName);
            c.setEmail(contactEmail);
        }
        MlMessageHandler mh = new MlMessageHandler();
        if (!linkCandidateMap.isEmpty()) {
            IslandUpdater iu = new IslandUpdater(em);
            for (mlsObject lc : linkCandidateMap.keySet()) {
                boolean islandsReconciled = false;
//                lc = em.merge(lc);
                boolean updatedA = false;
                boolean updatedB = false;
                // 
                if (linkCandidateMap.get(lc) && !editedObject.getRelatives().contains(lc)) {
                    MlsLink link = new MlsLink(editedObject, lc, currUser);
                    em.persist(link);
                    iu.reconcileAfterLinking(editedObject, lc);
                    islandsReconciled = true;
                    updatedA = true;
                }
                if (!lc.getRelatives().contains(editedObject)) {
                    MlsLink link = new MlsLink(lc, editedObject, currUser);
                    em.persist(link);
                    if (!islandsReconciled) {
                        iu.reconcileAfterLinking(lc, editedObject);
                    }
                    updatedB = true;
                }
                if (updatedA || updatedB) {
                    em.flush();
                }
                if (updatedA) {
                    EntityRefresher.updateCachedEntity(em, editedObject.getId(), editedObject);
                }
                if (updatedB) {
                    EntityRefresher.updateCachedEntity(em, lc.getId(), lc);
                    mh.sendMessage(currUser, lc, MessageEventType.OBJECT_UPDATE_EVENT, "linking");
                }
            }
        }
        if (!unlinkCandidates.isEmpty()) {
            // @todo update islands after removing link
            for (mlsObject o : unlinkCandidates) {
                o = em.merge(o);
                if (editedObject.getRelatives().contains(o)) {
                    Query q = em.createNamedQuery("MlsLink.removeLink");
                    q.setParameter("id1", editedObject.getId());
                    q.setParameter("id2", o.getId());
                    q.setParameter("relativeType", LinkRelativeType.OBJECT);
                    q.executeUpdate();
                    editedObject.getRelatives().remove(o);
                    o.getRelatives().remove(editedObject);
                    em.flush();
                    EntityRefresher.updateCachedEntity(em, editedObject.getId(), editedObject);
                    EntityRefresher.updateCachedEntity(em, o.getId(), o);
                    mh.sendMessage(currUser, o, MessageEventType.OBJECT_UPDATE_EVENT, "unlinking");
                }
            }
        }
        em.merge(editedObject);
        if (owner != null) {
            editedObject.setOwner(owner);
        }
        // here I get a persistence exception because object (must be editedObject) was changed or modified in the meantime
        em.flush();

        mh.sendMessage(currUser, editedObject, MessageEventType.OBJECT_UPDATE_EVENT, "headline");
        mh.closeConnection();
    }

    /**
     * Analyzes the inputString string and returns an array of Strings
     *
     * @param inputString An inputString string in the form "mar" or "mar-mes"
     * where the first is interpreted as a first name fragment and the second is
     * interpreted as a firstname fragement and a lastname fragment,
     * respectively.
     * @return An array of two Strings there [0] is the firstname fragment and
     * [1] is the lastname fragment
     */
    private String[] getNameComponents(String inputString) {
        String[] result = new String[2];
        if (inputString.contains(nameSeparator)) {
            result[0] = inputString.substring(2, inputString.indexOf(nameSeparator));

            if (inputString.length() > inputString.indexOf(nameSeparator)) {
                result[1] = inputString.substring(inputString.indexOf(nameSeparator) + 1);
            }
        } else {
            result[0] = inputString.substring(2);
            result[1] = "";
        }
        return result;
    }

    private void determineOwner() {
        for (String taggedWord : newHeadlineParser.getMindlinerTagOccurances()) {
            if (taggedWord.length() > 1 && taggedWord.substring(0, 2).equals(MindlinerStripTag.getFullTag(MindlinerStripTag.TagKey.OWNER))) {
                String[] nameComponents = getNameComponents(taggedWord);
                List<mlsUser> users = objectManager.getUsersByFirstAndLAstnameSubstrings(nameComponents[0], nameComponents[1], editedObject.getClient().getId());
                if (users.size() == 1) {
                    mlsUser u = users.get(0);
                    if (u.getClients().contains(editedObject.getClient())) {
                        owner = u;
                    }
                } else {
                    System.err.println(getClass().getName() + ": ignoring the owner tag that was ambiguous");
                }
            }
        }
    }

    /**
     * Method looks for a date tag (.d) interprets the tag's value and updates
     * the date field accordingly. The interpretation of the date field depends
     * on the actual class of the input element handled into the analyzer.
     */
    private void determineDate() {
        for (String taggedWord : newHeadlineParser.getMindlinerTagOccurances()) {
            if (taggedWord.length() > 1 && taggedWord.substring(0, 2).equals(MindlinerStripTag.getFullTag(MindlinerStripTag.TagKey.DUEDATE))) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());

                String dateDescriptor = taggedWord.substring(2);
                int dayOfWeek;

                if ("tomorrow".contains(dateDescriptor)) {
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                } else if ("month".contains(dateDescriptor)) {
                    cal.add(Calendar.MONTH, 1);
                } else if ("week".contains(dateDescriptor)) {
                    cal.add(Calendar.DAY_OF_MONTH, 7);
                } else {
                    if ("monday".contains(dateDescriptor)) {
                        dayOfWeek = Calendar.MONDAY;
                    } else if ("tuesday".contains(dateDescriptor)) {
                        dayOfWeek = Calendar.TUESDAY;
                    } else if ("wednesday".contains(dateDescriptor)) {
                        dayOfWeek = Calendar.WEDNESDAY;
                    } else if ("thursday".contains(dateDescriptor)) {
                        dayOfWeek = Calendar.THURSDAY;
                    } else if ("friday".contains(dateDescriptor)) {
                        dayOfWeek = Calendar.FRIDAY;
                    } else if ("saturday".contains(dateDescriptor)) {
                        dayOfWeek = Calendar.SATURDAY;
                    } else if ("sunday".contains(dateDescriptor)) {
                        dayOfWeek = Calendar.SUNDAY;
                    } // in case we have a typo or no day was found go to next Monday
                    else {
                        dayOfWeek = Calendar.MONDAY;
                    }

                    cal.add(Calendar.DAY_OF_MONTH, 1); // we are looking for next week
                    while (cal.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }
                date = cal.getTime();
            }
        }
    }

    private void determinePrivacyFlag() {
        for (String tag : newHeadlineParser.getMindlinerTagOccurances()) {
            if (tag.length() > 2 && tag.substring(0, 2).equals(MindlinerStripTag.getFullTag(MindlinerStripTag.TagKey.PRIVACY))) {
                String valueString = tag.substring(2, 3);
                if (valueString.isEmpty() == false) {
                    privateAccess = !valueString.toLowerCase().equals("f");
                }
            }
        }
    }

    /**
     * Builds a new string with just plain words and the tags cut out.
     *
     * @return A string of plain words with all the tags cut out.
     */
    private String concatenatePlainWords() {
        StringBuilder sb = new StringBuilder();
        Iterator it = newHeadlineParser.getPlainWords().iterator();
        while (it.hasNext()) {
            String s = (String) it.next();
            sb.append(s);
            if (it.hasNext()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private mlsObject createNewTagObject(String tag) {
        mlsObject newObject;
        mlsUser currentUser = userManager.getCurrentUser();
        // create new hash tag object as collection
        mlsObjectCollection oc = new mlsObjectCollection();
        newObject = oc;
        // implicitly make the tag belong to the same client as the object bearing the new tag in the headline
        newObject.setClient(editedObject.getClient());
        newObject.setConfidentiality(editedObject.getConfidentiality());
        newObject.setOwner(currentUser);
        newObject.setPrivateAccess(editedObject.getPrivateAccess());
        newObject.setStatus(ObjectReviewStatus.REVIEWED);
        newObject.setHeadline(MindlinerStripTag.getTagObjectHeadline(tag));
        newObject.setDescription("This is a Mindliner tag object - don't change its headline or description, just delete it if you no longer need it. "
                + "To link another object to this tag add the following to the headline of that object: " + MindlinerStripTag.getFullTag(MindlinerStripTag.TagKey.TAG).concat(tag));
        em.persist(newObject);
        em.flush();
        solrServer.addObject(newObject, true);
        return newObject;
    }

    /**
     * This function searches for .t tags followed by the name of a hash tag
     * (less the initial hash). If such an object is found then it is linked to
     * the current object.
     */
    private void processObjectTag() {
        for (String tag : newHeadlineParser.getMindlinerTagOccurances()) {
            if (tag.length() > 2 && tag.substring(0, 2).equals(MindlinerStripTag.getFullTag(MindlinerStripTag.TagKey.TAG))) {
                String valueString = tag.substring(2);
                mlsObject tagObject = searchManager.getTagObject(valueString);
                if (tagObject != null) {
                    if (!linkCandidateMap.containsKey(tagObject)) {
                        linkCandidateMap.put(tagObject, false);
                    }
                } else {
                    mlsObject newTagObject = createNewTagObject(valueString);
                    linkCandidateMap.put(newTagObject, false);
                }
            }
        }
    }

    private void processIdTags() {
        for (String tag : newHeadlineParser.getMindlinerTagOccurances()) {
            if (tag.length() > 2 && tag.substring(0, 2).equals(MindlinerStripTag.getFullTag(MindlinerStripTag.TagKey.ID))) {
                String valueString = tag.substring(2);
                try {
                    int candidateId = Integer.parseInt(valueString);
                    mlsObject relative = em.find(mlsObject.class, candidateId);
                    if (relative != null && !linkCandidateMap.containsKey(relative)) {
                        linkCandidateMap.put(relative, true);
                    }
                } catch (NumberFormatException ex) {
                    // do nothing, just ignore this tag
                }
            }
        }
    }

}
