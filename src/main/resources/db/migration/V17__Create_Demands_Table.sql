-- V17: Create Demands Table for Buyer Procurement Inquiries
-- Purpose: Store buyer requests for commodities with fulfillment tracking

CREATE TABLE demands (
    id BIGSERIAL PRIMARY KEY,
    party_id BIGINT NOT NULL,
    commodity_id BIGINT NOT NULL,
    quality_grade_id BIGINT,
    
    -- Quantity and Pricing
    required_quantity NUMERIC(15, 4) NOT NULL,
    target_rate NUMERIC(12, 4) NOT NULL,
    fulfilled_quantity NUMERIC(15, 4) DEFAULT 0,
    
    -- Location and Zone
    destination VARCHAR(255) NOT NULL,
    destination_zone VARCHAR(50),
    
    -- Delivery & Validity
    valid_till TIMESTAMP NOT NULL,
    required_by TIMESTAMP,
    delivery_terms VARCHAR(500),
    
    -- Status
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'PARTIAL', 'FULFILLED', 'CANCELLED')),
    visibility VARCHAR(20) DEFAULT 'PUBLIC' CHECK (visibility IN ('PUBLIC', 'RESTRICTED', 'ARCHIVED')),
    
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

-- Index for finding active demands by commodity (for seller searches)
CREATE INDEX idx_demands_commodity_status ON demands(commodity_id, status);

-- Index for finding demands by buyer
CREATE INDEX idx_demands_party_status ON demands(party_id, status);

-- Index for valid_till searches (expiring demands)
CREATE INDEX idx_demands_valid_till ON demands(valid_till, status);

-- Index for zone-based filtering
CREATE INDEX idx_demands_zone ON demands(destination_zone);
