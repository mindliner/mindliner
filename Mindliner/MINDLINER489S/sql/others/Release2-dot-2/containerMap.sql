drop table worksphere_mapnodes;

delimiter $$

CREATE TABLE `containermaps` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=163906 DEFAULT CHARSET=utf8$$

delimiter $$

CREATE TABLE `containers` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `POS_X` smallint(6) NOT NULL,
  `POS_Y` smallint(6) NOT NULL,
  `WIDTH` smallint(6) NOT NULL,
  `HEIGHT` smallint(6) NOT NULL,
  `COLOR` varchar(7) DEFAULT '' COMMENT 'HEX CSS color',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=163911 DEFAULT CHARSET=utf8$$

delimiter $$

CREATE TABLE `objects_containermap_positions` (
  `CONTAINER_MAP_ID` int(10) NOT NULL,
  `OBJECT_ID` int(10) NOT NULL,
  `POS_X` smallint(6) NOT NULL,
  `POS_Y` smallint(6) NOT NULL,
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8$$


delimiter $$

CREATE TABLE `objects_containermap_links` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `SOURCE_OBJ_ID` int(10) NOT NULL,
  `TARGET_OBJ_ID` int(10) NOT NULL,
  `ONE_WAY` bit(1) NOT NULL,
  `CONTAINER_MAP_ID` int(10) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$



