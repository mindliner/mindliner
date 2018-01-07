# Create the table that holds the rating parameter sets.

CREATE  TABLE `mindliner5`.`ratingparametersets` (
  `ID` INT NOT NULL ,
  `SET_NAME` VARCHAR(64) NOT NULL ,
  `CLIENT_ID` INT NOT NULL ,
  `ACTIVE` TINYINT NULL DEFAULT 0 ,
  PRIMARY KEY (`ID`) ,
  INDEX `IDX_CLIENT` (`CLIENT_ID` ASC) )
COMMENT = 'The parameter sets that control the object rating.';

# Create a new table that holds the rating parameters
CREATE  TABLE `mindliner5`.`ratingparameters` (
  `ID` INT NOT NULL ,
  `SET_ID` INT NOT NULL COMMENT 'The parameter set this parameter is part of.' ,
  `PARAMETER_NAME` VARCHAR(64) NOT NULL COMMENT 'The name of the parameter (an enum type in Hybernate)' ,
  `PARAMETER_VALUE` DOUBLE NOT NULL ,
  PRIMARY KEY (`ID`) ,
  INDEX `IDX_SET` (`SET_ID` ASC) )
COMMENT = 'Defines the object rating parameters for each client.';
