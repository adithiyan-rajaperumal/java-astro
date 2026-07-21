import { useState } from 'react';
import LocationSearch from './LocationSearch';
import { t } from '../i18n/translations';

function BirthForm({ onSubmit, initialValues = {}, submitLabel = 'Submit', lang = 'en' }) {
  const [name, setName] = useState(initialValues.name || '');
      const [dateText, setDateText] = useState(() => {
    if (initialValues.date && initialValues.date.includes('-')) {
      const [y, m, d] = initialValues.date.split('-');
      return `${d}/${m}/${y}`;
    }
    return '';
  });
  const [time, setTime] = useState(initialValues.time || '');
  const [location, setLocation] = useState(initialValues.location || null);
  const [ayanamsa, setAyanamsa] = useState(initialValues.ayanamsa || 'LAHIRI');

  const handleDateChange = (val) => {
    const clean = val.replace(/\D/g, '');
    let formatted = clean;
    if (clean.length > 2) {
      formatted = `${clean.slice(0, 2)}/${clean.slice(2)}`;
    }
    if (clean.length > 4) {
      formatted = `${clean.slice(0, 2)}/${clean.slice(2, 4)}/${clean.slice(4, 8)}`;
    }
    setDateText(formatted);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!name.trim() || !dateText || !time || !location) {
      alert('Please fill in all details, including selecting a location from the search suggestions.');
      return;
    }

    const dateRegex = /^(\d{2})\/(\d{2})\/(\d{4})$/;
    const match = dateText.match(dateRegex);
    if (!match) {
      alert('Please enter date in DD/MM/YYYY format (e.g. 15/05/1995)');
      return;
    }

    const day = parseInt(match[1]);
    const month = parseInt(match[2]);
    const year = parseInt(match[3]);

    if (month < 1 || month > 12 || day < 1 || day > 31 || year < 1800 || year > 2100) {
      alert('Please enter a valid date.');
      return;
    }
    
    // Parse time (HH:MM)
    const [hour, minute] = time.split(':').map(Number);

    onSubmit({
      name,
      year,
      month,
      day,
      hour,
      minute,
      second: 0,
      latitude: location.latitude,
      longitude: location.longitude,
      ayanamsa
    });
  };

  return (
    <form onSubmit={handleSubmit} className="card">
      <div style={{ marginBottom: '15px' }}>
        <label>{t('name', lang)}</label>
        <input
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Enter name"
          required
        />
      </div>

      <div className="grid-2">
        <div>
          <label>{t('birthDate', lang)} (DD/MM/YYYY)</label>
          <input
            type="text"
            value={dateText}
            onChange={(e) => handleDateChange(e.target.value)}
            placeholder="DD/MM/YYYY"
            maxLength="10"
            required
          />
        </div>
        <div>
          <label>{t('birthTime', lang)}</label>
          <input
            type="time"
            value={time}
            onChange={(e) => setTime(e.target.value)}
            required
          />
        </div>
      </div>

      <div style={{ marginBottom: '15px' }}>
        <label>{t('birthLocation', lang)}</label>
        <LocationSearch
          value={location}
          onChange={setLocation}
          placeholder="Type city name and select suggestion..."
        />
      </div>

      <div style={{ marginBottom: '20px' }}>
        <label>{t('ayanamsa', lang)}</label>
        <select value={ayanamsa} onChange={(e) => setAyanamsa(e.target.value)}>
          <option value="LAHIRI">Lahiri (Chitra Paksha)</option>
          <option value="RAMAN">Raman</option>
          <option value="KP">KP (Krishnamurti Padhdhati)</option>
          <option value="SAYANA">Sayana (Tropical)</option>
        </select>
      </div>

      <button type="submit" className="btn-primary" style={{ width: '100%' }}>
        {t(submitLabel, lang) || submitLabel}
      </button>
    </form>
  );
}

export default BirthForm;
