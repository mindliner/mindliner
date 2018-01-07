/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.mindliner.managers.WorkManagerLocal;
import java.util.Calendar;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author Marius Messerli
 */
@ManagedBean
@ViewScoped
public class WorkBB {

    @EJB
    private WorkManagerLocal workManager;

    private int year;
    private int month;

    @PostConstruct
    public void init() {
        Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        System.out.println("WorkBB created: year = " + year + ", month = " + month);
    }

    public String getWorkUnits() {
        return workManager.getWorkUnitsJSON(year, month);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

}
