# Dropping columns that are no longer needed

ALTER TABLE `mindliner5`.`log` DROP COLUMN `CLIENT_ID` ;

ALTER TABLE `mindliner5`.`users` DROP COLUMN `CLIENT_ID` ;

ALTER TABLE `mindliner5`.`synchunits` DROP COLUMN `USER_ID` ;

ALTER TABLE `mindliner5`.`knowlets` DROP COLUMN `CATEGORY_ID` ;

ALTER TABLE `mindliner5`.`colorschemes` DROP COLUMN `CLIENT_ID` ;

drop table `mindliner5`.`ratingranges`;

drop table `mindliner5`.`knowletcategories`;

# As of 1.9.0 we are using Solr for searches, hence we drop our own
# structures and code for it.

drop table `mindliner5`.`words`;
drop table `mindliner5`.`objects_words`;
