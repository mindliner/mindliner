/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.MlsEventType.EventType;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.entities.MlUserPreferences;
import com.mindliner.entities.MlsNews;
import com.mindliner.entities.MlsSubscription;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsLog;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.NonExistingObjectException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * This class manages the definition and delivery of subscriptions.
 *
 * @author Marius Messerli
 */
@Stateless
@DeclareRoles(value = {"Admin", "User", "MasterAdmin"})
@RolesAllowed(value = {"User"})
public class SubscriptionManager implements SubscriptionManagerLocal, SubscriptionManagerRemote {

    @PersistenceContext
    private EntityManager em;
    // the time window (in ms) within which all udpates on an object are considered belonging to a single piece of work
    // this should prevent multiple news records for the same thing (e.g. the initial set of object edits)
    private static final int HUMAN_TRANSACTION_WINDOW = 10 * 60 * 1000;

    @EJB
    UserManagerLocal userManager;

    @EJB
    ObjectFactoryLocal objectFactory;

    @Override
    @RolesAllowed(value = {"User"})
    public void subscribe(EventType event, mlsObject object, mlsUser actor, boolean reverse) {
        mlsUser u = userManager.getCurrentUser();
        if (object != null) {
            object = em.merge(object);
        }
        if (actor != null) {
            actor = em.merge(actor);
        }
        if (!isSubscriptionExisting(u, event, object, actor, reverse)) {
            MlsSubscription s = new MlsSubscription(u, event, object, actor, reverse);
            em.persist(s);
        }
    }

    private boolean isSubscriptionExisting(mlsUser u, EventType event, mlsObject object, mlsUser actor, boolean reverse) {
        Query subscriptionsQuery = em.createNamedQuery("MlsSubscription.findAllForUser");
        subscriptionsQuery.setParameter("userId", u.getId());
        List<MlsSubscription> subscriptions = subscriptionsQuery.getResultList();
        boolean existing = false;
        for (MlsSubscription s : subscriptions) {
            if (s.getEventType().equals(event)
                    && (object == null && s.getObject() == null || object.equals(s.getObject()))
                    && (actor == null && s.getActor() == null || actor.equals(s.getActor()))
                    && reverse == s.isReverse()) {
                existing = true;
            }
        }
        return existing;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public void deleteAllSubscriptions(mlsUser actor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RolesAllowed(value = {"User"})
    public void deleteAllSubscriptions(mlsObject object) {
        if (object == null) {
            return;
        }
        String queryString
                = "DELETE FROM MlsSubscription s "
                + "WHERE s.user.id = " + userManager.getCurrentUser().getId()
                + " AND s.object.id = " + object.getId();
        em.createQuery(queryString).executeUpdate();
    }

    @Override
    @RolesAllowed(value = {"User"})
    public void deleteAllSubscriptions() {
        String queryString = "DELETE FROM MlsSubscription s WHERE s.user.id = " + userManager.getCurrentUser().getId();
        em.createQuery(queryString).executeUpdate();
    }

    @Override
    @RolesAllowed(value = {"User"})
    public void deleteAllSubscriptions(EventType event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RolesAllowed(value = {"User"})
    public void deleteSubscription(int id) throws ForeignOwnerException {
        MlsSubscription s = em.find(MlsSubscription.class, id);
        if (!s.getUser().equals(userManager.getCurrentUser())) {
            throw new ForeignOwnerException("Caller does not own subscription with id " + id + ". Ignored deletion request");
        }
        em.remove(s);
    }

    @Override
    @RolesAllowed(value = {"User"})
    public boolean isSubscribed(mlsObject o, EventType eventType, mlsUser actor) {
        if (o != null) {
            Query q = em.createNamedQuery("MlsSubscription.findByObjectAndUser");
            q.setParameter("objectId", o.getId());
            q.setParameter("userId", userManager.getCurrentUser().getId());
            List<MlsSubscription> subscriptions = q.getResultList();
            for (MlsSubscription s : subscriptions) {
                if (!s.isReverse()
                        && (eventType.equals(EventType.Any) || eventType.equals(s.getEventType()))
                        && (actor == null || actor.equals(s.getActor()))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public List<MlsSubscription> getAllSubscriptions() {
        Query q = em.createNamedQuery("MlsSubscription.findAllForUser");
        q.setParameter("userId", userManager.getCurrentUser().getId());
        return q.getResultList();
    }

    @Override
    @RolesAllowed(value = {"User"})
    public MlsSubscription getSubscription(int id) throws ForeignOwnerException, NonExistingObjectException {
        MlsSubscription s = em.find(MlsSubscription.class, id);
        if (s == null) {
            throw new NonExistingObjectException("Object with id " + id + " does not exist.");
        }
        if (!s.getUser().equals(userManager.getCurrentUser())) {
            throw new ForeignOwnerException("Caller does not own subscription with id " + id + ". Ignored deletion request");
        }
        return s;
    }

    private boolean checkAuthorizations(NewsCandidate nc) {

        // if the log record does not belong to one of the user's data pools then fail
        if (nc.getUserPrefs() == null || !nc.getUserPrefs().getUser().getClients().contains(nc.getLogRecord().getDataPool())) {
            return false;
        }

        if (nc.getLogObject() == null) {
            // @todo in case we have an ObjectDeleted event we don't have a confi as the log record does not provide one.....  FIX THIS
            return true;
        } else if (nc.getLogObject().getOwner().equals(nc.getUserPrefs().getUser())) {
            return true;
        } else {
            // the log refers to a forein object
            if (nc.getLogObject().getPrivateAccess()) {
                return false;
            }
            mlsConfidentiality userClientMaxConfi = nc.getUserPrefs().getUser().getMaxConfidentiality(nc.getLogObject().getClient());
            return userClientMaxConfi != null && userClientMaxConfi.compareTo(nc.getLogObject().getConfidentiality()) >= 0;
        }
    }

    /**
     * This method may be called by a timer expiration and so the current cannot
     * be used when assigning the new object's user, data pool and
     * confidentiality.
     *
     * If a logobject is available the dp and confi are taken from that one.
     */
    private MlsNews createNewsRecord(NewsCandidate nc, mlsUser subscriber) {
        mlsClient dataPool;
        mlsConfidentiality confidentiality;
        if (!checkAuthorizations(nc)) {
            return null;
        }

        if (nc.getLogObject() != null) {
            dataPool = nc.getLogObject().getClient();
            confidentiality = nc.getLogObject().getConfidentiality();
        } else {
            dataPool = nc.getLogRecord().getDataPool();
            confidentiality = nc.getUserPrefs().getConfidentiality(dataPool);
            if (confidentiality == null) {
                confidentiality = nc.getUserPrefs().getUser().getMaxConfidentiality(dataPool);
            }
        }

        MlsNews news = (MlsNews) objectFactory.createNewsRecord(dataPool, confidentiality, nc.getLogRecord().getHeadline(), nc.getLogRecord().getDescription(), subscriber);
        if (news != null) {
            news.setClient(dataPool);
            news.setConfidentiality(confidentiality);
            news.setLog(nc.getLogRecord());
            news.setOwner(nc.getUserPrefs().getUser());

            // in case we have a log object we use the log timestamp as news creation
            if (nc.getLogRecord() != null) {
                news.setCreationDate(nc.getLogRecord().getTime());
            }
            return news;
        }
        return null;
    }

    @Override
    @Schedule(minute = "*/30", hour = "*")
    public void deliverSubscriptions() {
        Query q = em.createNamedQuery("MlsSubscription.findAll");
        List<MlsSubscription> subscriptions = q.getResultList();
        Set<mlsUser> subscribers = new HashSet<>();
        for (MlsSubscription s : subscriptions) {
            if (!subscribers.contains(s.getUser())) {
                subscribers.add(s.getUser());
            }
        }
        System.out.println("Delivering news for " + subscribers.size() + " subscribers");

        // now run a digest for each user that has at least one subscription as their intervall may be different
        for (mlsUser subscriber : subscribers) {
            MlUserPreferences userPref = userManager.getUserPreferences(subscriber.getId());
            Date start = userPref == null ? null : userPref.getNewsLastDigest();
            if (start == null) {
                System.out.println("missing subscription deivery. Using current time for user " + subscriber);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, -1);
                start = cal.getTime();
            }
            q = em.createNamedQuery("mlsLog.findAfterStart");
            q.setParameter("startTime", start);

            List<mlsLog> newLogRecords = q.getResultList();
//            System.out.println("analyzing " + newLogRecords.size() + " log records after: " + start);

            if (!newLogRecords.isEmpty()) {
                List<NewsCandidate> newsCandidates = new ArrayList<>();
                List<MlsSubscription> reverseSubs = new ArrayList<>();
                for (MlsSubscription s : subscriptions) {
                    if (s.getUser().equals(subscriber)) {
                        if (s.isReverse()) {
                            reverseSubs.add(s);
                        } else {
                            for (mlsLog logRecord : newLogRecords) {
                                mlsObject logObject = em.find(mlsObject.class, logRecord.getObjectId());
                                // the test for logObject != null will be dropped later as we want to create news for events that don't have an object, too
                                if (logObject != null
                                        // object match or wildcard
                                        && (s.getObject() == null || s.getObject().equals(logObject))
                                        // type match or wildcard
                                        && (s.getEventType().equals(EventType.Any) || s.getEventType().equals(logRecord.getEventType()))
                                        // actor match or wildcard
                                        && (s.getActor() == null || s.getActor().equals(logRecord.getUser()))
                                        // user is the object is still in one of subscriber's data pools (note: object might have been moved to another data pool in the meantime)
                                        && (subscriber.getClients().contains(logRecord.getDataPool()) && (logObject == null || subscriber.getClients().contains(logObject.getClient())))
                                        // prevent news on foreign private objects, note: the unnecessary test for logObject is left here because in the future we want to create news for logObject == null
                                        && (logObject == null || (!logObject.getPrivateAccess() || logObject.getOwner().equals(subscriber)))
                                        // prevent news on classified objects, note: the unnecessary test for logObject is left here because in the future we want to create news for logObject == null
                                        && (logObject == null || logObject.getConfidentiality().compareTo(subscriber.getMaxConfidentiality(logObject.getClient())) <= 0)) {
                                    NewsCandidate nc = new NewsCandidate(userPref, logRecord, logObject);
                                    // the following call works OK for new objects, see NewsCandidate.equals()
                                    if (!newsCandidates.contains(nc)) {
                                        newsCandidates.add(nc);
                                    }
                                }
                            }
                        }
                    }
                }
                processRejections(reverseSubs, newsCandidates);
                newsCandidates = consolidateNews(newsCandidates);
                for (NewsCandidate nc : newsCandidates) {
                    createNewsRecord(nc, subscriber);
                }
            }
            userManager.updateLastNewsDeliveryDigest(subscriber.getId());
            System.out.println("Subscription delivery completed normally.");
        }
    }

    /**
     * This function checks if probe is already represented in the list.
     * Represented means that a news record covers the same object and the same
     * event within a timeframe short enough to be considered the same
     * transaction.
     *
     * @param list The list to be updated if required
     * @param probe The new object which is to be inserted or swapped into the
     * list
     */
    private void insertOrSwapin(List<NewsCandidate> list, NewsCandidate probe) {
        if (list.isEmpty()) {
            list.add(probe);
            return;
        }
        boolean replaced = false;
        for (int i = list.size() - 1; !replaced && i >= 0; i--) {
            NewsCandidate n = list.get(i);
            if ( // do both candidates have a log object?
                    n.getLogObject() != null && probe.getLogObject() != null
                    // are these equal?
                    && n.getLogObject().equals(probe.getLogObject())
                    // are the log event types equal?
                    && probe.getLogRecord().getEventType().equals(n.getLogRecord().getEventType())
                    // are both candidates within the timeframe considered to be one action?
                    && Math.abs(probe.getLogRecord().getTime().getTime() - n.getLogRecord().getTime().getTime()) < HUMAN_TRANSACTION_WINDOW) {

                // if probe is newer then replace the news item on the list with the probe, otherwise do nothing
                if (probe.getLogRecord().getTime().getTime() > n.getLogRecord().getTime().getTime()) {
                    // remove the older of the two seemingly redundant elements
                    list.remove(i);
                    // add the newer of the two seemingly redundant elements
                    list.add(i, probe);
                    replaced = true;
                }
            }
        }
        if (!replaced) {
            list.add(probe);
        }
    }

    /**
     * This function removes identical modifications that happen within a
     * timeframe that is considered a single work transaction. For example, a
     * user might change the headline three times in 2 minutes so we only need
     * to know the last one.
     *
     * @param reverseSubs
     * @param newsCandidates
     */
    private List<NewsCandidate> consolidateNews(List<NewsCandidate> newsCandidates) {
        List<NewsCandidate> consolidatedNewsCandidates = new ArrayList<>();
        newsCandidates.stream().forEach((s) -> {
            insertOrSwapin(consolidatedNewsCandidates, s);
        });
        return consolidatedNewsCandidates;
    }

    private void processRejections(List<MlsSubscription> reverseSubs, List<NewsCandidate> newsCandidates) {
        if (reverseSubs.isEmpty()) {
            return;
        }

        for (MlsSubscription rs : reverseSubs) {
            for (Iterator it = newsCandidates.iterator(); it.hasNext();) {
                NewsCandidate nc = (NewsCandidate) it.next();
                if (rs.getActor() == null) {
                    if (rs.getEventType().equals(EventType.Any)) {
                        // object-specific rejection
                        if (rs.getObject().equals(nc.getLogObject())) {
                            it.remove();
                        }
                    } else if (rs.getObject() == null) {
                        // type-specific rejection
                        if (nc.getLogRecord() != null && rs.getEventType().equals(nc.getLogRecord().getEventType())) {
                            it.remove();
                        }
                    } else // type and object specific rejection
                    {
                        if (rs.getObject().equals(nc.getLogObject()) && (nc.getLogRecord() != null && rs.getEventType().equals(nc.getLogRecord().getEventType()))) {
                            it.remove();
                        }
                    }
                } else if (rs.getEventType().equals(EventType.Any)) {
                    if (rs.getObject() == null) {
                        // actor-specific rejection
                        if (rs.getActor().equals(nc.getActor())) {
                            it.remove();
                        }
                    } else // actor and object-specific rejection
                    {
                        if (rs.getObject().equals(nc.getLogObject()) && rs.getActor().equals(nc.getActor())) {
                            it.remove();
                        }
                    }
                } else if (rs.getObject() == null) {
                    // actor and event-specific rejection
                    if (rs.getActor().equals(nc.getActor()) && (nc.getLogRecord() != null && rs.getEventType().equals(nc.getLogRecord().getEventType()))) {
                        it.remove();
                    }
                } else // actor, type, and object-specific rejection
                {
                    if (rs.getActor().equals(nc.getActor())
                            && (nc.getLogRecord() != null && nc.getLogRecord().getEventType().equals(rs.getEventType()))
                            && rs.getObject().equals(nc.getLogObject())) {
                        it.remove();
                    }
                }
            }
        }
    }

    @Override
    public List<MlsNews> getNews() {
        Query q = em.createNamedQuery("MlsNews.findActiveByUser");
        q.setParameter("owner", userManager.getCurrentUser());
        return q.getResultList();
    }

    @Override
    public void archiveNewsArticle(MlsNews news) {
        em.merge(news);
        news.setArchived(true);
        em.flush();
    }

    @Override
    public List<Integer> getNewsIds() {
        List<MlsNews> news = getNews();
        return mlsObject.getIds((List) news);
    }

    @Override
    public void archiveNewsArticle(int newsId) {
        MlsNews n = em.find(MlsNews.class, newsId);
        if (n != null) {
            n.setArchived(true);
        }
    }
}
