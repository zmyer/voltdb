file -inlinebatch END_OF_DROP_BATCH

DROP PROCEDURE Initialize           IF EXISTS;
DROP PROCEDURE AuthorizeCall        IF EXISTS;
DROP PROCEDURE ContinueCall         IF EXISTS;
DROP PROCEDURE EndCall              IF EXISTS;
DROP PROCEDURE NewAccountType       IF EXISTS;
DROP PROCEDURE NewAccount           IF EXISTS;
DROP PROCEDURE NewPhone             IF EXISTS;
DROP VIEW  active_callers_by_tower  IF EXISTS;
DROP TABLE phones                   IF EXISTS;
DROP TABLE accounts_realtime        IF EXISTS;
DROP TABLE account_types            IF EXISTS;
DROP TABLE problem_reports          IF EXISTS;

END_OF_DROP_BATCH

-- Tell sqlcmd to batch the following commands together,
-- so that the schema loads quickly.
-- file -inlinebatch END_OF_BATCH


CREATE TABLE phones
(
    phone_number    bigint     NOT NULL,
    account_id      bigint     NOT NULL,
    cell_tower_id   smallint,
    call_start_time timestamp,  -- will be null if no call is in progress
    enabled         tinyint    NOT NULL, -- set to false if they try something malicious or lose their phone
    CONSTRAINT UC_number UNIQUE(phone_number, account_id),
);

PARTITION TABLE phones ON COLUMN account_id;

-- Holds account information that will need to be accessed in real time.
-- The account_id can also correspond to a more complete store of account information,
-- such as billing details, that don't need fast access.
-- This table is partitioned so different sites (processors) share the load.
CREATE TABLE accounts_realtime
(
    account_id    bigint     NOT NULL PRIMARY KEY,
    account_type  tinyint    NOT NULL, -- see 'account_types' table
    minutes_left  integer    NOT NULL,
    enabled       tinyint    NOT NULL, -- set to false if they try something malicious or report that their info was stolen
);

PARTITION TABLE accounts_realtime ON COLUMN account_id;

-- Static, replicated table used to look up account types.
CREATE TABLE account_types
(
    type_identifier   tinyint  NOT NULL PRIMARY KEY,
    minutes_per_month integer, -- NULL indicates unlimited
);

-- rollup of votes by contestant and state for the heat map and results
CREATE VIEW active_callers_by_tower
(
    tower_id,
    num_active_callers
)
AS
   SELECT cell_tower_id,
          COUNT(*)
     FROM phones
 GROUP BY cell_tower_id
;

-- END_OF_BATCH

-- Update classes from jar so that the server will know about classes
-- but not procedures yet.
-- This command cannot be part of a DDL batch.
LOAD CLASSES prepaidcaller-procs.jar;

-- The following CREATE PROCEDURE statements can all be batched.
file -inlinebatch END_OF_2ND_BATCH

-- stored procedures
CREATE PROCEDURE PARTITION ON TABLE phones COLUMN account_id FROM CLASS prepaidcaller.AuthorizeCall;
CREATE PROCEDURE PARTITION ON TABLE phones COLUMN account_id FROM CLASS prepaidcaller.ContinueCall;
CREATE PROCEDURE PARTITION ON TABLE phones COLUMN account_id FROM CLASS prepaidcaller.EndCall;
CREATE PROCEDURE FROM CLASS prepaidcaller.Initialize;


-- FIXME these are for testing bug reproducibility
CREATE PROCEDURE NewAccount PARTITION ON TABLE accounts_realtime COLUMN account_id AS 
    INSERT INTO accounts_realtime VALUES (?, ?, ?, 1);
CREATE PROCEDURE NewPhone PARTITION ON TABLE phones COLUMN account_id PARAMETER 1 AS
    INSERT INTO phones VALUES (?, ?, null, null, 1);

END_OF_2ND_BATCH
