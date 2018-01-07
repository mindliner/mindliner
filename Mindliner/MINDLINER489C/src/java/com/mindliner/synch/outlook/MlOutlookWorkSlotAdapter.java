/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.synch.outlook;

import com.moyosoft.connector.ms.outlook.appointment.OutlookAppointment;

/**
 * This implements a foreign work slot for Outlook
 * @author Marius Messerli
 */
public class MlOutlookWorkSlotAdapter extends MlOutlookAppointmentAdapter{

    public MlOutlookWorkSlotAdapter(OutlookAppointment foreignAppointment) {
        super(foreignAppointment);
    }
    
}
