import { useState, useEffect } from 'react';

interface TopBarProps {
  onToggleSidebar?: () => void;
}

export default function TopBar({ onToggleSidebar }: TopBarProps) {
  const [userName, setUserName] = useState('');

  useEffect(() => {
    const stored = localStorage.getItem('kepo_user');
    if (stored) {
      try {
        const user = JSON.parse(stored);
        setUserName(user.fullName || '');
      } catch {
        // ignore parse error
      }
    }
  }, []);

  return (
    <header className="main-header">
      <div className="main-header-left">
        <button className="btn-hamburger" onClick={onToggleSidebar} aria-label="Toggle sidebar">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <line x1="3" y1="6" x2="21" y2="6" />
            <line x1="3" y1="12" x2="21" y2="12" />
            <line x1="3" y1="18" x2="21" y2="18" />
          </svg>
        </button>
        <div style={{ marginLeft: 8 }}>
          <h2>KEPO</h2>
          <p>Command Center</p>
        </div>
      </div>
      <div className="main-header-right">
        <button className="btn-alerts" title="Notifikasi">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
        </button>
        <div className="header-user">
          <span>{userName}</span>
        </div>
      </div>
    </header>
  );
}
