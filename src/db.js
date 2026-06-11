import Database from 'better-sqlite3'
import { readFileSync, readdirSync } from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'
import config from './config.js'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const MIGRATIONS_DIR = path.join(__dirname, '..', 'migrations')

const db = new Database(config.dbPath)

db.pragma('journal_mode = WAL')
db.pragma('foreign_keys = ON')

// Run all numbered migration .sql files in order, skipping already-applied ones
db.exec(`
  CREATE TABLE IF NOT EXISTS _migrations (
    name TEXT PRIMARY KEY,
    applied_at INTEGER NOT NULL DEFAULT (unixepoch() * 1000)
  )
`)

const applied = new Set(
  db.prepare('SELECT name FROM _migrations').all().map(r => r.name)
)

const insert = db.prepare('INSERT INTO _migrations (name) VALUES (?)')

const files = readdirSync(MIGRATIONS_DIR)
  .filter(f => f.endsWith('.sql'))
  .sort()

for (const file of files) {
  if (applied.has(file)) continue
  const sql = readFileSync(path.join(MIGRATIONS_DIR, file), 'utf8')
  db.exec(sql)
  insert.run(file)
  console.log(`Migration applied: ${file}`)
}

export default db
