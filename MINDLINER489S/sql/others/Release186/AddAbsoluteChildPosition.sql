# Add the position field to the links

ALTER TABLE `mindliner5`.`links` ADD COLUMN `RELATIVE_LIST_POSITION` INT NOT NULL DEFAULT 0 
COMMENT 'The list position of the object specified by RELATIVE_ID in the list of relatives for the object specified by HOLDER_ID.'  
AFTER `RELATIVE_TYPE` ;

# Add a flag to objects indicating whether or not the relatives are in defined order

ALTER TABLE `mindliner5`.`objects` ADD COLUMN `RELATIVES_ORDERED` TINYINT NOT NULL DEFAULT 0 
COMMENT 'If non-zero then the relatives of this object are sorted using the RELATIVE_LIST_POSITION field of the links table.'  
AFTER `ISLAND_ID` ;

