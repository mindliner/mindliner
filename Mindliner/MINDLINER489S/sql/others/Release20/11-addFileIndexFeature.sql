# Insert new software feature for indexing files in solr
INSERT INTO `mindliner5`.`sys_swfeatures` (`ID`, `NAME`, `DESCRIPTION`) VALUES ('36', 'FILE_INDEXING', 'Indexing of dropped  files in Solr for for file content search');

INSERT INTO `mindliner5`.`sys_users_swfeatures` (`USER_ID`, `FEATURE_ID`) VALUES ('1', '36');
INSERT INTO `mindliner5`.`sys_users_swfeatures` (`USER_ID`, `FEATURE_ID`) VALUES ('86', '36');
INSERT INTO `mindliner5`.`sys_users_swfeatures` (`USER_ID`, `FEATURE_ID`) VALUES ('136', '36');
INSERT INTO `mindliner5`.`sys_users_swfeatures` (`USER_ID`, `FEATURE_ID`) VALUES ('138', '36');