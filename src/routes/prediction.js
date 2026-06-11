import { makeAnalysisRouter } from './analysisFactory.js'

// Patient-Level Prediction — /prediction
// Execution requires the ARACHNE execution engine; all generation endpoints return 501/[].

const router = makeAnalysisRouter('prediction', (r) => {
  r.get('/:id/download', (_req, res) => res.sendStatus(501))
})

export default router
