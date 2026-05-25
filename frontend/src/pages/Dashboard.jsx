import React, { useState, useEffect, useCallback } from 'react';
import { sessionApi, studentApi } from '../services/api';
import {
  RadialBarChart, RadialBar, ResponsiveContainer,
  AreaChart, Area, XAxis, YAxis, Tooltip, CartesianGrid,
  PieChart, Pie, Cell
} from 'recharts';

const EMOTION_COLORS = {
  ENGAGED: '#00ff9d',
  ATTENTIVE: '#4ade80',
  NEUTRAL: '#94a3b8',
  CONFUSED: '#fbbf24',
  BORED: '#f97316',
  DISTRACTED: '#ef4444',
  HAPPY: '#a78bfa',
};

const EMOTION_ICONS = {
  ENGAGED: '🎯', ATTENTIVE: '👁️', NEUTRAL: '😐',
  CONFUSED: '🤔', BORED: '😴', DISTRACTED: '📱', HAPPY: '😊'
};

function EngagementRing({ value, label, color }) {
  const r = 42;
  const circ = 2 * Math.PI * r;
  const dash = (value / 100) * circ;

  return (
    <div className="engagement-ring">
      <svg viewBox="0 0 100 100" width="100" height="100">
        <circle cx="50" cy="50" r={r} fill="none" stroke="#1e2a3a" strokeWidth="8" />
        <circle
          cx="50" cy="50" r={r} fill="none"
          stroke={color} strokeWidth="8"
          strokeDasharray={`${dash} ${circ - dash}`}
          strokeLinecap="round"
          transform="rotate(-90 50 50)"
          style={{ transition: 'stroke-dasharray 0.8s ease' }}
        />
        <text x="50" y="46" textAnchor="middle" fill="white" fontSize="18" fontWeight="700" fontFamily="JetBrains Mono">
          {Math.round(value)}
        </text>
        <text x="50" y="60" textAnchor="middle" fill="#94a3b8" fontSize="8">
          {label}
        </text>
      </svg>
    </div>
  );
}

function StudentCard({ student, onRaiseHand }) {
  const emotion = student.currentEmotion || 'NEUTRAL';
  const engColor = student.engagementScore > 70 ? '#00ff9d' : student.engagementScore > 40 ? '#fbbf24' : '#ef4444';

  return (
    <div className={`student-card ${student.hasRaisedHand ? 'hand-raised' : ''} ${student.engagementScore < 30 ? 'at-risk' : ''}`}>
      <div className="student-card-header">
        <div className="student-avatar">
          {student.name.charAt(0)}
          <span className="emotion-badge">{EMOTION_ICONS[emotion]}</span>
        </div>
        <div className="student-info">
          <span className="student-name">{student.name.split(' ')[0]}</span>
          <span className="student-seat">{student.seatPosition}</span>
        </div>
        {student.hasRaisedHand && <div className="hand-indicator">✋</div>}
      </div>
      <div className="student-bars">
        <div className="mini-bar">
          <span>ENG</span>
          <div className="bar-track">
            <div className="bar-fill" style={{ width: `${student.engagementScore || 0}%`, background: engColor }} />
          </div>
          <span className="bar-val" style={{ color: engColor }}>{Math.round(student.engagementScore || 0)}</span>
        </div>
        <div className="mini-bar">
          <span>ATT</span>
          <div className="bar-track">
            <div className="bar-fill" style={{ width: `${student.attentionLevel || 0}%`, background: '#818cf8' }} />
          </div>
          <span className="bar-val" style={{ color: '#818cf8' }}>{Math.round(student.attentionLevel || 0)}</span>
        </div>
      </div>
    </div>
  );
}

function SeatingChart({ students }) {
  const grid = {};
  students.forEach(s => {
    if (s.seatPosition) grid[s.seatPosition] = s;
  });

  const rows = ['R1', 'R2', 'R3', 'R4'];
  const cols = ['C1', 'C2', 'C3', 'C4', 'C5'];

  return (
    <div className="seating-chart">
      <div className="teacher-desk">🎓 Teacher's Desk</div>
      <div className="seating-grid">
        {rows.map(row => (
          <div key={row} className="seat-row">
            {cols.map(col => {
              const key = `${row}${col}`;
              const student = grid[key];
              if (!student) return <div key={key} className="seat empty" />;

              const score = student.engagementScore || 0;
              const color = score > 70 ? '#00ff9d' : score > 40 ? '#fbbf24' : '#ef4444';

              return (
                <div key={key} className="seat occupied" style={{ '--seat-color': color }}
                  title={`${student.name} | Eng: ${Math.round(score)} | ${student.currentEmotion || 'NEUTRAL'}`}>
                  <div className="seat-name">{student.name.split(' ')[0].substring(0, 4)}</div>
                  <div className="seat-score" style={{ color }}>{Math.round(score)}</div>
                  {student.hasRaisedHand && <div className="seat-hand">✋</div>}
                  {student.currentEmotion === 'CONFUSED' && <div className="seat-alert">?</div>}
                </div>
              );
            })}
          </div>
        ))}
      </div>
    </div>
  );
}

export default function Dashboard({ sessionId, sessionInfo }) {
  const [stats, setStats] = useState(null);
  const [students, setStudents] = useState([]);
  const [engagementHistory, setEngagementHistory] = useState([]);
  const [activeTab, setActiveTab] = useState('overview');

  const loadData = useCallback(async () => {
    try {
      const [dashRes, studRes] = await Promise.all([
        sessionApi.getDashboard(sessionId),
        sessionApi.getStudents(sessionId)
      ]);
      setStats(dashRes.data);

      const presentStudents = studRes.data.filter(s => s.attendanceStatus === 'PRESENT');
      setStudents(presentStudents);

      // Build engagement history
      const now = new Date();
      setEngagementHistory(prev => {
        const point = {
          time: now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
          engagement: dashRes.data.averageEngagement || 0,
          attention: dashRes.data.averageAttention || 0,
        };
        const updated = [...prev, point];
        return updated.slice(-20); // keep last 20 data points
      });
    } catch (err) {
      console.error('Dashboard load error:', err);
    }
  }, [sessionId]);

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 3000);
    return () => clearInterval(interval);
  }, [loadData]);

  if (!stats) return <div className="dash-loading">Loading dashboard...</div>;

  const emotionData = Object.entries(stats.emotionDistribution || {}).map(([name, value]) => ({
    name, value, color: EMOTION_COLORS[name] || '#94a3b8'
  }));

  const attendanceRate = stats.totalStudents > 0
    ? Math.round((stats.presentStudents / stats.totalStudents) * 100) : 0;

  return (
    <div className="dashboard">
      {/* Header Stats */}
      <div className="stats-bar">
        <div className="stat-item">
          <EngagementRing value={stats.averageEngagement || 0} label="ENGAGEMENT" color="#00ff9d" />
        </div>
        <div className="stat-item">
          <EngagementRing value={stats.averageAttention || 0} label="ATTENTION" color="#818cf8" />
        </div>
        <div className="stat-item">
          <EngagementRing value={stats.teacherEffectivenessScore || 0} label="EFFECTIVENESS" color="#f59e0b" />
        </div>
        <div className="stat-item">
          <EngagementRing value={attendanceRate} label="ATTENDANCE" color="#38bdf8" />
        </div>

        <div className="stat-counts">
          <div className="count-item">
            <span className="count-num">{stats.presentStudents}</span>
            <span className="count-label">Present</span>
          </div>
          <div className="count-item">
            <span className="count-num">{stats.totalStudents}</span>
            <span className="count-label">Total</span>
          </div>
          <div className="count-item">
            <span className="count-num">{stats.activeParticipants}</span>
            <span className="count-label">Active</span>
          </div>
          <div className="count-item">
            <span className="count-num" style={{ color: '#ef4444' }}>{stats.atRiskStudents?.length || 0}</span>
            <span className="count-label">At Risk</span>
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="tab-nav">
        {['overview', 'seating', 'students', 'insights'].map(tab => (
          <button key={tab} className={`tab-btn ${activeTab === tab ? 'active' : ''}`}
            onClick={() => setActiveTab(tab)}>
            {tab.charAt(0).toUpperCase() + tab.slice(1)}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      <div className="tab-content">
        {activeTab === 'overview' && (
          <div className="overview-grid">
            {/* Engagement Chart */}
            <div className="panel chart-panel">
              <h3 className="panel-title">Live Engagement & Attention</h3>
              <ResponsiveContainer width="100%" height={220}>
                <AreaChart data={engagementHistory}>
                  <defs>
                    <linearGradient id="engGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#00ff9d" stopOpacity={0.3} />
                      <stop offset="95%" stopColor="#00ff9d" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="attGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#818cf8" stopOpacity={0.3} />
                      <stop offset="95%" stopColor="#818cf8" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1e2a3a" />
                  <XAxis dataKey="time" tick={{ fill: '#64748b', fontSize: 10 }} interval={4} />
                  <YAxis domain={[0, 100]} tick={{ fill: '#64748b', fontSize: 10 }} />
                  <Tooltip contentStyle={{ background: '#0f172a', border: '1px solid #1e2a3a', borderRadius: 8 }}
                    labelStyle={{ color: '#94a3b8' }} />
                  <Area type="monotone" dataKey="engagement" stroke="#00ff9d" fill="url(#engGrad)" strokeWidth={2} name="Engagement" />
                  <Area type="monotone" dataKey="attention" stroke="#818cf8" fill="url(#attGrad)" strokeWidth={2} name="Attention" />
                </AreaChart>
              </ResponsiveContainer>
            </div>

            {/* Emotion Distribution */}
            <div className="panel emotion-panel">
              <h3 className="panel-title">Emotion Distribution</h3>
              {emotionData.length > 0 ? (
                <>
                  <ResponsiveContainer width="100%" height={180}>
                    <PieChart>
                      <Pie data={emotionData} cx="50%" cy="50%" innerRadius={50} outerRadius={80}
                        dataKey="value" paddingAngle={3}>
                        {emotionData.map((entry, idx) => (
                          <Cell key={idx} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip contentStyle={{ background: '#0f172a', border: '1px solid #1e2a3a', borderRadius: 8 }}
                        labelStyle={{ color: '#94a3b8' }} />
                    </PieChart>
                  </ResponsiveContainer>
                  <div className="emotion-legend">
                    {emotionData.map(e => (
                      <div key={e.name} className="emotion-legend-item">
                        <span style={{ background: e.color }} className="legend-dot" />
                        <span className="legend-icon">{EMOTION_ICONS[e.name]}</span>
                        <span className="legend-name">{e.name}</span>
                        <span className="legend-val">{e.value}</span>
                      </div>
                    ))}
                  </div>
                </>
              ) : (
                <div className="no-data">No emotion data yet</div>
              )}
            </div>

            {/* Alerts Panel */}
            <div className="panel alerts-panel">
              <h3 className="panel-title">⚠ Insights & Alerts</h3>
              <div className="alerts-list">
                {stats.atRiskStudents?.length > 0 && (
                  <div className="alert alert-danger">
                    <span className="alert-icon">🔴</span>
                    <div>
                      <div className="alert-title">Low Engagement Detected</div>
                      <div className="alert-desc">{stats.atRiskStudents.map(s => s.name).join(', ')} need attention</div>
                    </div>
                  </div>
                )}
                {(stats.emotionDistribution?.CONFUSED || 0) >= 3 && (
                  <div className="alert alert-warning">
                    <span className="alert-icon">🟡</span>
                    <div>
                      <div className="alert-title">Confusion Spike</div>
                      <div className="alert-desc">{stats.emotionDistribution.CONFUSED} students appear confused</div>
                    </div>
                  </div>
                )}
                {stats.averageEngagement > 75 && (
                  <div className="alert alert-success">
                    <span className="alert-icon">🟢</span>
                    <div>
                      <div className="alert-title">High Engagement</div>
                      <div className="alert-desc">Class engagement is excellent at {Math.round(stats.averageEngagement)}%</div>
                    </div>
                  </div>
                )}
                {stats.activeParticipants > 0 && (
                  <div className="alert alert-info">
                    <span className="alert-icon">🔵</span>
                    <div>
                      <div className="alert-title">Active Participation</div>
                      <div className="alert-desc">{stats.activeParticipants} students actively participating</div>
                    </div>
                  </div>
                )}
                {students.filter(s => s.hasRaisedHand).map(s => (
                  <div key={s.id} className="alert alert-hand">
                    <span className="alert-icon">✋</span>
                    <div>
                      <div className="alert-title">Hand Raised</div>
                      <div className="alert-desc">{s.name} has a question</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {activeTab === 'seating' && (
          <div className="panel">
            <h3 className="panel-title">Smart Seating Analytics</h3>
            <div className="seating-legend">
              <span><span className="dot" style={{ background: '#00ff9d' }} /> High (&gt;70)</span>
              <span><span className="dot" style={{ background: '#fbbf24' }} /> Medium (40-70)</span>
              <span><span className="dot" style={{ background: '#ef4444' }} /> Low (&lt;40)</span>
            </div>
            <SeatingChart students={students} />
          </div>
        )}

        {activeTab === 'students' && (
          <div className="students-grid">
            {students.map(student => (
              <StudentCard key={student.id} student={student} />
            ))}
          </div>
        )}

        {activeTab === 'insights' && (
          <div className="insights-view">
            <div className="panel">
              <h3 className="panel-title">🏆 Top Engaged Students</h3>
              {stats.topEngagedStudents?.map((s, i) => (
                <div key={s.id} className="rank-row">
                  <span className="rank-num">#{i + 1}</span>
                  <div className="rank-avatar">{s.name.charAt(0)}</div>
                  <div className="rank-info">
                    <span className="rank-name">{s.name}</span>
                    <span className="rank-seat">{s.seatPosition}</span>
                  </div>
                  <div className="rank-score" style={{ color: '#00ff9d' }}>
                    {Math.round(s.engagementScore || 0)}%
                  </div>
                  <div className="rank-bar">
                    <div style={{ width: `${s.engagementScore || 0}%`, background: '#00ff9d', height: '6px', borderRadius: '3px', transition: 'width 0.5s' }} />
                  </div>
                </div>
              ))}
            </div>

            <div className="panel">
              <h3 className="panel-title">🔴 Students Needing Attention</h3>
              {stats.atRiskStudents?.length === 0 && (
                <div className="no-data">All students are engaged! 🎉</div>
              )}
              {stats.atRiskStudents?.map(s => (
                <div key={s.id} className="rank-row at-risk">
                  <div className="rank-avatar" style={{ background: '#ef444430', color: '#ef4444' }}>{s.name.charAt(0)}</div>
                  <div className="rank-info">
                    <span className="rank-name">{s.name}</span>
                    <span className="rank-seat">{s.currentEmotion} | Seat: {s.seatPosition}</span>
                  </div>
                  <div className="rank-score" style={{ color: '#ef4444' }}>
                    {Math.round(s.engagementScore || 0)}%
                  </div>
                </div>
              ))}
            </div>

            <div className="panel teacher-score-panel">
              <h3 className="panel-title">📊 Teaching Effectiveness Analysis</h3>
              <div className="effectiveness-breakdown">
                <div className="eff-item">
                  <span>Engagement Impact</span>
                  <div className="eff-bar"><div style={{ width: `${stats.averageEngagement || 0}%`, background: '#00ff9d' }} /></div>
                  <span>{Math.round(stats.averageEngagement || 0)}%</span>
                </div>
                <div className="eff-item">
                  <span>Attention Hold</span>
                  <div className="eff-bar"><div style={{ width: `${stats.averageAttention || 0}%`, background: '#818cf8' }} /></div>
                  <span>{Math.round(stats.averageAttention || 0)}%</span>
                </div>
                <div className="eff-item">
                  <span>Participation Rate</span>
                  <div className="eff-bar"><div style={{ width: `${stats.presentStudents > 0 ? (stats.activeParticipants / stats.presentStudents) * 100 : 0}%`, background: '#f59e0b' }} /></div>
                  <span>{stats.presentStudents > 0 ? Math.round((stats.activeParticipants / stats.presentStudents) * 100) : 0}%</span>
                </div>
                <div className="eff-total">
                  Overall Score: <strong style={{ color: '#f59e0b' }}>{Math.round(stats.teacherEffectivenessScore || 0)}%</strong>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
