import { useState, useEffect } from 'react';
import './App.css';
import PanchangamPage from './pages/PanchangamPage';
import HoroscopePage from './pages/HoroscopePage';
import MatchingPage from './pages/MatchingPage';
import SettingsPage from './pages/SettingsPage';
import { t } from './i18n/translations';

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
      const saved = localStorage.getItem('drikvedic_settings');
      return saved ? JSON.parse(saved) : DEFAULT_SETTINGS;
    } catch {
      return DEFAULT_SETTINGS;
    }
  });

  const [activeTab, setActiveTab] = useState('panchangam');

  useEffect(() => {
    localStorage.setItem('drikvedic_settings', JSON.stringify(settings));
  }, [settings]);

  const handleSettingsChange = (newSettings) => {
    setSettings(newSettings);
  };

  const tabs = [
    { id: 'panchangam', labelKey: 'panchangam', icon: '🏠' },
    { id: 'horoscope', labelKey: 'horoscope', icon: '📜' },
    { id: 'matching', labelKey: 'matching', icon: '💑' },
    { id: 'settings', labelKey: 'settings', icon: '⚙️' }
  ];

  return (
    <>
      {/* Top Navbar */}
      <nav className="navbar">
        <div className="navbar-brand">
          🕉️ {t('appTitle', settings.language)}
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
              <span>{t(tab.labelKey, settings.language)}</span>
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
            <div>{t(tab.labelKey, settings.language)}</div>
          </div>
        ))}
      </nav>
    </>
  );
}

export default App;
