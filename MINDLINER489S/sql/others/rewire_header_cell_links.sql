# insert spreadsheet -> cell relation
insert into objects_objects (holder_id,relative_id)
select s.ID , cc.cell 
from 
(select c1.ID as header, c2.ID as cell from cells c1, cells c2, objects_objects oo WHERE 
c1.CELL_TYPE!='Content' and c2.CELL_TYPE='Content' and
c1.ID = oo.HOLDER_ID and oo.RELATIVE_ID = c2.ID) as cc, 
spreadsheets s, 
objects_objects oo1 
WHERE
s.ID = oo1.HOLDER_ID and oo1.RELATIVE_ID = cc.header;

# insert cell -> spreadsheet relation
insert into objects_objects (holder_id,relative_id)
select cc.cell , s.ID
from 
(select c1.ID as header, c2.ID as cell from cells c1, cells c2, objects_objects oo WHERE 
c1.CELL_TYPE!='Content' and c2.CELL_TYPE='Content' and
c1.ID = oo.HOLDER_ID and oo.RELATIVE_ID = c2.ID) as cc, 
spreadsheets s, 
objects_objects oo1 
WHERE
s.ID = oo1.HOLDER_ID and oo1.RELATIVE_ID = cc.header;

# remove header - cell relations
delete from objects_objects where exists 
(select * from cells c1, cells c2
where c1.ID = objects_objects.HOLDER_ID and objects_objects.RELATIVE_ID = c2.ID 
and ((c1.CELL_TYPE!='Content' and c2.CELL_TYPE='Content') or 
(c2.CELL_TYPE!='Content' and c1.CELL_TYPE='Content')));