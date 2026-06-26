import { useState, useEffect } from 'react'
import { api } from '../services/api'
import type { Event } from '../types'

const mockEvents: Event[] = [
  { eventId: 1, name: 'Banjir Cisarua', location: 'Kecamatan Cisarua, Kabupaten Bogor', status: 'ACTIVE', description: 'Banjir akibat curah hujan tinggi', shelterCount: 8 },
  { eventId: 2, name: 'Longsor Megamendung', location: 'Kecamatan Megamendung, Kabupaten Bogor', status: 'ACTIVE', description: 'Tanah longsor di area perbukitan', shelterCount: 5 },
  { eventId: 3, name: 'Angin Puting Beliung Cipayung', location: 'Kecamatan Cipayung, Kabupaten Bogor', status: 'CLOSED', description: 'Kerusakan akibat angin kencang', shelterCount: 3 },
  { eventId: 4, name: 'Gempa Cianjur', location: 'Kabupaten Cianjur', status: 'MONITORING', description: 'Gempa bumi 5.6 SR', shelterCount: 12 },
]

export default function Events() {
  const [events, setEvents] = useState<Event[]>([])
  const [search, setSearch] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [selectedEvent, setSelectedEvent] = useState<Event | null>(null)
  const [form, setForm] = useState({ name: '', location: '', status: 'ACTIVE' as Event['status'], description: '', shelterCount: 0 })

  useEffect(() => {
    const fetch = async () => {
      try { setEvents(await api.getEvents()) } catch { setEvents(mockEvents) }
    }
    fetch()
  }, [])

  const filtered = events.filter(e =>
    e.name.toLowerCase().includes(search.toLowerCase()) ||
    e.location.toLowerCase().includes(search.toLowerCase())
  )

  const activeEvents = events.filter(e => e.status === 'ACTIVE').length
  const totalShelters = events.reduce((s, e) => s + e.shelterCount, 0)
  const totalRefugees = activeEvents * 350

  const openDrawer = (e: Event | null) => {
    setSelectedEvent(e)
    setForm(e ? { name: e.name, location: e.location, status: e.status, description: e.description, shelterCount: e.shelterCount } : { name: '', location: '', status: 'ACTIVE', description: '', shelterCount: 0 })
    setDrawerOpen(true)
  }

  const handleSave = async () => {
    const payload = { ...form } as Partial<Event>
    if (selectedEvent) payload.eventId = selectedEvent.eventId
    try {
      const saved = await api.saveEvent(payload)
      setEvents(prev => {
        const idx = prev.findIndex(e => e.eventId === saved.eventId)
        if (idx >= 0) { const c = [...prev]; c[idx] = saved; return c }
        return [...prev, saved]
      })
    } catch {
      const id = selectedEvent?.eventId || Date.now()
      const mock = { ...form, eventId: id } as Event
      setEvents(prev => {
        const idx = prev.findIndex(e => e.eventId === id)
        if (idx >= 0) { const c = [...prev]; c[idx] = mock; return c }
        return [...prev, mock]
      })
    }
    setDrawerOpen(false)
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Hapus event ini?')) return
    try { await api.deleteEvent(id) } catch {}
    setEvents(prev => prev.filter(e => e.eventId !== id))
    setDrawerOpen(false)
  }

  return (
    <>
      <div className="kpi-row">
        <div className="card card-padded kpi-card">
          <div><div className="kpi-title">Event Aktif</div><div className="kpi-value">{activeEvents}</div><div className="kpi-subtitle">Sedang berlangsung</div></div>
        </div>
        <div className="card card-padded kpi-card">
          <div><div className="kpi-title">Total Shelter Terlibat</div><div className="kpi-value">{totalShelters}</div><div className="kpi-subtitle">Tersebar di semua event</div></div>
        </div>
        <div className="card card-padded kpi-card">
          <div><div className="kpi-title">Pengungsi Terdata</div><div className="kpi-value">{totalRefugees}</div><div className="kpi-subtitle">Perkiraan jiwa</div></div>
        </div>
      </div>

      <div className="search-bar mb-20">
        <input className="search-input" placeholder="Cari event..." value={search} onChange={e => setSearch(e.target.value)} />
        <button className="btn btn-primary" onClick={() => openDrawer(null)}>Tambah Event Baru</button>
      </div>

      <div className="panel-body">
        <div className="panel-list">
          <div className="cards-grid">
            {filtered.map(e => (
              <div className="card data-card" key={e.eventId} onClick={() => openDrawer(e)}>
                <div className="data-card-title">{e.name}</div>
                <div className="data-card-subtitle">{e.location}</div>
                <span className={`badge ${e.status === 'ACTIVE' ? 'badge-active' : e.status === 'CLOSED' ? 'badge-closed' : 'badge-warning'}`} style={{ marginBottom: 8, display: 'inline-block' }}>{e.status}</span>
                <div className="data-card-row">
                  <span className="data-card-label">Shelter</span>
                  <span className="data-card-value">{e.shelterCount}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {drawerOpen && (
          <div className="card card-padded panel-drawer">
            <div className="drawer-title">{selectedEvent ? 'Edit Event' : 'Tambah Event Baru'}</div>
            <div className="form-group">
              <label className="form-label">Nama Event</label>
              <input className="input" value={form.name} onChange={e => setForm(p => ({ ...p, name: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Lokasi</label>
              <input className="input" value={form.location} onChange={e => setForm(p => ({ ...p, location: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Status</label>
              <select className="select" value={form.status} onChange={e => setForm(p => ({ ...p, status: e.target.value as Event['status'] }))}>
                <option value="ACTIVE">ACTIVE</option>
                <option value="MONITORING">MONITORING</option>
                <option value="CLOSED">CLOSED</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Deskripsi</label>
              <textarea className="textarea" value={form.description} onChange={e => setForm(p => ({ ...p, description: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Jumlah Shelter</label>
              <input className="input" type="number" value={form.shelterCount} onChange={e => setForm(p => ({ ...p, shelterCount: Number(e.target.value) }))} />
            </div>
            <hr className="drawer-separator" />
            <div className="drawer-btn-row">
              <button className="btn btn-primary flex-1" onClick={handleSave}>Simpan</button>
              {selectedEvent && <button className="btn btn-danger" onClick={() => handleDelete(selectedEvent.eventId)}>Hapus</button>}
              <button className="btn btn-outline" onClick={() => setDrawerOpen(false)}>Batal</button>
            </div>
          </div>
        )}
      </div>
    </>
  )
}
