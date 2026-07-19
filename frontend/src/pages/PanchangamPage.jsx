import { useState, useEffect } from 'react';

function PanchangamPage({ settings }) {
  const [currentDate, setCurrentDate] = useState(new Date().toISOString().split('T')[0]);
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
        headers: { 'Content-Type': 'application/json' },
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
    const d = new Date(currentDate);
    d.setDate(d.getDate() + days);
    setCurrentDate(d.toISOString().split('T')[0]);
  };

  const renderTimeSlotList = (slots = [], title, isAuspicious) => {
    if (!slots || slots.length === 0) return null;
    return (
      <div className="time-slot-container">
        <h4 style={{ margin: '10px 0 5px', fontSize: '14px', color: 'var(--text-secondary)' }}>{title}</h4>
        {slots.map((s, idx) => (
          <div key={idx} className={`time-slot-bar ${isAuspicious ? 'auspicious' : 'inauspicious'}`}>
            <span>{s.label}</span>
            <span>{s.start} - {s.end}</span>
          </div>
        ))}
      </div>
    );
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <button onClick={() => changeDate(-1)} className="btn-primary" style={{ padding: '8px 15px' }}>← Prev</button>
        <span style={{ fontSize: '18px', fontWeight: 'bold', color: 'var(--accent-gold)' }}>
          📅 {currentDate}
        </span>
        <button onClick={() => changeDate(1)} className="btn-primary" style={{ padding: '8px 15px' }}>Next →</button>
      </div>

      {loading && (
        <div className="spinner-container">
          <div className="spinner"></div>
          <p>Calculating planetary equations...</p>
        </div>
      )}

      {error && (
        <div className="card" style={{ borderLeft: '4px solid var(--danger)' }}>
          <p style={{ color: 'var(--danger)', fontWeight: 'bold' }}>Error Loading Panchangam</p>
          <p>{error}</p>
          <button onClick={() => fetchPanchangam(currentDate)} className="btn-primary" style={{ marginTop: '10px' }}>Retry</button>
        </div>
      )}

      {!loading && !error && data && (
        <div className="grid-2">
          {/* Main Panchangam Elements */}
          <div>
            <div className="card">
              <h3 className="title-gold">Sun & Moon Timings</h3>
              <div className="grid-2" style={{ fontSize: '15px' }}>
                <div>🌅 <strong>Sunrise:</strong> {data.sunrise}</div>
                <div>🌇 <strong>Sunset:</strong> {data.sunset}</div>
                <div>🌙 <strong>Moonrise:</strong> {data.moonrise}</div>
                <div>🌕 <strong>Moonset:</strong> {data.moonset}</div>
              </div>
            </div>

            <div className="card">
              <h3 className="title-gold">Panchangam Elements</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <div>
                  <strong>Thithi:</strong> {data.thithi?.localizedName || data.thithi?.name} 
                  <span style={{ color: 'var(--text-secondary)', marginLeft: '10px' }}>ends at {data.thithi?.endTime}</span>
                </div>
                <div>
                  <strong>Nakshatra:</strong> {data.nakshatra?.localizedName || data.nakshatra?.name}
                  <span style={{ color: 'var(--text-secondary)', marginLeft: '10px' }}>ends at {data.nakshatra?.endTime}</span>
                </div>
                <div>
                  <strong>Yogam:</strong> {data.yogam?.localizedName || data.yogam?.name}
                  <span style={{ color: 'var(--text-secondary)', marginLeft: '10px' }}>ends at {data.yogam?.endTime}</span>
                </div>
                <div>
                  <strong>Karanam:</strong> {data.karanam?.localizedName || data.karanam?.name}
                  <span style={{ color: 'var(--text-secondary)', marginLeft: '10px' }}>ends at {data.karanam?.endTime}</span>
                </div>
                <div style={{ borderTop: '1px solid var(--border)', paddingTop: '10px', marginTop: '5px' }}>
                  <strong>Rashi:</strong> {data.rashi} | <strong>Chandrastamam:</strong> {data.chandrastamamRashi}
                </div>
              </div>
            </div>

            <div className="card">
              <h3 className="title-gold">Muhurtham & Vasthu</h3>
              <div className="grid-2">
                <div style={{ color: data.muhurthamDay ? 'var(--success)' : 'var(--danger)', fontWeight: 'bold' }}>
                  {data.muhurthamDay ? '✅ Auspicious Day' : '❌ Inauspicious / Regular Day'}
                </div>
                <div style={{ color: data.vasthuDay ? 'var(--success)' : 'var(--text-secondary)' }}>
                  🏡 {data.vasthuDay ? 'Vasthu Day (Auspicious for construction)' : 'No Vasthu Activity'}
                </div>
              </div>
              <div style={{ display: 'flex', gap: '15px', marginTop: '15px', fontSize: '13px', color: 'var(--text-secondary)' }}>
                <div>Netram: {data.netram}</div>
                <div>Jeevan: {data.jeevan}</div>
              </div>
            </div>
          </div>

          {/* Time Slots & Horai */}
          <div>
            <div className="card">
              <h3 className="title-gold">Auspicious Times</h3>
              {renderTimeSlotList(data.nallaNeram, 'Nalla Neram', true)}
              {renderTimeSlotList(data.gowriNallaNeram, 'Gowri Nalla Neram', true)}
            </div>

            <div className="card">
              <h3 className="title-gold">Inauspicious Times</h3>
              {renderTimeSlotList(data.raghuKalam, 'Rahu Kalam', false)}
              {renderTimeSlotList(data.emagandam, 'Yamagandam', false)}
              {renderTimeSlotList(data.kulikai, 'Gulika Kalam', false)}
            </div>
          </div>

          {/* 24 Horai Table */}
          <div style={{ gridColumn: '1 / -1' }} className="card">
            <h3 className="title-gold">24 Horas (Hourly Divisions)</h3>
            <div className="horai-table-container">
              <table className="horai-table">
                <thead>
                  <tr>
                    <th>Hr</th>
                    <th>Interval</th>
                    <th>Planet</th>
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
