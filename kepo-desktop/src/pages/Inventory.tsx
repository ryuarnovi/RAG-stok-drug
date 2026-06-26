import { useState, useEffect } from 'react'
import { api } from '../services/api'
import type { Medicine, Supplier } from '../types'

const mockMedicines: Medicine[] = [
  { medicineId: 1, medicineCode: 'OBT-001', medicineName: 'Paracetamol 500mg', category: 'Analgesik', batchNumber: 'BATCH-001', unit: 'Strip', stockQuantity: 500, minimumStock: 100, purchasePrice: 5000, sellingPrice: 7500, expiryDate: '2026-12-31', supplierId: 1 },
  { medicineId: 2, medicineCode: 'OBT-002', medicineName: 'Amoxicillin 250mg', category: 'Antibiotik', batchNumber: 'BATCH-002', unit: 'Strip', stockQuantity: 30, minimumStock: 100, purchasePrice: 8000, sellingPrice: 12000, expiryDate: '2026-06-30', supplierId: 1 },
  { medicineId: 3, medicineCode: 'OBT-003', medicineName: 'Oralit 200ml', category: 'Elektrolit', batchNumber: 'BATCH-003', unit: 'Botol', stockQuantity: 0, minimumStock: 200, purchasePrice: 3000, sellingPrice: 5000, expiryDate: '2027-01-15', supplierId: 2 },
]
const mockSuppliers: Supplier[] = [
  { supplierId: 1, supplierName: 'PT Farmasi Sehat', contactPerson: 'Budi', phone: '021-123456', email: 'budi@sehat.com', address: 'Jakarta' },
  { supplierId: 2, supplierName: 'CV Medika Utama', contactPerson: 'Siti', phone: '022-654321', email: 'siti@medika.com', address: 'Bandung' },
]

export default function Inventory() {
  const [medicines, setMedicines] = useState<Medicine[]>([])
  const [suppliers, setSuppliers] = useState<Supplier[]>([])
  const [search, setSearch] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [selectedMedicine, setSelectedMedicine] = useState<Medicine | null>(null)
  const [form, setForm] = useState({ medicineCode: '', medicineName: '', category: '', batchNumber: '', unit: '', stockQuantity: 0, minimumStock: 0, purchasePrice: 0, sellingPrice: 0, expiryDate: '', supplierId: 0 })
  const [showAdjust, setShowAdjust] = useState(false)
  const [adjType, setAdjType] = useState('IN')
  const [adjQty, setAdjQty] = useState(0)

  useEffect(() => {
    const fetch = async () => {
      try { setMedicines(await api.getMedicines()) } catch { setMedicines(mockMedicines) }
      try { setSuppliers(await api.getSuppliers()) } catch { setSuppliers(mockSuppliers) }
    }
    fetch()
  }, [])

  const filtered = medicines.filter(m =>
    m.medicineName.toLowerCase().includes(search.toLowerCase()) ||
    m.medicineCode.toLowerCase().includes(search.toLowerCase())
  )

  const openDrawer = (m: Medicine | null) => {
    if (m) {
      setSelectedMedicine(m)
      setForm({ medicineCode: m.medicineCode, medicineName: m.medicineName, category: m.category, batchNumber: m.batchNumber, unit: m.unit, stockQuantity: m.stockQuantity, minimumStock: m.minimumStock, purchasePrice: m.purchasePrice, sellingPrice: m.sellingPrice, expiryDate: m.expiryDate || '', supplierId: m.supplierId || 0 })
    } else {
      setSelectedMedicine(null)
      setForm({ medicineCode: '', medicineName: '', category: '', batchNumber: '', unit: '', stockQuantity: 0, minimumStock: 0, purchasePrice: 0, sellingPrice: 0, expiryDate: '', supplierId: 0 })
    }
    setDrawerOpen(true)
  }

  const handleSave = async () => {
    const payload = { ...form } as Partial<Medicine>
    if (selectedMedicine) payload.medicineId = selectedMedicine.medicineId
    try {
      const saved = await api.saveMedicine(payload)
      setMedicines(prev => {
        const idx = prev.findIndex(m => m.medicineId === saved.medicineId)
        if (idx >= 0) { const c = [...prev]; c[idx] = saved; return c }
        return [...prev, saved]
      })
    } catch {
      const mockId = selectedMedicine?.medicineId || Date.now()
      const mock = { ...form, medicineId: mockId } as Medicine
      setMedicines(prev => {
        const idx = prev.findIndex(m => m.medicineId === mockId)
        if (idx >= 0) { const c = [...prev]; c[idx] = mock; return c }
        return [...prev, mock]
      })
    }
    setDrawerOpen(false)
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Hapus obat ini?')) return
    try { await api.deleteMedicine(id) } catch {}
    setMedicines(prev => prev.filter(m => m.medicineId !== id))
    setDrawerOpen(false)
  }

  const handleAdjust = async () => {
    if (!selectedMedicine || adjQty <= 0) return
    try { await api.adjustStock(selectedMedicine.medicineId, adjQty, adjType) } catch {}
    setMedicines(prev => prev.map(m => m.medicineId === selectedMedicine.medicineId ? { ...m, stockQuantity: adjType === 'OUT' ? Math.max(0, m.stockQuantity - adjQty) : m.stockQuantity + adjQty } : m))
    setShowAdjust(false)
    setAdjQty(0)
  }

  const statusBadge = (m: Medicine) => {
    if (m.stockQuantity <= 0) return 'badge-expired'
    if (m.stockQuantity < m.minimumStock) return 'badge-near-expiry'
    return 'badge-in-stock'
  }

  const statusLabel = (m: Medicine) => {
    if (m.stockQuantity <= 0) return 'OUT OF STOCK'
    if (m.stockQuantity < m.minimumStock) return 'LOW STOCK'
    return 'IN STOCK'
  }

  return (
    <>
      <div className="search-bar mb-20">
        <input className="search-input" placeholder="Cari obat..." value={search} onChange={e => setSearch(e.target.value)} />
        <button className="btn btn-primary" onClick={() => openDrawer(null)}>Tambah Obat Baru</button>
      </div>

      <div className="panel-body">
        <div className="panel-list">
          <div className="cards-grid">
            {filtered.map(m => (
              <div className="card data-card" key={m.medicineId} onClick={() => openDrawer(m)}>
                <div className="data-card-title">{m.medicineName}</div>
                <div className="data-card-subtitle">{m.medicineCode}</div>
                <div className="data-card-row">
                  <span className="data-card-label">Stok</span>
                  <span className="data-card-value">{m.stockQuantity} {m.unit}</span>
                </div>
                <div className="data-card-row">
                  <span className="data-card-label">Kadaluarsa</span>
                  <span className="data-card-value">{m.expiryDate || '-'}</span>
                </div>
                <div style={{ marginTop: 10 }}><span className={`badge ${statusBadge(m)}`}>{statusLabel(m)}</span></div>
              </div>
            ))}
          </div>
        </div>

        {drawerOpen && (
          <div className="card card-padded panel-drawer">
            <div className="drawer-title">{selectedMedicine ? 'Edit Obat' : 'Tambah Obat Baru'}</div>
            <div className="form-group">
              <label className="form-label">Kode</label>
              <input className="input" value={form.medicineCode} onChange={e => setForm(p => ({ ...p, medicineCode: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Nama Obat</label>
              <input className="input" value={form.medicineName} onChange={e => setForm(p => ({ ...p, medicineName: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Kategori</label>
              <input className="input" value={form.category} onChange={e => setForm(p => ({ ...p, category: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Batch</label>
              <input className="input" value={form.batchNumber} onChange={e => setForm(p => ({ ...p, batchNumber: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Unit</label>
              <input className="input" value={form.unit} onChange={e => setForm(p => ({ ...p, unit: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Stok</label>
              <input className="input" type="number" value={form.stockQuantity} onChange={e => setForm(p => ({ ...p, stockQuantity: Number(e.target.value) }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Min Stok</label>
              <input className="input" type="number" value={form.minimumStock} onChange={e => setForm(p => ({ ...p, minimumStock: Number(e.target.value) }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Harga Beli</label>
              <input className="input" type="number" value={form.purchasePrice} onChange={e => setForm(p => ({ ...p, purchasePrice: Number(e.target.value) }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Harga Jual</label>
              <input className="input" type="number" value={form.sellingPrice} onChange={e => setForm(p => ({ ...p, sellingPrice: Number(e.target.value) }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Tanggal Kadaluarsa</label>
              <input className="input" type="date" value={form.expiryDate} onChange={e => setForm(p => ({ ...p, expiryDate: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Supplier</label>
              <select className="select" value={form.supplierId} onChange={e => setForm(p => ({ ...p, supplierId: Number(e.target.value) }))}>
                <option value={0}>Pilih Supplier</option>
                {suppliers.map(s => <option key={s.supplierId} value={s.supplierId}>{s.supplierName}</option>)}
              </select>
            </div>
            <hr className="drawer-separator" />
            <div className="drawer-btn-row">
              <button className="btn btn-primary flex-1" onClick={handleSave}>Simpan</button>
              {selectedMedicine && (
                <>
                  <button className="btn btn-outline" onClick={() => { setShowAdjust(true); setAdjQty(0) }}>Sesuaikan Stok</button>
                  <button className="btn btn-danger" onClick={() => handleDelete(selectedMedicine.medicineId)}>Hapus</button>
                </>
              )}
              <button className="btn btn-outline" onClick={() => setDrawerOpen(false)}>Batal</button>
            </div>
          </div>
        )}
      </div>

      {showAdjust && (
        <div className="card card-padded" style={{ position: 'fixed', top: '50%', left: '50%', transform: 'translate(-50%,-50%)', zIndex: 1000, width: 360 }}>
          <div className="drawer-title">Sesuaikan Stok</div>
          <div className="form-group">
            <label className="form-label">Tipe</label>
            <select className="select" value={adjType} onChange={e => setAdjType(e.target.value)}>
              <option value="IN">IN (Masuk)</option>
              <option value="OUT">OUT (Keluar)</option>
              <option value="ADJUSTMENT">ADJUSTMENT (Penyesuaian)</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Jumlah</label>
            <input className="input" type="number" value={adjQty} onChange={e => setAdjQty(Number(e.target.value))} />
          </div>
          <div className="drawer-btn-row">
            <button className="btn btn-primary flex-1" onClick={handleAdjust}>Proses</button>
            <button className="btn btn-outline" onClick={() => setShowAdjust(false)}>Batal</button>
          </div>
        </div>
      )}

      {showAdjust && <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.3)', zIndex: 999 }} onClick={() => setShowAdjust(false)} />}
    </>
  )
}
