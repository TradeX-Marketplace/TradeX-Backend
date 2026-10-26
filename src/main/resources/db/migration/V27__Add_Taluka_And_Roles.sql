-- V27: Add Taluka and Plant Location to Parties table, and insert CONSUMER and BROKER roles
-- Purpose: Support detailed agricultural mandi locations and broker role

ALTER TABLE parties ADD COLUMN IF NOT EXISTS taluka VARCHAR(100);
ALTER TABLE parties ADD COLUMN IF NOT EXISTS plant_location TEXT;

-- Insert CONSUMER and BROKER roles into roles table
INSERT INTO roles (name, description, active)
VALUES
    ('CONSUMER', 'Consumer/Purchaser desk who buys processed agricultural feeds and commodities', true),
    ('BROKER', 'Broker/Agent who facilitates transactions between buyers and sellers', true)
ON CONFLICT (name) DO NOTHING;
