import { useState, useEffect } from 'react'
import { api } from '../services/api'
import type { Refugee, Shelter, RefugeeMovement } from '../types'

const mockRefugees: Refugee[] = [
  { refugeeId: 1, name: 'Ahmad Rizki', nik: '3201010101990001', age: 27, gender: 'Laki-laki', shelterId: 1, shelterName: 'Posko Cisarua', status: 'CHECKED_IN', priorityStatus: 'REGULAR', familyCode: 'FAM-001', medicalNotes: 'Hipertensi ringan' },
  { refugeeId: 2, name: 'Siti Nurhaliza', nik: '3201010202990002', age: 25, gender: 'Perempuan', shelterId: 1, shelterName: 'Posko Cisarua', status: 'CHECKED_IN', priorityStatus: 'IBU_HAMIL', familyCode: 'FAM-001', medicalNotes: 'Ibu hamil 6 bulan' },
  { refugeeId: 3, name: 'Bambang Suprapto', nik: '3201010303990003', age: 65, gender: 'Laki-laki', shelterId: 2, shelterName: 'Posko Megamendung', status: 'CHECKED_IN', priorityStatus: 'LANSIA', familyCode: 'FAM-002' },
  { refugeeId: 4, name: 'Dewi Sartika', nik: '3201010404990004', age: 35, gender: 'Perempuan', shelterId: 3, shelterName: 'Posko Cipayung', status: 'CHECKED_OUT', priorityStatus: 'DISABILITAS', familyCode: 'FAM-003' },
]
const mockShelters: Shelter[] = [
  { shelterId: 1, name: 'Posko Cisarua', location: 'Cisarua', capacity: 500, currentOccupancy: 475, penanggungJawab: 'Ahmad', status: 'WARNING' },
  { shelterId: 2, name: 'Posko Megamendung', location: 'Megamendung', capacity: 300, currentOccupancy: 200, penanggungJawab: 'Rina', status: 'SAFE' },
  { shelterId: 3, name: 'Posko Cipayung', location: 'Cipayung', capacity: 400, currentOccupancy: 390, penanggungJawab: 'Dedi', status: 'WARNING' },
]
const mockHistory: RefugeeMovement[] = [
  { movementId: 1, refugeeId: 1, fromShelterName: 'Posko Induk', toShelterName: 'Posko Cisarua', movedBy: 'Admin', movedAt: '2026-06-20T08:00:00Z', notes: 'Pemindahan awal' },
]

export default function Refugees() {
  const [refugees, setRefugees] = useState<Refugee[]>([])
  const [shelters, setShelters] = useState<Shelter[]>([])
  const [search, setSearch] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [selectedRefugee, setSelectedRefugee] = useState<Refugee | null>(null)
  const [history, setHistory] = useState<RefugeeMovement[]>([])
  const [form, setForm] = useState({ name: '', nik: '', age: 0, gender: 'Laki-laki', priorityStatus: 'REGULAR', familyCode: '', shelterId: 0, status: 'CHECKED_IN' as Refugee['status'], medicalNotes: '' })
  const [transferTarget, setTransferTarget] = useState(0)
  const [transferNotes, setTransferNotes] = useState('')

  useEffect(() => {
    const fetch = async () => {
      try { setRefugees(await api.getRefugees()) } catch { setRefugees(mockRefugees) }
      try { setShelters(await api.getShelters()) } catch { setShelters(mockShelters) }
    }
    fetch()
  }, [])

  const filtered = refugees.filter(r =>
    r.name.toLowerCase().includes(search.toLowerCase()) ||
    r.nik.includes(search)
  )

  const openDrawer = async (r: Refugee | null) => {
    setSelectedRefugee(r)
    setForm(r ? { name: r.name, nik: r.nik, age: r.age, gender: r.gender, priorityStatus: r.priorityStatus, familyCode: r.familyCode || '', shelterId: r.shelterId || 0, status: r.status, medicalNotes: r.medicalNotes || '' } : { name: '', nik: '', age: 0, gender: 'Laki-laki', priorityStatus: 'REGULAR', familyCode: '', shelterId: 0, status: 'CHECKED_IN', medicalNotes: '' })
    setTransferTarget(0)
    setTransferNotes('')
    setDrawerOpen(true)
    if (r) {
      try { setHistory(await api.getRefugeeMovementHistory(r.refugeeId)) } catch { setHistory(mockHistory) }
    } else {
      setHistory([])
    }
  }

  const handleSave = async () => {
    const payload = { ...form } as Partial<Refugee>
    if (selectedRefugee) payload.refugeeId = selectedRefugee.refugeeId
    try {
      const saved = await api.saveRefugee(payload)
      setRefugees(prev => {
        const idx = prev.findIndex(r => r.refugeeId === saved.refugeeId)
        if (idx >= 0) { const c = [...prev]; c[idx] = saved; return c }
        return [...prev, saved]
      })
    } catch {
      const id = selectedRefugee?.refugeeId || Date.now()
      const shelter = shelters.find(s => s.shelterId === form.shelterId)
      const mock = { ...form, refugeeId: id, shelterName: shelter?.name || '' } as Refugee
      setRefugees(prev => {
        const idx = prev.findIndex(r => r.refugeeId === id)
        if (idx >= 0) { const c = [...prev]; c[idx] = mock; return c }
        return [...prev, mock]
      })
    }
    setDrawerOpen(false)
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Hapus data pengungsi ini?')) return
    try { await api.deleteRefugee(id) } catch {}
    setRefugees(prev => prev.filter(r => r.refugeeId !== id))
    setDrawerOpen(false)
  }

  const handleCheckIn = async () => {
    if (!selectedRefugee || !form.shelterId) return
    try { await api.checkInRefugee(selectedRefugee.refugeeId, form.shelterId) } catch {}
    setRefugees(prev => prev.map(r => r.refugeeId === selectedRefugee.refugeeId ? { ...r, status: 'CHECKED_IN', shelterId: form.shelterId, shelterName: shelters.find(s => s.shelterId === form.shelterId)?.name } : r))
    setDrawerOpen(false)
  }

  const handleCheckOut = async () => {
    if (!selectedRefugee) return
    try { await api.checkOutRefugee(selectedRefugee.refugeeId) } catch {}
    setRefugees(prev => prev.map(r => r.refugeeId === selectedRefugee.refugeeId ? { ...r, status: 'CHECKED_OUT', shelterId: undefined, shelterName: undefined } : r))
    setDrawerOpen(false)
  }

  const handleTransfer = async () => {
    if (!selectedRefugee || !transferTarget) return
    try { await api.transferRefugee(selectedRefugee.refugeeId, transferTarget, transferNotes) } catch {}
    const targetShelter = shelters.find(s => s.shelterId === transferTarget)
    setRefugees(prev => prev.map(r => r.refugeeId === selectedRefugee.refugeeId ? { ...r, shelterId: transferTarget, shelterName: targetShelter?.name } : r))
    setTransferTarget(0)
    setTransferNotes('')
  }

  const priorityBadge = (p: string) => {
    const map: Record<string, string> = { REGULAR: 'badge-safe', BALITA: 'badge-info', LANSIA: 'badge-warning', IBU_HAMIL: 'badge-primary', DISABILITAS: 'badge-warning', SICK: 'badge-critical' }
    return map[p] || 'badge-safe'
  }

  return (
    <>
      <div className="search-bar mb-20">
        <input className="search-input" placeholder="Cari pengungsi..." value={search} onChange={e => setSearch(e.target.value)} />
        <button className="btn btn-primary" onClick={() => openDrawer(null)}>Registrasi Pengungsi</button>
      </div>

      <div className="panel-body">
        <div className="panel-list">
          <div className="cards-grid">
            {filtered.map(r => (
              <div className="card data-card" key={r.refugeeId} onClick={() => openDrawer(r)}>
                <div className="data-card-title">{r.name}</div>
                <div className="data-card-subtitle">{r.gender} / {r.age} thn</div>
                <div className="data-card-row">
                  <span className="data-card-label">NIK</span>
                  <span className="data-card-value">{r.nik}</span>
                </div>
                <div className="data-card-row">
                  <span className="data-card-label">Shelter</span>
                  <span className="data-card-value">{r.shelterName || '-'}</span>
                </div>
                {r.medicalNotes && <div className="data-card-row"><span className="data-card-label">Medis</span><span className="data-card-value">{r.medicalNotes}</span></div>}
                <div style={{ marginTop: 10, display: 'flex', gap: 6 }}>
                  <span className={`badge ${r.status === 'CHECKED_IN' ? 'badge-safe' : 'badge-closed'}`}>{r.status === 'CHECKED_IN' ? 'CHECKED IN' : 'CHECKED OUT'}</span>
                  <span className={`badge ${priorityBadge(r.priorityStatus)}`}>{r.priorityStatus}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {drawerOpen && (
          <div className="card card-padded panel-drawer" style={{ width: 380, minWidth: 380 }}>
            <div className="drawer-title">{selectedRefugee ? 'Edit Pengungsi' : 'Registrasi Pengungsi'}</div>
            <div className="form-group">
              <label className="form-label">Nama</label>
              <input className="input" value={form.name} onChange={e => setForm(p => ({ ...p, name: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">NIK</label>
              <input className="input" value={form.nik} onChange={e => setForm(p => ({ ...p, nik: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Usia</label>
              <input className="input" type="number" value={form.age} onChange={e => setForm(p => ({ ...p, age: Number(e.target.value) }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Gender</label>
              <select className="select" value={form.gender} onChange={e => setForm(p => ({ ...p, gender: e.target.value }))}>
                <option value="Laki-laki">Laki-laki</option>
                <option value="Perempuan">Perempuan</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Kelompok Prioritas</label>
              <select className="select" value={form.priorityStatus} onChange={e => setForm(p => ({ ...p, priorityStatus: e.target.value }))}>
                <option value="REGULAR">REGULAR</option>
                <option value="BALITA">BALITA</option>
                <option value="LANSIA">LANSIA</option>
                <option value="IBU_HAMIL">IBU_HAMIL</option>
                <option value="DISABILITAS">DISABILITAS</option>
                <option value="SICK">SICK</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Kode Keluarga</label>
              <input className="input" value={form.familyCode} onChange={e => setForm(p => ({ ...p, familyCode: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Shelter</label>
              <select className="select" value={form.shelterId} onChange={e => setForm(p => ({ ...p, shelterId: Number(e.target.value) }))}>
                <option value={0}>Pilih Shelter</option>
                {shelters.map(s => <option key={s.shelterId} value={s.shelterId}>{s.name}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Status</label>
              <select className="select" value={form.status} onChange={e => setForm(p => ({ ...p, status: e.target.value as Refugee['status'] }))}>
                <option value="CHECKED_IN">CHECKED IN</option>
                <option value="CHECKED_OUT">CHECKED OUT</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Catatan Medis</label>
              <textarea className="textarea" value={form.medicalNotes} onChange={e => setForm(p => ({ ...p, medicalNotes: e.target.value }))} />
            </div>

            {selectedRefugee && (
              <>
                <hr className="drawer-separator" />
                <div className="drawer-title">Check-In / Check-Out</div>
                <div className="drawer-btn-row">
                  {selectedRefugee.status === 'CHECKED_OUT' && <button className="btn btn-sm btn-primary" onClick={handleCheckIn}>Check-In</button>}
                  {selectedRefugee.status === 'CHECKED_IN' && <button className="btn btn-sm btn-danger" onClick={handleCheckOut}>Check-Out</button>}
                </div>

                <hr className="drawer-separator" />
                <div className="drawer-title">Pemindahan Shelter</div>
                <div className="form-group">
                  <label className="form-label">Shelter Tujuan</label>
                  <select className="select" value={transferTarget} onChange={e => setTransferTarget(Number(e.target.value))}>
                    <option value={0}>Pilih Shelter</option>
                    {shelters.filter(s => s.shelterId !== selectedRefugee.shelterId).map(s => <option key={s.shelterId} value={s.shelterId}>{s.name}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Catatan</label>
                  <textarea className="textarea" value={transferNotes} onChange={e => setTransferNotes(e.target.value)} />
                </div>
                <button className="btn btn-sm btn-secondary w-full" onClick={handleTransfer}>Proses Pemindahan</button>

                <hr className="drawer-separator" />
                <div className="drawer-title">Riwayat Perpindahan</div>
                {history.length === 0 && <p style={{ fontSize: 12, color: 'var(--on-surface-variant)' }}>Belum ada riwayat</p>}
                {history.map(h => (
                  <div key={h.movementId} style={{ padding: '8px 0', borderBottom: '1px solid var(--border)', fontSize: 12 }}>
                    <div style={{ fontWeight: 600 }}>{h.fromShelterName} → {h.toShelterName}</div>
                    <div style={{ color: 'var(--on-surface-variant)' }}>{h.movedBy} · {new Date(h.movedAt).toLocaleDateString()}</div>
                    {h.notes && <div style={{ color: 'var(--on-surface-variant)' }}>{h.notes}</div>}
                  </div>
                ))}
              </>
            )}

            <hr className="drawer-separator" />
            <div className="drawer-btn-row">
              <button className="btn btn-primary flex-1" onClick={handleSave}>Simpan</button>
              {selectedRefugee && <button className="btn btn-danger" onClick={() => handleDelete(selectedRefugee.refugeeId)}>Hapus</button>}
              <button className="btn btn-outline" onClick={() => setDrawerOpen(false)}>Batal</button>
            </div>
          </div>
        )}
      </div>
    </>
  )
}
