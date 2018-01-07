
/**
 * Author:  marius
 * Created: 20.11.2015
 */

ALTER TABLE `mindliner5`.`objects` ADD COLUMN `RELATIVE_COUNT` INT(11) NOT NULL DEFAULT '-1'  AFTER `ARCHIVED` ;