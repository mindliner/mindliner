delimiter $$

CREATE TABLE `links` (
  `LINK_ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `HOLDER_ID` int(10) unsigned NOT NULL DEFAULT '0',
  `RELATIVE_ID` int(10) unsigned NOT NULL DEFAULT '0',
  `LABEL` text,
  `OWNER_ID` int(10) unsigned NOT NULL DEFAULT '0',
  `MODIFICATION_TS` timestamp NULL DEFAULT NULL,
  `CREATION_TS` timestamp NULL DEFAULT NULL,
  `CLIENT_ID` int(10) unsigned NOT NULL DEFAULT '0',
  `RELATIVE_TYPE` int(4) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`LINK_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=232656 DEFAULT CHARSET=latin1$$
