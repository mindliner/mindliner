
ALTER TABLE `mindliner5`.`contacts` 
ADD COLUMN `PHONE_NUMBER` VARCHAR(64) NULL AFTER `PICTURE_ID` , 
ADD COLUMN `MOBILE_NUMBER` VARCHAR(64) NULL  AFTER `PHONE_NUMBER` ;