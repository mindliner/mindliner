/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * This class sends message regarding created, changed, deleted objects to
 * interested clients. Because this is a POJO (I don't want to duplicate code in
 * all the clients of this class) I have to use JNDI lookups instead of the more
 * convenient injection of resources.
 *
 * @todo Ideally The connection, session, producer would be created only once
 * per ML session. I didn't find a way to get this done properly.
 *
 * @author Marius Messerli
 */
public class MlMessageHandler {

    public static final String ID_PROPERTY_NAME = "ObjectID";
    public static final String EVENT_NAME = "MessageEvent";
    public static final String CALLER_LOGIN = "CallerLogin";
    public static final String INT_ARGUMENT = "Argument";
    public static final String COMMENT = "Comment";

    public static enum MessageEventType {

        OBJECT_CREATION_EVENT,
        OBJECT_UPDATE_EVENT,
        OBJECT_DELETION_EVENT,
        LOGIN_EVENT,
        LOGOUT_EVENT,
        OBJECT_REPLACE_EVENT,
        QUERY_EVENT,
        USER_CREATION_EVENT,
        CLIENT_CREATION_EVENT
    }
    private Session session = null;
    private MessageProducer producer = null;
    private Connection connection;
    private Topic topic;

    /**
     *
     * @param caller The user who sends this message. Ensure that the clients
     * are instantiated by calling getClients().size() on the user before
     * passing this argument
     * @param object
     * @param eventType One of the static type Strings specified by this class
     * @param comment A comment that is passed along with the message to the
     * consumers
     */
    public void sendMessage(mlsUser caller, mlsObject object, MessageEventType eventType, String comment) {
        sendMessage(caller, object, -1, eventType, comment);
    }

    public void sendMessage(mlsUser caller, mlsObject object, int arg, MessageEventType eventType, String comment) {
        try {
            if (connection == null) {
                InitialContext ctx = new InitialContext();
                ConnectionFactory factory = (ConnectionFactory) ctx.lookup("jms/MindlinerMessage");
                if (factory == null) {
                    return;
                }
                connection = factory.createConnection();
                if (connection == null) {
                    return;
                }
            }
            if (topic == null) {
                InitialContext ctx;
                ctx = new InitialContext();
                topic = (Topic) ctx.lookup("jms/MindlinerObjectEvent");
            }
            if (session == null) {
                session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            }
            if (producer == null) {
                producer = session.createProducer(topic);
            }
            MapMessage msg = session.createMapMessage();
            msg.setInt(ID_PROPERTY_NAME, object == null ? -1 : object.getId());
            msg.setString(EVENT_NAME, eventType.toString());
            msg.setString(CALLER_LOGIN, caller.getUserName());
            msg.setString(COMMENT, comment);
            msg.setInt(INT_ARGUMENT, arg);
            producer.send(msg);
        } catch (NamingException | JMSException ex) {
            Logger.getLogger(MlMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This convenience method is used for bulk operation on a series of
     * objects.
     *
     * @param caller The user who initiates the call
     * @param objectIds The objects that were affected
     * @param event The event
     * @param comment An optional comment; this call with concatenate the ids
     * with the comment
     */
    public void sendBulkMessage(mlsUser caller, List<Integer> objectIds, MessageEventType event, String comment) {
        StringBuilder sb = new StringBuilder(comment);
        Iterator it = objectIds.iterator();
        for (int i = 0; it.hasNext(); i++) {
            Integer id = (Integer) it.next();
            sb.append(id);
            if (i < objectIds.size() - 1) {
                sb.append(",");
            }
        }
        sendMessage(caller, null, event, sb.toString());
    }

    public void closeConnection() {
        try {
            if (producer != null) {
                producer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException ex) {
            Logger.getLogger(MlMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
