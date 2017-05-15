-- This is a set of UPDATE statements that tests the bare minimum
-- necessary to start to think that maybe we actually support the
-- subset of SQL that we claim to support.
--
-- In the brave new meta-template world (yeah, okay, kill me), these
-- statements expect that the includer will have set the template macros like
-- @comparabletype to have a value that makes sense for the schema under test.
-- So, if the configuration is pushing on string
-- operations, you'll want a line: {@comparabletype = "string"} in
-- the template file that includes this one.
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

--UPDATE
-- Update tests in reverse complexity order since they have persistent effects
-- test where expressions

-- Explicitly using @updatecolumn instead of _variable here in the SET since we
-- don't fail gracefully if the types of the assignment can't be
-- implicitly cast.  See Ticket 200
-- Also, to avoid updating the partition key (by convention, "ID")

-- test simple update
UPDATE @dmltable SET @updatecolumn = @updatevalue
--- test comparison ops (<, <=, =, >=, >)
UPDATE @dmltable SET @updatecolumn = @updatevalue WHERE @columnpredicate
-- Confirm the values that were updated
SELECT @star FROM @dmltable

--- test logic operators (AND) with comparison ops
UPDATE @dmltable SET @updatecolumn = @updatevalue WHERE (@updatecolumn @cmp @comparablevalue) _logicop @columnpredicate
--- test arithmetic operators (+, -, *, /) with comparison ops
UPDATE @dmltable SET @updatecolumn = @updatevalue WHERE (_variable[@comparabletype] @aftermath) @cmp @comparableconstant
-- Confirm the values that were updated
SELECT @star FROM @dmltable

-- test set expression
UPDATE @dmltable SET @updatecolumn = @updatesource @aftermath
-- Confirm the values that were updated
SELECT @star FROM @dmltable

--- test arithmetic (+, -, *, /) ops
-- These fail with an odd message and kill the volt run.
--UPDATE @dmltable SET @updatecolumn = _variable _math _value[byte]
--UPDATE @dmltable SET @updatecolumn = _variable _math _value[int16]
--UPDATE @dmltable SET @updatecolumn = _variable _math _value[int32]
--UPDATE @dmltable SET @updatecolumn = _variable _math _value[int64]
--UPDATE @dmltable SET @updatecolumn = _variable _math _value[float]
--UPDATE @dmltable SET @updatecolumn = _variable _math _value[string]
--UPDATE @dmltable SET @updatecolumn = _variable _math _value[decimal]

-- test type casting
-- ENG-495 test type casting errors
-- These don't make it out of the planner
--UPDATE @dmltable SET @updatecolumn = _value[string]
--UPDATE @dmltable SET @updatecolumn = _value[decimal]
-- Confirm the values that were updated
--SELECT @star FROM @dmltable
