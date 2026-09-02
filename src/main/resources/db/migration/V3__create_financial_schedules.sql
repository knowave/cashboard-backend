CREATE TABLE financial_schedules (
    id UUID PRIMARY KEY,
    schedule_type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    amount BIGINT NOT NULL,
    direction VARCHAR(20) NOT NULL,
    recurrence_type VARCHAR(20) NOT NULL,
    scheduled_date DATE,
    month_of_year SMALLINT,
    day_of_month SMALLINT,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT chk_financial_schedules_amount CHECK (amount > 0),
    CONSTRAINT chk_financial_schedules_period CHECK (
        end_date IS NULL OR start_date IS NULL OR end_date >= start_date
    )
);

CREATE INDEX idx_financial_schedules_scheduled_date
    ON financial_schedules(scheduled_date);

CREATE INDEX idx_financial_schedules_active_period
    ON financial_schedules(start_date, end_date);
