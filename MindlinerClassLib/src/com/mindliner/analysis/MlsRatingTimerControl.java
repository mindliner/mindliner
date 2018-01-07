/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis;

import com.mindliner.analysis.RatingEngineFactory.EngineType;
import java.io.Serializable;
import java.util.Date;
import javax.ejb.TimerHandle;

/**
 * This class provides the parameters for timers that deal with object rating.
 * It also provides a handle to the timer so that timers can be cancelled later.
 *
 * @author Marius Messerli
 */
public class MlsRatingTimerControl implements Serializable {

    private final Date start;
    private final long intervall;
    private final int batchSize;
    private final int clientId;
    private final double inheritanceRate;
    private final double expirationDiscount;
    private TimerHandle timer;
    private final EngineType engineType;
    private static final long serialVersionUID = 19640205L;

    public MlsRatingTimerControl(Date start, long intervall, int clientId, double inheritanceRate, double expirationDiscount, EngineType engineType, int batchSize) {
        this.start = start;
        this.intervall = intervall;
        this.clientId = clientId;
        this.inheritanceRate = inheritanceRate;
        this.expirationDiscount = expirationDiscount;
        this.engineType = engineType;
        this.batchSize = batchSize;
    }

    public Date getStart() {
        return start;
    }

    public long getIntervall() {
        return intervall;
    }

    public int getClientId() {
        return clientId;
    }

    public double getInheritanceRate() {
        return inheritanceRate;
    }

    public double getExpirationDiscount() {
        return expirationDiscount;
    }

    public EngineType getEngineType() {
        return engineType;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public TimerHandle getTimer() {
        return timer;
    }

    public void setTimer(TimerHandle timer) {
        this.timer = timer;
    }

    @Override
    public String toString() {
        return "Client = " + clientId + ", intervall (mins) = " + intervall / 1000 / 60 + ", inheritance = " + inheritanceRate;
    }
}
