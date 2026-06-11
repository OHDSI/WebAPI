import sql from 'mssql'
import config from './config.js'

// One mssql connection pool per source, keyed by sourceKey
const pools = new Map()

function buildPoolConfig (source) {
  return {
    server: source.server,
    port: source.port || 1433,
    database: source.database,
    user: source.username,
    password: source.password,
    connectionString: source.connectionString || undefined,
    options: {
      encrypt: source.encrypt !== false,
      trustServerCertificate: source.trustServerCertificate || false,
      enableArithAbort: true
    },
    pool: {
      max: 10,
      min: 0,
      idleTimeoutMillis: 30000
    }
  }
}

// Eagerly open all pools on startup
export async function initSources () {
  for (const source of config.sources) {
    try {
      const pool = source.connectionString
        ? await new sql.ConnectionPool(source.connectionString).connect()
        : await new sql.ConnectionPool(buildPoolConfig(source)).connect()
      pools.set(source.sourceKey, pool)
      console.log(`Connected to source: ${source.sourceKey}`)
    } catch (err) {
      console.error(`Failed to connect to source ${source.sourceKey}: ${err.message}`)
    }
  }
}

// Get a connected pool by sourceKey; throws if not found
export function getPool (sourceKey) {
  const pool = pools.get(sourceKey)
  if (!pool) throw Object.assign(new Error(`Unknown source: ${sourceKey}`), { status: 404 })
  return pool
}

// Get source config object by sourceKey; throws if not found
export function getSource (sourceKey) {
  const source = config.sources.find(s => s.sourceKey === sourceKey)
  if (!source) throw Object.assign(new Error(`Unknown source: ${sourceKey}`), { status: 404 })
  return source
}

// Build the Atlas-compatible SourceInfo shape for one source
export function toSourceInfo (source, index) {
  const id = index + 1
  const daimonBase = id * 10
  const daimons = []

  if (source.cdmSchema) {
    daimons.push({ sourceDaimonId: daimonBase + 1, daimonType: 'CDM', tableQualifier: source.cdmSchema, priority: 0 })
  }
  if (source.vocabSchema) {
    daimons.push({ sourceDaimonId: daimonBase + 2, daimonType: 'Vocabulary', tableQualifier: source.vocabSchema, priority: 0 })
  }
  if (source.resultsSchema) {
    daimons.push({ sourceDaimonId: daimonBase + 3, daimonType: 'Results', tableQualifier: source.resultsSchema, priority: 0 })
  }
  if (source.tempSchema) {
    daimons.push({ sourceDaimonId: daimonBase + 4, daimonType: 'Temp', tableQualifier: source.tempSchema, priority: 0 })
  }

  return {
    sourceId: id,
    sourceName: source.sourceName,
    sourceKey: source.sourceKey,
    sourceDialect: 'sql server',
    daimons
  }
}

export function getAllSourceInfos () {
  return config.sources.map((s, i) => toSourceInfo(s, i))
}
