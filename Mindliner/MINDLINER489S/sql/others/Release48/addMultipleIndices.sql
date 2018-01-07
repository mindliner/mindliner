# Add indices to speed up search in object table
ALTER TABLE `mindliner5`.`objects` 
ADD INDEX `IDX_PRIVACY` (`PRIVATE` ASC) 
, ADD INDEX `IDX_OWNER` (`OWNER_ID` ASC) 
, ADD INDEX `IDX_CLIENT` (`CLIENT_ID` ASC) ;

# add indices to task table to speed up search
ALTER TABLE `mindliner5`.`tasks` 
ADD INDEX `IDX_COMPLETION` (`COMPLETED` ASC) 
, ADD INDEX `IDX_PRIORITY` (`PRIORITY_ID` ASC) ;

# add indices to knowlets table
ALTER TABLE `mindliner5`.`knowlets` 
ADD INDEX `IDX_CATEGORY` (`CATEGORY_ID` ASC) ;

# add index to word table
ALTER TABLE `mindliner5`.`words` 
ADD INDEX `IDX_CLIENT` (`CLIENT_ID` ASC) ;