CREATE DATABASE portfolio_tracker;

CREATE TABLE users(
	user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
	username VARCHAR(50) NOT NULL UNIQUE,
	email VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE portfolios(
	portfolio_id BIGINT PRIMARY KEY AUTO_INCREMENT,
	created_by BIGINT,
	FOREIGN KEY (created_by) REFERENCES users(user_id),
	name VARCHAR(100) NOT NULL
);

CREATE TABLE membership(
	membership_id BIGINT PRIMARY KEY AUTO_INCREMENT,
	user_id BIGINT,
	FOREIGN KEY (user_id) REFERENCES users(user_id),
	portfolio_id BIGINT,
	FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id),
	role ENUM('admin', 'member', 'visitor') NOT NULL,
	UNIQUE(portfolio_id, user_id)
);

CREATE TABLE transactions(
	transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
	portfolio_id BIGINT NOT NULL,
	FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id),
	transaction_type ENUM('buy', 'sell', 'withdrawal', 'deposit') NOT NULL,
	symbol VARCHAR(10) NOT NULL,
	asset_type ENUM('stock', 'crypto', 'cash'),
	quantity DECIMAL(18, 8) NOT NULL,
	price_per_unit BIGINT NOT NULL,
	timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE positions (
    position_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    portfolio_id BIGINT NOT NULL,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id),
    symbol VARCHAR(10) NOT NULL,
    asset_type ENUM('stock', 'crypto', 'cash') NOT NULL,
    total_quantity DECIMAL(18, 8) NOT NULL,
    total_cost BIGINT NOT NULL,
    average_cost_per_unit BIGINT NOT NULL,
    UNIQUE(portfolio_id, symbol)
);

CREATE TABLE price_quotes (
	symbol VARCHAR(10) PRIMARY KEY,
    price BIGINT NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    source VARCHAR(50) NOT NULL
);

CREATE TABLE invites (
    invite_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    portfolio_id BIGINT NOT NULL,
    invite_code VARCHAR(50) UNIQUE NOT NULL,
    created_by BIGINT NOT NULL,
    role ENUM('admin', 'member', 'visitor'),
    max_uses INT NOT NULL,
    current_uses INT DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

