-- V25: Change valid_till to DATE in supplies and demands tables
-- Date: 2026-09-28

ALTER TABLE supplies ALTER COLUMN valid_till TYPE DATE USING valid_till::DATE;
ALTER TABLE demands ALTER COLUMN valid_till TYPE DATE USING valid_till::DATE;
