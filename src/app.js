import express from 'express'
import userMiddleware from './middleware/user.js'
import errorHandler from './middleware/errors.js'
import infoRouter from './routes/info.js'
import sourceRouter from './routes/source.js'
import userRouter from './routes/user.js'
import vocabularyRouter from './routes/vocabulary.js'
import conceptsetRouter from './routes/conceptset.js'
import cohortdefinitionRouter from './routes/cohortdefinition.js'
import cdmresultsRouter from './routes/cdmresults.js'
import notificationsRouter from './routes/notifications.js'
import jobRouter from './routes/job.js'
import irRouter from './routes/ir.js'
import cohortcharacterizationRouter from './routes/cohortcharacterization.js'
import pathwayRouter from './routes/pathway.js'
import estimationRouter from './routes/estimation.js'
import predictionRouter from './routes/prediction.js'
import tagsRouter from './routes/tags.js'
import cohortresultsRouter from './routes/cohortresults.js'
import personRouter from './routes/person.js'

const app = express()

// strict: false allows bare JSON primitives (Atlas sends a bare timestamp to POST /notifications/viewed)
app.use(express.json({ limit: '10mb', strict: false }))

// Atlas sends cross-origin requests from its own origin
app.use((req, res, next) => {
  res.setHeader('Access-Control-Allow-Origin', '*')
  res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,DELETE,OPTIONS')
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type,Authorization')
  if (req.method === 'OPTIONS') return res.sendStatus(204)
  next()
})

app.use(userMiddleware)

app.use('/info', infoRouter)
app.use('/source', sourceRouter)
app.use('/vocabulary', vocabularyRouter)
app.use('/conceptset', conceptsetRouter)
app.use('/cohortdefinition', cohortdefinitionRouter)
app.use('/cdmresults', cdmresultsRouter)
app.use('/notifications', notificationsRouter)
app.use('/job', jobRouter)
app.use('/ir', irRouter)
app.use('/cohort-characterization', cohortcharacterizationRouter)
app.use('/pathway-analysis', pathwayRouter)
app.use('/estimation', estimationRouter)
app.use('/prediction', predictionRouter)
app.use('/tag', tagsRouter)
app.use('/cohortresults', cohortresultsRouter)
// Person profile: /:sourceKey/person/:personId — must come after all fixed-prefix routes
app.use('/:sourceKey/person', personRouter)
app.use('/', userRouter)

app.use(errorHandler)

export default app
