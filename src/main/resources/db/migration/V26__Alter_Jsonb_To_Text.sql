-- V26: Alter JSONB columns to TEXT for seamless Hibernate schema validation
-- Date: 2026-09-28

ALTER TABLE supplies ALTER COLUMN lab_specs TYPE TEXT USING lab_specs::TEXT;
ALTER TABLE contracts ALTER COLUMN terms_conditions TYPE TEXT USING terms_conditions::TEXT;
