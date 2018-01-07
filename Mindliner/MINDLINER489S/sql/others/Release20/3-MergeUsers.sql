# Marius' accounts
update colorschemes SET owner_id = 1 WHERE owner_id IN (21, 33, 93, 113, 124, 133);
update links SET owner_id = 1 WHERE owner_id IN (21, 33, 93, 113, 124, 133);
update log SET user_id = 1 WHERE user_id IN (21, 33, 93, 113, 124, 133);
update maps SET owner_id = 1 WHERE owner_id IN (21, 33, 93, 113, 124, 133);
update objects SET owner_id = 1 WHERE owner_id IN (21, 33, 93, 113, 124, 133);
update queries SET user_id = 1 WHERE user_id IN (21, 33, 93, 113, 124, 133);
update synchers SET user_id = 1 WHERE user_id IN (21, 33, 93, 113, 124, 133);
update weekplans SET user_id = 1 WHERE user_id IN (21, 33, 93, 113, 124, 133);
update users_clients SET user_id = 1 WHERE user_id IN (21, 33, 93, 113, 124, 133);
INSERT INTO logincounts (select id, sum(login_count) from users where id IN (1, 21, 33, 93, 113, 124, 133));
DELETE FROM sys_users_swfeatures WHERE user_id IN (21, 33, 93, 113, 124, 133);
DELETE FROM users WHERE id IN (21, 33, 93, 113, 124, 133);
DELETE FROM users_groups WHERE user_id IN (21, 33, 93, 113, 124, 133);

# Niklaus' accounts
update colorschemes SET owner_id = 22 WHERE owner_id IN (43, 59);
update links SET owner_id = 22 WHERE owner_id IN (43, 59);
update log SET user_id = 22 WHERE user_id IN (43, 59);
update maps SET owner_id = 22 WHERE owner_id IN (43, 59);
update objects SET owner_id = 22 WHERE owner_id IN (43, 59);
update queries SET user_id = 22 WHERE user_id IN (43, 59);
update synchers SET user_id = 22 WHERE user_id IN (43, 59);
update weekplans SET user_id = 22 WHERE user_id IN (43, 59);
update users_clients SET user_id = 22 WHERE user_id IN (43, 59);
INSERT INTO logincounts (select id, sum(login_count) from users where id IN (22, 43, 59));
DELETE FROM sys_users_swfeatures WHERE user_id IN (43, 59);
DELETE FROM users WHERE id IN (43, 59);
DELETE FROM users_groups WHERE user_id IN (43, 59);

# Ming's accounts
update colorschemes SET owner_id = 136 WHERE owner_id IN (140, 141);
update links SET owner_id = 136 WHERE owner_id IN (140, 141);
update log SET user_id = 136 WHERE user_id IN (140, 141);
update maps SET owner_id = 136 WHERE owner_id IN (140, 141);
update objects SET owner_id = 136 WHERE owner_id IN (140, 141);
update queries SET user_id = 136 WHERE user_id IN (140, 141);
update synchers SET user_id = 136 WHERE user_id IN (140, 141);
update weekplans SET user_id = 136 WHERE user_id IN (140, 141);
update users_clients SET user_id = 136 WHERE user_id IN (140, 141);
INSERT INTO logincounts (select id, sum(login_count) from users where id IN (136, 140, 141));
DELETE FROM sys_users_swfeatures WHERE user_id IN (140, 141);
DELETE FROM users WHERE id IN (140, 141);
DELETE FROM users_groups WHERE user_id IN (140, 141);

# Osman's accounts
update colorschemes SET owner_id = 138 WHERE owner_id = 139;
update links SET owner_id = 138 WHERE owner_id = 139;
update log SET user_id = 138 WHERE user_id = 139;
update maps SET owner_id = 138 WHERE owner_id = 139;
update objects SET owner_id = 138 WHERE owner_id = 139;
update queries SET user_id = 138 WHERE user_id = 139;
update synchers SET user_id = 138 WHERE user_id = 139;
update weekplans SET user_id = 138 WHERE user_id = 139;
update users_clients SET user_id = 138 WHERE user_id = 139;
INSERT INTO logincounts (select id, sum(login_count) from users where id IN (138, 139));
DELETE FROM sys_users_swfeatures WHERE user_id = 139;
DELETE FROM users WHERE id = 139;
DELETE FROM users_groups WHERE user_id = 139;

# John Flury's accounts
update colorschemes SET owner_id = 134 WHERE owner_id = 135; 
update links SET owner_id = 134 WHERE owner_id = 135;
update log SET user_id = 134 WHERE user_id = 135;
update maps SET owner_id = 134 WHERE owner_id = 135;
update objects SET owner_id = 134 WHERE owner_id = 135;
update queries SET user_id = 134 WHERE user_id = 135;
update synchers SET user_id = 134 WHERE user_id = 135;
update weekplans SET user_id = 134 WHERE user_id = 135;
update users_clients SET user_id = 134 WHERE user_id = 135;
INSERT INTO logincounts (select id, sum(login_count) from users where id IN (134, 135));
DELETE FROM users WHERE id = 135;
DELETE FROM users_groups WHERE user_id = 135;
DELETE FROM sys_users_swfeatures WHERE user_id = 135;

# Ingo's accounts
update colorschemes SET owner_id = 58 WHERE owner_id = 122; 
update links SET owner_id = 58 WHERE owner_id = 122; 
update log SET user_id = 58 WHERE user_id = 122;
update maps SET owner_id = 58 WHERE owner_id = 122;
update objects SET owner_id = 58 WHERE owner_id = 122;
update queries SET user_id = 58 WHERE user_id = 122;
update synchers SET user_id = 58 WHERE user_id = 122;
update weekplans SET user_id = 58 WHERE user_id = 122;
update users_clients SET user_id = 58 WHERE user_id = 122;
INSERT INTO logincounts (select id, sum(login_count) from users where id IN (58, 122));
DELETE FROM users WHERE id = 122;
DELETE FROM users_groups WHERE user_id = 122;
DELETE FROM sys_users_swfeatures WHERE user_id = 122;

# Luciano's accounts
update colorschemes SET owner_id = 7 WHERE owner_id = 73; 
update links SET owner_id = 7 WHERE owner_id = 73; 
update log SET user_id = 7 WHERE user_id = 73;
update maps SET owner_id = 7 WHERE owner_id = 73;
update objects SET owner_id = 7 WHERE owner_id = 73;
update queries SET user_id = 7 WHERE user_id = 73;
update synchers SET user_id = 7 WHERE user_id = 73;
update weekplans SET user_id = 7 WHERE user_id = 73;
update users_clients SET user_id = 7 WHERE user_id = 73;
INSERT INTO logincounts (select id, sum(login_count) from users where id IN (7, 73));
DELETE FROM users WHERE id = 73;
DELETE FROM users_groups WHERE user_id = 73;
DELETE FROM sys_users_swfeatures WHERE user_id = 73;

# Only once all above users were patched copy combined login counts back to the user table and drop the tmp table
update users u join logincounts l set u.login_count = l.count WHERE u.id = l.userid;
drop table `mindliner5`.`logincounts`;
