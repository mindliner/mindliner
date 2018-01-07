# EXPERIMENTAL

# This table lists the prices for Mindliner services or products

CREATE  TABLE `prices` (
  `ID` INT NOT NULL AUTO_INCREMENT ,
  `ACTIVE` TINYINT NULL DEFAULT 1 ,
  `PTYPE` VARCHAR(45) NOT NULL COMMENT 'The product/service type (i.e. knowlet creation, knowlet modification, linking, etc). Enum' ,
  `AMOUNT` INT NOT NULL ,
  `TSTART` TIMESTAMP NOT NULL COMMENT 'The timestamp at which the price becomes or became active.' ,
  `USER_ID` INT NOT NULL COMMENT 'The user who posted the price' ,
  PRIMARY KEY (`ID`) ,
  INDEX `IDX_USER` (`USER_ID` ASC) );

# This table holds the user transactions

CREATE  TABLE `transactions` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `USER_ID` INT UNSIGNED NOT NULL ,
  `AMOUNT` INT NOT NULL COMMENT 'The amount in Mindliner units' ,
  `REASON` VARCHAR(45) NOT NULL COMMENT 'A Java enum that explains the reason.' ,
  `COMMENT` VARCHAR(255) NULL ,
  `TTIME` TIMESTAMP NULL DEFAULT NOW() ,
  `OBJECT_ID` INT NULL COMMENT 'optional object id for transaction that are related to an object' ,
  PRIMARY KEY (`ID`) ,
  UNIQUE INDEX `ID_UNIQUE` (`ID` ASC) )
COMMENT = 'This table holds all the transactions made against Mindliner';

ALTER TABLE `transactions` 
ADD INDEX `IDX_USER` (`USER_ID` ASC) 
, ADD INDEX `IDX_OBJECT` (`OBJECT_ID` ASC) ;