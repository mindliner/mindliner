/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis;

/**
 *
 * This class defines Mindliner search types
 * @author Marius Messerli
 */
public class QueryTypes {
    
    public static enum StandardQueryType {
        RecentChanges,
        OverdueTasks,
        UpcomingTasks, 
        PriorityTasks,
        CurrentWorkTasks,
        StandAloneObjects,
        IslandPeaks
    }
    
}
