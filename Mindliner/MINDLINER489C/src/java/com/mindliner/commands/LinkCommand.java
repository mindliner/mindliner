/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.MlLinkException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.LinkManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.MindlinerMain;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class LinkCommand extends MindlinerOnlineCommand {
    
    private mlcObject object2 = null;
    private boolean isOneWay;
    private LinkRelativeType type;
    
    public LinkCommand(mlcObject o, mlcObject o2, boolean isOneWay, LinkRelativeType type) {
        super(o, true);
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        if (o2 == null) {
            throw new IllegalArgumentException("The second argument must not be null.");
        }
        this.isOneWay = isOneWay;
        this.object2 = o2;
        this.type = type;
        if (type.equals(LinkRelativeType.OBJECT)) {
            CacheEngineStatic.addRelative(o, o2, isOneWay);
            updateNonCellRelatives(o, o2, true, isOneWay);
        }
    }

    public LinkCommand(mlcObject o, mlcObject o2, boolean isOneWay) {
        this(o, o2, isOneWay, LinkRelativeType.OBJECT);
    } 

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        try {
            super.execute();
            mapTemporaryObjectId(object2);
            
            // @todo - move all of this to the cache subsystem
            LinkManagerRemote linker = (LinkManagerRemote) RemoteLookupAgent.getManagerForClass(LinkManagerRemote.class);
            int[] vers = linker.link(getObject().getId(), object2.getId(), isOneWay, type);
            
            // @todo - do we REALLY need this? a link will trigger an update message that automatically 
            // executes a forceFetchServerObject that will get the object with the new version
            if (vers.length == 2) {
                getObject().setVersion(vers[0]);
                if (!isOneWay) {
                    object2.setVersion(vers[1]);
                }
            }
            setExecuted(true);
        } catch (MlLinkException | NonExistingObjectException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), "Linking Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        CacheEngineStatic.removeRelative(getObject(), object2, isOneWay);
        updateNonCellRelatives(getObject(), object2, false, isOneWay);
        if (isExecuted()) {
            LinkManagerRemote lmr = (LinkManagerRemote) RemoteLookupAgent.getManagerForClass(LinkManagerRemote.class);
            int[] vers = lmr.unlink(getObject().getId(), object2.getId(), true, type);
            if (vers.length == 2) {
                getObject().setVersion(vers[0]);
                if (!isOneWay) {
                    object2.setVersion(vers[1]);
                }
            }
            setUndone(true);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.object2 != null ? this.object2.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LinkCommand other = (LinkCommand) obj;
        if (other.isOneWay == this.isOneWay && other.object2.equals(this.object2) && other.getObject().equals(this.getObject())) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Linking (" + getFormattedId() + " to " + getFormattedId(object2.getId()) + ")";
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }
    
    // updates the number of non-cell relatives for both objects
    public static void updateNonCellRelatives(mlcObject o, mlcObject o2, boolean incr, boolean isOneWay) {
        System.out.println("LinkCommand.updateNonCellRelatives: don't think we use this anymore");
    }
    
    public static void updateNonCellRelatives(mlcObject o, mlcObject o2, boolean incr) {
        updateNonCellRelatives(o, o2, incr, false);
    }

    public mlcObject getObject2() {
        return object2;
    }

    public void setObject2(mlcObject object2) {
        this.object2 = object2;
    }

    public LinkRelativeType getType() {
        return type;
    }
    
}
