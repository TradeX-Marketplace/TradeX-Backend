-- V14: Create Buyer Demands Table
-- Purpose: Buyer demand for specific commodities with matching to sellers
-- Date: 2026-09-26

CREATE TABLE IF NOT EXISTS buyer_demands (
    id BIGSERIAL PRIMARY KEY,
    demand_code VARCHAR(50) NOT NULL UNIQUE,
    party_id BIGINT NOT NULL,
    commodity_id BIGINT NOT NULL,
    required_quantity NUMERIC(15,2) NOT NULL,
    quantity_unit VARCHAR(20) NOT NULL,
    required_date DATE NOT NULL,
    price_range_min NUMERIC(15,2) NOT NULL,
    price_range_max NUMERIC(15,2) NOT NULL,
    location VARCHAR(100) NOT NULL,
    is_urgent BOOLEAN NOT NULL DEFAULT FALSE,
    quality_grade_id BIGINT,
    special_requirements TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'FULFILLED', 'CANCELLED', 'EXPIRED')),
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE' CHECK (visibility IN ('PRIVATE', 'PUBLIC')),
    is_fulfilled BOOLEAN NOT NULL DEFAULT FALSE,
    fulfilled_by BIGINT,
    fulfilled_date TIMESTAMP,
    cancellation_reason TEXT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sellers_notified_count INTEGER DEFAULT 0,
    
    -- Foreign Keys
    CONSTRAINT fk_demand_party FOREIGN KEY (party_id) REFERENCES parties(id) ON DELETE CASCADE,
    CONSTRAINT fk_demand_commodity FOREIGN KEY (commodity_id) REFERENCES commodities(id) ON DELETE RESTRICT,
    CONSTRAINT fk_demand_quality_grade FOREIGN KEY (quality_grade_id) REFERENCES quality_grades(id) ON DELETE SET NULL,
    CONSTRAINT fk_demand_fulfilled_by FOREIGN KEY (fulfilled_by) REFERENCES parties(id) ON DELETE SET NULL,
    CONSTRAINT fk_demand_created_by FOREIGN KEY (created_by) REFERENCES parties(id) ON DELETE RESTRICT,
    
    -- Validation constraints
    CONSTRAINT chk_price_range CHECK (price_range_max > price_range_min),
    CONSTRAINT chk_quantity CHECK (required_quantity > 0)
);

-- Create indexes for optimal query performance

-- Basic lookups
CREATE INDEX IF NOT EXISTS idx_demand_demand_code ON buyer_demands(demand_code);
CREATE INDEX IF NOT EXISTS idx_demand_party_id ON buyer_demands(party_id);
CREATE INDEX IF NOT EXISTS idx_demand_commodity_id ON buyer_demands(commodity_id);
CREATE INDEX IF NOT EXISTS idx_demand_status ON buyer_demands(status);

-- Date-based queries (find upcoming demands, expiring demands)
CREATE INDEX IF NOT EXISTS idx_demand_required_date ON buyer_demands(required_date);

-- Creation tracking
CREATE INDEX IF NOT EXISTS idx_demand_created_at ON buyer_demands(created_at);

-- CRITICAL INDEX: Used for seller matching - find demands for a commodity
-- When finding sellers' opportunities:
-- WHERE commodity_id = X AND status = 'ACTIVE' AND required_date >= TODAY
-- ORDER BY is_urgent DESC, required_date ASC
CREATE INDEX IF NOT EXISTS idx_demand_commodity_status 
    ON buyer_demands(commodity_id, status, required_date, is_urgent);

-- Location-based queries (future feature)
CREATE INDEX IF NOT EXISTS idx_demand_location 
    ON buyer_demands(location, status, required_date);

-- Fulfillment tracking
CREATE INDEX IF NOT EXISTS idx_demand_fulfilled_by 
    ON buyer_demands(fulfilled_by, status);

-- Public demands (marketplace feature - future)
CREATE INDEX IF NOT EXISTS idx_demand_visibility_status 
    ON buyer_demands(visibility, status, is_urgent);

-- Expiration tracking (for automated expiry job)
CREATE INDEX IF NOT EXISTS idx_demand_expiry_check 
    ON buyer_demands(required_date, status) WHERE status = 'ACTIVE';

-- Add comments for documentation
COMMENT ON TABLE buyer_demands IS 'Buyer demands for commodities. Used for connecting buyers with sellers.';
COMMENT ON COLUMN buyer_demands.demand_code IS 'Unique demand identifier, format: DEM-YYYYMMDD-XXXXX';
COMMENT ON COLUMN buyer_demands.party_id IS 'References parties table - the buyer';
COMMENT ON COLUMN buyer_demands.commodity_id IS 'References commodities table - what is needed';
COMMENT ON COLUMN buyer_demands.required_quantity IS 'How much is needed';
COMMENT ON COLUMN buyer_demands.required_date IS 'Deadline by which commodity should be delivered';
COMMENT ON COLUMN buyer_demands.price_range_min IS 'Minimum price willing to pay per unit';
COMMENT ON COLUMN buyer_demands.price_range_max IS 'Maximum price willing to pay per unit';
COMMENT ON COLUMN buyer_demands.location IS 'Where delivery is needed (city/location)';
COMMENT ON COLUMN buyer_demands.is_urgent IS 'Flag for urgent demands - affects notification priority';
COMMENT ON COLUMN buyer_demands.status IS 'ACTIVE, FULFILLED, CANCELLED, or EXPIRED - demand lifecycle';
COMMENT ON COLUMN buyer_demands.visibility IS 'PRIVATE (default) or PUBLIC (marketplace visible)';
COMMENT ON COLUMN buyer_demands.fulfilled_by IS 'References parties table - which seller fulfilled this demand';
COMMENT ON COLUMN buyer_demands.sellers_notified_count IS 'How many sellers were notified about this demand';
