-- V22: Create Market Activities Table for Market Rate Updates and Activity Logging
-- Purpose: Store market rate updates, trading activities, and analytics data

CREATE TABLE market_activities (
    id BIGSERIAL PRIMARY KEY,
    
    -- Activity Type
    activity_type VARCHAR(50) NOT NULL,
    
    -- Related Entities
    commodity_id BIGINT,
    supply_id BIGINT,
    demand_id BIGINT,
    bid_id BIGINT,
    deal_id BIGINT,
    contract_id BIGINT,
    
    -- Parties Involved
    initiator_party_id BIGINT NOT NULL,
    counterparty_id BIGINT,
    
    -- Activity Details
    details JSONB,
    
    -- Market Rate Info (for analytics)
    rate NUMERIC(12, 4),
    quantity NUMERIC(15, 4),
    
    -- Timestamp
    activity_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (commodity_id) REFERENCES commodities(id) ON DELETE SET NULL,
    FOREIGN KEY (supply_id) REFERENCES supplies(id) ON DELETE SET NULL,
    FOREIGN KEY (demand_id) REFERENCES demands(id) ON DELETE SET NULL,
    FOREIGN KEY (bid_id) REFERENCES bids(id) ON DELETE SET NULL,
    FOREIGN KEY (deal_id) REFERENCES deals(id) ON DELETE SET NULL,
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE SET NULL,
    FOREIGN KEY (initiator_party_id) REFERENCES parties(id) ON DELETE CASCADE,
    FOREIGN KEY (counterparty_id) REFERENCES parties(id) ON DELETE SET NULL
);

-- Index for recent activities (for dashboards and feeds)
CREATE INDEX idx_market_activities_time ON market_activities(activity_time DESC);

-- Index for activities by commodity (for market insights)
CREATE INDEX idx_market_activities_commodity ON market_activities(commodity_id, activity_time DESC);

-- Index for activities by party (for audit trail)
CREATE INDEX idx_market_activities_party ON market_activities(initiator_party_id, activity_time DESC);

-- Index for specific activity types (for analytics)
CREATE INDEX idx_market_activities_type ON market_activities(activity_type, activity_time DESC);

-- Index for rate tracking by commodity
CREATE INDEX idx_market_activities_commodity_rate ON market_activities(commodity_id, rate) WHERE activity_type IN ('BID_PLACED', 'DEAL_MATCHED');
