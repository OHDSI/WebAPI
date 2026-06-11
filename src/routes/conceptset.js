import { Router } from 'express'
import db from '../db.js'
import { getPool, getSource } from '../sources.js'
import config from '../config.js'

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

function rowToDto (row) {
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

function defaultVocabSource () {
  const s = config.sources[0]
  if (!s) throw Object.assign(new Error('No vocabulary source configured'), { status: 503 })
  return s
}

// Fetch concept details from the vocabulary source for a list of integer concept IDs.
async function fetchConceptDetails (source, conceptIds) {
  if (conceptIds.length === 0) return {}
  const pool = getPool(source.sourceKey)
  const idList = conceptIds.join(',')
  const result = await pool.request().query(
    `SELECT CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT,
     ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID,
     DOMAIN_ID, VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE
     FROM ${source.vocabSchema}.concept
     WHERE CONCEPT_ID IN (${idList})`
  )
  const map = {}
  for (const row of result.recordset) {
    const d = row.VALID_START_DATE
    const e = row.VALID_END_DATE
    map[row.CONCEPT_ID] = {
      CONCEPT_ID: row.CONCEPT_ID,
      CONCEPT_NAME: row.CONCEPT_NAME,
      STANDARD_CONCEPT: row.STANDARD_CONCEPT,
      INVALID_REASON: row.INVALID_REASON,
      CONCEPT_CODE: row.CONCEPT_CODE,
      CONCEPT_CLASS_ID: row.CONCEPT_CLASS_ID,
      DOMAIN_ID: row.DOMAIN_ID,
      VOCABULARY_ID: row.VOCABULARY_ID,
      VALID_START_DATE: d instanceof Date ? d.toISOString().slice(0, 10) : String(d || '').slice(0, 10),
      VALID_END_DATE: e instanceof Date ? e.toISOString().slice(0, 10) : String(e || '').slice(0, 10)
    }
  }
  return map
}

// Build ConceptSetExpression from stored items + looked-up concept details.
async function buildExpression (conceptSetId, source) {
  const items = db.prepare('SELECT * FROM concept_set_item WHERE concept_set_id = ?').all(conceptSetId)
  const conceptIds = items.map(i => i.concept_id)
  const details = await fetchConceptDetails(source, conceptIds)
  return {
    items: items.map(i => ({
      concept: details[i.concept_id] || { CONCEPT_ID: i.concept_id },
      isExcluded: Boolean(i.is_excluded),
      includeDescendants: Boolean(i.include_descendants),
      includeMapped: Boolean(i.include_mapped)
    }))
  }
}

// --- static routes (must precede /:id) ---

// GET /exportlist
router.get('/exportlist', (_req, res) => {
  res.json([])
})

// GET /
router.get('/', (_req, res) => {
  const rows = db.prepare('SELECT * FROM concept_set ORDER BY id DESC').all()
  res.json(rows.map(rowToDto))
})

// POST /
router.post('/', (req, res) => {
  const { name, description } = req.body || {}
  if (!name) return res.status(400).json({ message: 'name is required' })
  const login = req.user ? req.user.login : 'anonymous'
  const result = db.prepare(
    'INSERT INTO concept_set (name, description, created_by) VALUES (?, ?, ?)'
  ).run(name, description || null, login)
  const row = db.prepare('SELECT * FROM concept_set WHERE id = ?').get(result.lastInsertRowid)
  res.status(201).json(rowToDto(row))
})

// --- single concept set ---

// GET /:id
router.get('/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM concept_set WHERE id = ?').get(req.params.id)
  if (!row) return res.status(404).json({ message: 'Concept set not found' })
  res.json(rowToDto(row))
})

// PUT /:id
router.put('/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM concept_set WHERE id = ?').get(req.params.id)
  if (!row) return res.status(404).json({ message: 'Concept set not found' })
  const { name, description } = req.body || {}
  const login = req.user ? req.user.login : 'anonymous'
  db.prepare(
    `UPDATE concept_set SET name = ?, description = ?, modified_by = ?,
     modified_date = (unixepoch() * 1000) WHERE id = ?`
  ).run(name || row.name, description !== undefined ? description : row.description, login, row.id)
  const updated = db.prepare('SELECT * FROM concept_set WHERE id = ?').get(row.id)
  res.json(rowToDto(updated))
})

// DELETE /:id
router.delete('/:id', (req, res) => {
  const row = db.prepare('SELECT id FROM concept_set WHERE id = ?').get(req.params.id)
  if (!row) return res.status(404).json({ message: 'Concept set not found' })
  db.prepare('DELETE FROM concept_set WHERE id = ?').run(row.id)
  res.sendStatus(204)
})

// --- copy name ---

// GET /:id/copy-name
router.get('/:id/copy-name', (req, res) => {
  const row = db.prepare('SELECT name FROM concept_set WHERE id = ?').get(req.params.id)
  if (!row) return res.status(404).json({ message: 'Concept set not found' })
  res.json({ copyName: `Copy of ${row.name}` })
})

// --- exists check ---

// GET /:id/exists?name=...
router.get('/:id/exists', (req, res) => {
  const name = req.query.name
  const id = parseInt(req.params.id, 10)
  const row = db.prepare('SELECT COUNT(*) AS cnt FROM concept_set WHERE name = ? AND id != ?').get(name, id)
  res.json(row.cnt)
})

// --- items ---

// GET /:id/items
router.get('/:id/items', (req, res) => {
  const cs = db.prepare('SELECT id FROM concept_set WHERE id = ?').get(req.params.id)
  if (!cs) return res.status(404).json({ message: 'Concept set not found' })
  const items = db.prepare('SELECT * FROM concept_set_item WHERE concept_set_id = ?').all(req.params.id)
  res.json(items.map(i => ({
    id: i.id,
    conceptSetId: i.concept_set_id,
    conceptId: i.concept_id,
    isExcluded: Boolean(i.is_excluded),
    includeDescendants: Boolean(i.include_descendants),
    includeMapped: Boolean(i.include_mapped)
  })))
})

// PUT /:id/items
router.put('/:id/items', (req, res) => {
  const cs = db.prepare('SELECT id FROM concept_set WHERE id = ?').get(req.params.id)
  if (!cs) return res.status(404).json({ message: 'Concept set not found' })
  const incomingItems = req.body || []

  const saveItems = db.transaction((csId, items) => {
    db.prepare('DELETE FROM concept_set_item WHERE concept_set_id = ?').run(csId)
    const insert = db.prepare(
      `INSERT INTO concept_set_item (concept_set_id, concept_id, is_excluded, include_descendants, include_mapped)
       VALUES (?, ?, ?, ?, ?)`
    )
    for (const item of items) {
      insert.run(
        csId,
        Number(item.conceptId || item.concept_id),
        item.isExcluded || item.is_excluded ? 1 : 0,
        item.includeDescendants || item.include_descendants ? 1 : 0,
        item.includeMapped || item.include_mapped ? 1 : 0
      )
    }
    db.prepare(
      `UPDATE concept_set SET modified_date = (unixepoch() * 1000) WHERE id = ?`
    ).run(csId)
    return db.prepare('SELECT * FROM concept_set_item WHERE concept_set_id = ?').all(csId)
  })

  const saved = saveItems(cs.id, incomingItems)
  res.json(saved.map(i => ({
    id: i.id,
    conceptSetId: i.concept_set_id,
    conceptId: i.concept_id,
    isExcluded: Boolean(i.is_excluded),
    includeDescendants: Boolean(i.include_descendants),
    includeMapped: Boolean(i.include_mapped)
  })))
})

// --- expression ---

// GET /:id/expression
router.get('/:id/expression', async (req, res, next) => {
  try {
    const cs = db.prepare('SELECT id FROM concept_set WHERE id = ?').get(req.params.id)
    if (!cs) return res.status(404).json({ message: 'Concept set not found' })
    const source = defaultVocabSource()
    res.json(await buildExpression(cs.id, source))
  } catch (err) { next(err) }
})

// GET /:id/expression/:sourceKey
router.get('/:id/expression/:sourceKey', async (req, res, next) => {
  try {
    const cs = db.prepare('SELECT id FROM concept_set WHERE id = ?').get(req.params.id)
    if (!cs) return res.status(404).json({ message: 'Concept set not found' })
    const source = getSource(req.params.sourceKey)
    res.json(await buildExpression(cs.id, source))
  } catch (err) { next(err) }
})

// GET /:id/version/:version/expression
router.get('/:id/version/:version/expression', async (req, res, next) => {
  try {
    const ver = db.prepare(
      `SELECT expression FROM version WHERE entity_type = 'conceptset' AND entity_id = ? AND version = ?`
    ).get(req.params.id, req.params.version)
    if (!ver || !ver.expression) return res.status(404).json({ message: 'Version not found' })
    res.json(JSON.parse(ver.expression))
  } catch (err) { next(err) }
})

// --- generation info (stub) ---

// GET /:id/generationinfo
router.get('/:id/generationinfo', (req, res) => {
  res.json([])
})

// --- version endpoints ---

const ENTITY_TYPE = 'concept_set'

function versionToDto (row) {
  return {
    id: row.id,
    entityId: row.entity_id,
    version: row.version,
    description: row.description || null,
    archived: !!row.is_archived,
    createdBy: { id: 0, login: row.created_by, name: row.created_by },
    createdDate: row.created_date ? new Date(row.created_date).toISOString() : null,
    hasWriteAccess: true
  }
}

function nextVersion (entityId) {
  return ((db.prepare(
    'SELECT MAX(version) AS v FROM version WHERE entity_type = ? AND entity_id = ?'
  ).get(ENTITY_TYPE, entityId))?.v ?? 0) + 1
}

router.get('/:id/version', (req, res) => {
  const rows = db.prepare(
    'SELECT * FROM version WHERE entity_type = ? AND entity_id = ? ORDER BY version DESC'
  ).all(ENTITY_TYPE, req.params.id)
  res.json(rows.map(versionToDto))
})

router.post('/:id/version', (req, res) => {
  const items = db.prepare('SELECT * FROM concept_set_item WHERE concept_set_id = ?').all(req.params.id)
  const user = req.user?.login || 'anonymous'
  const ver = nextVersion(req.params.id)
  db.prepare(
    'INSERT INTO version (entity_type, entity_id, version, expression, description, created_by) VALUES (?, ?, ?, ?, ?, ?)'
  ).run(ENTITY_TYPE, req.params.id, ver, JSON.stringify(items), req.body?.description || null, user)
  const saved = db.prepare(
    'SELECT * FROM version WHERE entity_type = ? AND entity_id = ? AND version = ?'
  ).get(ENTITY_TYPE, req.params.id, ver)
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
