/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Marius Messerli Created on 16.09.2012, 18:53:48
 */
@Entity
@Table(name = "colorizers")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Colorizer.findAll", query = "SELECT m FROM Colorizer m"),
    @NamedQuery(name = "Colorizer.findByColorizerClassName", query = "SELECT m FROM Colorizer m WHERE m.colorizerClassName = :colorizerClassName")})
public class Colorizer implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // the id field is used only for persistence and because the composite primary key mechanism (class name and scheme) did not work for me
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "COLORIZER_CLASS_NAME")
    private String colorizerClassName;
    @Basic(optional = false)
    @NotNull
    @Column(name = "MINIMUM_OR_THRESHOLD")
    private double minimumOrThreshold;
    @Basic(optional = false)
    @NotNull
    @Column(name = "MAXIMUM")
    private double maximum;
    @NotNull
    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private ColorizerValueType type;
    @OneToMany(mappedBy = "colorizer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<MlsColor> colors = new ArrayList<>();
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SCHEME_ID", referencedColumnName = "ID")
    private MlsColorScheme scheme;

    public static enum ColorDriverAttribute {
        TaskPriority,
        Owner,
        ModificationAge,
        Rating,
        Confidentiality,
        FixedKey,
        Brizwalk,
        DataPool
    }

    public enum ColorizerValueType {

        DiscreteStates,
        Threshold,
        Continuous
    }

    public enum ColorizerRangeKeys {

        MinimumBoundary,
        MaximumBoundary
    }

    public enum ColorizerThresholdKeys {

        InRange,
        OutOfRange
    }

    public Colorizer() {
    }

    public String getColorizerClassName() {
        return colorizerClassName;
    }

    public void setColorizerClassName(String colorizerClassName) {
        this.colorizerClassName = colorizerClassName;
    }

    public double getMinimumOrThreshold() {
        return minimumOrThreshold;
    }

    public void setMinimumOrThreshold(double minimumOrThreshold) {
        this.minimumOrThreshold = minimumOrThreshold;
    }

    public double getMaximum() {
        return maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public ColorizerValueType getType() {
        return type;
    }

    public void setType(ColorizerValueType type) {
        this.type = type;
    }

    public void setColors(List<MlsColor> colors) {
        this.colors = colors;
    }

    /**
     * Retrieves the colors of the scheme lazily as we sometimes only want the
     * color names for a listing.
     * @return 
     */
    public List<MlsColor> getColors() {
        return colors;
    }

    public void setScheme(MlsColorScheme s) {
        scheme = s;
    }

    public MlsColorScheme getScheme() {
        return scheme;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (colorizerClassName != null ? colorizerClassName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Colorizer)) {
            return false;
        }
        Colorizer other = (Colorizer) object;
        return (this.colorizerClassName != null || other.colorizerClassName == null) && (this.colorizerClassName == null || this.colorizerClassName.equals(other.colorizerClassName));
    }

    @Override
    public String toString() {
        return "colorizerClassName=" + colorizerClassName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
