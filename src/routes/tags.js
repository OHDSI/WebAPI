import { Router } from 'express'
import db from '../db.js'

// Tags — /tag
// Provides CRUD for tags and multi-assign/unassign against the entity_tag junction table.

const router = Router()

function rowToDto (row) {
  return {
    id: row.id,
    name: row.name,
    type: row.type || 'COMMON',
    count: 0,
    showGroup: false,
    multiSelection: false,
    permissionProtected: false,
    allowCustom: false,
    mandatory: false,
    hasWriteAccess: true
  }
}

// GET /search?namePart=... — must be before /:id
router.get('/search', (req, res) => {
  const part = req.query.namePart || ''
  const rows = db.prepare("SELECT * FROM tag WHERE name LIKE ? ORDER BY name").all(`%${part}%`)
  res.json(rows.map(rowToDto))
})

// GET /assignmentPermissions
router.get('/assignmentPermissions', (_req, res) => res.json([]))

// GET /
router.get('/', (_req, res) => {
  const rows = db.prepare('SELECT * FROM tag ORDER BY name').all()
  res.json(rows.map(rowToDto))
})

// POST /
router.post('/', (req, res) => {
  const { name, type } = req.body
  if (!name) return res.status(400).json({ message: 'name is required' })
  const user = req.user?.login || 'anonymous'
  try {
    const result = db.prepare(
      'INSERT INTO tag (name, type, created_by) VALUES (?, ?, ?)'
    ).run(name, type || 'COMMON', user)
    const row = db.prepare('SELECT * FROM tag WHERE id = ?').get(result.lastInsertRowid)
    res.status(201).json(rowToDto(row))
  } catch (e) {
    if (e.message?.includes('UNIQUE')) return res.status(409).json({ message: 'Tag name already exists' })
    throw e
  }
})

// POST /multiAssign — body: { tagIds: [1,2], entityIds: [3,4], entityType: 'COHORT' }
router.post('/multiAssign', (req, res) => {
  const { tagIds = [], entityIds = [], entityType = '' } = req.body
  const insert = db.prepare(
    'INSERT OR IGNORE INTO entity_tag (tag_id, entity_type, entity_id) VALUES (?, ?, ?)'
  )
  const tx = db.transaction(() => {
    for (const tagId of tagIds) {
      for (const entityId of entityIds) {
        insert.run(tagId, entityType, entityId)
      }
    }
  })
  tx()
  res.sendStatus(200)
})

// POST /multiUnassign
router.post('/multiUnassign', (req, res) => {
  const { tagIds = [], entityIds = [], entityType = '' } = req.body
  const del = db.prepare(
    'DELETE FROM entity_tag WHERE tag_id = ? AND entity_type = ? AND entity_id = ?'
  )
  const tx = db.transaction(() => {
    for (const tagId of tagIds) {
      for (const entityId of entityIds) {
        del.run(tagId, entityType, entityId)
      }
    }
  })
  tx()
  res.sendStatus(200)
})

// GET /:id
router.get('/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM tag WHERE id = ?').get(req.params.id)
  if (!row) return res.status(404).json({ message: 'Tag not found' })
  res.json(rowToDto(row))
})

// PUT /:id
router.put('/:id', (req, res) => {
  const { name, type } = req.body
  const result = db.prepare('UPDATE tag SET name = ?, type = ? WHERE id = ?').run(
    name || null, type || 'COMMON', req.params.id
  )
  if (!result.changes) return res.status(404).json({ message: 'Tag not found' })
  const row = db.prepare('SELECT * FROM tag WHERE id = ?').get(req.params.id)
  res.json(rowToDto(row))
})

// DELETE /:id
router.delete('/:id', (req, res) => {
  const result = db.prepare('DELETE FROM tag WHERE id = ?').run(req.params.id)
  if (!result.changes) return res.status(404).json({ message: 'Tag not found' })
  res.sendStatus(200)
})

export default router
