# Since the last release weekplans may not contain any object type but tasks,
# and linker tasks have been created to link to the plan and to any non-task object
# that was formerly linked to a weekplan.
# However, due to incomplete migration there are still some weekplans having collections 
# rather than tasks. This SQL statement updates the join table so that all plans
# using the substitution task as objects.

UPDATE weekplans_objects 
left join weekplans on weekplan_id = weekplans.id 
left join objects on object_id = objects.id 
left join workunits ON (workunits.object_id = objects.id and workunits.weekplan_id = weekplans.id)
SET weekplans_objects.object_id = workunits.task_id
where DTYPE = 'OCOL' and task_id > 0

# Now there were some link records for objects that no longer exist, the next
# statement deletes those

DELETE  wo.*
FROM weekplans_objects wo
WHERE exists
(SELECT 1 FROM objects o
WHERE o.id = wo.object_id and DTYPE <> 'TASK')

# Finally, after the above has been executed we drop the column object_id as it is not fully replaced by the column task_id

ALTER TABLE `mindliner5`.`workunits` DROP COLUMN `OBJECT_ID` ;
