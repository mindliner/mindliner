package com.mindliner.commands;

import com.mindliner.clientobjects.MlcContainer;
import com.mindliner.clientobjects.MlcContainerMap;
import com.mindliner.clientobjects.ObjectIdLister;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.managers.ContainerMapManagerRemote;
import com.mindliner.objects.transfer.MltContainer;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;

/**
 *
 * @author Dominic Plangger
 */
public class CMUpdateContainerCommand extends MindlinerOnlineCommand {

    private MlcContainer container;
    private final MltContainer newSpecs;
    private final MltContainer oldSpecs;
    
    private final List<Integer> objsInside;
    private final List<Integer> oldObjsInside;
    private final MlcContainerMap map;

    public CMUpdateContainerCommand(MlcContainer obj, MlcContainerMap map, MltContainer newSpecs, List<mlcObject> objsInside, List<mlcObject> oldObjsInside) {
        super(obj, true);
        if (obj == null || map == null) {
            throw new IllegalArgumentException("source object nor template can be null");
        }
        this.newSpecs = newSpecs;
        this.oldSpecs = new MltContainer(obj.getPosX(), obj.getPosY(), obj.getWidth(), obj.getHeight(), obj.getColor(), obj.getStrokeWidth(), obj.getOpacity(), obj.getStrokeStyle());
        this.container = obj;
        this.oldObjsInside = ObjectIdLister.getIdList(oldObjsInside);
        this.map = map;
        // null means leave the objects inside as it is.
        // objsInside should be null whenever only the container specs change, but not the objs inside.
        // however oldObjsInside must never be null as for undoing we always must speicfy the exact objects inside
        this.objsInside = objsInside == null ? null : ObjectIdLister.getIdList(objsInside); 
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        if (!isExecuted()) {
            super.execute();
            // Convert any temporary IDs to real IDs. Alternative would be to directly
            // save the mlcObjects instead of their ID's.
            CommandRecorder cr = CommandRecorder.getInstance();
            List<Integer> objInsideReal = new ArrayList<>();
            if (objsInside == null) {
                objInsideReal = null;
            } else {
                for (int id : objsInside) {
                    int newId = cr.mapId(id);
                    objInsideReal.add(newId);
                }
            }
            ContainerMapManagerRemote cmmr = (ContainerMapManagerRemote) RemoteLookupAgent.getManagerForClass(ContainerMapManagerRemote.class);
            cmmr.updateContainer(container.getId(), map.getId(), newSpecs, objInsideReal);
            setExecuted(true);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        if (isExecuted()) {
            ContainerMapManagerRemote cmmr = (ContainerMapManagerRemote) RemoteLookupAgent.getManagerForClass(ContainerMapManagerRemote.class);
            oldSpecs.setHeadline(getObject().getHeadline());
            cmmr.updateContainer(container.getId(), map.getId(), oldSpecs, oldObjsInside);
            setUndone(true);
        }
    }

    @Override
    public String toString() {
        return "Container Update (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "new pos = " + newSpecs.getPosX() + ", " + newSpecs.getPosY() + ", new dim = " + newSpecs.getWidth() + ", " + newSpecs.getHeight();
    }
    
    
    @Override
    public boolean equals(Object obj) {
        boolean equal = super.equals(obj);
        if (equal) {
            CMUpdateContainerCommand nobj = (CMUpdateContainerCommand) obj;
            return this.newSpecs == nobj.newSpecs;
        }
        else {
            return false;
        }
    }

}
