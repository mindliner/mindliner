CREATE  TABLE `mindliner5`.`sys_releases` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `VERSION_STRING` VARCHAR(45) NOT NULL COMMENT 'A human readable version string like \"1.7.2\" or \"Tiger X5\"' ,
  `VERSION_NUMBER` INT NOT NULL  UNIQUE COMMENT 'A number which increases with every release and which is used to verify application integrity across multiple components.' ,
  `OLDEST_DESKTOP_VERSION` INT NOT NULL COMMENT 'The oldest desktop version number which is still compatible with this release (server running newer version than client)' ,
  `LATEST_DESKTOP_VERSION` INT NOT NULL COMMENT 'The latest desktop release which is still compatible with this release (server running older version than client).' ,
  `WEBSTART_URL` VARCHAR(255) NOT NULL ,
  `DISTRIBUTION_URL` VARCHAR(255) NOT NULL ,
  `RELEASE_NOTES_URL` VARCHAR(255) NULL ,
  `RELEASE_DATE` TIMESTAMP NOT NULL ,
  PRIMARY KEY (`ID`) )
COMMENT = 'This is a Mindliner system table and shows all releases.';