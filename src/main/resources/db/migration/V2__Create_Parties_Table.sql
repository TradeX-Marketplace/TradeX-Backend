-- Create Parties Table
CREATE TABLE IF NOT EXISTS parties (
    id BIGSERIAL PRIMARY KEY,
    party_code VARCHAR(50) NOT NULL UNIQUE,
    company_name VARCHAR(255) NOT NULL,
    gstin VARCHAR(15) UNIQUE,
    pan VARCHAR(10),
    contact_person VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    alternate_phone VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    description TEXT,
    website VARCHAR(255),
    bank_account VARCHAR(50),
    bank_name VARCHAR(255),
    ifsc_code VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for parties
CREATE INDEX IF NOT EXISTS idx_parties_party_code ON parties(party_code);
CREATE INDEX IF NOT EXISTS idx_parties_email ON parties(email);
CREATE INDEX IF NOT EXISTS idx_parties_phone ON parties(phone);
CREATE INDEX IF NOT EXISTS idx_parties_gstin ON parties(gstin);
CREATE INDEX IF NOT EXISTS idx_parties_active ON parties(active);

-- Create Party Roles Junction Table
CREATE TABLE IF NOT EXISTS party_roles (
    party_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (party_id, role_id),
    FOREIGN KEY (party_id) REFERENCES parties(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create index for party_roles
CREATE INDEX IF NOT EXISTS idx_party_roles_party_id ON party_roles(party_id);
CREATE INDEX IF NOT EXISTS idx_party_roles_role_id ON party_roles(role_id);
