/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.categories.MlsNewsType;
import com.mindliner.contentfilter.Completable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * An action item is an object to which the user has to take some action, either
 * read it, complete it, or do something else with it.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "news")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue(value = "NEWS")
@NamedQuery(name = "MlsNews.findActiveByUser", query = "SELECT n FROM MlsNews n WHERE n.owner=:owner AND n.archived=false")
public class MlsNews extends mlsObject {

    MlsNewsType newsType = null;
    private mlsLog log;

    @ManyToOne
    @JoinColumn(name = "LOG_ID", referencedColumnName = "ID")
    public mlsLog getLog() {
        return log;
    }

    public void setLog(mlsLog log) {
        this.log = log;
    }

    public void setNewsType(MlsNewsType at) {
        newsType = at;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "NEWS_TYPE_ID", referencedColumnName = "ID")
    public MlsNewsType getNewsType() {
        return newsType;
    }

}
