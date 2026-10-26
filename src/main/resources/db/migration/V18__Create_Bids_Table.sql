-- V18: Create Bids Table for Anonymous Bidding and Counter-Offers
-- Purpose: Store buyer bids on supplies with counter-offer mechanism

CREATE TABLE bids (
    id BIGSERIAL PRIMARY KEY,
    supply_id BIGINT NOT NULL,
    bidder_party_id BIGINT NOT NULL,
    
    -- Bidding Details
    bid_rate NUMERIC(12, 4) NOT NULL,
    bid_quantity NUMERIC(15, 4) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    
    -- Counter-Offer Tracking
    counter_rate NUMERIC(12, 4),
    counter_remarks VARCHAR(500),
    
    -- Status
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COUNTERED', 'ACCEPTED', 'REJECTED', 'MATCHED')),
    bid_type VARCHAR(10) CHECK (bid_type IN ('BUY', 'SELL')),
    
    -- Remarks
    remarks VARCHAR(500),
    
    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (supply_id) REFERENCES supplies(id) ON DELETE CASCADE,
    FOREIGN KEY (bidder_party_id) REFERENCES parties(id) ON DELETE CASCADE
);

-- Index for finding bids on a specific supply (for counter-offers)
CREATE INDEX idx_bids_supply_status ON bids(supply_id, status);

-- Index for finding bids by bidder (for party dashboard)
CREATE INDEX idx_bids_party ON bids(bidder_party_id, created_at DESC);

-- Index for finding highest bid on a supply
CREATE INDEX idx_bids_supply_rate ON bids(supply_id, bid_rate DESC);
