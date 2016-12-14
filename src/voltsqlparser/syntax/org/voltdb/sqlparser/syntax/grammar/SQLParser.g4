/*
 * Open questions:
 *   1.) Is it true that constraints defined in a create
 *       table statement must be column names?
 *   2.) Is it true that the order by expressions in a
 *       delete statement must be column names?
 */
grammar SQLParser;

@header {
package org.voltdb.sqlparser.syntax.grammar;
}

dml_statement_list:
       ( dml_statement ( ';' dml_statement )* )
    ;       
ddl_statement_list:
        ( ddl_statement ( ';' ddl_statement )* )
    ;
    
meta_statement_list:
        ( meta_statement ( ';' meta_statement )* )
    ;

dml_statement:
        insert_statement
    |   
        update_statement
    |
        delete_statement
    ;

ddl_statement:
        alter_table
    |
        create_index
    |
        create_table
    |
        create_view
    |
        create_procedure
    |
        create_role
    |
        dr_statement
    |
        drop_statement
    |
        export_statement
    |
        import_statement
    |
        partition_statement
    |
        truncate_table_statement
    ;

dql_statment:
        cursor_specification
    ;

meta_statement:
        set_statement
    ;

/*************************************************************************
 * DDL Statements.
 *************************************************************************/
/*
 * Alter Table.
 */
alter_table:
        ALTER TABLE table_primary
        (
            alter_table_drop
        |
            alter_table_add
        |
            alter_table_alter
        )
    ;

alter_table_drop:
        DROP
        (
            CONSTRAINT constraint_name
        |
            ( COLUMN )? column_name ( CASCADE )?
        |
            ( PRIMARY KEY | LIMIT PARTITION ROWS )
        )
    ;

alter_table_add:
        ADD
        (
            constraint_definition
        |
            ( COLUMN )? column_definition ( BEFORE column_name )?
        )
    ;

alter_table_alter:
        ALTER
           ( COLUMN )? column_name
           (
               column_definition_metadata ( CASCADE )?
            |
               column_definition_settings
           )
    ;

column_definition_metadata:
        datatype ( DEFAULT constant_value_expression  )?
                 ( NOT NULL )?
                 ( index_type )?
    ;

column_definition_settings:
        SET ( DEFAULT constant_value | ( NOT )? NULL )
    ;

column_definition:
        column_name column_definition_metadata
    ;

constraint_definition:
        ( CONSTRAINT constraint_name )? ( index_definition | limit_definition )
    ;

index_definition:
        index_type '(' index_column (',' index_column )* ')'
    ;

limit_definition:
        LIMIT PARTITION ROWS row_count
    ;

row_count:
        constant_integer_value
    ;
    
index_type:
        ( PRIMARY KEY )
    |
        UNIQUE
    |
        ASSUMEUNIQUE
    ;
/*
 * Create index.
 */
create_index:
        CREATE (UNIQUE | ASSUMEUNIQUE )? INDEX index_name
        ON ( table_primary | view_name )
        '(' index_column ( ',' index_column )* ')'
        WHERE where_boolean_expression
    ;

index_column:
        value_expression
    ;

/*
 * Create Procedure
 */
create_procedure:
        CREATE PROCEDURE procedure_name
        ( PARTITION ON TABLE table_primary COLUMN column_name ( PARAMETER position )? )?
        ( ALLOW role_name ( ', ' role_name )* )?
        (
            from_class_procedure
        |
            as_single_statement
        )
    ;

position:
        constant_integer_value
    ;

from_class_procedure:
        FROM CLASS class_name
    ;

as_single_statement:
        AS
        (
            SOURCE_CODE LANGUAGE GROOVY
        |
            query_expression
        )
    ;

/*
 * Create Role
 */
create_role:
        CREATE ROLE role_name
        ( WITH permission_name (',' permission_name )* )?
    ;

/*
 * Create stream.
 */
create_stream:
        CREATE STREAM stream_name 
        ( PARTITION ON COLUMN column_name )?
        ( EXPORT TO TARGET export_target_name )?
        '(' 
            column_definition ( ',' column_definition )*
        ')'
    ;

/*
 * Create Table.
 */
create_table:
        CREATE TABLE table_primary '(' 
            ( column_definition ( ',' column_definition )* )?
            (',' ct_constraint_definition ( ',' ct_constraint_definition )* )?
        ')'
    ;

//
// Create table can only have column names
// in indexes.
//
ct_constraint_definition:
        ( CONSTRAINT constraint_name )? ( ct_index_definition | limit_definition )
    ;

ct_index_definition:
        index_type '(' column_name (',' column_name )* ')'
    ;

/*
 * Create View.
 *
 * There are many non contextfree rules here
 * which the grammar does not reflect.
 */
create_view:
        CREATE VIEW view_name '(' view_column_name (',' view_column_name )* ')'
        AS
        view_query_expression
        ;

view_query_expression:
        SELECT project_item ( ',' project_item )*
        FROM table_expression
        ( WHERE where_boolean_expression )?
        ( GROUP BY value_expression
                   ( ',' value_expression )* )?
    ;
// Note: Only stream and table can have CASCADE.
drop_statement:
        DROP dropped_kind dropped_name ( IF EXISTS )? ( CASCADE )?
    ;

dropped_kind:
        INDEX
    |
        TABLE
    |
        ROLE
    |
        STREAM
    |
        PROCEDURE
    |
        VIEW
    ;

/*
 * Partition Statement.
 */
partition_statement:
       partition_procedure_statement
    |
       partition_table_statement
    ;

partition_procedure_statement:
        PARTITION PROCEDURE procedure_name
        ON TABLE table_primary
        COLUMN column_name
        ( PARAMETER position )?
    ;
partition_table_statement:
        PARTITION TABLE table_primary ON COLUMN column_name
    ;

/*
 * DR Statement.
 */
dr_statement:
        DR TABLE table_primary ( DISABLE )?
    ;

/*************************************************************************
 * DML Statements
 *************************************************************************/
/*
 * Delete statement.
 */
delete_statement:
        DELETE FROM table_primary
        ( WHERE where_boolean_expression )?
        ( ORDER BY ( delete_sort_specification ( ',' delete_sort_specification ) )? )
    ;

//
// These may be only column names, and not more
// general expressions.  This may not be right.
// See the questions above.
//
delete_sort_specification:
        column_name ( ordering_specification )? ( null_ordering )?
    ;

/*
 * Insert statement.
 */
insert_statement:
        ( INSERT | UPSERT ) INTO table_primary
        ( column_name (',' column_name)* )?
        (
            insert_values
        |
            insert_select
        )
    ;

insert_values:
        VALUES '(' constant_value_expression (',' constant_value_expression )* ')'
    ;

insert_select:
        query_expression
    ;

/*
 * Select Statement.
 * This is called a query_expression in the standard document.
 */
cursor_specification:
        query_expression
        ( order_by_clause )?
    ;

order_by_clause:
        ORDER BY sort_specification_list
    ;

query_expression:
        ( with_clause )?
        query_expression_body
    ;

with_clause:
        WITH with_list_element ( ',' with_list_element )*
    ;

query_expression_body:
        query_term
    |
        query_expression_body
        ( UNION ( ALL )? | EXCEPT )
        query_term
    ;
query_term:
        query_primary
    |
        query_term
        ( INTERSECT ( ALL )? )
        query_primary
    ;

query_primary:
        simple_table
    |
        '(' query_expression_body ')'
    ;

simple_table:
        query_specification
    |
        explicit_table
    ;

explicit_table:
        TABLE table_or_query_name
    ;

query_specification:
        SELECT ( set_quantifier )? select_list table_expression
        ( ORDER BY sort_specification (',' sort_specification )* )?
        ( LIMIT ( limit_value | ALL ) )?
        ( OFFSET offset_value ( ROW | ROWS ) )?
    ;

select_list:
        ASTERISK
    |
        select_sublist ( ',' select_sublist )*
    ;

with_list_element:
        query_name
        ( '(' column_name (',' column_name )* ')' )?
        AS '(' query_expression ')'
    ;

select_sublist:
        derived_column
    ;

derived_column:
        value_expression ( AS column_alias_name )?
    ;

sort_specification_list:
        sort_specification (',' sort_specification)*
    ;

sort_specification:
        sort_key ( ordering_specification )? ( null_ordering )?
    ;

sort_key:
        value_expression
    ;

ordering_specification:
        ASC | DESC
    ;
    
null_ordering:
        ( NULLS FIRST) | (NULLS LAST )
    ;

project_item:
        value_expression ( ( AS )? column_alias_name )?
    |
        table_alias_name '.' ASTERISK
    ;

table_expression:
        table_reference (',' table_reference )*
    |
        table_expression
        (
            INNER
        |
            ( ( LEFT | RIGHT | FULL )? ( OUTER )? )
        ) JOIN table_expression ( join_condition )?
    ;

join_condition:
        ON join_boolean_expression
    |
        USING '(' column_name (',' column_name )* ')'
    ;

//
// These three are broken up because there are some
// special semantics associated with each.
//   1.) join_boolean_expressions cannot use display
//       list aliases and cannot use scalar subqueries.
//   2.) where_boolean_expressions can use scalar
//       subqueries but cannot use display list
//       aliases.
//   3.) What are the constraints for having_boolean_expression?
//
table_reference:
        (
            table_primary
        |
            view_name
        |
            table_subquery
        )
        // Note: For subquery this is not optional.
        ( ( AS )? table_alias_name )?
    ;

table_primary:
        table_name
    ;

table_subquery:
        subquery
    ;

scalar_subquery:
        subquery
    ;
    
subquery:
        '(' query_expression ')'
    ;

group_item:
        value_expression
    |
        '(' ')'
    |
        '(' value_expression (',' value_expression )* ')'
    |
        CUBE '(' value_expression (',' value_expression )* ')'
    |
        ROLLUP '(' value_expression (',' value_expression )* ')'
    |
        GROUPING SETS '(' group_item (',' group_item )* ')'
    ;

window_ref:
        window_name
    |
        window_spec
    ;

window_spec:
        ( window_name )?
        '('
        ( ORDER BY sort_specification_list )?
        ( PARTITION BY partition_specification_list )?
            (
                 RANGE ( UNBOUNDED | numeric_or_interval_expression) ( PRECEDING | FOLLOWING )
             |
                 ROWS ( UNBOUNDED | numeric_expression ) ( PRECEDING | FOLLOWING )
            )?
        ')'
    ;

partition_specification_list:
        partition_specification (',' partition_specification )*
    ;

partition_specification:
        value_expression
    ;

limit_value:
        constant_integer_value
    ;

offset_value:
        constant_integer_value
    ;

/*
 * Update statement.
 *
 */
update_statement:
        UPDATE table_primary
        SET assign (',' assign )*
        ( WHERE boolean_expression )?
    ;

assign:
        IDENTIFIER '=' value_expression
    ;

truncate_table_statement:
        TRUNCATE TABLE table_primary
    ;

set_quantifier:
        ALL | DISTINCT
    ;

/*************************************************************************
 * Meta statements
 *************************************************************************/
set_statement:
        SET assign
    ;

/*
 * Export and Import Statements
 */
export_statement:
        EXPORT TABLE table_primary TO STREAM stream_name
    ;

import_statement:
        IMPORT CLASS class_name
    ;

/*************************************************************************
 * Datatypes.
 *************************************************************************/
datatype:
        DATATYPE
    ;

/*************************************************************************
 * Expressions.
 *************************************************************************/
value_expression:
        VALUE_EXPRESSION
    ;

boolean_expression:
        value_expression
    ;

constant_value_expression:
        value_expression
    ;

having_boolean_expression:
        value_expression
    ;

join_boolean_expression:
        value_expression
    ;

numeric_expression:
        value_expression
    ;

numeric_or_interval_expression:
        value_expression
    ;

where_boolean_expression:
        value_expression
    ;

constant_value:
        constant_value_expression
    ;

constant_integer_value:
        ( NZDIGIT ( DIGIT )*) | ( ZERODIGIT )*
    ;

/*************************************************************************
 * Names.
 *************************************************************************/
catalog_name:
        IDENTIFIER
    ;
    
class_name:
        IDENTIFIER
    ;
    
column_alias_name:
        IDENTIFIER
    ;
    
column_name:
        IDENTIFIER
    ;
    
constraint_name:
        IDENTIFIER
    ;
    
database_name:
        IDENTIFIER
    ;
    
dropped_name:
        IDENTIFIER
    ;

export_target_name:
        IDENTIFIER
    ;
    
index_name:
        IDENTIFIER
    ;
    
permission_name:
        IDENTIFIER
    ;
    
procedure_name:
        IDENTIFIER
    ;
    
query_name:
        IDENTIFIER
    ;
    
role_name:
        IDENTIFIER
    ;
    
schema_name:
        IDENTIFIER
    ;
    
stream_name:
        IDENTIFIER
    ;
    
table_alias_name:
        IDENTIFIER
    ;
    
table_name:
        IDENTIFIER
    ;
    
table_or_query_name:
        IDENTIFIER
    ;
    
view_column_name:
        IDENTIFIER
    ;
    
view_name:
        IDENTIFIER
    ;
    
window_name:
        IDENTIFIER
    ;

fragment DIGIT: [0-9];

fragment NZDIGIT: [1-9];

fragment ZERODIGIT: [0];

ADD: A D D;
    
ALL: A L L;
    
ALLOW: A L L O W;
    
ALTER: A L T E R;
    
AS: A S;
    
ASC: A S C;
    
ASSUMEUNIQUE: A S S U M E U N I Q U E;
    
ASTERISK: '*';

ATTRIBUTES: A T T R I B U T E S ;
    
BEFORE: B E F O R E;
    
BY: B Y;
    
CASCADE: C A S C A D E;
    
CATALOG: C A T A L O G;
    
CLASS: C L A S S;
    
COLUMN: C O L U M N;
    
CONSTRAINT: C O N S T R A I N T;
    
CREATE: C R E A T E;
    
CUBE: C U B E ;
    
DATABASE: D A T A B A S E;
    
DEFAULT: D E F A U L T;
    
DELETE: D E L E T E;
    
DESC: D E S C ;
    
DISABLE: D I S A B L E;
    
DISTINCT: D I S T I N C T;
    
DML: D M L;
    
DR: D R;
    
DROP: D R O P;
    
EXCEPT: E X C E P T;
    
EXCLUDING: E X C L U D I N G;
    
EXISTS: E X I S T S;
    
EXPORT: E X P O R T;
    
FIRST: F I R S T;
    
FOLLOWING: F O L L O W I N G;
    
FOR: F O R;
    
FROM: F R O M ;
    
FULL: F U L L ;
    
GROOVY: G R O O V Y;
    
GROUP: G R O U P;
    
GROUPING: G R O U P I N G;
    
IF: I F;
    
IMPLEMENTATION: I M P L E M E N T A T I O N;
    
IMPORT: I M P O R T;
    
INCLUDING: I N C L U D I N G;
    
INDEX: I N D E X;
    
INNER: I N N E R;
    
INSERT: I N S E R T;
    
INTERSECT: I N T E R S E C T;
    
INTO: I N T O;
    
JOIN: J O I N;
    
JSON: J S O N;
    
KEY: K E Y ;
    
LANGUAGE: L A N G U A G E;
    
LAST: L A S T;
    
LEFT: L E F T;
    
LIMIT:  L I M I T;
    
NOT: N O T;
    
NULL:  N U L L;
    
NULLS:  N U L L S;
    
OFFSET: O F F S E T;
    
ON: O N;
    
ORDER: O R D E R;
    
OUTER: O U T E R;
    
PARAMETER: P A R A M E T E R;
    
PARTITION: P A R T I T I O N;
    
PLAN: P L A N;
    
PRECEDING: P R E C E D I N G;
    
PRIMARY: P R I M A R Y;
    
PROCEDURE: P R O C E D U R E;
    
RANGE: R A N G E;
    
RIGHT: R I G H T;
    
ROLE: R O L E;
    
ROLLUP: R O L L U P;
    
ROW: R O W;
    
ROWS: R O W S;
    
SCHEMA: S C H E M A;
    
SELECT: S E L E C T;
    
SESSION: S E S S I O N;
    
SET: S E T;
    
SETS: S E T S;
    
SOURCE_CODE: '###' [^#]* '###';

STATEMENT: S T A T E M E N T;
    
STREAM: S T R E A M;
    
SYSTEM: S Y S T E M;
    
TABLE: T A B L E;
    
TARGET: T A R G E T;
    
TO: T O;
    
TRUNCATE: T R U N C A T E;
    
TYPE: T Y P E;
    
UNBOUNDED: U N B O U N D E D;
    
UNION: U N I O N;
    
UNIQUE: U N I Q U E;
    
UPDATE: U P D A T E;
    
UPSERT: U P S E R T;
    
USING: U S I N G;
    
VALUES: V A L U E S;
    
VIEW: V I E W ;
    
WHERE: W H E R E;
    
WITH: W I T H;
    
WITHOUT: W I T H O U T;
    
XML: X M L;

BARBARA:      '||';

IDENTIFIER: LETTER ( LETTER | DIGIT )*;
fragment LETTER: [a-zA-Z\u0080-\u00FF_] ;

fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');

/* Comments */

/* Comments */
COMMENT: '-' '-' .*? ('\r')? '\n' -> skip;
SPACE: [ \t\n] -> skip;
