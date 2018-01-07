# Where to find the asadmin executable 
GLASSFISH_ASADMIN="/home/glassfish/bin/asadmin" 
# GF admin port. Defaults to 4848. Is used to specify which domain should be setup 
DOMAIN_PORT=4848
# Is used for ssljms. In local environment, use localhost. When used in prod environment, use arboardone.mindliner.com.
HOST_NAME="arboardone.mindliner.com" 

echo "deleting old JMS ConnectionFactory and Destination (if exist)..."
$GLASSFISH_ASADMIN --host localhost --port $DOMAIN_PORT delete-jms-resource jms/MindlinerObjectEvent
$GLASSFISH_ASADMIN --host localhost --port $DOMAIN_PORT delete-jms-resource jms/MindlinerMessage

echo "creating JMS ConnectionFactory and Destination..."
$GLASSFISH_ASADMIN --host localhost --port $DOMAIN_PORT create-jms-resource --restype javax.jms.Topic --description 'Handles messages on created, changed, or deleted Mindliner objects' --property Name=MindlinerObjectEvent jms/MindlinerObjectEvent 
$GLASSFISH_ASADMIN --host localhost --port $DOMAIN_PORT create-jms-resource --restype javax.jms.TopicConnectionFactory --description 'Connectin factory for ssl based JMS communication (ssljms)' --property AddressList=mqssl\\://$HOST_NAME\\:7676/ssljms jms/MindlinerMessage

echo "deleting old JDBC Connection Pool and Data Source (if exist)..."
$GLASSFISH_ASADMIN --host localhost --port $DOMAIN_PORT delete-jdbc-resource MindlinerDataSource
$GLASSFISH_ASADMIN --host localhost --port $DOMAIN_PORT delete-jdbc-connection-pool MindlinerPool

echo "creating JDBC Connection Pool and Data Source..."
$GLASSFISH_ASADMIN --host localhost --port $DOMAIN_PORT create-jdbc-connection-pool --restype javax.sql.DataSource --description 'Mindliner Data Pool' --datasourceclassname com.mysql.jdbc.jdbc2.optional.MysqlDataSource --property URL=jdbc\\:mysql\\://localhost\\:3307/mindliner5?useUnicode\\=true:connectionAttributes=characterEncoding\\=UTF-8:User=mindman:Password=wake.x MindlinerPool
$GLASSFISH_ASADMIN --host localhost --port $DOMAIN_PORT create-jdbc-resource --connectionpoolid MindlinerPool --description 'Mindliner Data Source' MindlinerDataSource

echo "deleting old jdbcRealm (if exists)..."
$GLASSFISH_ASADMIN --host localhost --port $DOMAIN_PORT delete-auth-realm jdbcRealm

echo "creating new jdbcRealm..."
$GLASSFISH_ASADMIN --host localhost --port $DOMAIN_PORT create-auth-realm --classname com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm --property jaas-context=jdbcRealm:datasource-jndi=MindlinerDataSource:user-table=users:user-name-column=username:password-column=password:group-table=user_group_view:group-name-column=groupname:encoding=Base64:digest-algorithm=SHA-256 jdbcRealm

echo "Glassfish setup finished!"
