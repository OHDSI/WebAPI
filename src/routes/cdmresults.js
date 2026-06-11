import { Router } from 'express'
import { readdirSync } from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'
import { getPool, getSource } from '../sources.js'
import { loadSql, renderSql } from '../sqlrender.js'

const router = Router()

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const CDM_SQL_DIR = path.join(__dirname, '..', 'sql', 'cdmresults')

// --- helpers ---

// Convert any column name variant (snake_case, UPPER_CASE, camelCase) to camelCase.
function normKey (str) {
  if (!str.includes('_')) {
    // Already camelCase or plain word — lowercase the first letter only.
    return str.charAt(0).toLowerCase() + str.slice(1)
  }
  // snake_case or UPPER_CASE → camelCase
  return str.toLowerCase().replace(/_([a-z0-9])/g, (_, c) => c.toUpperCase())
}

function normRow (row) {
  const out = {}
  for (const [k, v] of Object.entries(row)) {
    out[normKey(k)] = v
  }
  return out
}

// Run a single CDM results SQL template against a source.
async function runReport (sourceKey, relPath, extraParams) {
  const source = getSource(sourceKey)
  const pool = getPool(sourceKey)
  const rawSql = loadSql(`cdmresults/${relPath}`)
  const rendered = renderSql(rawSql, source, extraParams || {})
  const result = await pool.request().query(rendered)
  return result.recordset.map(normRow)
}

// Run all *.sql files in a directory, returning { filename_without_ext: rows[] }.
async function runDirectory (sourceKey, relDir, extraParams) {
  const source = getSource(sourceKey)
  const pool = getPool(sourceKey)
  const absDir = path.join(CDM_SQL_DIR, relDir)
  let files
  try { files = readdirSync(absDir).filter(f => f.endsWith('.sql')).sort() } catch { return {} }

  const out = {}
  for (const file of files) {
    const rawSql = loadSql(`cdmresults/${relDir}/${file}`)
    const rendered = renderSql(rawSql, source, extraParams || {})
    try {
      const result = await pool.request().query(rendered)
      out[file.replace('.sql', '')] = result.recordset.map(normRow)
    } catch (err) {
      out[file.replace('.sql', '')] = []
    }
  }
  return out
}

// --- warmCache / refreshCache / clearCache stubs ---

router.get('/:sourceKey/warmCache', (_req, res) => res.json({ status: 'not implemented' }))
router.get('/:sourceKey/refreshCache', (_req, res) => res.json({ status: 'not implemented' }))
router.post('/:sourceKey/clearCache', (_req, res) => res.sendStatus(204))
router.post('/clearCache', (_req, res) => res.sendStatus(204))

// --- dashboard ---

router.get('/:sourceKey/dashboard', async (req, res, next) => {
  try {
    const sk = req.params.sourceKey
    const [summary, ageAtFirstObservation, gender, cumulativeObservation, observedByMonth] = await Promise.all([
      runReport(sk, 'report/person/population.sql'),
      runReport(sk, 'report/observationperiod/ageatfirst.sql'),
      runReport(sk, 'report/person/gender.sql'),
      runReport(sk, 'report/observationperiod/cumulativeduration.sql'),
      runReport(sk, 'report/observationperiod/observedbymonth.sql')
    ])
    res.json({ summary, ageAtFirstObservation, gender, cumulativeObservation, observedByMonth })
  } catch (err) { next(err) }
})

// --- person ---

router.get('/:sourceKey/person', async (req, res, next) => {
  try {
    const sk = req.params.sourceKey
    const [summary, gender, race, ethnicity, yearOfBirth, yearOfBirthStats] = await Promise.all([
      runReport(sk, 'report/person/population.sql'),
      runReport(sk, 'report/person/gender.sql'),
      runReport(sk, 'report/person/race.sql'),
      runReport(sk, 'report/person/ethnicity.sql'),
      runReport(sk, 'report/person/yearofbirth_data.sql'),
      runReport(sk, 'report/person/yearofbirth_stats.sql')
    ])
    res.json({ summary, gender, race, ethnicity, yearOfBirth, yearOfBirthStats })
  } catch (err) { next(err) }
})

// --- datadensity ---

router.get('/:sourceKey/datadensity', async (req, res, next) => {
  try {
    const sk = req.params.sourceKey
    const [conceptsPerPerson, recordsPerPerson, totalRecords] = await Promise.all([
      runReport(sk, 'report/datadensity/conceptsperperson.sql'),
      runReport(sk, 'report/datadensity/recordsperperson.sql'),
      runReport(sk, 'report/datadensity/totalrecords.sql')
    ])
    res.json({ conceptsPerPerson, recordsPerPerson, totalRecords })
  } catch (err) { next(err) }
})

// --- death ---

router.get('/:sourceKey/death', async (req, res, next) => {
  try {
    const sk = req.params.sourceKey
    const [prevalenceByGenderAgeYear, prevalenceByMonth, deathByType, ageAtDeath] = await Promise.all([
      runReport(sk, 'report/death/sqlPrevalenceByGenderAgeYear.sql'),
      runReport(sk, 'report/death/sqlPrevalenceByMonth.sql'),
      runReport(sk, 'report/death/sqlDeathByType.sql'),
      runReport(sk, 'report/death/sqlAgeAtDeath.sql')
    ])
    res.json({ prevalenceByGenderAgeYear, prevalenceByMonth, deathByType, ageAtDeath })
  } catch (err) { next(err) }
})

// --- observationPeriod ---

router.get('/:sourceKey/observationPeriod', async (req, res, next) => {
  try {
    const sk = req.params.sourceKey
    const [
      ageAtFirst, observationLength, observationLengthStats,
      personsWithContinuousObservationsByYearStats, personsWithContinuousObservationsByYear,
      ageByGender, observationLengthByAge, observationLengthByGender,
      observedByMonth, periodsPerPerson
    ] = await Promise.all([
      runReport(sk, 'report/observationperiod/ageatfirst.sql'),
      runReport(sk, 'report/observationperiod/observationlength_data.sql'),
      runReport(sk, 'report/observationperiod/observationlength_stats.sql'),
      runReport(sk, 'report/observationperiod/observedbyyear_stats.sql'),
      runReport(sk, 'report/observationperiod/observedbyyear_data.sql'),
      runReport(sk, 'report/observationperiod/agebygender.sql'),
      runReport(sk, 'report/observationperiod/observationlengthbyage.sql'),
      runReport(sk, 'report/observationperiod/observationlengthbygender.sql'),
      runReport(sk, 'report/observationperiod/observedbymonth.sql'),
      runReport(sk, 'report/observationperiod/periodsperperson.sql')
    ])
    res.json({
      ageAtFirst, observationLength, observationLengthStats,
      personsWithContinuousObservationsByYearStats, personsWithContinuousObservationsByYear,
      ageByGender, observationLengthByAge, observationLengthByGender,
      observedByMonth, periodsPerPerson
    })
  } catch (err) { next(err) }
})

// --- conceptRecordCount ---

router.post('/:sourceKey/conceptRecordCount', async (req, res, next) => {
  try {
    const source = getSource(req.params.sourceKey)
    const pool = getPool(req.params.sourceKey)
    const ids = (req.body || []).map(Number).filter(Boolean)
    if (ids.length === 0) return res.json([])

    const rendered = renderSql(
      loadSql('cdmresults/getConceptRecordCount.sql'),
      source,
      { resultTableQualifier: source.resultsSchema, conceptIdentifiers: ids.join(',') }
    )
    const result = await pool.request().query(rendered)
    // Return as [{key: conceptId, value: [recordCount, descendantRecordCount]}, ...]
    const response = result.recordset.map(r => ({
      key: r.concept_id || r.CONCEPT_ID,
      value: [
        r.record_count !== undefined ? r.record_count : (r.RECORD_COUNT || 0),
        r.descendant_record_count !== undefined ? r.descendant_record_count : (r.DESCENDANT_RECORD_COUNT || 0)
      ]
    }))
    res.json(response)
  } catch (err) { next(err) }
})

// --- treemap ---

// GET /:sourceKey/:domain/
// Express needs a trailing-slash-aware matcher; catch it with optional trailing slash.
const VALID_DOMAINS = new Set([
  'condition', 'conditionera', 'drug', 'drugera',
  'procedure', 'observation', 'measurement', 'visit'
])

router.get('/:sourceKey/:domain', async (req, res, next) => {
  const domain = req.params.domain.toLowerCase()
  if (!VALID_DOMAINS.has(domain)) return next()
  try {
    const source = getSource(req.params.sourceKey)
    const pool = getPool(req.params.sourceKey)
    const rawSql = loadSql(`cdmresults/report/${domain}/treemap.sql`)
    const rendered = renderSql(rawSql, source)
    const result = await pool.request().query(rendered)
    res.json(result.recordset.map(normRow))
  } catch (err) { next(err) }
})

// --- drilldown ---

router.get('/:sourceKey/:domain/:conceptId', async (req, res, next) => {
  const domain = req.params.domain.toLowerCase()
  if (!VALID_DOMAINS.has(domain)) return next()
  try {
    const conceptId = parseInt(req.params.conceptId, 10)
    if (isNaN(conceptId)) return res.status(400).json({ message: 'Invalid conceptId' })
    const result = await runDirectory(
      req.params.sourceKey,
      `report/${domain}/drilldown`,
      { conceptId: String(conceptId) }
    )
    res.json(result)
  } catch (err) { next(err) }
})

export default router
