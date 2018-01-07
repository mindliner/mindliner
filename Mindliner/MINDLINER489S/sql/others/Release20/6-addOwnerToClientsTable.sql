# Add the owner field to the client table

alter table clients add column OWNER_ID int(11) not null;

# Initialize the new field with current owners

UPDATE `mindliner5`.`clients` SET `OWNER_ID`='1' WHERE `ID`='1';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='1' WHERE `ID`='5';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='1' WHERE `ID`='3';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='96' WHERE `ID`='22';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='50' WHERE `ID`='11';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='58' WHERE `ID`='12';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='110' WHERE `ID`='28';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='7' WHERE `ID`='14';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='80' WHERE `ID`='15';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='82' WHERE `ID`='16';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='83' WHERE `ID`='17';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='95' WHERE `ID`='21';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='89' WHERE `ID`='19';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='94' WHERE `ID`='20';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='98' WHERE `ID`='23';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='99' WHERE `ID`='24';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='100' WHERE `ID`='25';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='101' WHERE `ID`='26';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='104' WHERE `ID`='27';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='112' WHERE `ID`='29';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='114' WHERE `ID`='30';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='119' WHERE `ID`='34';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='118' WHERE `ID`='33';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='121' WHERE `ID`='35';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='123' WHERE `ID`='36';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='125' WHERE `ID`='37';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='126' WHERE `ID`='38';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='127' WHERE `ID`='39';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='128' WHERE `ID`='40';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='132' WHERE `ID`='41';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='134' WHERE `ID`='42';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='138' WHERE `ID`='44';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='136' WHERE `ID`='45';
UPDATE `mindliner5`.`clients` SET `OWNER_ID`='137' WHERE `ID`='43';