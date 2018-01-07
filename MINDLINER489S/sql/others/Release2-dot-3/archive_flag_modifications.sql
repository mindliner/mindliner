-- Introduce the ARCHIVED property to all Mindliner objects (and replace lifetime with it)
ALTER TABLE `mindliner5`.`objects` ADD COLUMN `ARCHIVED` TINYINT(1) NULL DEFAULT 0  
AFTER `RELATIVES_ORDERED` , ADD INDEX `IDX_ARCHIVED` (`ARCHIVED` ASC) ;

-- Initialize the archived flag for tasks.
UPDATE tasks t LEFT JOIN objects o ON t.id = o.id SET archived = completed
WHERE o.dtype = 'TASK'

-- Initialized the archive flag for knowlets
UPDATE objects o left join knowlets k on o.id = k.id 
SET archived = (SELECT ABS(TIMESTAMPDIFF(DAY, now(), modification))  >= lifetime)
WHERE dtype = 'KNOW'

-- now before we set the archived flag for collections we increase the lifetime of all collections of 2014 to save them from being archived
update objectcollections c left join objects o on c.id = o.id
SET lifetime = 400 where TIMESTAMPDIFF(DAY, now(), modification) > -360

-- Initialize archived field for object collections
UPDATE objects o left join objectcollections c on o.id = c.id
SET archived = ABS(TIMESTAMPDIFF(DAY, now(), modification))  >= lifetime
WHERE dtype = 'OCOL'

-- Only if the above worked we can now delete the lifetime columns from collections and knowlets
ALTER TABLE `mindliner5`.`objectcollections` DROP COLUMN `LIFETIME` ;
ALTER TABLE `mindliner5`.`knowlets` DROP COLUMN `LIFETIME` ;
