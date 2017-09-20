-- This is a set of INSERT statements that tests their basic functionality.
-- These statements expect that the includer will have set the template macros
-- like @comparabletype to have a value that makes sense for the schema under
-- test. So, if the configuration is testing string operations, you'll want a
-- line: {@comparabletype = "string"} in the template file that includes this
-- one.
-- NOTE: this file is nearly identical to basic-upsert.sql, with "UPSERT"
-- changed to "INSERT".

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
-- Confirm the values that were inserted
SELECT @star FROM @dmltable

-- ... using a subset of columns
UPSERT INTO @dmltable (_variable[id], _variable[@comparabletype])                             VALUES (_id, @updatevalue)
UPSERT INTO @dmltable (_variable[@comparabletype], _variable[id])                             VALUES (@updatevalue, _id)
UPSERT INTO @dmltable (_variable[id], _variable[@comparabletype], _variable[@comparabletype]) VALUES (_id, @updatevalue, @updatevalue)
UPSERT INTO @dmltable (_variable[@comparabletype], _variable[id], _variable[@comparabletype]) VALUES (@updatevalue, _id, @updatevalue)
UPSERT INTO @dmltable (_variable[@comparabletype], _variable[@comparabletype], _variable[id]) VALUES (@updatevalue, @updatevalue, _id)
-- Confirm the values that were inserted
SELECT @star FROM @dmltable

-- ... using a WHERE clause, with comparison ops (<, <=, =, >=, >)


-- Tests of UPSERT INTO SELECT, using all columns (with and without a column list)
UPSERT INTO @dmltable               SELECT @insertcols FROM @fromtables ORDER BY @idcol
UPSERT INTO @dmltable (@insertcols) SELECT @insertcols FROM @fromtables ORDER BY @idcol

-- ... using SELECT *
UPSERT INTO @dmltable               SELECT @star       FROM @fromtables ORDER BY @idcol
UPSERT INTO @dmltable (@insertcols) SELECT @star       FROM @fromtables ORDER BY @idcol
-- Confirm the values that were inserted
SELECT @star FROM @dmltable

-- ... using a WHERE clause, with comparison ops (<, <=, =, >=, >)
UPSERT INTO @dmltable               SELECT @insertcols FROM @fromtables WHERE @columnpredicate ORDER BY @idcol
UPSERT INTO @dmltable (@insertcols) SELECT @insertcols FROM @fromtables WHERE @columnpredicate ORDER BY @idcol
--- ... with logic operators (AND, OR) and comparison ops
UPSERT INTO @dmltable               SELECT @insertcols FROM @fromtables WHERE (@updatecolumn @somecmp @comparablevalue) _logicop (@updatesource @somecmp @comparablevalue)  ORDER BY @idcol
UPSERT INTO @dmltable (@insertcols) SELECT @insertcols FROM @fromtables WHERE (@updatecolumn @somecmp @comparablevalue) _logicop (@updatesource @somecmp @comparablevalue)  ORDER BY @idcol
--- ... with arithmetic operators (+, -, *, /) and comparison ops
UPSERT INTO @dmltable               SELECT @insertcols FROM @fromtables WHERE (_variable[@comparabletype] @aftermath) @cmp @comparableconstant ORDER BY @idcol
UPSERT INTO @dmltable (@insertcols) SELECT @insertcols FROM @fromtables WHERE (_variable[@comparabletype] @aftermath) @cmp @comparableconstant ORDER BY @idcol
-- Confirm the values that were inserted
SELECT @star FROM @dmltable

-- ... using arithmetic (+, -, *, /) ops in the SELECT clause
UPSERT INTO @dmltable (_variable[id], _variable[#c2 @comparabletype])                                 SELECT @idcol + 20, __[#c2]@plus10                 FROM @fromtables ORDER BY @idcol
UPSERT INTO @dmltable (_variable[#c2 @comparabletype], _variable[id])                                 SELECT __[#c2]@plus10, @idcol + 30                 FROM @fromtables ORDER BY @idcol
UPSERT INTO @dmltable (_variable[id], _variable[#c2 @comparabletype], _variable[#c3 @comparabletype]) SELECT @idcol + 40, __[#c2]@plus10, __[#c3]@plus10 FROM @fromtables ORDER BY @idcol
UPSERT INTO @dmltable (_variable[#c2 @comparabletype], _variable[id], _variable[#c3 @comparabletype]) SELECT __[#c2]@plus10, @idcol + 50, __[#c3]@plus10 FROM @fromtables ORDER BY @idcol
UPSERT INTO @dmltable (_variable[#c2 @comparabletype], _variable[#c3 @comparabletype], _variable[id]) SELECT __[#c2]@plus10, __[#c3]@plus10, @idcol + 60 FROM @fromtables ORDER BY @idcol
-- Confirm the values that were inserted
SELECT @star FROM @dmltable



-- Test DML (UPSERT INTO VALUES) using sub-queries - in the VALUES clause (correlated and uncorrelated)
UPSERT INTO @dmltable[#tbl] (@idcol, _variable[#c2 @comparabletype]) VALUES ( _id, \
    (SELECT _symbol[#agfcn @agg](__[#c2]) FROM @fromtables        IV10 WHERE _variable[#c4 @comparabletype] <= (SELECT __[#agfcn](__[#c4]) FROM __[#tbl]) ) )
UPSERT INTO @dmltable       (_variable[#c2 @comparabletype], @idcol) VALUES ( \
    (SELECT @agg(__[#c2])                 FROM @fromtables IV11), _id )
UPSERT INTO @dmltable[#tbl1] (_variable[#c2 @comparabletype], @idcol, _variable[#c3 @comparabletype]) VALUES ( \
    (SELECT _symbol[#agfcn @agg](__[#c2]) FROM @fromtables[#tbl2] IV12 WHERE _variable[#c4] >  (SELECT __[#agfcn](__[#c4]) FROM __[#tbl1]) ), _id, \
    (SELECT           __[#agfcn](__[#c3]) FROM @fromtables[#tbl2]      WHERE _variable[#c4] <= (SELECT __[#agfcn](__[#c4]) FROM __[#tbl1]) ) )
UPSERT INTO @dmltable[#tbl] (@idcol, _variable[#c2 @comparabletype]) VALUES ( _id, \
    (SELECT __[#c2]                       FROM @fromtables        IV13 ORDER BY @idcol _sortorder LIMIT 1) )
-- Deliberately (probably) invalid (LIMIT != 1):
UPSERT INTO @dmltable[#tbl] (@idcol, _variable[#c2 @comparabletype]) VALUES ( _id, \
    (SELECT __[#c2]                       FROM @fromtables        IV14 ORDER BY @idcol _sortorder LIMIT 2) )
UPSERT INTO @dmltable[#tbl] (@idcol, _variable[#c2 @comparabletype]) VALUES ( _id, \
    (SELECT __[#c2]                       FROM @fromtables        IV15 ORDER BY @idcol _sortorder LIMIT 0) )
-- Confirm the values that were inserted
SELECT @star FROM @dmltable


-- Test DML (UPSERT INTO SELECT) using sub-queries
-- ... in the SELECT clause (correlated and uncorrelated)
-- (use of @somecmp rather than @cmp reduces the explosion of generated queries)
UPSERT INTO @dmltable[#tbl] (@idcol, _variable[#c2 @comparabletype]) \
    SELECT @idcol + 2000, (SELECT _symbol[#agfcn @agg](S.__[#c2]) FROM @fromtables S WHERE S._variable[#c4 @comparabletype] <= (SELECT __[#agfcn](S.__[#c4]) FROM __[#tbl]) ) \
    FROM @fromtables IS10 ORDER BY @idcol
UPSERT INTO @dmltable       (_variable[#c2 @comparabletype], @idcol) \
    SELECT (SELECT @agg(S.__[#c2])                                FROM @fromtables S), @idcol + 4000 \
    FROM @fromtables IS11 ORDER BY @idcol
UPSERT INTO @dmltable       (_variable[#c2 @comparabletype], @idcol, _variable[#c3 @comparabletype]) \
    SELECT (SELECT _symbol[#agfcn @agg](__[#c2])                  FROM @fromtables[#tbl] WHERE __[#c2] >  @comparablevalue), @idcol + 8000, \
           (SELECT           __[#agfcn](__[#c3])                  FROM @fromtables[#tbl] WHERE __[#c3] <= @comparablevalue) \
    FROM @fromtables IS12 ORDER BY @idcol
UPSERT INTO @dmltable[#tbl] (@idcol, _variable[#c2 @comparabletype]) \
    SELECT @idcol + 16000, (SELECT     S.__[#c2]                  FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 1) \
    FROM @fromtables IS13 ORDER BY @idcol
-- Deliberately (likely to be) invalid (LIMIT != 1):
UPSERT INTO @dmltable[#tbl] (@idcol, _variable[#c2 @comparabletype]) \
    SELECT @idcol + 32000, (SELECT     S.__[#c2]                  FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 2) \
    FROM @fromtables IS14 ORDER BY @idcol
UPSERT INTO @dmltable[#tbl] (@idcol, _variable[#c2 @comparabletype]) \
    SELECT @idcol + 32000, (SELECT     S.__[#c2]                  FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 0) \
    FROM @fromtables IS15 ORDER BY @idcol
-- Confirm the values that were inserted
SELECT @star FROM @dmltable


-- ... in the WHERE clause (correlated and uncorrelated)
UPSERT INTO @dmltable[#tbl] (@idcol, _variable[#c2 @comparabletype]) SELECT @idcol + 200, __[#c2] FROM @fromtables IS19 \
    WHERE __[#agg]      @somecmp (SELECT @agg(S._variable[#agg @comparabletype]) FROM @fromtables S WHERE S._variable[#c2] <= __[#tbl].__[#c2]) \
    ORDER BY @idcol _sortorder
UPSERT INTO @dmltable[#tbl] (@idcol, _variable[#c2 @comparabletype]) SELECT @idcol + 200, __[#c2] FROM @fromtables IS20 \
    WHERE __[#agg] _symbol[#scmp @somecmp] (SELECT @agg(S._variable[#agg @comparabletype]) FROM @fromtables S WHERE S._variable[#c2] __[#scmp] IS20.__[#c2]) \
    ORDER BY @idcol _sortorder
UPSERT INTO @dmltable[#tbl] (@idcol, _variable[#c2 @comparabletype]) SELECT @idcol + 220, __[#c2] FROM @fromtables IS21 \
    WHERE __[#c2]       <=       (SELECT @agg(S._variable[#agg] @comparabletype) FROM @fromtables S WHERE S.__[#agg] @somecmp @comparablevalue) \
    ORDER BY @idcol _sortorder
UPSERT INTO @dmltable       (@idcol, _variable[#c2 @comparabletype]) SELECT @idcol + 240, __[#c2] FROM @fromtables IS22 \
    WHERE __[#agg]      @somecmp (SELECT      S._variable[#agg]                  FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 1)
-- Deliberately (likely to be) invalid (LIMIT != 1):
UPSERT INTO @dmltable[#tbl] (@idcol, _variable[#c2 @comparabletype]) SELECT @idcol + 250, __[#c2] FROM @fromtables IS23 \
    WHERE __[#agg]      @somecmp (SELECT      S.@updatecolumn                    FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 2)
UPSERT INTO @dmltable[#tbl] (@idcol, _variable[#c2 @comparabletype]) SELECT @idcol + 250, __[#c2] FROM @fromtables IS24 \
    WHERE __[#agg]      @somecmp (SELECT      S.@updatecolumn                    FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 0)
-- Confirm the values that were inserted
SELECT @star FROM @dmltable

-- TODO: ???
-- ... in both the SELECT and WHERE clauses (correlated and uncorrelated)




-- TODO: delete all this - just for comparison ...


--TODO: just a temp check of how / whether UPSERT will work:
--UPSERT  INTO @dmltable       (_variable[#c2 @comparabletype], @idcol) VALUES ( \
--    (SELECT @agg(UV11.__[#c2])                                FROM @fromtables UV11), _id )

-- Test DML (UPDATE) using sub-queries - in the WHERE clause (correlated and uncorrelated)
-- (use of @somecmp rather than @cmp reduces the explosion of generated queries)
--UPDATE _table U10 SET @updatecolumn = @updatevalue WHERE __[#agg]      @somecmp (SELECT @agg(S._variable[#agg]) FROM _table S WHERE S._variable[#col] = U10.__[#col])
--UPDATE _table U11 SET @updatecolumn = @updatevalue WHERE __[#agg]      =        (SELECT @agg(S._variable[#agg]) FROM _table S WHERE S.__[#agg] @somecmp @comparablevalue)
--UPDATE _table U12 SET @updatecolumn = @updatevalue WHERE __[#agg]      @somecmp (SELECT      S._variable[#agg]  FROM _table S ORDER BY @idcol _sortorder LIMIT 1)
-- Deliberately invalid (LIMIT != 1):
--UPDATE _table U13 SET @updatecolumn = @updatevalue WHERE @updatecolumn @somecmp (SELECT      S.@updatecolumn    FROM _table S ORDER BY @idcol _sortorder LIMIT 2)
--UPDATE _table U14 SET @updatecolumn = @updatevalue WHERE @updatecolumn @somecmp (SELECT      S.@updatecolumn    FROM _table S ORDER BY @idcol _sortorder LIMIT 0)
-- Confirm the values that were updated
--SELECT @star FROM @dmltable

-- ... in the SET clause (correlated and uncorrelated)
--UPDATE _table U15 SET @updatecolumn = (SELECT @agg(S._variable[#agg @comparabletype]) FROM _table S WHERE S._variable[#col] = U15.__[#col])
--UPDATE _table U16 SET @updatecolumn = (SELECT @agg(S._variable[#agg @comparabletype]) FROM _table S WHERE S.__[#agg] @somecmp @comparablevalue)
--UPDATE _table U17 SET @updatecolumn = (SELECT      S._variable[#agg @comparabletype]  FROM _table S ORDER BY @idcol _sortorder LIMIT 1)
-- Deliberately invalid (LIMIT != 1):
--UPDATE _table U18 SET @updatecolumn = (SELECT      S.@updatecolumn                    FROM _table S ORDER BY @idcol _sortorder LIMIT 2)
--UPDATE _table U19 SET @updatecolumn = (SELECT      S.@updatecolumn                    FROM _table S ORDER BY @idcol _sortorder LIMIT 0)
-- Confirm the values that were updated
--SELECT @star FROM @dmltable
