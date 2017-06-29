select count(*) from timedata;
select max(update_ts) from timedata;

-- getNthOldestTimestamp 
SELECT update_ts, SINCE_EPOCH(MILLISECOND, update_ts) FROM timedata ORDER BY update_ts ASC OFFSET 5408 LIMIT 1;

--- deleteOlderThanDate
2017-07-02 21:52:03.914000
DELETE FROM timedata WHERE update_ts <= '2017-07-02 21:52:03.914000';

DELETE FROM timedata WHERE update_ts <= ?;

