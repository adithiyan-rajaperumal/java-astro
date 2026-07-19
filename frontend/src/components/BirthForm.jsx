import { useState } from 'react';
import LocationSearch from './LocationSearch';
import { t } from '../i18n/translations';

function BirthForm({ onSubmit, initialValues = {}, submitLabel = 'Submit', lang = 'en' }) {
  const [name, setName] = useState(initialValues.name || '');
  const [date, setDate] = useState(initialValues.date || '');
  const [time, setTime] = useState(initialValues.time || '');
  const [location, setLocation] = useState(initialValues.location || null);
  const [ayanamsa, setAyanamsa] = useState(initialValues.ayanamsa || 'LAHIRI');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!name.trim() || !date || !time || !location) {
      alert('Please fill in all details, including selecting a location from the search suggestions.');
      return;
    }
    
    // Parse date (YYYY-MM-DD)
    const [year, month, day] = date.split('-').map(Number);
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
          <label>{t('birthDate', lang)}</label>
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
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
