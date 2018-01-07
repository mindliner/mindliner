/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synch.outlook;

import com.mindliner.synchronization.foreign.ForeignAppointment;
import com.moyosoft.connector.ms.outlook.appointment.OutlookAppointment;
import com.moyosoft.connector.ms.outlook.item.SensitivityType;
import com.sun.istack.logging.Logger;
import java.util.Date;
import java.util.logging.Level;

/**
 * This class implements a ForeignAppointment for Outlook's Appointment
 *
 * @author Marius Messerli
 */
public class MlOutlookAppointmentAdapter extends ForeignAppointment {

    private final OutlookAppointment foreignAppointment;

    public MlOutlookAppointmentAdapter(OutlookAppointment foreignAppointment) {
        this.foreignAppointment = foreignAppointment;
    }

    @Override
    public String getId() {
        return foreignAppointment.getItemId().getEntryId();
    }

    @Override
    public String getHeadline() {
        return foreignAppointment.getSubject();
    }

    @Override
    public void setHeadline(String headline) {
        foreignAppointment.setSubject(headline);
    }

    @Override
    public String getDescription() {
        return foreignAppointment.getBody();
    }

    @Override
    public void setDescription(String description) {
        foreignAppointment.setBody(description);
    }

    @Override
    public Date getModificationDate() {
        return foreignAppointment.getLastModificationTime();
    }

    @Override
    public Date getCreationDate() {
        return foreignAppointment.getCreationTime();
    }

    @Override
    public boolean isPrivate() {
        return foreignAppointment.getSensitivity().equals(SensitivityType.PRIVATE);
    }

    @Override
    public void setPrivacyFlag(boolean privacy) {
        if (privacy) {
            foreignAppointment.setSensitivity(SensitivityType.PRIVATE);
        } else {
            foreignAppointment.setSensitivity(SensitivityType.NORMAL);
        }
    }

    @Override
    public void setOwnerName(String name) {
    }

    @Override
    public String getOwnerName() {
        return "";
    }

    @Override
    public void save() {
        foreignAppointment.save();
    }

    @Override
    public void delete() {
        foreignAppointment.delete();
    }

    @Override
    public void setCategory(String category) {
        foreignAppointment.setCategories(category);
    }

    @Override
    public String getCategory() {
        return foreignAppointment.getCategories();
    }

    @Override
    public boolean isCompleted() {
        Date now = new Date();
        if (now.after(foreignAppointment.getEnd())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setLifetime(int days) {
        Logger.getLogger(MlOutlookAppointmentAdapter.class).log(Level.WARNING, "Cannot set the lifetime of a foreign appointment; please update start time in Outlook. Appointment is: " + foreignAppointment.getSubject());
    }

    @Override
    public int getLifetime() {
        Date appointmentEnd = foreignAppointment.getEnd();
        Date now = new Date();
        int lifeTime = (int) ((appointmentEnd.getTime() - now.getTime()) / 1000 / 60 / 60 / 24) + EXTRA_LIFETIME;
        return lifeTime;
    }
}
