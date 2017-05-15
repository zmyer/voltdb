-- This file holds UPDATE statement patterns that cannot go in basic-update.sql,
-- because they would not work for Geospatial data types (GEOGRAPHY_POINT and
-- GEOGRAPHY). For now, that means DELETE statements that use (DML) sub-queries.
--
-- Required preprocessor macros (with example values):
-- {@aftermath = " _math _value[int:1,3]"}
-- {@cmp = "_cmp"} -- all comparison operators (=, <>, !=, <, >, <=, >=)
-- {@somecmp = "_somecmp"} -- a smaller list of comparison operators (=, <, >=)
-- {@col_type = "decimal"}
-- {@columnpredicate = "_numericcolumnpredicate"}
-- {@comparableconstant = "42.42"}
-- {@comparabletype = "numeric"}
-- {@comparablevalue = "_numericvalue"}
-- {@dmltable = "_table"}
-- {@fromtables = "_table"}
-- {@updatecolumn = "CASH"}
-- {@updatesource = "ID"}
-- {@updatevalue = "_value[decimal]"}

-- Test DML (UPDATE) using sub-queries
-- ... in the WHERE clause (correlated and uncorrelated)
-- (use of @somecmp rather than @cmp reduces the explosion of generated queries)
UPDATE @dmltable U10 SET @updatecolumn = @updatevalue WHERE __[#agg]      @somecmp (SELECT @agg(S._variable[#agg])                 FROM @fromtables S WHERE S._variable[#col] <= U10.__[#col])
UPDATE @dmltable U11 SET @updatecolumn = @updatevalue WHERE __[#agg]      @somecmp (SELECT @agg(S._variable[#agg])                 FROM @fromtables S)
UPDATE @dmltable U12 SET @updatecolumn = @updatevalue WHERE __[#agg]      <=       (SELECT @agg(S._variable[#agg @comparabletype]) FROM @fromtables S WHERE S.__[#agg] @somecmp @comparablevalue)
UPDATE @dmltable U13 SET @updatecolumn = @updatevalue WHERE __[#agg]      @somecmp (SELECT      S._variable[#agg]                  FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 1)
-- Deliberately (probably) invalid (LIMIT != 1):
UPDATE @dmltable U14 SET @updatecolumn = @updatevalue WHERE @updatecolumn @somecmp (SELECT      S.@updatecolumn                    FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 2)
UPDATE @dmltable U15 SET @updatecolumn = @updatevalue WHERE @updatecolumn @somecmp (SELECT      S.@updatecolumn                    FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 0)
-- Confirm the values that were updated
SELECT @star FROM @dmltable

-- ... in the SET clause (correlated and uncorrelated)
UPDATE @dmltable U20 SET @updatecolumn = (SELECT @agg(S._variable[#agg @comparabletype]) FROM @fromtables S WHERE S._variable[#col] <= U20.__[#col])
UPDATE @dmltable U21 SET @updatecolumn = (SELECT @agg(S._variable[#agg @comparabletype]) FROM @fromtables S)
UPDATE @dmltable U22 SET @updatecolumn = (SELECT @agg(S._variable[#agg @comparabletype]) FROM @fromtables S WHERE S.__[#agg] @somecmp @comparablevalue)
UPDATE @dmltable U23 SET @updatecolumn = (SELECT      S._variable[#agg @comparabletype]  FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 1)
-- Deliberately (probably) invalid (LIMIT != 1):
UPDATE @dmltable U24 SET @updatecolumn = (SELECT      S.@updatecolumn                    FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 2)
UPDATE @dmltable U25 SET @updatecolumn = (SELECT      S.@updatecolumn                    FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 0)
-- Confirm the values that were updated
SELECT @star FROM @dmltable

-- ... in both the SET and WHERE clauses (correlated and uncorrelated)
UPDATE @dmltable U30 SET @updatecolumn = (SELECT @agg(S._variable[#agg @comparabletype]) FROM @fromtables S WHERE S._variable[#col] <= U30.__[#col]) \
            WHERE __[#agg]      @somecmp (SELECT @agg(T._variable[#agg])                 FROM @fromtables T WHERE T._variable[#col] <= U30.__[#col])
UPDATE @dmltable U31 SET @updatecolumn = (SELECT @agg(S._variable[#agg @comparabletype]) FROM @fromtables S) \
            WHERE __[#agg]      @somecmp (SELECT @agg(S._variable[#agg])                 FROM @fromtables S)
UPDATE @dmltable U32 SET @updatecolumn = (SELECT @agg(S._variable[#agg @comparabletype]) FROM @fromtables S WHERE S.__[#agg] @somecmp @comparablevalue) \
            WHERE __[#agg]      <=       (SELECT @agg(S._variable[#agg @comparabletype]) FROM @fromtables S WHERE S.__[#agg] @somecmp @comparablevalue)
UPDATE @dmltable U33 SET @updatecolumn = (SELECT      S._variable[#agg @comparabletype]  FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 1) \
            WHERE __[#agg]      @somecmp (SELECT      S._variable[#agg]                  FROM @fromtables S ORDER BY @idcol _sortorder LIMIT 1)
-- Confirm the values that were updated
SELECT @star FROM @dmltable
