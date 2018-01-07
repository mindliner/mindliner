ALTER TABLE objectcollections ADD `TYPE` VARCHAR(64);
UPDATE objectcollections SET `TYPE` = "GENERIC";