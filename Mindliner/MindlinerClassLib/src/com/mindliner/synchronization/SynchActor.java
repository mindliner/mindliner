/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synchronization;

import com.mindliner.exceptions.synch.SynchConnectionException;
import com.mindliner.objects.transfer.mltSyncher;
import com.mindliner.objects.transfer.mltSynchunit;
import com.mindliner.synchronization.foreign.ForeignObject;
import java.util.Collection;
import java.util.List;

/**
 * This class represents a synchronization worker that takes responsibility for
 * a foreign synch source and an object type.
 *
 * @author Marius Messerli
 */
public abstract class SynchActor {

    public static final String DEFAULT_FOLDER_PATH = "Default";
    protected mltSyncher syncher;

    /**
     * This method is a type and brand specific method to obtain a folder or a
     * source path. The result is written back using setSourceFolder()
     */
    public abstract void chooseSourceFolder();

    /**
     * Determins from the content of the elements whether two items represent
     * the same record. This test should be implemented very stringently so that
     * it rather calls two objects different than identical.
     *
     * @param foreignObject
     * @param mindlinerObject The mindliner object to check against
     * @return True if the content of both objects match sufficiently, false
     * otherwise.
     */
    public abstract boolean isEqual(ForeignObject foreignObject, Object mindlinerObject);

    /**
     * Checks if a ML object is present with similar content.
     *
     * @param foreignObject The foreign object for which a similar ML object is
     * searched.
     * @return The ML object id or -1 if none was found or if the syncher's
     * isContentCheck() if false
     */
    public abstract int getMindlinerObjectWithSimilarContent(ForeignObject foreignObject);

    /**
     * If isContentCheck is true then ML will run isEqual() on new inbound items
     * and only import those who return false. Because this isEqual() check can
     * be costly this prep routine should enable the syncher to build lookup
     * maps before.
     */
    public abstract void prepareForContentCheck();

    public mltSyncher getSyncher() {
        return syncher;
    }

    public void setSyncher(mltSyncher syncher) {
        this.syncher = syncher;
    }

    /**
     * Connects to the foreign store. The method can be called multiple times
     * without overhead or harm.
     *
     * @throws SynchConnectionException if the connection could not be
     * established.
     */
    public abstract void connect() throws SynchConnectionException;

    /**
     * Closes the connection to the synch partner. The method can be called
     * multiple times without overhead or harm.
     */
    public abstract void disconnect();

    /**
     * Synchronizes elements with this data source according to the
     * specifications.
     *
     * separate process
     */
    public abstract void synchronizeElements();

    public abstract void setProgressReporter(MlSynchProgressReporter reporter);

    public abstract void updateForeignObject(ForeignObject foreignObject, int mindlinerObjectId);

    public abstract void updateMindlinerObject(ForeignObject foreignObject, int mindlinerObjectId);

    public abstract ForeignObject getForeignObject(String remoteId);

    /**
     * This call is modelled after Outlook, perhaps needs to be updated later.
     *
     * @param mindlinerObjectId
     * @return
     */
    public abstract ForeignObject createForeignObject(int mindlinerObjectId);

    /**
     * Creates a new Mindliner object of the appropriate class. The object
     * adopts the data pool from the syncher that is requesting the creation.
     *
     * @return The id of the new object.
     */
    public abstract int createMindlinerObject();

    /**
     * This operation deletes a foreign object if the corresponding
     * MindlinerObject was deleted and teh user has configured this syncher to
     * delete the counterpart.
     *
     * @param fo The foreign object to be deleted
     */
    public abstract boolean safeDeleteForeignObject(ForeignObject fo);

    /**
     * Iterates through the foreign data source and creates ForeignObjects for
     * each element found. This call may apply filters as it may block
     * completed/expired or past objects depending on the syncher's pref
     * settings.
     * @return 
     */
    public abstract List<ForeignObject> getForeignObjects();

    public Collection<mltSynchunit> getSynchUnits() {
        return syncher.getSynchUnits();
    }

    public mltSynchunit getSynchUnit(ForeignObject fo) {
        for (mltSynchunit s : syncher.getSynchUnits()) {
            if (s.getForeignObjectId().equals(fo.getId())) {
                return s;
            }
        }
        return null;
    }

    /**
     * Returns a synch unit for the specified mindliner object id
     *
     * @param mindlinerObjectId
     * @return The corresponding synch unit or null if none was found matching
     * the specified id
     */
    public mltSynchunit getSynchUnit(int mindlinerObjectId) {
        for (mltSynchunit s : syncher.getSynchUnits()) {
            if (s.getMindlinerObjectId() == mindlinerObjectId) {
                return s;
            }
        }
        return null;
    }

    public void addSynchUnit(mltSynchunit unit) {
        syncher.getSynchUnits().add(unit);
    }

    /**
     * This function ensures that three modification time stamps are identical:
     * the one of the foreign object, the mindliner object, and the synch unit.
     * @param foreignObject The foreign object of this synch unit
     * @param mindlinerId The mindliner object
     * @param su The synch unit to be updated
     */
    public void updateSynchUnit(ForeignObject foreignObject, int mindlinerId, mltSynchunit su) {
        if (su == null) {
            mltSynchunit synchUnit = getSynchUnit(foreignObject);
            if (synchUnit == null || synchUnit.getMindlinerObjectId() != mindlinerId) {
                return;
            } else {
                su = synchUnit;
            }
        }
        if (su != null) {
            su.setLastSynched(foreignObject.getModificationDate());
        }
    }

    @Override
    public String toString() {
        return syncher.getType().toString() + " / " + syncher.getBrand().toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (this.syncher != null ? this.syncher.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SynchActor other = (SynchActor) obj;
        return this.syncher == other.syncher || (this.syncher != null && this.syncher.equals(other.syncher));
    }

    /**
     * Stores this synch actor's state and synch units to database.
     */
    public abstract void store();
}
