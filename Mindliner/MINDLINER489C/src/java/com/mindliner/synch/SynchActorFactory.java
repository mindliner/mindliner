package com.mindliner.synch;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.entities.Syncher.SourceBrand;
import com.mindliner.entities.Syncher.SourceType;
import com.mindliner.synch.outlook.MlOutlookAppointmentSynchActor;
import com.mindliner.synch.outlook.MlOutlookContactSynchActor;
import com.mindliner.synch.outlook.MlOutlookTaskSyncher;
import com.mindliner.synch.outlook.MlOutlookWorkSlotSynchActor;
import com.mindliner.synchronization.SynchActor;

/**
 * This class creates a new synch actor and associated data structures.
 * @author Marius Messerli
 */
public class SynchActorFactory {

    public static SynchActor createSynchActor(SourceType type, SourceBrand brand) {
        if (!CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.SYNCH_BASICS)) {
            throw new IllegalStateException("Client does not have required authorization to use the synch subsystem.");
        }
        switch (type) {
            case AppointmentType:
                switch (brand) {
                    case Outlook:
                        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.SYNCH_OUTLOOK)) {
                            return new MlOutlookAppointmentSynchActor();
                        } else {
                            throw new IllegalStateException("Client does not have required authorization to use the Outlook synchronization subsystem.");
                        }
                    default:
                        throw new AssertionError();
                }

            case ContactType:
                switch (brand) {
                    case Outlook:
                        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.SYNCH_OUTLOOK)) {
                            return new MlOutlookContactSynchActor();
                        } else {
                            throw new IllegalStateException("Client does not have required authorization to use the Outlook synchronization subsystem.");
                        }
                    default:
                        throw new AssertionError();
                }

            case InfoType:
                return null;

            case TaskType:
                switch (brand) {
                    case Outlook:
                        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.SYNCH_OUTLOOK)) {
                            return new MlOutlookTaskSyncher();
                        } else {
                            throw new IllegalStateException("Client does not have required authorization to use the Outlook synchronization subsystem.");
                        }
                    default:
                        throw new AssertionError();
                }
            
            case WorkUnitType:
                switch (brand) {
                    case Outlook:
                        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.SYNCH_OUTLOOK)) {
                            return new MlOutlookWorkSlotSynchActor();
                        }
                        else {
                            throw new IllegalStateException("Client does not have required authorization to use the Outlook synchronization subsystem.");
                        }

                    default:
                        throw new AssertionError();
                }

            default:
                throw new AssertionError();
        }
    }
}
