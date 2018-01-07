-- There were a large number of log records with a client id of null or 0.
-- The null group was from 2008 and reflects a problem long gone.
-- The 0 group was current and reflected a bug in LinkLogger.java; now fixed.
-- Marius

update log l inner join users u on l.`USER_ID` = u.`ID`
SET l.`CLIENT_ID` = u.`CLIENT_ID`
WHERE l.client_id = 0;

-- There are 4 records left that belonged to a user which no longer exists
DELETE from log where client_id = 0