import LocationSearch from '../components/LocationSearch';
import { t } from '../i18n/translations';

function SettingsPage({ settings, onSettingsChange }) {
  const languages = [
    { code: 'en', name: 'English', script: 'English' },
    { code: 'ta', name: 'Tamil', script: 'தமிழ்' },
    { code: 'hi', name: 'Hindi', script: 'हिंदी' },
    { code: 'kn', name: 'Kannada', script: 'ಕನ್ನಡ' },
    { code: 'te', name: 'Telugu', script: 'తెలుగు' },
    { code: 'ml', name: 'Malayalam', script: 'മലയാളം' },
  ];

  const handleLanguageSelect = (langCode) => {
    onSettingsChange({ ...settings, language: langCode });
  };

  const handleLocationChange = (loc) => {
    onSettingsChange({ ...settings, location: loc });
  };

  const handleAyanamsaChange = (e) => {
    onSettingsChange({ ...settings, ayanamsa: e.target.value });
  };

  return (
    <div>
      <h2 className="title-gold">{t('settings', settings.language)}</h2>
      
      <div className="card">
        <h3 style={{ marginTop: 0, color: 'var(--accent-gold)' }}>{t('appLanguage', settings.language)}</h3>
        <div className="lang-selector-grid">
          {languages.map((lang) => (
            <div
              key={lang.code}
              className={`lang-card ${settings.language === lang.code ? 'active' : ''}`}
              onClick={() => handleLanguageSelect(lang.code)}
            >
              <h3>{lang.script}</h3>
              <span>{lang.name}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginTop: 0, color: 'var(--accent-gold)' }}>{t('defaultLocation', settings.language)}</h3>
        <LocationSearch
          value={settings.location}
          onChange={handleLocationChange}
          placeholder={t('searchLocationPlaceholder', settings.language)}
        />
        {settings.location && (
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginTop: '5px' }}>
            {t('currentDefault', settings.language)}: Lat {settings.location.latitude.toFixed(4)}, Lon {settings.location.longitude.toFixed(4)}
          </p>
        )}
      </div>

      <div className="card">
        <h3 style={{ marginTop: 0, color: 'var(--accent-gold)' }}>{t('calculationEngineSettings', settings.language)}</h3>
        <label>{t('ayanamsa', settings.language)}</label>
        <select value={settings.ayanamsa} onChange={handleAyanamsaChange}>
          <option value="LAHIRI">Lahiri (Chitra Paksha)</option>
          <option value="RAMAN">Raman</option>
          <option value="KP">KP (Krishnamurti Padhdhati)</option>
          <option value="SAYANA">Sayana (Tropical)</option>
        </select>
      </div>

      <div className="card" style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
        <h3 style={{ marginTop: 0, color: 'var(--accent-gold)' }}>{t('aboutTitle', settings.language)}</h3>
        <p>{t('version', settings.language)} 1.0.0 (Release 2026)</p>
        <p style={{ marginTop: '10px' }}>
          {t('aboutText', settings.language)}
        </p>
      </div>
    </div>
  );
}

export default SettingsPage;
