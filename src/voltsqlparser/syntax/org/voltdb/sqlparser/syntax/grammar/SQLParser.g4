/*
 * Open questions:
 *   1.) Is it true that constraints defined in a create
 *       table statement must be column names?
 *   2.) Is it true that the order by expressions in a
 *       delete statement must be column names?
 */
grammar SQLParser;
import SQLLexer;

@header {
package org.voltdb.sqlparser.syntax.grammar;
}

dml_statement_list:
       ( dml_statement ( ';' dml_statement )* )
    ;       
ddl_statement_list:
        ( ddl_statement ( ';' ddl_statement )* )
    ;
    
dql_statement_list:
        ( dql_statement ( ';' dql_statement )* )
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

dql_statement:
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
        datatype (
        		      column_default_value
        		  |
                      column_not_null
                  |
                      index_type
                  )*
    ;

column_default_value:
		DEFAULT default_string
	;

column_not_null:
		NOT NULL
	;

column_definition_settings:
        SET ( DEFAULT default_string | ( NOT )? NULL )
    ;

default_string:
		STRING
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

//
// Until we get full expressions implemented, this is the
// best we can do.
//
index_column:
        column_name
    ;

/*
 * Create Procedure
 */
create_procedure:
        CREATE PROCEDURE procedure_name
        ( PARTITION ON TABLE table_primary COLUMN column_name ( PARAMETER position )? )?
        ( ALLOW role_name ( ',' role_name )* )?
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
            (',' constraint_definition ( ',' constraint_definition )* )?
        ')'
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
        ( '(' column_name (',' column_name)* ')' )?
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
 *
 * Apparently the LIMIT and OFFSET are not standard.
 */
cursor_specification:
        query_expression
        ( order_by_clause )?
        ( limit_clause )?
        ( offset_clause )?
    ;

order_by_clause:
        ORDER BY sort_specification_list
    ;

limit_clause:
		LIMIT constant_value_expression
	;
	
offset_clause:
		OFFSET constant_value_expression
	;

query_expression:
        query_expression_body
    ;

with_clause:
        WITH with_list_element ( ',' with_list_element )*
    ;

//
// The standard is more complicated, so as to get
// the precedence right.  We lean on Antlr to bind
// intersection more tightly than union/except.
//
query_expression_body:
        query_primary
    |
    	query_expression_body query_intersect_op query_expression_body
    |
        query_expression_body query_union_op query_expression_body 
    ;

query_union_op:
        ( UNION ( ALL )? ) 
    | 
    	EXCEPT
    ;

query_intersect_op:
		INTERSECT
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
    ;

table_expression:
		from_clause
		( where_clause )?
		( group_by_clause )?
		( having_clause )?
   ;
   
from_clause:
		FROM table_reference_list
	;

table_reference_list:
        table_reference (',' table_reference )*
    ;

table_reference:
		table_factor
		( join_operator
          table_factor
          join_condition )*
    ;

table_factor:
		table_primary ( AS table_alias_name )?
	;

table_primary:
		table_name ( AS table_alias_name )?
	| 
		derived_table AS table_alias_name
	|
		'(' table_reference ')'
	;

derived_table:
		table_subquery
	;

join_operator:
        (
            ( INNER )?
         |
            ( LEFT | RIGHT | FULL )
            ( OUTER )?
        )
        JOIN
    ;

join_condition:
        ON join_boolean_expression
    |
        USING '(' column_name (',' column_name )* ')'
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

where_clause:
		boolean_expression
	;
	

group_by_clause:
		GROUP BY group_item ( ',' group_item )*
	;
	
group_item:
        value_expression
    ;

having_clause:
		HAVING boolean_expression
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
        datatype_name ( '(' constant_integer_value (',' constant_integer_value )? ')' )?
    ;

/*************************************************************************
 * Expressions.
 *************************************************************************/
value_expression:
                '(' value_expression ')'                #null_expr
        |
                value_expression op=timesop value_expression  #times_expr
        |
                value_expression op=addop value_expression    #add_expr
        |
                value_expression op=relop value_expression    #rel_expr
        |
                NOT value_expression                    #not_expr
        |
                value_expression AND value_expression          #conjunction_expr
        |
                value_expression OR value_expression          #disjunction_expr
        |
                boolconst=TRUE                    #true_expr
        |
                boolconst=FALSE                   #false_expr
        |
                col=column_reference              #colref_expr
        |
                num=NUMBER                        #numeric_expr
        |
        		str=STRING	                      #string_expr
        |
        		scalar_function_name '(' ( value_expression (',' value_expression )* )? ')'
        		                                  #scalar_function
        |
        		aggregate_function_name '(' ( value_expression ( ',' value_expression )? )? ')'
        		( OVER window_ref )
        										  #aggregate_function
       	//
       	// There are some other time/date functions with
       	// eccentric syntax which need to be put here.
        ;
        
timesop:
                '*'|'/'
        ;
        
addop:
                '+'|'-'
        ;
        
relop:
                '='|'<'|'>'|'<='|'>='|'!='
        ;

column_reference:
        IDENTIFIER ( '.' IDENTIFIER )?
    ;

boolean_expression:
        value_expression
    ;

// Better if we have full expressions.
constant_value_expression:
        constant_integer_value
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
       	NUMBER
    ;

/*************************************************************************
 * Names.
 *************************************************************************/
 aggregate_function_name:
 		IDENTIFIER
 	;
 
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
    
datatype_name:
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
    
scalar_function_name:
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

