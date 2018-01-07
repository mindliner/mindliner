# add a user object link to action items
# formerly done via the relationships

ALTER TABLE `mindliner5`.`actionitems` ADD COLUMN `USER_OBJECT_ID` INT NULL  AFTER `ACTION_TYPE_ID` 
, ADD INDEX `IDX_COMPLETED` (`COMPLETED` ASC) ;

# add a new action item type for random polling

INSERT INTO `mindliner5`.`actionitemtypes` (`NAME`, `CLIENT_ID`, `PERSISTENT`, `TAG`) VALUES ('random', '1', '0', 'RANDOM_POLL');
