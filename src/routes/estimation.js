import { makeAnalysisRouter } from './analysisFactory.js'

// Population-Level Estimation — /estimation
// Execution requires the ARACHNE execution engine; all generation endpoints return 501/[].

const router = makeAnalysisRouter('estimation', (r) => {
  r.get('/:id/download', (_req, res) => res.sendStatus(501))
})

export default router
