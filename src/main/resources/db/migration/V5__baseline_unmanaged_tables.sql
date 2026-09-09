CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    balance BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS loans (
    id UUID PRIMARY KEY,
    principal BIGINT NOT NULL,
    annual_interest_rate NUMERIC(6, 3) NOT NULL,
    monthly_payment BIGINT NOT NULL,
    current_balance BIGINT NOT NULL,
    start_month VARCHAR(7) NOT NULL,
    maturity_month VARCHAR(7) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS fixed_expenses (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    amount BIGINT NOT NULL,
    category VARCHAR(100) NOT NULL,
    start_month VARCHAR(7) NOT NULL,
    end_month VARCHAR(7),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
