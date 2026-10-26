-- Create audit_logs table for comprehensive audit trail
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    party_id BIGINT,
    action_type VARCHAR(100) NOT NULL,
    action_description TEXT NOT NULL,
    actor_id VARCHAR(100),
    actor_type VARCHAR(50),
    old_values TEXT,
    new_values TEXT,
    ip_address VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (party_id) REFERENCES parties(id) ON DELETE SET NULL,
    CHECK (action_type IN (
        'PARTY_REGISTERED',
        'PARTY_CREATED_BY_ADMIN',
        'PARTY_APPROVED',
        'PARTY_REJECTED',
        'PARTY_CHANGES_REQUESTED',
        'PARTY_UPDATED',
        'PARTY_DEACTIVATED',
        'PARTY_ACTIVATED',
        'PARTY_RESUBMITTED',
        'CAPABILITY_ADDED',
        'CAPABILITY_REMOVED',
        'CAPABILITY_UPDATED',
        'COMMODITY_MAPPING_CREATED',
        'COMMODITY_MAPPING_UPDATED',
        'COMMODITY_MAPPING_DELETED',
        'COMMODITY_MAPPING_BULK_UPDATE',
        'LOGIN_SUCCESS',
        'LOGIN_FAILED',
        'LOGOUT',
        'PASSWORD_CHANGED',
        'PASSWORD_RESET',
        'OTP_GENERATED',
        'OTP_VERIFIED',
        'EMAIL_VERIFIED',
        'PHONE_VERIFIED',
        'ADMIN_APPROVAL_DECISION',
        'ADMIN_REJECTION_DECISION',
        'ADMIN_CHANGE_REQUEST',
        'COMMODITY_ADDED',
        'COMMODITY_UPDATED',
        'COMMODITY_DELETED',
        'SYSTEM_NOTIFICATION_SENT',
        'SYSTEM_ERROR',
        'DATA_EXPORT',
        'BULK_OPERATION'
    )),
    CHECK (actor_type IN ('ADMIN', 'PARTY', 'SYSTEM')),
    CHECK (status IN ('SUCCESS', 'FAILURE'))
);

-- Create indexes for audit logs
CREATE INDEX idx_audit_party_id ON audit_logs(party_id);
CREATE INDEX idx_audit_action_type ON audit_logs(action_type);
CREATE INDEX idx_audit_actor_id ON audit_logs(actor_id);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_status ON audit_logs(status);

-- Combined index for common queries
CREATE INDEX idx_audit_party_action ON audit_logs(party_id, action_type, created_at DESC);
