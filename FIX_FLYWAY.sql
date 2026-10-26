-- Run this SQL directly in PostgreSQL to fix Flyway migration checksum error
-- Connect to the tradex database and run this script

-- Update the flyway_schema_history to remove or fix V11 migration
-- Option 1: Delete and let it re-run
DELETE FROM flyway_schema_history WHERE version = 11;

-- After running this, restart the application and the V11 migration will re-run with correct checksum
-- The AdminInitializer will still run and update the password if needed
