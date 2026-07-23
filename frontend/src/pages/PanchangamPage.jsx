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
  const [showGowriModal, setShowGowriModal] = useState(false);

  const getGowriGuideText = (subKey, lang) => {
    const guideObj = t('gowriGuide', lang);
    if (typeof guideObj === 'object' && guideObj[subKey]) {
      return guideObj[subKey];
    }
    return t(`gowriGuide.${subKey}`, lang);
  };

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

  const renderSlotLabelContent = (label) => {
    if (!label) return null;
    const match = label.match(/^(.*?)\s*(\(.*?\))$/);
    if (match) {
      return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', flex: '1 1 auto', minWidth: '140px', paddingRight: '8px' }}>
          <span style={{ fontWeight: 'bold', fontSize: '13.5px' }}>{match[1]}</span>
          <span style={{ fontSize: '11.5px', opacity: 0.85, fontWeight: 'normal', lineHeight: '1.3' }}>{match[2]}</span>
        </div>
      );
    }
    return <span style={{ fontWeight: 'bold', flex: '1 1 auto' }}>{label}</span>;
  };

  const renderTimeSlotList = (slots, titleKey, isAuspicious) => {
    if (!slots || slots.length === 0) return null;
    const nextDayText = t('nextDay', settings.language);
    return (
      <div style={{ marginBottom: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', margin: '10px 0 6px' }}>
          <h4 style={{ margin: 0, fontSize: '14px', color: 'var(--text-secondary)' }}>{t(titleKey, settings.language)}</h4>
          {titleKey === 'gowriNallaNeram' && (
            <button
              onClick={() => setShowGowriModal(true)}
              style={{
                background: 'rgba(255, 215, 0, 0.12)',
                border: '1px solid var(--accent-gold)',
                color: 'var(--accent-gold)',
                borderRadius: '50%',
                width: '22px',
                height: '22px',
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '12px',
                fontWeight: 'bold',
                cursor: 'pointer'
              }}
              title="Gowri Panchangam Guide"
            >
              ℹ️
            </button>
          )}
        </div>
        {slots.map((s, idx) => {
          const formattedStart = formatTimeString(s.start, null, nextDayText);
          const formattedEnd = formatTimeString(s.end, s.start, nextDayText);
          return (
            <div key={idx} className={`time-slot-bar ${isAuspicious ? 'auspicious' : 'inauspicious'}`}>
              {renderSlotLabelContent(s.label)}
              <span style={{ whiteSpace: 'nowrap', fontWeight: 'bold', marginLeft: 'auto', alignSelf: 'flex-start' }}>
                {formattedStart} - {formattedEnd}
              </span>
            </div>
          );
        })}
      </div>
    );
  };

  const renderNakshatraYogamsList = (slots) => {
    if (!slots || slots.length === 0) return null;
    const nextDayText = t('nextDay', settings.language);
    return (
      <div style={{ marginBottom: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', margin: '10px 0 6px' }}>
          <h4 style={{ margin: 0, fontSize: '14px', color: 'var(--text-secondary)' }}>🌟 {t('nakshatraYogam', settings.language)}</h4>
        </div>
        {slots.map((s, idx) => {
          const labelLower = (s.label || '').toLowerCase();
          const isGood = labelLower.includes('amrita') || labelLower.includes('siddha') 
            || s.label.includes('அமிர்த') || s.label.includes('சித்த')
            || s.label.includes('अमृत') || s.label.includes('सिद्ध')
            || s.label.includes('ಅಮೃತ') || s.label.includes('ಸಿದ್ಧ')
            || s.label.includes('అమృత') || s.label.includes('సిద్ధ')
            || s.label.includes('അമൃത') || s.label.includes('സിദ്ധ');

          const formattedStart = formatTimeString(s.start, null, nextDayText);
          const formattedEnd = formatTimeString(s.end, s.start, nextDayText);

          return (
            <div key={idx} className={`time-slot-bar ${isGood ? 'auspicious' : 'inauspicious'}`}>
              <span style={{ fontWeight: 'bold' }}>{s.label}</span>
              <span style={{ whiteSpace: 'nowrap', fontWeight: 'bold', marginLeft: 'auto', alignSelf: 'flex-start' }}>
                {formattedStart} - {formattedEnd}
              </span>
            </div>
          );
        })}
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
      if (refMins >= 0 && endMins < refMins) {
        return true;
      }
    }

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

            {/* Card 2: Core Panchangam Limbs, Zodiac & Nakshatra */}
            <div className="card" style={{ margin: 0 }}>
              <h3 className="title-gold" style={{ marginTop: 0 }}>📜 {t('panchangamElements', settings.language)} & 🌙 {t('rashi', settings.language)}</h3>
              
              {/* Rashi & Chandrastamam Row */}
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

              {/* Thithi */}
              <div className="element-detail-item">
                <div className="element-label">{t('thithi', settings.language)}</div>
                <div className="element-content">{formatElementTiming(data.thithi)}</div>
              </div>

              {/* Nakshatra */}
              <div className="element-detail-item">
                <div className="element-label">{t('nakshatra', settings.language)}</div>
                <div className="element-content">{formatElementTiming(data.nakshatra)}</div>
              </div>

              {/* Yogam */}
              <div className="element-detail-item">
                <div className="element-label">{t('yogam', settings.language)}</div>
                <div className="element-content">{formatElementTiming(data.yogam)}</div>
              </div>

              {/* Karanam */}
              <div className="element-detail-item" style={{ marginBottom: 0 }}>
                <div className="element-label">{t('karanam', settings.language)}</div>
                <div className="element-content">{formatElementTiming(data.karanam)}</div>
              </div>
            </div>

          </div>

          {/* RIGHT COLUMN: Auspicious & Inauspicious Timings & 24 Horai Table */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            
            {/* Card 3: Auspicious Timings Card */}
            <div className="card" style={{ margin: 0, borderLeft: '4px solid var(--accent-gold)' }}>
              <h3 className="title-gold" style={{ marginTop: 0 }}>🌟 {t('auspicious', settings.language)}</h3>
              {data.abhijitMuhurtham && (
                <div style={{ marginBottom: '12px', padding: '10px 12px', backgroundColor: 'rgba(255, 215, 0, 0.08)', borderRadius: '8px', borderLeft: '4px solid var(--accent-gold)' }}>
                  <div style={{ fontWeight: 'bold', color: 'var(--accent-gold)', marginBottom: '3px' }}>☀️ {t('abhijitMuhurtham', settings.language)}</div>
                  <div style={{ fontSize: '14px', color: 'var(--text-primary)' }}>{data.abhijitMuhurtham.start} - {data.abhijitMuhurtham.end}</div>
                </div>
              )}
              {renderTimeSlotList(data.nallaNeram, 'nallaNeram', true)}
              {renderTimeSlotList(data.gowriNallaNeram, 'gowriNallaNeram', true)}
              {renderNakshatraYogamsList(data.nakshatraYogams)}
            </div>

            {/* Card 4: Inauspicious Kalam Divisions Card */}
            <div className="card" style={{ margin: 0, borderLeft: '4px solid var(--danger)' }}>
              <h3 style={{ marginTop: 0, color: 'var(--danger)' }}>⚠️ {t('inauspicious', settings.language)}</h3>
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

      {showGowriModal && (
        <div 
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.7)',
            backdropFilter: 'blur(4px)',
            zIndex: 9999,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '16px'
          }}
          onClick={() => setShowGowriModal(false)}
        >
          <div 
            style={{
              backgroundColor: 'var(--bg-card, #1e1e24)',
              color: 'var(--text-primary, #ffffff)',
              borderRadius: '14px',
              padding: '22px',
              maxWidth: '500px',
              width: '100%',
              maxHeight: '85vh',
              overflowY: 'auto',
              border: '1px solid var(--accent-gold, #ffd700)',
              boxShadow: '0 10px 30px rgba(0,0,0,0.5)',
              position: 'relative'
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <button 
              onClick={() => setShowGowriModal(false)}
              style={{
                position: 'absolute',
                top: '14px',
                right: '16px',
                background: 'none',
                border: 'none',
                fontSize: '20px',
                color: 'var(--text-secondary)',
                cursor: 'pointer'
              }}
            >
              ✕
            </button>
            <h3 style={{ margin: '0 0 14px', color: 'var(--accent-gold)', fontSize: '17px', borderBottom: '1px dashed var(--border)', paddingBottom: '10px' }}>
              ℹ️ {getGowriGuideText('title', settings.language)}
            </h3>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '13px', lineHeight: '1.4' }}>
              <div style={{ padding: '10px 14px', borderRadius: '8px', background: 'rgba(76, 175, 80, 0.12)', borderLeft: '4px solid #4caf50' }}>
                <div style={{ fontWeight: 'bold', color: '#4caf50', fontSize: '14px' }}>🥇 {getGowriGuideText('rank1Title', settings.language)}</div>
                <div style={{ color: 'var(--text-primary)', marginTop: '3px', fontSize: '13px', lineHeight: '1.45' }}>{getGowriGuideText('rank1Desc', settings.language)}</div>
              </div>

              <div style={{ padding: '10px 14px', borderRadius: '8px', background: 'rgba(76, 175, 80, 0.12)', borderLeft: '4px solid #4caf50' }}>
                <div style={{ fontWeight: 'bold', color: '#4caf50', fontSize: '14px' }}>🥈 {getGowriGuideText('rank2Title', settings.language)}</div>
                <div style={{ color: 'var(--text-primary)', marginTop: '3px', fontSize: '13px', lineHeight: '1.45' }}>{getGowriGuideText('rank2Desc', settings.language)}</div>
              </div>

              <div style={{ padding: '10px 14px', borderRadius: '8px', background: 'rgba(76, 175, 80, 0.12)', borderLeft: '4px solid #4caf50' }}>
                <div style={{ fontWeight: 'bold', color: '#4caf50', fontSize: '14px' }}>🥉 {getGowriGuideText('rank3Title', settings.language)}</div>
                <div style={{ color: 'var(--text-primary)', marginTop: '3px', fontSize: '13px', lineHeight: '1.45' }}>{getGowriGuideText('rank3Desc', settings.language)}</div>
              </div>

              <div style={{ padding: '10px 14px', borderRadius: '8px', background: 'rgba(76, 175, 80, 0.12)', borderLeft: '4px solid #4caf50' }}>
                <div style={{ fontWeight: 'bold', color: '#4caf50', fontSize: '14px' }}>🌸 {getGowriGuideText('rank4Title', settings.language)}</div>
                <div style={{ color: 'var(--text-primary)', marginTop: '3px', fontSize: '13px', lineHeight: '1.45' }}>{getGowriGuideText('rank4Desc', settings.language)}</div>
              </div>

              <div style={{ padding: '10px 14px', borderRadius: '8px', background: 'rgba(76, 175, 80, 0.12)', borderLeft: '4px solid #4caf50' }}>
                <div style={{ fontWeight: 'bold', color: '#4caf50', fontSize: '14px' }}>🌅 {getGowriGuideText('rank5Title', settings.language)}</div>
                <div style={{ color: 'var(--text-primary)', marginTop: '3px', fontSize: '13px', lineHeight: '1.45' }}>{getGowriGuideText('rank5Desc', settings.language)}</div>
              </div>

              <div style={{ marginTop: '4px', padding: '10px 14px', borderRadius: '8px', background: 'rgba(244, 67, 54, 0.12)', borderLeft: '4px solid #ef5350' }}>
                <div style={{ fontWeight: 'bold', color: '#ef5350', fontSize: '14px' }}>⚠️ {getGowriGuideText('inauspiciousTitle', settings.language)}</div>
                <div style={{ color: 'var(--text-primary)', marginTop: '3px', fontSize: '13px', lineHeight: '1.45' }}>{getGowriGuideText('inauspiciousDesc', settings.language)}</div>
              </div>
            </div>

            <button 
              onClick={() => setShowGowriModal(false)}
              style={{
                marginTop: '16px',
                width: '100%',
                padding: '10px',
                backgroundColor: 'var(--accent-gold)',
                color: '#000',
                border: 'none',
                borderRadius: '8px',
                fontWeight: 'bold',
                cursor: 'pointer'
              }}
            >
              {getGowriGuideText('close', settings.language)}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default PanchangamPage;
