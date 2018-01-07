# actionitems -> actor_id
update actionitems a inner join users u on a.actor_id = u.contact_id set a.actor_id = u.USER_ID;

# colorschemes -> owner_id
update colorschemes c inner join users u on c.owner_id = u.contact_id set c.owner_id = u.USER_ID;

# maps -> owner_id
update maps m inner join users u on m.owner_id = u.contact_id set m.owner_id = u.USER_ID;





# Some links reference conctacts as owners that don't have a user. Therefore we first need to replace the contacts with contacts that have users.

# client 1: take marius messerli (29632)
update links l left join users u on l.owner_id = u.contact_id set l.owner_id = 29632 where u.user_id is null and l.client_id = 1;

# client 12: take ben ching(71596)
update links l left join users u on l.owner_id = u.contact_id set l.owner_id = 71596 where u.user_id is null and l.client_id = 12;

# client 14: take anna Paszulewicz (73174)
update links l left join users u on l.owner_id = u.contact_id set l.owner_id = 73174 where u.user_id is null and l.client_id = 14;

# all other clients: take marius messerli (29632)
update links l left join users u on l.owner_id = u.contact_id set l.owner_id = 29632 where u.user_id is null;

# links -> owner_id
update links l inner join users u on l.owner_id = u.contact_id set l.owner_id = u.USER_ID;




#Same goes for objects

# client 1: take Marius Messerli (29632)
update objects o left join users u on o.owner_id = u.contact_id set o.owner_id = 29632 where u.user_id is null and o.client_id = 1;

# client 10: take Yier Toh (70387)
update objects o left join users u on o.owner_id = u.contact_id set o.owner_id = 70387 where u.user_id is null and o.client_id = 10;

#client 12: take Ben Ching (71596)
update objects o left join users u on o.owner_id = u.contact_id set o.owner_id = 71596 where u.user_id is null and o.client_id = 12;

#client 13: take Niklaus Messerli (73000)
update objects o left join users u on o.owner_id = u.contact_id set o.owner_id = 73000 where u.user_id is null and o.client_id = 13;

#client 14: take anna paszulewicz (73174)
update objects o left join users u on o.owner_id = u.contact_id set o.owner_id = 73174 where u.user_id is null and o.client_id = 14;

#client 15: take Edi Füllemann (73365)
update objects o left join users u on o.owner_id = u.contact_id set o.owner_id = 73365 where u.user_id is null and o.client_id = 15;

#objects -> owner_id
update objects o inner join users u on o.owner_id = u.contact_id set o.owner_id = u.USER_ID;



# colors of the OwnerColorizer (110) have as driver_value the owner contact. So replace them with the user
update colors c inner join users u on c.driver_value = u.CONTACT_ID 
set c.driver_value = u.USER_ID where c.colorizer_id = 110;


# drop users contact column
ALTER TABLE `mindliner5`.`users` DROP COLUMN `CONTACT_ID` ;
