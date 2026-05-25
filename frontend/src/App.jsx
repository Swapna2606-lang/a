import React, { useState, useEffect, useCallback } from 'react';
import { sessionApi } from './services/api';
import Dashboard from './pages/Dashboard';
import './App.css';

function pickDefaultSession(list) {
  if (!list?.length) return null;
  const active = list.find(s => String(s.status).toUpperCase() === 'ACTIVE');
  return active ?? list[0];
}

export default function App() {
  const [sessions, setSessions] = useState([]);
  const [activeSession, setActiveSession] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const loadSessions = useCallback(async () => {
    try {
      setLoadError(null);
      const res = await sessionApi.getAll();
      const list = Array.isArray(res.data) ? res.data : [];
      setSessions(list);

      setActiveSession(prev => {
        if (prev && list.some(s => s.id === prev.id)) return prev;
        return pickDefaultSession(list);
      });
    } catch (err) {
      console.error('Failed to load sessions:', err);
      setLoadError(
        err.code === 'ECONNABORTED'
          ? 'Backend not responding. Start it with: mvn spring-boot:run (in backend folder)'
          : 'Could not load sessions. Check backend at http://127.0.0.1:8080/api/sessions'
      );
    } finally {
      setLoading(false);
    }
  }, []);

  const handleStartSession = async (session) => {
    if (!session) return;
    setActionLoading(true);
    try {
      const res = await sessionApi.start(session.id);
      setActiveSession(res.data);
      await loadSessions();
    } catch (err) {
      console.error('Failed to start session:', err);
      setLoadError('Failed to start session. See browser console for details.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCreateDemoSession = async () => {
    setActionLoading(true);
    try {
      const res = await sessionApi.create({
        sessionName: 'Data Structures & Algorithms',
        subject: 'Computer Science',
        teacherName: 'Dr. Priya Sharma',
        roomNumber: 'CS-301',
      });
      await sessionApi.start(res.data.id);
      await loadSessions();
    } catch (err) {
      console.error('Failed to create session:', err);
      setLoadError('Failed to create session. Is the backend running?');
    } finally {
      setActionLoading(false);
    }
  };

  useEffect(() => {
    loadSessions();
    const interval = setInterval(loadSessions, 10000);
    return () => clearInterval(interval);
  }, [loadSessions]);

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="loading-orb" />
        <p>Initializing Classroom Intelligence...</p>
      </div>
    );
  }

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-brand">
          <div className="brand-dot" />
          <span className="brand-name">ClassroomIQ</span>
          <span className="brand-sub">Real-Time Intelligence System</span>
        </div>
        <nav className="header-nav">
          {sessions.map(s => (
            <button
              key={s.id}
              className={`nav-session ${activeSession?.id === s.id ? 'active' : ''}`}
              onClick={() => setActiveSession(s)}
            >
              <span className={`status-dot ${s.status.toLowerCase()}`} />
              {s.sessionName}
            </button>
          ))}
        </nav>
        <div className="header-info">
          <span className="live-badge">● LIVE</span>
          <span className="time">{new Date().toLocaleTimeString()}</span>
        </div>
      </header>

      <main className="app-main">
        {activeSession ? (
          <Dashboard sessionId={activeSession.id} sessionInfo={activeSession} />
        ) : (
          <div className="no-session">
            <div className="no-session-icon">📡</div>
            <h2>No Active Session</h2>
            {loadError && <p className="no-session-error">{loadError}</p>}
            {!loadError && sessions.length === 0 && (
              <p>No sessions found. Create one or restart the backend (demo data loads on startup).</p>
            )}
            {!loadError && sessions.length > 0 && (
              <p>Select a session below or start it to begin monitoring.</p>
            )}
            <div className="no-session-actions">
              {sessions.map(s => (
                <button
                  key={s.id}
                  type="button"
                  className="session-action-btn"
                  disabled={actionLoading}
                  onClick={() =>
                    String(s.status).toUpperCase() === 'ACTIVE'
                      ? setActiveSession(s)
                      : handleStartSession(s)
                  }
                >
                  {String(s.status).toUpperCase() === 'ACTIVE'
                    ? `Open ${s.sessionName}`
                    : `Start ${s.sessionName}`}
                </button>
              ))}
              {sessions.length === 0 && (
                <button
                  type="button"
                  className="session-action-btn primary"
                  disabled={actionLoading}
                  onClick={handleCreateDemoSession}
                >
                  {actionLoading ? 'Creating…' : 'Create demo session'}
                </button>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}