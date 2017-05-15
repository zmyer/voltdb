-- This file holds DELETE statement patterns that cannot go in basic-delete.sql,
-- because they would not work for Geospatial data types (GEOGRAPHY_POINT and
-- GEOGRAPHY). For now, that means DELETE statements that use (DML) sub-queries.

-- Required preprocessor macros (with example values):
-- {@agg = "_numagg"}
-- {@cmp = "_cmp"} -- all comparison operators (=, <>, !=, <, >, <=, >=)
-- {@somecmp = "_somecmp"} -- a smaller list of comparison operators (=, <, >=)
-- {@comparabletype = "numeric"}
-- {@dmltable = "_table"}
-- {@fromtables = "_table"}
-- {@insertvals = "_id, _value[decimal], _value[decimal], _value[float]"}

-- Delete all rows, then re-insert, then do trickier deletions
DELETE FROM @dmltable
-- Confirm the values that were deleted
SELECT @star FROM @dmltable

-- Test DML (DELETE) using sub-queries - in the WHERE clause (correlated and uncorrelated)
-- (use of @somecmp rather than @cmp reduces the explosion of generated queries)
INSERT INTO @dmltable VALUES (@insertvals)
INSERT INTO @dmltable VALUES (@insertvals)
INSERT INTO @dmltable VALUES (@insertvals)

-- TODO: one possible version of DELETE statements, using table alias, once ENG-12295 is fixed:
--DELETE FROM @dmltable D10 WHERE __[#agg]      @somecmp (SELECT @agg(S._variable[#agg])                     FROM @fromtables S WHERE S._variable[#col] <= D10.__[#col])
--INSERT INTO @dmltable VALUES (@insertvals)
--DELETE FROM @dmltable D11 WHERE __[#agg]      @somecmp (SELECT @agg(S._variable[#agg])                     FROM @fromtables S)
--INSERT INTO @dmltable VALUES (@insertvals)
--DELETE FROM @dmltable D12 WHERE __[#agg]      <=       (SELECT @agg(S._variable[#agg @comparabletype])     FROM @fromtables S WHERE S.__[#agg] @somecmp @comparablevalue)
--INSERT INTO @dmltable VALUES (@insertvals)
--DELETE FROM @dmltable D13 WHERE __[#agg]      @somecmp (SELECT      S._variable[#agg]                      FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 1)
-- Deliberately (likely to be) invalid (LIMIT != 1):
--INSERT INTO @dmltable VALUES (@insertvals)
--DELETE FROM @dmltable D14 WHERE @updatecolumn @somecmp (SELECT      S.@updatecolumn                        FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 2)
--INSERT INTO @dmltable VALUES (@insertvals)
--DELETE FROM @dmltable D15 WHERE @updatecolumn @somecmp (SELECT      S.@updatecolumn                        FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 0)
-- Confirm the values that were deleted
--SELECT @star FROM @dmltable

-- Another version of DELETE statements, using table names, working around ENG-12295:
DELETE FROM @dmltable[#tbl] WHERE __[#agg]      @somecmp (SELECT @agg(D20._variable[#agg])                 FROM @fromtables D20 WHERE D20._variable[#col] <= __[#tbl].__[#col])
INSERT INTO @dmltable VALUES (@insertvals)
DELETE FROM @dmltable       WHERE __[#agg]      @somecmp (SELECT @agg(D21._variable[#agg])                 FROM @fromtables D21)
INSERT INTO @dmltable VALUES (@insertvals)
DELETE FROM @dmltable       WHERE __[#agg]      <=       (SELECT @agg(D22._variable[#agg @comparabletype]) FROM @fromtables D22 WHERE D22.__[#agg] @somecmp @comparablevalue)
INSERT INTO @dmltable VALUES (@insertvals)
DELETE FROM @dmltable       WHERE __[#agg]      @somecmp (SELECT      D23._variable[#agg]                  FROM @fromtables D23 ORDER BY @idcol _sortorder LIMIT 1)
-- Deliberately (likely to be) invalid (LIMIT != 1):
INSERT INTO @dmltable VALUES (@insertvals)
DELETE FROM @dmltable       WHERE @updatecolumn @somecmp (SELECT      D24.@updatecolumn                    FROM @fromtables D24 ORDER BY @idcol _sortorder LIMIT 2)
INSERT INTO @dmltable VALUES (@insertvals)
DELETE FROM @dmltable       WHERE @updatecolumn @somecmp (SELECT      D25.@updatecolumn                    FROM @fromtables D25 ORDER BY @idcol _sortorder LIMIT 0)
-- Confirm the values that were deleted
SELECT @star FROM @dmltable
