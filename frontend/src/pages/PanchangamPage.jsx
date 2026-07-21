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

  const formatElementTiming = (elem) => {
    if (!elem) return '';
    const firstName = elem.localizedName || elem.name;
    if (!elem.endTime) return firstName;

    const untilStr = t('until', settings.language);
    const fromStr = t('from', settings.language);
    const thenStr = t('then', settings.language);

    let text = `${firstName} ${elem.endTime} ${untilStr}`;

    const nextName = elem.nextLocalizedName || elem.nextName;
    if (nextName) {
      if (elem.nextEndTime) {
        text += `, ${thenStr} ${nextName} ${elem.endTime} ${fromStr} - ${elem.nextEndTime} ${untilStr}`;
      } else {
        text += `, ${thenStr} ${nextName} ${elem.endTime} ${fromStr}`;
      }
    }

    return text;
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <button onClick={() => changeDate(-1)} className="btn-primary" style={{ padding: '8px 15px' }}>← {t('prev', settings.language)}</button>
        <span style={{ fontSize: '18px', fontWeight: 'bold', color: 'var(--accent-gold)' }}>
          📅 {currentDate}
        </span>
        <button onClick={() => changeDate(1)} className="btn-primary" style={{ padding: '8px 15px' }}>{t('next', settings.language)} →</button>
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
        <div className="grid-2">
          {/* Main Panchangam Elements */}
          <div>
            <div className="card">
              <h3 className="title-gold">{t('sunrise', settings.language)} & {t('sunset', settings.language)}</h3>
              <div className="grid-2" style={{ fontSize: '15px' }}>
                <div>🌅 <strong>{t('sunrise', settings.language)}:</strong> {data.sunrise}</div>
                <div>🌇 <strong>{t('sunset', settings.language)}:</strong> {data.sunset}</div>
                <div>🌙 <strong>{t('moonrise', settings.language)}:</strong> {data.moonrise}</div>
                <div>🌕 <strong>{t('moonset', settings.language)}:</strong> {data.moonset}</div>
              </div>
            </div>

            <div className="card">
              <h3 className="title-gold">{t('panchangamElements', settings.language)}</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <div>
                  <strong>{t('thithi', settings.language)}:</strong> {formatElementTiming(data.thithi)}
                </div>
                <div>
                  <strong>{t('nakshatra', settings.language)}:</strong> {formatElementTiming(data.nakshatra)}
                </div>
                <div>
                  <strong>{t('yogam', settings.language)}:</strong> {formatElementTiming(data.yogam)}
                </div>
                <div>
                  <strong>{t('karanam', settings.language)}:</strong> {formatElementTiming(data.karanam)}
                </div>
                <div style={{ borderTop: '1px solid var(--border)', paddingTop: '10px', marginTop: '5px' }}>
                  <strong>{t('rashi', settings.language)}:</strong> {data.rashi} | <strong>{t('chandrastamam', settings.language)}:</strong> {Array.isArray(data.chandrastamamNakshatras) ? data.chandrastamamNakshatras.join(', ') : (data.chandrastamamRashi || '')}
                </div>
              </div>
            </div>

            <div className="card">
              <h3 className="title-gold">{t('muhurtham', settings.language)}</h3>
              <div className="grid-2">
                <div style={{ color: data.muhurthamDay ? 'var(--success)' : 'var(--danger)', fontWeight: 'bold' }}>
                  {data.muhurthamDay ? '✅ ' + t('auspiciousDay', settings.language) : '❌ ' + t('inauspiciousDay', settings.language)}
                </div>
                <div style={{ color: data.vasthuDay ? 'var(--success)' : 'var(--text-secondary)' }}>
                  🏡 {data.vasthuDay ? t('vasthuDay', settings.language) : t('noVasthuDay', settings.language)}
                </div>
              </div>
              <div style={{ display: 'flex', gap: '15px', marginTop: '15px', fontSize: '13px', color: 'var(--text-secondary)' }}>
                <div>{t('netram', settings.language)}: {data.netram}</div>
                <div>{t('jeevan', settings.language)}: {data.jeevan}</div>
              </div>
            </div>
          </div>

          {/* Time Slots & Horai */}
          <div>
            <div className="card">
              <h3 className="title-gold">{t('auspicious', settings.language)}</h3>
              {renderTimeSlotList(data.nallaNeram, 'nallaNeram', true)}
              {renderTimeSlotList(data.gowriNallaNeram, 'gowriNallaNeram', true)}
            </div>

            <div className="card">
              <h3 className="title-gold">{t('inauspicious', settings.language)}</h3>
              {renderTimeSlotList(data.raghuKalam, 'rahuKalam', false)}
              {renderTimeSlotList(data.emagandam, 'yamagandam', false)}
              {renderTimeSlotList(data.kulikai, 'gulikaKalam', false)}
            </div>
          </div>

          {/* 24 Horai Table */}
          <div style={{ gridColumn: '1 / -1' }} className="card">
            <h3 className="title-gold">{t('horas', settings.language)}</h3>
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
                          width: '10px',
                          height: '10px',
                          borderRadius: '50%',
                          backgroundColor: h.planet === 'Sun' ? 'yellow' : h.planet === 'Moon' ? 'lightblue' : h.planet === 'Mars' ? 'red' : h.planet === 'Mercury' ? 'green' : h.planet === 'Jupiter' ? 'orange' : h.planet === 'Venus' ? 'pink' : 'purple',
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
      )}
    </div>
  );
}

export default PanchangamPage;
