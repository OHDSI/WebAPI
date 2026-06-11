import { Router } from 'express'

const router = Router()

// Returns the current user derived from the auth header
router.get('/user/me', (req, res) => {
  const { login, name } = req.user
  res.json({
    id: 1,
    login,
    name,
    // Grant all permissions — access control is the proxy's responsibility
    permissions: [],
    permissionIdx: {}
  })
})

// User list — stub; Atlas needs this to not crash on admin screens
router.get('/user', (req, res) => {
  res.json([])
})

// Role endpoints — minimal stubs
router.get('/role', (req, res) => res.json([]))

router.get('/role/:id', (req, res) => {
  res.status(404).json({ message: 'Not found' })
})

router.post('/role', (req, res) => {
  res.status(501).json({ message: 'Role management not implemented' })
})

router.get('/role/:id/permissions', (req, res) => res.json([]))

router.get('/role/:id/users', (req, res) => res.json([]))

router.get('/user/:userId/permissions', (req, res) => res.json([]))

router.get('/user/:userId/roles', (req, res) => res.json([]))

export default router
