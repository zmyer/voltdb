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


-- Incident reports are uncommon and best addressed by humans.
-- The 'problem reported' tinyints are 
-- Enterprise Edition users should keep this as a real time export stream.
-- Community Edition users can write a make it a table and query it with 'sqlcmd' (or upgrade)

-- CREATE STREAM problem_reports
CREATE TABLE problem_reports
(
    phone_number    bigint        NOT NULL,
    account_id      integer       NOT NULL PRIMARY KEY,
    report_time     timestamp     NOT NULL,
    phone_blocked   tinyint       NOT NULL,
    account_blocked tinyint       NOT NULL,
    description     varchar(1000),
);

PARTITION TABLE problem_reports ON COLUMN account_id;

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
LOAD CLASSES voter-procs.jar;

-- The following CREATE PROCEDURE statements can all be batched.
file -inlinebatch END_OF_2ND_BATCH

-- stored procedures
CREATE PROCEDURE PARTITION ON TABLE phones COLUMN account_id FROM CLASS voter.AuthorizeCall;
CREATE PROCEDURE PARTITION ON TABLE phones COLUMN account_id FROM CLASS voter.ContinueCall;
CREATE PROCEDURE PARTITION ON TABLE phones COLUMN account_id FROM CLASS voter.EndCall;
CREATE PROCEDURE FROM CLASS voter.Initialize;

END_OF_2ND_BATCH

-- verify problem reports can be written
INSERT INTO problem_reports VALUES (0010000000000, 0, CURRENT_TIMESTAMP, 0, 0, 'Benign Test Incident');

