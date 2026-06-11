CREATE TABLE IF NOT EXISTS cohort_definition (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  name        TEXT    NOT NULL,
  description TEXT,
  created_by  TEXT    NOT NULL DEFAULT 'anonymous',
  modified_by TEXT,
  created_date  INTEGER NOT NULL DEFAULT (unixepoch() * 1000),
  modified_date INTEGER
);

CREATE TABLE IF NOT EXISTS cohort_definition_details (
  id         INTEGER PRIMARY KEY REFERENCES cohort_definition(id) ON DELETE CASCADE,
  expression TEXT,
  hash_code  TEXT
);

CREATE TABLE IF NOT EXISTS cohort_generation_info (
  cohort_definition_id INTEGER NOT NULL,
  source_key           TEXT    NOT NULL,
  status               TEXT    NOT NULL DEFAULT 'PENDING',
  start_time           INTEGER,
  execution_duration   INTEGER,
  is_valid             INTEGER NOT NULL DEFAULT 0,
  is_canceled          INTEGER NOT NULL DEFAULT 0,
  fail_message         TEXT,
  person_count         INTEGER,
  record_count         INTEGER,
  created_by           TEXT,
  PRIMARY KEY (cohort_definition_id, source_key),
  FOREIGN KEY (cohort_definition_id) REFERENCES cohort_definition(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS concept_set (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  name         TEXT    NOT NULL,
  description  TEXT,
  created_by   TEXT    NOT NULL DEFAULT 'anonymous',
  modified_by  TEXT,
  created_date  INTEGER NOT NULL DEFAULT (unixepoch() * 1000),
  modified_date INTEGER
);

CREATE TABLE IF NOT EXISTS concept_set_item (
  id                INTEGER PRIMARY KEY AUTOINCREMENT,
  concept_set_id    INTEGER NOT NULL REFERENCES concept_set(id) ON DELETE CASCADE,
  concept_id        INTEGER NOT NULL,
  is_excluded       INTEGER NOT NULL DEFAULT 0,
  include_descendants INTEGER NOT NULL DEFAULT 0,
  include_mapped    INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS ir_analysis (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  name         TEXT NOT NULL,
  description  TEXT,
  expression   TEXT,
  created_by   TEXT NOT NULL DEFAULT 'anonymous',
  modified_by  TEXT,
  created_date  INTEGER NOT NULL DEFAULT (unixepoch() * 1000),
  modified_date INTEGER
);

CREATE TABLE IF NOT EXISTS cc_analysis (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  name         TEXT NOT NULL,
  description  TEXT,
  expression   TEXT,
  created_by   TEXT NOT NULL DEFAULT 'anonymous',
  modified_by  TEXT,
  created_date  INTEGER NOT NULL DEFAULT (unixepoch() * 1000),
  modified_date INTEGER
);

CREATE TABLE IF NOT EXISTS pathway_analysis (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  name         TEXT NOT NULL,
  description  TEXT,
  expression   TEXT,
  created_by   TEXT NOT NULL DEFAULT 'anonymous',
  modified_by  TEXT,
  created_date  INTEGER NOT NULL DEFAULT (unixepoch() * 1000),
  modified_date INTEGER
);

CREATE TABLE IF NOT EXISTS estimation (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  name         TEXT NOT NULL,
  description  TEXT,
  expression   TEXT,
  created_by   TEXT NOT NULL DEFAULT 'anonymous',
  modified_by  TEXT,
  created_date  INTEGER NOT NULL DEFAULT (unixepoch() * 1000),
  modified_date INTEGER
);

CREATE TABLE IF NOT EXISTS prediction (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  name         TEXT NOT NULL,
  description  TEXT,
  expression   TEXT,
  created_by   TEXT NOT NULL DEFAULT 'anonymous',
  modified_by  TEXT,
  created_date  INTEGER NOT NULL DEFAULT (unixepoch() * 1000),
  modified_date INTEGER
);

CREATE TABLE IF NOT EXISTS job (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  name          TEXT NOT NULL,
  type          TEXT NOT NULL,
  status        TEXT NOT NULL DEFAULT 'STARTED',
  exit_status   TEXT,
  start_time    INTEGER NOT NULL DEFAULT (unixepoch() * 1000),
  end_time      INTEGER,
  params        TEXT,
  fail_message  TEXT
);

CREATE TABLE IF NOT EXISTS tag (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  name         TEXT NOT NULL UNIQUE,
  type         TEXT NOT NULL DEFAULT 'COMMON',
  created_by   TEXT NOT NULL DEFAULT 'anonymous',
  created_date INTEGER NOT NULL DEFAULT (unixepoch() * 1000)
);

CREATE TABLE IF NOT EXISTS entity_tag (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  tag_id      INTEGER NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
  entity_type TEXT NOT NULL,
  entity_id   INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS version (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  entity_type  TEXT NOT NULL,
  entity_id    INTEGER NOT NULL,
  version      INTEGER NOT NULL,
  expression   TEXT,
  description  TEXT,
  is_archived  INTEGER NOT NULL DEFAULT 0,
  created_by   TEXT NOT NULL DEFAULT 'anonymous',
  created_date INTEGER NOT NULL DEFAULT (unixepoch() * 1000)
);
