/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.events;

import com.mindliner.categories.MlsEventType;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.LogCommand;

/**
 * Convenience class for logging user events
 * @author Marius Messerli
 */
public class MlEventLogger {

    public static void logReadEvent(mlcObject o) {
        CommandRecorder.getInstance().scheduleCommand(new LogCommand(o, o.getClient(), MlsEventType.EventType.ObjectRead, o.getHeadline()));
    }

}
