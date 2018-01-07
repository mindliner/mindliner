#/bin/bash
# Script to launch Mindliner on Unix/Linux systems
java "-Djavax.net.ssl.trustStore=java/lib/security/cacerts" "-Djava.security.auth.login.config=config/login.conf" "-Djava.naming.factory.initial=com.sun.enterprise.naming.SerialInitContextFactory" "-Dorg.omg.CORBA.ORBInitialHost=mls1.mindliner.com" "-Dorg.omg.CORBA.ORBInitialPort=3820" "-Dcom.sun.CSIV2.ssl.standalone.client.required=true" -jar bin/MindlinerDesktop.jar

