import { Router } from 'express'
import config from '../config.js'

const router = Router()

router.get('/', (req, res) => {
  res.json({
    version: config.version,
    buildInfo: {
      artifactVersion: `WebAPI ${config.version}`,
      build: 'node',
      timestamp: new Date().toISOString(),
      branch: null,
      commitId: null,
      atlasRepositoryInfo: { milestoneId: null, releaseTag: null },
      webapiRepositoryInfo: { milestoneId: null, releaseTag: null }
    },
    configuration: {}
  })
})

export default router
