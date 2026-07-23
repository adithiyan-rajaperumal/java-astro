import { useState, useEffect } from 'react';
import LocationSearch from '../components/LocationSearch';
import { t } from '../i18n/translations';

function MatchingPage({ settings }) {
  const [boyName, setBoyName] = useState('');
  const [boyDate, setBoyDate] = useState('');
  const [boyTime, setBoyTime] = useState('');
  const [boyLocation, setBoyLocation] = useState(null);

  const [girlName, setGirlName] = useState('');
  const [girlDate, setGirlDate] = useState('');
  const [girlTime, setGirlTime] = useState('');
  const [girlLocation, setGirlLocation] = useState(null);

  const [matchingSystem, setMatchingSystem] = useState('ASHTA_KOOTA');
  const [strictness, setStrictness] = useState('MODERATE');
  const [ayanamsa, setAyanamsa] = useState(settings.ayanamsa || 'LAHIRI');

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    setResult(null);
  }, [settings.language]);

  const handleDateChange = (val, setter) => {
    const clean = val.replace(/\D/g, '');
    let formatted = clean;
    if (clean.length > 2) {
      formatted = `${clean.slice(0, 2)}/${clean.slice(2)}`;
    }
    if (clean.length > 4) {
      formatted = `${clean.slice(0, 2)}/${clean.slice(2, 4)}/${clean.slice(4, 8)}`;
    }
    setter(formatted);
  };

  const parseDateText = (dateText) => {
    const dateRegex = /^(\d{2})\/(\d{2})\/(\d{4})$/;
    const match = dateText.match(dateRegex);
    if (!match) return null;
    const day = parseInt(match[1]);
    const month = parseInt(match[2]);
    const year = parseInt(match[3]);
    if (month < 1 || month > 12 || day < 1 || day > 31 || year < 1800 || year > 2100) {
      return null;
    }
    return { year, month, day };
  };

  const handleMatch = async (e) => {
    e.preventDefault();
    if (!boyName.trim() || !boyDate || !boyTime || !boyLocation ||
        !girlName.trim() || !girlDate || !girlTime || !girlLocation) {
      alert('Please fill in all details for both Boy and Girl.');
      return;
    }

    const bDateParsed = parseDateText(boyDate);
    const gDateParsed = parseDateText(girlDate);

    if (!bDateParsed || !gDateParsed) {
      alert('Please enter valid dates in DD/MM/YYYY format (e.g. 15/05/1995).');
      return;
    }

    setLoading(true);
    setError(null);

    const [bHour, bMinute] = boyTime.split(':').map(Number);
    const [gHour, gMinute] = girlTime.split(':').map(Number);

    const payload = {
      boy: {
        name: boyName,
        year: bDateParsed.year,
        month: bDateParsed.month,
        day: bDateParsed.day,
        hour: bHour,
        minute: bMinute,
        second: 0,
        latitude: boyLocation.latitude,
        longitude: boyLocation.longitude,
        ayanamsa
      },
      girl: {
        name: girlName,
        year: gDateParsed.year,
        month: gDateParsed.month,
        day: gDateParsed.day,
        hour: gHour,
        minute: gMinute,
        second: 0,
        latitude: girlLocation.latitude,
        longitude: girlLocation.longitude,
        ayanamsa
      },
      matchingSystem,
      strictness
    };

    try {
      const response = await fetch('/api/v1/astrology/match', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Accept-Language': settings.language
        },
        body: JSON.stringify(payload)
      });
      if (response.ok) {
        const data = await response.json();
        setResult(data);
      } else {
        throw new Error('Failed to compute compatibility score.');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadPdf = async () => {
    if (!result) return;
    const bDateParsed = parseDateText(boyDate);
    const gDateParsed = parseDateText(girlDate);
    if (!bDateParsed || !gDateParsed) {
      alert('Please enter valid dates in DD/MM/YYYY format (e.g. 15/05/1995).');
      return;
    }
    const [bHour, bMinute] = boyTime.split(':').map(Number);
    const [gHour, gMinute] = girlTime.split(':').map(Number);

    const payload = {
      boy: {
        name: boyName,
        year: bDateParsed.year,
        month: bDateParsed.month,
        day: bDateParsed.day,
        hour: bHour,
        minute: bMinute,
        second: 0,
        latitude: boyLocation.latitude,
        longitude: boyLocation.longitude,
        ayanamsa
      },
      girl: {
        name: girlName,
        year: gDateParsed.year,
        month: gDateParsed.month,
        day: gDateParsed.day,
        hour: gHour,
        minute: gMinute,
        second: 0,
        latitude: girlLocation.latitude,
        longitude: girlLocation.longitude,
        ayanamsa
      },
      matchingSystem,
      strictness
    };

    try {
      const response = await fetch(`/api/v1/astrology/match/download-pdf?systemType=DRIK_TIRUKANITHAM`, {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Accept-Language': settings.language
        },
        body: JSON.stringify(payload)
      });
      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Compatibility_Report_${boyName}_${girlName}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      } else {
        alert('Failed to download PDF report.');
      }
    } catch (err) {
      console.error(err);
      alert('Error occurred while downloading PDF matching report.');
    }
  };

  const getVerdictClass = (verdict = '') => {
    const v = verdict.toLowerCase();
    if (v.includes('excellent')) return 'excellent';
    if (v.includes('good')) return 'good';
    if (v.includes('average') || v.includes('moderate')) return 'average';
    return 'not_recommended';
  };

  const getStatusText = (status) => {
    if (status === 'MATCHED') return '✅ Matched';
    if (status === 'MATCHED_VIA_NULLIFICATION') return '🔄 Matched via Exception';
    return '❌ Not Matched';
  };

  return (
    <div>
      <h2 className="title-gold">{t('matching', settings.language)}</h2>

      {!result && !loading && (
        <form onSubmit={handleMatch}>
          <div className="grid-2">
            {/* Boy's card */}
            <div className="card">
              <h3 className="title-gold" style={{ borderBottom: '1px solid var(--border)', paddingBottom: '10px' }}>
                🙋‍♂️ {t('boyDetails', settings.language)}
              </h3>
              <div style={{ marginTop: '15px' }}>
                <label>{t('name', settings.language)}</label>
                <input type="text" value={boyName} onChange={(e) => setBoyName(e.target.value)} required />
              </div>
              <div className="grid-2">
                <div>
                  <label>{t('birthDate', settings.language)}</label>
                  <input
                    type="text"
                    value={boyDate}
                    onChange={(e) => handleDateChange(e.target.value, setBoyDate)}
                    placeholder="DD/MM/YYYY"
                    maxLength="10"
                    required
                  />
                </div>
                <div>
                  <label>{t('birthTime', settings.language)}</label>
                  <input type="time" value={boyTime} onChange={(e) => setBoyTime(e.target.value)} required />
                </div>
              </div>
              <div>
                <label>{t('birthLocation', settings.language)}</label>
                <LocationSearch value={boyLocation} onChange={setBoyLocation} />
              </div>
            </div>

            {/* Girl's card */}
            <div className="card">
              <h3 className="title-gold" style={{ borderBottom: '1px solid var(--border)', paddingBottom: '10px' }}>
                🙋‍♀️ {t('girlDetails', settings.language)}
              </h3>
              <div style={{ marginTop: '15px' }}>
                <label>{t('name', settings.language)}</label>
                <input type="text" value={girlName} onChange={(e) => setGirlName(e.target.value)} required />
              </div>
              <div className="grid-2">
                <div>
                  <label>{t('birthDate', settings.language)}</label>
                  <input
                    type="text"
                    value={girlDate}
                    onChange={(e) => handleDateChange(e.target.value, setGirlDate)}
                    placeholder="DD/MM/YYYY"
                    maxLength="10"
                    required
                  />
                </div>
                <div>
                  <label>{t('birthTime', settings.language)}</label>
                  <input type="time" value={girlTime} onChange={(e) => setGirlTime(e.target.value)} required />
                </div>
              </div>
              <div>
                <label>{t('birthLocation', settings.language)}</label>
                <LocationSearch value={girlLocation} onChange={setGirlLocation} />
              </div>
            </div>
          </div>

          {/* Settings block */}
          <div className="card grid-3">
            <div>
              <label>{t('methodology', settings.language)}</label>
              <select value={matchingSystem} onChange={(e) => setMatchingSystem(e.target.value)}>
                <option value="ASHTA_KOOTA">Ashta Koota (North Indian 36 Points)</option>
                <option value="DASA_PORUTHAM">Dasa Porutham (South Indian 10 matches)</option>
              </select>
            </div>
            <div>
              <label>{t('strictness', settings.language)}</label>
              <select value={strictness} onChange={(e) => setStrictness(e.target.value)}>
                <option value="LENIENT">Lenient</option>
                <option value="MODERATE">Moderate</option>
                <option value="STRICT">Strict</option>
              </select>
            </div>
            <div>
              <label>{t('ayanamsa', settings.language)}</label>
              <select value={ayanamsa} onChange={(e) => setAyanamsa(e.target.value)}>
                <option value="LAHIRI">Lahiri (Chitra Paksha)</option>
                <option value="KP">KP (Krishnamurti Padhdhati)</option>
                <option value="RAMAN">B.V. Raman</option>
                <option value="SURYA_SIDDHANTA">Surya Siddhanta</option>
                <option value="PUSHYAPAKSHA">Pushyapaksha</option>
              </select>
            </div>
          </div>

          <button type="submit" className="btn-primary" style={{ width: '100%', padding: '15px', fontSize: '16px' }}>
            {t('calculateMatch', settings.language)}
          </button>
        </form>
      )}

      {loading && (
        <div className="spinner-container">
          <div className="spinner"></div>
          <p>Analyzing compatibility parameters and dosha configurations...</p>
        </div>
      )}

      {error && (
        <div className="card" style={{ borderLeft: '4px solid var(--danger)' }}>
          <p style={{ color: 'var(--danger)', fontWeight: 'bold' }}>Matching Engine Error</p>
          <p>{error}</p>
          <button onClick={() => setResult(null)} className="btn-primary" style={{ marginTop: '10px' }}>Try Again</button>
        </div>
      )}

      {!loading && result && (
        <div>
          {/* Result Header Gauge */}
          <div className="card matching-header">
            <div className="score-circle">
              <span className="number">
                {result.totalScore}
              </span>
              <span className="label">
                {t('outOf', settings.language)} {result.maxScore}
              </span>
            </div>
            <div className={`verdict-badge ${getVerdictClass(result.verdict)}`}>
              {result.verdict} ({result.percentage.toFixed(0)}%)
            </div>
            
            <div style={{ marginTop: '10px', fontSize: '13px', color: 'var(--text-secondary)', textAlign: 'center' }}>
              🙋‍♂️ {result.boyProfile?.name} ({result.boyProfile?.birthProfile?.nakshatra}) &nbsp;|&nbsp; 🙋‍♀️ {result.girlProfile?.name} ({result.girlProfile?.birthProfile?.nakshatra}) &nbsp;|&nbsp; {t('ayanamsa', settings.language)}: {result.boyProfile?.ayanamsa || ayanamsa}
            </div>
            
            <div style={{ marginTop: '20px', display: 'flex', gap: '15px' }}>
              <button onClick={handleDownloadPdf} className="btn-primary">
                📥 {t('downloadPdf', settings.language)}
              </button>
              <button onClick={() => setResult(null)} className="btn-primary" style={{ background: 'none', border: '1px solid var(--border)', color: 'var(--text-primary)' }}>
                {t('newMatch', settings.language)}
              </button>
            </div>
          </div>

          {/* Warnings and alerts */}
          {result.warnings && result.warnings.length > 0 && (
            <div className="card" style={{ borderLeft: '4px solid var(--warning)', backgroundColor: 'rgba(255, 152, 0, 0.05)' }}>
              <h4 style={{ margin: '0 0 10px', color: 'var(--accent-gold)' }}>⚠️ {t('warningsTitle', settings.language)}</h4>
              <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '14px', color: 'var(--text-primary)' }}>
                {result.warnings.map((w, idx) => (
                  <li key={idx} style={{ marginBottom: '5px' }}>{w}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Koota/Porutham detail table */}
          <div className="card">
            <h3 className="title-gold">{t('breakdownTitle', settings.language)}</h3>
            <div className="horai-table-container">
              <table className="horai-table">
                <thead>
                  <tr>
                    <th>{t('matching', settings.language)}</th>
                    <th>{t('scored', settings.language)}</th>
                    <th>{t('max', settings.language)}</th>
                    <th>{t('status', settings.language)}</th>
                    <th>{t('notes', settings.language)}</th>
                  </tr>
                </thead>
                <tbody>
                  {result.kootas?.map((k, idx) => (
                    <tr key={idx}>
                      <td style={{ fontWeight: 'bold' }}>{k.name}</td>
                      <td>{k.scoredPoints}</td>
                      <td>{k.maxPoints}</td>
                      <td style={{
                        fontWeight: 'bold',
                        color: k.status === 'NOT_MATCHED' ? 'var(--danger)' : k.status === 'MATCHED' ? 'var(--success)' : 'var(--accent-gold)'
                      }}>
                        {getStatusText(k.status)}
                      </td>
                      <td style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                        {k.description}
                        {k.nullificationReason && (
                          <div style={{ color: 'var(--success)', marginTop: '4px', fontStyle: 'italic' }}>
                            Exception: {k.nullificationReason}
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default MatchingPage;
