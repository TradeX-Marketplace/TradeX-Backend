-- V13: Create Party Commodity Mapping Table
-- Purpose: Many-to-many relationship between parties and commodities
-- Date: 2026-09-26

CREATE TABLE IF NOT EXISTS party_commodity_mapping (
    id BIGSERIAL PRIMARY KEY,
    party_id BIGINT NOT NULL,
    commodity_id BIGINT NOT NULL,
    capability_type VARCHAR(20) NOT NULL CHECK (capability_type IN ('BUY', 'SELL', 'BOTH')),
    quality_grade_id BIGINT,
    notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED', 'DELETED')),
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    
    -- Foreign Keys
    CONSTRAINT fk_pcm_party FOREIGN KEY (party_id) REFERENCES parties(id) ON DELETE CASCADE,
    CONSTRAINT fk_pcm_commodity FOREIGN KEY (commodity_id) REFERENCES commodities(id) ON DELETE CASCADE,
    CONSTRAINT fk_pcm_quality_grade FOREIGN KEY (quality_grade_id) REFERENCES quality_grades(id) ON DELETE SET NULL,
    CONSTRAINT fk_pcm_created_by FOREIGN KEY (created_by) REFERENCES admins(id) ON DELETE SET NULL,
    CONSTRAINT fk_pcm_updated_by FOREIGN KEY (updated_by) REFERENCES admins(id) ON DELETE SET NULL,
    
    -- Unique constraint: no duplicate (party, commodity, capability) combinations
    CONSTRAINT uq_party_commodity_capability UNIQUE (party_id, commodity_id, capability_type)
);

-- Create indexes for optimal query performance
CREATE INDEX IF NOT EXISTS idx_pcm_party_id ON party_commodity_mapping(party_id);
CREATE INDEX IF NOT EXISTS idx_pcm_commodity_id ON party_commodity_mapping(commodity_id);
CREATE INDEX IF NOT EXISTS idx_pcm_status ON party_commodity_mapping(status);
CREATE INDEX IF NOT EXISTS idx_pcm_capability ON party_commodity_mapping(capability_type);
CREATE INDEX IF NOT EXISTS idx_pcm_notification ON party_commodity_mapping(notification_enabled);

-- CRITICAL INDEX: Used for demand matching - find sellers for a commodity
-- When buyer creates demand, system queries: 
-- WHERE commodity_id = X AND capability_type IN ('SELL', 'BOTH') 
--       AND status = 'ACTIVE' AND notification_enabled = true
CREATE INDEX IF NOT EXISTS idx_pcm_commodity_capability 
    ON party_commodity_mapping(commodity_id, capability_type, status, notification_enabled);

-- Index for finding active mappings by party
CREATE INDEX IF NOT EXISTS idx_pcm_party_status 
    ON party_commodity_mapping(party_id, status);

-- Index for audit queries
CREATE INDEX IF NOT EXISTS idx_pcm_created_at 
    ON party_commodity_mapping(created_at);

-- Add comments for documentation
COMMENT ON TABLE party_commodity_mapping IS 'Many-to-many relationship between parties and commodities. Represents what commodities a party can buy/sell.';
COMMENT ON COLUMN party_commodity_mapping.party_id IS 'References parties table - the party';
COMMENT ON COLUMN party_commodity_mapping.commodity_id IS 'References commodities table - the commodity';
COMMENT ON COLUMN party_commodity_mapping.capability_type IS 'BUY, SELL, or BOTH - what the party can do with this commodity';
COMMENT ON COLUMN party_commodity_mapping.notification_enabled IS 'If true, party receives notifications about this commodity';
COMMENT ON COLUMN party_commodity_mapping.status IS 'ACTIVE, INACTIVE, ARCHIVED, or DELETED - lifecycle status';
COMMENT ON COLUMN party_commodity_mapping.created_by IS 'Admin who created this mapping (if any)';
