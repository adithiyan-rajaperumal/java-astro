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

    // Early morning hours (00:00 AM to 08:30 AM) belong to next calendar day morning relative to panchangam start
    if (endMins >= 0 && endMins <= 8 * 60 + 30) {
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
    const thenStr = t('then', settings.language);

    let text = `${firstName} ${elem.endTime} ${untilStr}`;

    const nextName = elem.nextLocalizedName || elem.nextName;
    if (nextName) {
      if (elem.nextEndTime) {
        text += `, ${thenStr} ${nextName} ${elem.nextEndTime} ${untilStr}`;
      } else {
        text += `, ${thenStr} ${nextName}`;
      }
    }

    return text;
  };

  return (
    <div>
      {/* Simple Centered Date Chooser with Aligned Today Button */}
      <div className="panchangam-top-bar">
        <button 
          onClick={() => setCurrentDate(getTodayDateString(settings.location))} 
          className="today-btn"
        >
          {t('today', settings.language)}
        </button>
        <input
          type="date"
          className="date-picker-input"
          value={currentDate}
          onChange={(e) => e.target.value && setCurrentDate(e.target.value)}
        />
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
        <div className="panchangam-grid-2col">
          {/* LEFT COLUMN: Core Astronomical & Panchangam Elements */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            
            {/* Card 1 (TOP): Sunrise, Sunset & Muhurtham / Vasthu Summary */}
            <div className="card" style={{ margin: 0 }}>
              <h3 className="title-gold" style={{ marginTop: 0 }}>🌅 {t('sunrise', settings.language)} & 🌇 {t('sunset', settings.language)}</h3>
              
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(110px, 1fr))', gap: '10px', marginBottom: '12px' }}>
                <div className="element-detail-item" style={{ margin: 0, textAlign: 'center' }}>
                  <div className="element-label">🌅 {t('sunrise', settings.language)}</div>
                  <strong style={{ fontSize: '14px' }}>{data.sunrise}</strong>
                </div>
                <div className="element-detail-item" style={{ margin: 0, textAlign: 'center' }}>
                  <div className="element-label">🌇 {t('sunset', settings.language)}</div>
                  <strong style={{ fontSize: '14px' }}>{data.sunset}</strong>
                </div>
                <div className="element-detail-item" style={{ margin: 0, textAlign: 'center' }}>
                  <div className="element-label">🌙 {t('moonrise', settings.language)}</div>
                  <strong style={{ fontSize: '14px' }}>{data.moonrise}</strong>
                </div>
                <div className="element-detail-item" style={{ margin: 0, textAlign: 'center' }}>
                  <div className="element-label">🌕 {t('moonset', settings.language)}</div>
                  <strong style={{ fontSize: '14px' }}>{data.moonset}</strong>
                </div>
              </div>

              <div className="element-detail-item" style={{ margin: 0, display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '10px' }}>
                  <div style={{ color: data.muhurthamDay ? 'var(--success)' : 'var(--danger)', fontWeight: 'bold', fontSize: '13.5px' }}>
                    {data.muhurthamDay ? '✅ ' + t('auspiciousDay', settings.language) : '❌ ' + t('inauspiciousDay', settings.language)}
                  </div>

                  {data.vasthuDay && (
                    <div style={{ background: '#e8f5e9', border: '1px solid #a5d6a7', color: '#2e7d32', padding: '4px 10px', borderRadius: '12px', fontWeight: 'bold', fontSize: '12.5px' }}>
                      🏡 {t('vasthuDay', settings.language)}
                    </div>
                  )}
                </div>

                <div style={{ fontSize: '12.5px', color: 'var(--text-secondary)', borderTop: '1px dashed var(--border)', paddingTop: '6px' }}>
                  {t('netram', settings.language)}: <strong>{data.netram}</strong> | {t('jeevan', settings.language)}: <strong>{data.jeevan}</strong>
                </div>
              </div>
            </div>

            {/* Card 2: Rashi, Nakshatram & Chandrastamam (Single Paired Block) */}
            <div className="card" style={{ margin: 0 }}>
              <h3 className="title-gold" style={{ marginTop: 0 }}>🌙 {t('rashi', settings.language)}, {t('nakshatra', settings.language)} & {t('chandrastamam', settings.language)}</h3>
              
              <div className="element-detail-item" style={{ background: '#fffaf4', borderLeft: '4px solid var(--accent-saffron)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '10px' }}>
                  <div>
                    <span className="element-label" style={{ display: 'inline-block', marginRight: '6px' }}>{t('rashi', settings.language)}:</span>
                    <strong style={{ fontSize: '15px', color: 'var(--accent-saffron)' }}>{data.rashi}</strong>
                  </div>
                  <div>
                    <span className="element-label" style={{ display: 'inline-block', marginRight: '6px', color: 'var(--accent-warm)' }}>{t('chandrastamam', settings.language)}:</span>
                    <strong style={{ fontSize: '14px', color: 'var(--danger)' }}>
                      {Array.isArray(data.chandrastamamNakshatras) ? data.chandrastamamNakshatras.join(', ') : (data.chandrastamamRashi || '')}
                    </strong>
                  </div>
                </div>
              </div>

              <div className="element-detail-item" style={{ marginBottom: 0 }}>
                <div className="element-label">{t('nakshatra', settings.language)}</div>
                <div className="element-content">{formatElementTiming(data.nakshatra)}</div>
              </div>
            </div>

            {/* Card 3: Other Core Panchangam Elements (Thithi, Yoga, Karana) */}
            <div className="card" style={{ margin: 0 }}>
              <h3 className="title-gold" style={{ marginTop: 0 }}>📜 {t('panchangamElements', settings.language)}</h3>
              
              <div className="element-detail-item">
                <div className="element-label">{t('thithi', settings.language)}</div>
                <div className="element-content">{formatElementTiming(data.thithi)}</div>
              </div>

              <div className="element-detail-item">
                <div className="element-label">{t('yogam', settings.language)}</div>
                <div className="element-content">{formatElementTiming(data.yogam)}</div>
              </div>

              <div className="element-detail-item" style={{ marginBottom: 0 }}>
                <div className="element-label">{t('karanam', settings.language)}</div>
                <div className="element-content">{formatElementTiming(data.karanam)}</div>
              </div>
            </div>

          </div>

          {/* RIGHT COLUMN: Time Slots & 24 Horai Table */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            
            {/* Card 4: Auspicious & Inauspicious Timings */}
            <div className="card" style={{ margin: 0 }}>
              <h3 className="title-gold" style={{ marginTop: 0 }}>⏱️ {t('auspicious', settings.language)} & {t('inauspicious', settings.language)}</h3>
              {data.abhijitMuhurtham && (
                <div style={{ marginBottom: '12px', padding: '10px 12px', backgroundColor: 'rgba(255, 215, 0, 0.08)', borderRadius: '8px', borderLeft: '4px solid var(--accent-gold)' }}>
                  <div style={{ fontWeight: 'bold', color: 'var(--accent-gold)', marginBottom: '3px' }}>☀️ {t('abhijitMuhurtham', settings.language)}</div>
                  <div style={{ fontSize: '14px', color: 'var(--text-primary)' }}>{data.abhijitMuhurtham.start} - {data.abhijitMuhurtham.end}</div>
                </div>
              )}
              {renderTimeSlotList(data.nallaNeram, 'nallaNeram', true)}
              {renderTimeSlotList(data.gowriNallaNeram, 'gowriNallaNeram', true)}
              <div style={{ margin: '12px 0 10px', borderTop: '1px dashed var(--border)' }}></div>
              {renderTimeSlotList(data.raghuKalam, 'rahuKalam', false)}
              {renderTimeSlotList(data.emagandam, 'yamagandam', false)}
              {renderTimeSlotList(data.kulikai, 'gulikaKalam', false)}
            </div>

            {/* Card 5: 24 Horai Table */}
            <div className="card" style={{ margin: 0 }}>
              <h3 className="title-gold" style={{ marginTop: 0 }}>⏳ {t('horas', settings.language)}</h3>
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
