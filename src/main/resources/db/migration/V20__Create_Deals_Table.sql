-- V20: Create Deals Table for Matched Buyer-Seller Agreements
-- Purpose: Store matched agreements with pessimistic locking support for inventory management

CREATE TABLE deals (
    id BIGSERIAL PRIMARY KEY,
    
    -- Supply and Demand Linkage
    supply_id BIGINT NOT NULL,
    demand_id BIGINT,
    bid_id BIGINT,
    negotiation_id BIGINT,
    
    -- Party Tracking
    buyer_party_id BIGINT NOT NULL,
    seller_party_id BIGINT NOT NULL,
    
    -- Deal Terms
    quantity NUMERIC(15, 4) NOT NULL,
    rate NUMERIC(12, 4) NOT NULL,
    total_value NUMERIC(18, 4) GENERATED ALWAYS AS (quantity * rate) STORED,
    
    -- Pessimistic Locking (Inventory Management)
    lock_status VARCHAR(20) DEFAULT 'UNLOCKED' CHECK (lock_status IN ('UNLOCKED', 'LOCKED')),
    locked_at TIMESTAMP,
    locked_by VARCHAR(100),
    
    -- Status
    status VARCHAR(20) DEFAULT 'MATCHED' CHECK (status IN ('MATCHED', 'LOCKED', 'DISPATCHED', 'COMPLETED', 'CANCELLED')),
    
    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (supply_id) REFERENCES supplies(id) ON DELETE RESTRICT,
    FOREIGN KEY (demand_id) REFERENCES demands(id) ON DELETE SET NULL,
    FOREIGN KEY (bid_id) REFERENCES bids(id) ON DELETE SET NULL,
    FOREIGN KEY (negotiation_id) REFERENCES negotiations(id) ON DELETE SET NULL,
    FOREIGN KEY (buyer_party_id) REFERENCES parties(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_party_id) REFERENCES parties(id) ON DELETE CASCADE
);

-- Index for finding locked deals (for inventory tracking)
CREATE INDEX idx_deals_lock_status ON deals(lock_status, status);

-- Index for finding deals by supply (for pessimistic locking queries)
CREATE INDEX idx_deals_supply_lock ON deals(supply_id, lock_status);

-- Index for finding deals by party
CREATE INDEX idx_deals_buyer ON deals(buyer_party_id, status);
CREATE INDEX idx_deals_seller ON deals(seller_party_id, status);

-- Index for finding deals requiring dispatch
CREATE INDEX idx_deals_status ON deals(status);
