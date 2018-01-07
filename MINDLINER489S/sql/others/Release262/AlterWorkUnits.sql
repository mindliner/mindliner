
/**
 * These udpates remove the link between work units and week plans. WorkUnits
 * are newly linked to Users and Tasks, only.
 *
 * Author:  marius
 * Created: 15.12.2015
 */

-- Now that we remove the link to week plans we need to have a user references
alter table workunits add column USER_ID INT NOT NULL;

-- Copy user references from weekplan reference
update workunits wu left join weekplans wp on wu.weekplan_id = wp.id  set wu.user_id = wp.user_id;

-- Delete the now unsed week plan reference in the work units
alter table workunits drop column weekplan_id;

