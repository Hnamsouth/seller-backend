--  *********************************************************************
--  SQL to roll back currently unexecuted changes
--  *********************************************************************
--  Change Log: db/changelog/db.changelog-master.yaml
--  Ran at: 12/11/24, 4:18 PM
--  Against: vipo@100.100.0.1@jdbc:mysql://10.255.51.214:8048/voso
--  Liquibase version: 4.29.1
--  *********************************************************************

--  Lock Database
UPDATE voso.liquibase_seller_database_changelog_lock SET `LOCKED` = 1, LOCKEDBY = 'VTP-OSCNTT5 (10.208.165.73)', LOCKGRANTED = NOW() WHERE ID = 1 AND `LOCKED` = 0;

--  Rolling Back ChangeSet: db/changelog/changes/phase5/04-add-data-to-new-tables.yaml::insert-child-seller_order_status::anhdev
--  Release Database Lock
UPDATE voso.liquibase_seller_database_changelog_lock SET `LOCKED` = 0, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1;

