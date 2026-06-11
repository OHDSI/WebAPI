import { Router } from 'express'
import db from '../db.js'
import { jobToResource } from '../jobResource.js'

const router = Router()

// --- static routes before /:jobId ---

// GET /execution — paged list of all executions (Spring Page shape)
router.get('/execution', (req, res) => {
  const pageIndex = parseInt(req.query.pageIndex || '0', 10)
  const pageSize = parseInt(req.query.pageSize || '20', 10)
  const jobName = req.query.jobName || null

  const countStmt = jobName
    ? db.prepare('SELECT COUNT(*) AS cnt FROM job WHERE type = ?')
    : db.prepare('SELECT COUNT(*) AS cnt FROM job')
  const totalElements = (jobName ? countStmt.get(jobName) : countStmt.get()).cnt

  const listStmt = jobName
    ? db.prepare('SELECT * FROM job WHERE type = ? ORDER BY id DESC LIMIT ? OFFSET ?')
    : db.prepare('SELECT * FROM job ORDER BY id DESC LIMIT ? OFFSET ?')

  const rows = jobName
    ? listStmt.all(jobName, pageSize, pageIndex * pageSize)
    : listStmt.all(pageSize, pageIndex * pageSize)

  const content = rows.map(jobToResource)
  const totalPages = Math.ceil(totalElements / pageSize) || 1

  res.json({
    content,
    totalElements,
    totalPages,
    size: pageSize,
    number: pageIndex,
    first: pageIndex === 0,
    last: pageIndex >= totalPages - 1,
    numberOfElements: content.length
  })
})

// GET /execution/:executionId — find a single execution by its ID
router.get('/execution/:executionId', (req, res) => {
  const row = db.prepare('SELECT * FROM job WHERE id = ?').get(req.params.executionId)
  if (!row) return res.status(404).json({ message: 'Job execution not found' })
  res.json(jobToResource(row))
})

// GET /type/:jobType/name/:jobName — find a running job by type and name
router.get('/type/:jobType/name/:jobName', (req, res) => {
  const row = db.prepare(
    "SELECT * FROM job WHERE type = ? AND name = ? AND status IN ('STARTED','STARTING') ORDER BY id DESC LIMIT 1"
  ).get(req.params.jobType, req.params.jobName)
  if (!row) return res.json(null)
  res.json(jobToResource(row))
})

// GET / — list of distinct job type names
router.get('/', (_req, res) => {
  const rows = db.prepare('SELECT DISTINCT type FROM job ORDER BY type').all()
  res.json(rows.map(r => r.type))
})

// --- parameterised routes ---

// GET /:jobId — job instance by ID
router.get('/:jobId', (req, res) => {
  const row = db.prepare('SELECT * FROM job WHERE id = ?').get(req.params.jobId)
  if (!row) return res.json(null)
  res.json({ instanceId: row.id, name: row.type || row.name })
})

// GET /:jobId/execution/:executionId
router.get('/:jobId/execution/:executionId', (req, res) => {
  const row = db.prepare('SELECT * FROM job WHERE id = ? AND id = ?').get(
    req.params.jobId, req.params.executionId
  )
  if (!row) return res.status(404).json({ message: 'Job execution not found' })
  res.json(jobToResource(row))
})

export default router
