import { readFileSync } from 'fs'
import { fileURLToPath } from 'url'
import path from 'path'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const SQL_DIR = path.join(__dirname, 'sql')

// Load a SQL template file relative to src/sql/
export function loadSql (relativePath) {
  return readFileSync(path.join(SQL_DIR, relativePath), 'utf8')
}

// Replace @param tokens with values, then inject CDM/vocab/results/temp qualifiers.
// Schema identifiers are substituted directly (not as bind params) because they are
// identifiers, not data. User data values must be bound separately via mssql params.
export function renderSql (sql, source, extraParams = {}) {
  const params = {
    cdm_database_schema: source.cdmSchema,
    vocabulary_database_schema: source.vocabSchema,
    vocab_database_schema: source.vocabSchema,   // alias used by cdmresults SQL
    results_database_schema: source.resultsSchema,
    temp_database_schema: source.tempSchema,
    ...extraParams
  }

  return Object.entries(params).reduce((s, [key, value]) => {
    // Replace @key (whole-word, case-insensitive)
    return s.replace(new RegExp(`@${key}\\b`, 'gi'), value ?? '')
  }, sql)
}
