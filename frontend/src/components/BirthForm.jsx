import { useState, useEffect } from 'react';
import LocationSearch from './LocationSearch';
import ClearableInput from './ClearableInput';
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

  useEffect(() => {
    if (initialValues.ayanamsa) {
      setAyanamsa(initialValues.ayanamsa);
    }
    if (initialValues.location) {
      setLocation(initialValues.location);
    }
  }, [initialValues.ayanamsa, initialValues.location]);

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
      location,
      ayanamsa,
      panchangamSystem: 'DRIK_TIRUKANITHAM'
    });
  };

  return (
    <form onSubmit={handleSubmit} className="card">
      <div style={{ marginBottom: '15px' }}>
        <label>{t('name', lang)}</label>
        <ClearableInput
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          onClear={() => setName('')}
          placeholder={t('namePlaceholder', lang) || "Enter name"}
          required
        />
      </div>

      <div className="grid-2">
        <div>
          <label>{t('birthDate', lang)} (DD/MM/YYYY)</label>
          <ClearableInput
            type="text"
            value={dateText}
            onChange={(e) => handleDateChange(e.target.value)}
            onClear={() => setDateText('')}
            placeholder="DD/MM/YYYY"
            maxLength="10"
            required
          />
        </div>
        <div>
          <label>{t('birthTime', lang)}</label>
          <ClearableInput
            type="time"
            value={time}
            onChange={(e) => setTime(e.target.value)}
            onClear={() => setTime('')}
            required
          />
        </div>
      </div>

      <div style={{ marginBottom: '15px' }}>
        <label>{t('birthLocation', lang)}</label>
        <LocationSearch
          value={location}
          onChange={setLocation}
          placeholder={t('locationPlaceholder', lang) || "Type city name and select suggestion..."}
        />
      </div>

      <div style={{ marginBottom: '20px' }}>
        <label>{t('ayanamsa', lang)}</label>
        <select value={ayanamsa} onChange={(e) => setAyanamsa(e.target.value)}>
          <option value="LAHIRI">{t('ayanamsaLahiri', lang)}</option>
          <option value="KP">{t('ayanamsaKP', lang)}</option>
          <option value="RAMAN">{t('ayanamsaRaman', lang)}</option>
          <option value="SURYA_SIDDHANTA">{t('ayanamsaSurya', lang)}</option>
          <option value="PUSHYAPAKSHA">{t('ayanamsaPushyapaksha', lang)}</option>
        </select>
      </div>

      <button type="submit" className="btn-primary" style={{ width: '100%' }}>
        {t(submitLabel, lang) || submitLabel}
      </button>
    </form>
  );
}

export default BirthForm;
