ALTER TABLE `mindliner5`.`users` CHANGE COLUMN `USER_ID` `ID` INT(11) NOT NULL AUTO_INCREMENT  ;

DROP VIEW IF EXISTS user_group_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `user_group_view` AS select `u`.`USERNAME` AS `username`,`g`.`GROUPNAME` AS `groupname` from ((`users` `u` join `users_groups` `ug`) join `groups` `g`) where ((`u`.`ID` = `ug`.`USER_ID`) and (`ug`.`GROUP_ID` = `g`.`ID`));