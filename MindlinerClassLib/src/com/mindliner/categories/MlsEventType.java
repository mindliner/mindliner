/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.categories;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Describes a Mindliner event which can be used for things like logging and
 * subscription.
 *
 * @author Marius Messerli
 */
public class MlsEventType extends mlsMindlinerCategory implements Serializable {

    public static enum EventType {

        Any, // used in subscriptions to show that the subscriber is interested in any event
        ObjectCreated,
        ObjectUpdated,
        ObjectTypeChanged,
        ObjectDeleted,
        ObjectLinked,
        ObjectLinkPositionUpdate,
        ObjectUnlinked,
        ObjectNavigated,
        ObjectRead,
        ObjectWorkUnitAdded,
        ObjectWorkUnitRemoved,
        ObjectAddedToWeekplan,
        ObjectRemovedFromWeekplan,
        ClientCreated,
        ClientModified,
        ClientDeleted
    }

    /**
     * Returns a list of all EventType values that are available to all users.
     * It excludes the events that are reserved to MasterAdmin and Admin roles.
     *
     * @return A list of EventTypes that are available to ordinary users
     */
    public static List<EventType> getUserEventTypes() {
        // create a copy, I cannot use the "asList" wrapper as it does not allow removal
        List<EventType> userEvents = new ArrayList<>(Arrays.asList(EventType.values()));
        userEvents.remove(EventType.ClientCreated);
        userEvents.remove(EventType.ClientDeleted);
        userEvents.remove(EventType.ClientModified);
        return userEvents;
    }
}
