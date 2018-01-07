package com.mindliner.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mindliner.analysis.MindlinerStripTag;
import com.mindliner.analysis.MlClassHandler;
import com.mindliner.categories.MlsEventType;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import com.mindliner.common.WeekUtil;
import com.mindliner.comparatorsS.IslandSizeComparator;
import com.mindliner.comparatorsS.LinkPositionComparatorTransfer;
import com.mindliner.comparatorsS.mlsRatingComparator;
import com.mindliner.contentfilter.BaseFilter;
import com.mindliner.contentfilter.TimeFilter;
import com.mindliner.contentfilter.TimeFilter.TimePeriod;
import com.mindliner.contentfilter.mlFilterTO;
import com.mindliner.entities.Island;
import com.mindliner.entities.MlsImage;
import com.mindliner.entities.MlsImage.ImageType;
import com.mindliner.entities.MlsLink;
import com.mindliner.entities.MlsNews;
import com.mindliner.entities.UserQuery;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsLog;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsObjectCollection;
import com.mindliner.entities.mlsTask;
import com.mindliner.entities.mlsUser;
import com.mindliner.entities.mlsWeekPlan;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.objects.transfer.MltLink;
import com.mindliner.objects.transfer.MltObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import com.mindliner.json.MindlinerObjectJson;

/**
 * This class implements all the search functions on the server. It is
 * stateless, all search parameters are passed to the ejb in each call
 *
 * @author Marius Messerli
 */
@Stateless
@DeclareRoles(value = {"MasterAdmin", "Admin", "User"})
@RolesAllowed(value = {"MasterAdmin", "Admin", "User"})
public class SearchManagerBean implements SearchManagerRemote, SearchManagerLocal {

    private static final String SOLR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // ISO 8601 standard that solr requires

    @PersistenceContext
    private EntityManager em;
    @EJB
    CategoryManagerRemote categoryManager;
    @EJB
    UserManagerLocal userManager;
    @EJB
    SolrServerBean solrServer;
    
    @EJB
    private LogManagerLocal logManager;
    @EJB
    private ObjectManagerLocal objectManager;

    /**
     * ATTENTION:
     *
     * The function below must duplicate the array. Otherwise the elements get
     * actually unlinked in the database too because they are managed when
     * passed into this function!
     *
     * @todo This should be possible without duplication by calling em.detach on
     * those objects
     */
    private List<mlsObject> filterRecords(List<mlsObject> sourcelist, BaseFilter filter) {
        List<mlsObject> resultlist = new ArrayList();
        for (mlsObject o : sourcelist) {
            resultlist.add(o);
        }
        filter.setRecords(resultlist);
        filter.reconfigureEvaluators();
        filter.removeNonconformingObjects();
        return filter.getRecords();
    }

    /**
     * This function creates a new filter with all defaults according to web
     * usage.
     */
    private BaseFilter createDefaultFilter() {
        BaseFilter filter = new BaseFilter();
        filter.setCurrentUser(userManager.getCurrentUser());
        filter.setTextConstraint("");
        filter.setShowPrivateElements(true);
        filter.setOwnerIds(new ArrayList<>());
        filter.reconfigureEvaluators();
        return filter;
    }

    private BaseFilter createFilterForDesktop() {
        BaseFilter filter = new BaseFilter();
        filter.setCurrentUser(userManager.getCurrentUser());
        Map<Integer, mlsConfidentiality> confMap = new HashMap<>();
        for (mlsConfidentiality c : userManager.getCurrentUser().getMaxConfidentialities()) {
            confMap.put(c.getClient().getId(), c);
        }
        filter.setMaxConfidentialities(confMap);
        filter.setShowArchived(true);
        filter.setShowPrivateElements(true);
        filter.setMaxModificationAge(TimePeriod.All);
        filter.reconfigureEvaluators();
        return filter;
    }

    private String buildMainQuery(mlFilterTO filter, String searchString) {
        /*
         Solr uses prefix operators instead of boolean operators 
         (See http://java.dzone.com/articles/use-prefix-operators-instead for further information about prefix operators)
         Each subquery has a prefix. To pass a query, a document must
         fullfill each subquery.
        
         + means must occur
         - means must not occur
         no prefix means can occur (a hit would account to the result score)
        
         NOTE: -X Y Z   does not mean (NOT X) OR Y OR Z but rather (NOT X) AND (Y OR Z).
         */
        // Escape special characters of solr except ~,*,? which may be used by the user
        searchString = searchString.replaceAll("\\\\|\\:|\\!|\\-|\\+|\\^|\\{|\\}|\\(|\\)|\\[|\\]|\\*", "\\\\$0");
        StringBuilder query = new StringBuilder();

        if (searchString != null && !searchString.isEmpty()) {
            StringTokenizer st = new StringTokenizer(searchString);
            do {
                String word = st.nextToken();
                if (word.length() < 3) {
                    // currently our solr filter supports only words with size >2
                    continue;
                }
                String fileQuery = "";
                if (filter.isIncludeFiles()) {
                    fileQuery = " {!join from=attached_id to=id}content:" + word;
                }
                // each word may be in headline or description. However headline matches have higher priority (search boost with factor 3)
                query.append(" +(headline:").append(word).append("^3").append(" description:").append(word);
                // in case of a contact search we also look at first and lastname (the headline is not always equal 'firstname lastname')
                if (MlClassHandler.MindlinerObjectType.Contact.equals(filter.getObjectType())) {
                    query.append(" firstname:").append(word).append(" lastname:").append(word);
                }
                query.append(fileQuery).append(")");
            } while (st.hasMoreElements());
        }
        mlsUser cUser = userManager.getCurrentUser();

        if (filter.getOwnerIds() != null && !filter.getOwnerIds().isEmpty()) {
            String ownerList = buildSolrIdList(filter.getOwnerIds());
            query.append(" +owner_id:").append(ownerList);
        }

        if (filter.getMaxModificationAge() != TimePeriod.All) {
            switch (filter.getMaxModificationAge()) {
                case Hour:
                    query.append(" +modification:[NOW-1HOUR TO NOW]");
                    break;
                case Day:
                    query.append(" +modification:[NOW-1DAY TO NOW]");
                    break;
                case Week:
                    query.append(" +modification:[NOW-7DAY TO NOW]");
                    break;
                case Fortnight:
                    query.append(" +modification:[NOW-14DAY TO NOW]");
                    break;
                case Month:
                    query.append(" +modification:[NOW-1MONTH TO NOW]");
                    break;
                case Year:
                    query.append(" +modification:[NOW-1YEAR TO NOW]");
                    break;
                case SinceLastLogout:
                    DateFormat df = new SimpleDateFormat(SOLR_DATE_FORMAT); // ISO 8601 standard that solr requires
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String lastLogout = df.format(cUser.getLastLogout());
                    query.append(" +modification:[").append(lastLogout).append(" TO NOW]");
                    break;
                default:
                    Logger.getLogger(SearchManagerBean.class.getName()).log(Level.WARNING, "Unsupported ModificationAge qualifier used: {0}", filter.getMaxModificationAge());
            }
        }

        if (query.toString().isEmpty()) {
            // empty query should return any results
            // and for safety, queries that only contain negations are forbidden (e.g. only isExpired constraint)
            query.append("*:*");
        }

        return query.toString();
    }

    private String buildSolrIdList(Collection<Integer> ids) {
        Iterator it = ids.iterator();
        if (ids.size() == 1) {
            return String.valueOf(it.next());
        }
        StringBuilder list = new StringBuilder("(");
        while (it.hasNext()) {
            Integer i = (Integer) it.next();
            list.append(i);
            if (it.hasNext()) {
                list.append(" ");
            }
        }
        list.append(")");

        return list.toString();
    }

    private List<Integer> solrSearch(mlFilterTO fto, String searchString) throws SolrServerException {
        SolrServer server = solrServer.getServer();

        SolrQuery parameters = new SolrQuery();
        // Use only fields in the main query that should be considered for scoring (e.g. headline, description,...)
        // All other field restrictions are better introduced as a Filter Query because Filter Query results are cached and therefore faster
        String query = buildMainQuery(fto, searchString);
        parameters.set("q", query);
        parameters.set("rows", fto.getMaxNumberOfElements());
        parameters.set("fl", "id, modification, dtype, lifetime");
        addFilterQueries(parameters, fto);

        // as rating sort, use solr internal priority rating
        switch (fto.getDefaultSorting()) {
            case Creation:
                parameters.set("sort", "creation_date desc, score desc");
                break;

            case Modification:
                parameters.set("sort", "modification desc, score desc");
                break;

            case Rating:
                parameters.set("sort", "rating desc, score desc");
                break;
        }

        List<Integer> objs = new ArrayList<>();
        // Get ID list from solr server
        QueryResponse res = server.query(parameters);
        for (SolrDocument item : res.getResults()) {
            Object id = item.getFieldValue("id");
            if (id instanceof String) {
                objs.add(Integer.valueOf((String) id));
            }
        }
        UserQuery uq = new UserQuery(userManager.getCurrentUser().getId(), searchString, fto.getObjectType(), objs.size());
        em.persist(uq);
        return objs;
    }

    private void addFilterQueries(SolrQuery parameters, mlFilterTO fto) {
        // fix this parameter as we now may have objects belonging to different clients with a different max confi per client
        // I may need to run multiple solr searches, or we omit the restriction here and check it
        // below I just take the max conf of the first client to get a functional setup (very likely the only one for now)
        mlsConfidentiality conf = userManager.getCurrentUser().getMaxConfidentialities().get(0);
        parameters.addFilterQuery("+clevel:[* TO " + conf.getClevel() + "]");

        mlsUser cUser = userManager.getCurrentUser();
        String clientIds;
        if (fto.getDataPoolIds() != null && !fto.getDataPoolIds().isEmpty()) {
            clientIds = buildSolrIdList(fto.getDataPoolIds());
        } else {
            // Empty data pool list means no restrictions on the data pools the user belongs to
            List<Integer> cIds = new ArrayList<>();
            for (mlsClient c : cUser.getClients()) {
                cIds.add(c.getId());
            }
            clientIds = buildSolrIdList(cIds);
        }
        parameters.addFilterQuery("client_id:" + clientIds);

        if (fto.getShowPrivate()) {
            parameters.addFilterQuery(" +(private:false owner_id:" + cUser.getId() + ")");
        } else {
            parameters.addFilterQuery("private:false");
        }
        if (!fto.isShowArchived()) {
            parameters.addFilterQuery("archived:false");
        }

        switch (fto.getObjectType()) {
            case Task:
                parameters.addFilterQuery("dtype:TASK");
                break;
            case Collection:
                parameters.addFilterQuery("dtype:OCOL");
                break;
            case Knowlet:
                parameters.addFilterQuery("dtype:KNOW");
                break;
            case Contact:
                parameters.addFilterQuery("dtype:CONT");
                break;
            case Image:
                parameters.addFilterQuery("dtype:SIMG");
                break;
            case News:
                parameters.addFilterQuery("dtype:ACTI");
                break;
            case Container:
                parameters.addFilterQuery("dtype:CNTR");
                break;
            case Map:
                parameters.addFilterQuery("dtype:CMAP");
                break;
        }
    }

    @Override
    public List<Integer> getTextSearchResultsIds(String searchString, mlFilterTO fto) {
        return mlsObject.getIds(getTextSearchResults(searchString, fto));
    }

    @Override
    public List<mlsObject> getTextSearchResults(String searchString, mlFilterTO fto) {
        try {
            List<Integer> resultIds = solrSearch(fto, searchString);
            if (resultIds.isEmpty()) {
                return new ArrayList<>();
            }
            Query q = em.createNamedQuery("mlsObject.findByIdRange");
            q.setParameter("ids", resultIds);
            Map<Integer, mlsObject> oMap = new HashMap<>();
            for (Object o : q.getResultList()) {
                mlsObject obj = (mlsObject) o;
                oMap.put(obj.getId(), obj);
            }
            List<mlsObject> results = new ArrayList<>();
            for (Integer id : resultIds) {
                if (oMap.get(id) != null) {
                    
                    // @todo find a better way to exclude non-searchable objects
                    boolean addIt = true;

                    if (oMap.get(id) instanceof MlsNews) {
                        addIt = false;
                    } else if (oMap.get(id) instanceof MlsImage) {
                        MlsImage img = (MlsImage) oMap.get(id);
                        if (img.getType() == ImageType.ProfilePicture || img.getType() == ImageType.Icon) {
                            addIt = false;
                        }
                    }
                    if (addIt) {
                        results.add(oMap.get(id));
                    }
                }

            }
            return results;
        } catch (SolrServerException ex) {
            Logger.getLogger(SearchManagerBean.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }

    /* Uses default filter */
    @Override
    public String fetchRelativesJson(int key, int level, MindlinerObjectJson root) {
        // default desktop filter only restricts in max confidentiality -> can also be used for web
        return fetchRelativesJson(key, level, root, createFilterForDesktop());
    }

    /* Enables custom options for filtering */
    @Override
    public String fetchRelativesJson(int key, int level, MindlinerObjectJson root, boolean includeArchived, boolean includePrivate) {
        BaseFilter filter = createFilterForDesktop();
        filter.setShowArchived(includeArchived);
        filter.setShowPrivateElements(includePrivate);
        filter.reconfigureEvaluators();
        return fetchRelativesJson(key, level, root, filter);
    }

    @Override
    public String fetchRelativesJson(int key, int level, MindlinerObjectJson root, BaseFilter filter) {
        mlsObject o = em.find(mlsObject.class, key);
        if (o == null || !objectManager.isAuthorizedForCurrentUser(o)) {
            return "";
        }
        root.addNode(null, o);
        if (level > 1) {
            buildSubTree(level - 1, root, o, filter);
        }
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(root);
        return json;
    }

    // Traverse tree in a DFS manner
    private void buildSubTree(int level, MindlinerObjectJson root, mlsObject parent, BaseFilter filter) {
        List<mlsObject> relatives = parent.getRelatives();
        List<mlsObject> filteredRelatives = filterRecords(relatives, filter);
        for (mlsObject obj : filteredRelatives) {
            if (objectManager.isAuthorizedForCurrentUser(obj)) {
                root.addNode(parent, obj);
                if (level > 1) {
                    buildSubTree(level - 1, root, obj, filter);
                }
            }
        }
    }

    /**
     * I don't use fetchRelatives(int key) because some functions (filter
     * creation) is too much overhead to be in a loop.
     *
     * @param ids The ids for which I need the relative ids
     * @return
     */
    @Override
    public Map<Integer, List<MltLink>> fetchRelativesMap(Collection<Integer> ids) {
        Map<Integer, List<MltLink>> map = new HashMap<>(ids.size(), 1.0F);
        BaseFilter filter = createFilterForDesktop();
        for (Integer i : ids) {
            List filtered = getFilteredLinks(i, filter);

            // if the parent object needs its relatives in particular order then sort the links now
            mlsObject o = em.find(mlsObject.class, i);
            if (o != null && o.isRelativesOrdered()) {
                Collections.sort(filtered, new LinkPositionComparatorTransfer());
            }
            map.put(i, filtered);
        }
        return map;
    }

    private List<MltLink> getFilteredLinks(int id, BaseFilter filter) {
        // get all object relatives/links
        Query nq = em.createNamedQuery("mlsObject.getObjectLinks");
        nq.setParameter("holderId", id);
        nq.setParameter("relativeType", Arrays.asList(LinkRelativeType.OBJECT, LinkRelativeType.CONTAINER_MAP));
        List<Object[]> records = nq.getResultList();
        if (records == null) {
            return new ArrayList<>();
        }
        List<mlsObject> relatives = new ArrayList<>();
        List<MlsLink> backLinks = new ArrayList<>();
        Map<Integer, MlsLink> links = new HashMap<>();
        for (Object[] item : records) {
            mlsObject obj = (mlsObject) item[0];
            MlsLink l = (MlsLink) item[1];
            // check if there is a backward link
            if (obj.getId() == id) {
                backLinks.add(l);
            } else {
                relatives.add((mlsObject) item[0]);
                links.put(l.getRelativeId(), l);
            }
        }
        // filter them
        List<mlsObject> filteredRelatives = filterRecords(relatives, filter);
        List<MltLink> filteredLinks = new ArrayList<>();
        for (mlsObject relative : filteredRelatives) {
            MlsLink l = links.get(relative.getId());
            MltLink tl = new MltLink(l);
            filteredLinks.add(tl);
            // only for the client: indicate whether there exists a backward link or not
            boolean isOneWay = true;
            for (MlsLink bl : backLinks) {
                if (bl.getHolderId() == l.getRelativeId()) {
                    isOneWay = false;
                    break;
                }
            }
            tl.setIsOneWay(isOneWay);
        }
        return filteredLinks;
    }

    @Override
    public List<MltObject> fetchMatchingObjects(String headline, String description) {
        Query nq = em.createNamedQuery("mlsObject.getByHeadlineAndDescription");
        nq.setParameter("headline", headline);
        nq.setParameter("description", description);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * This function is not exposed in the remote interface and only used for
     * server-side calls.
     *
     * @param objectId The id of the object for which the relatives are needed
     * @return A list of objects relatived to the specified one.
     */
    @Override
    public List<mlsObject> loadRelatives(int objectId, boolean includeArchived, boolean includePrivate) {
        try {
            mlsObject o = em.find(mlsObject.class, objectId);
            if (o != null) {
                // remove all text constraints for relation load
                BaseFilter filter = createDefaultFilter();
                filter.setShowArchived(includeArchived);
                filter.setShowPrivateElements(includePrivate);
                logManager.log(o.getClient(), MlsEventType.EventType.ObjectNavigated, o, 0, o.getHeadline(), "loadRelatives", mlsLog.Type.Info);
                return filterRecords(o.getRelatives(), filter);
            }
            return new ArrayList<>();
        } catch (OptimisticLockException ex) {
            Logger.getLogger(SearchManagerBean.class.getName()).log(Level.INFO, null, "Failure to merge an object: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public mlsObject getTagObject(String tag) {
        BaseFilter df = createDefaultFilter();
        try {
            mlFilterTO filter = new mlFilterTO(df);
            filter.setObjectType(MlClassHandler.MindlinerObjectType.Collection);
            List<Integer> hits = solrSearch(filter, MindlinerStripTag.getTagObjectHeadline(tag));
            if (hits.isEmpty()) {
                return null;
            }
            if (hits.size() > 1) {
                Logger.getLogger(SearchManagerBean.class.getName()).log(Level.SEVERE, null, "Found more than one tag object with tag " + MindlinerStripTag.getTagObjectHeadline(tag));
            }
            return em.find(mlsObjectCollection.class, hits.get(0));

        } catch (SolrServerException ex) {
            Logger.getLogger(SearchManagerBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private Set<Integer> currTargets;

    @Override
    public List<Integer> getShortestPath(Set<Integer> targets, Integer source, int maxDepth) {
        if (targets == null || targets.isEmpty()) {
            throw new IllegalArgumentException("Target list must not be null or empty");
        }
        if (maxDepth < 1) {
            throw new IllegalArgumentException("Max depth [" + maxDepth + "] must be at least one");
        }
        mlsObject srcObj = em.find(mlsObject.class, source);

        currTargets = targets;
        List<Integer> shortestPath = findShortestPath(null, srcObj, maxDepth);
        return shortestPath;
    }

    private List<Integer> findShortestPath(mlsObject parent, mlsObject current, int availableDepth) {
        List<Integer> shortestPath = null;
        for (mlsObject relative : current.getRelatives()) {
            if (parent == null || relative.getId() != parent.getId()) {
                if (currTargets.contains(relative.getId())) {
                    List<Integer> res = new ArrayList<>();
                    res.add(relative.getId());
                    return res;
                } else if (availableDepth >= 1) {
                    List<Integer> res = findShortestPath(current, relative, availableDepth - 1);
                    if (res != null && (shortestPath == null || shortestPath.size() >= res.size())) {
                        res.add(relative.getId());
                        shortestPath = res;
                    }
                }
            }
        }
        return shortestPath;
    }

    @Override
    public List<Integer> getIslandPeaks(int minIslandSize, int maxNumberOfIslands) {
        List<mlsObject> islandRatingPeaks = new ArrayList<>();
        if (minIslandSize < 2) {
            System.err.println("This function only returns islands with a minimum of two objects, adjusting minIslandSize parameter to 2.");
            minIslandSize = 2;
        }
        List<Island> islands = new ArrayList<>();
        for (mlsClient c : userManager.getCurrentUser().getClients()) {
            islands.addAll(c.getIslands());
        }
        Collections.sort(islands, Collections.reverseOrder(new IslandSizeComparator()));
        for (Island i : islands) {
            if (i.getObjects().size() >= minIslandSize) {
                Collections.sort(i.getObjects(), Collections.reverseOrder(new mlsRatingComparator()));
                islandRatingPeaks.add(i.getObjects().get(0));
            }
        }
        BaseFilter filter = createDefaultFilter();
        filter.setMaximumNumberOfDisplayElements(maxNumberOfIslands);
        filter.setDefaultSorting(BaseFilter.SortingMode.Rating);
        filter.reconfigureEvaluators();
        filter.setRecords(islandRatingPeaks);
        filter.removeNonconformingObjects();
        return mlsObject.getIds(filter.getRecords());
    }

    @Override
    public void ensureMyDueTasksAreOnWeekPlan(int year, int week) {
        Date startDate = WeekUtil.getWeekStart(year, week);
        Date endDate = WeekUtil.getWeekEnd(year, week);

        List<mlsTask> tasks = getCallerDueTasks(startDate, endDate);

        Query q = em.createNamedQuery("mlsWeekPlan.getPlanForYearAndWeek");
        q.setParameter("year", year);
        q.setParameter("week", week);
        List<mlsWeekPlan> plans = q.getResultList();
        if (plans.isEmpty()) {
            return;
        }
        if (plans.size() > 1) {
            Logger.getLogger(SearchManagerBean.class.getName()).log(Level.WARNING, null, "Found more than one week plan for specified year " + year + " and week " + week);
        }
        mlsWeekPlan plan = plans.get(0);
        for (mlsTask t : tasks) {
            if (!plan.getObjects().contains(t)) {
                plan.getObjects().add(t);
            }
        }
    }

    private List<mlsTask> getCallerDueTasks(Date startDate, Date endDate) {
        List<mlsTask> tasks = new ArrayList<>();
        try {
            SolrServer server = solrServer.getServer();
            SolrQuery parameters = new SolrQuery();
            parameters.addFilterQuery("dtype:TASK");

            StringBuilder query = new StringBuilder();
            DateFormat df = new SimpleDateFormat(SOLR_DATE_FORMAT);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            String start = df.format(startDate);
            String end = df.format(endDate);
            query.append(" +duedate:[").append(start).append(" TO ").append(end).append("]");
            query.append(" +owner_id:").append(userManager.getCurrentUser().getId());

            parameters.set("q", query.toString());
            parameters.set("rows", 1000); // safety
            parameters.set("fl", "id");

            // Get ID list from solr server
            QueryResponse res = server.query(parameters);
            for (SolrDocument item : res.getResults()) {
                Object id = item.getFieldValue("id");
                if (id instanceof String) {
                    try {
                        mlsTask task = em.find(mlsTask.class, Integer.valueOf((String) id));
                        if (task != null) {
                            tasks.add(task);
                        } else {
                            Logger.getLogger(SearchManagerBean.class.getName()).log(Level.WARNING, "Object with id [{0}] does exist in SOLR but not in SQL DB.", id);
                        }

                    } catch (NumberFormatException ex) {
                        Logger.getLogger(SearchManagerBean.class.getName()).log(Level.SEVERE, "Unexpected type conversion error", ex);
                    }
                }
            }
        } catch (SolrServerException ex) {
            Logger.getLogger(SearchManagerBean.class.getName()).log(Level.SEVERE, "SOLR exception while querying for due week tasks", ex);
        }
        return tasks;
    }

    @Override
    public List<Integer> getOverdueTasksIds() {
        return mlsObject.getIds((List) getOverdueTasks());
    }

    @Override
    public List<Integer> getUpcomingTasksIds(TimePeriod period) {
        return mlsObject.getIds((List) getUpcomingTasks(period));
    }

    @Override
    public List<mlsTask> getOverdueTasks() {
        Query q = em.createNamedQuery("mlsTask.getOverdue");
        q.setParameter("owner", userManager.getCurrentUser());
        q.setParameter("endDate", new Date());
        return q.getResultList();
    }

    @Override
    public List<mlsTask> getUpcomingTasks(TimePeriod period) {
        Query q = em.createNamedQuery("mlsTask.getUpcoming");
        q.setParameter("owner", userManager.getCurrentUser());
        Date start = new Date();
        q.setParameter("startDate", start);
        Date endDate = TimeFilter.applyOffset(start, true, period);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }

    @Override
    public List<Integer> getOpenPriorityTasksIds() {
        return mlsObject.getIds((List) getPriorityTasks());
    }

    @Override
    public List<mlsTask> getPriorityTasks() {
        Query q = em.createNamedQuery("mlsTask.getPriority");
        q.setParameter("owner", userManager.getCurrentUser());
        mlsPriority p = em.find(mlsPriority.class, mlsPriority.PRIORITY_HIGH);
        q.setParameter("priority", p);
        return q.getResultList();
    }

    @Override
    public List<mlsObject> getStandAloneObjects() {
        mlsUser cu = userManager.getCurrentUser();
        Query q = em.createQuery("SELECT o FROM mlsObject o WHERE o.archived = false AND o.relativeCount = 0 AND o.owner.id = " + cu.getId());
        List<mlsObject> objects = q.getResultList();
        // we don't need to check for confidentiality because only object owned by the caller are returned
        return objects;
    }

    @Override
    public List<Integer> getActiveStandAloneObjectIds() {
        return mlsObject.getIds((List) getStandAloneObjects());
    }

}
