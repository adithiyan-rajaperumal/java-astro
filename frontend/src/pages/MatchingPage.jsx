import { useState } from 'react';
import LocationSearch from '../components/LocationSearch';

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

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleMatch = async (e) => {
    e.preventDefault();
    if (!boyName.trim() || !boyDate || !boyTime || !boyLocation ||
        !girlName.trim() || !girlDate || !girlTime || !girlLocation) {
      alert('Please fill in all details for both Boy and Girl.');
      return;
    }

    setLoading(true);
    setError(null);

    const [bYear, bMonth, bDay] = boyDate.split('-').map(Number);
    const [bHour, bMinute] = boyTime.split(':').map(Number);
    const [gYear, gMonth, gDay] = girlDate.split('-').map(Number);
    const [gHour, gMinute] = girlTime.split(':').map(Number);

    const payload = {
      boy: {
        name: boyName,
        year: bYear,
        month: bMonth,
        day: bDay,
        hour: bHour,
        minute: bMinute,
        second: 0,
        latitude: boyLocation.latitude,
        longitude: boyLocation.longitude,
        ayanamsa: settings.ayanamsa
      },
      girl: {
        name: girlName,
        year: gYear,
        month: gMonth,
        day: gDay,
        hour: gHour,
        minute: gMinute,
        second: 0,
        latitude: girlLocation.latitude,
        longitude: girlLocation.longitude,
        ayanamsa: settings.ayanamsa
      },
      matchingSystem,
      strictness
    };

    try {
      const response = await fetch('/api/v1/astrology/match', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
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
    const [bYear, bMonth, bDay] = boyDate.split('-').map(Number);
    const [bHour, bMinute] = boyTime.split(':').map(Number);
    const [gYear, gMonth, gDay] = girlDate.split('-').map(Number);
    const [gHour, gMinute] = girlTime.split(':').map(Number);

    const payload = {
      boy: {
        name: boyName,
        year: bYear,
        month: bMonth,
        day: bDay,
        hour: bHour,
        minute: bMinute,
        second: 0,
        latitude: boyLocation.latitude,
        longitude: boyLocation.longitude,
        ayanamsa: settings.ayanamsa
      },
      girl: {
        name: girlName,
        year: gYear,
        month: gMonth,
        day: gDay,
        hour: gHour,
        minute: gMinute,
        second: 0,
        latitude: girlLocation.latitude,
        longitude: girlLocation.longitude,
        ayanamsa: settings.ayanamsa
      },
      matchingSystem,
      strictness
    };

    try {
      const response = await fetch(`/api/v1/astrology/match/download-pdf?systemType=DRIK_TIRUKANITHAM`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
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
      <h2 className="title-gold">Marriage Compatibility Matching</h2>

      {!result && !loading && (
        <form onSubmit={handleMatch}>
          <div className="grid-2">
            {/* Boy's card */}
            <div className="card">
              <h3 className="title-gold" style={{ borderBottom: '1px solid var(--border)', paddingBottom: '10px' }}>
                🙋‍♂️ Boy's Details
              </h3>
              <div style={{ marginTop: '15px' }}>
                <label>Name</label>
                <input type="text" value={boyName} onChange={(e) => setBoyName(e.target.value)} required />
              </div>
              <div className="grid-2">
                <div>
                  <label>Birth Date</label>
                  <input type="date" value={boyDate} onChange={(e) => setBoyDate(e.target.value)} required />
                </div>
                <div>
                  <label>Birth Time</label>
                  <input type="time" value={boyTime} onChange={(e) => setBoyTime(e.target.value)} required />
                </div>
              </div>
              <div>
                <label>Birth Location</label>
                <LocationSearch value={boyLocation} onChange={setBoyLocation} />
              </div>
            </div>

            {/* Girl's card */}
            <div className="card">
              <h3 className="title-gold" style={{ borderBottom: '1px solid var(--border)', paddingBottom: '10px' }}>
                🙋‍♀️ Girl's Details
              </h3>
              <div style={{ marginTop: '15px' }}>
                <label>Name</label>
                <input type="text" value={girlName} onChange={(e) => setGirlName(e.target.value)} required />
              </div>
              <div className="grid-2">
                <div>
                  <label>Birth Date</label>
                  <input type="date" value={girlDate} onChange={(e) => setGirlDate(e.target.value)} required />
                </div>
                <div>
                  <label>Birth Time</label>
                  <input type="time" value={girlTime} onChange={(e) => setGirlTime(e.target.value)} required />
                </div>
              </div>
              <div>
                <label>Birth Location</label>
                <LocationSearch value={girlLocation} onChange={setGirlLocation} />
              </div>
            </div>
          </div>

          {/* Settings block */}
          <div className="card grid-2">
            <div>
              <label>Matching Methodology</label>
              <select value={matchingSystem} onChange={(e) => setMatchingSystem(e.target.value)}>
                <option value="ASHTA_KOOTA">Ashta Koota (North Indian 36 Points)</option>
                <option value="DASA_PORUTHAM">Dasa Porutham (South Indian 10 matches)</option>
              </select>
            </div>
            <div>
              <label>Match Strictness</label>
              <select value={strictness} onChange={(e) => setStrictness(e.target.value)}>
                <option value="LENIENT">Lenient</option>
                <option value="MODERATE">Moderate</option>
                <option value="STRICT">Strict</option>
              </select>
            </div>
          </div>

          <button type="submit" className="btn-primary" style={{ width: '100%', padding: '15px', fontSize: '16px' }}>
            Calculate Compatibility Match
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
                out of {result.maxScore}
              </span>
            </div>
            <div className={`verdict-badge ${getVerdictClass(result.verdict)}`}>
              {result.verdict} ({result.percentage.toFixed(0)}%)
            </div>
            
            <div style={{ marginTop: '20px', display: 'flex', gap: '15px' }}>
              <button onClick={handleDownloadPdf} className="btn-primary">
                📥 Download PDF Compatibility Report
              </button>
              <button onClick={() => setResult(null)} className="btn-primary" style={{ background: 'none', border: '1px solid var(--border)', color: 'var(--text-primary)' }}>
                New Match
              </button>
            </div>
          </div>

          {/* Warnings and alerts */}
          {result.warnings && result.warnings.length > 0 && (
            <div className="card" style={{ borderLeft: '4px solid var(--warning)', backgroundColor: 'rgba(255, 152, 0, 0.05)' }}>
              <h4 style={{ margin: '0 0 10px', color: 'var(--accent-gold)' }}>⚠️ Compatibility Notes & Warnings</h4>
              <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '14px', color: 'var(--text-primary)' }}>
                {result.warnings.map((w, idx) => (
                  <li key={idx} style={{ marginBottom: '5px' }}>{w}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Koota/Porutham detail table */}
          <div className="card">
            <h3 className="title-gold">Porutham/Koota Detailed Breakdown</h3>
            <div className="horai-table-container">
              <table className="horai-table">
                <thead>
                  <tr>
                    <th>Match Parameter</th>
                    <th>Scored</th>
                    <th>Max</th>
                    <th>Status</th>
                    <th>Detail</th>
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
