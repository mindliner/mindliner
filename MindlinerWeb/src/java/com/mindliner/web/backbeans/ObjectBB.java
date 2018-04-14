/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.analysis.UriUtils;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsMindlinerCategory;
import com.mindliner.categories.mlsPriority;
import com.mindliner.contentfilter.Completable;
import com.mindliner.entities.MlUserPreferences;
import com.mindliner.entities.MlsNews;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsKnowlet;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsObjectCollection;
import com.mindliner.entities.mlsTask;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.IsOwnerException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.managers.CategoryManagerRemote;
import com.mindliner.managers.HeadlineParserRemote;
import com.mindliner.managers.MlMessageHandler;
import com.mindliner.managers.ObjectFactoryLocal;
import com.mindliner.managers.ObjectManagerLocal;
import com.mindliner.managers.SecurityManagerRemote;
import com.mindliner.managers.UserManagerLocal;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.persistence.OptimisticLockException;

/**
 * This class represents the current object of the application.
 *
 * @author Marius Messerli
 */
@ManagedBean
@ViewScoped
@DeclareRoles(value = {"Admin", "User", "MasterAdmin"})
@RolesAllowed(value = {"Admin", "User", "MasterAdmin"})
public class ObjectBB implements Serializable {

    @EJB
    private CategoryManagerRemote categoryManager;
    @EJB
    private SecurityManagerRemote securityManager;
    @EJB
    private UserManagerLocal userManager;
    @EJB
    private HeadlineParserRemote headlineParser;
    @EJB
    private ObjectManagerLocal objectManager;
    @EJB
    private ObjectFactoryLocal objectFactory;

    @PostConstruct
    public void init() {
        initCreationWithoutRelative();
        System.out.println("Bean reconstructed " + new Date());
    }

    public static enum ObjectType {

        Knowlet,
        Task,
        Collection
    }

    // Current selected object
    private mlsObject object;
    private String oldHeadline;
    private int selectedId;

    // GUI variables
    private boolean mapview = false;
    private boolean treeview = false;
    private boolean editMode = false;
    private boolean createMode = false;
    String activePanel = "1"; //either new form or details form

    // attributes of the new object
    private mlsClient datapool;
    private mlsConfidentiality confidentiality;
    private String headline;
    private String description;
    private boolean privateAccess;
    private ObjectType type;
    private int newId;

    // used for object type change
    private ObjectType newType;
    private mlsObject objectBeforeTypeChange;

    // this map hold the allowed (i.e. all up to her/his level) confidentialities for the current user
    private Map<mlsClient, List<mlsConfidentiality>> allowedConfidentialitiesCache = new HashMap<>();
    private List<mlsPriority> prioritiesCache = new ArrayList<>();

    public boolean getMapview() {
        return mapview;
    }

    public void setMapview(boolean mapview) {
        this.mapview = mapview;
    }

    public boolean getTreeview() {
        return treeview;
    }

    public void setTreeview(boolean treeview) {
        this.treeview = treeview;
    }

    public void setView(boolean mapview, boolean treeview) {
        this.mapview = mapview && treeview ? !mapview : mapview; //favor tree view
        this.treeview = treeview;
    }

    public mlsObject getObject() {
        return object;
    }

    public List<String> getDescriptionUris(mlsObject o) {
        List<String> uris = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(o.getDescription());
        for (; st.hasMoreTokens();) {
            URI absoluteUri = UriUtils.getAbsoluteUri(st.nextToken());
            if (absoluteUri != null) {
                uris.add(absoluteUri.toString());
            }
        }
        return uris;
    }

    public mlsMindlinerCategory getCategory() {
        if (object instanceof mlsTask) {
            return ((mlsTask) object).getPriority();
        } else if (object instanceof MlsNews) {
            return ((MlsNews) object).getNewsType();
        }
        return null;
    }

    // Use datapool in object creation and object.getClient() in object editor
    public List<mlsConfidentiality> getConfidentialities() {
        mlsClient client = datapool == null ? object.getClient() : datapool;
        if ((allowedConfidentialitiesCache.get(client)) == null) {
            mlsConfidentiality maxConf = userManager.getCurrentUser().getMaxConfidentiality(datapool);
            if (maxConf != null) {
                allowedConfidentialitiesCache.put(client,
                        securityManager.getAllowedConfidentialities(datapool == null ? object.getClient().getId() : datapool.getId(), maxConf.getClevel()));
            }
        }
        return allowedConfidentialitiesCache.get(datapool);
    }

    public mlsConfidentiality getObjectConfidentiality() {
        return object.getConfidentiality();
    }

    public void setObjectConfidentiality(mlsConfidentiality confidentiality) {
        object.setConfidentiality(confidentiality);
    }

    public List<mlsPriority> getPriorities() {
        if (prioritiesCache.isEmpty()) {
            prioritiesCache = categoryManager.getAllPriorities();
        }
        return prioritiesCache;
    }

    public List<mlsClient> getClients() {
        return userManager.getCurrentUser().getClients();
    }

    public mlsClient getDataPool() {
        return object.getClient();
    }

    public void setDataPool(mlsClient dataPool) {
        object.setClient(dataPool);
    }

    /**
     * This method stores changes to the current object to the database. If the
     * current object is a new, yet unpersisted object, then this function
     * creates a new object and stores.
     *
     * @todo I should use the same mechanism to update objects like for the
     * desktop client (logging through interceptors etc)
     *
     */
    public void save() {
        editMode = false;
        if (object.getId() == -1) {
            throw new IllegalStateException("Object detected with id of -1. It should have been persisted before.");
        }
        try {
            // first merge all updated fields
            object.setModificationDate(new Date());
            object = objectManager.merge(object);

            // after the general merge, handle the headline field and let the tags overwrite the other settings
            if (!oldHeadline.equals(object.getHeadline())) {
                try {
                    headlineParser.updateHeadline(object.getId(), object.getHeadline());
                } catch (mlModifiedException | NonExistingObjectException ex) {
                    // todo feed error messages back to page
                    Logger.getLogger(ObjectBB.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
            // handle object type change
            if (newType != getType(object)) {
                changeObjectType(object, newType);
            }
            userManager.updateObjectDefaults(userManager.getCurrentUser().getId(), object.getClient().getId(), object.getPrivateAccess(), getPriority() != null ? getPriority().getId() : -1, object.getConfidentiality().getId());
            MlMessageHandler mh = new MlMessageHandler();
            mh.sendMessage(userManager.getCurrentUser(), object, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "");
            mh.closeConnection();
        } catch (OptimisticLockException ex) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Sorry: Could not save changes: {0}", ex.getMessage());
        }
    }

    public boolean isMyObject() {
        return (object == null) ? false : object.getOwner().equals(userManager.getCurrentUser());
    }

    public boolean isTask() {
        return (object instanceof mlsTask);
    }

    public mlsPriority getPriority() {
        if (object instanceof mlsTask) {
            return ((mlsTask) object).getPriority();
        }
        return null;
    }

    public Date getDueDate() {
        if (object instanceof mlsTask) {
            return ((mlsTask) object).getDueDate();
        }
        return null;
    }

    public void setDueDate(Date d) {
        if (object instanceof mlsTask) {
            ((mlsTask) object).setDueDate(d);
        }
    }

    public void setPriority(mlsPriority p) {
        if (object instanceof mlsTask) {
            ((mlsTask) object).setPriority(p);
        }
    }

    /**
     * Determines if the object is of a completable type.
     *
     * @return True if the object implements the interface Completable, false
     * otherwise.
     */
    public boolean isCompletable() {
        return (object instanceof Completable);
    }

    /**
     * Determins if the object is completed.
     *
     * @return True if the object is both completable and completed, false
     * otherwise.
     */
    public boolean isCompleted() {
        if (!(object instanceof Completable)) {
            return false;
        } else {
            return ((Completable) object).isCompleted();
        }
    }

    /**
     * If the current object is of a completable type sets the completion state.
     *
     * @param completed
     */
    public void setCompleted(boolean completed) {
        if (object instanceof Completable) {
            ((Completable) object).setCompleted(completed);
        }
    }

    public String getObjectClassAltString(mlsObject o) {
        if (o == null) {
            return "";
        }
        ObjectType oType = getType(o);

        return oType == null ? MlClassHandler.getClassNameOnly(o.getClass().getName())
                : com.mindliner.web.util.Messages.getStringFromBundle(oType.toString());
    }

    public void deleteObjectById(String objectId) {
        int id = Integer.parseInt(objectId);
        mlsObject objectToBeDeleted = objectManager.findLocal(id);
        deleteObject(objectToBeDeleted);
    }

    /**
     * Allows the user to delete her/his own objects, only.
     *
     * @param o The object to be deleted.
     */
    public void deleteObject(mlsObject o) {
        try {
            try {
                objectManager.remove(o);
            } catch (IsOwnerException ex) {
                Logger.getLogger(ObjectBB.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

        } catch (ForeignOwnerException ex) {
            Logger.getLogger(ObjectBB.class
                    .getName()).log(Level.SEVERE, null, ex);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not delete object", ex.getMessage());

            throw new ValidatorException(msg);
        }
    }

    public void setObject(int key) throws IOException {
        if (key == 0 && object != null) {
            return;
        }
        object = objectManager.findLocal(key);
        if (object != null) {
            if (objectManager.isAuthorizedForCurrentUser(object)) {
                setActivePanel("1");
                oldHeadline = object.getHeadline();
            } else {
                object = null;
                com.mindliner.web.util.Messages.generateErrorMessageFromBundle("AccessDenied");
            }
        } else if (key > 0) {
            com.mindliner.web.util.Messages.generateErrorMessageFromBundle("ObjectNotFound");
        }
    }

    public void changeObjectType(mlsObject o, ObjectType newType) {
        Class clazz;
        switch (newType) {
            case Collection:
                clazz = mlsObjectCollection.class;
                break;

            case Knowlet:
                clazz = mlsKnowlet.class;
                break;

            case Task:
                clazz = mlsTask.class;
                break;

            default:
                throw new AssertionError();
        }
        if (clazz.equals(o.getClass())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Object is already of requested type", "");
            throw new ValidatorException(msg);
        } else {
            try {
                objectBeforeTypeChange = o;
                object = objectManager.changeObjectType(o, clazz);
            } catch (Exception ex) {
                Logger.getLogger(ObjectBB.class.getName()).log(Level.SEVERE, null, ex);
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not change type", ex.getMessage());
                throw new ValidatorException(msg);
            }
        }
    }

    private ObjectType getType(mlsObject o) {
        if (o instanceof mlsKnowlet) {
            return ObjectType.Knowlet;
        } else if (o instanceof mlsTask) {
            return ObjectType.Task;
        } else if (o instanceof mlsObjectCollection) {
            return ObjectType.Collection;
        }
        Logger.getLogger(ObjectBB.class.getName()).log(Level.WARNING, null, "Type not supported");
        return null;
    }

    public ObjectType getNewType() {
        return newType;
    }

    public void setNewType(ObjectType newType) {
        this.newType = newType;
    }

    public mlsObject getObjectBeforeTypeChange() {
        return objectBeforeTypeChange;
    }

    public boolean getEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        if (editMode) {
            // initialize correct type for select radio 
            if (object instanceof mlsKnowlet) {
                setNewType(ObjectType.Knowlet);
            } else if (object instanceof mlsTask) {
                setNewType(ObjectType.Task);
            } else if (object instanceof mlsObjectCollection) {
                setNewType(ObjectType.Collection);
            }
        }
        createMode = false;
        this.editMode = editMode;
    }

    public boolean getCreateMode() {
        return createMode;
    }

    public void setCreateMode(boolean createMode) throws IOException {
        editMode = false;
        if (!createMode && this.createMode && object == null) {
            // Case where creation w/o parent has been canceled
            setActivePanel("0");
            FacesContext.getCurrentInstance().getExternalContext().redirect("workspace.xhtml");
        } else if (!createMode && this.createMode
                || createMode && !this.createMode) {
            setActivePanel("1");
        }
        this.createMode = createMode;
    }

    public int getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(int selectedId) {
        if (selectedId == 0 && object != null) {
            return;
        }
        if (selectedId == 0 && !createMode) {
            setActivePanel("0");
            return;
        }
        mlsObject o = objectManager.findLocal(selectedId);
        if (o != null && objectManager.isAuthorizedForCurrentUser(o)) {
            this.selectedId = selectedId;
        }
    }

    /**
     * Initializes all object fields
     *
     * @param relativeId relative of the object which serves as a base of the
     * new object
     */
    public void initCreationWithRelative(int relativeId) {
        mlsObject parent = objectManager.findLocal(relativeId);
        datapool = parent.getClient();
        MlUserPreferences defs = userManager.getUserPreferences(userManager.getCurrentUser().getId());
        if (defs != null && defs.getConfidentiality(datapool) != null) {
            confidentiality = defs.getConfidentiality(datapool);
        } else {
            confidentiality = parent.getConfidentiality();
        }
        headline = "";
        description = "";
        type = ObjectType.Knowlet;
        privateAccess = false;
    }

    public void initCreationWithoutRelative() {
        MlUserPreferences defs = userManager.getUserPreferences(userManager.getCurrentUser().getId());
        if (defs != null) {
            datapool = defs.getDataPool();
            confidentiality = defs.getConfidentiality(datapool);
        } else {
            datapool = userManager.getCurrentUser().getClients().get(0);
            confidentiality = datapool.getConfidentialities().get(0);
        }
        headline = "";
        description = "";
        type = ObjectType.Knowlet;
        privateAccess = false;
    }

    /**
     * This method creates a new object which is linked to the specified
     * relative.
     *
     * @param relativeId The id of the object the new object will be linked to
     * @throws com.mindliner.exceptions.ForeignOwnerException
     * @throws java.io.IOException
     */
    public void createObject(int relativeId) throws ForeignOwnerException, IOException {
        mlsObject o = objectFactory.createLocal(getClassForType(), datapool, null, confidentiality, relativeId, LinkRelativeType.OBJECT, null, headline, description);
        setNewId(o.getId());
        objectManager.setPrivacyFlag(o.getId(), privateAccess);
        this.createMode = false;//do not use setCreateMode!
        if (relativeId == 0) {
            FacesContext.getCurrentInstance().getExternalContext().redirect("workspace.xhtml?id=" + o.getId());
        }
    }

    public int getNewId() {
        return newId;
    }

    public void setNewId(int newId) {
        this.newId = newId;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getHeadline() {
        return headline;
    }

    public mlsConfidentiality getConfidentiality() {
        return confidentiality;
    }

    public void setConfidentiality(mlsConfidentiality confidentiality) {
        this.confidentiality = confidentiality;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public mlsClient getDatapool() {
        return datapool;
    }

    public void setDatapool(mlsClient datapool) {
        this.datapool = datapool;
    }

    public List<mlsClient> getDataPools() {
        return userManager.getCurrentUser().getClients();
    }

    public boolean isPrivateAccess() {
        return privateAccess;
    }

    public void setPrivateAccess(boolean privateAccess) {
        this.privateAccess = privateAccess;
    }

    public ObjectType getType() {
        return type;
    }

    public void setType(ObjectType type) {
        this.type = type;
    }

    public List<ObjectType> getObjectTypes() {
        return Arrays.asList(ObjectType.values());
    }

    private Class getClassForType() {
        switch (type) {
            case Knowlet:
                return mlsKnowlet.class;
            case Task:
                return mlsTask.class;
            case Collection:
                return mlsObjectCollection.class;
            default:
                throw new AssertionError();
        }
    }

    public String getActivePanel() {
        return activePanel;
    }

    public void setActivePanel(String activePanel) {
        this.activePanel = activePanel;
    }

}
