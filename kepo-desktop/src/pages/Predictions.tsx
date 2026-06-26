import { useState, useEffect, useCallback } from 'react'
import { api } from '../services/api'

const mockData = {
  summary: 'Berdasarkan analisis data historis dan kondisi terkini, terdapat potensi krisis logistik di 3 shelter dalam 7 hari ke depan. Prioritas utama adalah distribusi obat antibiotik dan makanan siap saji ke Posko Cisarua dan Posko Megamendung.',
  shelterForecasts: [
    'Posko Cisarua: Kapasitas 500 jiwa diproyeksikan penuh dalam 3 hari (kritis)',
    'Posko Megamendung: Kapasitas 300 jiwa, proyeksi 85% dalam 5 hari (waspada)',
    'Posko Cipayung: Kapasitas 400 jiwa, proyeksi 60% dalam 7 hari (aman)',
    'Posko Cisarua II: Kapasitas 250 jiwa, proyeksi 45% dalam 7 hari (aman)',
  ],
  priorities: [
    'Distribusi logistik ke Posko Cisarua (prioritas 1)',
    'Tambahan tenaga medis ke Posko Megamendung (prioritas 2)',
    'Evakuasi preventif untuk area rawan longsor (prioritas 3)',
    'Stok obat malaria dan diare untuk 500 jiwa (prioritas 4)',
  ],
  medPredictions: [
    'Paracetamol: Stok 500 unit - cukup untuk 10 hari (aman)',
    'Amoxicillin: Stok 30 unit - akan habis dalam 2 hari (kritis)',
    'Oralit: Stok 0 unit - kebutuhan mendesak (kritis)',
    'Vitamin: Stok 200 unit - cukup untuk 14 hari (aman)',
  ],
  lackingLogistics: [
    'Makanan siap saji - kekurangan 1.200 porsi',
    'Air mineral - kekurangan 800 botol',
    'Selimut - kekurangan 500 unit',
    'Obat antibiotik - kekurangan 200 strip',
  ],
}

export default function Predictions() {
  const [data, setData] = useState<typeof mockData | null>(null)
  const [loading, setLoading] = useState(false)

  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      const res = await api.getPredictions()
      setData(res as typeof mockData)
    } catch {
      setData(mockData)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchData() }, [fetchData])

  if (!data) return <div className="text-center" style={{ padding: 40, color: 'var(--on-surface-variant)' }}>Memuat data...</div>

  return (
    <>
      <div className="flex items-center justify-between mb-20">
        <h2 style={{ fontFamily: "'Plus Jakarta Sans', sans-serif", fontSize: 22, fontWeight: 700 }}>Analisis Prediktif & Proyeksi</h2>
        <button className="btn btn-primary" onClick={fetchData} disabled={loading}>{loading ? 'Memproses...' : 'Jalankan Analisis'}</button>
      </div>

      <div className="card card-padded mb-20" style={{ borderLeft: '4px solid var(--info)' }}>
        <h4 style={{ fontSize: 15, fontWeight: 700, marginBottom: 8 }}>Executive Summary</h4>
        <p style={{ fontSize: 13, color: 'var(--on-surface-variant)', lineHeight: 1.6 }}>{data.summary}</p>
      </div>

      <div className="grid-2col">
        <div>
          <div className="card card-padded mb-20">
            <h4 style={{ fontSize: 15, fontWeight: 700, marginBottom: 12 }}>Proyeksi Kapasitas Shelter</h4>
            {data.shelterForecasts.map((item, i) => {
              const variant = item.includes('kritis') ? 'kritis' : item.includes('waspada') ? 'waspada' : 'aman'
              return <div key={i} className={`prediction-card ${variant}`}><div className="prediction-title">Proyeksi {i + 1}</div><div className="prediction-desc">{item}</div></div>
            })}
          </div>
          <div className="card card-padded">
            <h4 style={{ fontSize: 15, fontWeight: 700, marginBottom: 12 }}>Prioritas Alokasi</h4>
            {data.priorities.map((item, i) => (
              <div key={i} className="prediction-card info"><div className="prediction-title">Prioritas {i + 1}</div><div className="prediction-desc">{item}</div></div>
            ))}
          </div>
        </div>
        <div>
          <div className="card card-padded mb-20">
            <h4 style={{ fontSize: 15, fontWeight: 700, marginBottom: 12 }}>Analisis Kebutuhan Logistik</h4>
            {data.lackingLogistics.map((item, i) => (
              <div key={i} className="prediction-card kritis"><div className="prediction-title">Kebutuhan Mendesak</div><div className="prediction-desc">{item}</div></div>
            ))}
          </div>
          <div className="card card-padded">
            <h4 style={{ fontSize: 15, fontWeight: 700, marginBottom: 12 }}>Proyeksi Stok Obat</h4>
            {data.medPredictions.map((item, i) => {
              const variant = item.includes('kritis') ? 'kritis' : item.includes('waspada') ? 'waspada' : 'aman'
              return <div key={i} className={`prediction-card ${variant}`}><div className="prediction-title">Proyeksi {i + 1}</div><div className="prediction-desc">{item}</div></div>
            })}
          </div>
        </div>
      </div>
    </>
  )
}
