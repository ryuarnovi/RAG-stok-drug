import { useState, useEffect } from 'react'
import { api } from '../services/api'
import type { MedicineRequest, Refugee, Medicine } from '../types'

const mockRequests: MedicineRequest[] = [
  { requestId: 1, refugeeId: 1, refugeeName: 'Slamet Raharjo', shelterId: 1, shelterName: 'GOR Grogol', medicineCode: 'MED-001', medicineName: 'Amoxicillin 500mg', quantity: 2, status: 'PENDING', notes: 'Sesak napas', createdAt: new Date().toISOString() },
  { requestId: 2, refugeeId: 2, refugeeName: 'Siti Rahmawati', shelterId: 2, shelterName: 'Kantor Kelurahan Bengkalis', medicineCode: 'MED-002', medicineName: 'Paracetamol 500mg', quantity: 1, status: 'PENDING', notes: 'Demam', createdAt: new Date().toISOString() },
]

export default function MedicineRequests() {
  const [requests, setRequests] = useState<MedicineRequest[]>([])
  const [refugees, setRefugees] = useState<Refugee[]>([])
  const [medicines, setMedicines] = useState<Medicine[]>([])
  const [search, setSearch] = useState('')
  const [filterStatus, setFilterStatus] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ refugeeId: 0, shelterId: 0, medicineCode: '', medicineName: '', quantity: 1, notes: '' })
  const [counts, setCounts] = useState({ pending: 0, approved: 0, fulfilled: 0, total: 0 })

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
      refreshAll()
    } catch { /* ignore */ }
  }

  const handleStatus = async (id: number, status: string) => {
    try {
      await api.updateMedicineRequestStatus(id, status)
      refreshAll()
    } catch { /* ignore */ }
  }

  const onRefugeeSelect = (refugeeId: number) => {
    const r = refugees.find(x => x.refugeeId === refugeeId)
    setForm(prev => ({ ...prev, refugeeId, shelterId: r?.shelterId || 0 }))
  }

  const onMedicineSelect = (code: string) => {
    const m = medicines.find(x => x.medicineCode === code)
    setForm(prev => ({ ...prev, medicineCode: code, medicineName: m?.medicineName || '' }))
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h2>Permintaan Obat</h2>
          <p className="text-muted">Pengajuan obat per pengungsi — {counts.total} total, {counts.pending} pending</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowForm(true)}>+ Buat Permintaan</button>
      </div>

      <div className="grid cols-4 mb-20">
        <div className="card card-padded" style={{ borderLeft: '4px solid var(--warning)' }}>
          <div className="text-muted" style={{ fontSize: 12 }}>Pending</div>
          <div style={{ fontSize: 28, fontWeight: 700 }}>{counts.pending}</div>
        </div>
        <div className="card card-padded" style={{ borderLeft: '4px solid var(--primary)' }}>
          <div className="text-muted" style={{ fontSize: 12 }}>Disetujui</div>
          <div style={{ fontSize: 28, fontWeight: 700 }}>{counts.approved}</div>
        </div>
        <div className="card card-padded" style={{ borderLeft: '4px solid var(--safe)' }}>
          <div className="text-muted" style={{ fontSize: 12 }}>Dipenuhi</div>
          <div style={{ fontSize: 28, fontWeight: 700 }}>{counts.fulfilled}</div>
        </div>
        <div className="card card-padded" style={{ borderLeft: '4px solid var(--on-surface-variant)' }}>
          <div className="text-muted" style={{ fontSize: 12 }}>Total</div>
          <div style={{ fontSize: 28, fontWeight: 700 }}>{counts.total}</div>
        </div>
      </div>

      <div className="card">
        <div className="table-toolbar">
          <input className="input" placeholder="Cari pengungsi, obat, shelter..." value={search} onChange={e => setSearch(e.target.value)} style={{ maxWidth: 300 }} />
          <select className="input" value={filterStatus} onChange={e => setFilterStatus(e.target.value)} style={{ maxWidth: 150 }}>
            <option value="">Semua Status</option>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Disetujui</option>
            <option value="REJECTED">Ditolak</option>
            <option value="FULFILLED">Dipenuhi</option>
          </select>
        </div>
        <table className="table">
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
                <td>
                  <span className={`badge badge-${r.status === 'FULFILLED' ? 'safe' : r.status === 'APPROVED' ? 'info' : r.status === 'REJECTED' ? 'expired' : 'warning'}`}>
                    {r.status}
                  </span>
                </td>
                <td className="text-muted" style={{ fontSize: 12 }}>{new Date(r.createdAt).toLocaleDateString()}</td>
                <td>
                  {r.status === 'PENDING' && (
                    <div className="flex gap-6">
                      <button className="btn btn-primary btn-sm" onClick={() => handleStatus(r.requestId, 'APPROVED')}>Approve</button>
                      <button className="btn btn-outline btn-sm" onClick={() => handleStatus(r.requestId, 'REJECTED')}>Tolak</button>
                    </div>
                  )}
                  {r.status === 'APPROVED' && (
                    <button className="btn btn-safe btn-sm" onClick={() => handleStatus(r.requestId, 'FULFILLED')}>Tandai Dipenuhi</button>
                  )}
                </td>
              </tr>
            ))}
            {filtered.length === 0 && (
              <tr><td colSpan={7} className="text-muted" style={{ textAlign: 'center', padding: 20 }}>Belum ada permintaan obat.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {showForm && (
        <div className="drawer-overlay" onClick={() => setShowForm(false)}>
          <div className="drawer" onClick={e => e.stopPropagation()}>
            <div className="drawer-header">
              <h3>Permintaan Obat Baru</h3>
              <button className="btn btn-outline btn-sm" onClick={() => setShowForm(false)}>Tutup</button>
            </div>
            <div className="drawer-body">
              <div className="form-group">
                <label>Pengungsi</label>
                <select className="input" value={form.refugeeId} onChange={e => onRefugeeSelect(Number(e.target.value))}>
                  <option value={0}>Pilih pengungsi...</option>
                  {refugees.filter(r => r.status === 'CHECKED_IN').map(r => (
                    <option key={r.refugeeId} value={r.refugeeId}>{r.name} — {r.shelterName || '-'}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Obat</label>
                <select className="input" value={form.medicineCode} onChange={e => onMedicineSelect(e.target.value)}>
                  <option value="">Pilih obat...</option>
                  {medicines.map(m => (
                    <option key={m.medicineCode} value={m.medicineCode}>{m.medicineName} (stok: {m.stockQuantity})</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Jumlah</label>
                <input className="input" type="number" min={1} value={form.quantity} onChange={e => setForm(prev => ({ ...prev, quantity: Math.max(1, Number(e.target.value)) }))} />
              </div>
              <div className="form-group">
                <label>Catatan</label>
                <textarea className="input" rows={3} value={form.notes} onChange={e => setForm(prev => ({ ...prev, notes: e.target.value }))} placeholder="Keluhan atau alasan medis..." />
              </div>
              <button className="btn btn-primary" style={{ width: '100%' }} onClick={handleSubmit}>Ajukan Permintaan</button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
