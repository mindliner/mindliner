# Create new field CATEGORY_NAME
# Each syncher now has its own category name enabling users with multiple data pools to specifically synch to pools
# Initialize all existing synchers with the previous value.

ALTER TABLE `mindliner5`.`synchers` ADD COLUMN `CATEGORY_NAME` VARCHAR(45) NOT NULL DEFAULT ''  AFTER `PERFORM_CONTENT_CHECK` ;

UPDATE synchers SET CATEGORY_NAME = 'MindlinerObject'