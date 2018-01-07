/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.categories.mlsConfidentiality;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author marius
 */
@Entity
@Table(name = "foreignsources")
public class mlsForeignDataSource implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "CONTACT_TABLE_NAME")
    public String getContactTableName() {
        return contactTableName;
    }

    @Column(name = "PASS")
    public String getPassWord() {
        return passWord;
    }

    @Column(name = "URL")
    public String getUrl() {
        return url;
    }

    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setClient(mlsClient client) {
        this.client = client;
    }

    public void setContactTableName(String contactTableName) {
        this.contactTableName = contactTableName;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) id;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof mlsForeignDataSource)) {
            return false;
        }
        mlsForeignDataSource other = (mlsForeignDataSource) object;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mindliner.entities.mlsForeignDataSource[id=" + id + "]";
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "CLIENT_ID", referencedColumnName = "ID")
    public mlsClient getClient() {
        return client;
    }

    public void setDefaultContactConfidentiality(mlsConfidentiality defaultContactConfidentiality) {
        this.defaultContactConfidentiality = defaultContactConfidentiality;
    }

    public void setDefaultContactOwner(mlsContact defaultContactOwner) {
        this.defaultContactOwner = defaultContactOwner;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DEFAULT_CONTACT_CONFIDENTIALITY_ID", referencedColumnName = "ID")
    public mlsConfidentiality getDefaultContactConfidentiality() {
        return defaultContactConfidentiality;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DEFAULT_CONTACT_OWNER_ID", referencedColumnName = "ID")
    public mlsContact getDefaultContactOwner() {
        return defaultContactOwner;
    }
    private int id;
    private mlsClient client = null;
    private String url = "";
    private String userName = "";
    private String passWord = "";
    private String contactTableName = "";
    private mlsContact defaultContactOwner = null;
    private mlsConfidentiality defaultContactConfidentiality = null;
    private static final long serialVersionUID = 19640205L;
}
