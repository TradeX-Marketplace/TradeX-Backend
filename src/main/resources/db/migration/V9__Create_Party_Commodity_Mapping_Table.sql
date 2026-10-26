-- Create party_commodity_mapping table for many-to-many relationships
CREATE TABLE IF NOT EXISTS party_commodity_mapping (
    id BIGSERIAL PRIMARY KEY,
    party_id BIGINT NOT NULL,
    commodity_id BIGINT NOT NULL,
    capability_type VARCHAR(20) NOT NULL,
    quality_grade_id BIGINT,
    notification_enabled BOOLEAN NOT NULL DEFAULT true,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    FOREIGN KEY (party_id) REFERENCES parties(id) ON DELETE CASCADE,
    FOREIGN KEY (commodity_id) REFERENCES commodities(id) ON DELETE CASCADE,
    FOREIGN KEY (quality_grade_id) REFERENCES quality_grades(id) ON DELETE SET NULL,
    UNIQUE (party_id, commodity_id, capability_type),
    CHECK (capability_type IN ('BUY', 'SELL', 'BOTH')),
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

-- Create indexes for performance
CREATE INDEX idx_pcm_party_id ON party_commodity_mapping(party_id);
CREATE INDEX idx_pcm_commodity_id ON party_commodity_mapping(commodity_id);
CREATE INDEX idx_pcm_capability ON party_commodity_mapping(capability_type);
CREATE INDEX idx_pcm_status ON party_commodity_mapping(status);
CREATE INDEX idx_pcm_notification ON party_commodity_mapping(notification_enabled);
CREATE INDEX idx_pcm_quality_grade ON party_commodity_mapping(quality_grade_id);

-- Create index for finding eligible parties for notifications
CREATE INDEX idx_pcm_notification_eligible ON party_commodity_mapping(commodity_id, capability_type, notification_enabled, status)
    WHERE notification_enabled = true AND status = 'ACTIVE';
