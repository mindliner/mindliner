/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity representing the create/delete/modify stats for one user on one client at one point in time
 * @author Dominic Plangger
 */
@Entity
@Table(name = "userreports")
@NamedQueries({
    @NamedQuery(name = "MlsUserReport.getReports", query = "SELECT ur FROM MlsUserReport ur WHERE (ur.creationDate BETWEEN :startDate AND :endDate) AND ur.client.id IN :clientIds AND ur.user.id = :userId ORDER BY ur.creationDate ASC")
})
public class MlsUserReport implements Serializable {
    
    private int id = -1;
    private Date creationDate = new Date();
    private mlsUser user = null;
    private mlsClient client = null;
    private int createCnt;
    private int removeCnt;
    private int modifyCnt;
    private int selfLinksCnt;
    private int foreignLinksCnt;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    public int getId() {
        return id;
    }
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    public mlsUser getUser() {
        return user;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATION_TS")
    public Date getCreationDate() {
        return creationDate;
    }
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CLIENT_ID", referencedColumnName = "ID")
    public mlsClient getClient() {
        return client;
    }
    
    @Column(name="CREATE_CNT")
    public int getCreateCount() {
        return createCnt;
    }
    
    @Column(name="REMOVE_CNT")
    public int getRemoveCount() {
        return removeCnt;
    }
    
    @Column(name="MODIFY_CNT")
    public int getModifyCount() {
        return modifyCnt;
    }
    
    @Column(name="SELF_LINKS_CNT")
    public int getSelfLinksCount() {
        return selfLinksCnt;
    }
    
    @Column(name="FOREIGN_LINKS_CNT")
    public int getForeignLinksCount() {
        return foreignLinksCnt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setUser(mlsUser user) {
        this.user = user;
    }

    public void setClient(mlsClient client) {
        this.client = client;
    }

    public void setCreateCount(int createCnt) {
        this.createCnt = createCnt;
    }

    public void setRemoveCount(int removeCnt) {
        this.removeCnt = removeCnt;
    }

    public void setModifyCount(int modifyCnt) {
        this.modifyCnt = modifyCnt;
    }

    public void setSelfLinksCount(int selfLinksCnt) {
        this.selfLinksCnt = selfLinksCnt;
    }

    public void setForeignLinksCount(int foreignLinksCnt) {
        this.foreignLinksCnt = foreignLinksCnt;
    }
    
    
}
