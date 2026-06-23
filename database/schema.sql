-- ============================================================
-- SmartBank Wallet - Complete PostgreSQL Schema
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- ROLES TABLE
-- ============================================================
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- USERS TABLE
-- ============================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    mobile_number VARCHAR(15) NOT NULL,
    address TEXT,
    password VARCHAR(255) NOT NULL,
    profile_image VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('ACTIVE','INACTIVE','BLOCKED','PENDING')),
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- USER_ROLES (JUNCTION)
-- ============================================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ============================================================
-- WALLETS TABLE
-- ============================================================
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    wallet_id VARCHAR(50) NOT NULL UNIQUE DEFAULT ('WLT-' || UPPER(SUBSTRING(uuid_generate_v4()::text, 1, 8))),
    account_number VARCHAR(16) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    balance DECIMAL(15,2) DEFAULT 0.00 NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','BLOCKED','FROZEN')),
    daily_limit DECIMAL(15,2) DEFAULT 50000.00,
    monthly_limit DECIMAL(15,2) DEFAULT 500000.00,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TRANSACTIONS TABLE
-- ============================================================
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL UNIQUE DEFAULT ('TXN-' || UPPER(SUBSTRING(uuid_generate_v4()::text, 1, 12))),
    sender_wallet_id BIGINT REFERENCES wallets(id),
    receiver_wallet_id BIGINT REFERENCES wallets(id),
    amount DECIMAL(15,2) NOT NULL,
    transaction_type VARCHAR(30) NOT NULL CHECK (transaction_type IN ('CREDIT','DEBIT','TRANSFER','ADD_MONEY','WITHDRAWAL')),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('SUCCESS','FAILED','PENDING','REVERSED')),
    description TEXT,
    remarks TEXT,
    reference_number VARCHAR(100),
    payment_method VARCHAR(50),
    balance_before DECIMAL(15,2),
    balance_after DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- AUDIT_LOGS TABLE
-- ============================================================
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- REFRESH_TOKENS TABLE
-- ============================================================
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- PASSWORD_RESET_TOKENS TABLE
-- ============================================================
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- EMAIL_LOGS TABLE
-- ============================================================
CREATE TABLE email_logs (
    id BIGSERIAL PRIMARY KEY,
    recipient_email VARCHAR(150) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT,
    status VARCHAR(20) DEFAULT 'SENT' CHECK (status IN ('SENT','FAILED','PENDING')),
    error_message TEXT,
    sent_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- NOTIFICATIONS TABLE
-- ============================================================
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) DEFAULT 'INFO' CHECK (type IN ('INFO','SUCCESS','WARNING','ERROR')),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_wallets_user_id ON wallets(user_id);
CREATE INDEX idx_wallets_account_number ON wallets(account_number);
CREATE INDEX idx_transactions_sender ON transactions(sender_wallet_id);
CREATE INDEX idx_transactions_receiver ON transactions(receiver_wallet_id);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- Roles
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'System Administrator'),
('ROLE_USER', 'Regular User');

-- Admin User (password: Admin@123)
INSERT INTO users (first_name, last_name, username, email, mobile_number, address, password, status, email_verified)
VALUES ('Super', 'Admin', 'admin', 'admin@smartbank.com', '9999999999', 'SmartBank HQ, Mumbai',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', TRUE);

-- Sample Users (password: User@123)
INSERT INTO users (first_name, last_name, username, email, mobile_number, address, password, status, email_verified)
VALUES
('Rahul', 'Sharma', 'rahul.sharma', 'rahul@example.com', '9876543210', '12 MG Road, Bangalore', '$2a$10$EqKain5LBbBLczIONiPFdO21TDMg11Qr.NJFWbm6K.bXMaqBqCl0e', 'ACTIVE', TRUE),
('Priya', 'Patel', 'priya.patel', 'priya@example.com', '9876543211', '45 Park Street, Kolkata', '$2a$10$EqKain5LBbBLczIONiPFdO21TDMg11Qr.NJFWbm6K.bXMaqBqCl0e', 'ACTIVE', TRUE),
('Amit', 'Kumar', 'amit.kumar', 'amit@example.com', '9876543212', '78 Connaught Place, Delhi', '$2a$10$EqKain5LBbBLczIONiPFdO21TDMg11Qr.NJFWbm6K.bXMaqBqCl0e', 'ACTIVE', TRUE);

-- Assign roles
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1), (1, 2), (2, 2), (3, 2), (4, 2);

-- Wallets
INSERT INTO wallets (account_number, user_id, balance, status) VALUES
('1000000000000001', 1, 999999.00, 'ACTIVE'),
('1000000000000002', 2, 25000.00, 'ACTIVE'),
('1000000000000003', 3, 15000.00, 'ACTIVE'),
('1000000000000004', 4, 8500.00, 'ACTIVE');

-- Sample Transactions
INSERT INTO transactions (transaction_id, sender_wallet_id, receiver_wallet_id, amount, transaction_type, status, description, payment_method, balance_before, balance_after)
VALUES
('TXN-SAMPLE001', NULL, 2, 5000.00, 'ADD_MONEY', 'SUCCESS', 'Added money via UPI', 'UPI', 20000.00, 25000.00),
('TXN-SAMPLE002', 2, 3, 2000.00, 'TRANSFER', 'SUCCESS', 'Transfer to Priya', NULL, 25000.00, 23000.00),
('TXN-SAMPLE003', NULL, 3, 5000.00, 'ADD_MONEY', 'SUCCESS', 'Added money via Debit Card', 'DEBIT_CARD', 10000.00, 15000.00),
('TXN-SAMPLE004', 3, 4, 1500.00, 'TRANSFER', 'SUCCESS', 'Payment to Amit', NULL, 15000.00, 13500.00);

-- Sample Notifications
INSERT INTO notifications (user_id, title, message, type)
VALUES
(2, 'Welcome to SmartBank!', 'Your account has been successfully created.', 'SUCCESS'),
(2, 'Money Added', 'Rs. 5000 added to your wallet successfully.', 'SUCCESS'),
(3, 'Welcome to SmartBank!', 'Your account has been successfully created.', 'SUCCESS');

-- Auto-update updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_wallets_updated_at BEFORE UPDATE ON wallets FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
