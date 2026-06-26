import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../services/api'

export default function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const user = await api.login(username, password)
      localStorage.setItem('kepo_user', JSON.stringify(user))
      navigate('/app/dashboard')
    } catch {
      try {
        const mock = { userId: 1, username: 'admin', fullName: 'Administrator', role: 'ADMIN' as const }
        if (username === 'admin' && password === 'admin') {
          localStorage.setItem('kepo_user', JSON.stringify(mock))
          navigate('/app/dashboard')
        } else {
          setError('Username atau password salah')
        }
      } catch {
        setError('Username atau password salah')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-card card card-padded">
        <h1>KEPO</h1>
        <p className="subtitle">Kendali Evakuasi & Pengelolaan Operasional Bencana</p>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Username</label>
            <input className="input" type="text" placeholder="Username" value={username} onChange={e => setUsername(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Password</label>
            <input className="input" type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} required />
          </div>
          {error && <p className="error-text">{error}</p>}
          <button className="btn btn-primary login-btn" type="submit" disabled={loading}>
            {loading ? 'MEMPROSES...' : 'MASUK'}
          </button>
        </form>
        <p className="login-hint">Demo: admin / admin</p>
      </div>
    </div>
  )
}
