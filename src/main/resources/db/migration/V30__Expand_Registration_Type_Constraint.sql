-- V30: Expand chk_registration_type constraint in parties table
-- Support SELF_SIGNUP, INVITED, SELF_REGISTERED, ADMIN_CREATED

ALTER TABLE parties DROP CONSTRAINT IF EXISTS chk_registration_type;

ALTER TABLE parties ADD CONSTRAINT chk_registration_type
    CHECK (registration_type IN ('SELF_REGISTERED', 'ADMIN_CREATED', 'SELF_SIGNUP', 'INVITED'));
