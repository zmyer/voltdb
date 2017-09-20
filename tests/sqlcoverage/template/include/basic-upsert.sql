-- This is a set of UPSERT statements that tests their basic functionality.
-- These statements expect that the includer will have set the template macros
-- like @comparabletype to have a value that makes sense for the schema under
-- test. So, if the configuration is testing string operations, you'll want a
-- line: {@comparabletype = "string"} in the template file that includes this
-- one.
-- NOTE: this file is nearly identical to basic-insert.sql, with "UPSERT"
-- rather than "INSERT".

-- Required preprocessor macros (with example values):
-- {@aftermath = " _math _value[int:1,3]"}
-- {@cmp = "_cmp"} -- all comparison operators (=, <>, !=, <, >, <=, >=)
-- {@columnpredicate = "_numericcolumnpredicate"}
-- {@comparableconstant = "42.42"}
-- {@comparabletype = "numeric"}
-- {@comparablevalue = "_numericvalue"} -- TODO: use of this is currently commented out
-- {@dmltable = "_table"}
-- {@fromtables = "_table"}
-- {@idcol = "ID"}
-- {@insertcols = "ID, VCHAR, NUM, RATIO"}
-- {@insertvals = "_id, _value[string], _value[int16 null30], _value[float]"}
-- {@star = "*"}
-- {@plus10 = "+ 10"}
-- {@updatecolumn = "NUM"} -- TODO: use of this is currently commented out
-- {@updatevalue = "_value[byte]"}


-- Tests of UPSERT INTO VALUES, using all columns (with and without a column list)
UPSERT INTO @dmltable               VALUES (@insertvals)
UPSERT INTO @dmltable (@insertcols) VALUES (@insertvals)
-- Confirm the values that were "upserted"
SELECT @star FROM @dmltable

-- ... using a subset of columns
UPSERT INTO @dmltable (_variable[id], _variable[@comparabletype])                             VALUES (_value[byte], @updatevalue)
UPSERT INTO @dmltable (_variable[@comparabletype], _variable[id])                             VALUES (@updatevalue, _value[byte])
UPSERT INTO @dmltable (_variable[id], _variable[@comparabletype], _variable[@comparabletype]) VALUES (_value[byte], @updatevalue, @updatevalue)
UPSERT INTO @dmltable (_variable[@comparabletype], _variable[id], _variable[@comparabletype]) VALUES (@updatevalue, _value[byte], @updatevalue)
UPSERT INTO @dmltable (_variable[@comparabletype], _variable[@comparabletype], _variable[id]) VALUES (@updatevalue, @updatevalue, _value[byte])
-- Confirm the values that were "upserted"
SELECT @star FROM @dmltable

-- Tests of UPSERT INTO SELECT, using all columns (with and without a column list)
UPSERT INTO @dmltable               SELECT @insertcols FROM @fromtables AS UIS10 ORDER BY @idcol
UPSERT INTO @dmltable (@insertcols) SELECT @insertcols FROM @fromtables AS UIS11 ORDER BY @idcol

-- ... using SELECT *
UPSERT INTO @dmltable               SELECT @star       FROM @fromtables AS UIS12 ORDER BY @idcol
UPSERT INTO @dmltable (@insertcols) SELECT @star       FROM @fromtables AS UIS13 ORDER BY @idcol
-- Confirm the values that were "upserted"
SELECT @star FROM @dmltable

-- ... using a WHERE clause, with comparison ops (<, <=, =, >=, >)
UPSERT INTO @dmltable               SELECT @insertcols FROM @fromtables AS UIS20 WHERE @columnpredicate ORDER BY @idcol
UPSERT INTO @dmltable (@insertcols) SELECT @insertcols FROM @fromtables AS UIS21 WHERE @columnpredicate ORDER BY @idcol
--- ... with logic operators (AND, OR) and comparison ops
UPSERT INTO @dmltable               SELECT @insertcols FROM @fromtables AS UIS22 WHERE (@updatecolumn @somecmp @comparablevalue) _logicop (@updatesource @somecmp @comparablevalue)  ORDER BY @idcol
UPSERT INTO @dmltable (@insertcols) SELECT @insertcols FROM @fromtables AS UIS23 WHERE (@updatecolumn @somecmp @comparablevalue) _logicop (@updatesource @somecmp @comparablevalue)  ORDER BY @idcol
--- ... with arithmetic operators (+, -, *, /) and comparison ops
UPSERT INTO @dmltable               SELECT @insertcols FROM @fromtables AS UIS24 WHERE (_variable[@comparabletype] @aftermath) @cmp @comparableconstant ORDER BY @idcol
UPSERT INTO @dmltable (@insertcols) SELECT @insertcols FROM @fromtables AS UIS25 WHERE (_variable[@comparabletype] @aftermath) @cmp @comparableconstant ORDER BY @idcol
-- Confirm the values that were "upserted"
SELECT @star FROM @dmltable

-- ... using arithmetic (+, -, *, /) ops in the SELECT clause
UPSERT INTO @dmltable (_variable[id], _variable[#c2 @comparabletype])                                 SELECT @idcol + 20, __[#c2]@plus10                 FROM @fromtables AS UIS30 ORDER BY @idcol
UPSERT INTO @dmltable (_variable[#c2 @comparabletype], _variable[id])                                 SELECT __[#c2]@plus10, @idcol + 30                 FROM @fromtables AS UIS31 ORDER BY @idcol
UPSERT INTO @dmltable (_variable[id], _variable[#c2 @comparabletype], _variable[#c3 @comparabletype]) SELECT @idcol + 40, __[#c2]@plus10, __[#c3]@plus10 FROM @fromtables AS UIS32 ORDER BY @idcol
UPSERT INTO @dmltable (_variable[#c2 @comparabletype], _variable[id], _variable[#c3 @comparabletype]) SELECT __[#c2]@plus10, @idcol + 50, __[#c3]@plus10 FROM @fromtables AS UIS33 ORDER BY @idcol
UPSERT INTO @dmltable (_variable[#c2 @comparabletype], _variable[#c3 @comparabletype], _variable[id]) SELECT __[#c2]@plus10, __[#c3]@plus10, @idcol + 60 FROM @fromtables AS UIS34 ORDER BY @idcol
-- Confirm the values that were "upserted"
SELECT @star FROM @dmltable



-- Test DML (UPSERT INTO VALUES) using sub-queries - in the VALUES clause
UPSERT INTO @dmltable (@idcol, _variable[#c2 @comparabletype]) VALUES ( _value[byte], \
    (SELECT @agg(UIV10.__[#c2])        FROM @fromtables UIV10) )
UPSERT INTO @dmltable (_variable[#c2 @comparabletype], @idcol, _variable[#c3 @comparabletype]) VALUES (   \
    (SELECT _symbol[#agfcn @agg](UIV11A.__[#c2]) FROM @fromtables UIV11A WHERE UIV11A.__[#c2] <= @comparablevalue), _value[byte] \
    (SELECT _symbol[#agfcn @agg](UIV11B.__[#c3]) FROM @fromtables UIV11B WHERE UIV11B.__[#c3] > @comparablevalue) )



-- Test DML (UPSERT INTO SELECT) using sub-queries
-- ... in the SELECT clause
-- (use of @somecmp rather than @cmp reduces the explosion of generated queries)
UPSERT INTO @dmltable (@idcol, _variable[#c2 @comparabletype]) \
    SELECT @idcol@plus10, (SELECT @agg(S.__[#c2])  FROM @fromtables S WHERE S.__[#c2] <> @comparablevalue) \
    FROM @fromtables AS UIS40 ORDER BY @idcol

UPSERT INTO @dmltable (@idcol, _variable[#c2 @comparabletype]) \
    SELECT @idcol@plus10, (SELECT @agg(S.__[#c2])  FROM @fromtables) \
    FROM @fromtables AS UIS41 ORDER BY @idcol

UPSERT INTO @dmltable (_variable[#c2 @comparabletype], _variable[#c3 @comparabletype], @idcol) \
    SELECT (SELECT _symbol[#agfcn @agg](S.__[#c2]) FROM @fromtables S WHERE S.__[#c2] <  @comparablevalue), \
           (SELECT _symbol[#agfcn @agg](T.__[#c3]) FROM @fromtables T WHERE T.__[#c3] >= @comparablevalue), @idcol@plus10, \
    FROM @fromtables AS UIS42 ORDER BY @idcol


UPSERT INTO @dmltable (@idcol, _variable[#c2 @comparabletype]) \
    SELECT @idcol@plus10, (SELECT     S.__[#c2]    FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 1) \
    FROM @fromtables AS UIS43 ORDER BY @idcol

-- Deliberately (likely to be) invalid (LIMIT != 1):
UPSERT INTO @dmltable (@idcol, _variable[#c2 @comparabletype]) \
    SELECT @idcol@plus10, (SELECT     S.__[#c2]    FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 2) \
    FROM @fromtables AS UIS44 ORDER BY @idcol

UPSERT INTO @dmltable (@idcol, _variable[#c2 @comparabletype]) \
    SELECT @idcol@plus10, (SELECT     S.__[#c2]    FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 0) \
    FROM @fromtables AS UIS45 ORDER BY @idcol

-- Confirm the values that were "upserted"
SELECT @star FROM @dmltable



-- ... in the WHERE clause
UPSERT INTO @dmltable UIS50 (@idcol, _variable[#c2 @comparabletype]) SELECT @idcol + 200, __[#c2] FROM @fromtables S UIS50 \
    WHERE __[#agg]      @somecmp (SELECT @agg(S._variable[#agg])                 FROM S WHERE S._variable[#c2] <= U15.__[#c2]) \
    ORDER BY @idcol _sortorder
UPSERT INTO @dmltable UIS51 (@idcol, _variable[#c2 @comparabletype]) SELECT @idcol + 220, __[#c2] FROM @fromtables S \
    WHERE __[#agg]      <=       (SELECT @agg(S._variable[#agg] @comparabletype) FROM S WHERE S.__[#agg] @somecmp @comparablevalue) \
    ORDER BY @idcol _sortorder
UPSERT INTO @dmltable UIS52 (@idcol, _variable[#c2 @comparabletype]) SELECT @idcol + 240, __[#c2] FROM @fromtables S \
    WHERE __[#c2]       @somecmp (SELECT      S._variable[#c2]                   FROM S ORDER BY @idcol _sortorder LIMIT 1)
-- Deliberately (probably) invalid (LIMIT != 1):
UPSERT INTO @dmltable UIS53 (@idcol, _variable[#c2 @comparabletype]) SELECT @idcol + 250, __[#c2] FROM @fromtables S \
    WHERE __[#c2]       @somecmp (SELECT      S._variable[#c2]                   FROM S ORDER BY @idcol _sortorder LIMIT 2)
UPSERT INTO @dmltable UIS54 (@idcol, _variable[#c2 @comparabletype]) SELECT @idcol + 250, __[#c2] FROM @fromtables S \
    WHERE __[#c2]       @somecmp (SELECT      S._variable[#c2]                   FROM S ORDER BY @idcol _sortorder LIMIT 0)
-- Confirm the values that were "upserted"
SELECT @star FROM @dmltable

-- TODO: ???
-- ... in both the SELECT and WHERE clauses


