/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.naming.NamingException;

/**
 *
 * @author Marius Messerli
 */
public class ModificationUpdateCommand extends MindlinerOnlineCommand {

    private Date newDate = null;
    private Date oldDate = null;

    public ModificationUpdateCommand(mlcObject o, Date newDate) {
        super(o, true);
        this.newDate = newDate;
        o.setModificationDate(newDate);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        oldDate = getObject().getModificationDate();
        int version = omr.setModificationDate(getObject().getId(), newDate);
        getObject().setVersion(version);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        int version = omr.setModificationDate(getObject().getId(), oldDate);
        getObject().setVersion(version);
        setUndone(true);
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Modification Date Update (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        return "New modification date = " + sdf.format(newDate);
    }
}
