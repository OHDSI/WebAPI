import { makeAnalysisRouter, rowToDto } from './analysisFactory.js'
import db from '../db.js'

// Cohort Characterization — /cohort-characterization
// GET /:id/design returns the full expression (same as GET /:id for our flat storage).
// All generation endpoints return stubs — execution requires external Java tooling.

const router = makeAnalysisRouter('cc_analysis', (r) => {
  // Extended result-explore endpoint Atlas may request post-generation
  r.get('/generation/:generationId/result/explore/prevalence/:sourceKey/:conceptId/:domainId',
    (_req, res) => res.json([]))
})

// Atlas fetches the full design via /:id/design when opening an analysis for editing.
// This is safe to register after the factory because /:id only matches single-segment paths.
router.get('/:id/design', (req, res) => {
  const row = db.prepare('SELECT * FROM cc_analysis WHERE id = ?').get(req.params.id)
  if (!row) return res.status(404).json({ message: 'Not found' })
  res.json(rowToDto(row))
})

export default router
