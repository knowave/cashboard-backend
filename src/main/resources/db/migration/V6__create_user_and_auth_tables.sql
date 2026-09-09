CREATE TABLE users (
    id UUID PRIMARY KEY,
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    nickname VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(255),
    timezone VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uq_users_provider_provider_id
        UNIQUE (provider, provider_id),

    CONSTRAINT uq_users_provider_email
        UNIQUE (provider, email),

    CONSTRAINT uq_users_nickname
        UNIQUE (nickname)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,   -- SHA-256 hex는 정확히 64자다. 엔티티의 @Column(length = 64)와 일치시킨다.
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    -- ON DELETE CASCADE: refresh token은 User에서 파생된 자격 증명이고 독립적인 가치가 없다.
    -- 탈퇴 시 별도로 지우게 두면(RESTRICT) 그 삭제를 잊는 순간 탈퇴가 FK 위반으로 막힌다.
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_refresh_tokens_token_hash
        UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
