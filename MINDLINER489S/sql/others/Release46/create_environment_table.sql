CREATE  TABLE `mindliner5`.`sys_environment` (
  `ENVKEY` VARCHAR(128) NOT NULL ,
  `ENVVALUE` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`ENVKEY`) )
COMMENT = 'Describes the environment the application server lives in.';

INSERT INTO `mindliner5`.`sys_environment` (`ENVKEY`, `ENVVALUE`) VALUES ('SERVER_HOSTNAME', 'kontiki');
INSERT INTO `mindliner5`.`sys_environment` (`ENVKEY`, `ENVVALUE`) VALUES ('LOCATION_INHOUSE', 'false');