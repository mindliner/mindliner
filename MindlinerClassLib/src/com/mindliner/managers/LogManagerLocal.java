/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.MlsEventType;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsLog;
import com.mindliner.entities.mlsObject;
import com.mindliner.exceptions.ForeignClientException;
import java.util.Date;
import java.util.List;
import javax.ejb.Local;

/**
 * The local interface to LogManagerBean
 * 
 * @author Marius Messerli
 */
@Local
public interface LogManagerLocal {
   
    /**
     * Creates a new log record for the specified object.
     * @param dataPool The data pool for this log
     * @param eventType The event type that is logged
     * @param object The object that is linked to the log record; if the object is of type MlsNews then the call is ignored to avoid loops through subscriptions
     * @param linkObjectId The id of the link partner object
     * @param headline The summary of what this log record is about
     * @param method The method that is logged (will be removed soon; use eventType for future)
     * @param type The old type ((will be removed soon; use eventType for future)
     * @param timestamp The event timestamp, specify null if you want the current time to be logged
     */
    public void log(mlsClient dataPool, MlsEventType.EventType eventType, mlsObject object, int linkObjectId, String headline, String method, mlsLog.Type type, Date timestamp);

    
    public void log(mlsClient dataPool, MlsEventType.EventType eventType, mlsObject object, int linkObjectId, String headline, String method, mlsLog.Type type);

    /**
     * This call is for MasterAdmins only to retrieve the log of the specifieduser.
     * @param userId
     * @param numberOfRecords
     * @return
     */
    public List<mlsLog> getLog(int userId, int numberOfRecords);
    
    /**
     * This is a version of the call getLog which is available to Admin roles
     * also (data room admins)
     *
     * @param userId The id of the user for whom the log is requested
     * @param numberOfRecords The maximum number of log records to return
     * @throws ForeignClientException If the user cannot be found or belongs to
     * a foreign client
     * @return
     */
    public List<mlsLog> clientAdminGetUserLog(int userId, int numberOfRecords) throws ForeignClientException;


}
