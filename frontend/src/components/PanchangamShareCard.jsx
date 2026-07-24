import React from 'react';
import { t } from '../i18n/translations';

export function PanchangamShareCard({ data, currentDate, settings }) {
  if (!data) return null;

  const lang = settings?.language || 'ta';
  const untilStr = t('until', lang);
  const thenStr = t('then', lang);

  const formatElementTiming = (elem) => {
    if (!elem) return '';
    const firstName = elem.localizedName || elem.name;
    if (!elem.endTime) return firstName;
    let text = `${firstName} (${elem.endTime} ${untilStr})`;
    const nextName = elem.nextLocalizedName || elem.nextName;
    if (nextName) {
      if (elem.nextEndTime) {
        text += `, ${thenStr} ${nextName} (${elem.nextEndTime} ${untilStr})`;
      } else {
        text += `, ${thenStr} ${nextName}`;
      }
    }
    return text;
  };

  return (
    <div
      id="panchangam-share-card"
      style={{
        width: '1080px',
        padding: '32px',
        backgroundColor: '#fffdf7',
        color: '#1a1a1a',
        fontFamily: "'Segoe UI', Roboto, 'Mukta', 'Noto Sans Tamil', sans-serif",
        boxSizing: 'border-box',
        borderRadius: '16px',
        border: '6px double #b71c1c',
        boxShadow: '0 15px 40px rgba(0,0,0,0.3)',
        position: 'relative'
      }}
    >
      {/* Traditional Temple/Calendar Top Banner */}
      <div style={{
        backgroundColor: '#b71c1c',
        color: '#ffffff',
        padding: '24px 20px',
        borderRadius: '10px',
        textAlign: 'center',
        border: '2px solid #ffd700',
        marginBottom: '24px',
        boxShadow: '0 4px 15px rgba(183, 28, 28, 0.4)'
      }}>
        <div style={{ fontSize: '22px', color: '#ffecb3', fontWeight: 'bold', letterSpacing: '1px', marginBottom: '6px' }}>
          🕉️ {t('appTitle', lang)} • {t('panchangam', lang)}
        </div>
        <div style={{ fontSize: '42px', fontWeight: '900', color: '#ffffff', textTransform: 'uppercase', margin: '4px 0' }}>
          📅 {currentDate}
        </div>
        <div style={{ fontSize: '22px', color: '#ffe082', fontWeight: '600' }}>
          📍 {settings?.location?.name || 'Chennai'}
        </div>

        {/* Sun & Moon Times */}
        <div style={{
          display: 'flex',
          justify: 'center',
          gap: '24px',
          marginTop: '14px',
          paddingTop: '12px',
          borderTop: '1px dashed rgba(255, 255, 255, 0.4)',
          fontSize: '18px',
          fontWeight: 'bold',
          color: '#ffffff'
        }}>
          <span>🌅 {t('sunrise', lang)}: {data.sunrise}</span>
          <span>🌇 {t('sunset', lang)}: {data.sunset}</span>
          <span>🌕 {t('moonrise', lang)}: {data.moonrise}</span>
          <span>🌕 {t('moonset', lang)}: {data.moonset}</span>
        </div>
      </div>

      {/* Badges Bar (Muhurtham, Vasthu, Agni, Netram/Jeevan) */}
      <div style={{
        display: 'flex',
        flexWrap: 'wrap',
        gap: '12px',
        justify: 'space-between',
        alignItems: 'center',
        backgroundColor: '#fff3e0',
        padding: '16px 20px',
        borderRadius: '10px',
        border: '1.5px solid #ffe0b2',
        marginBottom: '24px',
        fontSize: '17px',
        fontWeight: 'bold'
      }}>
        <div style={{ color: data.muhurthamDay ? '#2e7d32' : '#c62828' }}>
          {data.muhurthamDay ? '✅ ' + t('subhaMuhurtham', lang) : '❌ ' + t('inauspiciousDay', lang)}
        </div>

        {data.vasthuDay && (
          <div style={{ backgroundColor: '#e8f5e9', color: '#1b5e20', padding: '6px 14px', borderRadius: '20px', border: '1px solid #a5d6a7' }}>
            🏡 {t('vasthuTitle', lang)}: {data.vasthuNeram?.start} - {data.vasthuNeram?.end} | {t('vasthuPujaTime', lang)}: {data.vasthuPujaNeram?.start} - {data.vasthuPujaNeram?.end}
          </div>
        )}

        {data.isAgniNakshathiram && (
          <div style={{ backgroundColor: '#fff3e0', color: '#e65100', padding: '6px 14px', borderRadius: '20px', border: '1px solid #ffcc80' }}>
            🔥 {t('agniNakshathiram', lang)}
          </div>
        )}

        <div style={{ color: '#424242' }}>
          👁️ {t('netram', lang)}: <strong>{data.netram}</strong> | 🌿 {t('jeevan', lang)}: <strong>{data.jeevan}</strong>
        </div>
      </div>

      {/* Core 5 Limbs & Zodiac Section */}
      <div style={{
        backgroundColor: '#ffffff',
        border: '2px solid #d84315',
        borderRadius: '12px',
        padding: '20px',
        marginBottom: '24px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
      }}>
        <h3 style={{ margin: '0 0 14px', color: '#d84315', fontSize: '22px', borderBottom: '2px solid #ffccbc', paddingBottom: '8px' }}>
          📜 {t('panchangamElements', lang)} & 🌙 {t('rashi', lang)}
        </h3>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', fontSize: '18px', lineHeight: '1.7' }}>
          <div>
            <div style={{ marginBottom: '8px' }}>
              <span style={{ color: '#d84315', fontWeight: 'bold' }}>{t('rashi', lang)}: </span>
              <strong style={{ fontSize: '20px', color: '#bf360c' }}>{data.rashi}</strong>
            </div>
            <div style={{ marginBottom: '8px' }}>
              <span style={{ color: '#d84315', fontWeight: 'bold' }}>{t('thithi', lang)}: </span>
              <span>{formatElementTiming(data.thithi)}</span>
            </div>
            <div>
              <span style={{ color: '#d84315', fontWeight: 'bold' }}>{t('nakshatra', lang)}: </span>
              <span>{formatElementTiming(data.nakshatra)}</span>
            </div>
          </div>

          <div>
            <div style={{ marginBottom: '8px' }}>
              <span style={{ color: '#c62828', fontWeight: 'bold' }}>{t('chandrastamam', lang)}: </span>
              <strong style={{ color: '#c62828' }}>
                {Array.isArray(data.chandrastamamNakshatras) ? data.chandrastamamNakshatras.join(', ') : (data.chandrastamamRashi || '')}
              </strong>
            </div>
            <div style={{ marginBottom: '8px' }}>
              <span style={{ color: '#d84315', fontWeight: 'bold' }}>{t('yogam', lang)}: </span>
              <span>{formatElementTiming(data.yogam)}</span>
            </div>
            <div>
              <span style={{ color: '#d84315', fontWeight: 'bold' }}>{t('karanam', lang)}: </span>
              <span>{formatElementTiming(data.karanam)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Auspicious Timings: Nalla Neram, Gowri Nalla Neram, Nakshatra Yogam & Abhijit Muhurtham */}
      <div style={{
        backgroundColor: '#f1f8e9',
        border: '2px solid #558b2f',
        borderRadius: '12px',
        padding: '20px',
        marginBottom: '24px'
      }}>
        <h3 style={{ margin: '0 0 14px', color: '#2e7d32', fontSize: '22px', borderBottom: '2px solid #c8e6c9', paddingBottom: '8px' }}>
          🌟 {t('auspicious', lang)}
        </h3>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', fontSize: '17px' }}>
          {/* Nalla Neram */}
          <div>
            <div style={{ fontWeight: 'bold', color: '#1b5e20', fontSize: '18px', marginBottom: '8px' }}>
              ☀️ {t('nallaNeram', lang)}
            </div>
            {data.nallaNeram?.map((s, i) => (
              <div key={i} style={{ backgroundColor: '#ffffff', padding: '8px 12px', borderRadius: '6px', border: '1px solid #c8e6c9', marginBottom: '6px' }}>
                <strong>{s.label}:</strong> {s.start} - {s.end}
              </div>
            ))}
          </div>

          {/* Gowri Nalla Neram */}
          <div>
            <div style={{ fontWeight: 'bold', color: '#1b5e20', fontSize: '18px', marginBottom: '8px' }}>
              🌙 {t('gowriNallaNeram', lang)}
            </div>
            {data.gowriNallaNeram?.map((s, i) => (
              <div key={i} style={{ backgroundColor: '#ffffff', padding: '8px 12px', borderRadius: '6px', border: '1px solid #c8e6c9', marginBottom: '6px' }}>
                <strong>{s.label}:</strong> {s.start} - {s.end}
              </div>
            ))}
          </div>

          {/* Nakshatra Yogams */}
          {data.nakshatraYogams && data.nakshatraYogams.length > 0 && (
            <div>
              <div style={{ fontWeight: 'bold', color: '#1b5e20', fontSize: '18px', marginBottom: '8px' }}>
                🌟 {t('nakshatraYogam', lang)}
              </div>
              {data.nakshatraYogams.map((s, i) => (
                <div key={i} style={{ backgroundColor: '#ffffff', padding: '8px 12px', borderRadius: '6px', border: '1px solid #c8e6c9', marginBottom: '6px' }}>
                  <strong>{s.label}:</strong> {s.start} - {s.end}
                </div>
              ))}
            </div>
          )}

          {/* Abhijit Muhurtham */}
          {data.abhijitMuhurtham && (
            <div>
              <div style={{ fontWeight: 'bold', color: '#1b5e20', fontSize: '18px', marginBottom: '8px' }}>
                ☀️ {t('abhijitMuhurtham', lang)}
              </div>
              <div style={{ backgroundColor: '#ffffff', padding: '8px 12px', borderRadius: '6px', border: '1px solid #c8e6c9' }}>
                <strong>{data.abhijitMuhurtham.label || t('abhijitMuhurtham', lang)}:</strong> {data.abhijitMuhurtham.start} - {data.abhijitMuhurtham.end}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Inauspicious Timings */}
      <div style={{
        backgroundColor: '#ffebee',
        border: '2px solid #c62828',
        borderRadius: '12px',
        padding: '20px',
        marginBottom: '24px'
      }}>
        <h3 style={{ margin: '0 0 14px', color: '#c62828', fontSize: '22px', borderBottom: '2px solid #ffcdd2', paddingBottom: '8px' }}>
          ⚠️ {t('inauspicious', lang)}
        </h3>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '16px', fontSize: '17px' }}>
          <div style={{ backgroundColor: '#ffffff', padding: '10px 14px', borderRadius: '8px', border: '1px solid #ffcdd2' }}>
            <div style={{ fontWeight: 'bold', color: '#b71c1c' }}>{t('rahuKalam', lang)}</div>
            <div>{data.raghuKalam?.map(s => `${s.start} - ${s.end}`).join(', ')}</div>
          </div>

          <div style={{ backgroundColor: '#ffffff', padding: '10px 14px', borderRadius: '8px', border: '1px solid #ffcdd2' }}>
            <div style={{ fontWeight: 'bold', color: '#b71c1c' }}>{t('yamagandam', lang)}</div>
            <div>{data.emagandam?.map(s => `${s.start} - ${s.end}`).join(', ')}</div>
          </div>

          <div style={{ backgroundColor: '#ffffff', padding: '10px 14px', borderRadius: '8px', border: '1px solid #ffcdd2' }}>
            <div style={{ fontWeight: 'bold', color: '#b71c1c' }}>{t('gulikaKalam', lang)}</div>
            <div>{data.kulikai?.map(s => `${s.start} - ${s.end}`).join(', ')}</div>
          </div>
        </div>
      </div>

      {/* 24 Horai Table */}
      <div style={{
        backgroundColor: '#ffffff',
        border: '2px solid #f57c00',
        borderRadius: '12px',
        padding: '20px',
        marginBottom: '24px'
      }}>
        <h3 style={{ margin: '0 0 14px', color: '#e65100', fontSize: '22px', borderBottom: '2px solid #ffe0b2', paddingBottom: '8px' }}>
          ⏳ {t('horas', lang)}
        </h3>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: '10px', fontSize: '15px' }}>
          {data.horais?.map((h, idx) => (
            <div key={idx} style={{
              backgroundColor: '#fff8e1',
              padding: '8px 10px',
              borderRadius: '6px',
              border: '1px solid #ffe082',
              display: 'flex',
              flexDirection: 'column',
              gap: '2px'
            }}>
              <div style={{ fontWeight: 'bold', color: '#bf360c' }}>Hr {h.hour}: {h.start} - {h.end}</div>
              <div style={{ fontWeight: '600', color: '#424242' }}>{h.localizedPlanet || h.planet}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Footer Branding */}
      <div style={{
        textAlign: 'center',
        borderTop: '2px dashed #b71c1c',
        paddingTop: '16px',
        fontSize: '20px',
        color: '#b71c1c',
        fontWeight: 'bold',
        letterSpacing: '0.5px'
      }}>
        Generated by https://tinyurl.com/drik-vedic
      </div>
    </div>
  );
}
