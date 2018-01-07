/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import java.io.Serializable;

/**
 * DAO for the default confidentialities.
 *
 * @author Marius Messerli
 */
public class MltObjectDefaultConfidentialities implements Serializable {

    private int dataPoolId = -1;
    private int confidentialityId = -1;

    public MltObjectDefaultConfidentialities() {
    }

    public MltObjectDefaultConfidentialities(int dataPoolId, int confidentialityId) {
        this.dataPoolId = dataPoolId;
        this.confidentialityId = confidentialityId;
    }

    public int getDataPoolId() {
        return dataPoolId;
    }

    public void setDataPoolId(int dataPoolId) {
        this.dataPoolId = dataPoolId;
    }

    public int getConfidentialityId() {
        return confidentialityId;
    }

    public void setConfidentialityId(int confidentialityId) {
        this.confidentialityId = confidentialityId;
    }

}
