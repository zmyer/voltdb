
LOAD classes sp.jar;

file -inlinebatch END_OF_BATCH

------- Kafka Importer Tables ---G

CREATE TABLE kafkaimporttable0
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table0 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable0 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly0 as upsert into KAFKAIMPORTTABLE0 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly0 ON TABLE Kafkaimporttable0 COLUMN instance_id;

CREATE TABLE kafkaimporttable1
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table1 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable1 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly1 as upsert into KAFKAIMPORTTABLE1 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly1 ON TABLE Kafkaimporttable1 COLUMN instance_id;

CREATE TABLE kafkaimporttable2
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table2 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable2 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly2 as upsert into KAFKAIMPORTTABLE2 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly2 ON TABLE Kafkaimporttable2 COLUMN instance_id;

CREATE TABLE kafkaimporttable3
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table3 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable3 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly3 as upsert into KAFKAIMPORTTABLE3 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly3 ON TABLE Kafkaimporttable3 COLUMN instance_id;

CREATE TABLE kafkaimporttable4
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table4 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable4 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly4 as upsert into KAFKAIMPORTTABLE4 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly4 ON TABLE Kafkaimporttable4 COLUMN instance_id;

CREATE TABLE kafkaimporttable5
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table5 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable5 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly5 as upsert into KAFKAIMPORTTABLE5 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly5 ON TABLE Kafkaimporttable5 COLUMN instance_id;

CREATE TABLE kafkaimporttable6
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table6 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable6 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly6 as upsert into KAFKAIMPORTTABLE6 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly6 ON TABLE Kafkaimporttable6 COLUMN instance_id;

CREATE TABLE kafkaimporttable7
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table7 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable7 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly7 as upsert into KAFKAIMPORTTABLE7 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly7 ON TABLE Kafkaimporttable7 COLUMN instance_id;

CREATE TABLE kafkaimporttable8
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table8 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable8 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly8 as upsert into KAFKAIMPORTTABLE8 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly8 ON TABLE Kafkaimporttable8 COLUMN instance_id;



CREATE TABLE kafkaimporttable9
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table9 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable9 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly9 as upsert into KAFKAIMPORTTABLE9 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly9 ON TABLE Kafkaimporttable9 COLUMN instance_id;



CREATE TABLE kafkaimporttable10
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table10 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable10 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly10 as upsert into KAFKAIMPORTTABLE10 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly10 ON TABLE Kafkaimporttable10 COLUMN instance_id;



CREATE TABLE kafkaimporttable11
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table11 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable11 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly11 as upsert into KAFKAIMPORTTABLE11 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly11 ON TABLE Kafkaimporttable11 COLUMN instance_id;



CREATE TABLE kafkaimporttable12
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table12 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable12 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly12 as upsert into KAFKAIMPORTTABLE12 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly12 ON TABLE Kafkaimporttable12 COLUMN instance_id;



CREATE TABLE kafkaimporttable13
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table13 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable13 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly13 as upsert into KAFKAIMPORTTABLE13 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly13 ON TABLE Kafkaimporttable13 COLUMN instance_id;



CREATE TABLE kafkaimporttable14
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table14 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable14 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly14 as upsert into KAFKAIMPORTTABLE14 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly14 ON TABLE Kafkaimporttable14 COLUMN instance_id;



CREATE TABLE kafkaimporttable15
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table15 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable15 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly15 as upsert into KAFKAIMPORTTABLE15 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly15 ON TABLE Kafkaimporttable15 COLUMN instance_id;



CREATE TABLE kafkaimporttable16
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table16 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable16 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly16 as upsert into KAFKAIMPORTTABLE16 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly16 ON TABLE Kafkaimporttable16 COLUMN instance_id;



CREATE TABLE kafkaimporttable17
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table17 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable17 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly17 as upsert into KAFKAIMPORTTABLE17 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly17 ON TABLE Kafkaimporttable17 COLUMN instance_id;



CREATE TABLE kafkaimporttable18
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table18 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable18 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly18 as upsert into KAFKAIMPORTTABLE18 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly18 ON TABLE Kafkaimporttable18 COLUMN instance_id;



CREATE TABLE kafkaimporttable19
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table19 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable19 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly19 as upsert into KAFKAIMPORTTABLE19 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly19 ON TABLE Kafkaimporttable19 COLUMN instance_id;



CREATE TABLE kafkaimporttable20
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table20 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable20 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly20 as upsert into KAFKAIMPORTTABLE20 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly20 ON TABLE Kafkaimporttable20 COLUMN instance_id;



CREATE TABLE kafkaimporttable21
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table21 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable21 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly21 as upsert into KAFKAIMPORTTABLE21 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly21 ON TABLE Kafkaimporttable21 COLUMN instance_id;



CREATE TABLE kafkaimporttable22
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table22 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable22 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly22 as upsert into KAFKAIMPORTTABLE22 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly22 ON TABLE Kafkaimporttable22 COLUMN instance_id;



CREATE TABLE kafkaimporttable23
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table23 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable23 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly23 as upsert into KAFKAIMPORTTABLE23 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly23 ON TABLE Kafkaimporttable23 COLUMN instance_id;



CREATE TABLE kafkaimporttable24
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table24 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable24 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly24 as upsert into KAFKAIMPORTTABLE24 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly24 ON TABLE Kafkaimporttable24 COLUMN instance_id;



CREATE TABLE kafkaimporttable25
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table25 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable25 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly25 as upsert into KAFKAIMPORTTABLE25 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly25 ON TABLE Kafkaimporttable25 COLUMN instance_id;



CREATE TABLE kafkaimporttable26
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table26 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable26 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly26 as upsert into KAFKAIMPORTTABLE26 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly26 ON TABLE Kafkaimporttable26 COLUMN instance_id;



CREATE TABLE kafkaimporttable27
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table27 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable27 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly27 as upsert into KAFKAIMPORTTABLE27 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly27 ON TABLE Kafkaimporttable27 COLUMN instance_id;



CREATE TABLE kafkaimporttable28
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table28 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable28 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly28 as upsert into KAFKAIMPORTTABLE28 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly28 ON TABLE Kafkaimporttable28 COLUMN instance_id;



CREATE TABLE kafkaimporttable29
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table29 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable29 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly29 as upsert into KAFKAIMPORTTABLE29 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly29 ON TABLE Kafkaimporttable29 COLUMN instance_id;



CREATE TABLE kafkaimporttable30
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table30 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable30 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly30 as upsert into KAFKAIMPORTTABLE30 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly30 ON TABLE Kafkaimporttable30 COLUMN instance_id;



CREATE TABLE kafkaimporttable31
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table31 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable31 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly31 as upsert into KAFKAIMPORTTABLE31 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly31 ON TABLE Kafkaimporttable31 COLUMN instance_id;



CREATE TABLE kafkaimporttable32
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table32 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable32 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly32 as upsert into KAFKAIMPORTTABLE32 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly32 ON TABLE Kafkaimporttable32 COLUMN instance_id;



CREATE TABLE kafkaimporttable33
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table33 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable33 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly33 as upsert into KAFKAIMPORTTABLE33 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly33 ON TABLE Kafkaimporttable33 COLUMN instance_id;



CREATE TABLE kafkaimporttable34
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table34 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable34 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly34 as upsert into KAFKAIMPORTTABLE34 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly34 ON TABLE Kafkaimporttable34 COLUMN instance_id;



CREATE TABLE kafkaimporttable35
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table35 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable35 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly35 as upsert into KAFKAIMPORTTABLE35 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly35 ON TABLE Kafkaimporttable35 COLUMN instance_id;



CREATE TABLE kafkaimporttable36
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table36 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable36 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly36 as upsert into KAFKAIMPORTTABLE36 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly36 ON TABLE Kafkaimporttable36 COLUMN instance_id;



CREATE TABLE kafkaimporttable37
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table37 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable37 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly37 as upsert into KAFKAIMPORTTABLE37 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly37 ON TABLE Kafkaimporttable37 COLUMN instance_id;



CREATE TABLE kafkaimporttable38
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table38 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable38 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly38 as upsert into KAFKAIMPORTTABLE38 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly38 ON TABLE Kafkaimporttable38 COLUMN instance_id;



CREATE TABLE kafkaimporttable39
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table39 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable39 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly39 as upsert into KAFKAIMPORTTABLE39 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly39 ON TABLE Kafkaimporttable39 COLUMN instance_id;



CREATE TABLE kafkaimporttable40
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table40 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable40 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly40 as upsert into KAFKAIMPORTTABLE40 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly40 ON TABLE Kafkaimporttable40 COLUMN instance_id;



CREATE TABLE kafkaimporttable41
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table41 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable41 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly41 as upsert into KAFKAIMPORTTABLE41 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly41 ON TABLE Kafkaimporttable41 COLUMN instance_id;



CREATE TABLE kafkaimporttable42
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table42 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable42 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly42 as upsert into KAFKAIMPORTTABLE42 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly42 ON TABLE Kafkaimporttable42 COLUMN instance_id;



CREATE TABLE kafkaimporttable43
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table43 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable43 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly43 as upsert into KAFKAIMPORTTABLE43 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly43 ON TABLE Kafkaimporttable43 COLUMN instance_id;



CREATE TABLE kafkaimporttable44
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table44 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable44 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly44 as upsert into KAFKAIMPORTTABLE44 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly44 ON TABLE Kafkaimporttable44 COLUMN instance_id;



CREATE TABLE kafkaimporttable45
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table45 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable45 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly45 as upsert into KAFKAIMPORTTABLE45 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly45 ON TABLE Kafkaimporttable45 COLUMN instance_id;



CREATE TABLE kafkaimporttable46
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table46 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable46 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly46 as upsert into KAFKAIMPORTTABLE46 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly46 ON TABLE Kafkaimporttable46 COLUMN instance_id;



CREATE TABLE kafkaimporttable47
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table47 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable47 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly47 as upsert into KAFKAIMPORTTABLE47 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly47 ON TABLE Kafkaimporttable47 COLUMN instance_id;



CREATE TABLE kafkaimporttable48
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table48 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable48 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly48 as upsert into KAFKAIMPORTTABLE48 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly48 ON TABLE Kafkaimporttable48 COLUMN instance_id;



CREATE TABLE kafkaimporttable49
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table49 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable49 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly49 as upsert into KAFKAIMPORTTABLE49 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly49 ON TABLE Kafkaimporttable49 COLUMN instance_id;



CREATE TABLE kafkaimporttable50
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table50 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable50 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly50 as upsert into KAFKAIMPORTTABLE50 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly50 ON TABLE Kafkaimporttable50 COLUMN instance_id;



CREATE TABLE kafkaimporttable51
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table51 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable51 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly51 as upsert into KAFKAIMPORTTABLE51 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly51 ON TABLE Kafkaimporttable51 COLUMN instance_id;



CREATE TABLE kafkaimporttable52
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table52 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable52 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly52 as upsert into KAFKAIMPORTTABLE52 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly52 ON TABLE Kafkaimporttable52 COLUMN instance_id;



CREATE TABLE kafkaimporttable53
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table53 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable53 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly53 as upsert into KAFKAIMPORTTABLE53 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly53 ON TABLE Kafkaimporttable53 COLUMN instance_id;



CREATE TABLE kafkaimporttable54
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table54 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable54 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly54 as upsert into KAFKAIMPORTTABLE54 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly54 ON TABLE Kafkaimporttable54 COLUMN instance_id;



CREATE TABLE kafkaimporttable55
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table55 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable55 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly55 as upsert into KAFKAIMPORTTABLE55 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly55 ON TABLE Kafkaimporttable55 COLUMN instance_id;



CREATE TABLE kafkaimporttable56
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table56 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable56 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly56 as upsert into KAFKAIMPORTTABLE56 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly56 ON TABLE Kafkaimporttable56 COLUMN instance_id;



CREATE TABLE kafkaimporttable57
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table57 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable57 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly57 as upsert into KAFKAIMPORTTABLE57 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly57 ON TABLE Kafkaimporttable57 COLUMN instance_id;



CREATE TABLE kafkaimporttable58
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table58 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable58 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly58 as upsert into KAFKAIMPORTTABLE58 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly58 ON TABLE Kafkaimporttable58 COLUMN instance_id;



CREATE TABLE kafkaimporttable59
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table59 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable59 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly59 as upsert into KAFKAIMPORTTABLE59 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly59 ON TABLE Kafkaimporttable59 COLUMN instance_id;



CREATE TABLE kafkaimporttable60
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table60 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable60 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly60 as upsert into KAFKAIMPORTTABLE60 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly60 ON TABLE Kafkaimporttable60 COLUMN instance_id;



CREATE TABLE kafkaimporttable61
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table61 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable61 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly61 as upsert into KAFKAIMPORTTABLE61 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly61 ON TABLE Kafkaimporttable61 COLUMN instance_id;



CREATE TABLE kafkaimporttable62
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table62 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable62 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly62 as upsert into KAFKAIMPORTTABLE62 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly62 ON TABLE Kafkaimporttable62 COLUMN instance_id;



CREATE TABLE kafkaimporttable63
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table63 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable63 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly63 as upsert into KAFKAIMPORTTABLE63 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly63 ON TABLE Kafkaimporttable63 COLUMN instance_id;



CREATE TABLE kafkaimporttable64
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table64 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable64 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly64 as upsert into KAFKAIMPORTTABLE64 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly64 ON TABLE Kafkaimporttable64 COLUMN instance_id;



CREATE TABLE kafkaimporttable65
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table65 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable65 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly65 as upsert into KAFKAIMPORTTABLE65 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly65 ON TABLE Kafkaimporttable65 COLUMN instance_id;



CREATE TABLE kafkaimporttable66
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table66 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable66 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly66 as upsert into KAFKAIMPORTTABLE66 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly66 ON TABLE Kafkaimporttable66 COLUMN instance_id;



CREATE TABLE kafkaimporttable67
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table67 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable67 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly67 as upsert into KAFKAIMPORTTABLE67 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly67 ON TABLE Kafkaimporttable67 COLUMN instance_id;



CREATE TABLE kafkaimporttable68
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table68 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable68 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly68 as upsert into KAFKAIMPORTTABLE68 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly68 ON TABLE Kafkaimporttable68 COLUMN instance_id;



CREATE TABLE kafkaimporttable69
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table69 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable69 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly69 as upsert into KAFKAIMPORTTABLE69 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly69 ON TABLE Kafkaimporttable69 COLUMN instance_id;



CREATE TABLE kafkaimporttable70
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table70 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable70 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly70 as upsert into KAFKAIMPORTTABLE70 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly70 ON TABLE Kafkaimporttable70 COLUMN instance_id;



CREATE TABLE kafkaimporttable71
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table71 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable71 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly71 as upsert into KAFKAIMPORTTABLE71 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly71 ON TABLE Kafkaimporttable71 COLUMN instance_id;



CREATE TABLE kafkaimporttable72
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table72 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable72 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly72 as upsert into KAFKAIMPORTTABLE72 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly72 ON TABLE Kafkaimporttable72 COLUMN instance_id;



CREATE TABLE kafkaimporttable73
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table73 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable73 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly73 as upsert into KAFKAIMPORTTABLE73 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly73 ON TABLE Kafkaimporttable73 COLUMN instance_id;



CREATE TABLE kafkaimporttable74
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table74 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable74 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly74 as upsert into KAFKAIMPORTTABLE74 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly74 ON TABLE Kafkaimporttable74 COLUMN instance_id;



CREATE TABLE kafkaimporttable75
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table75 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable75 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly75 as upsert into KAFKAIMPORTTABLE75 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly75 ON TABLE Kafkaimporttable75 COLUMN instance_id;



CREATE TABLE kafkaimporttable76
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table76 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable76 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly76 as upsert into KAFKAIMPORTTABLE76 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly76 ON TABLE Kafkaimporttable76 COLUMN instance_id;



CREATE TABLE kafkaimporttable77
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table77 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable77 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly77 as upsert into KAFKAIMPORTTABLE77 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly77 ON TABLE Kafkaimporttable77 COLUMN instance_id;



CREATE TABLE kafkaimporttable78
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table78 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable78 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly78 as upsert into KAFKAIMPORTTABLE78 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly78 ON TABLE Kafkaimporttable78 COLUMN instance_id;



CREATE TABLE kafkaimporttable79
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table79 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable79 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly79 as upsert into KAFKAIMPORTTABLE79 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly79 ON TABLE Kafkaimporttable79 COLUMN instance_id;



CREATE TABLE kafkaimporttable80
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table80 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable80 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly80 as upsert into KAFKAIMPORTTABLE80 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly80 ON TABLE Kafkaimporttable80 COLUMN instance_id;



CREATE TABLE kafkaimporttable81
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table81 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable81 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly81 as upsert into KAFKAIMPORTTABLE81 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly81 ON TABLE Kafkaimporttable81 COLUMN instance_id;



CREATE TABLE kafkaimporttable82
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table82 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable82 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly82 as upsert into KAFKAIMPORTTABLE82 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly82 ON TABLE Kafkaimporttable82 COLUMN instance_id;



CREATE TABLE kafkaimporttable83
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table83 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable83 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly83 as upsert into KAFKAIMPORTTABLE83 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly83 ON TABLE Kafkaimporttable83 COLUMN instance_id;



CREATE TABLE kafkaimporttable84
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table84 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable84 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly84 as upsert into KAFKAIMPORTTABLE84 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly84 ON TABLE Kafkaimporttable84 COLUMN instance_id;



CREATE TABLE kafkaimporttable85
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table85 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable85 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly85 as upsert into KAFKAIMPORTTABLE85 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly85 ON TABLE Kafkaimporttable85 COLUMN instance_id;



CREATE TABLE kafkaimporttable86
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table86 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable86 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly86 as upsert into KAFKAIMPORTTABLE86 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly86 ON TABLE Kafkaimporttable86 COLUMN instance_id;



CREATE TABLE kafkaimporttable87
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table87 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable87 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly87 as upsert into KAFKAIMPORTTABLE87 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly87 ON TABLE Kafkaimporttable87 COLUMN instance_id;



CREATE TABLE kafkaimporttable88
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table88 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable88 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly88 as upsert into KAFKAIMPORTTABLE88 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly88 ON TABLE Kafkaimporttable88 COLUMN instance_id;



CREATE TABLE kafkaimporttable89
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table89 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable89 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly89 as upsert into KAFKAIMPORTTABLE89 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly89 ON TABLE Kafkaimporttable89 COLUMN instance_id;



CREATE TABLE kafkaimporttable90
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table90 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable90 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly90 as upsert into KAFKAIMPORTTABLE90 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly90 ON TABLE Kafkaimporttable90 COLUMN instance_id;



CREATE TABLE kafkaimporttable91
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table91 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable91 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly91 as upsert into KAFKAIMPORTTABLE91 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly91 ON TABLE Kafkaimporttable91 COLUMN instance_id;



CREATE TABLE kafkaimporttable92
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table92 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable92 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly92 as upsert into KAFKAIMPORTTABLE92 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly92 ON TABLE Kafkaimporttable92 COLUMN instance_id;



CREATE TABLE kafkaimporttable93
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table93 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable93 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly93 as upsert into KAFKAIMPORTTABLE93 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly93 ON TABLE Kafkaimporttable93 COLUMN instance_id;



CREATE TABLE kafkaimporttable94
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table94 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable94 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly94 as upsert into KAFKAIMPORTTABLE94 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly94 ON TABLE Kafkaimporttable94 COLUMN instance_id;



CREATE TABLE kafkaimporttable95
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table95 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable95 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly95 as upsert into KAFKAIMPORTTABLE95 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly95 ON TABLE Kafkaimporttable95 COLUMN instance_id;



CREATE TABLE kafkaimporttable96
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table96 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable96 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly96 as upsert into KAFKAIMPORTTABLE96 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly96 ON TABLE Kafkaimporttable96 COLUMN instance_id;



CREATE TABLE kafkaimporttable97
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table97 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable97 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly97 as upsert into KAFKAIMPORTTABLE97 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly97 ON TABLE Kafkaimporttable97 COLUMN instance_id;



CREATE TABLE kafkaimporttable98
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table98 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable98 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly98 as upsert into KAFKAIMPORTTABLE98 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly98 ON TABLE Kafkaimporttable98 COLUMN instance_id;



CREATE TABLE kafkaimporttable99
(
  INSTANCE_ID        BIGINT NOT NULL,
  SEQ                BIGINT,
  EVENT_TYPE_ID      INTEGER,
  EVENT_DATE         TIMESTAMP default now,
  TRANS              VARCHAR(1000),
  CONSTRAINT pk_kafka_import_table99 PRIMARY KEY (instance_id)
);
PARTITION TABLE kafkaimporttable99 ON COLUMN instance_id;

CREATE PROCEDURE InsertOnly99 as upsert into KAFKAIMPORTTABLE99 VALUES(?, ?, ?, ?, ?);
PARTITION PROCEDURE InsertOnly99 ON TABLE Kafkaimporttable99 COLUMN instance_id;

CREATE PROCEDURE ImportCountMinMax as select count(instance_id), min(instance_id), max(instance_id) from kafkaimporttable0;

END_OF_BATCH

