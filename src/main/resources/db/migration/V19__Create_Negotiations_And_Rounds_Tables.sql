-- V19: Create Negotiations and NegotiationRounds Tables for Multi-Round Price Negotiation
-- Purpose: Store negotiation threads between buyers and sellers with round-by-round offers

CREATE TABLE negotiations (
    id BIGSERIAL PRIMARY KEY,
    bid_id BIGINT NOT NULL,
    
    -- Party Tracking
    buyer_party_id BIGINT NOT NULL,
    seller_party_id BIGINT NOT NULL,
    
    -- Current Offer Tracking
    current_offer NUMERIC(12, 4),
    
    -- Status
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'AGREED', 'REJECTED', 'EXPIRED')),
    
    -- Validity
    expires_at TIMESTAMP,
    
    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (bid_id) REFERENCES bids(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_party_id) REFERENCES parties(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_party_id) REFERENCES parties(id) ON DELETE CASCADE
);

-- Index for finding negotiations involving a party
CREATE INDEX idx_negotiations_buyer_status ON negotiations(buyer_party_id, status);
CREATE INDEX idx_negotiations_seller_status ON negotiations(seller_party_id, status);

-- Index for finding ongoing negotiations
CREATE INDEX idx_negotiations_expires_at ON negotiations(expires_at, status);


CREATE TABLE negotiation_rounds (
    id BIGSERIAL PRIMARY KEY,
    negotiation_id BIGINT NOT NULL,
    
    -- Round Details
    round_number INT NOT NULL,
    offered_by VARCHAR(20) NOT NULL,
    rate NUMERIC(12, 4) NOT NULL,
    
    -- Comments
    remarks VARCHAR(500),
    
    -- Timestamp
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (negotiation_id) REFERENCES negotiations(id) ON DELETE CASCADE
);

-- Index for retrieving all rounds of a negotiation in chronological order
CREATE INDEX idx_negotiation_rounds_order ON negotiation_rounds(negotiation_id, round_number ASC);
