package com.mindliner.analysis;

import java.io.Serializable;

/**
 * This class describes the current work activity for a user.
 *
 * @author Marius Messerli
 */
public class CurrentWorkTask implements Serializable {

    private final int userId;
    private final int taskId;

    public CurrentWorkTask(int userId, int taskId) {
        this.userId = userId;
        this.taskId = taskId;
    }

    public int getUserId() {
        return userId;
    }

    public int getTaskId() {
        return taskId;
    }

}
