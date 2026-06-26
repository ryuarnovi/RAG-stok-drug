import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import TopBar from '../components/TopBar';
import type { User } from '../types';

export default function AppLayout() {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const stored = localStorage.getItem('kepo_user');
    if (stored) {
      try {
        setCurrentUser(JSON.parse(stored));
      } catch {
        localStorage.removeItem('kepo_user');
        navigate('/login');
      }
    } else {
      navigate('/login');
    }
  }, []);

  const activePage = location.pathname.split('/')[2] || 'dashboard';

  const handleNavigate = (page: string) => {
    navigate(`/app/${page}`);
    setSidebarOpen(false);
  };

  return (
    <div className="app-layout">
      <div className={`sidebar-overlay${sidebarOpen ? ' show' : ''}`} onClick={() => setSidebarOpen(false)} />
      <Sidebar
        currentUser={currentUser}
        activePage={activePage}
        onNavigate={handleNavigate}
        isOpen={sidebarOpen}
      />
      <div className="main-content">
        <TopBar onToggleSidebar={() => setSidebarOpen(v => !v)} />
        <div className="page-content">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
