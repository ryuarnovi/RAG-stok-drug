import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../services/api'
import type { DashboardStats, Distribution } from '../types'

const mockStats: DashboardStats = { totalShelters: 24, totalRefugees: 1847, criticalShelters: 4, activeEvents: 3, fullShelters: 7, criticalLogistics: 11 }
const mockAlerts = ['Shelter Cisarua mencapai 95% kapasitas', 'Stok obat antibiotik menipis di gudang pusat', 'Hujan lebat diprediksi 3 hari ke depan']
const mockDistributions: Distribution[] = [
  { distributionId: 1, docNum: 'DIST-001', shelterId: 1, shelterName: 'Posko Cisarua', itemType: 'Logistik', quantity: 120, status: 'SHIPPED' },
  { distributionId: 2, docNum: 'DIST-002', shelterId: 2, shelterName: 'Posko Megamendung', itemType: 'Medis', quantity: 50, status: 'RECEIVED' },
  { distributionId: 3, docNum: 'DIST-003', shelterId: 3, shelterName: 'Posko Cipayung', itemType: 'Logistik', quantity: 200, status: 'APPROVED' },
]
interface AutoDistSuggestion {
  id: number; description: string; status: string;
}

export default function Dashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [alerts, setAlerts] = useState<string[]>([])
  const [distributions, setDistributions] = useState<Distribution[]>([])
  const [autoSuggestions, setAutoSuggestions] = useState<AutoDistSuggestion[]>([])
  const [processingIds, setProcessingIds] = useState<Set<number>>(new Set())
  const [showAnalysis, setShowAnalysis] = useState<{ id: number; text: string } | null>(null)

  useEffect(() => {
    const fetchData = async () => {
      try { setStats(await api.getDashboardStats()) } catch { setStats(mockStats) }
      try { setAlerts(await api.getDashboardAlerts()) } catch { setAlerts(mockAlerts) }
      try { setDistributions(await api.getDashboardDistributions()) } catch { setDistributions(mockDistributions) }
      try { setAutoSuggestions(await api.getAutoDistSuggestions()) } catch { /* ignore */ }
    }
    fetchData()
  }, [])

  const handleAutoApprove = async (s: AutoDistSuggestion) => {
    if (processingIds.has(s.id)) return
    setProcessingIds(prev => new Set(prev).add(s.id))
    try {
      const shelterMatch = s.description.match(/Shelter\s+([^\s]+(?:\s+[^\s]+)*?)\s+membutuhkan/);
      const medMatch = s.description.match(/membutuhkan\s+(.+?)\./);
      const res = await api.approveAutoDist({
        shelterId: 1,
        shelterName: shelterMatch?.[1] || 'Shelter',
        medicineName: medMatch?.[1] || 'Obat',
        quantity: 30,
        itemType: 'OBAT',
      })
      setAutoSuggestions(prev => prev.map(x => x.id === s.id ? { ...x, status: 'APPROVED' } : x))
      if (res.analysis) {
        setShowAnalysis({ id: s.id, text: res.analysis })
      }
    } catch { /* ignore */ }
    setProcessingIds(prev => { const n = new Set(prev); n.delete(s.id); return n })
  }

  const handleAutoReject = async (s: AutoDistSuggestion) => {
    setAutoSuggestions(prev => prev.map(x => x.id === s.id ? { ...x, status: 'REJECTED' } : x))
  }

  const s = stats || mockStats
  const kpis = [
    { title: 'Total Shelter', value: s.totalShelters, subtitle: 'Tersebar di 6 kecamatan' },
    { title: 'Total Pengungsi', value: s.totalRefugees, subtitle: 'Jiwa terdata' },
    { title: 'Shelter Kritis', value: s.criticalShelters, subtitle: 'Perlu perhatian segera' },
    { title: 'Bencana Aktif', value: s.activeEvents, subtitle: 'Event berlangsung' },
    { title: 'Shelter Penuh', value: s.fullShelters, subtitle: 'Kapasitas maksimum' },
    { title: 'Kritis Logistik', value: s.criticalLogistics, subtitle: 'Butuh suplai' },
  ]

  const priorityGroups = [
    { label: 'Balita & Ibu Hamil', count: 342, color: 'var(--danger)' },
    { label: 'Lansia & Disabilitas', count: 156, color: 'var(--warning)' },
    { label: 'Sakit Kronis', count: 89, color: 'var(--info)' },
  ]

  return (
    <>
      <div className="kpi-row">
        {kpis.map(k => (
          <div className="card card-padded kpi-card" key={k.title}>
            <div>
              <div className="kpi-title">{k.title}</div>
              <div className="kpi-value">{k.value}</div>
              <div className="kpi-subtitle">{k.subtitle}</div>
            </div>
          </div>
        ))}
      </div>

      <div className="grid-2col mb-20">
        <div className="card card-padded">
          <h4 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16 }}>Status Kebutuhan & Kelompok Prioritas</h4>
          {priorityGroups.map(p => (
            <div key={p.label} className="flex items-center justify-between" style={{ padding: '12px 0', borderBottom: '1px solid var(--border)' }}>
              <div className="flex items-center gap-10">
                <div style={{ width: 10, height: 10, borderRadius: '50%', background: p.color }} />
                <span style={{ fontSize: 13, fontWeight: 600 }}>{p.label}</span>
              </div>
              <span style={{ fontSize: 15, fontWeight: 900 }}>{p.count}</span>
            </div>
          ))}
        </div>
        <div className="card card-padded">
          <div className="activity-header">
            <h4>Aktivitas Terbaru</h4>
            <Link to="/app/distribution">Lihat Semua</Link>
          </div>
          {distributions.map(d => (
            <div key={d.distributionId} className="flex items-center gap-10" style={{ padding: '10px 0', borderBottom: '1px solid var(--border)' }}>
              <div className="activity-icon" style={{ background: '#e0f2fe', color: 'var(--primary)' }}>D</div>
              <div>
                <div style={{ fontSize: 13, fontWeight: 600 }}>{d.shelterName}</div>
                <div style={{ fontSize: 11, color: 'var(--on-surface-variant)' }}>{d.itemType} - {d.quantity} unit</div>
              </div>
              <span className={`badge badge-${d.status === 'RECEIVED' ? 'safe' : d.status === 'SHIPPED' ? 'info' : d.status === 'APPROVED' ? 'warning' : ''}`} style={{ marginLeft: 'auto' }}>{d.status}</span>
            </div>
          ))}
        </div>
      </div>

      {alerts.length > 0 && (
        <div className="card card-padded mb-20" style={{ borderLeft: '4px solid var(--warning)' }}>
          <h4 style={{ fontSize: 15, fontWeight: 700, marginBottom: 8 }}>Peringatan & Notifikasi</h4>
          {alerts.map((a, i) => <p key={i} style={{ fontSize: 13, color: 'var(--on-surface-variant)', padding: '4px 0' }}>• {a}</p>)}
        </div>
      )}

      <div className="ai-hero-card">
        <div className="ai-hero-badge"><span className="dot" /> AI Auto-Distribution</div>
        <h3>Rekomendasi Distribusi Obat</h3>
        <p style={{ fontSize: 12, color: 'var(--ai-muted)', marginBottom: 12 }}>Sistem menyarankan distribusi otomatis ke shelter yang kekurangan. Approve atau reject setiap rekomendasi.</p>
        {autoSuggestions.filter(s => s.status !== 'NONE').map(s => (
          <div key={s.id} className="flex items-center justify-between" style={{ padding: '10px 0', borderTop: '1px solid rgba(255,255,255,0.1)' }}>
            <div style={{ flex: 1 }}>
              <p style={{ fontSize: 12, lineHeight: 1.4, color: 'var(--ai-muted)' }}>{s.description}</p>
              {s.status === 'APPROVED' && <span className="badge badge-safe" style={{ marginTop: 4 }}>APPROVED</span>}
              {s.status === 'REJECTED' && <span className="badge badge-expired" style={{ marginTop: 4 }}>REJECTED</span>}
            </div>
            {s.status === 'PENDING' && (
              <div className="flex gap-6" style={{ marginLeft: 12, flexShrink: 0 }}>
                <button
                  className="btn btn-primary" style={{ padding: '6px 14px', fontSize: 12 }}
                  onClick={() => handleAutoApprove(s)}
                  disabled={processingIds.has(s.id)}
                >{processingIds.has(s.id) ? '...' : 'Approve'}</button>
                <button
                  className="btn btn-outline" style={{ padding: '6px 14px', fontSize: 12, borderColor: 'rgba(255,255,255,0.3)', color: 'var(--ai-muted)' }}
                  onClick={() => handleAutoReject(s)}
                >Reject</button>
              </div>
            )}
            {s.status !== 'PENDING' && s.status !== 'NONE' && (
              <span style={{ fontSize: 11, color: 'var(--ai-muted)', marginLeft: 12, flexShrink: 0 }}>
                {s.status === 'APPROVED' ? 'Telah diproses' : 'Ditolak'}
              </span>
            )}
          </div>
        ))}
        {autoSuggestions.filter(s => s.status === 'NONE').length > 0 && (
          <p style={{ fontSize: 12, color: 'var(--ai-muted)' }}>Semua shelter terpantau aman.</p>
        )}
      </div>

      {showAnalysis && (
        <div className="card card-padded" style={{ borderLeft: '4px solid var(--primary)', position: 'relative' }}>
          <button onClick={() => setShowAnalysis(null)} style={{ position: 'absolute', top: 8, right: 12, cursor: 'pointer', background: 'none', border: 'none', fontSize: 18, color: 'var(--on-surface-variant)' }}>&times;</button>
          <h4 style={{ fontSize: 14, fontWeight: 700, marginBottom: 8 }}>Analisis AI</h4>
          <p style={{ fontSize: 13, lineHeight: 1.5, color: 'var(--on-surface-variant)', whiteSpace: 'pre-wrap' }}>{showAnalysis.text}</p>
        </div>
      )}
    </>
  )
}
