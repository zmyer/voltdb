-- This is a set of DELETE statements that tests the bare minimum
-- necessary to start to think that maybe we actually support the
-- subset of SQL that we claim to support.
--
-- In the brave new meta-template world (yeah, okay, kill me), these
-- statements expect that the includer will have set the template macros like
-- @comparabletype to have a value that makes sense for the schema under test.
-- So, if the configuration is pushing on string
-- operations, you'll want a line: {@comparabletype = "string"} in
-- the template file that includes this one.

-- Required preprocessor macros (with example values):
-- {@aftermath = " _math _value[int:1,3]"}
-- {@cmp = "_cmp"} -- all comparison operators (=, <>, !=, <, >, <=, >=)
-- {@columntype = "decimal"}
-- {@columnpredicate = "_numericcolumnpredicate"}
-- {@comparableconstant = "42.42"}
-- {@comparabletype = "numeric"}
-- {@dmltable = "_table"}
-- {@fromtables = "_table"}
-- {@insertvals = "_id, _value[decimal], _value[decimal], _value[float]"}

--SELECT
--DELETE
-- Delete them all, then re-insert, then do trickier deletions
-- test basic DELETE
DELETE FROM @dmltable
-- Confirm the values that were deleted
SELECT @star FROM @dmltable

INSERT INTO @dmltable VALUES (@insertvals)
INSERT INTO @dmltable VALUES (@insertvals)
INSERT INTO @dmltable VALUES (@insertvals)
-- test where expressions
--- test comparison ops (<, <=, =, >=, >)
DELETE FROM @dmltable WHERE @columnpredicate
-- Confirm the values that were deleted
SELECT @star FROM @dmltable

INSERT INTO @dmltable VALUES (@insertvals)
INSERT INTO @dmltable VALUES (@insertvals)
INSERT INTO @dmltable VALUES (@insertvals)
--- test logic operators (AND) with comparison ops
DELETE FROM @dmltable WHERE (_variable[@columntype] @cmp @comparableconstant) _logicop @columnpredicate
-- Confirm the values that were deleted
SELECT @star FROM @dmltable

INSERT INTO @dmltable VALUES (@insertvals)
INSERT INTO @dmltable VALUES (@insertvals)
INSERT INTO @dmltable VALUES (@insertvals)
--- test arithmetic operators (+, -, *, /) with comparison ops
DELETE FROM @dmltable WHERE (_variable[@comparabletype] @aftermath) @cmp @comparableconstant
-- Confirm the values that were deleted
SELECT @star FROM @dmltable

-- Test DML (DELETE) using sub-queries - in the WHERE clause (correlated and uncorrelated)
-- (use of @somecmp rather than @cmp reduces the explosion of generated queries)
INSERT INTO @dmltable VALUES (@insertvals)
INSERT INTO @dmltable VALUES (@insertvals)
INSERT INTO @dmltable VALUES (@insertvals)

-- TODO: preferred version of DELETE statements, using table alias (does not work due to ENG-12295):
--DELETE FROM @dmltable D10 WHERE __[#agg]      @somecmp (SELECT @agg(S._variable[#agg])                 FROM @fromtables S WHERE S._variable[#col] <= D10.__[#col])
--INSERT INTO @dmltable VALUES (@insertvals)
--DELETE FROM @dmltable D11 WHERE __[#agg]      @somecmp (SELECT @agg(S._variable[#agg])                 FROM @fromtables S)
--INSERT INTO @dmltable VALUES (@insertvals)
--DELETE FROM @dmltable D12 WHERE __[#agg]      <=       (SELECT @agg(S._variable[#agg @comparabletype]) FROM @fromtables S WHERE S.__[#agg] @somecmp @comparablevalue)
--INSERT INTO @dmltable VALUES (@insertvals)
--DELETE FROM @dmltable D13 WHERE __[#agg]      @somecmp (SELECT      S._variable[#agg]                  FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 1)
-- Deliberately (probably) invalid (LIMIT != 1):
--INSERT INTO @dmltable VALUES (@insertvals)
--DELETE FROM @dmltable D14 WHERE @updatecolumn @somecmp (SELECT      S.@updatecolumn                    FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 2)
--INSERT INTO @dmltable VALUES (@insertvals)
--DELETE FROM @dmltable D15 WHERE @updatecolumn @somecmp (SELECT      S.@updatecolumn                    FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 0)

-- TODO: substitute version of DELETE statements, working around ENG-12295:
DELETE FROM @dmltable[#tbl] WHERE __[#agg]      @somecmp (SELECT @agg(D10._variable[#agg])                 FROM @fromtables D10 WHERE D10._variable[#col] <= __[#tbl].__[#col])
INSERT INTO @dmltable VALUES (@insertvals)
DELETE FROM @dmltable       WHERE __[#agg]      @somecmp (SELECT @agg(D11._variable[#agg])                 FROM @fromtables D11)
INSERT INTO @dmltable VALUES (@insertvals)
DELETE FROM @dmltable       WHERE __[#agg]      <=       (SELECT @agg(D12._variable[#agg @comparabletype]) FROM @fromtables D12 WHERE D12.__[#agg] @somecmp @comparablevalue)
INSERT INTO @dmltable VALUES (@insertvals)
DELETE FROM @dmltable       WHERE __[#agg]      @somecmp (SELECT      D13._variable[#agg]                  FROM @fromtables D13 ORDER BY @idcol _sortorder LIMIT 1)
-- Deliberately (probably) invalid (LIMIT != 1):
INSERT INTO @dmltable VALUES (@insertvals)
DELETE FROM @dmltable       WHERE @updatecolumn @somecmp (SELECT      D14.@updatecolumn                    FROM @fromtables D14 ORDER BY @idcol _sortorder LIMIT 2)
INSERT INTO @dmltable VALUES (@insertvals)
DELETE FROM @dmltable       WHERE @updatecolumn @somecmp (SELECT      D15.@updatecolumn                    FROM @fromtables D15 ORDER BY @idcol _sortorder LIMIT 0)

-- Confirm the values that were deleted
SELECT @star FROM @dmltable
