/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.MlsNews;

/**
 * This class is used as transfer news object between server and client.
 * Unlike most other object it also serves as client object since it only
 * holds ids as references.
 * 
 * @author Marius Messerli
 */
public class MltNews extends MltObject {

    private int typeId = -1;
    private final int userObjectId = -1;
    private MltLog log;

    public MltNews(MlsNews news) {
        super(news);
        if (news.getLog() != null) {
            log = new MltLog(news.getLog());
        }
        if (news.getNewsType() != null) {
            typeId = news.getNewsType().getId();
        } else {
            System.err.println("action item type was null, assigning comment");
            typeId = 9;
        }
    }

    public int getTypeId() {
        return typeId;
    }

    public int getUserObjectId() {
        return userObjectId;
    }

    public MltLog getLog() {
        return log;
    }

}
