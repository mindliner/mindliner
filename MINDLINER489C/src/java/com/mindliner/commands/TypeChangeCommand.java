/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.MlCacheException;
import com.mindliner.clientobjects.MlClientClassHandler;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcKnowlet;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.clientobjects.mlcWorkUnit;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.objects.transfer.MltObject;
import com.mindliner.objects.transfer.mltKnowlet;
import com.mindliner.objects.transfer.mltObjectCollection;
import com.mindliner.objects.transfer.mltTask;
import com.mindliner.objects.transfer.mltWorkUnit;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.Date;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 * Changes the type of an object.
 * 
 * @author Dominic Plangger
 */
public class TypeChangeCommand extends MindlinerOnlineCommand {
    
    private final Class newType;
    private final mlcObject old;
    private final int startId;
    public TypeChangeCommand(mlcObject o, Class newType) {
        super(o,false);
        this.newType = newType;
        this.startId = o.getId();
        this.old = o;
    }

    
    @Override
    public void execute() throws NamingException, mlModifiedException , MlAuthorizationException{
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        Class serverClass = MlClientClassHandler.getMatchingServerClass(newType);
        try {
            MltObject newObj = omr.changeObjectType(getObject().getId(), serverClass);
            mlcObject cObj = buildFromTransfer(newObj);
            // replace object on map immediately, otherwise following can happen:
            // between the end of omr.changeObjectType and the arrival of the REPLACE message,
            // another command might query the server for relatives and will already receive the new id
            // of the replaced object. as the node on the map still has the old object, that will create new node
            ObjectChangeManager.objectReplaced(getObject().getId(), cObj);
            updateCommands(newObj);
            // set these attributes for a possible undo
            getObject().setId(cObj.getId());
            getObject().setVersion(cObj.getVersion());
        } catch (Exception ex) {
            Logger.getLogger(TypeChangeCommand.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Unexpected error at server side while changing object type");
        }
        
    }
    
    public void executeAsync() {
        try {
            mlcObject o = createTemporaryObject(); // create new temp object with same id
            setObject(o);
            CacheEngineStatic.addToCache(o); // overwrite old object
            ObjectChangeManager.objectReplaced(startId, o); // replace old object, the new temporary object has the same id
            CommandRecorder cr = CommandRecorder.getInstance();
            Queue<MindlinerCommand> cmdQueue = cr.getQueue();
            // replace the object also in all queued commands
            // is need for example in the ObjectCreationCommand, so that
            // the id and version is set on the correct object after execution
            for (MindlinerCommand cmd : cmdQueue) {

                mlcObject obj = cmd.getObject();
                if (obj != null && obj.getId() == o.getId()) {
                    cmd.setObject(o);
                }
                if (cmd instanceof ObjectCreationCommand) {
                    ObjectCreationCommand c = (ObjectCreationCommand) cmd;
                    if (c.getRelative() != null && c.getRelative().getId() == o.getId()) {
                        c.setRelative(o);
                    }
                }
                else if (cmd instanceof LinkCommand) {
                    LinkCommand c = (LinkCommand) cmd;
                    if (c.getObject2() != null && c.getObject2().getId() == o.getId()) {
                        c.setObject2(o);
                    }
                }
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(TypeChangeCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException , MlAuthorizationException{
        super.undo(); 
        
        TypeChangeCommand cmd = new TypeChangeCommand(getObject(), old.getClass());
        CommandRecorder cr = CommandRecorder.getInstance();
        cr.scheduleCommand(cmd);
    }
    
    
    
    private mlcObject createTemporaryObject() throws InstantiationException, IllegalAccessException {
        mlcObject o = getObject();
        mlcObject newObj = (mlcObject) newType.newInstance();
        newObj.setClient(o.getClient());
        newObj.setConfidentiality(o.getConfidentiality());
        newObj.setCreationDate(o.getCreationDate());
        newObj.setDescription(o.getDescription());
        newObj.setHeadline(o.getHeadline());
        newObj.setModificationDate(new Date());
        newObj.setOwner(o.getOwner());
        newObj.setPrivateAccess(o.isPrivateAccess());
        newObj.setRating(o.getRating());
        newObj.setSynchUnits(o.getSynchUnits());
        newObj.setVersion(o.getVersion());
        newObj.setStatus(o.getStatus());
        newObj.setIslandId(o.getIslandId());
        newObj.setIcons(o.getIcons());
        newObj.setId(o.getId());
        return newObj;
    }

    @Override
    public boolean isVersionChecking() {
        return true;
    }

    @Override
    public String toString() {
        return "Change " + getFormattedId() + " to type " + newType.getSimpleName();
    }

    private void updateCommands(MltObject newObj) {
        CommandRecorder cr = CommandRecorder.getInstance();
        Queue<MindlinerCommand> cmdQueue = cr.getQueue();
        int oldId = getObject().getId();
        int newId = newObj.getId();
        for (MindlinerCommand c : cmdQueue) {
            mlcObject obj = c.getObject();
            if (obj != null) {
                if (obj.getId() == oldId || obj.getId() == startId) {
                    obj.setId(newId);
                    obj.setVersion(newObj.getVersion());
                }
            }
            if (c instanceof ObjectDeletionCommand) {
                for (mlcObject o : ((ObjectDeletionCommand)c).getDeletedObjects()) {
                    if (o.getId() == oldId || o.getId() == startId) {
                        o.setId(newId);
                        o.setVersion(newObj.getVersion());
                    }
                }
            }
        }
    }

    private mlcObject buildFromTransfer(MltObject newObj) {
         mlcObject result = null;
        if (newObj instanceof mltKnowlet) {
            mltKnowlet t = (mltKnowlet) newObj;
            mlcKnowlet ct = new mlcKnowlet();
            result = ct;
        }
        else if (newObj instanceof mltObjectCollection) {
            mltObjectCollection t = (mltObjectCollection) newObj;
            mlcObjectCollection ct = new mlcObjectCollection();
            ct.setDescription(t.getDescription());
            result = ct;
        }
        else if (newObj instanceof mltTask) {
            mltTask t = (mltTask) newObj;
            mlcTask ct = new mlcTask();
            ct.setPriority(CacheEngineStatic.getPriority(t.getPriorityOrdinal()));
            ct.setEffortEstimation(t.getEffortEstimation());
            ct.setCompleted(t.getCompleted());
            ct.setDueDate(t.getDueDate());
            for (mltWorkUnit tw : t.getWorkUnits()){
                mlcWorkUnit nwu = new mlcWorkUnit(tw);
                nwu.setTaskId(ct.getId());
                ct.getWorkUnits().add(nwu);
            }
            result = ct;
        }
        setCommonFields(result, newObj);
        return result;
    }

    private void setCommonFields(mlcObject r, MltObject t) {
        r.setId(t.getId());
        r.setConfidentiality(CacheEngineStatic.getConfidentiality(t.getConfidentialityId()));
        r.setDescription(t.getDescription());
        mlcUser owner;
        try {
            owner = CacheEngineStatic.getUser(t.getOwnerId());
        } catch (MlCacheException ex) {
            throw new IllegalStateException("Could not find owner for transfer object.", ex);
        }
        r.setOwner(owner);
        r.setHeadline(t.getHeadline());
        r.setModificationDate(t.getModificationDate());
        r.setCreationDate(t.getCreationDate());
        r.setVersion(t.getVersion());
        r.setRating(t.getRating());
        r.setStatus(t.getStatus());
        r.setRelativesOrdered(t.isRelativesOrdered());
        r.setRelativeCount(t.getRelativeCount());
        r.setPrivateAccess(t.isPrivateAccess());
        mlcClient client = CacheEngineStatic.getClient(t.getClientId());
        if (client == null) {
            throw new IllegalStateException("Cannot reconstruct client for new object");
        }
        r.setSynchUnits(t.getSynchUnits());
        r.setClient(client);
    }
    
    
    
}
