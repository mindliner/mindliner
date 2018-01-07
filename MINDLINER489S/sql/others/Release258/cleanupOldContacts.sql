
/**
 * These statements remove some 30k stand-alone contacts from the Bitplane data pool that were imported from the Bitbase at the time
 * Author:  marius
 * Created: 20.11.2015
 */

-- Delete the records from the contact table
delete from contacts where id IN (SELECT id FROM objects where relative_count = 0 AND dtype = 'cont' and client_id = 1) 

-- and then the objects themselves
delete FROM objects where relative_count = 0 AND dtype = 'cont' and client_id = 1