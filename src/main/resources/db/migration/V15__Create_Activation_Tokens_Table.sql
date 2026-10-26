-- V15: Create Activation Tokens Table for Single-Use WhatsApp Deep Links
-- Purpose: Store temporary tokens for one-time WhatsApp activation links
-- Raw token is never stored; only SHA-256 hash is stored.

CREATE TABLE activation_tokens (
                                   id BIGSERIAL PRIMARY KEY,

                                   token_hash VARCHAR(128) NOT NULL UNIQUE,

                                   party_id BIGINT NOT NULL,

                                   phone VARCHAR(20) NOT NULL,

                                   target_redirect VARCHAR(255) DEFAULT '/party',

                                   consumed BOOLEAN NOT NULL DEFAULT FALSE,

                                   consumed_at TIMESTAMP,

                                   expires_at TIMESTAMP NOT NULL,

                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_activation_tokens_party
                                       FOREIGN KEY (party_id)
                                           REFERENCES parties(id)
                                           ON DELETE CASCADE
);

-- Column comments
COMMENT ON COLUMN activation_tokens.token_hash
    IS 'SHA-256 hash of the token (not the raw token)';

COMMENT ON COLUMN activation_tokens.phone
    IS 'WhatsApp phone number';

COMMENT ON COLUMN activation_tokens.target_redirect
    IS 'Where to redirect after token consumption';

COMMENT ON COLUMN activation_tokens.consumed
    IS 'Whether this token has been used';

COMMENT ON COLUMN activation_tokens.consumed_at
    IS 'Timestamp when token was consumed';

COMMENT ON COLUMN activation_tokens.expires_at
    IS 'Token expiry time, typically 1 hour';

-- Index for fast hash lookup during token consumption
CREATE INDEX idx_activation_tokens_hash
    ON activation_tokens(token_hash);

-- Index for finding unconsumed tokens by party
CREATE INDEX idx_activation_tokens_party_unconsumed
    ON activation_tokens(party_id, consumed);

-- Index for cleanup of expired tokens
CREATE INDEX idx_activation_tokens_expires_at
    ON activation_tokens(expires_at);