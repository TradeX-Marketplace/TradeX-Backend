ALTER TABLE bids
    ADD COLUMN bid_number VARCHAR(50);

UPDATE bids
SET bid_number = 'BID-' || id
WHERE bid_number IS NULL;

ALTER TABLE bids
    ALTER COLUMN bid_number SET NOT NULL;

CREATE UNIQUE INDEX idx_bids_bid_number
    ON bids(bid_number);