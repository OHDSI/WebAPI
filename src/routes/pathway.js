import { makeAnalysisRouter, rowToDto } from './analysisFactory.js'
import db from '../db.js'

// Pathway Analysis — /pathway-analysis
// GET /:id/design is the Atlas entrypoint for editing; returns full DTO same as GET /:id.

const router = makeAnalysisRouter('pathway_analysis')

router.get('/:id/design', (req, res) => {
  const row = db.prepare('SELECT * FROM pathway_analysis WHERE id = ?').get(req.params.id)
  if (!row) return res.status(404).json({ message: 'Not found' })
  res.json(rowToDto(row))
})

export default router
