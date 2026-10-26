-- V16: Create Supplies Table for Seller Commodity Lots
-- Purpose: Store inventory of commodities available for sale with quantity tracking and locking support

CREATE TABLE supplies (
    id BIGSERIAL PRIMARY KEY,
    party_id BIGINT NOT NULL,
    commodity_id BIGINT NOT NULL,
    quality_grade_id BIGINT,
    
    -- Quantity tracking (for pessimistic locking)
    total_quantity NUMERIC(15, 4) NOT NULL,
    available_quantity NUMERIC(15, 4) NOT NULL,
    locked_quantity NUMERIC(15, 4) DEFAULT 0,
    
    -- Pricing
    asking_rate NUMERIC(12, 4) NOT NULL,
    
    -- Location and Zone
    location VARCHAR(255) NOT NULL,
    origin_zone VARCHAR(50),
    
    -- Delivery & Validity
    valid_till TIMESTAMP NOT NULL,
    delivery_terms VARCHAR(500),
    
    -- Lab Specifications
    lab_specs JSONB,
    
    -- Status
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'PARTIAL', 'SOLD_OUT', 'CANCELLED')),
    
    -- Audit
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (party_id) REFERENCES parties(id) ON DELETE CASCADE,
    FOREIGN KEY (commodity_id) REFERENCES commodities(id) ON DELETE RESTRICT,
    FOREIGN KEY (quality_grade_id) REFERENCES quality_grades(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES parties(id) ON DELETE RESTRICT
);

-- Index for finding active supplies by commodity (for buyer searches)
CREATE INDEX idx_supplies_commodity_status ON supplies(commodity_id, status);

-- Index for finding supplies by seller
CREATE INDEX idx_supplies_party_status ON supplies(party_id, status);

-- Index for valid_till searches (expiring supplies)
CREATE INDEX idx_supplies_valid_till ON supplies(valid_till, status);

-- Index for zone-based filtering
CREATE INDEX idx_supplies_zone ON supplies(origin_zone);
