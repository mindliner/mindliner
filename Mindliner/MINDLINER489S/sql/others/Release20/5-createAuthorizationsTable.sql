# the table that holds enrollment requests for users to join pools or pooladmin to invite users

CREATE  TABLE `mindliner5`.`authorizations` (
  `TOKEN` INT(11) UNSIGNED NOT NULL, 
  `USER_ID` INT NULL ,
  `DATAPOOL_ID` INT NULL ,
  `AUTHORIZATION_TYPE` VARCHAR(64) NOT NULL  ,
  `COMPLETED` TINYINT NULL DEFAULT 0 ,
  `EXPIRATION_TS` TIMESTAMP NULL ,
  PRIMARY KEY (`TOKEN`) ,
  INDEX `USER_IDX` (`USER_ID` ASC) ,
  UNIQUE INDEX `TOKEN_UNIQUE` (`TOKEN` ASC) ,
  INDEX `POOL_IDX` (`DATAPOOL_ID` ASC))
COMMENT = 'This table holds authorizations granted by one user to another to support operations that need consent between two people';

