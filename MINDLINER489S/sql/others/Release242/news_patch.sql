-- The object link is a relict from when the table was called actionitems, now the object link is acomplished through the log link
ALTER TABLE `mindliner5`.`news` DROP COLUMN `OBJECT_ID`, DROP INDEX `IDX_OBJECT` ;

DELETE FROM news WHERE object_id IS NULL AND log_id IS NULL;