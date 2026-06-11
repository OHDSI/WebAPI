import { Router } from 'express'
import { readFileSync } from 'fs'
import { fileURLToPath } from 'url'
import { join, dirname } from 'path'
import { getSource } from '../sources.js'

const __dirname = dirname(fileURLToPath(import.meta.url))
const SQL_DIR = join(__dirname, '../sql/person')

function readSql (name) {
  return readFileSync(join(SQL_DIR, name), 'utf8')
}

function renderSql (sql, tableQualifier, extraParams = {}) {
  const params = { tableQualifier, ...extraParams }
  return Object.entries(params).reduce((s, [k, v]) => {
    return s.replace(new RegExp(`@${k}\\b`, 'gi'), v ?? '')
  }, sql)
}

// Person profile route is mounted at /:sourceKey/person by app.js.
// The params injected by Express include :sourceKey from the parent mount.
const router = Router({ mergeParams: true })

router.get('/:personId', async (req, res, next) => {
  const { sourceKey, personId } = req.params
  const source = getSource(sourceKey)
  if (!source) return res.status(404).json({ message: `Source not found: ${sourceKey}` })

  try {
    const pool = source.pool
    const tq = source.cdmSchema

    // Person demographics
    const infoSql = renderSql(readSql('personInfo.sql'), tq, { personId })
    const infoResult = await pool.request().query(infoSql)
    const personRow = infoResult.recordset[0]
    if (!personRow) return res.status(404).json({ message: `Person not found: ${personId}` })

    // Observation periods
    const opSql = renderSql(readSql('getObservationPeriods.sql'), tq, { personId })
    const opResult = await pool.request().query(opSql)

    // All clinical records (conditions, drugs, visits, etc.)
    const recSql = renderSql(readSql('getRecords.sql'), tq, { personId })
    const recResult = await pool.request().query(recSql)

    const profile = {
      gender: personRow.gender || personRow.GENDER || '',
      yearOfBirth: personRow.year_of_birth || personRow.YEAR_OF_BIRTH || 0,
      observationPeriods: opResult.recordset.map(r => ({
        id: r.observation_period_id ?? r.OBSERVATION_PERIOD_ID,
        startDate: r.start_date ?? r.START_DATE,
        endDate: r.end_date ?? r.END_DATE,
        type: r.observation_period_type ?? r.OBSERVATION_PERIOD_TYPE
      })),
      records: recResult.recordset.map(r => ({
        conceptId: r.concept_id ?? r.CONCEPT_ID,
        conceptName: r.concept_name ?? r.CONCEPT_NAME,
        domain: r.domain ?? r.DOMAIN,
        startDate: r.start_date ?? r.START_DATE,
        endDate: r.end_date ?? r.END_DATE
      }))
    }

    res.json(profile)
  } catch (err) {
    next(err)
  }
})

export default router
