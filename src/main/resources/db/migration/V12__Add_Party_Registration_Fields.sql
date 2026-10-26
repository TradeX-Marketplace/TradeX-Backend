-- V12: Add Party Registration Fields
-- Purpose: Add registration workflow fields to parties table
-- Date: 2026-09-26

ALTER TABLE parties ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'PENDING_APPROVAL'
    CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'REJECTED', 'CHANGES_REQUESTED', 'SUSPENDED', 'INACTIVE', 'DELETED'));

ALTER TABLE parties ADD COLUMN IF NOT EXISTS registration_type VARCHAR(50) DEFAULT 'SELF_REGISTERED'
    CHECK (registration_type IN ('SELF_REGISTERED', 'ADMIN_CREATED'));

ALTER TABLE parties ADD COLUMN IF NOT EXISTS can_login BOOLEAN DEFAULT FALSE;

ALTER TABLE parties ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP NULL;

-- Ensure approved_by is BIGINT (converting from VARCHAR if created by previous migration)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'parties' AND column_name = 'approved_by') THEN
        ALTER TABLE parties ALTER COLUMN approved_by TYPE BIGINT USING (NULLIF(approved_by, '')::BIGINT);
    ELSE
        ALTER TABLE parties ADD COLUMN approved_by BIGINT NULL;
    END IF;
END $$;

ALTER TABLE parties ADD COLUMN IF NOT EXISTS rejection_reason TEXT NULL;

-- Ensure rejected_by is BIGINT
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'parties' AND column_name = 'rejected_by') THEN
        ALTER TABLE parties ALTER COLUMN rejected_by TYPE BIGINT USING (NULLIF(rejected_by, '')::BIGINT);
    ELSE
        ALTER TABLE parties ADD COLUMN rejected_by BIGINT NULL;
    END IF;
END $$;

ALTER TABLE parties ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP NULL;

ALTER TABLE parties ADD COLUMN IF NOT EXISTS changes_required TEXT NULL;

ALTER TABLE parties ADD COLUMN IF NOT EXISTS changes_requested_by BIGINT NULL;

ALTER TABLE parties ADD COLUMN IF NOT EXISTS changes_requested_at TIMESTAMP NULL;

ALTER TABLE parties ADD COLUMN IF NOT EXISTS requires_approval BOOLEAN DEFAULT FALSE;

-- Create indexes for efficient queries
CREATE INDEX IF NOT EXISTS idx_party_status ON parties(status);
CREATE INDEX IF NOT EXISTS idx_party_registration_type ON parties(registration_type);
CREATE INDEX IF NOT EXISTS idx_party_can_login ON parties(can_login);
CREATE INDEX IF NOT EXISTS idx_party_approved_by ON parties(approved_by);

-- Add foreign key constraints safely
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_party_approved_by_admin') THEN
        ALTER TABLE parties
            ADD CONSTRAINT fk_party_approved_by_admin
            FOREIGN KEY (approved_by)
            REFERENCES admins(id)
            ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_party_rejected_by_admin') THEN
        ALTER TABLE parties
            ADD CONSTRAINT fk_party_rejected_by_admin
            FOREIGN KEY (rejected_by)
            REFERENCES admins(id)
            ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_party_changes_requested_by_admin') THEN
        ALTER TABLE parties
            ADD CONSTRAINT fk_party_changes_requested_by_admin
            FOREIGN KEY (changes_requested_by)
            REFERENCES admins(id)
            ON DELETE SET NULL;
    END IF;
END $$;

-- Update existing parties to ACTIVE status (for Phase 1 data)
UPDATE parties SET status = 'ACTIVE', can_login = TRUE
WHERE status IS NULL OR status = 'PENDING_APPROVAL';
