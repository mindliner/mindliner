# Discrete value distinguishing log types (create, remove, modify, link)
ALTER TABLE `mindliner5`.`log` ADD COLUMN `TYPE` INT(4) NOT NULL DEFAULT 0  AFTER `DESCRIPTION` ;

# update type value according to the method names
update log set type = 3 where method like '%link%';
update log set type = 2 where method like '%remove%';
update log set type = 1 where method like '%set%' or method like '%update%';
update log set type = 0 where method like '%create%' or method like '%creation%';

# As we cannot get the client of already removed objects, we might need to clear the log table
ALTER TABLE `mindliner5`.`log` ADD COLUMN `CLIENT_ID` INT(10) NOT NULL DEFAULT 0  AFTER `TYPE` ;

CREATE  TABLE IF NOT EXISTS `mindliner5`.`userreports` (
  `ID` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `CREATION_TS` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `USER_ID` INT(10) UNSIGNED NOT NULL ,
  `CLIENT_ID` INT(10) UNSIGNED NOT NULL ,
  `CREATE_CNT` INT(10) NOT NULL DEFAULT 0 ,
  `REMOVE_CNT` INT(10) NOT NULL DEFAULT 0 ,
  `MODIFY_CNT` INT(10) NOT NULL DEFAULT 0 ,
  `SELF_LINKS_CNT` INT(10) NOT NULL DEFAULT 0 ,
  `FOREIGN_LINKS_CNT` INT(10) NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`ID`) )
COMMENT = 'Table for reporting purpose. Accumulates periodically user behaviours (through mysql events) like number of creates or deletes.';

# Required for periodical execution of report event
SET GLOBAL event_scheduler = ON;

# Report event computing for each user on each client the number of creations, deletions, modifications, etc.
# Scheduled once a day
CREATE EVENT link_report_update
    ON SCHEDULE EVERY 1 DAY
    DO
      insert into userreports 
(user_id, client_id, create_cnt, remove_cnt, modify_cnt, self_links_cnt, foreign_links_cnt)
select user_id, client_id, COALESCE(cre_c,0), COALESCE(rem_c,0), COALESCE(mod_c,0), COALESCE(o2o_c,0), COALESCE(o2f_c,0) from 
	(select user_id, client_id from users_clients) all_u_t
natural left outer join 
	(select user_id, client_id, count(*) as cre_c from  log l where type = 0  group by user_id, client_id) cre_t
natural left outer join
	(select user_id, client_id, count(*) as mod_c from  log l where type = 1  group by user_id, client_id) mod_t
natural left outer join
	(select user_id, client_id, count(*) as rem_c from  log l where type = 2  group by user_id, client_id) rem_t
natural left outer join
	(select l.owner_id as user_id, l.client_id, count(*) as o2o_c 
	from links l, objects o1, objects o2
	where l.holder_id = o1.id and l.relative_id = o2.id 
	and o1.owner_id = l.owner_id and o2.owner_id = l.owner_id
	group by l.owner_id, client_id) o2o_t
natural left outer join
	(select l.owner_id as user_id, l.client_id, count(*) as o2f_c 
	from links l, objects o1, objects o2
	where l.holder_id = o1.id and l.relative_id = o2.id 
	and (o1.owner_id <> l.owner_id or o2.owner_id <> l.owner_id)
	group by l.owner_id, client_id) o2f_t; 