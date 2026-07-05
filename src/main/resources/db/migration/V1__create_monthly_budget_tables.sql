CREATE TABLE monthly_budgets (
    id UUID PRIMARY KEY,
    target_month VARCHAR(7) NOT NULL UNIQUE,
    monthly_budget BIGINT NOT NULL,
    used_amount BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE budget_expenses (
    id UUID PRIMARY KEY,
    monthly_budget_id UUID NOT NULL,
    amount BIGINT NOT NULL,
    category VARCHAR(50),
    memo VARCHAR(255),
    spent_at DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_budget_expenses_monthly_budget
        FOREIGN KEY (monthly_budget_id)
        REFERENCES monthly_budgets(id)
);

CREATE INDEX idx_monthly_budgets_target_month
    ON monthly_budgets(target_month);

CREATE INDEX idx_budget_expenses_monthly_budget_id
    ON budget_expenses(monthly_budget_id);

CREATE INDEX idx_budget_expenses_spent_at
    ON budget_expenses(spent_at);
