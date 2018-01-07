
-- Delete all action items from Mindliner; they are very old anyways
DELETE FROM objects WHERE DTYPE = 'ACTI';
DELETE FROM actionitems;

-- Rename actionitems to news and add a link column to the log
ALTER TABLE `mindliner5`.`actionitems` 
DROP COLUMN `ACTOR_ID` , DROP COLUMN `ACTION_REQUIRED` ,
CHANGE COLUMN `USER_OBJECT_ID` `OBJECT_ID` INT NULL COMMENT 'A link to a Mindliner object in the case the news recod is not based on a log in which case the object is referenced there.'  , 
ADD COLUMN `LOG_ID` INT NULL  AFTER `OBJECT_ID` , 
ADD INDEX `IDX_TYPE` (`ACTION_TYPE_ID` ASC) , 
ADD INDEX `IDX_OBJECT` (`OBJECT_ID` ASC) , 
ADD INDEX `IDX_LOG` (`LOG_ID` ASC) , 
RENAME TO  `mindliner5`.`news` ;

-- Rename actionitemtypes to newstypes and change columns accordingly, add another newstype for subscriptions
ALTER TABLE `mindliner5`.`actionitemtypes` RENAME TO  `mindliner5`.`newstypes` ;
ALTER TABLE `mindliner5`.`news` CHANGE COLUMN `ACTION_TYPE_ID` `NEWS_TYPE_ID` INT(10) UNSIGNED NOT NULL DEFAULT '0'  ;
INSERT INTO `mindliner5`.`newstypes` (`NAME`, `CLIENT_ID`, `PERSISTENT`, `TAG`) VALUES ('subscription', '1', '1', 'SUBSCRIPTION');

-- Rename the table that was before only used for object defaults and make fit to hold all userpreferences
ALTER TABLE `mindliner5`.`objectdefaults` 
DROP COLUMN `LIFETIME` , 
CHANGE COLUMN `PRIVATEFLAG` `PRIVATEFLAG` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'New object preference'  , 
CHANGE COLUMN `PRIORITY_ID` `PRIORITY_ID` INT(11) NOT NULL COMMENT 'New object preference; used for tasks only'  , 
CHANGE COLUMN `CLIENT_ID` `CLIENT_ID` INT(11) NOT NULL COMMENT 'New object preference'  , 
ADD COLUMN `NEWS_DIGEST_INTERVALL` INT NULL COMMENT 'The minimum interval between two news digests in minutes.'  AFTER `CLIENT_ID` , 
ADD COLUMN `NEWS_LAST_DIGEST` TIMESTAMP NULL COMMENT 'The TS of the last news delivery.'  AFTER `NEWS_DIGEST_INTERVALL` , 
COMMENT = 'This table stores object creation and other defaults for users' , 
RENAME TO  `mindliner5`.`userpreferences` ;

ALTER TABLE `mindliner5`.`objectdefaultsconfidentialities` CHANGE COLUMN `OBJECT_DEFAULTS_ID` `USER_ID` INT(11) NOT NULL  ;