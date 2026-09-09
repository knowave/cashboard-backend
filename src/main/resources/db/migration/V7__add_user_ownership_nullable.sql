ALTER TABLE monthly_budgets ADD COLUMN user_id UUID;
ALTER TABLE budget_expenses ADD COLUMN user_id UUID;
ALTER TABLE asset_goals ADD COLUMN user_id UUID;
ALTER TABLE saving_records ADD COLUMN user_id UUID;
ALTER TABLE financial_schedules ADD COLUMN user_id UUID;
ALTER TABLE accounts ADD COLUMN user_id UUID;
ALTER TABLE loans ADD COLUMN user_id UUID;
ALTER TABLE fixed_expenses ADD COLUMN user_id UUID;
ALTER TABLE notifications ADD COLUMN user_id UUID;
ALTER TABLE notification_settings ADD COLUMN user_id UUID;
ALTER TABLE notification_preferences ADD COLUMN user_id UUID;
ALTER TABLE balance_shortage_states ADD COLUMN user_id UUID;
ALTER TABLE notification_policy_markers ADD COLUMN user_id UUID;

-- 아래 6개만 비유니크 인덱스를 받는다. 나머지 7개(monthly_budgets, saving_records,
-- notification_settings, notifications, notification_policy_markers,
-- notification_preferences, balance_shortage_states)는 V8에서 user_id 선두
-- 복합 UNIQUE를 받으므로 독립 인덱스가 불필요하다.
CREATE INDEX idx_budget_expenses_user_id ON budget_expenses(user_id);
CREATE INDEX idx_asset_goals_user_id ON asset_goals(user_id);
CREATE INDEX idx_financial_schedules_user_id ON financial_schedules(user_id);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_loans_user_id ON loans(user_id);
CREATE INDEX idx_fixed_expenses_user_id ON fixed_expenses(user_id);
