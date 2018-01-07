-- The subscription table

CREATE TABLE `subscriptions` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `USER_ID` int(10) unsigned NOT NULL,
  `EVENT_TYPE` varchar(64) NOT NULL,
  `OBJECT_ID` int(10) unsigned DEFAULT NULL,
  `ACTOR_USER_ID` int(10) DEFAULT NULL COMMENT 'The user id of the person who has fired the event.',
  `REVERSE_SUBSCRIPTION` tinyint(4) DEFAULT '0' COMMENT 'If this field is not 0 then this subscription is interpreted in oppositve direction, meaning that a user does not want to receive a particular event notification.',
  PRIMARY KEY (`ID`),
  KEY `IDX_OBJECT` (`OBJECT_ID`),
  KEY `IDX_EVENT` (`EVENT_TYPE`),
  KEY `IDX_USER` (`USER_ID`),
  KEY `IDX_ACTOR` (`ACTOR_USER_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

-- A path for the log table

ALTER TABLE `mindliner5`.`log` ADD COLUMN `EVENT` varchar(64) DEFAULT 'Any' AFTER `TYPE` ;



  