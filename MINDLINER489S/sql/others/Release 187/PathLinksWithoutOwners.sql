# The method SpreadsheetManager.createCell() used the method objects.setRelatives() that created a MlsLink with owner_id = 0
# Here we patch those links and assign the object's owner as the link's owner

UPDATE links 
LEFT JOIN objects ON holder_id = objects.id  
SET links.owner_id = objects.owner_id 
WHERE links.owner_id = 0;

# We also no longer need a default owner_id in the links table so that links fail on persist() if its missing
ALTER TABLE `mindliner5`.`links` CHANGE COLUMN `OWNER_ID` `OWNER_ID` INT(10) UNSIGNED NOT NULL  ;