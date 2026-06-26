import { useState, useEffect } from 'react'
import { api } from '../services/api'
import type { Supplier, Donor } from '../types'

const mockSuppliers: Supplier[] = [
  { supplierId: 1, supplierName: 'PT Farmasi Sehat', contactPerson: 'Budi Santoso', phone: '021-12345678', email: 'budi@farmasisehat.com', address: 'Jl. Merdeka No. 1, Jakarta' },
  { supplierId: 2, supplierName: 'CV Medika Utama', contactPerson: 'Siti Rahma', phone: '022-87654321', email: 'siti@medikautama.com', address: 'Jl. Diponegoro No. 45, Bandung' },
  { supplierId: 3, supplierName: 'PT Logistik Nusantara', contactPerson: 'Agus Wijaya', phone: '031-5566778', email: 'agus@logistiknusantara.com', address: 'Jl. Raya Surabaya No. 78, Surabaya' },
]
const mockDonors: Donor[] = [
  { donorId: 1, donorName: 'Yayasan Peduli Sesama', contact: 'Rudi Hartono', phone: '0812-3456-7890', email: 'rudi@ypeduli.org', address: 'Jakarta Selatan' },
  { donorId: 2, donorName: 'PT Berkah Abadi', contact: 'Linda Kusuma', phone: '0813-9876-5432', email: 'linda@berkahabadi.com', address: 'Tangerang' },
  { donorId: 3, donorName: 'Komunitas Relawan Indonesia', contact: 'Fajar Pratama', phone: '0856-1122-3344', email: 'fajar@relawan.id', address: 'Bandung' },
]

export default function SupplierDonor() {
  const [activeTab, setActiveTab] = useState<'supplier' | 'donor'>('supplier')
  const [suppliers, setSuppliers] = useState<Supplier[]>([])
  const [donors, setDonors] = useState<Donor[]>([])
  const [search, setSearch] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editSupplier, setEditSupplier] = useState<Supplier | null>(null)
  const [editDonor, setEditDonor] = useState<Donor | null>(null)
  const [supForm, setSupForm] = useState({ supplierName: '', contactPerson: '', phone: '', email: '', address: '' })
  const [donForm, setDonForm] = useState({ donorName: '', contact: '', phone: '', email: '', address: '' })

  useEffect(() => {
    const fetch = async () => {
      try { setSuppliers(await api.getSuppliers()) } catch { setSuppliers(mockSuppliers) }
      try { setDonors(await api.getDonors()) } catch { setDonors(mockDonors) }
    }
    fetch()
  }, [])

  const filteredSuppliers = suppliers.filter(s =>
    s.supplierName.toLowerCase().includes(search.toLowerCase()) ||
    s.contactPerson?.toLowerCase().includes(search.toLowerCase())
  )
  const filteredDonors = donors.filter(d =>
    d.donorName.toLowerCase().includes(search.toLowerCase()) ||
    d.contact?.toLowerCase().includes(search.toLowerCase())
  )

  const openSupplierDrawer = (s: Supplier | null) => {
    setEditSupplier(s)
    setEditDonor(null)
    setSupForm(s ? { supplierName: s.supplierName, contactPerson: s.contactPerson || '', phone: s.phone || '', email: s.email || '', address: s.address || '' } : { supplierName: '', contactPerson: '', phone: '', email: '', address: '' })
    setDrawerOpen(true)
  }

  const openDonorDrawer = (d: Donor | null) => {
    setEditDonor(d)
    setEditSupplier(null)
    setDonForm(d ? { donorName: d.donorName, contact: d.contact || '', phone: d.phone || '', email: d.email || '', address: d.address || '' } : { donorName: '', contact: '', phone: '', email: '', address: '' })
    setDrawerOpen(true)
  }

  const saveSupplier = async () => {
    const payload = { ...supForm } as Partial<Supplier>
    if (editSupplier) payload.supplierId = editSupplier.supplierId
    try {
      const saved = await api.saveSupplier(payload)
      setSuppliers(prev => {
        const idx = prev.findIndex(s => s.supplierId === saved.supplierId)
        if (idx >= 0) { const c = [...prev]; c[idx] = saved; return c }
        return [...prev, saved]
      })
    } catch {
      const id = editSupplier?.supplierId || Date.now()
      const mock = { ...supForm, supplierId: id } as Supplier
      setSuppliers(prev => {
        const idx = prev.findIndex(s => s.supplierId === id)
        if (idx >= 0) { const c = [...prev]; c[idx] = mock; return c }
        return [...prev, mock]
      })
    }
    setDrawerOpen(false)
  }

  const saveDonor = async () => {
    const payload = { ...donForm } as Partial<Donor>
    if (editDonor) payload.donorId = editDonor.donorId
    try {
      const saved = await api.saveDonor(payload)
      setDonors(prev => {
        const idx = prev.findIndex(d => d.donorId === saved.donorId)
        if (idx >= 0) { const c = [...prev]; c[idx] = saved; return c }
        return [...prev, saved]
      })
    } catch {
      const id = editDonor?.donorId || Date.now()
      const mock = { ...donForm, donorId: id } as Donor
      setDonors(prev => {
        const idx = prev.findIndex(d => d.donorId === id)
        if (idx >= 0) { const c = [...prev]; c[idx] = mock; return c }
        return [...prev, mock]
      })
    }
    setDrawerOpen(false)
  }

  const deleteSupplier = async (id: number) => {
    if (!confirm('Hapus supplier ini?')) return
    try { await api.deleteSupplier(id) } catch {}
    setSuppliers(prev => prev.filter(s => s.supplierId !== id))
    setDrawerOpen(false)
  }

  const deleteDonor = async (id: number) => {
    if (!confirm('Hapus donatur ini?')) return
    try { await api.deleteDonor(id) } catch {}
    setDonors(prev => prev.filter(d => d.donorId !== id))
    setDrawerOpen(false)
  }

  return (
    <>
      <div className="tab-bar">
        <button className={`tab-item ${activeTab === 'supplier' ? 'active' : ''}`} onClick={() => { setActiveTab('supplier'); setDrawerOpen(false); setSearch('') }}>Supplier Logistik</button>
        <button className={`tab-item ${activeTab === 'donor' ? 'active' : ''}`} onClick={() => { setActiveTab('donor'); setDrawerOpen(false); setSearch('') }}>Donatur & Sponsor</button>
      </div>

      <div className="search-bar mb-20">
        <input className="search-input" placeholder={`Cari ${activeTab === 'supplier' ? 'supplier' : 'donatur'}...`} value={search} onChange={e => setSearch(e.target.value)} />
        <button className="btn btn-primary" onClick={() => activeTab === 'supplier' ? openSupplierDrawer(null) : openDonorDrawer(null)}>Tambah</button>
      </div>

      <div className="panel-body">
        <div className="panel-list">
          <div className="cards-grid">
            {(activeTab === 'supplier' ? filteredSuppliers : filteredDonors).map(item => {
              const sItem = item as Supplier
              const dItem = item as Donor
              const key = activeTab === 'supplier' ? sItem.supplierId : dItem.donorId
              return (
              <div className="card data-card" key={key} onClick={() => activeTab === 'supplier' ? openSupplierDrawer(sItem) : openDonorDrawer(dItem)}>
                <div className="data-card-title">{sItem.supplierName || dItem.donorName}</div>
                <div className="data-card-subtitle">{sItem.contactPerson || dItem.contact}</div>
                <div className="data-card-row">
                  <span className="data-card-label">Telepon</span>
                  <span className="data-card-value">{sItem.phone || dItem.phone || '-'}</span>
                </div>
                <div className="data-card-row">
                  <span className="data-card-label">Email</span>
                  <span className="data-card-value">{sItem.email || dItem.email || '-'}</span>
                </div>
              </div>
            )})}
          </div>
        </div>

        {drawerOpen && (
          <div className="card card-padded panel-drawer">
            <div className="drawer-title">
              {activeTab === 'supplier' ? (editSupplier ? 'Edit Supplier' : 'Tambah Supplier') : (editDonor ? 'Edit Donatur' : 'Tambah Donatur')}
            </div>
            {activeTab === 'supplier' ? (
              <>
                <div className="form-group">
                  <label className="form-label">Nama</label>
                  <input className="input" value={supForm.supplierName} onChange={e => setSupForm(p => ({ ...p, supplierName: e.target.value }))} />
                </div>
                <div className="form-group">
                  <label className="form-label">Kontak Person</label>
                  <input className="input" value={supForm.contactPerson} onChange={e => setSupForm(p => ({ ...p, contactPerson: e.target.value }))} />
                </div>
                <div className="form-group">
                  <label className="form-label">No Telepon</label>
                  <input className="input" value={supForm.phone} onChange={e => setSupForm(p => ({ ...p, phone: e.target.value }))} />
                </div>
                <div className="form-group">
                  <label className="form-label">Email</label>
                  <input className="input" type="email" value={supForm.email} onChange={e => setSupForm(p => ({ ...p, email: e.target.value }))} />
                </div>
                <div className="form-group">
                  <label className="form-label">Alamat</label>
                  <textarea className="textarea" value={supForm.address} onChange={e => setSupForm(p => ({ ...p, address: e.target.value }))} />
                </div>
                <hr className="drawer-separator" />
                <div className="drawer-btn-row">
                  <button className="btn btn-primary flex-1" onClick={saveSupplier}>Simpan</button>
                  {editSupplier && <button className="btn btn-danger" onClick={() => deleteSupplier(editSupplier.supplierId)}>Hapus</button>}
                  <button className="btn btn-outline" onClick={() => setDrawerOpen(false)}>Batal</button>
                </div>
              </>
            ) : (
              <>
                <div className="form-group">
                  <label className="form-label">Nama Donatur</label>
                  <input className="input" value={donForm.donorName} onChange={e => setDonForm(p => ({ ...p, donorName: e.target.value }))} />
                </div>
                <div className="form-group">
                  <label className="form-label">Nama Kontak</label>
                  <input className="input" value={donForm.contact} onChange={e => setDonForm(p => ({ ...p, contact: e.target.value }))} />
                </div>
                <div className="form-group">
                  <label className="form-label">No Telepon</label>
                  <input className="input" value={donForm.phone} onChange={e => setDonForm(p => ({ ...p, phone: e.target.value }))} />
                </div>
                <div className="form-group">
                  <label className="form-label">Email</label>
                  <input className="input" type="email" value={donForm.email} onChange={e => setDonForm(p => ({ ...p, email: e.target.value }))} />
                </div>
                <div className="form-group">
                  <label className="form-label">Alamat</label>
                  <textarea className="textarea" value={donForm.address} onChange={e => setDonForm(p => ({ ...p, address: e.target.value }))} />
                </div>
                <hr className="drawer-separator" />
                <div className="drawer-btn-row">
                  <button className="btn btn-primary flex-1" onClick={saveDonor}>Simpan</button>
                  {editDonor && <button className="btn btn-danger" onClick={() => deleteDonor(editDonor.donorId)}>Hapus</button>}
                  <button className="btn btn-outline" onClick={() => setDrawerOpen(false)}>Batal</button>
                </div>
              </>
            )}
          </div>
        )}
      </div>
    </>
  )
}
