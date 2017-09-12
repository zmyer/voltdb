/* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */
lexer grammar SQLLexer;

ADD: A D D;
    
ALL: A L L;
    
ALLOW: A L L O W;
    
ALTER: A L T E R;
    
AND: A N D;

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
    
FALSE: F A L S E;

FIRST: F I R S T;
    
FOLLOWING: F O L L O W I N G;
    
FOR: F O R;
    
FROM: F R O M ;
    
FULL: F U L L ;
    
GROOVY: G R O O V Y;
    
GROUP: G R O U P;
    
GROUPING: G R O U P I N G;
    
HAVING: H A V I N G ;
    
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
    
OR: O R;

ORDER: O R D E R;
    
OVER: O V E R;

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
    
TRUE: T R U E;
    
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

STRING: ['] ( NQLETTER )* ['] ;

// Need this some day.  NUMBER should include this.
// FLOAT: NUMBER ( '.' NUMBER )?;
// Have to do white space specially, though.
//

NUMBER: ( ZERODIGIT )+ | ( NZDIGIT ( DIGIT )*);

fragment LETTER: [a-zA-Z\u0080-\u00FF_] ;

fragment NQLETTER: ~['];	

fragment DIGIT: [0-9];

fragment NZDIGIT: [1-9];

fragment ZERODIGIT: [0];

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
COMMENT: '--' ~[\r\n]*? ('\r')* '\n' -> skip;
SPACE: [ \t\n\r]+ -> skip;
