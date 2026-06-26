import { useState, useEffect } from 'react'
import { api } from '../services/api'
import type { Distribution, Medicine, MedicineAllocation, Shelter } from '../types'

const mockDistributions: Distribution[] = [
  { distributionId: 1, docNum: 'DIST-001', shelterId: 1, shelterName: 'Posko Cisarua', itemType: 'Logistik', quantity: 120, status: 'DRAFT', notes: 'Pengiriman tahap 1', allocations: [{ medicineCode: 'OBT-001', medicineName: 'Paracetamol', quantity: 50, unit: 'Strip' }] },
  { distributionId: 2, docNum: 'DIST-002', shelterId: 2, shelterName: 'Posko Megamendung', itemType: 'Medis', quantity: 80, status: 'APPROVED', allocations: [{ medicineCode: 'OBT-002', medicineName: 'Amoxicillin', quantity: 30, unit: 'Strip' }] },
  { distributionId: 3, docNum: 'DIST-003', shelterId: 3, shelterName: 'Posko Cipayung', itemType: 'Logistik', quantity: 200, status: 'SHIPPED' },
  { distributionId: 4, docNum: 'DIST-004', shelterId: 1, shelterName: 'Posko Cisarua', itemType: 'Medis', quantity: 60, status: 'RECEIVED' },
]
const mockShelters: Shelter[] = [
  { shelterId: 1, name: 'Posko Cisarua', location: 'Cisarua', capacity: 500, currentOccupancy: 475, penanggungJawab: 'Ahmad', status: 'WARNING' },
  { shelterId: 2, name: 'Posko Megamendung', location: 'Megamendung', capacity: 300, currentOccupancy: 200, penanggungJawab: 'Rina', status: 'SAFE' },
  { shelterId: 3, name: 'Posko Cipayung', location: 'Cipayung', capacity: 400, currentOccupancy: 390, penanggungJawab: 'Dedi', status: 'WARNING' },
]
const mockMedicines: Medicine[] = [
  { medicineId: 1, medicineCode: 'OBT-001', medicineName: 'Paracetamol 500mg', category: 'Analgesik', batchNumber: 'B-001', unit: 'Strip', stockQuantity: 500, minimumStock: 100, purchasePrice: 5000, sellingPrice: 7500 },
  { medicineId: 2, medicineCode: 'OBT-002', medicineName: 'Amoxicillin 250mg', category: 'Antibiotik', batchNumber: 'B-002', unit: 'Strip', stockQuantity: 30, minimumStock: 100, purchasePrice: 8000, sellingPrice: 12000 },
]

const steps = ['DRAFT', 'APPROVED', 'SHIPPED', 'RECEIVED'] as const
const STATUS_LABEL: Record<string, string> = { DRAFT: 'Draft', APPROVED: 'Disetujui', SHIPPED: 'Dikirim', RECEIVED: 'Diterima' }

export default function Distribution() {
  const [distributions, setDistributions] = useState<Distribution[]>([])
  const [shelters, setShelters] = useState<Shelter[]>([])
  const [medicines, setMedicines] = useState<Medicine[]>([])
  const [search, setSearch] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [selectedDist, setSelectedDist] = useState<Distribution | null>(null)
  const [form, setForm] = useState({ docNum: '', shelterId: 0, notes: '' })
  const [allocations, setAllocations] = useState<MedicineAllocation[]>([])
  const [selMedId, setSelMedId] = useState(0)
  const [allocQty, setAllocQty] = useState(0)

  useEffect(() => {
    const fetch = async () => {
      try { setDistributions(await api.getDistributions()) } catch { setDistributions(mockDistributions) }
      try { setShelters(await api.getShelters()) } catch { setShelters(mockShelters) }
      try { setMedicines(await api.getMedicines()) } catch { setMedicines(mockMedicines) }
    }
    fetch()
  }, [])

  const filtered = distributions.filter(d =>
    d.docNum.toLowerCase().includes(search.toLowerCase()) ||
    d.shelterName.toLowerCase().includes(search.toLowerCase())
  )

  const openDrawer = (d: Distribution | null) => {
    if (d) {
      setSelectedDist(d)
      setForm({ docNum: d.docNum, shelterId: d.shelterId, notes: d.notes || '' })
      setAllocations(d.allocations || [])
    } else {
      setSelectedDist(null)
      setForm({ docNum: '', shelterId: 0, notes: '' })
      setAllocations([])
    }
    setDrawerOpen(true)
  }

  const handleSave = async () => {
    const payload = { ...form, allocations } as Partial<Distribution>
    if (selectedDist) payload.distributionId = selectedDist.distributionId
    try {
      const saved = await api.saveDistribution(payload)
      setDistributions(prev => {
        const idx = prev.findIndex(d => d.distributionId === saved.distributionId)
        if (idx >= 0) { const c = [...prev]; c[idx] = saved; return c }
        return [...prev, saved]
      })
    } catch {
      const mockId = selectedDist?.distributionId || Date.now()
      const shelter = shelters.find(s => s.shelterId === form.shelterId)
      const mock: Distribution = { distributionId: mockId, docNum: form.docNum, shelterId: form.shelterId, shelterName: shelter?.name || '', itemType: allocations.map(a => a.medicineName).join(', '), quantity: allocations.reduce((s, a) => s + a.quantity, 0), status: 'DRAFT', notes: form.notes, allocations }
      setDistributions(prev => {
        const idx = prev.findIndex(d => d.distributionId === mockId)
        if (idx >= 0) { const c = [...prev]; c[idx] = mock; return c }
        return [...prev, mock]
      })
    }
    setDrawerOpen(false)
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Hapus distribusi ini?')) return
    try { await api.deleteDistribution(id) } catch {}
    setDistributions(prev => prev.filter(d => d.distributionId !== id))
    setDrawerOpen(false)
  }

  const handleStatus = async (id: number, status: string) => {
    try { await api.updateDistributionStatus(id, status) } catch {}
    setDistributions(prev => prev.map(d => d.distributionId === id ? { ...d, status: status as Distribution['status'] } : d))
  }

  const addAllocation = () => {
    if (!selMedId || allocQty <= 0) return
    const med = medicines.find(m => m.medicineId === selMedId)
    if (!med) return
    setAllocations(prev => [...prev, { medicineCode: med.medicineCode, medicineName: med.medicineName, quantity: allocQty, unit: med.unit }])
    setSelMedId(0)
    setAllocQty(0)
  }

  const currentStep = (s: string) => {
    const idx = steps.indexOf(s as typeof steps[number])
    return idx >= 0 ? idx : 0
  }

  return (
    <>
      <div className="search-bar mb-20">
        <input className="search-input" placeholder="Cari distribusi..." value={search} onChange={e => setSearch(e.target.value)} />
        <button className="btn btn-primary" onClick={() => openDrawer(null)}>Tambah Distribusi</button>
      </div>

      <div className="panel-body">
        <div className="panel-list">
          <div className="cards-grid">
            {filtered.map(d => (
              <div className="card data-card" key={d.distributionId} onClick={() => openDrawer(d)}>
                <div className="data-card-title">{d.docNum}</div>
                <div className="data-card-subtitle">{d.shelterName}</div>
                <div className="data-card-row">
                  <span className="data-card-label">Item</span>
                  <span className="data-card-value">{d.itemType}</span>
                </div>
                <div className="data-card-row" style={{ marginBottom: 10 }}>
                  <span className="data-card-label">Qty</span>
                  <span className="data-card-value">{d.quantity}</span>
                </div>
                <div className="dist-timeline">
                  {steps.map((step, i) => {
                    const inactive = currentStep(d.status) < i
                    return (
                      <span key={step} className={`dist-step ${step.toLowerCase()}${inactive ? ' inactive' : ''}`}>
                        {STATUS_LABEL[step]}
                      </span>
                    )
                  })}
                </div>
              </div>
            ))}
          </div>
        </div>

        {drawerOpen && (
          <div className="card card-padded panel-drawer">
            <div className="drawer-title">{selectedDist ? 'Edit Distribusi' : 'Tambah Distribusi'}</div>

            <div className="form-group">
              <label className="form-label">No Dokumen</label>
              <input className="input" value={form.docNum} onChange={e => setForm(p => ({ ...p, docNum: e.target.value }))} />
            </div>

            <div className="form-group">
              <label className="form-label">Shelter Tujuan</label>
              <select className="select" value={form.shelterId} onChange={e => setForm(p => ({ ...p, shelterId: Number(e.target.value) }))}>
                <option value={0}>Pilih Shelter</option>
                {shelters.map(s => <option key={s.shelterId} value={s.shelterId}>{s.name}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Catatan</label>
              <textarea className="textarea" value={form.notes} onChange={e => setForm(p => ({ ...p, notes: e.target.value }))} />
            </div>

            <hr className="drawer-separator" />
            <div className="drawer-title">Alokasi Obat</div>

            {allocations.length === 0 && (
              <p style={{ fontSize: 12, color: 'var(--on-surface-variant)', marginBottom: 10 }}>Belum ada alokasi obat.</p>
            )}
            {allocations.map((a, i) => (
              <div key={i} className="dist-alloc-item">
                <div>
                  <div className="dist-alloc-name">{a.medicineName}</div>
                  <div className="dist-alloc-code">{a.medicineCode}</div>
                </div>
                <div className="dist-alloc-qty">{a.quantity} {a.unit}</div>
              </div>
            ))}

            <div className="dist-alloc-add">
              <select className="select" value={selMedId} onChange={e => setSelMedId(Number(e.target.value))}>
                <option value={0}>Pilih Obat</option>
                {medicines.map(m => <option key={m.medicineId} value={m.medicineId}>{m.medicineName} (stok: {m.stockQuantity})</option>)}
              </select>
              <input className="input" type="number" placeholder="Qty" value={allocQty} onChange={e => setAllocQty(Number(e.target.value))} />
              <button className="btn btn-sm btn-outline" onClick={addAllocation}>Tambah</button>
            </div>

            <hr className="drawer-separator" />

            {selectedDist && selectedDist.status !== 'DRAFT' && (
              <div className="dist-status-bar">
                <span className={`badge badge-${selectedDist.status === 'APPROVED' ? 'warning' : selectedDist.status === 'SHIPPED' ? 'info' : 'in-stock'}`}>
                  {STATUS_LABEL[selectedDist.status]}
                </span>
              </div>
            )}

            <div className="drawer-btn-row">
              <button className="btn btn-primary flex-1" onClick={handleSave}>Simpan</button>
              {selectedDist && selectedDist.status === 'DRAFT' && (
                <button className="btn btn-secondary" onClick={() => handleStatus(selectedDist.distributionId, 'APPROVED')}>Setujui</button>
              )}
              {selectedDist && selectedDist.status === 'APPROVED' && (
                <button className="btn btn-secondary" onClick={() => handleStatus(selectedDist.distributionId, 'SHIPPED')}>Kirim</button>
              )}
              {selectedDist && selectedDist.status === 'SHIPPED' && (
                <button className="btn btn-secondary" onClick={() => handleStatus(selectedDist.distributionId, 'RECEIVED')}>Terima</button>
              )}
              {selectedDist && (
                <button className="btn btn-danger" onClick={() => handleDelete(selectedDist.distributionId)}>Hapus</button>
              )}
              <button className="btn btn-outline" onClick={() => setDrawerOpen(false)}>Batal</button>
            </div>
          </div>
        )}
      </div>
    </>
  )
}