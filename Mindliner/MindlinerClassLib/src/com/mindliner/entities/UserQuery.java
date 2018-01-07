/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.analysis.MlClassHandler;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author marius
 */
@Entity
@Table(name = "queries")
public class UserQuery implements Serializable {

    private int id;
    private String queryString = "";
    private int userId = -1;
    private Date issueDate = null;
    private String objectClassName = "";
    private int resultCount = 0;
    private static final long serialVersionUID = 19640205L;

    public UserQuery() {
    }

    /**
     * Creates a new user query object.
     *
     * @param userId The user who is issueing the query.
     * @param q The query string
     * @param type The type object selector for the query
     * @param resultCount The number of objects that were returned for this query and the calling user
     */
    public UserQuery(int userId, String q, MlClassHandler.MindlinerObjectType type, int resultCount) {
        this.userId = userId;
        queryString = q;
        issueDate = new Date();
        objectClassName = type.name();
        this.resultCount = resultCount;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserQuery other = (UserQuery) obj;
        if (this.id != other.id) {
            return false;
        }
        if ((this.queryString == null) ? (other.queryString != null) : !this.queryString.equals(other.queryString)) {
            return false;
        }
        if ((this.objectClassName == null) ? (other.objectClassName != null) : !this.objectClassName.equals(other.objectClassName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + this.id;
        hash = 53 * hash + (this.queryString != null ? this.queryString.hashCode() : 0);
        hash = 53 * hash + (this.objectClassName != null ? this.objectClassName.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return queryString;
    }

    @Column(name = "QUERYSTRING")
    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String s) {
        queryString = s;
    }

    @Column(name = "USER_ID")
    public int getUserId() {
        return userId;
    }

    public void setUserId(int id) {
        userId = id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ISSUEDATE")
    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date d) {
        issueDate = d;
    }

    @Column(name = "OBJECT_CLASS_NAME")
    public String getObjectClassName() {
        return objectClassName;
    }

    public void setObjectClassName(String s) {
        objectClassName = s;
    }

    @Column(name="RESULT_COUNT")
    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }

    
}
