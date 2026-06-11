// eslint-disable-next-line no-unused-vars
export default function errorHandler (err, req, res, next) {
  const status = err.status || 500
  if (status >= 500) console.error(err)
  res.status(status).json({ message: err.message || 'Internal Server Error' })
}
