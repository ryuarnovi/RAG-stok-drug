import { useState } from 'react';
import type { User, PageId } from '../types';
import type { JSX } from 'react';

interface SidebarProps {
  currentUser: User | null;
  activePage: string;
  onNavigate: (page: string) => void;
  isOpen?: boolean;
}

interface NavItem {
  label: string;
  page: PageId;
  icon: JSX.Element;
}

interface NavGroup {
  label: string;
  key: string;
  roles: string[] | null;
  items: NavItem[];
}

const iconStyle = { width: 18, height: 18, flexShrink: 0 };

const navGroups: NavGroup[] = [
  {
    label: 'OPERASI',
    key: 'operasi',
    roles: ['ADMIN', 'SHELTER_OFFICER'],
    items: [
      {
        label: 'Event Bencana',
        page: 'event',
        icon: (
          <svg style={iconStyle} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
            <line x1="16" y1="2" x2="16" y2="6" />
            <line x1="8" y1="2" x2="8" y2="6" />
            <line x1="3" y1="10" x2="21" y2="10" />
          </svg>
        ),
      },
      {
        label: 'Shelter',
        page: 'shelter',
        icon: (
          <svg style={iconStyle} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
        ),
      },
      {
        label: 'Pengungsi',
        page: 'refugee',
        icon: (
          <svg style={iconStyle} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
          </svg>
        ),
      },
    ],
  },
  {
    label: 'LOGISTIK',
    key: 'logistik',
    roles: ['ADMIN', 'HEALTH_OFFICER'],
    items: [
      {
        label: 'Inventaris',
        page: 'medicine',
        icon: (
          <svg style={iconStyle} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12 2v20M2 12h20" />
          </svg>
        ),
      },
      {
        label: 'Distribusi',
        page: 'distribution',
        icon: (
          <svg style={iconStyle} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="1" y="3" width="15" height="13" />
            <polygon points="16 8 20 8 23 11 23 16 16 16 16 8" />
            <circle cx="5.5" cy="18.5" r="2.5" />
            <circle cx="18.5" cy="18.5" r="2.5" />
          </svg>
        ),
      },
      {
        label: 'Barcode Scanner',
        page: 'barcode',
        icon: (
          <svg style={iconStyle} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 7V5a2 2 0 0 1 2-2h2" /><path d="M17 3h2a2 2 0 0 1 2 2v2" /><path d="M21 17v2a2 2 0 0 1-2 2h-2" /><path d="M7 21H5a2 2 0 0 1-2-2v-2" />
            <rect x="7" y="7" width="10" height="10" rx="2" />
          </svg>
        ),
      },
      {
        label: 'Permintaan Obat',
        page: 'medrequest',
        icon: (
          <svg style={iconStyle} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2" />
            <rect x="8" y="2" width="8" height="4" rx="1" ry="1" />
            <path d="M12 11v6" />
            <path d="M9 14h6" />
          </svg>
        ),
      },
      {
        label: 'Supplier/Donor',
        page: 'supp_donor',
        icon: (
          <svg style={iconStyle} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
          </svg>
        ),
      },
    ],
  },
  {
    label: 'ANALISIS',
    key: 'analisis',
    roles: null,
    items: [
      {
        label: 'AI Assistant',
        page: 'ai',
        icon: (
          <svg style={iconStyle} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
          </svg>
        ),
      },
      {
        label: 'Prediksi',
        page: 'prediction',
        icon: (
          <svg style={iconStyle} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
            <polyline points="17 6 23 6 23 12" />
          </svg>
        ),
      },
    ],
  },
  {
    label: 'LAPORAN',
    key: 'laporan',
    roles: null,
    items: [
      {
        label: 'Report Center',
        page: 'report',
        icon: (
          <svg style={iconStyle} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <line x1="16" y1="13" x2="8" y2="13" />
            <line x1="16" y1="17" x2="8" y2="17" />
            <polyline points="10 9 9 9 8 9" />
          </svg>
        ),
      },
    ],
  },
  {
    label: 'SYSTEM',
    key: 'system',
    roles: null,
    items: [
      {
        label: 'Pengaturan',
        page: 'settings',
        icon: (
          <svg style={iconStyle} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="12" cy="12" r="3" />
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
          </svg>
        ),
      },
    ],
  },
];

export default function Sidebar({ currentUser, activePage, onNavigate, isOpen }: SidebarProps) {
  const [expandedGroups, setExpandedGroups] = useState<Record<string, boolean>>({
    operasi: true,
    logistik: true,
    analisis: true,
    laporan: true,
    system: true,
  });

  const toggleGroup = (key: string) => {
    setExpandedGroups(prev => ({ ...prev, [key]: !prev[key] }));
  };

  const userRole = currentUser?.role;

  const formatRole = (role: string) => {
    return role
      .split('_')
      .map(w => w.charAt(0) + w.slice(1).toLowerCase())
      .join(' ');
  };

  const visibleGroups = navGroups.filter(
    g => g.roles === null || (userRole && g.roles.includes(userRole))
  );

  return (
    <aside className={`sidebar${isOpen ? ' open' : ''}`}>
      <div className="sidebar-brand">
        <h1>KEPO</h1>
        <p>COMMAND CENTER</p>
      </div>
      <nav className="sidebar-nav">
        <button
          className={`sidebar-item${activePage === 'dashboard' ? ' active' : ''}`}
          onClick={() => onNavigate('dashboard')}
        >
          <svg style={iconStyle} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="3" width="7" height="7" />
            <rect x="14" y="3" width="7" height="7" />
            <rect x="14" y="14" width="7" height="7" />
            <rect x="3" y="14" width="7" height="7" />
          </svg>
          Dashboard
        </button>
        {visibleGroups.map(group => (
          <div key={group.key}>
            <div
              className="sidebar-section-label"
              onClick={() => toggleGroup(group.key)}
              style={{ cursor: 'pointer', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
            >
              {group.label}
              <svg
                width="12"
                height="12"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                style={{ transform: expandedGroups[group.key] ? 'rotate(0deg)' : 'rotate(-90deg)', transition: 'transform 0.15s ease' }}
              >
                <polyline points="6 9 12 15 18 9" />
              </svg>
            </div>
            {expandedGroups[group.key] && group.items.map(item => (
              <button
                key={item.page}
                className={`sidebar-item${activePage === item.page ? ' active' : ''}`}
                onClick={() => onNavigate(item.page)}
              >
                {item.icon}
                {item.label}
              </button>
            ))}
          </div>
        ))}
      </nav>
      <div className="sidebar-user">
        <div className="sidebar-avatar">
          {currentUser ? currentUser.fullName.charAt(0).toUpperCase() : '?'}
        </div>
        <div className="sidebar-user-info">
          <h4>{currentUser?.fullName || 'User'}</h4>
          <p>
            <span className="badge badge-primary">
              {currentUser ? formatRole(currentUser.role) : 'N/A'}
            </span>
          </p>
        </div>
      </div>
    </aside>
  );
}
