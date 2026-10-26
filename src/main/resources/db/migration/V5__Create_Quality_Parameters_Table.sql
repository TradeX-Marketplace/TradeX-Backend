-- Create Quality Parameters Table
CREATE TABLE IF NOT EXISTS quality_parameters (
    id BIGSERIAL PRIMARY KEY,
    commodity_id BIGINT NOT NULL,
    parameter_name VARCHAR(100) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    data_type VARCHAR(20) NOT NULL CHECK (data_type IN ('NUMBER', 'TEXT')),
    min_value DOUBLE PRECISION,
    max_value DOUBLE PRECISION,
    mandatory BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (commodity_id) REFERENCES commodities(id) ON DELETE CASCADE,
    UNIQUE (commodity_id, parameter_name)
);

-- Create indexes for quality parameters
CREATE INDEX IF NOT EXISTS idx_quality_parameters_commodity_id ON quality_parameters(commodity_id);
CREATE INDEX IF NOT EXISTS idx_quality_parameters_parameter_name ON quality_parameters(parameter_name);
CREATE INDEX IF NOT EXISTS idx_quality_parameters_data_type ON quality_parameters(data_type);
