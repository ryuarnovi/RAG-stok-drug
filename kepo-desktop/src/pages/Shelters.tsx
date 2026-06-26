import { useState, useEffect } from 'react'
import { api } from '../services/api'
import type { Shelter, Event } from '../types'

const mockShelters: Shelter[] = [
  { shelterId: 1, name: 'Posko Cisarua', location: 'Cisarua', capacity: 500, currentOccupancy: 475, penanggungJawab: 'Ahmad Fauzi', status: 'WARNING', eventId: 1, avgAvailability: 65 },
  { shelterId: 2, name: 'Posko Megamendung', location: 'Megamendung', capacity: 300, currentOccupancy: 200, penanggungJawab: 'Rina Marlina', status: 'SAFE', eventId: 1, avgAvailability: 80 },
  { shelterId: 3, name: 'Posko Cipayung', location: 'Cipayung', capacity: 400, currentOccupancy: 390, penanggungJawab: 'Dedi Kurniawan', status: 'CRITICAL', eventId: 2, avgAvailability: 30 },
  { shelterId: 4, name: 'Posko Cisarua II', location: 'Cisarua', capacity: 250, currentOccupancy: 120, penanggungJawab: 'Fitri Handayani', status: 'SAFE', eventId: 1, avgAvailability: 90 },
]
const mockEvents: Event[] = [
  { eventId: 1, name: 'Banjir Cisarua', location: 'Cisarua', status: 'ACTIVE', description: '', shelterCount: 3 },
  { eventId: 2, name: 'Longsor Megamendung', location: 'Megamendung', status: 'ACTIVE', description: '', shelterCount: 1 },
]

export default function Shelters() {
  const [shelters, setShelters] = useState<Shelter[]>([])
  const [events, setEvents] = useState<Event[]>([])
  const [search, setSearch] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [selectedShelter, setSelectedShelter] = useState<Shelter | null>(null)
  const [form, setForm] = useState({ name: '', location: '', capacity: 0, currentOccupancy: 0, penanggungJawab: '', eventId: 0 })

  useEffect(() => {
    const fetch = async () => {
      try { setShelters(await api.getShelters()) } catch { setShelters(mockShelters) }
      try { setEvents(await api.getEvents()) } catch { setEvents(mockEvents) }
    }
    fetch()
  }, [])

  const filtered = shelters.filter(s =>
    s.name.toLowerCase().includes(search.toLowerCase()) ||
    s.location.toLowerCase().includes(search.toLowerCase())
  )

  const openDrawer = (s: Shelter | null) => {
    setSelectedShelter(s)
    setForm(s ? { name: s.name, location: s.location, capacity: s.capacity, currentOccupancy: s.currentOccupancy, penanggungJawab: s.penanggungJawab, eventId: s.eventId || 0 } : { name: '', location: '', capacity: 0, currentOccupancy: 0, penanggungJawab: '', eventId: 0 })
    setDrawerOpen(true)
  }

  const handleSave = async () => {
    const payload = { ...form } as Partial<Shelter>
    if (selectedShelter) payload.shelterId = selectedShelter.shelterId
    try {
      const saved = await api.saveShelter(payload)
      setShelters(prev => {
        const idx = prev.findIndex(s => s.shelterId === saved.shelterId)
        if (idx >= 0) { const c = [...prev]; c[idx] = saved; return c }
        return [...prev, saved]
      })
    } catch {
      const id = selectedShelter?.shelterId || Date.now()
      const ratio = form.capacity > 0 ? form.currentOccupancy / form.capacity : 0
      const status = ratio >= 0.9 ? 'CRITICAL' : ratio >= 0.7 ? 'WARNING' : 'SAFE'
      const mock = { ...form, shelterId: id, status, avgAvailability: form.capacity > 0 ? Math.round((1 - form.currentOccupancy / form.capacity) * 100) : 100 } as Shelter
      setShelters(prev => {
        const idx = prev.findIndex(s => s.shelterId === id)
        if (idx >= 0) { const c = [...prev]; c[idx] = mock; return c }
        return [...prev, mock]
      })
    }
    setDrawerOpen(false)
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Hapus shelter ini?')) return
    try { await api.deleteShelter(id) } catch {}
    setShelters(prev => prev.filter(s => s.shelterId !== id))
    setDrawerOpen(false)
  }

  return (
    <>
      <div className="search-bar mb-20">
        <input className="search-input" placeholder="Cari shelter..." value={search} onChange={e => setSearch(e.target.value)} />
        <button className="btn btn-primary" onClick={() => openDrawer(null)}>Tambah Shelter Baru</button>
      </div>

      <div className="panel-body">
        <div className="panel-list">
          <div className="cards-grid">
            {filtered.map(s => {
              const density = s.capacity > 0 ? Math.round((s.currentOccupancy / s.capacity) * 100) : 0
              const logistik = s.avgAvailability ?? 50
              const badgeClass = s.status === 'CRITICAL' ? 'badge-critical' : s.status === 'WARNING' ? 'badge-warning' : 'badge-safe'
              const fillColor = s.status === 'CRITICAL' ? 'var(--danger)' : s.status === 'WARNING' ? 'var(--warning)' : 'var(--secondary)'
              const event = events.find(e => e.eventId === s.eventId)
              return (
                <div className="card data-card" key={s.shelterId} onClick={() => openDrawer(s)}>
                  <div className="data-card-title">{s.name}</div>
                  <div className="data-card-subtitle">{s.location}</div>
                  <div style={{ marginBottom: 8 }}>
                    <span className={`badge ${badgeClass}`}>{s.status}</span>
                    {event && <span className="badge badge-info" style={{ marginLeft: 6 }}>{event.name}</span>}
                  </div>
                  <div className="data-card-row">
                    <span className="data-card-label">Penanggung Jawab</span>
                    <span className="data-card-value">{s.penanggungJawab}</span>
                  </div>
                  <div style={{ marginTop: 10 }}>
                    <div className="flex justify-between" style={{ marginBottom: 4 }}>
                      <span className="data-card-label">Kepadatan ({density}%)</span>
                      <span className="data-card-label">{s.currentOccupancy}/{s.capacity}</span>
                    </div>
                    <div className="progress-bar"><div className="progress-fill" style={{ width: `${Math.min(density, 100)}%`, background: fillColor }} /></div>
                  </div>
                  <div style={{ marginTop: 8 }}>
                    <div className="flex justify-between" style={{ marginBottom: 4 }}>
                      <span className="data-card-label">Logistik ({logistik}%)</span>
                    </div>
                    <div className="progress-bar"><div className="progress-fill" style={{ width: `${Math.min(logistik, 100)}%`, background: 'var(--primary-light)' }} /></div>
                  </div>
                </div>
              )
            })}
          </div>
        </div>

        {drawerOpen && (
          <div className="card card-padded panel-drawer">
            <div className="drawer-title">{selectedShelter ? 'Edit Shelter' : 'Tambah Shelter Baru'}</div>
            <div className="form-group">
              <label className="form-label">Nama Shelter</label>
              <input className="input" value={form.name} onChange={e => setForm(p => ({ ...p, name: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Lokasi</label>
              <input className="input" value={form.location} onChange={e => setForm(p => ({ ...p, location: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Kapasitas</label>
              <input className="input" type="number" value={form.capacity} onChange={e => setForm(p => ({ ...p, capacity: Number(e.target.value) }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Terisi</label>
              <input className="input" type="number" value={form.currentOccupancy} onChange={e => setForm(p => ({ ...p, currentOccupancy: Number(e.target.value) }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Penanggung Jawab</label>
              <input className="input" value={form.penanggungJawab} onChange={e => setForm(p => ({ ...p, penanggungJawab: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Event</label>
              <select className="select" value={form.eventId} onChange={e => setForm(p => ({ ...p, eventId: Number(e.target.value) }))}>
                <option value={0}>Pilih Event</option>
                {events.map(e => <option key={e.eventId} value={e.eventId}>{e.name}</option>)}
              </select>
            </div>
            <hr className="drawer-separator" />
            <div className="drawer-btn-row">
              <button className="btn btn-primary flex-1" onClick={handleSave}>Simpan</button>
              {selectedShelter && <button className="btn btn-danger" onClick={() => handleDelete(selectedShelter.shelterId)}>Hapus</button>}
              <button className="btn btn-outline" onClick={() => setDrawerOpen(false)}>Batal</button>
            </div>
          </div>
        )}
      </div>
    </>
  )
}
