/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * This class attaches to the Mindliner message queue.
 * 
 * @author Marius Messerli
 */
public class MlMessageConfigurator {

    private final SimpleDateFormat sdf = new SimpleDateFormat();
    private Connection connection;
    private Session session;

    public String startup(MessageListener listener) throws NamingException, JMSException {

        StringBuilder sb = new StringBuilder();
        InitialContext ctx = new InitialContext();
        ConnectionFactory connFactory = (ConnectionFactory) ctx.lookup("jms/MindlinerMessage");
        connection = connFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest = (Destination) ctx.lookup("jms/MindlinerObjectEvent");
        MessageConsumer consumer = session.createConsumer(dest);
        consumer.setMessageListener(listener);
        connection.start();
        sb.append("Provider: ").append(connection.getMetaData().getJMSProviderName());
        sb.append("[").append(sdf.format(new Date())).append("] : listener started.");
        return sb.toString();
    }

}
