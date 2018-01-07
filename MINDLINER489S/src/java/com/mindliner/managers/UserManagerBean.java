/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.analysis.CurrentWorkTask;
import com.mindliner.categories.MlsEventType;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import com.mindliner.common.ClientIdsStringBuilder;
import com.mindliner.entities.EntityRefresher;
import com.mindliner.entities.MlAuthenticationGroups;
import com.mindliner.entities.MlObjectDefaultsConfidentialities;
import com.mindliner.entities.MlUserPreferences;
import com.mindliner.entities.MlsColorScheme;
import com.mindliner.entities.MlsNews;
import com.mindliner.entities.MlsSubscription;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.entities.Syncher;
import com.mindliner.entities.Synchunit;
import com.mindliner.entities.UserClientLink;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsLog;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsTask;
import com.mindliner.entities.mlsUser;
import com.mindliner.entities.mlsWorkUnit;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.UserCreationException;
import com.mindliner.objects.transfer.MltObjectDefaultConfidentialities;
import com.mindliner.objects.transfer.mltClient;
import com.mindliner.objects.transfer.mltUser;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Marius Messerli
 */
@Stateless
@ManagedBean
@DeclareRoles(value = {"Admin", "User", "MasterAdmin"})
public class UserManagerBean implements UserManagerRemote, UserManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @Resource
    private SessionContext sc;
    @EJB
    ObjectManagerLocal objectManager;
    @EJB
    UserCacheBean userCache;
    @EJB
    LogManagerLocal logManager;
    @EJB
    SolrServerBean solrServer;
    @EJB
    CategoryManagerRemote categoryManager;

    @RolesAllowed(value = {"Admin", "MasterAdmin"})
    @Override
    public void setActive(int key, boolean active) {
        mlsUser u = em.find(mlsUser.class, key);
        if (u != null) {
            u.setActive(active);
        }
    }

    @Override
    public mlsClient createClient(
            String clientName,
            mlsUser adminUser) {

        // adminUser was serialized due to remote bean call (see ClientBB.saveClient()), therefore we need to reattach the entity.
        // otherwise the cached entity will not be updated
        adminUser = em.merge(adminUser);

        // create a "team" confidentiality by default
        mlsConfidentiality defaultConfidentiality = new mlsConfidentiality(10, "team");

        mlsClient client = new mlsClient();
        client.setActive(true);
        client.setName(clientName);
        client.setOwner(adminUser);
        defaultConfidentiality.setClient(client);
        client.getConfidentialities().add(defaultConfidentiality);
        em.persist(client);
        em.persist(defaultConfidentiality);
        em.flush();

        // create a link between the new client and the admin user
        UserClientLink userClientLink = new UserClientLink(adminUser.getId(), client.getId());
        userClientLink.setMaxConfidentiality(defaultConfidentiality);
        em.persist(userClientLink);
        em.flush();
        EntityRefresher.updateCachedEntity(em, client.getId(), client);
        EntityRefresher.updateCachedEntity(em, adminUser.getId(), adminUser);

        MlMessageHandler mh = new MlMessageHandler();
        mh.sendMessage(getCurrentUser(), null, MlMessageHandler.MessageEventType.CLIENT_CREATION_EVENT, clientName);
        mh.closeConnection();
        logManager.log(client, MlsEventType.EventType.ClientCreated, null, 0, client.getName(), "createClient", mlsLog.Type.Info);

        return client;
    }

    /**
     * Removes all confidentialities for the speicified client. Ensure that none
     * of the confidentialities is in use.
     *
     * @param managedClient The client which must be a "managed object"
     * (following an em.find or em.merge)
     */
    private void removeConfidentialities(mlsClient managedClient) {
        Query q = em.createNamedQuery("mlsConfidentiality.removeForClient");
        q.setParameter("client", managedClient);
        q.executeUpdate();
    }

    private void removeObjectDefaults(mlsClient managedClient) {
        Query q = em.createNamedQuery("objectdefaultsconfidentialities.deleteByClient");
        q.setParameter("client", managedClient);
        q.executeUpdate();
    }

    private void removeSynchUnits(mlsUser user) {
        Query q = em.createNamedQuery("Syncher.findByUserId");
        q.setParameter("userId", user.getId());
        List<Syncher> synchers = q.getResultList();
        Iterator<Syncher> it = synchers.iterator();
        while (it.hasNext()) {
            Syncher s = it.next();
            Iterator<Synchunit> synchUnitIterator = s.getSynchUnits().iterator();
            while (synchUnitIterator.hasNext()) {
                Synchunit su = synchUnitIterator.next();
                em.remove(su);
                synchUnitIterator.remove();
            }
            em.remove(s);
        }
    }

    private void removeColorSchemes(mlsUser u) {
        Query q = em.createNamedQuery("MlsColorScheme.getByUser");
        q.setParameter("ownerId", u.getId());
        List<MlsColorScheme> schemes = q.getResultList();
        for (Iterator it = schemes.iterator(); it.hasNext();) {
            MlsColorScheme s = (MlsColorScheme) it.next();
            it.remove();
            em.remove(s);
        }
    }

    private void removeIslands(mlsClient managedClient) {
        Query q = em.createNamedQuery("Island.deleteForClient");
        q.setParameter("clientId", managedClient.getId());
        q.executeUpdate();
    }

    @RolesAllowed(value = {"MasterAdmin"})
    @Override
    public void deleteClient(int clientId) {
        mlsClient client = em.find(mlsClient.class, clientId);
        if (client != null) {
            logManager.log(client, MlsEventType.EventType.ClientDeleted,
                    null, 0, "deleting client " + client.getName(), "deleteClient", mlsLog.Type.Info);
            removeObjects(client);
            removeLinks(client);
            removeConfidentialities(client);
            removeClientUserLinks(client, clientId);
            removeIslands(client);
            removeObjectDefaults(client);
            deleteLog(client);

            List<mlsUser> usersToRefresh = new ArrayList<>();

            // remove users if they only are a member of this one client
            for (Iterator uit = client.getUsers().iterator(); uit.hasNext();) {
                mlsUser u = (mlsUser) uit.next();
                if (u.getClients().size() == 1) {
                    Logger.getLogger(UserManagerBean.class.getName()).log(
                            Level.INFO,
                            "Removing user {0} who's only membership is in data pool {1} which is being deleted too.",
                            new Object[]{u.getUserName(), client.getName()}
                    );
                    removeSynchUnits(u);
                    removeColorSchemes(u);
                    removeSubscriptions(u);
                    removeNews(u);
                    uit.remove();
                    em.remove(u);
                } else {
                    usersToRefresh.add(u);
                }
            }
            em.remove(client);

            for (mlsUser u : usersToRefresh) {
                EntityRefresher.updateCachedEntity(em, u.getId(), u);
            }
        }
    }

    private void deleteLog(mlsClient client) {
        Query q = em.createNamedQuery("mlsLog.deleteForClient");
        q.setParameter("client", client);
        q.executeUpdate();
    }

    private void removeClientUserLinks(mlsClient client, int clientId) {
        for (mlsUser u : client.getUsers()) {
            Query q = em.createNamedQuery("UserClientLink.remove");
            q.setParameter("clientId", clientId);
            q.setParameter("userId", u.getId());
        }
    }

    /**
     * We cannot use the normal removal mechanism as it is too slow and we don't
     * need to update many things as we know the entire client goes with all
     * objects.
     *
     * @param client The client who's objects are to be deleted
     */
    private void removeObjects(mlsClient client) {
        Query q = em.createNamedQuery("mlsObject.getObjectsForClient");
        q.setParameter("clientId", client.getId());
        for (Iterator it = q.getResultList().iterator(); it.hasNext();) {
            mlsObject o = (mlsObject) it.next();
            solrServer.removeObject(o, false);
            if (o instanceof mlsTask) {
                mlsTask t = (mlsTask) o;
                for (Iterator wit = t.getWorkUnits().iterator(); wit.hasNext();) {
                    mlsWorkUnit w = (mlsWorkUnit) wit.next();
                    wit.remove();
                    em.remove(w);
                }
            }
            it.remove();
            em.remove(o);
        }
        solrServer.commit();
    }

    private void removeLinks(mlsClient client) {
        Query q;
        // remove all the links belonging to the client
        q = em.createNamedQuery("MlsLink.removeLinksForClient");
        q.setParameter("client", client);
        q.executeUpdate();
    }

    private void removeNews(mlsUser u) {
        Query q;
        Iterator<mlsObject> it;
        q = em.createNamedQuery("MlsNews.findActiveByUser");
        q.setParameter("owner", u);
        for (it = q.getResultList().iterator(); it.hasNext();) {
            MlsNews n = (MlsNews) it.next();
            it.remove();
            em.remove(n);
        }
    }

    private void removeSubscriptions(mlsUser u) {
        Query q;
        q = em.createNamedQuery("MlsSubscription.findAllForUser");
        q.setParameter("userId", u.getId());
        for (Iterator it = q.getResultList().iterator(); it.hasNext();) {
            MlsSubscription s = (MlsSubscription) it.next();
            it.remove();
            em.remove(s);
        }
    }

    @RolesAllowed(value = "MasterAdmin")
    @Override
    public mltUser findUserRemote(String login) {
        mlsUser u = findUser(login);
        if (u == null) {
            return null;
        }
        return new mltUser(u);
    }

    @Override
    public mlsUser findUser(String login) {
        Query nq = em.createNamedQuery("mlsUser.getUserByUserName");
        nq.setParameter("userName", login);
        List resultList = nq.getResultList();
        if (resultList.size() > 0) {
            return (mlsUser) resultList.get(0);
        }
        return null;
    }

    @Override
    public mlsUser findUser(int userId) {
        return em.find(mlsUser.class, userId);
    }

    @RolesAllowed(value = {"Admin", "MasterAdmin"})
    @Override
    public List<mltUser> getUsersForClient(int clientId) {
        mlsClient client = em.find(mlsClient.class, clientId);
        if (client != null) {
            List<mltUser> tusers = new ArrayList<>();
            for (mlsUser u : client.getUsers()) {
                tusers.add(new mltUser(u));
            }
            return tusers;
        }
        return null;
    }

    @RolesAllowed(value = "MasterAdmin")
    @Override
    public List<mltClient> getAllClients() {
        List<mltClient> ctlist = new ArrayList<>();
        for (mlsClient c : getClients()) {
            ctlist.add(new mltClient(c));
        }
        return ctlist;
    }

    @Override
    public mlsClient getOneClient(int clientId) {
        return em.find(mlsClient.class, clientId);
    }

    @RolesAllowed(value = {"MasterAdmin", "Admin"})
    @Override
    public List<mlsClient> getClients() {
        Query q = em.createNamedQuery("mlsClient.findAll");
        return q.getResultList();
    }

    @Override
    public List<mlsClient> getOwnedClients() {
        Query q = em.createNamedQuery("mlsClient.findByOwner");
        q.setParameter("owner", getCurrentUser());
        return q.getResultList();
    }

    @Override
    public int getCurrentUserId() {
        mlsUser currentUser = getCurrentUser();
        return currentUser.getId();
    }

    @Override
    public boolean isInRole(String role) {
        return sc.isCallerInRole(role);
    }

    private mlsUser updateUserClientMaxConfidentiality(mlsUser u, mlsClient client, mlsConfidentiality conf) {
        // first remove the max confi for the user and client if exists
        Iterator it = u.getMaxConfidentialities().iterator();
        for (; it.hasNext();) {
            mlsConfidentiality userConfi = (mlsConfidentiality) it.next();
            if (userConfi.getClient().equals(client)) {
                it.remove();
                break;
            }
        }
        u.getMaxConfidentialities().add(conf);
        return u;
    }

    @RolesAllowed(value = {"MasterAdmin"})
    @Override
    public void setMaxConfidentiality(int userId, int confId, int clientId) {
        mlsUser u = em.find(mlsUser.class, userId);
        if (u == null) {
            throw new IllegalArgumentException("The specified user does not exist");
        }
        mlsClient client = em.find(mlsClient.class, clientId);
        if (client == null) {
            throw new IllegalArgumentException("The specified client does not exist");
        }
        mlsConfidentiality newConfi = em.find(mlsConfidentiality.class, confId);
        if (newConfi == null) {
            throw new IllegalArgumentException("The specified confidentiality does not exist");
        }
        u = updateUserClientMaxConfidentiality(u, client, newConfi);
        em.merge(u);
    }

    @Override
    public void setContactDetails(String firstName, String lastName, String email) {
        mlsUser u = getCurrentUser();
        if (u != null) {
            u.setFirstName(firstName);
            u.setLastName(lastName);
            u.setEmail(email);
        }
    }

    @Override
    public void heartBeat() {
        Principal p = sc.getCallerPrincipal();
        int id = userCache.getUserId(p.getName());
        userCache.setLastSeen(id, new Date());
    }

    @RolesAllowed(value = {"MasterAdmin"})
    @Override
    public List<mltUser> getAllLoggedInUsers() {
        Query q = em.createNamedQuery("mlsUser.getActiveUsers");
        List<mlsUser> users = q.getResultList();
        List<mltUser> tusers = new ArrayList<>();
        Date now = new Date();
        for (mlsUser u : users) {
            Date lastSeen = userCache.getLastSeen(u.getId());
            if (lastSeen == null) {
                lastSeen = u.getLastSeen();
            }
            if (lastSeen != null && now.getTime() - lastSeen.getTime() < 2 * HEARTBEAT_INTERVALL) {
                tusers.add(new mltUser(u));
            }
        }
        return tusers;
    }

    /**
     * This function returns the current user after a successful login. It also
     * updates the login count, and the last login and last logout time stamps.
     *
     * @return The current user.
     * @todo add a proper exception handling
     */
    @Override
    @RolesAllowed(value = {"Admin", "User"})
    public mltUser login() {
        Principal p = sc.getCallerPrincipal();
        int id = userCache.getUserId(p.getName());
        mlsUser u = em.find(mlsUser.class, id);
        u.setLoginCount(u.getLoginCount() + 1);
        u.setLastLogout(u.getLastSeen());
        u.setLastLogin(new Date());
        return new mltUser(u);
    }

    @Override
    public mlsUser createUnconfirmedUser(
            String login,
            String firstName,
            String lastName,
            String email,
            String encryptedPassword) throws UserCreationException {

        mlsUser u = new mlsUser();
        u.setUserName(login);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPassword(encryptedPassword);

        //user has to be confirmed first, add it to the right access group
        u.setActive(false);
        Query q = em.createNamedQuery("MlAuthenticationGroups.findUnconfirmedGroup");
        MlAuthenticationGroups g = (MlAuthenticationGroups) q.getSingleResult();

        //add user to authentication group and not the other way round, otherwise the website will not be updated correctly
        g.getUsers().add(u);

        //persist user
        em.persist(u);
        Logger.getLogger(UserManagerBean.class.getName()).log(Level.INFO, "New user created: name={0}", u.getUserName());
        return u;
    }

    @Override
    public String isUsernameAvailable(String username) {
        mlsUser user = findUser(username);
        if (user == null) {
            return username;
        }
        for (int i = 0; i < 10; i++) {
            String suggestion = username + Integer.toString(i);
            System.out.println("user name " + username + " not available - trying " + suggestion);
            user = findUser(suggestion);
            if (user == null) {
                return suggestion;
            }
        }
        return "";
    }

    @Override
    public mlsUser createUser(String username, String firstName, String lastName, String email, mlsClient client, mlsConfidentiality maxConfidentiality, String encryptedPassword) throws UserCreationException {
        // ensure that we don't duplicate entities when values come from a possibly detached state
        if (client != null) {
            client = em.merge(client);
            maxConfidentiality = em.merge(maxConfidentiality);
            if (maxConfidentiality == null) {
                throw new UserCreationException("Could not find the specified confidentiality.");
            }
        }
        mlsUser u = new mlsUser();
        u.setUserName(username);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPassword(encryptedPassword);

        Query q = em.createNamedQuery("MlAuthenticationGroups.findUserGroup");
        MlAuthenticationGroups g = (MlAuthenticationGroups) q.getSingleResult();
        u.getAuthGroups().add(g);

        em.persist(u);
        // need to flush here because I need a valid user id for the primary key in the link object
        em.flush();
        if (client != null) {
            UserClientLink ucl = new UserClientLink(u.getId(), client.getId());
            ucl.setMaxConfidentiality(maxConfidentiality);
            em.persist(ucl);
            em.flush();
            EntityRefresher.updateCachedEntity(em, u.getId(), u);
            EntityRefresher.updateCachedEntity(em, client.getId(), client);
        }
        Logger.getLogger(UserManagerBean.class.getName()).log(Level.INFO, "New user created: name={0}", u.getUserName());
        return u;
    }

    @Override
    public mltUser getUser(int userId) throws NonExistingObjectException {
        mlsUser u = em.find(mlsUser.class, userId);
        if (u == null || Collections.disjoint(getCurrentUser().getClients(), u.getClients())) {
            throw new NonExistingObjectException("The user with the specified userId does not exist or is not accessible");
        }
        return new mltUser(u);
    }

    @Override
    public mltClient getClient(int clientId) {
        mlsClient c = em.find(mlsClient.class, clientId);
        if (c == null) {
            return null;
        }
        return new mltClient(c);
    }

    @Override
    public List<Integer> getSignaturesOfActiveUsers() {
        Query q = em.createQuery("SELECT u FROM mlsUser u, IN(u.clients) as client WHERE client.id IN ("
                + ClientIdsStringBuilder.buildClientIdsString(getCurrentUser().getClients()) + ")");

        List<mlsUser> resultList = q.getResultList();
        List<Integer> ids = new ArrayList<>();
        for (mlsUser u : resultList) {
            if (u.getActive()) {
                ids.add(u.getId());
            }
        }
        return ids;
    }

    @Override
    @RolesAllowed(value = {"User", "Admin", "MasterAdmin"})
    public void addFeatureAuthorization(int userId, int featureId) throws NonExistingObjectException {
        mlsUser u = em.find(mlsUser.class, userId);
        if (u == null) {
            throw new NonExistingObjectException("User with specified id (" + userId + ") does not exist.");
        }
        SoftwareFeature f = em.find(SoftwareFeature.class, featureId);
        if (f == null) {
            throw new NonExistingObjectException("Feature with specified id (" + featureId + ") does not exist.");
        }
        u.getSoftwareFeatures().add(f);
        f.getUsers().add(u);
        em.flush();
    }

    @Override
    @RolesAllowed(value = {"Admin", "MasterAdmin"})
    public void removeFeatureAuthorization(int userId, int featureId) throws NonExistingObjectException {
        mlsUser u = em.find(mlsUser.class, userId);
        if (u == null) {
            throw new NonExistingObjectException("User with specified id (" + userId + ") does not exist.");
        }
        SoftwareFeature f = em.find(SoftwareFeature.class, featureId);
        if (f == null) {
            throw new NonExistingObjectException("Feature with specified id (" + featureId + ") does not exist.");
        }
        u.getSoftwareFeatures().remove(f);
        f.getUsers().remove(u);
        // important to increase the version number of mlsUser immediately so that the cache gets updated
        em.flush();
    }

    @Override
    public List<MlAuthenticationGroups> getAllAuthenticationGroups() {
        Query q = em.createNamedQuery("MlAuthenticationGroups.findAll");
        return q.getResultList();
    }

    @Override
    @RolesAllowed(value = {"MasterAdmin"})
    public void updateUserAuth(int userId, String encodedPassword, List<Integer> roleIds) {
        mlsUser u = em.find(mlsUser.class, userId);
        if (u == null) {
            return;
        }
        for (Integer rid : roleIds) {
            MlAuthenticationGroups g = em.find(MlAuthenticationGroups.class, rid);
            if (g != null) {
                if (!u.getAuthGroups().contains(g)) {
                    u.getAuthGroups().add(g);
                }
                if (!g.getUsers().contains(u)) {
                    g.getUsers().add(u);
                }
            } else {
                throw new IllegalArgumentException("One of the specified roles could not be found on server, ignoring update request");
            }
        }
        u.setPassword(encodedPassword);
        em.flush();
    }

    @Override
    public void updatePassword(String encodedPassword) {
        mlsUser u = getCurrentUser();
        u.setPassword(encodedPassword);
    }

    @Override
    @RolesAllowed(value = {"MasterAdmin"})
    public void updatePassword(int userId, String encodedPassword) {
        mlsUser u = em.find(mlsUser.class, userId);
        if (u != null) {
            u.setPassword(encodedPassword);
        }
    }

    @Override
    public mlsUser getCurrentUser() {
        Principal p = sc.getCallerPrincipal();
        int id = userCache.getUserId(p.getName());
        mlsUser u = em.find(mlsUser.class, id);
        return u;
    }

    @Override
    public mlsUser getCurrentUserRemote() {
        Principal p = sc.getCallerPrincipal();
        int id = userCache.getUserId(p.getName());
        mlsUser u = em.find(mlsUser.class, id);
        // ensure attributes are loaded as the return value will be serialized (-> remote call)
        u.getClients().size();
        u.getSoftwareFeatures().size();
        u.getAuthGroups().size();
        u.getMaxConfidentialities().size();
        return u;
    }

    @Override
    @RolesAllowed(value = {"MasterAdmin"})
    public void deleteClient(mlsClient c) {
        deleteClient(c.getId());
    }

    @Override
    public MlUserPreferences getDefaults(int userId) {
        return em.find(MlUserPreferences.class, userId);
    }

    @Override
    public void setObjectDefaults(int userId, int dataPoolId, boolean privateflag, int priorityId, List<MltObjectDefaultConfidentialities> transferConfidentialities) {
        mlsUser user = em.find(mlsUser.class, userId);
        mlsPriority defPrio = em.find(mlsPriority.class, priorityId);
        if (defPrio == null) {
            defPrio = categoryManager.getAllPriorities().get(0);
        }
        mlsClient defDataPool = em.find(mlsClient.class, dataPoolId);
        if (user == null || defPrio == null || defDataPool == null) {
            System.err.println("Could not store user defaults as at least one of the keys did not exist on the server");
            return;
        }

        // delete existing defaults for specified user and client (should only be one)
        MlUserPreferences existingDefaults = em.find(MlUserPreferences.class, userId);
        if (existingDefaults != null) {
            em.remove(existingDefaults);
            em.flush();
        }

        MlUserPreferences newObjectDefaults = new MlUserPreferences();
        newObjectDefaults.setDataPool(defDataPool);
        newObjectDefaults.setPriority(defPrio);
        newObjectDefaults.setPrivateflag(privateflag);
        newObjectDefaults.setUser(user);
        em.persist(newObjectDefaults);
        em.flush();

        List<MlObjectDefaultsConfidentialities> confidentialities = new ArrayList<>();
        for (MltObjectDefaultConfidentialities tc : transferConfidentialities) {
            mlsConfidentiality dpconfi = em.find(mlsConfidentiality.class, tc.getConfidentialityId());
            mlsClient dp = em.find(mlsClient.class, tc.getDataPoolId());
            if (dp != null && dpconfi != null) {
                MlObjectDefaultsConfidentialities newDefaultConf = new MlObjectDefaultsConfidentialities();
                newDefaultConf.setObjectDefaults(newObjectDefaults);
                newDefaultConf.setClient(dp);
                newDefaultConf.setConfidentiality(dpconfi);
                confidentialities.add(newDefaultConf);
            }
        }
        newObjectDefaults.setObjectDefaultsConfidentialities(confidentialities);
    }

    @Override
    public void updateObjectDefaults(int userId, int clientId, boolean privateflag, int priorityId, int confidentialityId) {
        MlUserPreferences d = em.find(MlUserPreferences.class, userId);
        if (d == null) {
            List<MltObjectDefaultConfidentialities> confis = initCreateSingleDefaultConfidentiality(clientId, confidentialityId);
            setObjectDefaults(userId, clientId, privateflag, priorityId, confis);
        }
        mlsClient dataPool = em.find(mlsClient.class, clientId);
        if (d != null) {
            if (d.getDataPool() == null || d.getDataPool().getId() != clientId) {
                if (dataPool != null) {
                    d.setDataPool(dataPool);
                }
            }
            d.setPrivateflag(privateflag);
            if (priorityId >= 0) {
                mlsPriority p = em.find(mlsPriority.class, priorityId);
                if (p != null) {
                    d.setPriority(p);
                }
            }
            mlsConfidentiality confidentiality = em.find(mlsConfidentiality.class, confidentialityId);
            if (confidentiality != null) {
                for (MlObjectDefaultsConfidentialities odc : d.getObjectDefaultsConfidentialities()) {
                    if (odc.getClient().equals(dataPool)) {
                        odc.setConfidentiality(confidentiality);
                    }
                }
            }
        }
    }

    /**
     * This method is used if the user does not yet have object prefs and they
     * are initialized with a single confi for a single data pool
     */
    private List<MltObjectDefaultConfidentialities> initCreateSingleDefaultConfidentiality(int clientId, int confidentialityId) {
        List<MltObjectDefaultConfidentialities> confis = new ArrayList<>();
        confis.add(new MltObjectDefaultConfidentialities(clientId, confidentialityId));
        return confis;
    }

    @Override
    public MlUserPreferences getUserPreferences(int userId) {
        MlUserPreferences up = em.find(MlUserPreferences.class, userId);
        if (up == null) {
            return null;
        }
        // force instantiation
        up.getObjectDefaultsConfidentialities().size();
        return up;
    }

    @Override
    public void setClientName(int clientId, String clientName) {
        mlsClient client = em.find(mlsClient.class, clientId);
        if (client != null) {
            client.setName(clientName);
            logManager.log(client, MlsEventType.EventType.ClientModified, null, 0, client.getName(), "setClientName", mlsLog.Type.Info);
        }
    }

    @Override
    public List<mlsUser> getUnconfirmedUsers() {
        Query q = em.createNamedQuery("MlAuthenticationGroups.findUnconfirmedGroup");
        MlAuthenticationGroups g = (MlAuthenticationGroups) q.getSingleResult();
        return g.getUsers();
    }

    @RolesAllowed(value = {"MasterAdmin"})
    @Override
    public boolean confirmUser(String login) {
        mlsUser u = findUser(login);
        if (u != null) {
            //remove user from unconfirmed group
            Query q = em.createNamedQuery("MlAuthenticationGroups.findUnconfirmedGroup");
            MlAuthenticationGroups g = (MlAuthenticationGroups) q.getSingleResult();
            g.getUsers().remove(u);
            u.getAuthGroups().clear();
            //add user to regular user group
            q = em.createNamedQuery("MlAuthenticationGroups.findUserGroup");
            g = (MlAuthenticationGroups) q.getSingleResult();
            u.getAuthGroups().add(g);
            u.setActive(true);
            return true;
        }
        return false;
    }

    @Override
    @RolesAllowed(value = {"Admin", "User", "MasterAdmin"})
    public List<mlsUser> getUsersWithSharedDatapool(int userId) {
        Query q = em.createNamedQuery("mlsUser.getUsersWithSharedDataPools");
        q.setParameter("userId", userId);
        return q.getResultList();
    }

    @Override
    public mlsUser findUserByEmail(String email) {
        Query nq = em.createNamedQuery("mlsUser.getUserByEmail");
        nq.setParameter("email", email);
        List resultList = nq.getResultList();
        if (resultList.size() > 0) {
            return (mlsUser) resultList.get(0);
        }
        return null;
    }

    @RolesAllowed(value = {"MasterAdmin"})
    @Override
    public void deleteUnconfirmedUser(mlsUser user) {
        //first check if user to be deleted is really unconfirmed
        List<mlsUser> unconfirmedUsers = getUnconfirmedUsers();
        if (!unconfirmedUsers.contains(user)) {
            throw new IllegalArgumentException("This user is not unconfirmed.");
        }
        //remove user from unconfirmed group
        unconfirmedUsers.remove(user);
        em.remove(em.merge(user));
    }

    @Override
    public boolean isAuthorized(SoftwareFeature.CurrentFeatures feature) {
        for (SoftwareFeature f : getCurrentUser().getSoftwareFeatures()) {
            if (feature.toString().equals(f.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateLastNewsDeliveryDigest(int userId) {
        MlUserPreferences up = em.find(MlUserPreferences.class, userId);
        if (up == null) {
            up = createDefaultUserPreferences(userId);
        }
        if (up != null) {
            Date lastNewsUpdate = new Date();
            up.setNewsLastDigest(lastNewsUpdate);
            System.out.println("\tUpdated last news delivery date for " + up.getUser().getFirstName() + " to " + lastNewsUpdate);
        }
    }

    /**
     * This method is used if no user preferences exist yet and have to be
     * created pretty much out of thin air....
     *
     * @param userId The user for which the prefs need to be created
     * @return The new
     */
    private MlUserPreferences createDefaultUserPreferences(int userId) {
        mlsUser u = em.find(mlsUser.class, userId);
        if (u == null) {
            return null;
        }
        mlsClient dataPool = u.getClients().get(0);
        List<MltObjectDefaultConfidentialities> confis = initCreateSingleDefaultConfidentiality(userId, u.getMaxConfidentiality(dataPool).getId());
        setObjectDefaults(userId, u.getClients().get(0).getId(), false, categoryManager.getAllPriorities().get(0).getId(), confis);
        return em.find(MlUserPreferences.class, userId);
    }

    @RolesAllowed(value = {"MasterAdmin"})
    @Override
    public List<mlsUser> getAllUsers() {
        return em.createNamedQuery("mlsUser.findAll").getResultList();
    }

    @Override
    public void setCurrentWorkObject(Integer taskId) {
        if (taskId == null) {
            userCache.setCurrentWorkObject(getCurrentUser(), null);
        } else {
            mlsObject o = em.find(mlsObject.class, taskId);
            if (o instanceof mlsTask) {
                mlsTask t = (mlsTask) o;
                userCache.setCurrentWorkObject(getCurrentUser(), t);
            }
        }
    }

    @Override
    public List<CurrentWorkTask> getCurrentWorkTasks() {
        List<CurrentWorkTask> currentWorkTasks = userCache.getCurrentWorkTasks();
        // filter the tasks that are not accessible to the caller
        List<CurrentWorkTask> results = new ArrayList<>();
        mlsUser cu = getCurrentUser();
        List<mlsClient> clients = cu.getClients();
        for (CurrentWorkTask cwt : currentWorkTasks) {
            mlsObject o = em.find(mlsTask.class, cwt.getTaskId());
            if (o != null && o instanceof mlsTask
                    && clients.contains(o.getClient())
                    && (o.getPrivateAccess() == false || o.getOwner().equals(cu))
                    && o.getConfidentiality().compareTo(cu.getMaxConfidentiality(o.getClient())) <= 0) {
                results.add(cwt);
            }
        }
        return results;
    }

}
