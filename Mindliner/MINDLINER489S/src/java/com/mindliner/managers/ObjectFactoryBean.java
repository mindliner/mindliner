/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.analysis.TagParser;
import com.mindliner.categories.MlsEventType;
import com.mindliner.categories.MlsNewsType;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import com.mindliner.entities.MlUserPreferences;
import com.mindliner.entities.MlsNews;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsLog;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsRatingDetail;
import com.mindliner.entities.mlsTask;
import com.mindliner.entities.mlsUser;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.enums.ObjectReviewStatus;
import com.mindliner.exceptions.MlLinkException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.mlModifiedException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * This class creates a new MindlinerObject and makes sure that all required
 * fields are defined.
 *
 * @author Marius Messerli
 * @todo Replace JMS URL by configurable elements
 */
@Stateless
public class ObjectFactoryBean implements ObjectFactoryRemote, ObjectFactoryLocal {

    @PersistenceContext
    private EntityManager em;
    @EJB
    UserManagerLocal userManager;
    @EJB
    LinkManagerRemote linkManager;
    @EJB
    CategoryManagerRemote categoryManager;
    @EJB
    HeadlineParserRemote headlineParser;
    @EJB
    ObjectManagerRemote objectManager;
    @EJB
    SecurityManagerRemote securityManager;
    @EJB
    SolrServerBean solrServer;
    @EJB
    LogManagerLocal logManager;

    private mlsObject createObject(Class clazz, mlsClient dataPool, mlsPriority priority, mlsConfidentiality requestedConfidentiality,
            int relativeId, LinkRelativeType linkType, MlsNewsType newsType, String headline, String description, mlsUser user) {

        // Ensure that essential attributes are found somehow or throw
        MlUserPreferences defaults = em.find(MlUserPreferences.class, user.getId());
        if (priority == null && clazz == mlsTask.class) {
            if (defaults != null) {
                priority = defaults.getPriority();
            } else {
                mlsPriority normal = em.find(mlsPriority.class, mlsPriority.PRIORITY_NORMAL);
                priority = normal;
            }
        }

        if (dataPool == null) {
            if (defaults != null) {
                dataPool = defaults.getDataPool();
            } else {
                dataPool = user.getClients().get(0);
            }
        }
        assert dataPool != null : "Cannot create an object with data pool null";

        // if we are working with a single-confi datapool then make it the requested confi
        if (dataPool.getConfidentialities().size() == 1) {
            requestedConfidentiality = dataPool.getConfidentialities().get(0);
        }

        if (requestedConfidentiality == null) {
            if (defaults != null) {
                requestedConfidentiality = defaults.getConfidentiality(dataPool);
            }
            if (requestedConfidentiality == null) {
                List<mlsConfidentiality> confs = securityManager.getAllAllowedConfidentialities();
                for (mlsConfidentiality c : confs) {
                    if (c.getClient().equals(dataPool)) {
                        requestedConfidentiality = c;
                    }
                }
            }
        }

        try {
            mlsConfidentiality maxConfidentiality;
            mlsConfidentiality grantedConfidentiality;

            maxConfidentiality = user.getMaxConfidentiality(dataPool);
            if (maxConfidentiality == null) {
                throw new IllegalStateException("Max confidentiality is null for user " + user.getUserName() + " and data pool " + dataPool);
            }
            if (requestedConfidentiality == null
                    || requestedConfidentiality.compareTo(user.getMaxConfidentiality(dataPool)) > 0
                    || !user.getClients().contains(requestedConfidentiality.getClient())) {
                System.out.println("The requested confidentiality is not valid: Assigning users's max confidentiality instead: " + maxConfidentiality);
                grantedConfidentiality = maxConfidentiality;
            } else {
                grantedConfidentiality = requestedConfidentiality;
            }
            mlsObject newObject = (mlsObject) clazz.newInstance();
            newObject.setClient(dataPool);
            newObject.setOwner(user);
            newObject.setConfidentiality(grantedConfidentiality);
            newObject.setHeadline(headline);
            newObject.setDescription(description);
            newObject.setModificationDate(new Date());

            if (newObject instanceof mlsTask) {
                ((mlsTask) newObject).setPriority(priority);

            } else if (newObject instanceof MlsNews) {
                ((MlsNews) newObject).setNewsType(newsType);
            }
            mlsRatingDetail rd = new mlsRatingDetail();
            em.persist(rd);
            newObject.setRatingDetail(rd);
            em.persist(newObject);
            em.flush();

            mlsObject relative = em.find(mlsObject.class, relativeId);
            if (relative != null && relativeId >= 0) {
                linkManager.link(newObject.getId(), relative.getId(), false, linkType);
            }

            // to prevent loop-back we don't communicate the creation of news objects
            if (!(newObject instanceof MlsNews)) {
                logManager.log(dataPool, MlsEventType.EventType.ObjectCreated, newObject, 0, headline, "createLocal", mlsLog.Type.Create);
                solrServer.addObject(newObject, true);
                // make sure to send the message only after the new object is fully built
                MlMessageHandler mh = new MlMessageHandler();
                mh.sendMessage(user, newObject, MlMessageHandler.MessageEventType.OBJECT_CREATION_EVENT, "");
                mh.closeConnection();
            }
            return newObject;
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ObjectFactoryBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (MlLinkException | NonExistingObjectException ex) {
            Logger.getLogger(ObjectFactoryBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    @Override
    public mlsObject createLocal(Class clazz, mlsClient dataPool, mlsPriority priority, mlsConfidentiality requestedConfidentiality,
            int relativeId, LinkRelativeType linkType, MlsNewsType newsType, String headline, String description) {
        return createObject(clazz, dataPool, priority, requestedConfidentiality, relativeId, linkType, newsType, headline, description, userManager.getCurrentUser());
    }

    @Override
    public mlsObject createNewsRecord(mlsClient client, mlsConfidentiality confidentiality, String headline, String description, mlsUser user) {
        Query q = em.createNamedQuery("MlsNewsType.findByTag");
        q.setParameter("tag", "SUBSCRIPTION");
        MlsNewsType newsType = (MlsNewsType) q.getSingleResult();
        return createObject(MlsNews.class, client, null, confidentiality, -1, LinkRelativeType.OBJECT, newsType, headline, description, user);
    }

    @Override
    public int[] create(Class clazz,
            int clientId,
            int confidentialityId,
            Date creationDate,
            int priorityId,
            boolean privateAccess,
            int relativeId,
            String headline,
            String description,
            ObjectReviewStatus status,
            LinkRelativeType linkType,
            MlsNewsType newsType) {

        mlsPriority priority = em.find(mlsPriority.class, priorityId);
        mlsClient dataPool = em.find(mlsClient.class, clientId);
        mlsConfidentiality confidentiality = em.find(mlsConfidentiality.class, confidentialityId);

        mlsObject newObject = createLocal(clazz, dataPool, priority, confidentiality, relativeId, linkType, newsType, headline, description);
        if (newObject == null) {
            int[] res = new int[2];
            res[0] = -1;
            res[1] = -1;
            return res;
        }
        newObject.setPrivateAccess(privateAccess);
        newObject.setStatus(status);
        if (newObject instanceof mlsTask) {
            ((mlsTask) newObject).setPriority(priority);
        }
        newObject.setHeadline(headline);
        newObject.setDescription(description);
        newObject.setCreationDate(creationDate);
        try {
            // if headline contains a tag then call the parser
            TagParser tp = new TagParser(headline);
            if (!tp.getMindlinerTagOccurances().isEmpty()) {
                headlineParser.updateHeadline(newObject.getId(), headline);
            }
        } catch (mlModifiedException | NonExistingObjectException ex) {
            Logger.getLogger(ObjectFactoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        int[] res = new int[3];
        res[0] = newObject.getId();
        res[1] = newObject.getVersion();
        res[2] = -1;

        if (relativeId >= 0) {
            mlsObject rel = em.find(mlsObject.class, relativeId);
            if (rel != null) {
                res[2] = rel.getVersion();
            }
        }
        solrServer.addObject(newObject, true);
        return res;
    }
}
