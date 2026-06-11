import { Router } from 'express'
import db from '../db.js'
import { jobToResource } from '../jobResource.js'

const router = Router()

// Atlas hides statuses passed as `hide_statuses=COMPLETED,FAILED`
// and polls this endpoint every few seconds to show job activity.
router.get('/', (req, res) => {
  const hidden = new Set(
    (req.query.hide_statuses || '').split(',').map(s => s.trim().toUpperCase()).filter(Boolean)
  )

  // Return the 50 most recent jobs, filtered by hidden statuses
  const rows = db.prepare('SELECT * FROM job ORDER BY id DESC LIMIT 50').all()
  const resources = rows
    .filter(r => !hidden.has((r.status || '').toUpperCase()))
    .map(jobToResource)

  res.json(resources)
})

// GET /notifications/viewed — timestamp when user last dismissed notifications
router.get('/viewed', (_req, res) => {
  const row = db.prepare('SELECT viewed_at FROM notifications_viewed WHERE id = 1').get()
  res.json(row ? row.viewed_at : Date.now())
})

// POST /notifications/viewed — mark notifications as viewed now.
// Atlas sends the timestamp as a bare JSON number; express.json() rejects primitives
// in strict mode, so we just record Date.now() instead.
router.post('/viewed', (_req, res) => {
  db.prepare('UPDATE notifications_viewed SET viewed_at = ? WHERE id = 1').run(Date.now())
  res.sendStatus(200)
})

export default router
