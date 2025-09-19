-- AI results table
CREATE TABLE IF NOT EXISTS ai_results (
    id SERIAL PRIMARY KEY,
    workflow_name VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL,
    payload_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Invitations between coach and player
CREATE TABLE IF NOT EXISTS invitations (
    id SERIAL PRIMARY KEY,
    coach_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    player_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_invitations_coach ON invitations(coach_id);
CREATE INDEX IF NOT EXISTS idx_invitations_player ON invitations(player_id);

-- Chat rooms
CREATE TABLE IF NOT EXISTS chat_rooms (
    id SERIAL PRIMARY KEY,
    coach_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    player_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_chat_room_pair ON chat_rooms(coach_id, player_id);

-- Chat messages
CREATE TABLE IF NOT EXISTS chat_messages (
    id SERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_chat_messages_room ON chat_messages(room_id);

