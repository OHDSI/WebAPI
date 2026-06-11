const sources = JSON.parse(process.env.WEBAPI_SOURCES || '[]')

if (sources.length === 0) {
  console.warn('Warning: WEBAPI_SOURCES is empty — no CDM sources available')
}

const config = Object.freeze({
  port: parseInt(process.env.PORT || '8080', 10),
  host: process.env.HOST || '0.0.0.0',
  dbPath: process.env.DB_PATH || '/data/webapi.db',
  authHeader: process.env.WEBAPI_AUTH_HEADER || 'x-forwarded-user',
  version: process.env.WEBAPI_VERSION || '2.15.1',
  sources
})

export default config
