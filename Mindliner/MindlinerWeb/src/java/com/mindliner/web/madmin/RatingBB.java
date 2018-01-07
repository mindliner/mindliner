/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.madmin;

import com.mindliner.analysis.MlsRatingTimerControl;
import com.mindliner.analysis.RatingEngineFactory.EngineType;
import com.mindliner.managers.RatingAgentRemote;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * This back bean support all rating functions.
 *
 * @author Marius Messerli
 */
@ManagedBean()
@SessionScoped
public class RatingBB implements Serializable {

    private EngineType engineType = EngineType.One;
    private double inheritanceRate = 0.1;
    private double expirationDiscount = 2D;
    private int batchSize = 15000;
    private Date timerStart = new Date();
    private int timerIntervallMinutes = 60;
    private MlsRatingTimerControl timerControl;

    @EJB
    RatingAgentRemote ratingAgent;

    /**
     * Creates a new instance of RatingBB
     */
    public RatingBB() {
    }

    public EngineType getEngineType() {
        return engineType;
    }

    public void setEngineType(EngineType engineType) {
        this.engineType = engineType;
    }

    public List<EngineType> getEngineTypes() {
        return Arrays.asList(EngineType.values());
    }

    public void initializeRating(int dataPoolId) {
        ratingAgent.initializeRatingDetails(dataPoolId);
    }

    public void run(int dataPoolId) {
        MlsRatingTimerControl rt = new MlsRatingTimerControl(new Date(), 0, dataPoolId, inheritanceRate, expirationDiscount, EngineType.One, batchSize);
        ratingAgent.updateRatings(rt);
    }

    public double getInheritanceRate() {
        return inheritanceRate;
    }

    public void setInheritanceRate(double inheritanceRate) {
        this.inheritanceRate = inheritanceRate;
    }

    public double getExpirationDiscount() {
        return expirationDiscount;
    }

    public void setExpirationDiscount(double expirationDiscount) {
        this.expirationDiscount = expirationDiscount;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public Date getTimerStart() {
        return timerStart;
    }

    public void setTimerStart(Date timerStart) {
        this.timerStart = timerStart;
    }

    public int getTimerIntervallMinutes() {
        return timerIntervallMinutes;
    }

    public void setTimerIntervallMinutes(int timerIntervallMinutes) {
        this.timerIntervallMinutes = timerIntervallMinutes;
    }

    public MlsRatingTimerControl getTimerControl() {
        return timerControl;
    }

    public void setTimerControl(MlsRatingTimerControl timerControl) {
        this.timerControl = timerControl;
    }
    
    public List<MlsRatingTimerControl> getTimers(){
        return ratingAgent.getTimerControls();
    }
    

    public void createTimer(int dataPoolId) {
        int intervallMillis = timerIntervallMinutes * 60 * 1000;
        MlsRatingTimerControl rt = new MlsRatingTimerControl(timerStart, intervallMillis, dataPoolId,
                inheritanceRate, expirationDiscount, EngineType.One, batchSize);
        ratingAgent.createTimer(rt);
    }

}
