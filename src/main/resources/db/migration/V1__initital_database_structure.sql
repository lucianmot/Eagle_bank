CREATE TABLE users (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    address_line3 VARCHAR(255),
    town VARCHAR(128) NOT NULL,
    county VARCHAR(128) NOT NULL,
    postcode VARCHAR(32) NOT NULL,
    phone_number VARCHAR(32) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_timestamp TIMESTAMP NOT NULL,
    updated_timestamp TIMESTAMP NOT NULL
);

CREATE TABLE accounts (
    account_number VARCHAR(16) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    account_type VARCHAR(32) NOT NULL,
    balance NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(8) NOT NULL,
    sort_code VARCHAR(16) NOT NULL,
    created_timestamp TIMESTAMP NOT NULL,
    updated_timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);

CREATE TYPE transaction_type AS ENUM ('deposit', 'withdrawal');

CREATE TABLE transactions (
    id VARCHAR(64) PRIMARY KEY,
    account_number VARCHAR(16) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    type transaction_type NOT NULL,
    reference VARCHAR(255),
    created_timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_account FOREIGN KEY(account_number) REFERENCES accounts(account_number) ON DELETE CASCADE,
    CONSTRAINT fk_user_transaction FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_transactions_account_number ON transactions(account_number);
