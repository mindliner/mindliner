# The parameters table

DELETE FROM mindliner5.ratingparameters;
ALTER TABLE `mindliner5`.`ratingparameters` CHANGE COLUMN `ID` `ID` INT(11) NOT NULL AUTO_INCREMENT  , CHANGE COLUMN `PARAMETER_VALUE` `PARAMETER_VALUE` VARCHAR(128) NOT NULL  ;

# The parameter set table

ALTER TABLE `mindliner5`.`ratingparametersets` CHARACTER SET = utf8 , CHANGE COLUMN `ID` `ID` INT(11) NOT NULL AUTO_INCREMENT  ;


