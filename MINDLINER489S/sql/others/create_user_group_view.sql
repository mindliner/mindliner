CREATE OR REPLACE VIEW `mindliner5`.`user_group_view` AS
SELECT u.username, g.groupname FROM users u, users_groups ug, groups g 
WHERE u.user_id = ug.user_id AND ug.group_id = g.id;