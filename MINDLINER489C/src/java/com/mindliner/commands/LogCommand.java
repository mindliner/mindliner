/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.categories.MlsEventType;
import com.mindliner.categories.MlsEventType.EventType;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.mlsLog;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.MindlinerMain;
import com.mindliner.managers.LogManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.Date;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * This command can be used to log a client event. Object change events are
 * already logged by the server so you don't need this mechanism. Object
 * reading, on the other hand cannot be logged on the server and needs this
 * command to be executed.
 *
 *
 * @author Marius Messerli
 */
public class LogCommand extends MindlinerOnlineCommand {

    MlsEventType.EventType event;
    String headline;
    Date timeStamp = new Date();
    mlcClient dataPool;

    public LogCommand(mlcObject o, mlcClient dataPool, EventType eventType, String headline) {
        super(o, true);
        this.event = eventType;
        this.headline = headline;
        this.dataPool = dataPool;
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        try {
            super.execute();
            LogManagerRemote lm = (LogManagerRemote) RemoteLookupAgent.getManagerForClass(LogManagerRemote.class);
            lm.remoteLog(dataPool.getId(), event, getObject().getId(), 0, headline, mlsLog.Type.Info, timeStamp);
        } catch (MlAuthorizationException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(),
                    ex.getMessage(),
                    "Event Log",
                    JOptionPane.ERROR_MESSAGE);
        }
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        // cannot undo logging
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Log Action";
    }
    
    

    @Override
    public String getDetails() {
        return "Event: " + event + getObject() == null ? "" : " for object (" + getObject().getHeadline() + ")";
    }

}
