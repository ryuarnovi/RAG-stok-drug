import { useState, useEffect, useRef } from 'react'
import { api } from '../services/api'
import type { MedicineRequest, Refugee, Medicine } from '../types'

const mockRequests: MedicineRequest[] = [
  { requestId: 1, refugeeId: 1, refugeeName: 'Slamet Raharjo', shelterId: 1, shelterName: 'GOR Grogol', medicineCode: 'MED-001', medicineName: 'Amoxicillin 500mg', quantity: 2, status: 'PENDING', notes: 'Sesak napas', createdAt: new Date().toISOString() },
  { requestId: 2, refugeeId: 2, refugeeName: 'Siti Rahmawati', shelterId: 2, shelterName: 'Kantor Kelurahan Bengkalis', medicineCode: 'MED-002', medicineName: 'Paracetamol 500mg', quantity: 1, status: 'PENDING', notes: 'Demam', createdAt: new Date().toISOString() },
]

const STATUS_LABEL: Record<string, string> = {
  PENDING: 'Pending',
  APPROVED: 'Disetujui',
  REJECTED: 'Ditolak',
  FULFILLED: 'Dipenuhi',
}

const STATUS_BADGE: Record<string, string> = {
  PENDING: 'badge-warning',
  APPROVED: 'badge-info',
  REJECTED: 'badge-expired',
  FULFILLED: 'badge-in-stock',
}

export default function MedicineRequests() {
  const [requests, setRequests] = useState<MedicineRequest[]>([])
  const [refugees, setRefugees] = useState<Refugee[]>([])
  const [medicines, setMedicines] = useState<Medicine[]>([])
  const [search, setSearch] = useState('')
  const [filterStatus, setFilterStatus] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ refugeeId: 0, shelterId: 0, medicineCode: '', medicineName: '', quantity: 1, notes: '' })
  const [counts, setCounts] = useState({ pending: 0, approved: 0, fulfilled: 0, total: 0 })

  const [refugeeSearch, setRefugeeSearch] = useState('')
  const [refugeeDropdown, setRefugeeDropdown] = useState(false)
  const [medicineSearch, setMedicineSearch] = useState('')
  const [medicineDropdown, setMedicineDropdown] = useState(false)
  const refugeeRef = useRef<HTMLDivElement>(null)
  const medicineRef = useRef<HTMLDivElement>(null)

  const checkedIn = refugees.filter(r => r.status === 'CHECKED_IN')
  const filteredRefugees = checkedIn.filter(r =>
    r.name.toLowerCase().includes(refugeeSearch.toLowerCase()) ||
    (r.shelterName && r.shelterName.toLowerCase().includes(refugeeSearch.toLowerCase()))
  )
  const filteredMedicines = medicines.filter(m =>
    m.medicineName.toLowerCase().includes(medicineSearch.toLowerCase()) ||
    m.medicineCode.toLowerCase().includes(medicineSearch.toLowerCase())
  )

  useEffect(() => {
    const fetch = async () => {
      try { setRequests(await api.getMedicineRequests()) } catch { setRequests(mockRequests) }
      try { setRefugees(await api.getRefugees()) } catch { /* ignore */ }
      try { setMedicines(await api.getMedicines()) } catch { /* ignore */ }
      try { setCounts(await api.getMedicineRequestCount()) } catch { /* ignore */ }
    }
    fetch()
    const interval = setInterval(fetch, 10000)
    return () => clearInterval(interval)
  }, [])

  useEffect(() => {
    const close = (e: MouseEvent) => {
      if (refugeeRef.current && !refugeeRef.current.contains(e.target as Node)) setRefugeeDropdown(false)
      if (medicineRef.current && !medicineRef.current.contains(e.target as Node)) setMedicineDropdown(false)
    }
    document.addEventListener('mousedown', close)
    return () => document.removeEventListener('mousedown', close)
  }, [])

  const filtered = requests.filter(r =>
    (r.refugeeName?.toLowerCase().includes(search.toLowerCase()) ||
     r.medicineName.toLowerCase().includes(search.toLowerCase()) ||
     r.shelterName?.toLowerCase().includes(search.toLowerCase())) &&
    (!filterStatus || r.status === filterStatus)
  )

  const refreshAll = async () => {
    try { setRequests(await api.getMedicineRequests()) } catch { /* ignore */ }
    try { setCounts(await api.getMedicineRequestCount()) } catch { /* ignore */ }
  }

  const handleSubmit = async () => {
    if (!form.refugeeId || !form.medicineCode) return
    try {
      await api.createMedicineRequest(form)
      setShowForm(false)
      setForm({ refugeeId: 0, shelterId: 0, medicineCode: '', medicineName: '', quantity: 1, notes: '' })
      setRefugeeSearch('')
      setMedicineSearch('')
      refreshAll()
    } catch { /* ignore */ }
  }

  const handleStatus = async (id: number, status: string) => {
    try {
      await api.updateMedicineRequestStatus(id, status)
      refreshAll()
    } catch { /* ignore */ }
  }

  const selectRefugee = (r: Refugee) => {
    setForm(prev => ({ ...prev, refugeeId: r.refugeeId, shelterId: r.shelterId || 0 }))
    setRefugeeSearch(r.name + (r.shelterName ? ` — ${r.shelterName}` : ''))
    setRefugeeDropdown(false)
  }

  const selectMedicine = (m: Medicine) => {
    setForm(prev => ({ ...prev, medicineCode: m.medicineCode, medicineName: m.medicineName }))
    setMedicineSearch(`${m.medicineName} (stok: ${m.stockQuantity})`)
    setMedicineDropdown(false)
  }

  const openForm = () => {
    setRefugeeSearch('')
    setMedicineSearch('')
    setForm({ refugeeId: 0, shelterId: 0, medicineCode: '', medicineName: '', quantity: 1, notes: '' })
    setShowForm(true)
  }

  return (
    <>
      <div className="search-bar mb-20">
        <input className="search-input" placeholder="Cari pengungsi, obat, shelter..." value={search} onChange={e => setSearch(e.target.value)} />
        <select className="select" value={filterStatus} onChange={e => setFilterStatus(e.target.value)} style={{ maxWidth: 160 }}>
          <option value="">Semua Status</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Disetujui</option>
          <option value="REJECTED">Ditolak</option>
          <option value="FULFILLED">Dipenuhi</option>
        </select>
        <button className="btn btn-primary" onClick={openForm}>+ Buat Permintaan</button>
      </div>

      <div className="kpi-row">
        <div className="card card-padded">
          <div className="kpi-title">Pending</div>
          <div className="kpi-value">{counts.pending}</div>
        </div>
        <div className="card card-padded">
          <div className="kpi-title">Disetujui</div>
          <div className="kpi-value">{counts.approved}</div>
        </div>
        <div className="card card-padded">
          <div className="kpi-title">Dipenuhi</div>
          <div className="kpi-value">{counts.fulfilled}</div>
        </div>
        <div className="card card-padded">
          <div className="kpi-title">Total</div>
          <div className="kpi-value">{counts.total}</div>
        </div>
      </div>

      <div className="card">
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Pengungsi</th>
                <th>Shelter</th>
                <th>Obat</th>
                <th>Qty</th>
                <th>Status</th>
                <th>Tanggal</th>
                <th>Aksi</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(r => (
                <tr key={r.requestId}>
                  <td><strong>{r.refugeeName}</strong></td>
                  <td>{r.shelterName}</td>
                  <td>{r.medicineName}</td>
                  <td>{r.quantity}</td>
                  <td><span className={`badge ${STATUS_BADGE[r.status] || 'badge-warning'}`}>{STATUS_LABEL[r.status] || r.status}</span></td>
                  <td style={{ fontSize: 12, color: 'var(--on-surface-variant)' }}>{new Date(r.createdAt).toLocaleDateString()}</td>
                  <td>
                    {r.status === 'PENDING' && (
                      <div style={{ display: 'flex', gap: 6 }}>
                        <button className="btn btn-primary btn-sm" onClick={() => handleStatus(r.requestId, 'APPROVED')}>Approve</button>
                        <button className="btn btn-outline btn-sm" onClick={() => handleStatus(r.requestId, 'REJECTED')}>Tolak</button>
                      </div>
                    )}
                    {r.status === 'APPROVED' && (
                      <button className="btn btn-secondary btn-sm" onClick={() => handleStatus(r.requestId, 'FULFILLED')}>Tandai Dipenuhi</button>
                    )}
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan={7} style={{ textAlign: 'center', padding: 20, color: 'var(--on-surface-variant)' }}>Belum ada permintaan obat.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showForm && (
        <div className="drawer-overlay" onClick={() => setShowForm(false)}>
          <div className="card card-padded panel-drawer" onClick={e => e.stopPropagation()} style={{ maxWidth: 440, width: '100%', borderRadius: 12 }}>
            <div className="drawer-title">Permintaan Obat Baru</div>

            <div className="form-group">
              <label className="form-label">Pengungsi</label>
              <div ref={refugeeRef} style={{ position: 'relative' }}>
                <input
                  className="input"
                  placeholder="Cari nama pengungsi..."
                  value={refugeeSearch}
                  onChange={e => { setRefugeeSearch(e.target.value); setRefugeeDropdown(true); setForm(p => ({ ...p, refugeeId: 0 })) }}
                  onFocus={() => setRefugeeDropdown(true)}
                />
                {refugeeDropdown && (
                  <div className="search-dropdown">
                    {filteredRefugees.length === 0 ? (
                      <div className="search-dropdown-empty">Pengungsi tidak ditemukan</div>
                    ) : filteredRefugees.map(r => (
                      <div key={r.refugeeId} className="search-dropdown-item" onClick={() => selectRefugee(r)}>
                        <div style={{ fontSize: 13, fontWeight: 600 }}>{r.name}</div>
                        <div style={{ fontSize: 11, color: 'var(--on-surface-variant)' }}>{r.shelterName || '-'} · {r.nik}</div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Obat</label>
              <div ref={medicineRef} style={{ position: 'relative' }}>
                <input
                  className="input"
                  placeholder="Cari nama obat..."
                  value={medicineSearch}
                  onChange={e => { setMedicineSearch(e.target.value); setMedicineDropdown(true); setForm(p => ({ ...p, medicineCode: '', medicineName: '' })) }}
                  onFocus={() => setMedicineDropdown(true)}
                />
                {medicineDropdown && (
                  <div className="search-dropdown">
                    {filteredMedicines.length === 0 ? (
                      <div className="search-dropdown-empty">Obat tidak ditemukan</div>
                    ) : filteredMedicines.map(m => (
                      <div key={m.medicineCode} className="search-dropdown-item" onClick={() => selectMedicine(m)}>
                        <div style={{ fontSize: 13, fontWeight: 600 }}>{m.medicineName}</div>
                        <div style={{ fontSize: 11, color: 'var(--on-surface-variant)' }}>{m.medicineCode} · Stok: {m.stockQuantity} {m.unit}</div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Jumlah</label>
              <input className="input" type="number" min={1} value={form.quantity} onChange={e => setForm(prev => ({ ...prev, quantity: Math.max(1, Number(e.target.value)) }))} />
            </div>

            <div className="form-group">
              <label className="form-label">Catatan</label>
              <textarea className="textarea" rows={3} value={form.notes} onChange={e => setForm(prev => ({ ...prev, notes: e.target.value }))} placeholder="Keluhan atau alasan medis..." />
            </div>

            <hr className="drawer-separator" />
            <div className="drawer-btn-row">
              <button className="btn btn-primary flex-1" onClick={handleSubmit} disabled={!form.refugeeId || !form.medicineCode}>Ajukan Permintaan</button>
              <button className="btn btn-outline" onClick={() => setShowForm(false)}>Batal</button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}