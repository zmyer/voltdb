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

-- Delete all rows, then re-insert, then do trickier deletions
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
