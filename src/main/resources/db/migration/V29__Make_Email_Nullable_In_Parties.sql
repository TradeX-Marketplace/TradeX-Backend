-- V29: Make email nullable in parties table
-- Requirement: Party registration and add-party can be done with mobile number and location; email is optional.

ALTER TABLE parties ALTER COLUMN email DROP NOT NULL;
