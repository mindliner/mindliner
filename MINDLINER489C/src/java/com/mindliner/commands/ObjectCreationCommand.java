/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.categories.*;
import com.mindliner.clientobjects.*;
import com.mindliner.entities.MlsImage;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.enums.ObjectReviewStatus;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.managers.ObjectFactoryRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 *
 * @author Marius Messerli
 */
public class ObjectCreationCommand extends MindlinerOnlineCommand {

    private Class objectClass = null;
    private mlcObject relative = null;
    private int temporaryId = 0;
    private Date creationDate = null;
    // local copies of the object default attributes at the time of creation
    private mlsPriority priority;
    private boolean privateAccess;
    private ObjectReviewStatus status;
    private mlsConfidentiality confidentiality;
    private int clientId = -1;
    private String headline = null;
    private String description = null;
    private List<ProgressListener> listeners;
    private LinkRelativeType linkType = LinkRelativeType.OBJECT;

    /**
     * Creates a new mindliner object.
     *
     * @param relative A relative that will be linked to the new node after
     * creation. Specify null if the new object should not be linked to any
     * other object.
     * @param clientObjectClass
     * @param headline
     * @param description
     * @todo - This must be re-written: when in offline mode the object creation
     * will take the DefaultObjectAttribute that will exist then when the edits
     * are synched back to the server and not at this moment in time
     */
    public ObjectCreationCommand(mlcObject relative, Class clientObjectClass, String headline, String description) {
        super(null, false);
        assert headline != null : "Headline must not be null";
        assert description != null : "Description must not be null";
        this.relative = relative;
        this.objectClass = clientObjectClass;
        this.headline = headline;
        this.description = description;
        listeners = new ArrayList<>();
        creationDate = new Date();
        priority = DefaultObjectAttributes.getPriority();
        privateAccess = DefaultObjectAttributes.getPrivateAccess();
        if (relative != null) {
            clientId = relative.getClient().getId();
        } else {
            clientId = DefaultObjectAttributes.getDataPoolId();
        }
        confidentiality = DefaultObjectAttributes.getConfidentiality(clientId);
        status = ObjectReviewStatus.REVIEWED;
        // create temporary object
        mlcObject tmpObject = createTemporaryObject(objectClass);
        temporaryId = tmpObject.getId();
        setObject(tmpObject);
    }

    public ObjectCreationCommand(mlcObject relative, Class clientObjectClass, String headline, String description, ObjectReviewStatus status) {
        this(relative, clientObjectClass, headline, description);
        this.status = status;
    }

    public ObjectCreationCommand(mlcObject relative, Class clientObjectClass, String headline, String description, LinkRelativeType linkType) {
        this(relative, clientObjectClass, headline, description);
        this.linkType = linkType;
    }

    public interface ProgressListener extends Serializable {

        public void creationFinished(mlcObject obj);
    }

    /**
     * A progress listener can be added to the command that will be called as
     * soon as the command has finished.
     *
     * @param listener
     */
    public void addProgressListener(ProgressListener listener) {
        listeners.add(listener);
    }

    /**
     * Unlike most other commands we don't call super.execute() which would
     * attempt to map the temporary ID which created only with this command.
     *
     * @throws javax.naming.NamingException
     */
    @Override
    public void execute() throws NamingException {
        if (isExecuted() == false) {
            ObjectFactoryRemote of = (ObjectFactoryRemote) RemoteLookupAgent.getManagerForClass(ObjectFactoryRemote.class);

            int relativeId = -1;
            if (relative == null) {
                checkForFutureLinkCmd();
            }
            if (relative != null) {
                relativeId = relative.getId();
            }
            int[] res;
            res = of.create(
                    MlClientClassHandler.getMatchingServerClass(objectClass),
                    clientId,
                    confidentiality.getId(),
                    creationDate,
                    priority.getId(),
                    privateAccess,
                    relativeId,
                    headline,
                    description,
                    status,
                    linkType,
                    null); // the newsType is specified as null because for now we don't create news instances here
            int id = res[0];
            int version = res[1];
            getObject().setId(id);
            getObject().setVersion(version);
            CacheEngineStatic.addToCache(getObject()); // the temporary object is still with the negative id in the cache

            if (relative != null) {
                // update also version of relative after linking
                relative.setVersion(res[2]);
                // force an update of the cached link set
                CacheEngineStatic.getLinkedObjects(getObject());
                // the non cell relatives of an offline object is already updated
                if (temporaryId >= 0) {
                    LinkCommand.updateNonCellRelatives(getObject(), relative, true);
                }
            }

            // if this command was created in offline mode then the ID is negative and the id map record needs to be updated
            if (temporaryId < 0) {
                CommandRecorder.getInstance().addIdPair(temporaryId, getObject().getId());
            }
            setExecuted(true);

            for (ProgressListener listener : listeners) {
                listener.creationFinished(getObject());
            }
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        CacheEngineStatic.removeObject(getObject());
        if (relative != null) {
            LinkCommand.updateNonCellRelatives(getObject(), relative, false);
        }
        BulkUpdater.publishDeletion(getObject());
    }

    public void setTemporaryId(int temporaryId) {
        this.temporaryId = temporaryId;
    }

    public void setRelative(mlcObject relative) {
        this.relative = relative;
    }

    public mlcObject getRelative() {
        return relative;
    }

    private void applyDefaults(mlcObject o) {
        if (o instanceof mlcTask) {
            mlcTask t = (mlcTask) o;
            t.setPriority(priority);
        } else if (o instanceof MlcImage) {
            MlcImage i = (MlcImage) o;
            i.setType(MlsImage.ImageType.URL);
        }
    }

    private mlcObject createTemporaryObject(Class c) {
        try {
            CommandRecorder cr = CommandRecorder.getInstance();
            mlcObject newObject = (mlcObject) c.newInstance();
            if (newObject != null) {
                newObject.setId(cr.getTemporaryId());
                mlcUser currentUser = CacheEngineStatic.getCurrentUser();
                newObject.setOwner(currentUser);
                newObject.setClient(CacheEngineStatic.getClient(clientId));
                newObject.setConfidentiality(confidentiality);
                newObject.setCreationDate(creationDate);
                newObject.setModificationDate(creationDate);
                newObject.setPrivateAccess(privateAccess);
                newObject.setVersion(0);
                newObject.setHeadline(headline);
                newObject.setDescription(description);
                applyDefaults(newObject);
                CacheEngineStatic.addToCache(newObject);

                // this ensures that the new object becomes linked also in offline mode
                if (relative != null) {
                    CacheEngineStatic.addRelative(newObject, relative, false);
                    LinkCommand.updateNonCellRelatives(newObject, relative, true);
                }
            }
            return newObject;
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public String toString() {
        return "Create " + MlClassHandler.getClassNameOnly(objectClass.getName() + " (" + getFormattedId() + ")");
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    // checks if there is a link command in the queue that links the object we create here
    // with another already existing object. if yes, we remove the link command and set the relative
    // already here in the creation procedure. Reasons:
    // 1: performance. 2: In offline mode: when creating obj1, then obj2, then link  them, then go online: 
    // ML sometimes sets them both as root nodes and we see obj1->obj2, and obj2->obj1  due to a race condition.
    
    // TODO dominic 02.09.2015: check above, what kind of race condition?
    private void checkForFutureLinkCmd() {
        Queue<MindlinerCommand> cmds = CommandRecorder.getInstance().getQueue();
        Iterator<MindlinerCommand> it = cmds.iterator();
        while (it.hasNext()) {
            MindlinerCommand cmd = it.next();
            if (cmd instanceof LinkCommand) {
                LinkCommand lcmd = (LinkCommand) cmd;
                if (!LinkRelativeType.OBJECT.equals(lcmd.getType())) {
                    // ICON link commands for example should not be removed
                    return;
                }
                if (lcmd.getObject().getId() == getObject().getId()) {
                    if (lcmd.getObject2().getId() >= 0) {
                        relative = lcmd.getObject2();
                        it.remove();
                        return;
                    }
                } else if (lcmd.getObject2().getId() == getObject().getId()) {
                    if (lcmd.getObject().getId() >= 0) {
                        relative = lcmd.getObject();
                        it.remove();
                        return;
                    }
                }
            }
        }
    }
}
