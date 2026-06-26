import { useState, useEffect } from 'react'
import { api } from '../services/api'
import type { User } from '../types'

const mockUsers: User[] = [
  { userId: 1, username: 'admin', fullName: 'Administrator', role: 'ADMIN' },
  { userId: 2, username: 'shelter1', fullName: 'Petugas Shelter Cisarua', role: 'SHELTER_OFFICER' },
  { userId: 3, username: 'health1', fullName: 'Petugas Kesehatan', role: 'HEALTH_OFFICER' },
]

export default function Settings() {
  const [users, setUsers] = useState<User[]>([])
  const [search, setSearch] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [selectedUser, setSelectedUser] = useState<User | null>(null)
  const [form, setForm] = useState({ username: '', password: '', fullName: '', role: 'SHELTER_OFFICER' as User['role'] })
  const currentUser: User | null = (() => {
    try { return JSON.parse(localStorage.getItem('kepo_user') || 'null') } catch { return null }
  })()

  useEffect(() => {
    const fetch = async () => {
      try { setUsers(await api.getUsers()) } catch { setUsers(mockUsers) }
    }
    fetch()
  }, [])

  const filtered = users.filter(u =>
    u.fullName.toLowerCase().includes(search.toLowerCase()) ||
    u.username.toLowerCase().includes(search.toLowerCase())
  )

  const openDrawer = (u: User | null) => {
    setSelectedUser(u)
    setForm(u ? { username: u.username, password: '', fullName: u.fullName, role: u.role } : { username: '', password: '', fullName: '', role: 'SHELTER_OFFICER' })
    setDrawerOpen(true)
  }

  const handleSave = async () => {
    const payload = { username: form.username, fullName: form.fullName, role: form.role } as Partial<User> & { password?: string }
    if (form.password) payload.password = form.password
    if (selectedUser) payload.userId = selectedUser.userId
    try {
      const saved = await api.saveUser(payload)
      setUsers(prev => {
        const idx = prev.findIndex(u => u.userId === saved.userId)
        if (idx >= 0) { const c = [...prev]; c[idx] = saved; return c }
        return [...prev, saved]
      })
    } catch {
      const id = selectedUser?.userId || Date.now()
      const mock = { userId: id, username: form.username, fullName: form.fullName, role: form.role } as User
      setUsers(prev => {
        const idx = prev.findIndex(u => u.userId === id)
        if (idx >= 0) { const c = [...prev]; c[idx] = mock; return c }
        return [...prev, mock]
      })
    }
    setDrawerOpen(false)
  }

  const handleDelete = async (id: number) => {
    if (id === currentUser?.userId) { alert('Tidak dapat menghapus akun sendiri!'); return }
    if (!confirm('Hapus operator ini?')) return
    try { await api.deleteUser(id) } catch {}
    setUsers(prev => prev.filter(u => u.userId !== id))
    setDrawerOpen(false)
  }

  const roleBadge = (role: string) => {
    const map: Record<string, string> = { ADMIN: 'badge-critical', SHELTER_OFFICER: 'badge-info', HEALTH_OFFICER: 'badge-safe', FIELD_COORDINATOR: 'badge-warning' }
    return map[role] || 'badge-primary'
  }

  const isAdmin = currentUser?.role === 'ADMIN'

  return (
    <>
      <h2 style={{ fontFamily: "'Plus Jakarta Sans', sans-serif", fontSize: 22, fontWeight: 700, marginBottom: 20 }}>Pengaturan Sistem & Manajemen Pengguna</h2>

      {!isAdmin && (
        <div className="card card-padded mb-20" style={{ borderLeft: '4px solid var(--warning)' }}>
          <p style={{ fontSize: 13, color: 'var(--on-surface-variant)' }}>Anda login sebagai {currentUser?.fullName} ({currentUser?.role}). Beberapa fitur admin terbatas.</p>
        </div>
      )}

      <div className="search-bar mb-20">
        <input className="search-input" placeholder="Cari operator..." value={search} onChange={e => setSearch(e.target.value)} />
        {isAdmin && <button className="btn btn-primary" onClick={() => openDrawer(null)}>Tambah Operator</button>}
      </div>

      <div className="panel-body">
        <div className="panel-list">
          <div className="cards-grid">
            {filtered.map(u => (
              <div className="card data-card" key={u.userId} onClick={() => isAdmin && openDrawer(u)} style={isAdmin ? {} : { cursor: 'default' }}>
                <div className="data-card-title">{u.fullName}</div>
                <div className="data-card-subtitle">@{u.username}</div>
                <div style={{ marginTop: 8 }}><span className={`badge ${roleBadge(u.role)}`}>{u.role}</span></div>
              </div>
            ))}
          </div>
        </div>

        {drawerOpen && isAdmin && (
          <div className="card card-padded panel-drawer">
            <div className="drawer-title">{selectedUser ? 'Edit Operator' : 'Tambah Operator'}</div>
            <div className="form-group">
              <label className="form-label">Username</label>
              <input className="input" value={form.username} onChange={e => setForm(p => ({ ...p, username: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Password</label>
              <input className="input" type="password" placeholder={selectedUser ? 'Kosongkan jika tidak diubah' : ''} value={form.password} onChange={e => setForm(p => ({ ...p, password: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Nama Lengkap</label>
              <input className="input" value={form.fullName} onChange={e => setForm(p => ({ ...p, fullName: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Hak Akses</label>
              <select className="select" value={form.role} onChange={e => setForm(p => ({ ...p, role: e.target.value as User['role'] }))}>
                <option value="ADMIN">ADMIN</option>
                <option value="SHELTER_OFFICER">SHELTER_OFFICER</option>
                <option value="HEALTH_OFFICER">HEALTH_OFFICER</option>
                <option value="FIELD_COORDINATOR">FIELD_COORDINATOR</option>
              </select>
            </div>
            <hr className="drawer-separator" />
            <div className="drawer-btn-row">
              <button className="btn btn-primary flex-1" onClick={handleSave}>Simpan</button>
              {selectedUser && <button className="btn btn-danger" onClick={() => handleDelete(selectedUser.userId)}>Hapus</button>}
              <button className="btn btn-outline" onClick={() => setDrawerOpen(false)}>Batal</button>
            </div>
          </div>
        )}
      </div>
    </>
  )
}
