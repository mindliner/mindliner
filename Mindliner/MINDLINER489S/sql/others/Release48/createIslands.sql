# create the islands table

CREATE TABLE `islands` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `CLIENT_ID` int NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `islands_CLIENT_ID_idx` (`CLIENT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Islands are collections of objects that form a closed topological set.';

# Add column island_id to objects
ALTER TABLE `mindliner5`.`objects` ADD COLUMN `ISLAND_ID` INT AFTER `STATUS` 
, ADD INDEX `IDX_ISLAND` (`ISLAND_ID` ASC) ;


