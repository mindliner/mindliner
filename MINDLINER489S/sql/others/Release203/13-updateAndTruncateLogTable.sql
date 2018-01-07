# Konvertieren von Enum Ordinal in Enum String so dass man direkt in der DB versteht, das die Type bedeuten

ALTER TABLE `mindliner5`.`log` ADD COLUMN `TYPEVC` VARCHAR(64) NULL  AFTER `CLIENT_ID` ;
UPDATE log SET TYPEVC = 'Create' WHERE TYPE = 0;
UPDATE log SET TYPEVC = 'Modify' WHERE TYPE = 1;
UPDATE log SET TYPEVC = 'Remove' WHERE TYPE = 2;
UPDATE log SET TYPEVC = 'Link' WHERE TYPE = 3;
ALTER TABLE `mindliner5`.`log` DROP COLUMN `TYPE` , CHANGE COLUMN `TYPEVC` `TYPE` VARCHAR(64) NULL  ;

# Das Löschen fand ich nun doch ein bischen heftig ...
# delete from log;
# ALTER TABLE `mindliner5`.`log` CHANGE COLUMN `TYPE` `TYPE` VARCHAR(64);
