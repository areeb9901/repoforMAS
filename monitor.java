Hi,

The Search functionality on this screen retrieves data from the "LDM_MA_MODIF" table.

For an exact MA ID, the equivalent query is:

SELECT corr_id, ma_id, status, mod_date, FAILED_MSG, IS_PROCESSED
FROM LDM_MA_MODIF
WHERE ma_id = :MA_ID
ORDER BY mod_date DESC;

For an MA ID range:

SELECT corr_id, ma_id, status, mod_date, FAILED_MSG, IS_PROCESSED
FROM LDM_MA_MODIF
WHERE ma_id BETWEEN :START_MA_ID AND :END_MA_ID
ORDER BY mod_date DESC;

Please note that although the query can return multiple records for an MA, the screen displays only the most recent record for each MA ID based on "mod_date".

Best regards,
Areeb Faiz