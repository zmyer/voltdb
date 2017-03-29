file -inlinebatch END_OF_DROP_BATCH

DROP PROCEDURE Initialize                      IF EXISTS;
DROP PROCEDURE Results                         IF EXISTS;
DROP PROCEDURE Vote                            IF EXISTS;
DROP PROCEDURE ContestantWinningStates         IF EXISTS;
DROP PROCEDURE GetStateHeatmap                 IF EXISTS;
DROP VIEW  v_votes_by_phone_number             IF EXISTS;
DROP VIEW  v_votes_by_contestant_number        IF EXISTS;
DROP VIEW  v_votes_by_contestant_number_state  IF EXISTS;
DROP TABLE contestants                         IF EXISTS;
DROP TABLE votes                               IF EXISTS;
DROP TABLE area_code_state                     IF EXISTS;

END_OF_DROP_BATCH

-- Tell sqlcmd to batch the following commands together,
-- so that the schema loads quickly.
file -inlinebatch END_OF_BATCH


CREATE TABLE phones
(
    phone_number    bigint    NOT NULL PRIMARY KEY,
    account_id      integer   NOT NULL,
    cell_tower_id   smallint  NULL,
    call_start_time timestamp NULL, -- will be null if no call is in progress
    enabled         boolean   NOT NULL, -- set to false if they try something malicious or lose their phone
);

PARTITION TABLE phones ON COLUMN account_id;

-- Holds account information that will need to be accessed in real time.
-- The account_id can also correspond to a more complete store of account information,
-- such as billing details, that don't need fast access.
-- This table is partitioned so different sites (processors) share the load.
CREATE TABLE accounts_realtime
(
    account_id    integer  NOT NULL PRIMARY KEY,
    account_type  char     NOT NULL, -- see 'account_types' table
    minutes_left  integer  NOT NULL,
    enabled       boolean  NOT NULL, -- set to false if they try something malicious or report that their info was stolen
    valid_numbers bigint[] NOT NULL,
);

PARTITION TABLE accounts_realtime ON COLUMN account_id;

-- Static, replicated table used to look up account types.
CREATE TABLE account_types
(
    type_identifier   char    NOT NULL PRIMARY KEY,
    minutes_per_month integer NULL, -- NULL indicates unlimited
);


-- Incident reports are uncommon and best addressed by humans.
-- The 'problem reported' booleans are 
-- Enterprise Edition users should keep this as a real time export stream.
-- Community Edition users can write a make it a table and query it with 'sqlcmd' (or upgrade)

-- CREATE STREAM problem_reports
CREATE TABLE problem_reports
(
    phone_number    bigint        NOT NULL,
    account_id      integer       NOT NULL PRIMARY KEY,
    report_time     timestamp     NOT NULL,
    phone_blocked   boolean       NOT NULL,
    account_blocked boolean       NOT NULL,
    description     varchar(1000) NULL,
);


-- rollup of votes by contestant and state for the heat map and results
CREATE VIEW active_callers_by_tower
(
    tower_id,
    num_active_callers,
)
AS
   SELECT cell_tower_id,
        , COUNT(*)
     FROM phones
 GROUP BY cell_tower_id
;

END_OF_BATCH

-- Update classes from jar so that the server will know about classes
-- but not procedures yet.
-- This command cannot be part of a DDL batch.
LOAD CLASSES voter-procs.jar;

-- The following CREATE PROCEDURE statements can all be batched.
file -inlinebatch END_OF_2ND_BATCH

-- stored procedures
CREATE PROCEDURE FROM CLASS voter.Initialize;
CREATE PROCEDURE FROM CLASS voter.Results;
CREATE PROCEDURE PARTITION ON TABLE votes COLUMN phone_number FROM CLASS voter.Vote;
CREATE PROCEDURE FROM CLASS voter.ContestantWinningStates;
CREATE PROCEDURE FROM CLASS voter.GetStateHeatmap;

END_OF_2ND_BATCH


-- The following statements which populate tables can all be batched.
file -inlinebatch END_OF_3RD_BATCH

-- load account types
INSERT INTO account_types VALUES ('U', null); -- Unlimited Plan
INSERT INTO account_types VALUES ('P', 5000); -- Premium Plan
INSERT INTO account_types VALUES ('S', 1000); -- Standard Plan
INSERT INTO account_types VALUES ('E', 30);   -- Emergency Only Plan (helps test running out of minutes)
INSERT INTO account_types VALUES ('R', 5);    -- Ripoff Plan (for testing running out of minutes)

-- verify problem reports can be written
INSERT INTO problem_reports VALUES (0010000000000, 0, CURRENT_TIME(), false, false, 'Benign Test Incident');


END_OF_3RD_BATCH
