/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsUser;
import java.io.Serializable;

/**
 * @todo add List<mlsConfidentiality> confidentialities as a field and copy from mlsClient
 * @author Marius Messerli
 */
public class mltClient implements Serializable {

    private int id;
    private String name = "";
    private boolean active = false;
    private int version = -1;
    private mlsUser owner;
    private static final long serialVersionUID = 19640205L;

    public mltClient(mlsClient c) {
        id = c.getId();
        name = c.getName();
        active = c.getActive();
        version = c.getVersion();
        owner = c.getOwner();
    }

    public boolean isActive() {
        return active;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }
    
    public mlsUser getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return name;
    }
}
