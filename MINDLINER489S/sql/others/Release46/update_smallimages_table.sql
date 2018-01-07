
ALTER TABLE `mindliner5`.`smallimages` ADD COLUMN `URL` TEXT NULL DEFAULT NULL  AFTER `IMAGE_DATA` , 
COMMENT = 'This table holds image icons, profile pictures, and url images' , RENAME TO  `mindliner5`.`images` ;

ALTER TABLE `mindliner5`.`mapnodes_smallimages` RENAME TO  `mindliner5`.`mapnodes_images` ;