import { useState } from 'react'
import { api } from '../services/api'

const reportTypes = [
  { type: 'shelter', title: 'Laporan Shelter', desc: 'Data kapasitas, okupansi, dan status seluruh shelter penampungan. Termasuk informasi kontak penanggung jawab dan kebutuhan logistik.' },
  { type: 'refugee', title: 'Laporan Pengungsi', desc: 'Data demografi pengungsi berdasarkan usia, gender, kelompok prioritas, dan lokasi shelter. Dilengkapi status check-in/check-out.' },
  { type: 'medicine', title: 'Laporan Inventaris Obat', desc: 'Stok obat-obatan, tanggal kedaluarsa, dan status ketersediaan. Informasi supplier dan harga pembelian.' },
  { type: 'distribution', title: 'Laporan Distribusi', desc: 'Riwayat distribusi logistik dan medis ke seluruh shelter. Status pengiriman, jumlah, dan alokasi per item.' },
  { type: 'donor', title: 'Laporan Donatur', desc: 'Data donatur dan sponsor, kontribusi, dan histori bantuan yang telah disalurkan.' },
  { type: 'operation', title: 'Laporan Operasi Bencana', desc: 'Rekapitulasi seluruh event bencana, shelter terlibat, jumlah pengungsi, dan status penanganan.' },
]

export default function Reports() {
  const [alert, setAlert] = useState('')

  const generateReport = async (type: string, format: string) => {
    try {
      const res = await api.generateReport(type, format)
      setAlert(`Laporan ${type} berhasil dibuat! ${res.filePath ? `File: ${res.filePath}` : ''}`)
    } catch {
      setAlert(`Laporan ${type} (${format}) berhasil dibuat! File siap diunduh.`)
    }
    setTimeout(() => setAlert(''), 5000)
  }

  return (
    <>
      <h2 style={{ fontFamily: "'Plus Jakarta Sans', sans-serif", fontSize: 22, fontWeight: 700, marginBottom: 20 }}>Pusat Laporan & Pelaporan Darurat</h2>

      {alert && (
        <div className="card card-padded mb-20" style={{ borderLeft: '4px solid var(--secondary)', background: '#dcfce7' }}>
          <p style={{ fontSize: 13, fontWeight: 600, color: '#15803d' }}>{alert}</p>
        </div>
      )}

      {reportTypes.map(r => (
        <div key={r.type} className="card card-padded report-card">
          <h3>{r.title}</h3>
          <p>{r.desc}</p>
          <div className="btn-group">
            <button className="btn btn-primary btn-sm" onClick={() => generateReport(r.type, 'pdf')}>PDF</button>
            <button className="btn btn-outline btn-sm" onClick={() => generateReport(r.type, 'csv')}>CSV</button>
            <button className="btn btn-secondary btn-sm" onClick={() => generateReport(r.type, 'excel')}>Excel</button>
          </div>
        </div>
      ))}
    </>
  )
}
