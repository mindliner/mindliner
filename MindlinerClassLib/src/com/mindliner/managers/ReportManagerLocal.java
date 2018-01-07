/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.mlsClient;
import java.util.Date;
import javax.ejb.Local;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 *
 * @author Marius Messerli
 */
@Local
public interface ReportManagerLocal {

    /**
     * Returns the number of objects owned by the specified user
     *
     * @param userId The id of the user for whom the object count is required
     * @return The number of objects owned by the specified user
     */
    public int getObjectCount(@QueryParam("userId") int userId);

    /**
     * Returns the number of objects belonging to the specified datapool
     *
     * @param c
     * @return
     */
    public int getObjectCount(mlsClient c);

    /**
     * This call returns the number of all the Mindliner objects
     *
     * @return
     */
    public int getObjectCount();

    /**
     * The number of links in all of Mindliner
     *
     * @return
     */
    public int getLinkCount();

    /**
     * The number of online users
     *
     * @return
     */
    public int getLoggedInUserCount();

    /**
     * The last time an object was modified in the entire application.
     *
     * @return
     */
    public Date getLastObjectModification();
}
