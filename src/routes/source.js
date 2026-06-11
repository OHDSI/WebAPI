import { Router } from 'express'
import { getAllSourceInfos, toSourceInfo, getSource } from '../sources.js'
import config from '../config.js'

const router = Router()

// List all sources
router.get('/sources', (req, res) => {
  res.json(getAllSourceInfos())
})

// Refresh endpoint — no-op in our static config model
router.get('/refresh', (req, res) => {
  res.json(getAllSourceInfos())
})

// Priority vocabulary source — first source that has a Vocabulary daimon
router.get('/priorityVocabulary', (req, res) => {
  const idx = config.sources.findIndex(s => s.vocabSchema)
  if (idx === -1) return res.status(404).json({ message: 'No vocabulary source configured' })
  res.json(toSourceInfo(config.sources[idx], idx))
})

// Single source by key
router.get('/:key', (req, res, next) => {
  try {
    const source = getSource(req.params.key)
    const idx = config.sources.indexOf(source)
    res.json(toSourceInfo(source, idx))
  } catch (err) {
    next(err)
  }
})

export default router
