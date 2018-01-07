/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.MlsEventType.EventType;
import com.mindliner.entities.mlsLog;
import com.mindliner.exceptions.ForeignClientException;
import com.mindliner.objects.transfer.MltLog;
import java.util.Date;
import java.util.List;
import javax.ejb.Remote;

/**
 *
 * @author Marius Messerli
 */
@Remote
public interface LogManagerRemote {

    /**
     * This call is for MasterAdmins only who retrieve the log of the specified
     * user.
     *
     * @param userId
     * @param numberOfRecords
     * @return
     */
    public List<MltLog> getLogTransferObjects(int userId, int numberOfRecords);
    
    /**
     * Returns the log records with the specified ids.
     * @param ids
     * @return 
     */
    public List<MltLog> getLogRecords(List<Integer> ids);

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

    public List<mlsLog> getLogForObject(int objectId, int recordCount);

    public enum mlRemoteLogMethod {

        mlUpgrade, mlSynchronize, mlCreation
    };

    /**
     * Returns a list of ids for objects that have changed between the last
     * logout and the last login
     *
     * @return A list of object ids that were changed/created
     */
    public List<Integer> getChangeList();

    /**
     * 
     * @param dataPoolId
     * @param eventType
     * @param objectId
     * @param linkObjectId
     * @param headline
     * @param type
     * @param timestamp The event timestamp - specify null if the record should get the current time
     */
    public void remoteLog(int dataPoolId, EventType eventType, int objectId, int linkObjectId, String headline, mlsLog.Type type, Date timestamp);
}
