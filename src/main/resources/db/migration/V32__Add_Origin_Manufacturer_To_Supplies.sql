-- V32: Add Origin Manufacturer to Supplies
-- Purpose: Track original manufacturing plant / producer across reselling cycles

ALTER TABLE supplies ADD COLUMN IF NOT EXISTS origin_manufacturer_id BIGINT;
ALTER TABLE supplies ADD COLUMN IF NOT EXISTS origin_manufacturer_name VARCHAR(255);

-- Foreign key constraint linking to parties table
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_supplies_origin_manufacturer'
    ) THEN
        ALTER TABLE supplies 
            ADD CONSTRAINT fk_supplies_origin_manufacturer 
            FOREIGN KEY (origin_manufacturer_id) REFERENCES parties(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Index for filtering/searching supplies by original manufacturer
CREATE INDEX IF NOT EXISTS idx_supplies_origin_mfg ON supplies(origin_manufacturer_id);
