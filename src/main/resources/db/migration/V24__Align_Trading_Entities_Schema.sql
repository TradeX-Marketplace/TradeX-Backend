-- V24: Align Trading Entities Schema with JPA Entity Mappings
-- Date: 2026-09-28

-- 1. Align supplies table
ALTER TABLE supplies ADD COLUMN IF NOT EXISTS supply_number VARCHAR(50);
ALTER TABLE supplies ADD COLUMN IF NOT EXISTS masked_label VARCHAR(100);

-- Convert created_by in supplies to VARCHAR(100) if needed
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'supplies' AND column_name = 'created_by' AND data_type = 'bigint'
    ) THEN
        ALTER TABLE supplies DROP CONSTRAINT IF EXISTS supplies_created_by_fkey;
        ALTER TABLE supplies ALTER COLUMN created_by TYPE VARCHAR(100) USING created_by::VARCHAR(100);
    END IF;
END $$;

UPDATE supplies SET supply_number = 'SUP-' || id WHERE supply_number IS NULL;
ALTER TABLE supplies ALTER COLUMN supply_number SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_supplies_supply_number ON supplies(supply_number);

-- 2. Align demands table
ALTER TABLE demands ADD COLUMN IF NOT EXISTS demand_number VARCHAR(50);
ALTER TABLE demands ADD COLUMN IF NOT EXISTS masked_label VARCHAR(100);

-- Convert created_by in demands to VARCHAR(100) if needed
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'demands' AND column_name = 'created_by' AND data_type = 'bigint'
    ) THEN
        ALTER TABLE demands DROP CONSTRAINT IF EXISTS demands_created_by_fkey;
        ALTER TABLE demands ALTER COLUMN created_by TYPE VARCHAR(100) USING created_by::VARCHAR(100);
    END IF;
END $$;

UPDATE demands SET demand_number = 'DEM-' || id WHERE demand_number IS NULL;
ALTER TABLE demands ALTER COLUMN demand_number SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_demands_demand_number ON demands(demand_number);

-- 3. Align bids table
-- Rename bidder_party_id to party_id if exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'bids' AND column_name = 'bidder_party_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'bids' AND column_name = 'party_id'
    ) THEN
        ALTER TABLE bids RENAME COLUMN bidder_party_id TO party_id;
    END IF;
END $$;

-- Rename bid_quantity to quantity if exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'bids' AND column_name = 'bid_quantity'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'bids' AND column_name = 'quantity'
    ) THEN
        ALTER TABLE bids RENAME COLUMN bid_quantity TO quantity;
    END IF;
END $$;

ALTER TABLE bids ADD COLUMN IF NOT EXISTS commodity_id BIGINT;
ALTER TABLE bids ADD COLUMN IF NOT EXISTS demand_id BIGINT;
ALTER TABLE bids ADD COLUMN IF NOT EXISTS market_rate NUMERIC(12, 4);
ALTER TABLE bids ADD COLUMN IF NOT EXISTS masked_bidder_id VARCHAR(50);

-- Populate commodity_id from supply if null
UPDATE bids b
SET commodity_id = s.commodity_id
FROM supplies s
WHERE b.supply_id = s.id AND b.commodity_id IS NULL;

-- If commodity_id is still null, default to first commodity
UPDATE bids SET commodity_id = (SELECT id FROM commodities LIMIT 1) WHERE commodity_id IS NULL;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'bids' AND column_name = 'commodity_id') THEN
        ALTER TABLE bids ALTER COLUMN commodity_id SET NOT NULL;
    END IF;
END $$;

-- Add foreign keys for bids safely
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_bids_commodity') THEN
        ALTER TABLE bids ADD CONSTRAINT fk_bids_commodity FOREIGN KEY (commodity_id) REFERENCES commodities(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_bids_demand') THEN
        ALTER TABLE bids ADD CONSTRAINT fk_bids_demand FOREIGN KEY (demand_id) REFERENCES demands(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_bids_commodity ON bids(commodity_id);
CREATE INDEX IF NOT EXISTS idx_bids_demand ON bids(demand_id);

-- 4. Align negotiations table
ALTER TABLE negotiations ADD COLUMN IF NOT EXISTS negotiation_code VARCHAR(50);
ALTER TABLE negotiations ADD COLUMN IF NOT EXISTS commodity_id BIGINT;
ALTER TABLE negotiations ADD COLUMN IF NOT EXISTS quantity NUMERIC(15, 4);
ALTER TABLE negotiations ADD COLUMN IF NOT EXISTS destination VARCHAR(255);
ALTER TABLE negotiations ADD COLUMN IF NOT EXISTS market_rate NUMERIC(12, 4);
ALTER TABLE negotiations ADD COLUMN IF NOT EXISTS buyer_offer NUMERIC(12, 4);
ALTER TABLE negotiations ADD COLUMN IF NOT EXISTS seller_offer NUMERIC(12, 4);
ALTER TABLE negotiations ADD COLUMN IF NOT EXISTS last_offer_by VARCHAR(20) DEFAULT 'BUYER';

UPDATE negotiations SET negotiation_code = 'NEG-' || id WHERE negotiation_code IS NULL;
ALTER TABLE negotiations ALTER COLUMN negotiation_code SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_negotiations_code ON negotiations(negotiation_code);

-- Populate negotiation commodity_id from bid if null
UPDATE negotiations n
SET commodity_id = b.commodity_id
FROM bids b
WHERE n.bid_id = b.id AND n.commodity_id IS NULL;

UPDATE negotiations SET commodity_id = (SELECT id FROM commodities LIMIT 1) WHERE commodity_id IS NULL;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'negotiations' AND column_name = 'commodity_id') THEN
        ALTER TABLE negotiations ALTER COLUMN commodity_id SET NOT NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_negotiations_commodity') THEN
        ALTER TABLE negotiations ADD CONSTRAINT fk_negotiations_commodity FOREIGN KEY (commodity_id) REFERENCES commodities(id) ON DELETE CASCADE;
    END IF;
END $$;

-- 5. Align negotiation_rounds table
ALTER TABLE negotiation_rounds ADD COLUMN IF NOT EXISTS offered_by_party_id VARCHAR(100);

-- 6. Align deals table
ALTER TABLE deals ADD COLUMN IF NOT EXISTS deal_number VARCHAR(50);
ALTER TABLE deals ADD COLUMN IF NOT EXISTS commodity_id BIGINT;
ALTER TABLE deals ADD COLUMN IF NOT EXISTS destination VARCHAR(255);
ALTER TABLE deals ADD COLUMN IF NOT EXISTS deal_date DATE DEFAULT CURRENT_DATE;
ALTER TABLE deals ADD COLUMN IF NOT EXISTS notes TEXT;

UPDATE deals SET deal_number = 'DEA-' || id WHERE deal_number IS NULL;
ALTER TABLE deals ALTER COLUMN deal_number SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_deals_deal_number ON deals(deal_number);

-- Populate deals commodity_id from supply
UPDATE deals d
SET commodity_id = s.commodity_id
FROM supplies s
WHERE d.supply_id = s.id AND d.commodity_id IS NULL;

UPDATE deals SET commodity_id = (SELECT id FROM commodities LIMIT 1) WHERE commodity_id IS NULL;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'deals' AND column_name = 'commodity_id') THEN
        ALTER TABLE deals ALTER COLUMN commodity_id SET NOT NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_deals_commodity') THEN
        ALTER TABLE deals ADD CONSTRAINT fk_deals_commodity FOREIGN KEY (commodity_id) REFERENCES commodities(id) ON DELETE RESTRICT;
    END IF;
END $$;
