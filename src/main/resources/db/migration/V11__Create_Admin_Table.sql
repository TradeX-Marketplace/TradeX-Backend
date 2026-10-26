-- Create admins table for admin user management
CREATE TABLE IF NOT EXISTS admins (
    id BIGSERIAL PRIMARY KEY,
    admin_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    last_login TIMESTAMP,
    last_login_ip VARCHAR(50),
    failed_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    force_password_change BOOLEAN DEFAULT false,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED')),
    CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'MODERATOR'))
);

-- Create indexes for admins
CREATE INDEX idx_admin_code ON admins(admin_code);
CREATE INDEX idx_admin_email ON admins(email);
CREATE INDEX idx_admin_status ON admins(status);
CREATE INDEX idx_admin_active ON admins(active);

-- Insert default super admin
-- Email: admin@123gmail.com
-- Password: Admin@123
-- Password hash: bcrypt hash of "Admin@123" = $2a$10$SlVH2/k2xF/LM1L6K5L0BeQ5pW7.QaLqU1VlRuUCm4tFmG8L7O7fC
INSERT INTO admins (
    admin_code, 
    name, 
    email, 
    phone, 
    password_hash, 
    role, 
    status, 
    active, 
    created_at, 
    updated_at
) VALUES (
    'ADMIN-0001',
    'Super Admin',
    'admin@123gmail.com',
    '+91-0000000000',
    '$2a$10$SlVH2/k2xF/LM1L6K5L0BeQ5pW7.QaLqU1VlRuUCm4tFmG8L7O7fC',
    'SUPER_ADMIN',
    'ACTIVE',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;