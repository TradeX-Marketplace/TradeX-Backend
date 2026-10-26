-- Create auth_credentials table for authentication
CREATE TABLE IF NOT EXISTS auth_credentials (
    id BIGSERIAL PRIMARY KEY,
    party_id BIGINT NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    auth_status VARCHAR(50) NOT NULL DEFAULT 'INACTIVE',
    login_enabled BOOLEAN DEFAULT false,
    otp VARCHAR(10),
    otp_expiry TIMESTAMP,
    email_verified BOOLEAN DEFAULT false,
    phone_verified BOOLEAN DEFAULT false,
    last_login TIMESTAMP,
    last_login_ip VARCHAR(50),
    failed_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    force_password_change BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (party_id) REFERENCES parties(id) ON DELETE CASCADE,
    CHECK (auth_status IN ('ACTIVE', 'INACTIVE', 'LOCKED', 'PENDING_VERIFICATION', 'SUSPENDED', 'DELETED'))
);

-- Create indexes for auth_credentials
CREATE INDEX idx_auth_email ON auth_credentials(email);
CREATE INDEX idx_auth_phone ON auth_credentials(phone);
CREATE INDEX idx_auth_party_id ON auth_credentials(party_id);
CREATE INDEX idx_auth_status ON auth_credentials(auth_status);

-- Create login_attempts table for security
CREATE TABLE IF NOT EXISTS login_attempts (
    id BIGSERIAL PRIMARY KEY,
    auth_credential_id BIGINT NOT NULL,
    attempt_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent TEXT,
    success BOOLEAN NOT NULL DEFAULT false,
    reason TEXT,
    FOREIGN KEY (auth_credential_id) REFERENCES auth_credentials(id) ON DELETE CASCADE
);

CREATE INDEX idx_login_attempts_auth_id ON login_attempts(auth_credential_id);
CREATE INDEX idx_login_attempts_time ON login_attempts(attempt_time);

-- Create refresh_tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    auth_credential_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auth_credential_id) REFERENCES auth_credentials(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_token_auth_id ON refresh_tokens(auth_credential_id);
