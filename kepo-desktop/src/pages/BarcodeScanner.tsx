import { useState, useEffect, useRef, useCallback } from 'react'
import { api } from '../services/api'
import type { Medicine } from '../types'

export default function BarcodeScanner() {
  const [medicines, setMedicines] = useState<Medicine[]>([])
  const [selected, setSelected] = useState<Medicine | null>(null)
  const [barcodeUrl, setBarcodeUrl] = useState('')
  const [scanInput, setScanInput] = useState('')
  const [search, setSearch] = useState('')
  const [showScanner, setShowScanner] = useState(false)
  const [cameraReady, setCameraReady] = useState(false)
  const [scanning, setScanning] = useState(false)
  const videoRef = useRef<HTMLVideoElement>(null)
  const streamRef = useRef<MediaStream | null>(null)
  const scanTimerRef = useRef<number>(0)
  const lastCodeRef = useRef<string>('')
  const canvasRef = useRef<HTMLCanvasElement | null>(null)

  useEffect(() => {
    api.getMedicines().then(setMedicines).catch(() => {})
  }, [])

  const stopCamera = useCallback(() => {
    setScanning(false)
    if (scanTimerRef.current) {
      cancelAnimationFrame(scanTimerRef.current)
      scanTimerRef.current = 0
    }
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(t => t.stop())
      streamRef.current = null
    }
    setCameraReady(false)
  }, [])

  const scanFrame = useCallback(async () => {
    const video = videoRef.current
    if (!video || !video.videoWidth) {
      scanTimerRef.current = requestAnimationFrame(scanFrame)
      return
    }

    if (!canvasRef.current) {
      canvasRef.current = document.createElement('canvas')
    }
    const canvas = canvasRef.current
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    const ctx = canvas.getContext('2d')
    if (!ctx) {
      scanTimerRef.current = requestAnimationFrame(scanFrame)
      return
    }

    ctx.drawImage(video, 0, 0)
    const dataUrl = canvas.toDataURL('image/jpeg', 0.7)

    try {
      const res = await fetch('/api/barcode/decode', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ image: dataUrl }),
      }).then(r => r.json())

      if (res.code && res.code !== lastCodeRef.current) {
        lastCodeRef.current = res.code
        stopCamera()
        const medicine = await api.barcodeLookup(res.code)
        setBarcodeUrl(`/api/barcode/generate?code=${medicine.medicineCode}&t=${Date.now()}`)
        setShowScanner(false)
        setTimeout(() => setSelected(medicine), 150)
        return
      }
    } catch {
      // ignore scan errors, keep scanning
    }

    if (scanning) {
      scanTimerRef.current = requestAnimationFrame(scanFrame)
    }
  }, [scanning, stopCamera])

  // Start camera when popup opens
  useEffect(() => {
    if (!showScanner) return
    setCameraReady(false)
    lastCodeRef.current = ''
    const start = async () => {
      await new Promise(r => setTimeout(r, 200))
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: 'environment', width: { ideal: 640 }, height: { ideal: 480 } },
        })
        streamRef.current = stream
        const video = videoRef.current
        if (video) {
          video.srcObject = stream
          video.onloadedmetadata = () => {
            video.play().then(() => {
              setCameraReady(true)
              setScanning(true)
            }).catch(() => {})
          }
        }
      } catch {
        alert('Tidak dapat mengakses kamera. Pastikan izin kamera diberikan.')
        setShowScanner(false)
      }
    }
    start()
    return () => stopCamera()
  }, [showScanner, stopCamera])

  // Start continuous scan loop when camera is ready
  useEffect(() => {
    if (!cameraReady || !scanning) return
    scanTimerRef.current = requestAnimationFrame(scanFrame)
    return () => {
      if (scanTimerRef.current) {
        cancelAnimationFrame(scanTimerRef.current)
        scanTimerRef.current = 0
      }
    }
  }, [cameraReady, scanning, scanFrame])

  const showBarcode = (m: Medicine) => {
    setBarcodeUrl(`/api/barcode/generate?code=${m.medicineCode}&t=${Date.now()}`)
    setSelected(m)
  }

  const handleLookup = async () => {
    if (!scanInput.trim()) return
    try {
      const res = await api.barcodeLookup(scanInput.trim().toUpperCase())
      setBarcodeUrl(`/api/barcode/generate?code=${res.medicineCode}&t=${Date.now()}`)
      setSelected(res)
      setScanInput('')
    } catch {
      alert('Obat tidak ditemukan untuk kode: ' + scanInput)
    }
  }

  const closeScanner = () => {
    stopCamera()
    setShowScanner(false)
  }

  const filtered = medicines.filter(m =>
    m.medicineName.toLowerCase().includes(search.toLowerCase()) ||
    m.medicineCode.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <>
      <div className="search-bar mb-20">
        <input className="search-input" placeholder="Cari obat..." value={search} onChange={e => setSearch(e.target.value)} />
        <button className="btn btn-primary" onClick={() => setShowScanner(true)}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: 4 }}>
            <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" />
            <circle cx="12" cy="13" r="4" />
          </svg>
          Scan Kamera
        </button>
      </div>

      <div className="cards-grid">
        {filtered.length === 0 && (
          <div className="card card-padded" style={{ gridColumn: '1 / -1', textAlign: 'center', padding: 40, color: 'var(--on-surface-variant)' }}>
            Obat tidak ditemukan
          </div>
        )}
        {filtered.map(m => (
          <div className="card data-card" key={m.medicineId} onClick={() => showBarcode(m)}>
            <div className="data-card-title">{m.medicineName}</div>
            <div className="data-card-subtitle">
              <span>{m.medicineCode}</span>
              <span className="data-card-kategori">{m.category}</span>
            </div>
            <div className="barcode-container">
              <img
                className="barcode-img"
                src={`/api/barcode/generate?code=${m.medicineCode}`}
                alt={m.medicineCode}
                onError={e => {
                  const el = e.target as HTMLImageElement
                  if (el.style.display === 'none') return
                  el.style.display = 'none'
                  const fb = el.nextElementSibling
                  if (fb) (fb as HTMLElement).style.display = 'flex'
                }}
              />
              <div className="barcode-fallback">{m.medicineCode}</div>
            </div>
            <div className="data-card-row">
              <span className="data-card-label">Stok</span>
              <span className="data-card-value">{m.stockQuantity} {m.unit}</span>
            </div>
            <div className="data-card-row">
              <span className="data-card-label">Kadaluarsa</span>
              <span className="data-card-value">{m.expiryDate || '-'}</span>
            </div>
          </div>
        ))}
      </div>

      <div className="search-bar" style={{ marginTop: 20 }}>
        <input className="search-input" placeholder="Atau masukkan kode manual (contoh: MED-001)" value={scanInput} onChange={e => setScanInput(e.target.value.toUpperCase())} onKeyDown={e => e.key === 'Enter' && handleLookup()} />
        <button className="btn btn-primary" onClick={handleLookup}>Cari Kode</button>
      </div>

      {selected && (
        <div className="drawer-overlay" onClick={() => { setSelected(null); setBarcodeUrl('') }} style={{ zIndex: 1100 }}>
          <div className="card card-padded" onClick={e => e.stopPropagation()} style={{ textAlign: 'center', maxWidth: 420, width: '90%', padding: 24 }}>
            <div className="flex items-center justify-between mb-16">
              <div style={{ textAlign: 'left' }}>
                <div className="drawer-title" style={{ marginBottom: 0 }}>{selected.medicineName}</div>
                <div style={{ fontSize: 12, color: 'var(--on-surface-variant)', marginTop: 2 }}>{selected.medicineCode}</div>
              </div>
              <button className="btn btn-outline btn-sm" onClick={() => { setSelected(null); setBarcodeUrl('') }}>Tutup</button>
            </div>
            <div style={{ background: '#fff', borderRadius: 8, padding: '16px 0' }}>
              {barcodeUrl && <img src={barcodeUrl} alt={selected.medicineCode} style={{ maxWidth: '100%', height: 'auto', display: 'block', margin: '0 auto' }} />}
            </div>
            <div className="barcode-popup-info">
              <span>Batch: <strong>{selected.batchNumber || '-'}</strong></span>
              <span>Stok: <strong>{selected.stockQuantity} {selected.unit}</strong></span>
              <span>Exp: <strong>{selected.expiryDate || '-'}</strong></span>
            </div>
          </div>
        </div>
      )}

      {showScanner && (
        <div className="drawer-overlay" onClick={closeScanner}>
          <div className="scanner-popup" onClick={e => e.stopPropagation()}>
            <div className="scanner-popup-header">
              <h3>Scan Barcode Obat</h3>
              <button className="btn btn-outline btn-sm" onClick={closeScanner}>Tutup</button>
            </div>
            <div className="scanner-popup-body" style={{ position: 'relative' }}>
              {!cameraReady && <div className="scanner-loading">Mengakses kamera...</div>}
              <video ref={videoRef} className="scanner-video" style={{ display: cameraReady ? 'block' : 'none' }} autoPlay playsInline muted />
              {cameraReady && <div className="scanner-overlay-line" />}
            </div>
            <div className="scanner-hint">
              {scanning ? 'Memindai barcode secara otomatis...' : 'Menyiapkan kamera...'}
            </div>
          </div>
        </div>
      )}
    </>
  )
}