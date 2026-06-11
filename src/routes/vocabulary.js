import { Router } from 'express'
import sql from 'mssql'
import config from '../config.js'
import { getPool, getSource } from '../sources.js'
import { loadSql, renderSql } from '../sqlrender.js'
import { buildConceptSetExpressionSql } from '../conceptSetExpression.js'

const router = Router()

// --- helpers ---

function formatDate (d) {
  if (!d) return null
  if (d instanceof Date) return d.toISOString().slice(0, 10)
  return String(d).slice(0, 10)
}

function mapConcept (row) {
  return {
    conceptId: row.CONCEPT_ID,
    conceptName: row.CONCEPT_NAME,
    standardConcept: row.STANDARD_CONCEPT,
    invalidReason: row.INVALID_REASON,
    conceptCode: row.CONCEPT_CODE,
    conceptClassId: row.CONCEPT_CLASS_ID,
    domainId: row.DOMAIN_ID,
    vocabularyId: row.VOCABULARY_ID,
    validStartDate: formatDate(row.VALID_START_DATE),
    validEndDate: formatDate(row.VALID_END_DATE)
  }
}

function mapRelatedConcept (row) {
  return {
    ...mapConcept(row),
    relationshipName: row.RELATIONSHIP_NAME,
    relationshipDistance: row.RELATIONSHIP_DISTANCE
  }
}

// Returns the first configured source (priority vocabulary source).
function defaultSource () {
  const s = config.sources[0]
  if (!s) throw Object.assign(new Error('No vocabulary source configured'), { status: 503 })
  return s
}

function defaultSourceKey () {
  return defaultSource().sourceKey
}

// Build a mssql request with optional named parameters.
function makeRequest (pool, params) {
  const req = pool.request()
  for (const [name, { type, value }] of Object.entries(params || {})) {
    req.input(name, type, value)
  }
  return req
}

// Run a vocab query against a source.
async function queryVocab (sourceKey, querySql, params) {
  const pool = getPool(sourceKey)
  const result = await makeRequest(pool, params).query(querySql)
  return result.recordset
}

// Build filters clause + mssql params for a concept search.
function buildSearchFilters (search, request) {
  let filters = ''

  if (search.domainId && search.domainId.length > 0) {
    const names = search.domainId.map((d, i) => {
      request.input(`domainId${i}`, sql.NVarChar, d)
      return `@domainId${i}`
    })
    filters += ` AND DOMAIN_ID IN (${names.join(',')})`
  }

  if (search.vocabularyId && search.vocabularyId.length > 0) {
    const names = search.vocabularyId.map((v, i) => {
      request.input(`vocabId${i}`, sql.NVarChar, v)
      return `@vocabId${i}`
    })
    filters += ` AND VOCABULARY_ID IN (${names.join(',')})`
  }

  if (search.conceptClassId && search.conceptClassId.length > 0) {
    const names = search.conceptClassId.map((c, i) => {
      request.input(`classId${i}`, sql.NVarChar, c)
      return `@classId${i}`
    })
    filters += ` AND CONCEPT_CLASS_ID IN (${names.join(',')})`
  }

  if (search.invalidReason) {
    if (search.invalidReason === 'V') {
      filters += ' AND INVALID_REASON IS NULL'
    } else {
      request.input('invalidReason', sql.VarChar(1), search.invalidReason)
      filters += ' AND INVALID_REASON = @invalidReason'
    }
  }

  if (search.standardConcept !== undefined && search.standardConcept !== null) {
    if (search.standardConcept === 'N') {
      filters += ' AND STANDARD_CONCEPT IS NULL'
    } else {
      request.input('standardConcept', sql.VarChar(1), search.standardConcept)
      filters += ' AND STANDARD_CONCEPT = @standardConcept'
    }
  }

  if (search.query) {
    const q = search.query.replace('[', '[[]').toLowerCase()
    request.input('searchQ', sql.NVarChar, `%${q}%`)
    let qFilter = '(LOWER(CONCEPT_NAME) LIKE @searchQ OR LOWER(CONCEPT_CODE) LIKE @searchQ'
    if (/^\d+$/.test(search.query)) {
      request.input('searchQInt', sql.Int, parseInt(search.query, 10))
      qFilter += ' OR CONCEPT_ID = @searchQInt'
    }
    qFilter += ')'
    filters += ` AND ${qFilter}`
  }

  return filters
}

// --- concept search ---

async function executeSearch (sourceKey, search) {
  const source = getSource(sourceKey)
  const pool = getPool(sourceKey)
  const request = pool.request()
  const filters = buildSearchFilters(search, request)
  const querySql = renderSql(loadSql('vocabulary/search.sql'), source, { filters })
  const result = await request.query(querySql)
  return result.recordset.map(mapConcept)
}

// POST /:sourceKey/search
router.post('/:sourceKey/search', async (req, res, next) => {
  try {
    res.json(await executeSearch(req.params.sourceKey, req.body || {}))
  } catch (err) { next(err) }
})

// GET /:sourceKey/search  ?query=...
router.get('/:sourceKey/search', async (req, res, next) => {
  try {
    res.json(await executeSearch(req.params.sourceKey, { query: req.query.query || '' }))
  } catch (err) { next(err) }
})

// GET /:sourceKey/search/:query
router.get('/:sourceKey/search/:query', async (req, res, next) => {
  try {
    res.json(await executeSearch(req.params.sourceKey, { query: req.params.query }))
  } catch (err) { next(err) }
})

// POST /search (default source)
router.post('/search', async (req, res, next) => {
  try {
    res.json(await executeSearch(defaultSourceKey(), req.body || {}))
  } catch (err) { next(err) }
})

// GET /search/:query (default source)
router.get('/search/:query', async (req, res, next) => {
  try {
    res.json(await executeSearch(defaultSourceKey(), { query: req.params.query }))
  } catch (err) { next(err) }
})

// --- concept lookup ---

// POST /:sourceKey/lookup/identifiers
router.post('/:sourceKey/lookup/identifiers', async (req, res, next) => {
  try {
    const source = getSource(req.params.sourceKey)
    const ids = (req.body || []).map(Number).filter(Boolean)
    if (ids.length === 0) return res.json([])
    const querySql = renderSql(
      `SELECT CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT,
       ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID,
       DOMAIN_ID, VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE
       FROM @vocabulary_database_schema.concept
       WHERE CONCEPT_ID IN (${ids.join(',')})
       ORDER BY CONCEPT_NAME ASC`,
      source
    )
    const pool = getPool(req.params.sourceKey)
    const result = await pool.request().query(querySql)
    res.json(result.recordset.map(mapConcept))
  } catch (err) { next(err) }
})

// POST /lookup/identifiers (default source)
router.post('/lookup/identifiers', async (req, res, next) => {
  try {
    const source = defaultSource()
    const ids = (req.body || []).map(Number).filter(Boolean)
    if (ids.length === 0) return res.json([])
    const querySql = renderSql(
      `SELECT CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT,
       ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID,
       DOMAIN_ID, VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE
       FROM @vocabulary_database_schema.concept
       WHERE CONCEPT_ID IN (${ids.join(',')})
       ORDER BY CONCEPT_NAME ASC`,
      source
    )
    const pool = getPool(source.sourceKey)
    const result = await pool.request().query(querySql)
    res.json(result.recordset.map(mapConcept))
  } catch (err) { next(err) }
})

// POST /:sourceKey/lookup/sourcecodes
router.post('/:sourceKey/lookup/sourcecodes', async (req, res, next) => {
  try {
    const source = getSource(req.params.sourceKey)
    const codes = req.body || []
    if (codes.length === 0) return res.json([])
    const pool = getPool(req.params.sourceKey)
    const request = pool.request()
    const paramNames = codes.map((c, i) => {
      request.input(`code${i}`, sql.NVarChar, String(c))
      return `@code${i}`
    })
    const querySql = renderSql(
      `SELECT CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT,
       ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID,
       DOMAIN_ID, VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE
       FROM @vocabulary_database_schema.concept
       WHERE CONCEPT_CODE IN (${paramNames.join(',')})
       ORDER BY CONCEPT_NAME ASC`,
      source
    )
    const result = await request.query(querySql)
    res.json(result.recordset.map(mapConcept))
  } catch (err) { next(err) }
})

// POST /:sourceKey/lookup/mapped
router.post('/:sourceKey/lookup/mapped', async (req, res, next) => {
  try {
    const source = getSource(req.params.sourceKey)
    const ids = (req.body || []).map(Number).filter(Boolean)
    if (ids.length === 0) return res.json([])
    const idList = ids.join(',')
    const querySql = renderSql(
      `SELECT CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT,
       ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID,
       DOMAIN_ID, VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE
       FROM @vocabulary_database_schema.concept
       WHERE CONCEPT_ID IN (${idList})
       UNION
       SELECT c.CONCEPT_ID, c.CONCEPT_NAME, ISNULL(c.STANDARD_CONCEPT,'N') STANDARD_CONCEPT,
       ISNULL(c.INVALID_REASON,'V') INVALID_REASON, c.CONCEPT_CODE, c.CONCEPT_CLASS_ID,
       c.DOMAIN_ID, c.VOCABULARY_ID, c.VALID_START_DATE, c.VALID_END_DATE
       FROM @vocabulary_database_schema.concept_relationship cr
       JOIN @vocabulary_database_schema.concept c ON c.concept_id = cr.concept_id_1
       WHERE cr.concept_id_2 IN (${idList})
       AND cr.INVALID_REASON IS NULL
       AND cr.relationship_id = 'Maps to'
       UNION
       SELECT 0 AS CONCEPT_ID, SOURCE_CODE_DESCRIPTION AS CONCEPT_NAME,
       'N' AS STANDARD_CONCEPT, ISNULL(INVALID_REASON,'V') INVALID_REASON,
       SOURCE_CODE AS CONCEPT_CODE, NULL AS CONCEPT_CLASS_ID, NULL AS DOMAIN_ID,
       SOURCE_VOCABULARY_ID AS VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE
       FROM @vocabulary_database_schema.SOURCE_TO_CONCEPT_MAP
       WHERE TARGET_CONCEPT_ID IN (${idList})
       AND INVALID_REASON IS NULL
       ORDER BY DOMAIN_ID, VOCABULARY_ID`,
      source
    )
    const pool = getPool(req.params.sourceKey)
    const result = await pool.request().query(querySql)
    res.json(result.recordset.map(mapConcept))
  } catch (err) { next(err) }
})

// POST /lookup/mapped (default source)
router.post('/lookup/mapped', async (req, res, next) => {
  try {
    req.params.sourceKey = defaultSourceKey()
    router.handle(req, res, () => {})
  } catch (err) { next(err) }
})

// --- resolve concept set expression ---

async function resolveExpression (sourceKey, expression) {
  const source = getSource(sourceKey)
  const pool = getPool(sourceKey)
  const querySql = buildConceptSetExpressionSql(expression, source.vocabSchema)
  const result = await pool.request().query(querySql)
  return result.recordset.map(r => r.concept_id)
}

// POST /:sourceKey/resolveConceptSetExpression
router.post('/:sourceKey/resolveConceptSetExpression', async (req, res, next) => {
  try {
    res.json(await resolveExpression(req.params.sourceKey, req.body))
  } catch (err) { next(err) }
})

// POST /resolveConceptSetExpression (default source)
router.post('/resolveConceptSetExpression', async (req, res, next) => {
  try {
    res.json(await resolveExpression(defaultSourceKey(), req.body))
  } catch (err) { next(err) }
})

// POST /:sourceKey/included-concepts/count
router.post('/:sourceKey/included-concepts/count', async (req, res, next) => {
  try {
    const source = getSource(req.params.sourceKey)
    const pool = getPool(req.params.sourceKey)
    const innerSql = buildConceptSetExpressionSql(req.body, source.vocabSchema)
    const result = await pool.request().query(`SELECT COUNT(*) AS cnt FROM (${innerSql}) Q`)
    res.json(result.recordset[0].cnt)
  } catch (err) { next(err) }
})

// POST /included-concepts/count (default source)
router.post('/included-concepts/count', async (req, res, next) => {
  try {
    const source = defaultSource()
    const pool = getPool(source.sourceKey)
    const innerSql = buildConceptSetExpressionSql(req.body, source.vocabSchema)
    const result = await pool.request().query(`SELECT COUNT(*) AS cnt FROM (${innerSql}) Q`)
    res.json(result.recordset[0].cnt)
  } catch (err) { next(err) }
})

// --- single concept ---

// GET /:sourceKey/concept/:id
router.get('/:sourceKey/concept/:id', async (req, res, next) => {
  try {
    const source = getSource(req.params.sourceKey)
    const id = parseInt(req.params.id, 10)
    const querySql = renderSql(
      `SELECT CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT,
       ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID,
       DOMAIN_ID, VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE
       FROM @vocabulary_database_schema.concept WHERE CONCEPT_ID = ${id}`,
      source
    )
    const pool = getPool(req.params.sourceKey)
    const result = await pool.request().query(querySql)
    if (result.recordset.length === 0) return res.status(404).json({ message: 'Concept not found' })
    res.json(mapConcept(result.recordset[0]))
  } catch (err) { next(err) }
})

// GET /concept/:id (default source)
router.get('/concept/:id', async (req, res, next) => {
  try {
    req.params.sourceKey = defaultSourceKey()
    // Re-use the route above by forwarding
    const source = defaultSource()
    const id = parseInt(req.params.id, 10)
    const querySql = renderSql(
      `SELECT CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT,
       ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID,
       DOMAIN_ID, VOCABULARY_ID, VALID_START_DATE, VALID_END_DATE
       FROM @vocabulary_database_schema.concept WHERE CONCEPT_ID = ${id}`,
      source
    )
    const pool = getPool(source.sourceKey)
    const result = await pool.request().query(querySql)
    if (result.recordset.length === 0) return res.status(404).json({ message: 'Concept not found' })
    res.json(mapConcept(result.recordset[0]))
  } catch (err) { next(err) }
})

// --- related concepts ---

// GET /:sourceKey/concept/:id/related
router.get('/:sourceKey/concept/:id/related', async (req, res, next) => {
  try {
    const source = getSource(req.params.sourceKey)
    const id = parseInt(req.params.id, 10)
    const pool = getPool(req.params.sourceKey)
    const request = pool.request()
    request.input('id', sql.Int, id)
    const querySql = renderSql(loadSql('vocabulary/getRelatedConcepts.sql'), source)
    const result = await request.query(querySql)
    res.json(result.recordset.map(mapRelatedConcept))
  } catch (err) { next(err) }
})

// GET /concept/:id/related (default source)
router.get('/concept/:id/related', async (req, res, next) => {
  try {
    const source = defaultSource()
    const id = parseInt(req.params.id, 10)
    const pool = getPool(source.sourceKey)
    const request = pool.request()
    request.input('id', sql.Int, id)
    const querySql = renderSql(loadSql('vocabulary/getRelatedConcepts.sql'), source)
    const result = await request.query(querySql)
    res.json(result.recordset.map(mapRelatedConcept))
  } catch (err) { next(err) }
})

// GET /:sourceKey/concept/:id/descendants
router.get('/:sourceKey/concept/:id/descendants', async (req, res, next) => {
  try {
    const source = getSource(req.params.sourceKey)
    const id = parseInt(req.params.id, 10)
    const pool = getPool(req.params.sourceKey)
    const request = pool.request()
    request.input('id', sql.Int, id)
    const querySql = renderSql(loadSql('vocabulary/getDescendantConcepts.sql'), source)
    const result = await request.query(querySql)
    res.json(result.recordset.map(mapRelatedConcept))
  } catch (err) { next(err) }
})

// GET /concept/:id/descendants (default source)
router.get('/concept/:id/descendants', async (req, res, next) => {
  try {
    const source = defaultSource()
    const id = parseInt(req.params.id, 10)
    const pool = getPool(source.sourceKey)
    const request = pool.request()
    request.input('id', sql.Int, id)
    const querySql = renderSql(loadSql('vocabulary/getDescendantConcepts.sql'), source)
    const result = await request.query(querySql)
    res.json(result.recordset.map(mapRelatedConcept))
  } catch (err) { next(err) }
})

// --- common ancestors ---

// POST /:sourceKey/commonAncestors
router.post('/:sourceKey/commonAncestors', async (req, res, next) => {
  try {
    const source = getSource(req.params.sourceKey)
    const ids = (req.body || []).map(Number).filter(Boolean)
    if (ids.length === 0) return res.json([])
    const pool = getPool(req.params.sourceKey)
    const querySql = renderSql(
      loadSql('vocabulary/getCommonAncestors.sql'),
      source,
      {
        conceptIdentifierList: ids.join(','),
        conceptIdentifierListLength: ids.length
      }
    )
    const result = await pool.request().query(querySql)
    res.json(result.recordset.map(mapRelatedConcept))
  } catch (err) { next(err) }
})

// POST /commonAncestors (default source)
router.post('/commonAncestors', async (req, res, next) => {
  try {
    const source = defaultSource()
    const ids = (req.body || []).map(Number).filter(Boolean)
    if (ids.length === 0) return res.json([])
    const pool = getPool(source.sourceKey)
    const querySql = renderSql(
      loadSql('vocabulary/getCommonAncestors.sql'),
      source,
      {
        conceptIdentifierList: ids.join(','),
        conceptIdentifierListLength: ids.length
      }
    )
    const result = await pool.request().query(querySql)
    res.json(result.recordset.map(mapRelatedConcept))
  } catch (err) { next(err) }
})

// --- domains, vocabularies, info ---

// GET /:sourceKey/domains
router.get('/:sourceKey/domains', async (req, res, next) => {
  try {
    const source = getSource(req.params.sourceKey)
    const rows = await queryVocab(req.params.sourceKey, renderSql(loadSql('vocabulary/getDomains.sql'), source))
    res.json(rows.map(r => ({ domainId: r.DOMAIN_ID, domainName: r.DOMAIN_NAME, domainConceptId: r.DOMAIN_CONCEPT_ID })))
  } catch (err) { next(err) }
})

// GET /domains (default source)
router.get('/domains', async (req, res, next) => {
  try {
    const source = defaultSource()
    const rows = await queryVocab(source.sourceKey, renderSql(loadSql('vocabulary/getDomains.sql'), source))
    res.json(rows.map(r => ({ domainId: r.DOMAIN_ID, domainName: r.DOMAIN_NAME, domainConceptId: r.DOMAIN_CONCEPT_ID })))
  } catch (err) { next(err) }
})

// GET /:sourceKey/vocabularies
router.get('/:sourceKey/vocabularies', async (req, res, next) => {
  try {
    const source = getSource(req.params.sourceKey)
    const rows = await queryVocab(req.params.sourceKey, renderSql(loadSql('vocabulary/getVocabularies.sql'), source))
    res.json(rows.map(r => ({
      vocabularyId: r.VOCABULARY_ID,
      vocabularyName: r.VOCABULARY_NAME,
      vocabularyReference: r.VOCABULARY_REFERENCE,
      vocabularyVersion: r.VOCABULARY_VERSION,
      vocabularyConceptId: r.VOCABULARY_CONCEPT_ID
    })))
  } catch (err) { next(err) }
})

// GET /vocabularies (default source)
router.get('/vocabularies', async (req, res, next) => {
  try {
    const source = defaultSource()
    const rows = await queryVocab(source.sourceKey, renderSql(loadSql('vocabulary/getVocabularies.sql'), source))
    res.json(rows.map(r => ({
      vocabularyId: r.VOCABULARY_ID,
      vocabularyName: r.VOCABULARY_NAME,
      vocabularyReference: r.VOCABULARY_REFERENCE,
      vocabularyVersion: r.VOCABULARY_VERSION,
      vocabularyConceptId: r.VOCABULARY_CONCEPT_ID
    })))
  } catch (err) { next(err) }
})

// GET /:sourceKey/info
router.get('/:sourceKey/info', async (req, res, next) => {
  try {
    const source = getSource(req.params.sourceKey)
    const rows = await queryVocab(req.params.sourceKey, renderSql(loadSql('vocabulary/getInfo.sql'), source))
    const version = rows.length > 0 ? rows[0].VOCABULARY_VERSION : null
    res.json({ vocabularyVersion: version, dialect: 'sql server' })
  } catch (err) { next(err) }
})

// GET /info (default source) — NOTE: conflicts with the top-level /info route.
// This is mounted as /vocabulary so it becomes /vocabulary/info.
router.get('/info', async (req, res, next) => {
  try {
    const source = defaultSource()
    const rows = await queryVocab(source.sourceKey, renderSql(loadSql('vocabulary/getInfo.sql'), source))
    const version = rows.length > 0 ? rows[0].VOCABULARY_VERSION : null
    res.json({ vocabularyVersion: version, dialect: 'sql server' })
  } catch (err) { next(err) }
})

// POST /conceptSetExpressionSQL — not implemented (CIRCE dependency for cohort SQL)
router.post('/conceptSetExpressionSQL', (_req, res) => res.sendStatus(501))

export default router
