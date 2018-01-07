/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import javax.ejb.Local;

/**
 *
 * @author Marius Messerli
 */
@Local
public interface WorkManagerLocal {
    
    /**
     * Returns the work report in JSON format for the calling user
     * @param year The year for which the report is requested
     * @param month The month for which the report is requested
     * @return A JSON string with all work units
     */
    public String getWorkUnitsJSON(int year, int month);
    
}
