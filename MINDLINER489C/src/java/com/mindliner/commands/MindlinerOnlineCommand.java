/*
 * This class implements a command that is available in offline mode but who's full impact is
 * reached only after the next server synch.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.MindlinerMain;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * The base class for all online commands.
 *
 * The operation mode of commands is the following: 1. the command constructor
 * is created providing the client object on which the command takes place 2. in
 * the constructor, the client object's values to be changed are stored
 * (previousXXX) before updating the client object's attributes to reflect the
 * changes 3. in the execute() function the appropriate chagnes are made on the
 * server 4. if undo() is called the client object's attributes are reverted to
 * their state prior to this command and, if the command has been executed the
 * server changes are also reverted.
 *
 * @author Marius Messerli
 */
public abstract class MindlinerOnlineCommand extends MindlinerCommand {

    /**
     * In order to keep the client up to date in offline mode the constructor
     * must ensure that the client's data structures reflect the command's
     * change right after construction. This includes update of the cache
     * subsystem.
     *
     * @param o The object to operate on. In some rare cases (e.g. creation
     * command) this is ignored, therefore it can't be tested for null.
     * @param overriding True if only the last instance of this command is
     * relevant, false otherwise (e.g. only the last headline update is
     * relevant, any previous can be ingored)
     */
    public MindlinerOnlineCommand(mlcObject o, boolean overriding) {
        super(o, overriding);
    }

    public abstract boolean isVersionChecking();

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        mapTemporaryObjectId(getObject());
        if (isVersionChecking()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            int serverVersion = omr.getVersion(getObject().getId());
            if (getObject().getVersion() != serverVersion) {
                int response = showConflictDialog();
                if (response == 1) {
                    throw new mlModifiedException("Object modified: server version=" + serverVersion + ", local version=" + getObject().getVersion());
                }
            }
            if (OnlineManager.isOnline() == false) {
                throw new IllegalStateException("Cannot execute this command in offline mode");
            }
        }
    }

    protected int showConflictDialog() {
        String[] options = {"Post My Changes Anyway", "Don't Post and Resolve Manually"};
        return JOptionPane.showOptionDialog(MindlinerMain.getInstance(),
                "Object was modified in the meantime",
                getObject().getHeadline(),
                0,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);
    }

    /**
     * Maps a temporary local (negative) ID to the ID that this object got from
     * the server.
     *
     * @param o The object who's ID needs to be maped.
     */
    public static void mapTemporaryObjectId(mlcObject o) {
        // some commands don't take an object to operate and may have this null
        if (o == null) {
            return;
        }
        if (o.getId() < 0) {
            CommandRecorder cr = CommandRecorder.getInstance();
            int newId = cr.mapId(o.getId());
            if (newId <= 0) {
                throw new IllegalStateException("Negative ID detected that cannot be mapped to real ID: " + o.getId());
            }
            o.setId(newId);
        }
    }

    /**
     * Returns the ID fo the associated object. Temporary Ids are marked as such
     * by prefixing it with the letter N and reporting the id as positive
     * numbers. Permanent Ids are returned unchanged.
     *
     * @return
     */
    protected String getFormattedId() {
        if (getObject() != null) {
            if (getObject().getId() < 0) {
                return "N" + -getObject().getId();
            } else {
                return Integer.toString(getObject().getId());
            }
        } else {
            return "N";
        }
    }

    protected String getFormattedId(int id2) {
        if (id2 < 0) {
            return "N" + -id2;
        } else {
            return Integer.toString(id2);
        }
    }

    protected boolean versionCheck() throws NamingException {
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        int serverVersion = omr.getVersion(getObject().getId());
        if (getObject().getVersion() != serverVersion) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj.getClass().equals(this.getClass()))) {
            return false;
        }
        if (getObject() == null || obj == null) {
            return false;
        }
        MindlinerOnlineCommand that = (MindlinerOnlineCommand) obj;
        if (getObject().equals(that.getObject()) && this.getClass().equals(that.getClass())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7 + getObject().getId();
        return hash;
    }
}
