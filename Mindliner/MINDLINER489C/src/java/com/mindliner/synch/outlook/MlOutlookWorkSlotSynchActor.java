/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.synch.outlook;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcWorkUnit;
import com.mindliner.synchronization.foreign.ForeignObject;
import com.mindliner.synchronization.foreign.ForeignTimeSlot;

/**
 * This actor synchs work units to Outlook
 * 
 * @author Marius Messerli
 */
public class MlOutlookWorkSlotSynchActor extends MlOutlookAppointmentSynchActor{

    @Override
    public void synchronizeElements() {
        System.out.println("operation not implemented yet");
    }

    
    @Override
    public void updateMindlinerObject(ForeignObject fo, int mindlinerObjectId) {
        System.out.println("updateMindlinerObject: ignoring request as this synch actor is uni-directional (Mindliner to Foreign) only");
    }

    @Override
    public void updateForeignObject(ForeignObject foreignObject, int mindlinerObjectId) {
    }

    @Override
    public int getMindlinerObjectWithSimilarContent(ForeignObject foreignObject) {
        return -1;
    }

    @Override
    public void prepareForContentCheck() {
        // nothing to be done here
    }

    @Override
    protected Class getMindlinerObjectClass() {
        return mlcWorkUnit.class;
    }

    @Override
    public boolean isEqual(ForeignObject foreignObject, Object w) {
        ForeignTimeSlot ft = (ForeignTimeSlot) foreignObject;
        mlcWorkUnit wu = (mlcWorkUnit) w;
        mlcTask task = (mlcTask) CacheEngineStatic.getObject(wu.getTaskId());
        if (!ft.getSlotStart().equals(wu.getStart())) return false;
        if (!ft.getSlotEnd().equals(wu.getEnd())) return false;
        if (!ft.getHeadline().equals(task.getHeadline())) return false;
        if (!ft.getDescription().equals(task.getDescription())) return false;
        return true;
    }

    @Override
    public int createMindlinerObject() {
        System.err.println("createMindlinerObject: ignoring request as this synch actor is uni-directional (Mindliner to Foreign) only");
        return -1;
    }
    
    
}
