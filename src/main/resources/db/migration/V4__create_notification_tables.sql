CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE,
    read_at TIMESTAMP WITH TIME ZONE,
    deduplication_key VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_notifications_feed ON notifications(created_at DESC, id DESC);
CREATE INDEX idx_notifications_unread ON notifications(read_at) WHERE read_at IS NULL;

CREATE TABLE notification_settings (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY,
    scope_key VARCHAR(50) NOT NULL UNIQUE,
    push_enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE balance_shortage_states (
    id UUID PRIMARY KEY,
    scope_key VARCHAR(50) NOT NULL UNIQUE,
    shortage_date DATE,
    episode BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE notification_policy_markers (
    id UUID PRIMARY KEY,
    policy_key VARCHAR(255) NOT NULL UNIQUE,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
