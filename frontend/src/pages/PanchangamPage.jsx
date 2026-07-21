import { useState, useEffect } from 'react';
import { t } from '../i18n/translations';

const getTodayDateString = (loc) => {
  const now = new Date();
  if (loc?.timezone) {
    try {
      const formatter = new Intl.DateTimeFormat('en-CA', {
        timeZone: loc.timezone,
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
      return formatter.format(now);
    } catch {
      // Fallback to local
    }
  }
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

function PanchangamPage({ settings }) {
  const [currentDate, setCurrentDate] = useState(() => getTodayDateString(settings.location));
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchPanchangam = async (dateStr) => {
    if (!settings.location) return;
    setLoading(true);
    setError(null);
    try {
      const response = await fetch('/api/v1/astrology/panchangam', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Accept-Language': settings.language
        },
        body: JSON.stringify({
          date: dateStr,
          latitude: settings.location.latitude,
          longitude: settings.location.longitude,
          language: settings.language,
          ayanamsa: settings.ayanamsa
        })
      });
      if (response.ok) {
        const panchangamData = await response.json();
        setData(panchangamData);
      } else {
        throw new Error('Failed to load Panchangam details.');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPanchangam(currentDate);
  }, [currentDate, settings.location, settings.language, settings.ayanamsa]);

  const changeDate = (days) => {
    const d = new Date(currentDate + 'T12:00:00');
    d.setDate(d.getDate() + days);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    setCurrentDate(`${year}-${month}-${day}`);
  };

  const renderTimeSlotList = (slots = [], titleKey, isAuspicious) => {
    if (!slots || slots.length === 0) return null;
    return (
      <div className="time-slot-container">
        <h4 style={{ margin: '10px 0 5px', fontSize: '14px', color: 'var(--text-secondary)' }}>{t(titleKey, settings.language)}</h4>
        {slots.map((s, idx) => (
          <div key={idx} className={`time-slot-bar ${isAuspicious ? 'auspicious' : 'inauspicious'}`}>
            <span>{s.label}</span>
            <span>{s.start} - {s.end}</span>
          </div>
        ))}
      </div>
    );
  };

  const parseTimeToMinutes = (timeStr) => {
    if (!timeStr) return -1;
    const match = timeStr.match(/(\d{1,2}):(\d{2})\s*(AM|PM)/i);
    if (!match) return -1;
    let hours = parseInt(match[1], 10);
    const minutes = parseInt(match[2], 10);
    const isPM = match[3].toUpperCase() === 'PM';
    if (isPM && hours < 12) hours += 12;
    if (!isPM && hours === 12) hours = 0;
    return hours * 60 + minutes;
  };

  const isNextDayTime = (endStr, refStartStr = null) => {
    if (!endStr) return false;
    const nextDayKeywords = ['next day', 'அடுத்த நாள்', 'अगले दिन', 'ಮುಂದಿನ ದಿನ', 'తరువాత రోజు', 'അടുത്ത ദിവസം'];
    if (nextDayKeywords.some(k => endStr.includes(k))) return true;

    const endMins = parseTimeToMinutes(endStr);
    if (endMins < 0) return false;

    if (refStartStr) {
      const refMins = parseTimeToMinutes(refStartStr);
      if (refMins >= 0 && endMins < refMins && refMins >= 12 * 60) {
        return true;
      }
    }

    // Early morning hours (00:00 AM to 06:30 AM) belong to next calendar day morning
    if (endMins >= 0 && endMins <= 6 * 60 + 30) {
      return true;
    }

    return false;
  };

  const formatTimeString = (timeStr, refStartStr, nextDayText) => {
    if (!timeStr) return '';
    const ignoreKeywords = ['next day', 'அடுத்த நாள்', 'अगले दिन', 'ಮುಂದಿನ ದಿನ', 'తరువాత రోజు', 'അടുത്ത ദിവസം', 'throughout', 'நாள் முழுவதும்', 'दिन भर', 'ಇಡೀ ದಿನ', 'త్రోలట్', 'മുഴുവൻ'];
    if (ignoreKeywords.some(k => timeStr.includes(k))) return timeStr;

    if (isNextDayTime(timeStr, refStartStr)) {
      return `${timeStr} (${nextDayText})`;
    }
    return timeStr;
  };

  const formatElementTiming = (elem) => {
    if (!elem) return '';
    const firstName = elem.localizedName || elem.name;
    if (!elem.endTime) return firstName;

    const untilStr = t('until', settings.language);
    const fromStr = t('from', settings.language);
    const thenStr = t('then', settings.language);
    const nextDayStr = t('nextDay', settings.language);

    const formattedEndTime = formatTimeString(elem.endTime, null, nextDayStr);
    let text = `${firstName} ${formattedEndTime} ${untilStr}`;

    const nextName = elem.nextLocalizedName || elem.nextName;
    if (nextName) {
      let nextEnd = elem.nextEndTime;
      const ignoreKeywords = ['next day', 'அடுத்த நாள்', 'अगले दिन', 'ಮುಂದಿನ ದಿನ', 'తరువాత రోజు', 'അടുത്ത ദിവസം', 'throughout', 'நாள் முழுவதும்', 'दिन भर', 'ಇಡೀ ದಿನ', 'త్రోలట్', 'മുഴുവൻ'];
      if (nextEnd && !ignoreKeywords.some(k => nextEnd.includes(k))) {
        nextEnd = `${nextEnd} (${nextDayStr})`;
      }

      if (nextEnd) {
        text += `, ${thenStr} ${nextName} ${elem.endTime} ${fromStr} - ${nextEnd} ${untilStr}`;
      } else {
        text += `, ${thenStr} ${nextName} ${elem.endTime} ${fromStr}`;
      }
    }

    return text;
  };

  return (
    <div>
      {/* Top Header Control Bar */}
      <div className="panchangam-header-bar">
        <div className="panchangam-header-title">
          <h2>🕉️ {t('panchangam', settings.language)}</h2>
          {settings.location && (
            <span className="sub-pill">
              📍 {settings.location.label.split(',')[0]}
            </span>
          )}
        </div>
        <div className="panchangam-date-controls">
          <button onClick={() => changeDate(-1)} className="btn-primary" style={{ padding: '6px 14px' }}>
            ← {t('prev', settings.language)}
          </button>
          <button 
            onClick={() => setCurrentDate(getTodayDateString(settings.location))} 
            className="btn-primary" 
            style={{ padding: '6px 14px', background: 'none', border: '1px solid var(--border)', color: 'var(--text-primary)' }}
          >
            {t('today', settings.language)}
          </button>
          <input
            type="date"
            className="date-pill-input"
            value={currentDate}
            onChange={(e) => e.target.value && setCurrentDate(e.target.value)}
          />
          <button onClick={() => changeDate(1)} className="btn-primary" style={{ padding: '6px 14px' }}>
            {t('next', settings.language)} →
          </button>
        </div>
      </div>

      {loading && (
        <div className="spinner-container">
          <div className="spinner"></div>
          <p>{t('calculating', settings.language)}</p>
        </div>
      )}

      {error && (
        <div className="card" style={{ borderLeft: '4px solid var(--danger)' }}>
          <p style={{ color: 'var(--danger)', fontWeight: 'bold' }}>{t('errorLoadingPanchangam', settings.language)}</p>
          <p>{error}</p>
          <button onClick={() => fetchPanchangam(currentDate)} className="btn-primary" style={{ marginTop: '10px' }}>{t('retry', settings.language)}</button>
        </div>
      )}

      {!loading && !error && data && (
        <div>
          {/* Top Hero Summary Cards */}
          <div className="panchangam-hero-grid">
            <div className="hero-metric-card">
              <div className="hero-metric-header">🌅 {t('sunrise', settings.language)} & 🌇 {t('sunset', settings.language)}</div>
              <div className="hero-metric-value" style={{ fontSize: '13.5px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '6px' }}>
                <div>🌅 {data.sunrise}</div>
                <div>🌇 {data.sunset}</div>
                <div>🌙 {data.moonrise}</div>
                <div>🌕 {data.moonset}</div>
              </div>
            </div>

            <div className="hero-metric-card">
              <div className="hero-metric-header">✨ {t('thithi', settings.language)} & {t('nakshatra', settings.language)}</div>
              <div className="hero-metric-value">
                <div style={{ color: 'var(--accent-saffron)' }}>{data.thithi?.localizedName || data.thithi?.name}</div>
                <div style={{ fontSize: '13px', color: 'var(--text-secondary)', fontWeight: 'normal', marginTop: '4px' }}>
                  {data.nakshatra?.localizedName || data.nakshatra?.name}
                </div>
              </div>
            </div>

            <div className="hero-metric-card">
              <div className="hero-metric-header">⚖️ {t('yogam', settings.language)} & {t('karanam', settings.language)}</div>
              <div className="hero-metric-value">
                <div style={{ color: 'var(--accent-warm)' }}>{data.yogam?.localizedName || data.yogam?.name}</div>
                <div style={{ fontSize: '13px', color: 'var(--text-secondary)', fontWeight: 'normal', marginTop: '4px' }}>
                  {data.karanam?.localizedName || data.karanam?.name}
                </div>
              </div>
            </div>

            <div className="hero-metric-card">
              <div className="hero-metric-header">🏡 {t('muhurtham', settings.language)}</div>
              <div>
                <div style={{ color: data.muhurthamDay ? 'var(--success)' : 'var(--danger)', fontWeight: 'bold', fontSize: '14px' }}>
                  {data.muhurthamDay ? '✅ ' + t('auspiciousDay', settings.language) : '❌ ' + t('inauspiciousDay', settings.language)}
                </div>
                <div className="hero-sub-pills">
                  <span className="sub-pill">{t('netram', settings.language)}: {data.netram}</span>
                  <span className="sub-pill">{t('jeevan', settings.language)}: {data.jeevan}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Main Responsive 3-Column Dashboard */}
          <div className="panchangam-dashboard-grid">
            {/* Column 1: Detailed Panchangam Elements */}
            <div className="card" style={{ height: 'fit-content' }}>
              <h3 className="title-gold" style={{ marginTop: 0 }}>{t('panchangamElements', settings.language)}</h3>

              <div className="element-detail-item">
                <div className="element-label">{t('thithi', settings.language)}</div>
                <div className="element-content">{formatElementTiming(data.thithi)}</div>
              </div>

              <div className="element-detail-item">
                <div className="element-label">{t('nakshatra', settings.language)}</div>
                <div className="element-content">{formatElementTiming(data.nakshatra)}</div>
              </div>

              <div className="element-detail-item">
                <div className="element-label">{t('yogam', settings.language)}</div>
                <div className="element-content">{formatElementTiming(data.yogam)}</div>
              </div>

              <div className="element-detail-item">
                <div className="element-label">{t('karanam', settings.language)}</div>
                <div className="element-content">{formatElementTiming(data.karanam)}</div>
              </div>

              <div className="element-detail-item" style={{ background: '#fffaf4' }}>
                <div className="element-label">{t('rashi', settings.language)} & {t('chandrastamam', settings.language)}</div>
                <div className="element-content">
                  <strong>{t('rashi', settings.language)}:</strong> {data.rashi} <br/>
                  <strong style={{ color: 'var(--accent-warm)' }}>{t('chandrastamam', settings.language)}:</strong> {Array.isArray(data.chandrastamamNakshatras) ? data.chandrastamamNakshatras.join(', ') : (data.chandrastamamRashi || '')}
                </div>
              </div>
            </div>

            {/* Column 2: Time Slots (Auspicious & Inauspicious) */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div className="card" style={{ margin: 0 }}>
                <h3 className="title-gold" style={{ marginTop: 0 }}>{t('auspicious', settings.language)}</h3>
                {renderTimeSlotList(data.nallaNeram, 'nallaNeram', true)}
                {renderTimeSlotList(data.gowriNallaNeram, 'gowriNallaNeram', true)}
              </div>

              <div className="card" style={{ margin: 0 }}>
                <h3 className="title-gold" style={{ marginTop: 0 }}>{t('inauspicious', settings.language)}</h3>
                {renderTimeSlotList(data.raghuKalam, 'rahuKalam', false)}
                {renderTimeSlotList(data.emagandam, 'yamagandam', false)}
                {renderTimeSlotList(data.kulikai, 'gulikaKalam', false)}
              </div>
            </div>

            {/* Column 3: 24 Horai Table */}
            <div className="card panchangam-grid-full" style={{ height: 'fit-content', margin: 0 }}>
              <h3 className="title-gold" style={{ marginTop: 0 }}>{t('horas', settings.language)}</h3>
              <div className="horai-table-container">
                <table className="horai-table">
                  <thead>
                    <tr>
                      <th>{t('hr', settings.language)}</th>
                      <th>{t('interval', settings.language)}</th>
                      <th>{t('planet', settings.language)}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.horais?.map((h, idx) => (
                      <tr key={idx}>
                        <td>{h.hour}</td>
                        <td>{h.start} - {h.end}</td>
                        <td>
                          <span style={{
                            display: 'inline-block',
                            width: '9px',
                            height: '9px',
                            borderRadius: '50%',
                            backgroundColor: h.planet === 'Sun' ? '#ff9800' : h.planet === 'Moon' ? '#2196f3' : h.planet === 'Mars' ? '#f44336' : h.planet === 'Mercury' ? '#4caf50' : h.planet === 'Jupiter' ? '#ffc107' : h.planet === 'Venus' ? '#e91e63' : '#9c27b0',
                            marginRight: '8px'
                          }}></span>
                          {h.localizedPlanet || h.planet}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default PanchangamPage;
