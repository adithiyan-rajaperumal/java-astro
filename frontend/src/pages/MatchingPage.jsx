import { useState, useEffect } from 'react';
import LocationSearch from '../components/LocationSearch';
import AiMatchingView from '../components/AiMatchingView';
import { t } from '../i18n/translations';
import { getSavedHoroscopes } from '../utils/savedHoroscopes';

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

  const [activeSubTab, setActiveSubTab] = useState('classical');
  const [result, setResult] = useState(null);
  const [aiResult, setAiResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);
  const [pdfLoading, setPdfLoading] = useState(false);
  const [error, setError] = useState(null);
  const [savedProfiles] = useState(() => getSavedHoroscopes());

  useEffect(() => {
    if (settings.ayanamsa) {
      setAyanamsa(settings.ayanamsa);
    }
  }, [settings.ayanamsa]);

  useEffect(() => {
    setResult(null);
    setAiResult(null);
  }, [settings.language]);

  const handleSelectBoyProfile = (id) => {
    const p = savedProfiles.find(x => x.id === id);
    if (!p) return;
    setBoyName(p.name);
    setBoyDate(`${String(p.day).padStart(2, '0')}/${String(p.month).padStart(2, '0')}/${p.year}`);
    setBoyTime(`${String(p.hour).padStart(2, '0')}:${String(p.minute).padStart(2, '0')}`);
    setBoyLocation(p.location);
  };

  const handleSelectGirlProfile = (id) => {
    const p = savedProfiles.find(x => x.id === id);
    if (!p) return;
    setGirlName(p.name);
    setGirlDate(`${String(p.day).padStart(2, '0')}/${String(p.month).padStart(2, '0')}/${p.year}`);
    setGirlTime(`${String(p.hour).padStart(2, '0')}:${String(p.minute).padStart(2, '0')}`);
    setGirlLocation(p.location);
  };

  const handleDateChange = (val, setter) => {
    setter(val);
  };

  const parseDateText = (str) => {
    if (!str) return null;
    const parts = str.split('/');
    if (parts.length !== 3) return null;
    const day = parseInt(parts[0], 10);
    const month = parseInt(parts[1], 10);
    const year = parseInt(parts[2], 10);
    if (isNaN(day) || isNaN(month) || isNaN(year)) return null;
    return { day, month, year };
  };

  const handleMatch = async (e) => {
    e.preventDefault();
    if (!boyLocation || !girlLocation) {
      alert('Please select valid locations for both Boy and Girl.');
      return;
    }

    const bDateParsed = parseDateText(boyDate);
    const gDateParsed = parseDateText(girlDate);
    if (!bDateParsed || !gDateParsed) {
      alert('Please enter valid dates in DD/MM/YYYY format.');
      return;
    }

    const [bHour, bMinute] = boyTime.split(':').map(Number);
    const [gHour, gMinute] = girlTime.split(':').map(Number);

    setLoading(true);
    setError(null);
    setResult(null);
    setAiResult(null);
    setActiveSubTab('classical');

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
      const response = await fetch('/api/v1/astrology/match?systemType=DRIK_TIRUKANITHAM', {
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
        const errText = await response.text();
        throw new Error(errText || 'Failed to calculate compatibility match.');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateAiMatching = async (forceRefresh = false) => {
    if (!boyLocation || !girlLocation || aiLoading) return;
    const bDateParsed = parseDateText(boyDate);
    const gDateParsed = parseDateText(girlDate);
    if (!bDateParsed || !gDateParsed) return;

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

    setAiLoading(true);
    try {
      const response = await fetch(`/api/v1/astrology/match/ai?systemType=DRIK_TIRUKANITHAM&language=${settings.language}&forceRefresh=${forceRefresh}`, {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Accept-Language': settings.language
        },
        body: JSON.stringify(payload)
      });
      if (response.ok) {
        const data = await response.json();
        setAiResult(data);
      } else {
        const errText = await response.text();
        throw new Error(errText || 'Failed to generate AI marriage compatibility analysis.');
      }
    } catch (err) {
      console.error(err);
      setAiResult({
        enabled: false,
        message: 'Could not generate AI marriage compatibility report at this moment.'
      });
    } finally {
      setAiLoading(false);
    }
  };

  const handleDownloadPdf = async () => {
    if (!boyLocation || !girlLocation || !result || pdfLoading) return;
    const bDateParsed = parseDateText(boyDate);
    const gDateParsed = parseDateText(girlDate);
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

    setPdfLoading(true);
    try {
      const response = await fetch('/api/v1/astrology/match/download-pdf?systemType=DRIK_TIRUKANITHAM', {
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
    } finally {
      setPdfLoading(false);
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
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border)', paddingBottom: '10px' }}>
                <h3 className="title-gold" style={{ margin: 0 }}>
                  🙋‍♂️ {t('boyDetails', settings.language)}
                </h3>
                {savedProfiles.length > 0 && (
                  <select
                    onChange={(e) => handleSelectBoyProfile(e.target.value)}
                    defaultValue=""
                    style={{ padding: '4px 8px', fontSize: '12px', maxWidth: '160px' }}
                  >
                    <option value="" disabled>📁 {t('loadSaved', settings.language) || 'Load Profile'}</option>
                    {savedProfiles.map(p => (
                      <option key={p.id} value={p.id}>{p.name}</option>
                    ))}
                  </select>
                )}
              </div>
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
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border)', paddingBottom: '10px' }}>
                <h3 className="title-gold" style={{ margin: 0 }}>
                  🙋‍♀️ {t('girlDetails', settings.language)}
                </h3>
                {savedProfiles.length > 0 && (
                  <select
                    onChange={(e) => handleSelectGirlProfile(e.target.value)}
                    defaultValue=""
                    style={{ padding: '4px 8px', fontSize: '12px', maxWidth: '160px' }}
                  >
                    <option value="" disabled>📁 {t('loadSaved', settings.language) || 'Load Profile'}</option>
                    {savedProfiles.map(p => (
                      <option key={p.id} value={p.id}>{p.name}</option>
                    ))}
                  </select>
                )}
              </div>
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
          <div className="card" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
            <div>
              <label>{t('methodology', settings.language)}</label>
              <select value={matchingSystem} onChange={(e) => setMatchingSystem(e.target.value)}>
                <option value="ASHTA_KOOTA">{t('ashtaKoota', settings.language)}</option>
                <option value="DASA_PORUTHAM">{t('dasaPorutham', settings.language)}</option>
              </select>
            </div>
            <div>
              <label>{t('strictness', settings.language)}</label>
              <select value={strictness} onChange={(e) => setStrictness(e.target.value)}>
                <option value="LENIENT">{t('strictnessLenient', settings.language)}</option>
                <option value="MODERATE">{t('strictnessModerate', settings.language)}</option>
                <option value="STRICT">{t('strictnessStrict', settings.language)}</option>
              </select>
            </div>
            <div>
              <label>{t('ayanamsa', settings.language)}</label>
              <select value={ayanamsa} onChange={(e) => setAyanamsa(e.target.value)}>
                <option value="LAHIRI">{t('ayanamsaLahiri', settings.language)}</option>
                <option value="KP">{t('ayanamsaKP', settings.language)}</option>
                <option value="RAMAN">{t('ayanamsaRaman', settings.language)}</option>
                <option value="SURYA_SIDDHANTA">{t('ayanamsaSurya', settings.language)}</option>
                <option value="PUSHYAPAKSHA">{t('ayanamsaPushyapaksha', settings.language)}</option>
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
          <p>{t('analyzingMatchNotice', settings.language)}</p>
        </div>
      )}

      {error && (
        <div className="card" style={{ borderLeft: '4px solid var(--danger)' }}>
          <p style={{ color: 'var(--danger)', fontWeight: 'bold' }}>{t('matchingEngineError', settings.language)}</p>
          <p>{error}</p>
          <button onClick={() => setResult(null)} className="btn-primary" style={{ marginTop: '10px' }}>{t('tryAgain', settings.language)}</button>
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
              🙋‍♂️ {result.boyProfile?.name} ({result.boyProfile?.birthProfile?.nakshatra}) &nbsp;|&nbsp; 🙋‍♀️ {result.girlProfile?.name} ({result.girlProfile?.birthProfile?.nakshatra}) &nbsp;|&nbsp; {t('ayanamsa', settings.language)}: {result.boyProfile?.ayanamsa || ayanamsa} &nbsp;|&nbsp; {t('panchangamSystem', settings.language)}: {t('system' + (result.panchangamSystem === 'VAKYA' ? 'Vakya' : result.panchangamSystem === 'PARASARA_BHATTAR' ? 'ParasaraBhattar' : result.panchangamSystem === 'SURYA_SIDDHANTA' ? 'SuryaSiddhanta' : 'Drik'), settings.language)}
            </div>
            
            <div style={{ marginTop: '20px', display: 'flex', gap: '15px' }}>
              <button 
                onClick={handleDownloadPdf} 
                disabled={pdfLoading}
                className="btn-primary"
                style={{ opacity: pdfLoading ? 0.75 : 1, cursor: pdfLoading ? 'wait' : 'pointer' }}
              >
                {pdfLoading ? `⏳ ${t('generatingPdf', settings.language)}` : `📥 ${t('downloadPdf', settings.language)}`}
              </button>
              <button onClick={() => setResult(null)} className="btn-primary" style={{ background: 'none', border: '1px solid var(--border)', color: 'var(--text-primary)' }}>
                {t('newMatch', settings.language)}
              </button>
            </div>
          </div>

          {/* Subtabs Navigation */}
          <div className="tabs-header" style={{
            display: 'flex',
            gap: '8px',
            marginBottom: '20px',
            overflowX: 'auto',
            paddingBottom: '8px',
            scrollSnapType: 'x mandatory',
            WebkitOverflowScrolling: 'touch'
          }}>
            <button
              onClick={() => setActiveSubTab('classical')}
              className={`tab-btn ${activeSubTab === 'classical' ? 'active' : ''}`}
              style={{ flexShrink: 0, scrollSnapAlign: 'start' }}
            >
              📊 {t('classicalMatching', settings.language)}
            </button>
            <button
              onClick={() => {
                setActiveSubTab('ai_matching');
                if (!aiResult && !aiLoading) {
                  handleGenerateAiMatching(false);
                }
              }}
              className={`tab-btn ${activeSubTab === 'ai_matching' ? 'active' : ''}`}
              style={{ flexShrink: 0, scrollSnapAlign: 'start' }}
            >
              ✨ {t('aiMatchingTitle', settings.language)}
            </button>
          </div>

          {activeSubTab === 'classical' && (
            <div>
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
                                {t('exceptionLabel', settings.language)}: {k.nullificationReason}
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

          {activeSubTab === 'ai_matching' && (
            <AiMatchingView
              aiData={aiResult}
              loading={aiLoading}
              onGenerate={() => handleGenerateAiMatching(true)}
              language={settings.language}
            />
          )}
        </div>
      )}
    </div>
  );
}

export default MatchingPage;
