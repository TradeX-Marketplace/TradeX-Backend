-- V21: Create Contracts Table for Digital Legal Documents
-- Purpose: Store automated legal documents for completed deals with JSONB terms support

CREATE TABLE contracts (
    id BIGSERIAL PRIMARY KEY,
    deal_id BIGINT NOT NULL,
    
    -- Party Details (Captured at contract issuance time)
    seller_name VARCHAR(255) NOT NULL,
    seller_gstin VARCHAR(15) NOT NULL,
    seller_address VARCHAR(500),
    
    buyer_name VARCHAR(255) NOT NULL,
    buyer_gstin VARCHAR(15) NOT NULL,
    buyer_address VARCHAR(500),
    
    -- Contract Details
    contract_number VARCHAR(50) UNIQUE,
    
    -- Deal Summary
    commodity_name VARCHAR(255) NOT NULL,
    quantity NUMERIC(15, 4) NOT NULL,
    rate NUMERIC(12, 4) NOT NULL,
    total_amount NUMERIC(18, 4) GENERATED ALWAYS AS (quantity * rate) STORED,
    
    -- Terms and Conditions (JSONB)
    terms_conditions JSONB,
    
    -- Dispatch Details
    dispatch_deadline TIMESTAMP,
    delivery_zone VARCHAR(50),
    
    -- Status
    status VARCHAR(20) DEFAULT 'ISSUED' CHECK (status IN ('ISSUED', 'SIGNED', 'COMPLETED', 'CANCELLED')),
    
    -- Digital Signatures
    seller_signed_at TIMESTAMP,
    buyer_signed_at TIMESTAMP,
    
    -- Audit
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (deal_id) REFERENCES deals(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES parties(id) ON DELETE RESTRICT
);

-- Index for finding contracts by deal
CREATE INDEX idx_contracts_deal ON contracts(deal_id);

-- Index for finding unsigned contracts (for party dashboards)
CREATE INDEX idx_contracts_unsigned ON contracts(status) WHERE status IN ('ISSUED', 'SIGNED');

-- Index for finding completed contracts
CREATE INDEX idx_contracts_completed ON contracts(issued_at DESC) WHERE status = 'COMPLETED';
