# Add a log column for logging the second object of a link action
ALTER TABLE `mindliner5`.`log` ADD COLUMN `LINK_OBJECT_ID` INT(10) UNSIGNED NULL DEFAULT '0'  AFTER `CLIENT_ID` ;

DROP EVENT IF EXISTS link_report_update;

SET GLOBAL event_scheduler = ON;

# We need to update the report event: 
# 1. the log type is now a string and not a number anymore
# 2. the self and foreign links count can now be fetched also from the logs table
# (fetching the count from the links table can be affected badly by link deletes)
CREATE EVENT link_report_update
    ON SCHEDULE EVERY 1 DAY
    DO
      insert into userreports 
(user_id, client_id, create_cnt, remove_cnt, modify_cnt, self_links_cnt, foreign_links_cnt)
select user_id, client_id, COALESCE(cre_c,0), COALESCE(rem_c,0), COALESCE(mod_c,0), COALESCE(o2o_c,0), COALESCE(o2f_c,0) from 
	(select user_id, client_id from users_clients) all_u_t
natural left outer join 
	(select user_id, client_id, count(*) as cre_c from  log l where type = 'Create'  group by user_id, client_id) cre_t
natural left outer join
	(select user_id, client_id, count(*) as mod_c from  log l where type = 'Modify'  group by user_id, client_id) mod_t
natural left outer join
	(select user_id, client_id, count(*) as rem_c from  log l where type = 'Remove'  group by user_id, client_id) rem_t
natural left outer join
	(select l.user_id, l.client_id, count(*) as o2o_c from  log l, objects o1, objects o2 where type = 'Link'
	and l.USER_OBJECT_ID = o1.id and l.LINK_OBJECT_ID = o2.id   
	and o1.owner_id = l.USER_ID and o2.owner_id = l.USER_ID
	group by l.user_id, l.client_id) o2o_t
natural left outer join
	(select l.user_id, l.client_id, count(*) as o2f_c from  log l, objects o1, objects o2 where type = 'Link'
	and l.USER_OBJECT_ID = o1.id and l.LINK_OBJECT_ID = o2.id   
	and (o1.owner_id <> l.USER_ID or o2.owner_id <> l.USER_ID)
	group by l.user_id, l.client_id) o2f_t;