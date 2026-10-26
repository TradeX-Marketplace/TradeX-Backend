-- Create Commodities Table
CREATE TABLE IF NOT EXISTS commodities (
    id BIGSERIAL PRIMARY KEY,
    commodity_code VARCHAR(50) NOT NULL UNIQUE,
    commodity_name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    sub_category VARCHAR(100),
    default_unit VARCHAR(20) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for commodities
CREATE INDEX IF NOT EXISTS idx_commodities_commodity_code ON commodities(commodity_code);
CREATE INDEX IF NOT EXISTS idx_commodities_commodity_name ON commodities(commodity_name);
CREATE INDEX IF NOT EXISTS idx_commodities_category ON commodities(category);
CREATE INDEX IF NOT EXISTS idx_commodities_status ON commodities(status);
