-- Run the advanced-template against DDL with (Geographic) polygon type
<configure-for-geo-polygon.sql>
<advanced-template.sql>
-- Note that advanced-update.sql and advanced-delete.sql are not included here,
-- because they use MIN and MAX, which do not work on Geospatial data, in PostgreSQL
