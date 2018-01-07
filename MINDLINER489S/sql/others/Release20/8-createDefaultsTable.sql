-- The table that hold the main defaults

CREATE TABLE `objectdefaults` (
  `USER_ID` int(11) NOT NULL,
  `PRIVATEFLAG` tinyint(4) NOT NULL DEFAULT '0',
  `LIFETIME` smallint(6) NOT NULL DEFAULT '1000' COMMENT 'used for knowlets and collections only',
  `PRIORITY_ID` int(11) NOT NULL COMMENT 'used for tasks only',
  `CLIENT_ID` int(11) NOT NULL,
  PRIMARY KEY (`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores object creation defaults per user';

-- The table that hold the confidentiality defaults per client

CREATE TABLE `objectdefaultsconfidentialities` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `OBJECT_DEFAULTS_ID` int(11) NOT NULL,
  `CLIENT_ID` int(11) NOT NULL,
  `CONFIDENTIALITY_ID` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8;

