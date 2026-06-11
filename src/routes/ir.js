import { makeAnalysisRouter } from './analysisFactory.js'

// IR Analysis — /ir/
// POST /sql and POST /check are 501/[] because cohort SQL generation requires CIRCE.
// All generation/report endpoints stub-out; Atlas will show "no results" state.

const router = makeAnalysisRouter('ir_analysis', (r) => {
  r.post('/sql', (_req, res) => res.sendStatus(501))
  r.post('/check', (_req, res) => res.json([]))
})

// Per-analysis report — Atlas requests this after generation; return empty shape
router.get('/:id/report/:sourceKey', (_req, res) => res.json({ treemap: null, results: [] }))

export default router
