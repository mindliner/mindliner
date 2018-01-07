/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mindlinerstarter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Small starter application that calls the main Mindliner application with the
 * current java home dir as jvm arg. This jvm arg is needed for cloud services
 * like Google Drive.
 *
 * @author Dominic Plangger
 */
public class MindlinerStarter {

    private static String MINDLINER_ARG;
    private static String CLIENT_CFG;
    private static String SERVER_CFG;
    private static final String OUTPUT_PROP = "shell.enabled";
    private static final String JVM_ARG_PROP = "jvm.args";
    private static final String FACTORY_PROP = "java.naming.factory.initial";
    private static final String HOST_PROP = "org.omg.CORBA.ORBInitialHost";
    private static final String PORT_PROP = "org.omg.CORBA.ORBInitialPort";
    private static final String SSL_PROP = "com.sun.CSIV2.ssl.standalone.client.required"; 
    private static String STARTER_LOG;
    private static String JAVA_EXE;
    private static String JAVA_TRUSTSTORE;
    private static String LOGIN_CONF;
    private static final String fs = System.getProperty("file.separator");

    /**
     * Builds the arguments using the OS specific directory separator
     */
    private static void buildCommandElements() {
        MINDLINER_ARG = "-jar bin" + fs + "MindlinerDesktop.jar ";
        CLIENT_CFG = "config" + fs + "client.properties";
        SERVER_CFG = "config" + fs + "server.properties";
        JAVA_EXE = "java" + fs + "bin" + fs + "java.exe ";
        JAVA_TRUSTSTORE = "java" + fs + "lib" + fs + "security" + fs + "cacerts";
        LOGIN_CONF = "config" + fs + "login.conf";
        STARTER_LOG = "config" + fs + "start_log.log";
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws URISyntaxException {
        try {
            System.out.println(MindlinerStarter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            buildCommandElements();
            FileHandler fh = new FileHandler(STARTER_LOG);
            fh.setFormatter(new SimpleFormatter());
            Logger.getLogger(MindlinerStarter.class.getName()).addHandler(fh);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(MindlinerStarter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        String jvmArgs = null;
        boolean outputEnabled = false;
        
        

        // First see if user specified java home in the client.properties file. If yes, use this value.
        // It allows the user to set a specific java
        try {
            FileInputStream fis = new FileInputStream(CLIENT_CFG);
            PropertiesEscaped prop = new PropertiesEscaped();
            prop.load(fis);
            jvmArgs = prop.getProperty(JVM_ARG_PROP);
            String output = prop.getProperty(OUTPUT_PROP);
            if (output != null && output.trim().toLowerCase().equals("true")) {
                outputEnabled = true;
            }
        } catch (IOException | IllegalArgumentException ex) {
            Logger.getLogger(MindlinerStarter.class
                    .getName()).log(Level.WARNING, "Failed to properly load CLIENT property file", ex);
        }
        
        String factory, host, port;
        try {
            FileInputStream fis = new FileInputStream(SERVER_CFG);
            PropertiesEscaped prop = new PropertiesEscaped();
            prop.load(fis);
            factory = prop.getProperty(FACTORY_PROP);
            host = prop.getProperty(HOST_PROP);
            port = prop.getProperty(PORT_PROP);
        } catch (IOException | IllegalArgumentException ex) {
            Logger.getLogger(MindlinerStarter.class
                    .getName()).log(Level.SEVERE, "Failed to properly load SERVER property file", ex);
            // Without server properties, we do not know to which server we have to connect. Exit.
            return; 
        }
        
        Logger.getLogger(MindlinerStarter.class
                    .getName()).log(Level.INFO, "Properties: {0}, {1}, {2}", new Object[]{factory, host, port});

        StringBuilder command = new StringBuilder();
        command.append(JAVA_EXE);

        if (jvmArgs != null && !jvmArgs.isEmpty()) {
            command.append(jvmArgs).append(" ");
        }

        // Specify truststore for ssl certificate validation
        command.append("\"").append("-Djavax.net.ssl.trustStore=").append(JAVA_TRUSTSTORE).append("\" ");
        // Client authentication module for ProgrammaticLogin
        command.append("\"").append("-Djava.security.auth.login.config=").append(LOGIN_CONF).append("\" ");
        // InitialContext arguments. Specifying them as JVM argument sets them automatically as default for all InitialContexts. 
        // Specifying them at runtime per System.setProperty might cause some InitialContext to be initialized with the wrong default (e.g. localhost)
        command.append("\"").append("-D").append(FACTORY_PROP).append("=").append(factory).append("\" ");
        command.append("\"").append("-D").append(HOST_PROP).append("=").append(host).append("\" ");
        command.append("\"").append("-D").append(PORT_PROP).append("=").append(port).append("\" ");
        // forces secure GIOP request on ssl port when establishing connection (otherwise first an unsecure handshake on default port is issued)
        command.append("\"").append("-D").append(SSL_PROP).append("=").append("true").append("\" ");
    
        command.append(MINDLINER_ARG);

        try {
            Logger.getLogger(MindlinerStarter.class
                    .getName()).log(Level.INFO, "Starting Mindliner with [{0}]", command);
            String launchString;
            if (OSValidator.isWindows()) {
                launchString = "cmd /c " + (outputEnabled ? "start " : "") + command;
                Runtime.getRuntime().exec(launchString);
            } else if (OSValidator.isMac()) {
                // TODO
            } else if (OSValidator.isUnix()) {
                // TODO
            }
        } catch (IOException ex) {
            Logger.getLogger(MindlinerStarter.class
                    .getName()).log(Level.SEVERE, "Could not execute batch command to start Mindliner.", ex);
        }
    }

    /*
     Properties class that escapes '\' in the properties file, such that the user
     can specify paths in the file without being required to escape the backslash
     */
    static class PropertiesEscaped extends Properties {

        public void load(FileInputStream fis) throws IOException {
            Scanner in = new Scanner(fis);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            while (in.hasNext()) {
                out.write(in.nextLine().replace("\\", "\\\\").getBytes());
                out.write("\n".getBytes());
            }

            InputStream is = new ByteArrayInputStream(out.toByteArray());
            super.load(is);
        }
    }

}
