-- V31: Add is_deleted column to parties table for soft delete support
ALTER TABLE parties ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_parties_is_deleted ON parties(is_deleted);
