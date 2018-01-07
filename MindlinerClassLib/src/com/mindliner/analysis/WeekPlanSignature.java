/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis;

import java.io.Serializable;

/**
 * Defines the key parameters of a weekplan for quick verification of cache status.
 *
 * @author Marius Messerli
 */
public class WeekPlanSignature implements Serializable {

    private final int id;
    private final int version;

    // this is a thorogh test against the total count of all work units of all tasks of this week plan
    private final int taskCount;

    public WeekPlanSignature(int id, int version, int taskCount) {
        this.id = id;
        this.version = version;
        this.taskCount = taskCount;
    }

    public int getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public int getTaskCount() {
        return taskCount;
    }

}
