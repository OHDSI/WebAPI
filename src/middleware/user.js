import config from '../config.js'

export default function userMiddleware (req, res, next) {
  const login = req.headers[config.authHeader] || 'anonymous'
  req.user = { login, name: login }
  next()
}
