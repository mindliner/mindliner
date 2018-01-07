/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This entity implements a syncher between ML and a foreign data source.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "synchers")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Syncher.findAll", query = "SELECT s FROM Syncher s"),
    @NamedQuery(name = "Syncher.findByUserId", query = "SELECT s FROM Syncher s, IN(s.user.clients) client, IN(client.users) user WHERE user.id = :userId")
})
public class Syncher implements Serializable {

    public static enum SourceType {

        ContactType,
        TaskType,
        AppointmentType,
        InfoType,
        WorkUnitType
    }

    public static enum SourceBrand {

        // Use this if the foreign source is Outlook running on a client computer
        Outlook,
        // Use this if the foreign source is a MS-Exchange Server
        ExchangeServer
    }

    /**
     * Defines the direction in which objects are synched that exist only either
     * in Mindliner or in the foreign store. The direction is define from
     * Mindliner's perspective.
     */
    public static enum InitialSynchDirection {

        Export, Import, Both
    }

    public static enum SynchConflictResolution {

        MindlinerWins, ForeignWins, Manual
    }
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID")
    private mlsUser user;
    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;
    @Column(name = "BRAND")
    @Enumerated(EnumType.STRING)
    private SourceBrand sourceBrand;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CLIENT_ID")
    private mlsClient client;
    @Column(name = "IGNORE_COMPLETED")
    private Boolean ignoreCompleted;
    @Column(name = "IMMEDIATE_FOREIGN_UPDATE")
    private Boolean immediateForeignUpdate;
    @Column(name = "DELETE_ON_MISSING_COUNTERPART")
    private Boolean deleteOnMissingCounterpart;
    @Enumerated(EnumType.STRING)
    @Column(name = "INITIAL_DIRECTION")
    private InitialSynchDirection initialDirection;
    @Size(max = 128)
    @Column(name = "SOURCE_FOLDER")
    private String sourceFolder;
    @Enumerated(EnumType.STRING)
    @Column(name = "CONFLICT_RESOLUTION")
    private SynchConflictResolution conflictResolution;
    @Column(name = "CATEGORY_NAME")
    private String categoryName;
    @OneToMany(mappedBy = "syncher")
    private Collection<Synchunit> synchUnits = new ArrayList<>();
    @Basic
    @Column(name = "PERFORM_CONTENT_CHECK")
    private Boolean contentCheck;

    public Syncher() {
    }

    public Syncher(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public mlsUser getUser() {
        return user;
    }

    public void setUser(mlsUser user) {
        this.user = user;
    }

    public mlsClient getClient() {
        return client;
    }

    public void setClient(mlsClient client) {
        this.client = client;
    }

    public Boolean getIgnoreCompleted() {
        return ignoreCompleted;
    }

    public void setIgnoreCompleted(Boolean ignoreCompleted) {
        this.ignoreCompleted = ignoreCompleted;
    }

    public Boolean getImmediateForeignUpdate() {
        return immediateForeignUpdate;
    }

    public void setImmediateForeignUpdate(Boolean immediateForeignUpdate) {
        this.immediateForeignUpdate = immediateForeignUpdate;
    }

    public Boolean getDeleteOnMissingCounterpart() {
        return deleteOnMissingCounterpart;
    }

    public void setDeleteOnMissingCounterpart(Boolean deleteOnMissingCounterpart) {
        this.deleteOnMissingCounterpart = deleteOnMissingCounterpart;
    }

    public String getSourceFolder() {
        return sourceFolder;
    }

    public void setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public SourceBrand getSourceBrand() {
        return sourceBrand;
    }

    public void setSourceBrand(SourceBrand sourceBrand) {
        this.sourceBrand = sourceBrand;
    }

    public InitialSynchDirection getInitialDirection() {
        return initialDirection;
    }

    public void setInitialDirection(InitialSynchDirection initialDirection) {
        this.initialDirection = initialDirection;
    }

    public SynchConflictResolution getConflictResolution() {
        return conflictResolution;
    }

    public void setConflictResolution(SynchConflictResolution conflictResolution) {
        this.conflictResolution = conflictResolution;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    public Collection<Synchunit> getSynchUnits() {
        return synchUnits;
    }

    public void setSynchUnits(Collection<Synchunit> synchUnits) {
        this.synchUnits = synchUnits;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Syncher)) {
            return false;
        }
        Syncher other = (Syncher) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    public Boolean getContentCheck() {
        return contentCheck;
    }

    public void setContentCheck(Boolean contentCheck) {
        this.contentCheck = contentCheck;
    }

    @Override
    public String toString() {
        return "com.mindliner.entities.Syncher[ id=" + id + " ]";
    }
}
