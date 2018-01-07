/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.events;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.MlCacheException;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.main.MindlinerMain;
import com.mindliner.managers.MlMessageHandler;
import com.mindliner.managers.MlMessageHandler.MessageEventType;
import com.mindliner.serveraccess.MessageTrafficControl;
import com.mindliner.serveraccess.OnlineService;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * This class listens to server events.
 *
 * @author Marius Messerli
 */
public class MindlinerMessageListener implements MessageTrafficControl, Runnable, MessageListener, OnlineService {

    private OnlineStatus onlineStatus = OnlineStatus.offline;
    private Connection conn;
    private Session session;
    private boolean ignoreTraffic = false;
    private int connectionPriority = 0;

    @Override
    public void run() {
    }

    @Override
    public void onMessage(Message message) {
        if (!ignoreTraffic) {
            try {
                MapMessage msg = (MapMessage) message;
                int objectId = msg.getInt(MlMessageHandler.ID_PROPERTY_NAME);
                MessageEventType event;
                try {
                    event = (MessageEventType) MessageEventType.valueOf(msg.getString(MlMessageHandler.EVENT_NAME));
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(MindlinerMessageListener.class.getName()).log(Level.WARNING, "Message of unknown event type: {0}", msg.getString(MlMessageHandler.EVENT_NAME));
                    return;
                }
                MindlinerMain.getInstance().incomingMessageReceived();

                switch (event) {
                    case OBJECT_CREATION_EVENT:
                        mlcObject o = CacheEngineStatic.forceFetchServerObject(objectId);
                        if (o != null) { // it is null if caller does not have permission to see
                            ObjectChangeManager.objectCreated(o);
                        }
                        break;

                    case OBJECT_UPDATE_EVENT:
                        // support bulk events for updates.
                        List<Integer> objIds = new ArrayList<>();
                        if (objectId == -1) {
                            Pattern p = Pattern.compile("\\d+");
                            Matcher m = p.matcher(msg.getString(MlMessageHandler.COMMENT));
                            while (m.find()) {
                                objIds.add(Integer.valueOf(m.group()));
                            }
                        } else {
                            objIds.add(objectId);
                        }
                        for (Integer objId : objIds) {
                            o = CacheEngineStatic.forceFetchServerObject(objId);
                            if (o != null) {
                                // if a foreign object was marked private then delete all traces in this (as if it was actually deleted)
                                if (o.isPrivateAccess() && !o.getOwner().equals(CacheEngineStatic.getCurrentUser())) {
                                    ObjectChangeManager.objectDeleted(o);
                                } else {
                                    ObjectChangeManager.objectChanged(o);
                                }
                            }
                        }
                        break;

                    case OBJECT_DELETION_EVENT:

                        // probably a message encoding for the deletion of multiple objects at once
                        if (objectId == -1) {
                            boolean isIdString = true;
                            String candidateIdsString = msg.getString(MlMessageHandler.COMMENT);
                            StringTokenizer st = new StringTokenizer(candidateIdsString, ",");
                            for (; isIdString && st.hasMoreElements();) {
                                String s = st.nextToken();
                                try {
                                    int id = Integer.parseInt(s);
                                    mlcObject deletedObject = CacheEngineStatic.getObject(id);
                                    if (deletedObject != null) {
                                        ObjectChangeManager.objectDeleted(deletedObject);
                                    }
                                } catch (NumberFormatException ex) {
                                    isIdString = false;
                                }
                            }
                        }

                        o = CacheEngineStatic.getObject(objectId);
                        if (o != null) {
                            ObjectChangeManager.objectDeleted(o);
                        }
                        break;

                    case OBJECT_REPLACE_EVENT:
                        int newId = msg.getInt(MlMessageHandler.INT_ARGUMENT);
                        o = CacheEngineStatic.forceFetchServerObject(newId);
                        if (o != null) {
                            ObjectChangeManager.objectReplaced(objectId, o);
                        }
                        break;
                }

            } catch (MlCacheException ex) {
                // do nothing, most likely this was just a message from a user of another client to whom the caller does not have any access rights
            } catch (JMSException ex) {
                Logger.getLogger(MindlinerMessageListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void goOffline() {
        if (onlineStatus.equals(OnlineStatus.online)) {
            try {
                onlineStatus = OnlineStatus.offline;
                session.close();
                conn.close();
            } catch (JMSException ex) {
                Logger.getLogger(MindlinerMessageListener.class.getName()).log(Level.WARNING, null, ex);
            }
        }
    }

    @Override
    public void goOnline() throws MlCacheException {
        if (!onlineStatus.equals(OnlineStatus.online)) {
            try {
                InitialContext context = RemoteLookupAgent.getContext();
                ConnectionFactory connFactory = (ConnectionFactory) context.lookup("jms/MindlinerMessage");
                conn = connFactory.createConnection();
                session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination dest = (Destination) context.lookup("jms/MindlinerObjectEvent");
                MessageConsumer consumer = session.createConsumer(dest);
                consumer.setMessageListener(this);
                conn.start();
                onlineStatus = OnlineStatus.online;
            } catch (NamingException ex) {
                throw new MlCacheException(ex.getMessage());
            } catch (JMSException ex) {
                throw new MlCacheException("Cannot receive object change updates: " + ex.getMessage());
            }
        }
    }

    @Override
    public OnlineStatus getStatus() {
        return onlineStatus;
    }

    @Override
    public void setIgnoreAllTraffic(boolean status) {
        this.ignoreTraffic = status;
    }

    @Override
    public String getServiceName() {
        return "Object Change Listener";
    }

    @Override
    public int getConnectionPriority() {
        return connectionPriority;
    }

    @Override
    public void setConnectionPriority(int priority) {
        connectionPriority = priority;
    }
}
