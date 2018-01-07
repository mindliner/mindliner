/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.Colorizer;
import com.mindliner.entities.MlsColorScheme;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author M.Messerli Created on 18.09.2012, 10:01:12
 */
public class MlTransferColorScheme implements Serializable {

    private int id;
    private int version;
    private String name;
    private int ownerId;
    private Date modification = new Date();
    private List<MltColorizer> colorizers = new ArrayList<>();

    public MlTransferColorScheme() {
    }

    public MlTransferColorScheme(MlsColorScheme scs) {
        id = scs.getId();
        version = scs.getVersion();
        name = scs.getName();
        ownerId = scs.getOwner().getId();
        modification = scs.getModification();
        for (Colorizer clrzr : scs.getColorizers()) {
            colorizers.add(new MltColorizer(clrzr));
        }
    }

    public List<MltColorizer> getColorizers() {
        return colorizers;
    }

    public int getId() {
        return id;
    }

    public Date getModification() {
        return modification;
    }

    public String getName() {
        return name;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getVersion() {
        return version;
    }

    public void setColorizers(List<MltColorizer> colorizers) {
        this.colorizers = colorizers;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
    
    
}
