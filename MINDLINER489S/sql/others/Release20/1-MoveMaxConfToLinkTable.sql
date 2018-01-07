# SEQUENCE MATTERS

# Add column for the maximum confidentiality to the users_clients link table
ALTER TABLE `mindliner5`.`users_clients` ADD COLUMN `MAX_CONFIDENTIALITY_ID` INT NULL  AFTER `CLIENT_ID` ;

# Initialize the new column with values from the user table
UPDATE users_clients uc JOIN users u on uc.`USER_ID` = u.`ID` SET uc.`MAX_CONFIDENTIALITY_ID` = u.`MAX_CONFIDENTIALITY_ID` WHERE uc.`USER_ID` = u.`ID`;

# After the migration require the the confi column to be not null
ALTER TABLE `mindliner5`.`users_clients` CHANGE COLUMN `MAX_CONFIDENTIALITY_ID` `MAX_CONFIDENTIALITY_ID` INT(11) NOT NULL  ;

# Then drop the max confi colum from users
ALTER TABLE `mindliner5`.`users` DROP COLUMN `MAX_CONFIDENTIALITY_ID` ;