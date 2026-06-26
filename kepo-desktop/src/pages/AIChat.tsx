import { useState, useRef, useEffect } from 'react'
import { api } from '../services/api'

interface Message {
  id: number
  sender: 'ai' | 'user'
  text: string
  timestamp: Date
}

const suggestions = [
  { icon: '📊', title: 'Analisis Stok Kritis', desc: 'Obat apa yang perlu segera dipesan?' },
  { icon: '🚚', title: 'Distribusi Prioritas', desc: 'Shelter mana yang butuh suplai utama?' },
  { icon: '🔮', title: 'Prediksi Kebutuhan', desc: 'Perkiraan kebutuhan 7 hari ke depan' },
  { icon: '🏠', title: 'Optimasi Shelter', desc: 'Rekomendasi penyebaran pengungsi' },
  { icon: '📋', title: 'Shelter Kritis', desc: 'Shelter dengan kondisi darurat' },
  { icon: '💊', title: 'Obat Hampir Expired', desc: 'Obat yang harus segera digunakan' },
]

const quickActions = [
  'Analisis stok kritis',
  'Prioritas distribusi',
  'Shelter paling kritis',
  'Prediksi kebutuhan',
  'Laporan kondisi terkini',
]

function formatAIResponse(text: string): string {
  return text
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n{2,}/g, '\n\n')
    .trim()
}

export default function AIChat() {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 1,
      sender: 'ai',
      text: 'Selamat datang di **KEPO AI Assistant**.\n\nSaya terhubung ke database langsung dan siap membantu analisis operasional bencana secara real-time.\n\n**Contoh pertanyaan:**\n• Analisis stok obat kritis\n• Shelter paling membutuhkan distribusi\n• Prediksi kebutuhan 7 hari ke depan\n• Laporan kondisi shelter saat ini',
      timestamp: new Date(),
    },
  ])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [showSuggestions, setShowSuggestions] = useState(true)
  const endRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, loading])

  useEffect(() => {
    if (!loading) inputRef.current?.focus()
  }, [loading])

  const sendMessage = async (text: string) => {
    if (!text.trim() || loading) return
    const userMsg: Message = { id: Date.now(), sender: 'user', text, timestamp: new Date() }
    setMessages(prev => [...prev, userMsg])
    setInput('')
    setLoading(true)
    setShowSuggestions(false)
    try {
      const res = await api.aiChat(text)
      const aiMsg: Message = {
        id: Date.now() + 1,
        sender: 'ai',
        text: res.response || 'Tidak ada respons dari AI.',
        timestamp: new Date(),
      }
      setMessages(prev => [...prev, aiMsg])
    } catch {
      const aiMsg: Message = {
        id: Date.now() + 1,
        sender: 'ai',
        text: 'Maaf, layanan AI sedang tidak tersedia. Silakan coba lagi.',
        timestamp: new Date(),
      }
      setMessages(prev => [...prev, aiMsg])
    } finally {
      setLoading(false)
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage(input)
    }
  }

  return (
    <div className="ai-page" style={{ height: 'calc(100vh - 80px)' }}>
      <div className="ai-chat-container">
        <div className="ai-suggestions">
          <div className="ai-suggest-header">
            <h3>Saran Pertanyaan</h3>
            <button className="ai-close-btn" onClick={() => setShowSuggestions(false)}>✕</button>
          </div>
          <p className="ai-suggest-subtitle">Klik untuk langsung bertanya</p>
          <div className="ai-suggest-list">
            {suggestions.map((s, i) => (
              <div key={i} className="ai-suggest-card" onClick={() => sendMessage(s.title)}>
                <span className="ai-suggest-icon">{s.icon}</span>
                <div>
                  <h4>{s.title}</h4>
                  <p>{s.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
        <div className="ai-chat-main">
          <div className="ai-chat-header">
            <div className="ai-header-left">
              <div className="ai-avatar">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
                </svg>
              </div>
              <div>
                <span className="brand">KEPO AI</span>
                <span className="ai-model-badge">Gemini 2.0 Flash</span>
              </div>
            </div>
            <div className="ai-header-right">
              <div className="ai-status-dot">
                <span className="dot" />
                <span>Active</span>
              </div>
              <button className="ai-clear-btn" onClick={() => {
                setMessages([{
                  id: Date.now(),
                  sender: 'ai',
                  text: 'Percakapan telah dihapus. Silakan ajukan pertanyaan baru.',
                  timestamp: new Date(),
                }])
                setShowSuggestions(true)
              }} title="Hapus percakapan">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polyline points="3 6 5 6 21 6" />
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                </svg>
              </button>
            </div>
          </div>
          <div className="ai-messages">
            {messages.length === 1 && (
              <div className="ai-welcome">
                <div className="ai-welcome-icon">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                    <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
                  </svg>
                </div>
                <h3>KEPO AI Assistant</h3>
                <p>Terhubung ke database dengan Gemini 2.0 Flash. Ajukan pertanyaan tentang stok obat, distribusi, shelter, atau kondisi pengungsi.</p>
              </div>
            )}
            {messages.map(m => (
              <div key={m.id} className={`ai-msg ${m.sender}`}>
                {m.sender === 'ai' && (
                  <div className="ai-msg-avatar">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
                    </svg>
                  </div>
                )}
                <div className="ai-msg-content">
                  <div className="ai-msg-sender">{m.sender === 'ai' ? 'KEPO AI' : 'Anda'}</div>
                  <div className="ai-msg-text" dangerouslySetInnerHTML={{ __html: formatAIResponse(m.text) }} />
                  <div className="ai-msg-time">{m.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</div>
                </div>
              </div>
            ))}
            {loading && (
              <div className="ai-msg ai">
                <div className="ai-msg-avatar">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
                  </svg>
                </div>
                <div className="ai-msg-content">
                  <div className="ai-msg-sender">KEPO AI</div>
                  <div className="ai-typing">
                    <span className="ai-typing-dot" />
                    <span className="ai-typing-dot" />
                    <span className="ai-typing-dot" />
                  </div>
                </div>
              </div>
            )}
            <div ref={endRef} />
          </div>
          <div className="ai-input-bar">
            <div className="ai-quick-actions">
              {showSuggestions && quickActions.slice(0, 4).map((q, i) => (
                <button key={i} className="ai-quick-btn" onClick={() => sendMessage(q)}>{q}</button>
              ))}
              {!showSuggestions && (
                <button className="ai-quick-btn" onClick={() => setShowSuggestions(true)}>💡 Saran</button>
              )}
            </div>
            <div className="ai-input-row">
              <input
                ref={inputRef}
                className="ai-input"
                placeholder="Tanyakan analisis, prediksi, atau rekomendasi..."
                value={input}
                onChange={e => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
              />
              <button
                className="ai-send-btn"
                onClick={() => sendMessage(input)}
                disabled={loading || !input.trim()}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="22" y1="2" x2="11" y2="13" />
                  <polygon points="22 2 15 22 11 13 2 9 22 2" />
                </svg>
              </button>
            </div>
            <div className="ai-footer">AI menganalisis data dari database secara real-time — hasil bisa berbeda tergantung data terkini</div>
          </div>
        </div>
      </div>
    </div>
  )
}
