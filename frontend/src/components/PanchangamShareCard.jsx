import React from 'react';

export function PanchangamShareCard({ data, currentDate, settings }) {
  if (!data) return null;

  return (
    <div
      id="panchangam-share-card"
      style={{
        width: '1080px',
        padding: '36px',
        backgroundColor: '#13141c',
        color: '#ffffff',
        fontFamily: "'Segoe UI', Roboto, sans-serif",
        boxSizing: 'border-box',
        borderRadius: '20px',
        border: '3px solid #ffd700',
        boxShadow: '0 20px 50px rgba(0,0,0,0.8)'
      }}
    >
      {/* Header Banner */}
      <div style={{ textAlign: 'center', borderBottom: '2px dashed #ffd700', paddingBottom: '20px', marginBottom: '24px' }}>
        <h1 style={{ margin: 0, color: '#ffd700', fontSize: '38px', letterSpacing: '1px' }}>
          🕉️ VEDIC ASTRO PANCHANGAM
        </h1>
        <div style={{ fontSize: '24px', marginTop: '10px', color: '#ffb74d', fontWeight: 'bold' }}>
          📅 {currentDate} | 📍 {settings?.location?.name || 'Chennai'}
        </div>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '30px', marginTop: '12px', fontSize: '20px', color: '#e0e0e0' }}>
          <span>🌅 Sunrise: {data.sunrise}</span>
          <span>🌇 Sunset: {data.sunset}</span>
          <span>🌕 Moonrise: {data.moonrise}</span>
        </div>
      </div>

      {/* Core Panchangam Badges & Limbs */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '24px' }}>
        <div style={{ background: 'rgba(255, 215, 0, 0.08)', padding: '20px', borderRadius: '14px', borderLeft: '6px solid #ffd700' }}>
          <div style={{ fontSize: '20px', color: '#ffd700', fontWeight: 'bold' }}>📜 Core Limbs & Zodiac</div>
          <div style={{ fontSize: '18px', marginTop: '10px', lineHeight: '1.6' }}>
            <div><strong>Rashi:</strong> {data.rashi}</div>
            <div><strong>Chandrastamam:</strong> {Array.isArray(data.chandrastamamNakshatras) ? data.chandrastamamNakshatras.join(', ') : (data.chandrastamamRashi || '')}</div>
            <div><strong>Thithi:</strong> {data.thithi?.localizedName || data.thithi?.name} {data.thithi?.endTime ? `(${data.thithi.endTime})` : ''}</div>
            <div><strong>Nakshatra:</strong> {data.nakshatra?.localizedName || data.nakshatra?.name} {data.nakshatra?.endTime ? `(${data.nakshatra.endTime})` : ''}</div>
            <div><strong>Yogam:</strong> {data.yogam?.localizedName || data.yogam?.name} {data.yogam?.endTime ? `(${data.yogam.endTime})` : ''}</div>
          </div>
        </div>

        <div style={{ background: 'rgba(76, 175, 80, 0.08)', padding: '20px', borderRadius: '14px', borderLeft: '6px solid #4caf50' }}>
          <div style={{ fontSize: '20px', color: '#4caf50', fontWeight: 'bold' }}>✨ Auspicious & Vasthu Badges</div>
          <div style={{ fontSize: '18px', marginTop: '10px', lineHeight: '1.6' }}>
            <div><strong>Status:</strong> {data.muhurthamDay ? '✅ Subha Muhurtham Day' : '❌ Inauspicious Day'}</div>
            {data.vasthuDay && <div><strong>🏡 Vasthu Neram:</strong> {data.vasthuNeram?.start} - {data.vasthuNeram?.end}</div>}
            {data.vasthuPujaNeram && <div><strong>🙏 Vasthu Puja:</strong> {data.vasthuPujaNeram?.start} - {data.vasthuPujaNeram?.end}</div>}
            <div><strong>Netram:</strong> {data.netram} | <strong>Jeevan:</strong> {data.jeevan}</div>
          </div>
        </div>
      </div>

      {/* Auspicious Time Slots (Nalla Neram & Gowri Nalla Neram) */}
      <div style={{ background: 'rgba(33, 150, 243, 0.08)', padding: '20px', borderRadius: '14px', borderLeft: '6px solid #2196f3', marginBottom: '24px' }}>
        <div style={{ fontSize: '20px', color: '#2196f3', fontWeight: 'bold', marginBottom: '10px' }}>🌟 Nalla Neram & Gowri Nalla Neram</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', fontSize: '17px' }}>
          <div>
            <div style={{ fontWeight: 'bold', color: '#90caf9' }}>☀️ Nalla Neram:</div>
            {data.nallaNeram?.map((slot, i) => (
              <div key={i}>• {slot.label}: {slot.start} - {slot.end}</div>
            ))}
          </div>
          <div>
            <div style={{ fontWeight: 'bold', color: '#90caf9' }}>🌙 Gowri Nalla Neram:</div>
            {data.gowriNallaNeram?.map((slot, i) => (
              <div key={i}>• {slot.label}: {slot.start} - {slot.end}</div>
            ))}
          </div>
        </div>
      </div>

      {/* Inauspicious Times (Rahu / Yama / Gulika) */}
      <div style={{ background: 'rgba(244, 67, 54, 0.08)', padding: '20px', borderRadius: '14px', borderLeft: '6px solid #f44336', marginBottom: '24px' }}>
        <div style={{ fontSize: '20px', color: '#f44336', fontWeight: 'bold', marginBottom: '10px' }}>⚠️ Inauspicious Timings</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '16px', fontSize: '17px' }}>
          <div><strong>Rahu Kalam:</strong> {data.raghuKalam?.map(s => `${s.start} - ${s.end}`).join(', ')}</div>
          <div><strong>Yamagandam:</strong> {data.emagandam?.map(s => `${s.start} - ${s.end}`).join(', ')}</div>
          <div><strong>Gulika Kalam:</strong> {data.kulikai?.map(s => `${s.start} - ${s.end}`).join(', ')}</div>
        </div>
      </div>

      {/* 24 Horai Table */}
      <div style={{ background: 'rgba(255, 255, 255, 0.03)', padding: '20px', borderRadius: '14px', border: '1px solid rgba(255, 215, 0, 0.3)', marginBottom: '24px' }}>
        <div style={{ fontSize: '20px', color: '#ffd700', fontWeight: 'bold', marginBottom: '12px' }}>⏳ 24 Horais (Day & Night)</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: '10px', fontSize: '15px' }}>
          {data.horais?.map((h, idx) => (
            <div key={idx} style={{ background: 'rgba(0,0,0,0.4)', padding: '8px 12px', borderRadius: '8px', border: '1px solid #333' }}>
              <strong>Hr {h.hour}:</strong> {h.start} - {h.end} <br/>
              <span style={{ color: '#ffb74d' }}>{h.localizedPlanet || h.planet}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Footer Branding */}
      <div style={{ textAlign: 'center', borderTop: '1px dashed #ffd700', paddingTop: '16px', fontSize: '18px', color: '#ffd700', fontWeight: 'bold' }}>
        Generated by https://tinyurl.com/drik-vedic
      </div>
    </div>
  );
}
