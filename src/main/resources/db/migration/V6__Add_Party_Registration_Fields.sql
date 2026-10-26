-- Alter parties table to add registration flow fields
ALTER TABLE parties ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE parties ADD COLUMN registration_type VARCHAR(50) NOT NULL DEFAULT 'ADMIN_CREATED';
ALTER TABLE parties ADD COLUMN approval_reason TEXT;
ALTER TABLE parties ADD COLUMN rejected_reason TEXT;
ALTER TABLE parties ADD COLUMN changes_requested_reason TEXT;
ALTER TABLE parties ADD COLUMN approved_by VARCHAR(100);
ALTER TABLE parties ADD COLUMN approved_at TIMESTAMP;
ALTER TABLE parties ADD COLUMN rejected_by VARCHAR(100);
ALTER TABLE parties ADD COLUMN rejected_at TIMESTAMP;

-- Create index for status
CREATE INDEX idx_party_status ON parties(status);
CREATE INDEX idx_party_registration_type ON parties(registration_type);

-- Add check constraint for valid statuses
ALTER TABLE parties ADD CONSTRAINT chk_party_status 
    CHECK (status IN ('PENDING_APPROVAL', 'CHANGES_REQUESTED', 'APPROVED', 'REJECTED', 'ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED'));

-- Add check constraint for registration types
ALTER TABLE parties ADD CONSTRAINT chk_registration_type 
    CHECK (registration_type IN ('SELF_REGISTERED', 'ADMIN_CREATED'));
