CREATE TABLE IF NOT EXISTS notifications_viewed (
  id           INTEGER PRIMARY KEY CHECK (id = 1),
  viewed_at    INTEGER NOT NULL DEFAULT (unixepoch() * 1000)
);
INSERT OR IGNORE INTO notifications_viewed (id, viewed_at) VALUES (1, unixepoch() * 1000);
