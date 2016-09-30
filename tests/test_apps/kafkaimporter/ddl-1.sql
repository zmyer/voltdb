-- This is the import table into which a single value will be pushed by kafkaimporter.

-- LOAD classes sp.jar;

file -inlinebatch END_OF_BATCH

------- Kafka Importer Tables -------

CREATE TABLE kafkaimporttable0 
(
  SEQ                BIGINT,
  INSTANCE_ID        BIGINT NOT NULL,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table0 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable0 ON COLUMN instance_id;


CREATE PROCEDURE ImportCountMinMax as select count(instance_id), min(instance_id), max(instance_id) from kafkaimporttable0;

CREATE PROCEDURE InsertOnly0 as upsert into KAFKAIMPORTTABLE0(INSTANCE_ID, SEQ, EVENT_TYPE_ID, EVENT_DATE, TRANS) VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly0 ON TABLE Kafkaimporttable0 COLUMN instance_id;

END_OF_BATCH
