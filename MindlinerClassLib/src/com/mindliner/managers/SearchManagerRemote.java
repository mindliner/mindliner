/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.contentfilter.BaseFilter;
import com.mindliner.contentfilter.TimeFilter.TimePeriod;
import com.mindliner.contentfilter.mlFilterTO;
import com.mindliner.objects.transfer.MltLink;
import com.mindliner.objects.transfer.MltObject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Remote;
import com.mindliner.json.MindlinerObjectJson;

/**
 *
 * @author Marius Messerli
 */
@Remote
public interface SearchManagerRemote {

    /**
     * Obtains a list of signatures of objects that match.
     *
     * @param fto The search filter settings
     * @param searchString The text constraint.
     * @return A list of ids for matching objects.
     */
    public List<Integer> getTextSearchResultsIds(String searchString, mlFilterTO fto);

    /**
     * Returns a map with the specified ids as keys and lists of related objects
     * as values
     *
     * @param ids The ids of objects for which related objects are needed.
     * @return A map with object ids as keys and a list of ids of related
     * objects as values
     */
    public Map<Integer, List<MltLink>> fetchRelativesMap(Collection<Integer> ids);

    /**
     * Returns a list of objects matching the specified headline and description
     * and beloging to the caller's current client.
     *
     * @param headline
     * @param description
     * @return A list of transfer objects matching the specified headline and
     * description and belonging to the caller's current client.
     */
    public List<MltObject> fetchMatchingObjects(String headline, String description);

    /**
     *
     * @param source Set of object id's to which a shortest path from the source
     * should be found
     * @param targets Source object from which the shortest path search to the
     * targets is started
     * @param maxDepth Maximal length of the shortest path, longer paths won't
     * be considered
     * @return List containing the elements of the shortest path. Starting with
     * the target and ending with the source. Null if no shortest path exists.
     */
    public List<Integer> getShortestPath(Set<Integer> targets, Integer source, int maxDepth);

    /**
     * An island peak is the object with the highest rating inside an island.
     * This call returns the peaks of all islands for the caller's data pool.
     *
     * @param minIslandSize The minimal number of objects for an island to be
     * considered
     * @param maxResultSet The maximum number of island peaks returned ordered
     * by decreasing sizes of their respective islands
     * @return A list of ids of objects that represent the island peaks.
     */
    public List<Integer> getIslandPeaks(int minIslandSize, int maxResultSet);

    /**
     * This call ensures that all my tasks that are due in the specified week
     * are indeed on that week's plan.
     *
     * @param year The year
     * @param week The week number within the specified year
     */
    public void ensureMyDueTasksAreOnWeekPlan(int year, int week);

    /**
     * Converts the object of <code>key</code> and all its relatives up to a
     * certain <code>level</code> into a json string
     *
     * @param key
     * @param level
     * @param root the respective json object
     * @return relatives as json string
     */
    public String fetchRelativesJson(int key, int level, MindlinerObjectJson root);

    public String fetchRelativesJson(int key, int level, MindlinerObjectJson root, boolean includeArchived, boolean includePrivate);

    public String fetchRelativesJson(int key, int level, MindlinerObjectJson root, BaseFilter filter);

    /**
     * Returns the ids of overdue tasks owned by the caller
     *
     * @return A list of task ids
     */
    public List<Integer> getOverdueTasksIds();

    /**
     * Returns the ids of upcoming tasks owned by the caller
     *
     * @param period The forward-looking time period in which the tasks' due
     * date must be
     * @return A list of task ids
     */
    public List<Integer> getUpcomingTasksIds(TimePeriod period);

    /**
     * Returns the ids of all open priority tasks owned by the caller.
     *
     * @return
     */
    public List<Integer> getOpenPriorityTasksIds();

    /**
     * This call returns the ids of objects which are stand alone and are not
     * archived
     *
     * @return The ids for all (accessible) non-archived stand-alone objects
     */
    public List<Integer> getActiveStandAloneObjectIds();
}
