-- Create party_capabilities table for horizontal capabilities
CREATE TABLE IF NOT EXISTS party_capabilities (
    id BIGSERIAL PRIMARY KEY,
    party_id BIGINT NOT NULL,
    capability_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (party_id) REFERENCES parties(id) ON DELETE CASCADE,
    UNIQUE (party_id, capability_type),
    CHECK (capability_type IN ('BUYER', 'SELLER', 'MANUFACTURER', 'TRADER')),
    CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- Create indexes
CREATE INDEX idx_party_cap_party_id ON party_capabilities(party_id);
CREATE INDEX idx_party_cap_type ON party_capabilities(capability_type);
CREATE INDEX idx_party_cap_status ON party_capabilities(status);
