/*
 * mlMessage.java
 *
 * Created on 05.06.2007, 20:05:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.categories.MlsNewsType;
import com.mindliner.contentfilter.Completable;
import com.mindliner.objects.transfer.MltLog;
import java.io.Serializable;

/**
 * This class describes a work unit. Examples of work units are tasks that need
 * to be completed or delegated or rejected, knowlets that need to be
 * interpreted, communicated, or deleted.
 *
 * Work objects are not stored they are computed at runtime.
 *
 * @author Marius Messerli
 */
public class mlcNews extends mlcObject implements Serializable {

    MlsNewsType newsType = null;
    private int userObjectId = -1;
    private MltLog log;
    private static final long serialVersionUID = 19640205L;

    public void setActionItemType(MlsNewsType at) {
        newsType = at;
    }

    public MlsNewsType getNewsType() {
        return newsType;
    }

    public void setNewsType(MlsNewsType newsType) {
        this.newsType = newsType;
    }

    public int getUserObjectId() {
        return userObjectId;
    }

    public void setUserObjectId(int userObjectId) {
        this.userObjectId = userObjectId;
    }

    public MltLog getLog() {
        return log;
    }

    public void setLog(MltLog log) {
        this.log = log;
    }

}
