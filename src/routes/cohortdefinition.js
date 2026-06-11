import { Router } from 'express'
import db from '../db.js'

const router = Router()

// --- helpers ---

function formatDate (ms) {
  if (!ms) return null
  return new Date(ms).toISOString()
}

function toUserRef (login) {
  if (!login) return null
  return { id: 0, login, name: login }
}

function rowToDto (row, includeExpression = false) {
  const dto = {
    id: row.id,
    name: row.name,
    description: row.description || null,
    expressionType: row.expression_type || 'SIMPLE_EXPRESSION',
    createdBy: toUserRef(row.created_by),
    createdDate: formatDate(row.created_date),
    modifiedBy: toUserRef(row.modified_by),
    modifiedDate: formatDate(row.modified_date),
    tags: [],
    hasWriteAccess: true
  }

  if (includeExpression) {
    const detail = db.prepare('SELECT expression FROM cohort_definition_details WHERE id = ?').get(row.id)
    dto.expression = detail && detail.expression ? JSON.parse(detail.expression) : null
  }

  return dto
}

// --- static routes (before /:id) ---

// POST /sql → 501 (CIRCE dependency)
router.post('/sql', (_req, res) => res.sendStatus(501))

// POST /check → return empty warnings (CIRCE validation not implemented)
router.post('/check', (_req, res) => res.json([]))
router.post('/checkV2', (_req, res) => res.json([]))

// POST /printfriendly/cohort → 501
router.post('/printfriendly/cohort', (_req, res) => res.sendStatus(501))
router.post('/printfriendly/conceptsets', (_req, res) => res.sendStatus(501))

// GET /
router.get('/', (_req, res) => {
  const rows = db.prepare('SELECT * FROM cohort_definition ORDER BY id DESC').all()
  res.json(rows.map(r => rowToDto(r, false)))
})

// POST /
router.post('/', (req, res) => {
  const { name, description, expression, expressionType } = req.body || {}
  if (!name) return res.status(400).json({ message: 'name is required' })
  const login = req.user ? req.user.login : 'anonymous'

  const insertDef = db.transaction(() => {
    const result = db.prepare(
      `INSERT INTO cohort_definition (name, description, expression_type, created_by) VALUES (?, ?, ?, ?)`
    ).run(name, description || null, expressionType || 'SIMPLE_EXPRESSION', login)
    const id = result.lastInsertRowid
    db.prepare(
      `INSERT INTO cohort_definition_details (id, expression) VALUES (?, ?)`
    ).run(id, expression ? JSON.stringify(expression) : null)
    return id
  })

  const id = insertDef()
  const row = db.prepare('SELECT * FROM cohort_definition WHERE id = ?').get(id)
  res.status(201).json(rowToDto(row, true))
})

// --- parameterised routes ---

// GET /:id/exists?name=...
router.get('/:id/exists', (req, res) => {
  const name = req.query.name
  const id = parseInt(req.params.id, 10) || 0
  const row = db.prepare('SELECT COUNT(*) AS cnt FROM cohort_definition WHERE name = ? AND id != ?').get(name, id)
  res.json(row.cnt)
})

// GET /:id
router.get('/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM cohort_definition WHERE id = ?').get(req.params.id)
  if (!row) return res.status(404).json({ message: 'Cohort definition not found' })
  res.json(rowToDto(row, true))
})

// PUT /:id
router.put('/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM cohort_definition WHERE id = ?').get(req.params.id)
  if (!row) return res.status(404).json({ message: 'Cohort definition not found' })
  const { name, description, expression, expressionType } = req.body || {}
  const login = req.user ? req.user.login : 'anonymous'

  db.transaction(() => {
    db.prepare(
      `UPDATE cohort_definition SET name = ?, description = ?, expression_type = ?,
       modified_by = ?, modified_date = (unixepoch() * 1000) WHERE id = ?`
    ).run(
      name || row.name,
      description !== undefined ? description : row.description,
      expressionType || row.expression_type || 'SIMPLE_EXPRESSION',
      login,
      row.id
    )
    db.prepare(
      `INSERT INTO cohort_definition_details (id, expression) VALUES (?, ?)
       ON CONFLICT(id) DO UPDATE SET expression = excluded.expression`
    ).run(row.id, expression ? JSON.stringify(expression) : null)
  })()

  const updated = db.prepare('SELECT * FROM cohort_definition WHERE id = ?').get(row.id)
  res.json(rowToDto(updated, true))
})

// DELETE /:id
router.delete('/:id', (req, res) => {
  const row = db.prepare('SELECT id FROM cohort_definition WHERE id = ?').get(req.params.id)
  if (!row) return res.status(404).json({ message: 'Cohort definition not found' })
  db.prepare('DELETE FROM cohort_definition WHERE id = ?').run(row.id)
  res.sendStatus(204)
})

// GET /:id/copy
router.get('/:id/copy', (req, res) => {
  const row = db.prepare('SELECT * FROM cohort_definition WHERE id = ?').get(req.params.id)
  if (!row) return res.status(404).json({ message: 'Cohort definition not found' })
  const detail = db.prepare('SELECT expression FROM cohort_definition_details WHERE id = ?').get(row.id)
  const login = req.user ? req.user.login : 'anonymous'

  const newId = db.transaction(() => {
    const result = db.prepare(
      `INSERT INTO cohort_definition (name, description, expression_type, created_by) VALUES (?, ?, ?, ?)`
    ).run(`Copy of ${row.name}`, row.description, row.expression_type || 'SIMPLE_EXPRESSION', login)
    const id = result.lastInsertRowid
    db.prepare(
      `INSERT INTO cohort_definition_details (id, expression) VALUES (?, ?)`
    ).run(id, detail ? detail.expression : null)
    return id
  })()

  const copied = db.prepare('SELECT * FROM cohort_definition WHERE id = ?').get(newId)
  res.json(rowToDto(copied, true))
})

// GET /:id/info (generation status — empty until generation is implemented)
router.get('/:id/info', (req, res) => {
  const row = db.prepare('SELECT id FROM cohort_definition WHERE id = ?').get(req.params.id)
  if (!row) return res.status(404).json({ message: 'Cohort definition not found' })
  const infos = db.prepare('SELECT * FROM cohort_generation_info WHERE cohort_definition_id = ?').all(row.id)
  res.json(infos.map(i => ({
    id: { cohortDefinitionId: i.cohort_definition_id, sourceId: i.source_key },
    status: i.status,
    startTime: i.start_time,
    executionDuration: i.execution_duration,
    isValid: Boolean(i.is_valid),
    isCanceled: Boolean(i.is_canceled),
    failMessage: i.fail_message || null,
    personCount: i.person_count,
    recordCount: i.record_count
  })))
})

// GET /:id/generate/:sourceKey → 501 (CIRCE dependency)
router.get('/:id/generate/:sourceKey', (_req, res) => res.sendStatus(501))

// GET /:id/cancel/:sourceKey → stub
router.get('/:id/cancel/:sourceKey', (req, res) => {
  res.json({ status: 'CANCELED' })
})

// GET /:id/report/:sourceKey → 501
router.get('/:id/report/:sourceKey', (_req, res) => res.sendStatus(501))

// GET /:id/export/conceptset → stub
router.get('/:id/export/conceptset', (_req, res) => res.sendStatus(501))

// POST /:id/tag/
router.post('/:id/tag/', (req, res) => res.sendStatus(204))

// DELETE /:id/tag/:tagId
router.delete('/:id/tag/:tagId', (_req, res) => res.sendStatus(204))

// --- version endpoints ---

const ENTITY_TYPE = 'cohort_definition'

function versionToDto (row) {
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

router.get('/:id/version', (req, res) => {
  const rows = db.prepare(
    'SELECT * FROM version WHERE entity_type = ? AND entity_id = ? ORDER BY version DESC'
  ).all(ENTITY_TYPE, req.params.id)
  res.json(rows.map(versionToDto))
})

router.post('/:id/version', (req, res) => {
  const row = db.prepare('SELECT * FROM cohort_definition WHERE id = ?').get(req.params.id)
  if (!row) return res.status(404).json({ message: 'Not found' })
  const detail = db.prepare('SELECT expression FROM cohort_definition_details WHERE id = ?').get(row.id)
  const user = req.user?.login || 'anonymous'
  const nextVer = ((db.prepare(
    'SELECT MAX(version) AS v FROM version WHERE entity_type = ? AND entity_id = ?'
  ).get(ENTITY_TYPE, row.id))?.v ?? 0) + 1
  db.prepare(
    'INSERT INTO version (entity_type, entity_id, version, expression, description, created_by) VALUES (?, ?, ?, ?, ?, ?)'
  ).run(ENTITY_TYPE, row.id, nextVer, detail?.expression || null, req.body?.description || null, user)
  const saved = db.prepare(
    'SELECT * FROM version WHERE entity_type = ? AND entity_id = ? AND version = ?'
  ).get(ENTITY_TYPE, row.id, nextVer)
  res.status(201).json(versionToDto(saved))
})

router.get('/:id/version/:ver', (req, res) => {
  const row = db.prepare(
    'SELECT * FROM version WHERE entity_type = ? AND entity_id = ? AND version = ?'
  ).get(ENTITY_TYPE, req.params.id, req.params.ver)
  if (!row) return res.status(404).json({ message: 'Version not found' })
  res.json(versionToDto(row))
})

router.put('/:id/version/:ver', (req, res) => {
  const { description, archived } = req.body
  const result = db.prepare(
    'UPDATE version SET description = ?, is_archived = ? WHERE entity_type = ? AND entity_id = ? AND version = ?'
  ).run(description || null, archived ? 1 : 0, ENTITY_TYPE, req.params.id, req.params.ver)
  if (!result.changes) return res.status(404).json({ message: 'Version not found' })
  const row = db.prepare(
    'SELECT * FROM version WHERE entity_type = ? AND entity_id = ? AND version = ?'
  ).get(ENTITY_TYPE, req.params.id, req.params.ver)
  res.json(versionToDto(row))
})

router.delete('/:id/version/:ver', (req, res) => {
  const result = db.prepare(
    'DELETE FROM version WHERE entity_type = ? AND entity_id = ? AND version = ?'
  ).run(ENTITY_TYPE, req.params.id, req.params.ver)
  if (!result.changes) return res.status(404).json({ message: 'Version not found' })
  res.sendStatus(200)
})

export default router
