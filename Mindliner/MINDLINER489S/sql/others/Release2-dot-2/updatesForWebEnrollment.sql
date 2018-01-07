ALTER TABLE `mindliner5`.`authorizations` ADD COLUMN `EMAIL` VARCHAR(128);

ALTER TABLE `mindliner5`.`authorizations` MODIFY COLUMN `TOKEN` VARCHAR(128);

ALTER TABLE `mindliner5`.`authorizations` ADD COLUMN `MAX_CONFIDENTIALITY_ID` INT(11);
