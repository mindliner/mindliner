# add indices to links table
ALTER TABLE `mindliner5`.`links` 
ADD INDEX `IDX_HOLDER` (`HOLDER_ID` ASC), 
ADD INDEX `IDX_RELATIVE` (`RELATIVE_ID` ASC);
