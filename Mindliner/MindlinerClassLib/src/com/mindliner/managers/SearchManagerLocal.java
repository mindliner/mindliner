/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.analysis.MlClassHandler.MindlinerObjectType;
import com.mindliner.contentfilter.TimeFilter.TimePeriod;
import com.mindliner.contentfilter.mlFilterTO;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsTask;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author Marius Messerli
 */
@Local
public interface SearchManagerLocal {

    /**
     * Searches the database for any object who's headline matches the search
     * string
     *
     * @param searchString The text string to search for
     * @param fto The filter transfer object containing the search filter
     * settings
     * @return
     */
    public List<mlsObject> getTextSearchResults(String searchString, mlFilterTO fto);

    public List<mlsObject> loadRelatives(int objectId, boolean includeArchived, boolean includePrivate);

    /**
     * Returns the mindliner object who's headline matches the tag and contains
     * the Mindliner Object Tag Identifyer
     *
     * @param tag The plain tag
     * @return The tag object if it is accessible by the caller.
     */
    public mlsObject getTagObject(String tag);

    /**
     * Returns objects that are not linked to any other objects. This is a very
     * expensive call and so please use the time period carefully.
     *
     * @return 
     */
    List<mlsObject> getStandAloneObjects();

    /**
     * Returns all overdue tasks owned by the specified user
     *
     * @return
     */
    public List<mlsTask> getOverdueTasks();

    /**
     * Returns upcoming tasks
     *
     * @param period The forward-looking time period in which the tasks' due
     * date must be
     * @return
     */
    public List<mlsTask> getUpcomingTasks(TimePeriod period);

    /**
     * Returns all high priority tasks owned by the caller.
     *
     * @return
     */
    public List<mlsTask> getPriorityTasks();
    
}
