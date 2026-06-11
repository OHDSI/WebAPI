import './db.js' // run migrations on startup
import { initSources } from './sources.js'
import config from './config.js'
import app from './app.js'

initSources().then(() => {
  const server = app.listen(config.port, config.host, () => {
    console.log(`WebAPI listening on http://${config.host}:${config.port}`)
  })
  server.on('error', err => { console.error('Server error:', err.message); process.exit(1) })
})
