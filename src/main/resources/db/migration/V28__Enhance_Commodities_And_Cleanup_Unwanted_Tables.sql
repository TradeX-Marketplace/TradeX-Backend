-- V28: Enhance Commodities table with parent_name and quality_name, and drop obsolete tables

-- 1. Add parent_name and quality_name to commodities table
ALTER TABLE commodities ADD COLUMN IF NOT EXISTS parent_name VARCHAR(255);
ALTER TABLE commodities ADD COLUMN IF NOT EXISTS quality_name VARCHAR(255);

-- 2. Populate parent_name from existing commodity_name
UPDATE commodities 
SET parent_name = commodity_name 
WHERE parent_name IS NULL OR TRIM(parent_name) = '';

-- 3. Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_commodities_parent_name ON commodities(parent_name);
CREATE INDEX IF NOT EXISTS idx_commodities_quality_name ON commodities(quality_name);

-- 4. Clean up obsolete and unused tables
DROP TABLE IF EXISTS quality_parameters CASCADE;
DROP TABLE IF EXISTS buyer_demands CASCADE;
