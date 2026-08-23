CREATE TABLE asset_goals (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    target_amount BIGINT NOT NULL,
    target_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE saving_records (
    id UUID PRIMARY KEY,
    target_month VARCHAR(7) NOT NULL UNIQUE,
    amount BIGINT NOT NULL,
    memo VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_asset_goals_target_date
    ON asset_goals(target_date);

CREATE INDEX idx_saving_records_target_month
    ON saving_records(target_month);
