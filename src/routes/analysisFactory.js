import { Router } from 'express'
import db from '../db.js'

export function formatDate (ms) {
  if (!ms) return null
  return new Date(ms).toISOString()
}

export function toUserRef (login) {
  if (!login) return null
  return { id: 0, login, name: login }
}

export function rowToShortDto (row) {
  return {
    id: row.id,
    name: row.name,
    description: row.description || null,
    createdBy: toUserRef(row.created_by),
    createdDate: formatDate(row.created_date),
    modifiedBy: toUserRef(row.modified_by),
    modifiedDate: formatDate(row.modified_date),
    tags: [],
    hasWriteAccess: true
  }
}

export function rowToDto (row) {
  let expr = {}
  if (row.expression) {
    try { expr = JSON.parse(row.expression) } catch { /* ignore */ }
  }
  // Spread stored expression first, then override with canonical server-side fields
  return { ...expr, ...rowToShortDto(row) }
}

export function versionToDto (row) {
  return {
    id: row.id,
    entityId: row.entity_id,
    version: row.version,
    description: row.description || null,
    archived: !!row.is_archived,
    createdBy: toUserRef(row.created_by),
    createdDate: formatDate(row.created_date),
    hasWriteAccess: true
  }
}

/**
 * Returns an Express Router with standard analysis CRUD + generation stubs.
 * table     — SQLite table name (ir_analysis, cc_analysis, etc.)
 * configure — optional callback(router) called BEFORE the parameterised routes,
 *             allowing callers to add type-specific static/prefix routes.
 */
export function makeAnalysisRouter (table, configure) {
  const router = Router()

  // --- static routes ---

  router.get('/', (_req, res) => {
    const rows = db.prepare(`SELECT * FROM ${table} ORDER BY id DESC`).all()
    res.json(rows.map(rowToShortDto))
  })

  router.post('/', (req, res) => {
    const { name, description, ...rest } = req.body
    if (!name) return res.status(400).json({ message: 'name is required' })
    const user = req.user?.login || 'anonymous'
    const result = db.prepare(
      `INSERT INTO ${table} (name, description, expression, created_by) VALUES (?, ?, ?, ?)`
    ).run(name, description || null, JSON.stringify(rest), user)
    const row = db.prepare(`SELECT * FROM ${table} WHERE id = ?`).get(result.lastInsertRowid)
    res.status(201).json(rowToDto(row))
  })

  // Allow callers to add static routes before /:id
  if (configure) configure(router)

  // --- global generation routes (literal prefix, before /:id) ---

  // GET /generation/:generationId — stub: generation not tracked
  router.get('/generation/:generationId', (req, res) => {
    res.status(404).json({ message: 'Generation not found' })
  })

  // GET /generation/:generationId/result
  router.get('/generation/:generationId/result', (_req, res) => res.json([]))

  // DELETE /generation/:generationId
  router.delete('/generation/:generationId', (_req, res) => res.sendStatus(200))

  // --- version endpoints (read-only; versions are created on explicit save) ---

  router.get('/:id/version', (req, res) => {
    const rows = db.prepare(
      "SELECT * FROM version WHERE entity_type = ? AND entity_id = ? ORDER BY version DESC"
    ).all(table, req.params.id)
    res.json(rows.map(versionToDto))
  })

  router.get('/:id/version/:ver', (req, res) => {
    const row = db.prepare(
      "SELECT * FROM version WHERE entity_type = ? AND entity_id = ? AND version = ?"
    ).get(table, req.params.id, req.params.ver)
    if (!row) return res.status(404).json({ message: 'Version not found' })
    res.json(versionToDto(row))
  })

  router.put('/:id/version/:ver', (req, res) => {
    const { description, archived } = req.body
    const result = db.prepare(
      "UPDATE version SET description = ?, is_archived = ? WHERE entity_type = ? AND entity_id = ? AND version = ?"
    ).run(description || null, archived ? 1 : 0, table, req.params.id, req.params.ver)
    if (!result.changes) return res.status(404).json({ message: 'Version not found' })
    const row = db.prepare(
      "SELECT * FROM version WHERE entity_type = ? AND entity_id = ? AND version = ?"
    ).get(table, req.params.id, req.params.ver)
    res.json(versionToDto(row))
  })

  router.delete('/:id/version/:ver', (req, res) => {
    const result = db.prepare(
      "DELETE FROM version WHERE entity_type = ? AND entity_id = ? AND version = ?"
    ).run(table, req.params.id, req.params.ver)
    if (!result.changes) return res.status(404).json({ message: 'Version not found' })
    res.sendStatus(200)
  })

  // POST /:id/version — explicit snapshot save
  router.post('/:id/version', (req, res) => {
    const row = db.prepare(`SELECT * FROM ${table} WHERE id = ?`).get(req.params.id)
    if (!row) return res.status(404).json({ message: 'Not found' })
    const user = req.user?.login || 'anonymous'
    const nextVer = ((db.prepare(
      "SELECT MAX(version) AS v FROM version WHERE entity_type = ? AND entity_id = ?"
    ).get(table, req.params.id))?.v ?? 0) + 1
    db.prepare(
      "INSERT INTO version (entity_type, entity_id, version, expression, description, created_by) VALUES (?, ?, ?, ?, ?, ?)"
    ).run(table, row.id, nextVer, row.expression, req.body?.description || null, user)
    const saved = db.prepare(
      "SELECT * FROM version WHERE entity_type = ? AND entity_id = ? AND version = ?"
    ).get(table, row.id, nextVer)
    res.status(201).json(versionToDto(saved))
  })

  // --- parameterised routes ---

  router.get('/:id/exists', (req, res) => {
    const { name } = req.query
    if (!name) return res.json(false)
    const existing = db.prepare(
      `SELECT id FROM ${table} WHERE name = ? AND id != ?`
    ).get(name, req.params.id)
    res.json(!!existing)
  })

  router.get('/:id/copy', (req, res) => {
    const original = db.prepare(`SELECT * FROM ${table} WHERE id = ?`).get(req.params.id)
    if (!original) return res.status(404).json({ message: 'Not found' })
    const user = req.user?.login || 'anonymous'
    const result = db.prepare(
      `INSERT INTO ${table} (name, description, expression, created_by) VALUES (?, ?, ?, ?)`
    ).run(`Copy of ${original.name}`, original.description, original.expression, user)
    const row = db.prepare(`SELECT * FROM ${table} WHERE id = ?`).get(result.lastInsertRowid)
    res.status(201).json(rowToDto(row))
  })

  // generation stubs per-entity
  router.post('/:id/generation/:sourceKey', (_req, res) => res.sendStatus(501))
  router.delete('/:id/generation/:sourceKey', (_req, res) => res.sendStatus(200))
  router.get('/:id/generation', (_req, res) => res.json([]))
  router.get('/:id/info', (_req, res) => res.json([]))

  router.get('/:id', (req, res) => {
    const row = db.prepare(`SELECT * FROM ${table} WHERE id = ?`).get(req.params.id)
    if (!row) return res.status(404).json({ message: 'Not found' })
    res.json(rowToDto(row))
  })

  router.put('/:id', (req, res) => {
    const { name, description, ...rest } = req.body
    const user = req.user?.login || 'anonymous'
    const result = db.prepare(
      `UPDATE ${table} SET name = ?, description = ?, expression = ?, modified_by = ?, modified_date = ? WHERE id = ?`
    ).run(name || null, description || null, JSON.stringify(rest), user, Date.now(), req.params.id)
    if (!result.changes) return res.status(404).json({ message: 'Not found' })
    const row = db.prepare(`SELECT * FROM ${table} WHERE id = ?`).get(req.params.id)
    res.json(rowToDto(row))
  })

  router.delete('/:id', (req, res) => {
    const result = db.prepare(`DELETE FROM ${table} WHERE id = ?`).run(req.params.id)
    if (!result.changes) return res.status(404).json({ message: 'Not found' })
    res.sendStatus(200)
  })

  return router
}
