CREATE TABLE store
(
  key      varchar(250) not null
, value    varbinary(1048576) not null
, PRIMARY KEY (key)
);

PARTITION TABLE store ON COLUMN key;

create procedure storeUpsertMP as upsert into store values (?,?);
create procedure storeSelectMP as select * from store where key = ?;

DR table store;
