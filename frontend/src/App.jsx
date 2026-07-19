import { useState, useEffect } from 'react';
import './App.css';
import PanchangamPage from './pages/PanchangamPage';
import HoroscopePage from './pages/HoroscopePage';
import MatchingPage from './pages/MatchingPage';
import SettingsPage from './pages/SettingsPage';

const DEFAULT_SETTINGS = {
  language: 'en',
  ayanamsa: 'LAHIRI',
  location: {
    label: 'Chennai, Tamil Nadu, India',
    latitude: 13.0827,
    longitude: 80.2707
  }
};

function App() {
  const [settings, setSettings] = useState(() => {
    try {
      const saved = localStorage.getItem('stellavedic_settings');
      return saved ? JSON.parse(saved) : DEFAULT_SETTINGS;
    } catch {
      return DEFAULT_SETTINGS;
    }
  });

  const [activeTab, setActiveTab] = useState('panchangam');

  useEffect(() => {
    localStorage.setItem('stellavedic_settings', JSON.stringify(settings));
  }, [settings]);

  const handleSettingsChange = (newSettings) => {
    setSettings(newSettings);
  };

  const tabs = [
    { id: 'panchangam', label: 'Panchangam', icon: '🏠' },
    { id: 'horoscope', label: 'Horoscope', icon: '📜' },
    { id: 'matching', label: 'Matching', icon: '💑' },
    { id: 'settings', label: 'Settings', icon: '⚙️' }
  ];

  const getLanguageLabel = (code) => {
    switch (code) {
      case 'ta': return 'தமிழ்';
      case 'hi': return 'हिंदी';
      case 'kn': return 'ಕನ್ನಡ';
      case 'te': return 'తెలుగు';
      case 'ml': return 'മലയാളം';
      default: return 'EN';
    }
  };

  return (
    <>
      {/* Top Navbar */}
      <nav className="navbar">
        <div className="navbar-brand">
          🕉️ StellaVedic
        </div>
        <div className="navbar-actions">
          {settings.location && (
            <span style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
              📍 {settings.location.label.split(',')[0]}
            </span>
          )}
          <select 
            value={settings.language} 
            onChange={(e) => handleSettingsChange({ ...settings, language: e.target.value })}
            className="navbar-dropdown"
          >
            <option value="en">English</option>
            <option value="ta">தமிழ்</option>
            <option value="hi">हिंदी</option>
            <option value="kn">ಕನ್ನಡ</option>
            <option value="te">తెలుగు</option>
            <option value="ml">മലയാളം</option>
          </select>
        </div>
      </nav>

      {/* Main Layout containing Side Rail + Content */}
      <div className="main-wrapper">
        {/* Navigation Rail (Desktop) */}
        <aside className="nav-rail">
          {tabs.map((tab) => (
            <div
              key={tab.id}
              className={`nav-rail-item ${activeTab === tab.id ? 'active' : ''}`}
              onClick={() => setActiveTab(tab.id)}
            >
              <span style={{ fontSize: '20px' }}>{tab.icon}</span>
              <span>{tab.label}</span>
            </div>
          ))}
        </aside>

        {/* Content Page Area */}
        <main className="content-area">
          {activeTab === 'panchangam' && <PanchangamPage settings={settings} />}
          {activeTab === 'horoscope' && <HoroscopePage settings={settings} />}
          {activeTab === 'matching' && <MatchingPage settings={settings} />}
          {activeTab === 'settings' && (
            <SettingsPage settings={settings} onSettingsChange={handleSettingsChange} />
          )}
        </main>
      </div>

      {/* Bottom Navigation (Mobile) */}
      <nav className="bottom-nav">
        {tabs.map((tab) => (
          <div
            key={tab.id}
            className={`bottom-nav-item ${activeTab === tab.id ? 'active' : ''}`}
            onClick={() => setActiveTab(tab.id)}
          >
            <span>{tab.icon}</span>
            <div>{tab.label}</div>
          </div>
        ))}
      </nav>
    </>
  );
}

export default App;
