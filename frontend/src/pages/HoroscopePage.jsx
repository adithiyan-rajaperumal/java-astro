import { useState, useEffect } from 'react';
import BirthForm from '../components/BirthForm';
import IndianChart from '../components/IndianChart';
import AiPredictionsView from '../components/AiPredictionsView';
import DailyBalanView from '../components/DailyBalanView';
import { t } from '../i18n/translations';
import { getSavedHoroscopes, saveHoroscope, deleteSavedHoroscope, isProfileAlreadySaved } from '../utils/savedHoroscopes';

const AYANAMSA_I18N_MAP = {
  LAHIRI: 'ayanamsaLahiri',
  KP: 'ayanamsaKP',
  RAMAN: 'ayanamsaRaman',
  SURYA_SIDDHANTA: 'ayanamsaSurya',
  PUSHYAPAKSHA: 'ayanamsaPushyapaksha'
};

const PANCHANGAM_I18N_MAP = {
  DRIK_TIRUKANITHAM: 'panchangamThirukanitham',
  VAKYAM: 'panchangamVakyam',
  SURYA_SIDDHANTA: 'panchangamSurya'
};

export const getAyanamsaLabel = (val, lang = 'en') => {
  if (!val) return '';
  const key = AYANAMSA_I18N_MAP[val.toUpperCase()] || `ayanamsa${val}`;
  const translated = t(key, lang);
  return translated !== key ? translated : val;
};

export const getPanchangamSystemLabel = (val, lang = 'en') => {
  if (!val) return '';
  const key = PANCHANGAM_I18N_MAP[val.toUpperCase()] || `panchangam${val}`;
  const translated = t(key, lang);
  return translated !== key ? translated : val;
};

function HoroscopePage({ settings }) {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [activeSubTab, setActiveSubTab] = useState('charts');
  const [expandedDasa, setExpandedDasa] = useState(null);
  const [formPayload, setFormPayload] = useState(null);
  const [savedProfiles, setSavedProfiles] = useState(() => getSavedHoroscopes());
  const [saveSuccessMsg, setSaveSuccessMsg] = useState('');

  // Granular Feature Flags from Backend
  const [lifeEnabled, setLifeEnabled] = useState(true);
  const [dailyEnabled, setDailyEnabled] = useState(true);
  const [pdfLoading, setPdfLoading] = useState(false);

  // Lifetime Predictions state
  const [predictions, setPredictions] = useState(null);
  const [predLoading, setPredLoading] = useState(false);
  const [predError, setPredError] = useState(null);

  // Daily Balan state
  const [dailyBalan, setDailyBalan] = useState(null);
  const [dailyLoading, setDailyLoading] = useState(false);
  const [dailyError, setDailyError] = useState(null);

  useEffect(() => {
    fetch('/api/v1/astrology/config')
      .then((res) => (res.ok ? res.json() : null))
      .then((cfg) => {
        if (cfg) {
          if (typeof cfg.lifePredictionsEnabled === 'boolean') {
            setLifeEnabled(cfg.lifePredictionsEnabled);
          } else if (typeof cfg.aiPredictionsEnabled === 'boolean') {
            setLifeEnabled(cfg.aiPredictionsEnabled);
          }
          if (typeof cfg.dailyBalanEnabled === 'boolean') {
            setDailyEnabled(cfg.dailyBalanEnabled);
          }
        }
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    setReport(null);
    setPredictions(null);
    setDailyBalan(null);
    setActiveSubTab('charts'); // Reset subtab on language change
  }, [settings.language]);

  const handleResetToNewChart = () => {
    setReport(null);
    setPredictions(null);
    setDailyBalan(null);
    setActiveSubTab('charts'); // Clean reset so tabs never stick
  };

  const handleSaveCurrentProfile = () => {
    if (!formPayload) return;
    const profileToSave = {
      name: formPayload.name,
      year: formPayload.year,
      month: formPayload.month,
      day: formPayload.day,
      hour: formPayload.hour,
      minute: formPayload.minute,
      second: 0,
      latitude: formPayload.latitude,
      longitude: formPayload.longitude,
      location: formPayload.location,
      ayanamsa: formPayload.ayanamsa || settings.ayanamsa || 'LAHIRI',
      panchangamSystem: 'DRIK_TIRUKANITHAM'
    };
    const updated = saveHoroscope(profileToSave);
    setSavedProfiles(updated);
    setSaveSuccessMsg(t('profileSaved', settings.language) || 'Profile Saved!');
    setTimeout(() => setSaveSuccessMsg(''), 3000);
  };

  const handleDeleteProfile = (e, id) => {
    e.stopPropagation();
    const updated = deleteSavedHoroscope(id);
    setSavedProfiles(updated);
  };

  const handleLoadSavedProfile = (prof) => {
    const payload = {
      name: prof.name,
      year: prof.year,
      month: prof.month,
      day: prof.day,
      hour: prof.hour,
      minute: prof.minute,
      second: prof.second || 0,
      latitude: prof.latitude,
      longitude: prof.longitude,
      location: prof.location,
      ayanamsa: prof.ayanamsa || settings.ayanamsa || 'LAHIRI',
      panchangamSystem: 'DRIK_TIRUKANITHAM'
    };
    setFormPayload(payload);
    setActiveSubTab('charts'); // Always start on charts for loaded profile
    handleFormSubmit(payload);
  };

  const handleFormSubmit = async (payload) => {
    setLoading(true);
    setError(null);
    setFormPayload(payload);
    setPredictions(null);
    setDailyBalan(null);
    setActiveSubTab('charts'); // Always default to charts on calculate
    try {
      const response = await fetch('/api/v1/astrology/calculate?systemType=DRIK_TIRUKANITHAM', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Accept-Language': settings.language
        },
        body: JSON.stringify(payload)
      });
      if (response.ok) {
        const data = await response.json();
        setReport(data);
      } else {
        throw new Error('Failed to generate horoscope report.');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const getLifeStorageKey = (payload, lang) => {
    if (!payload) return null;
    return `drik_life_${payload.name}_${payload.year}_${payload.month}_${payload.day}_${payload.latitude}_${payload.longitude}_${lang}`;
  };

  const getDailyStorageKey = (payload, date, lang) => {
    if (!payload) return null;
    return `drik_daily_${payload.name}_${payload.year}_${payload.month}_${payload.day}_${date}_${lang}`;
  };

  const handleGeneratePredictions = async (forceRefresh = false) => {
    if (!report || !formPayload) return;
    const cacheKey = getLifeStorageKey(formPayload, settings.language);

    if (!forceRefresh && cacheKey) {
      try {
        const localCached = localStorage.getItem(cacheKey);
        if (localCached) {
          const parsed = JSON.parse(localCached);
          if (parsed && parsed.expiry && Date.now() < parsed.expiry) {
            setPredictions(parsed.data);
            return;
          }
        }
      } catch (e) {
        console.warn('localStorage read error:', e);
      }
    }

    setPredLoading(true);
    setPredError(null);
    try {
      const response = await fetch('/api/v1/astrology/predictions/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept-Language': settings.language
        },
        body: JSON.stringify({
          birthDetails: formPayload,
          chartData: report,
          language: settings.language,
          forceRefresh
        })
      });
      if (response.ok) {
        const data = await response.json();
        setPredictions(data);
        if (cacheKey && data.enabled) {
          try {
            const cacheObj = {
              expiry: Date.now() + 30 * 24 * 60 * 60 * 1000, // 30 days
              data
            };
            localStorage.setItem(cacheKey, JSON.stringify(cacheObj));
          } catch (e) {
            console.warn('localStorage write error:', e);
          }
        }
      } else {
        throw new Error('Failed to generate AI predictions.');
      }
    } catch (e) {
      setPredError(e.message);
    } finally {
      setPredLoading(false);
    }
  };

  const handleGenerateDailyBalan = async (targetDate, forceRefresh = false) => {
    if (!report || !formPayload) return;
    const dateStr = targetDate || new Date().toISOString().split('T')[0];
    const cacheKey = getDailyStorageKey(formPayload, dateStr, settings.language);

    if (!forceRefresh && cacheKey) {
      try {
        const localCached = localStorage.getItem(cacheKey);
        if (localCached) {
          const parsed = JSON.parse(localCached);
          if (parsed && parsed.expiry && Date.now() < parsed.expiry) {
            setDailyBalan(parsed.data);
            return;
          }
        }
      } catch (e) {
        console.warn('localStorage read error:', e);
      }
    }

    setDailyLoading(true);
    setDailyError(null);
    try {
      const response = await fetch('/api/v1/astrology/predictions/daily', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept-Language': settings.language
        },
        body: JSON.stringify({
          birthDetails: formPayload,
          chartData: report,
          targetDate: dateStr,
          language: settings.language,
          forceRefresh
        })
      });
      if (response.ok) {
        const data = await response.json();
        setDailyBalan(data);
        if (cacheKey && data.enabled) {
          try {
            const endOfDay = new Date(dateStr + 'T23:59:59').getTime();
            const cacheObj = {
              expiry: endOfDay,
              data
            };
            localStorage.setItem(cacheKey, JSON.stringify(cacheObj));
          } catch (e) {
            console.warn('localStorage write error:', e);
          }
        }
      } else {
        throw new Error('Failed to generate Daily Balan.');
      }
    } catch (e) {
      setDailyError(e.message);
    } finally {
      setDailyLoading(false);
    }
  };

  const handleDownloadPdf = async () => {
    if (!formPayload || pdfLoading) return;
    setPdfLoading(true);
    try {
      const response = await fetch('/api/v1/astrology/download-pdf?systemType=DRIK_TIRUKANITHAM', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Accept-Language': settings.language
        },
        body: JSON.stringify(formPayload)
      });
      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${formPayload.name.replace(/[^a-zA-Z0-9]/g, '')}_Horoscope.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      } else {
        alert('Failed to download PDF report.');
      }
    } catch (err) {
      console.error(err);
      alert('Error occurred while downloading PDF report.');
    } finally {
      setPdfLoading(false);
    }
  };

  const renderChartsTab = () => {
    if (!report) return null;
    const d1 = report.d1Chart || [];
    const d9 = report.d9Chart || d1;

    return (
      <div>
        <div className="grid-2">
          <div className="card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <h3 className="title-gold">{t('d1ChartTitle', settings.language)}</h3>
            <IndianChart positions={d1} style="south" title={t('d1ChartTitle', settings.language)} lang={settings.language} />
          </div>
          <div className="card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <h3 className="title-gold">{t('d9ChartTitle', settings.language)}</h3>
            <IndianChart positions={d9} style="south" title={t('d9ChartTitle', settings.language)} lang={settings.language} />
          </div>
        </div>

        <div className="card">
          <h3 className="title-gold">{t('planetaryPositions', settings.language)}</h3>
          <div className="horai-table-container">
            <table className="horai-table">
              <thead>
                <tr>
                  <th>{t('planet', settings.language)}</th>
                  <th>{t('rashi', settings.language)}</th>
                  <th>{t('degree', settings.language)}</th>
                </tr>
              </thead>
              <tbody>
                {d1.map((p, idx) => {
                  const planetKey = (p.planetKey || p.displayName || '').toLowerCase();
                  const localizedPlanet = t('planet.' + planetKey, settings.language) !== ('planet.' + planetKey)
                    ? t('planet.' + planetKey, settings.language)
                    : (p.displayName || p.planetKey);
                  return (
                    <tr key={idx}>
                      <td style={{ fontWeight: 'bold' }}>{localizedPlanet}</td>
                      <td>{p.rashiName}</td>
                      <td>{p.formattedDegree}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  };

  const formatDate = (val) => {
    if (!val) return '';
    if (typeof val === 'string') return val;
    if (Array.isArray(val)) {
      const [y, m, d] = val;
      return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    }
    if (typeof val === 'object') {
      if (val.year && val.monthValue && val.dayOfMonth) {
        return `${val.year}-${String(val.monthValue).padStart(2, '0')}-${String(val.dayOfMonth).padStart(2, '0')}`;
      }
    }
    return String(val);
  };

  const parseDateToComparable = (val) => {
    if (!val) return null;
    const str = formatDate(val);
    return str ? new Date(str + 'T00:00:00') : null;
  };

  const isPeriodActive = (startDateVal, endDateVal) => {
    const s = parseDateToComparable(startDateVal);
    const e = parseDateToComparable(endDateVal);
    if (!s || !e) return false;
    const now = new Date();
    // End date should include the full end day till 23:59:59
    const eEnd = new Date(e.getTime() + 86400000);
    return now >= s && now <= eEnd;
  };

  // Auto-expand active Mahadasa when report loads
  useEffect(() => {
    const timeline = report?.currentDasaTimeline || report?.vimshottariTimeline;
    if (timeline && timeline.length > 0) {
      const activeIdx = timeline.findIndex(d => isPeriodActive(d.startDate, d.endDate));
      if (activeIdx !== -1) {
        setExpandedDasa(activeIdx);
      }
    }
  }, [report]);

  const REQUIRED_SHADBALA_RUPAS = {
    SUN: 6.5,
    MOON: 6.0,
    MARS: 5.0,
    MERCURY: 7.0,
    JUPITER: 6.5,
    VENUS: 5.5,
    SATURN: 5.0
  };

  const renderDasaTab = () => {
    const timeline = report?.currentDasaTimeline || report?.vimshottariTimeline;
    if (!report || !timeline || timeline.length === 0) {
      return (
        <div className="card">
          <h3 className="title-gold">{t('dasaTab', settings.language)}</h3>
          <p style={{ color: 'var(--text-secondary)' }}>No Dasa-Bhukthi timeline data available.</p>
        </div>
      );
    }

    return (
      <div className="card">
        <h3 className="title-gold">{t('dasaTab', settings.language)}</h3>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '15px' }}>
          {language === 'ta' ? 'விம்சோத்தரி மகாதிசா மற்றும் அந்தர்திசா (புக்தி) கால அட்டவணை' : 'Vimshottari Mahadasa and Bhukthi Timeline'}
        </p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {timeline.map((dasa, dIdx) => {
            const isExpanded = expandedDasa === dIdx;
            const isActiveDasa = isPeriodActive(dasa.startDate, dasa.endDate);
            const planetKey = (dasa.planetKey || dasa.planetName || '').toLowerCase();
            const localizedPlanet = t('planet.' + planetKey, settings.language) !== ('planet.' + planetKey)
              ? t('planet.' + planetKey, settings.language)
              : (dasa.planetName || dasa.planetKey);

            return (
              <div 
                key={dIdx} 
                style={{ 
                  border: isActiveDasa ? '2px solid var(--accent-gold)' : '1px solid var(--border)', 
                  borderRadius: '8px', 
                  overflow: 'hidden',
                  background: isActiveDasa 
                    ? 'rgba(255, 215, 0, 0.08)' 
                    : (isExpanded ? 'rgba(255, 215, 0, 0.03)' : 'var(--bg-card)'),
                  boxShadow: isActiveDasa ? '0 0 10px rgba(255, 215, 0, 0.15)' : 'none'
                }}
              >
                <div 
                  onClick={() => setExpandedDasa(isExpanded ? null : dIdx)}
                  style={{ 
                    padding: '12px 16px', 
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    cursor: 'pointer',
                    userSelect: 'none',
                    flexWrap: 'wrap',
                    gap: '8px'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <span style={{ fontSize: '16px' }}>{isExpanded ? '▼' : '▶'}</span>
                    <strong style={{ fontSize: '15px', color: 'var(--accent-gold)' }}>
                      {localizedPlanet} {t('mahaDasa', settings.language)}
                    </strong>
                    {isActiveDasa && (
                      <span style={{ 
                        fontSize: '11px', 
                        background: 'var(--accent-gold)', 
                        color: '#000', 
                        padding: '2px 8px', 
                        borderRadius: '12px', 
                        fontWeight: 'bold',
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: '4px'
                      }}>
                        ✨ {t('currentActiveDasa', settings.language) || 'Current Dasa'}
                      </span>
                    )}
                  </div>
                  <div style={{ fontSize: '13px', color: isActiveDasa ? 'var(--accent-gold)' : 'var(--text-secondary)', fontWeight: isActiveDasa ? 'bold' : 'normal' }}>
                    {formatDate(dasa.startDate)} ➔ {formatDate(dasa.endDate)}
                  </div>
                </div>

                {isExpanded && dasa.bhukthis && dasa.bhukthis.length > 0 && (
                  <div style={{ padding: '8px 14px 14px 14px', borderTop: '1px solid var(--border)' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '8px', marginTop: '8px' }}>
                      {dasa.bhukthis.map((bhukthi, bIdx) => {
                        const isActiveBhukthi = isPeriodActive(bhukthi.startDate, bhukthi.endDate);
                        const bPlanetKey = (bhukthi.planetKey || bhukthi.planetName || '').toLowerCase();
                        const bLocalizedPlanet = t('planet.' + bPlanetKey, settings.language) !== ('planet.' + bPlanetKey)
                          ? t('planet.' + bPlanetKey, settings.language)
                          : (bhukthi.planetName || bhukthi.planetKey);

                        return (
                          <div 
                            key={bIdx}
                            style={{
                              display: 'flex',
                              justifyContent: 'space-between',
                              alignItems: 'center',
                              padding: '8px 12px',
                              borderRadius: '6px',
                              border: isActiveBhukthi ? '1px solid var(--accent-gold)' : '1px solid var(--border)',
                              background: isActiveBhukthi ? 'rgba(255, 215, 0, 0.14)' : 'rgba(255, 255, 255, 0.02)',
                              fontSize: '13px',
                              flexWrap: 'wrap',
                              gap: '6px'
                            }}
                          >
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                              <span style={{ fontWeight: 'bold', color: isActiveBhukthi ? 'var(--accent-gold)' : 'var(--text-primary)' }}>
                                {isActiveBhukthi ? '⭐ ' : ''}{bLocalizedPlanet}
                              </span>
                              {isActiveBhukthi && (
                                <span style={{ fontSize: '10px', background: 'var(--accent-gold)', color: '#000', padding: '1px 6px', borderRadius: '4px', fontWeight: 'bold' }}>
                                  {t('currentActiveDasa', settings.language) || 'Current'}
                                </span>
                              )}
                            </div>
                            <div style={{ fontSize: '12px', color: isActiveBhukthi ? 'var(--accent-gold)' : 'var(--text-secondary)', fontFamily: 'monospace' }}>
                              {formatDate(bhukthi.startDate)} ➔ {formatDate(bhukthi.endDate)}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  const renderShadbalaTab = () => {
    const shadbala = report?.shadbalaStrengths;
    if (!report || !shadbala || !shadbala.planetStrengths) {
      return (
        <div className="card">
          <h3 className="title-gold">{t('shadbalaTab', settings.language)}</h3>
          <p style={{ color: 'var(--text-secondary)' }}>No Shadbala strength data available.</p>
        </div>
      );
    }

    const planets = Object.keys(shadbala.planetStrengths);

    return (
      <div className="card">
        <h3 className="title-gold">{t('shadbalaTab', settings.language)}</h3>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '15px' }}>
          {language === 'ta' ? '6 விதமான கிரக பலங்களின் (ஸ்தான, திக், கால, சேஷ்டா, திருக், நைசர்கிக) மதிப்பீடு' : 'Six-fold Planetary Strength Assessment (in Rupas)'}
        </p>

        <div className="horai-table-container">
          <table className="horai-table">
            <thead>
              <tr>
                <th>{t('planet', settings.language)}</th>
                <th>{t('sthana', settings.language)}</th>
                <th>{t('dig', settings.language)}</th>
                <th>{t('kala', settings.language)}</th>
                <th>{t('cheshta', settings.language)}</th>
                <th>{t('total', settings.language)}</th>
                <th>{t('status', settings.language)}</th>
              </tr>
            </thead>
            <tbody>
              {planets.map((planetKey, idx) => {
                const strength = shadbala.planetStrengths[planetKey];
                const localizedPlanet = t('planet.' + planetKey.toLowerCase(), settings.language) !== ('planet.' + planetKey.toLowerCase())
                  ? t('planet.' + planetKey.toLowerCase(), settings.language)
                  : planetKey;
                const reqRupas = REQUIRED_SHADBALA_RUPAS[planetKey.toUpperCase()] || 6.0;
                const totalRupas = strength.totalShadbalaRupas || 0;
                const pct = Math.round((totalRupas / reqRupas) * 100);
                const isStrong = totalRupas >= reqRupas;

                return (
                  <tr key={idx}>
                    <td style={{ fontWeight: 'bold' }}>{localizedPlanet}</td>
                    <td>{strength.sthanaBala?.toFixed(1)}</td>
                    <td>{strength.digBala?.toFixed(1)}</td>
                    <td>{strength.kalaBala?.toFixed(1)}</td>
                    <td>{strength.cheshtaBala?.toFixed(1)}</td>
                    <td style={{ fontWeight: 'bold', color: isStrong ? '#2ecc71' : '#e67e22' }}>
                      {totalRupas.toFixed(2)} R ({pct}%)
                    </td>
                    <td>
                      <span style={{ 
                        color: isStrong ? '#27ae60' : '#e67e22',
                        background: isStrong ? 'rgba(39, 174, 96, 0.1)' : 'rgba(230, 126, 34, 0.1)',
                        padding: '2px 8px',
                        borderRadius: '10px',
                        fontWeight: 'bold',
                        fontSize: '12px'
                      }}>
                        {strength.strengthCategory || (isStrong ? 'STRONG' : 'MODERATE')}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  const renderDiagnosticsTab = () => {
    const diag = report?.structuralDiagnostics;
    if (!report || !diag) {
      return (
        <div className="card">
          <h3 className="title-gold">{t('diagnosticsTab', settings.language)}</h3>
          <p style={{ color: 'var(--text-secondary)' }}>No structural diagnostics available.</p>
        </div>
      );
    }

    const yogas = diag.activeYogas || [];
    const doshams = diag.discoveredDoshams || [];

    return (
      <div>
        {/* Yogas */}
        <div className="card">
          <h3 className="title-gold">{t('yogasDetected', settings.language)} ({yogas.length})</h3>
          {yogas.length === 0 ? (
            <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>{t('noYogasDetected', settings.language)}</p>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '12px' }}>
              {yogas.map((y, idx) => (
                <div key={idx} style={{ background: 'rgba(255, 215, 0, 0.04)', border: '1px solid rgba(255, 215, 0, 0.25)', borderRadius: '8px', padding: '12px' }}>
                  <h4 style={{ margin: '0 0 6px', fontSize: '14px', color: 'var(--accent-gold)' }}>
                    👑 {y.name}
                  </h4>
                  <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                    {y.description}
                  </p>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Doshams */}
        <div className="card">
          <h3 className="title-gold">{t('doshamsEvaluated', settings.language)} ({doshams.length})</h3>
          {doshams.length === 0 ? (
            <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>{t('noDoshasDetected', settings.language)}</p>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '12px' }}>
              {doshams.map((d, idx) => {
                const isNotDetected = !d.detected;
                const isNullified = d.detected && d.nullified;
                const isActive = d.detected && !d.nullified;

                const borderColor = isNotDetected 
                  ? 'rgba(39, 174, 96, 0.25)' 
                  : (isNullified ? 'rgba(39, 174, 96, 0.5)' : 'rgba(231, 76, 60, 0.5)');
                const bgColor = isNotDetected 
                  ? 'rgba(39, 174, 96, 0.02)' 
                  : (isNullified ? 'rgba(39, 174, 96, 0.06)' : 'rgba(231, 76, 60, 0.06)');
                const titleColor = isNotDetected 
                  ? '#27ae60' 
                  : (isNullified ? '#2ecc71' : '#e74c3c');
                const badgeText = isNotDetected 
                  ? `✓ ${t('noDosham', settings.language) || 'No Affliction'}` 
                  : (isNullified ? `🛡️ ${t('cancelled', settings.language) || 'Cancelled'}` : `⚠️ ${t('active', settings.language) || 'Active'}`);
                const badgeBg = isNotDetected 
                  ? 'rgba(39, 174, 96, 0.15)' 
                  : (isNullified ? '#27ae60' : '#e74c3c');
                const badgeColor = isNotDetected ? '#2ecc71' : '#fff';

                return (
                  <div 
                    key={idx} 
                    style={{ 
                      background: bgColor,
                      border: `1px solid ${borderColor}`,
                      borderRadius: '8px', 
                      padding: '14px' 
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                      <h4 style={{ margin: 0, fontSize: '14px', color: titleColor }}>
                        {isNotDetected ? '✓ ' : (isNullified ? '🛡️ ' : '⚠️ ')}{d.name}
                      </h4>
                      <span style={{ 
                        background: badgeBg, 
                        color: badgeColor, 
                        fontSize: '11px', 
                        padding: '2px 8px', 
                        borderRadius: '12px',
                        fontWeight: 'bold'
                      }}>
                        {badgeText}
                      </span>
                    </div>
                    <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-secondary)', lineHeight: '1.4' }}>
                      {isNullified 
                        ? (d.nullificationReason || d.reason || t('noDoshasDetected', settings.language))
                        : (isActive ? (d.remedySuggestion || d.reason || d.description) : (d.description || t('noDoshasDetected', settings.language)))}
                    </p>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    );
  };

  const language = settings.language;

  return (
    <div>
      {!report && !loading && (
        <>
          {/* Saved Profiles Section */}
          {savedProfiles.length > 0 && (
            <div className="card" style={{ marginBottom: '20px' }}>
              <h3 style={{ margin: '0 0 12px', fontSize: '16px', color: 'var(--accent-gold)' }}>
                📁 {t('savedProfiles', settings.language) || 'Saved Horoscope Profiles'} ({savedProfiles.length})
              </h3>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
                {savedProfiles.map((prof) => (
                  <div
                    key={prof.id}
                    onClick={() => handleLoadSavedProfile(prof)}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px',
                      background: 'rgba(255, 215, 0, 0.06)',
                      border: '1px solid var(--border)',
                      borderRadius: '6px',
                      padding: '8px 12px',
                      cursor: 'pointer',
                      fontSize: '13px'
                    }}
                  >
                    <span>👤 <strong>{prof.name}</strong> ({prof.day}/{prof.month}/{prof.year})</span>
                    <button
                      onClick={(e) => handleDeleteProfile(e, prof.id)}
                      style={{ background: 'none', border: 'none', color: '#e74c3c', cursor: 'pointer', fontSize: '12px', padding: 0 }}
                      title="Delete"
                    >
                      ✕
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          <BirthForm
            onSubmit={handleFormSubmit}
            initialValues={formPayload ? {
              name: formPayload.name,
              date: `${formPayload.year}-${String(formPayload.month).padStart(2, '0')}-${String(formPayload.day).padStart(2, '0')}`,
              time: `${String(formPayload.hour).padStart(2, '0')}:${String(formPayload.minute).padStart(2, '0')}`,
              location: formPayload.location || settings.location,
              ayanamsa: formPayload.ayanamsa || settings.ayanamsa,
              panchangamSystem: formPayload.panchangamSystem || settings.panchangamSystem
            } : {
              location: settings.location,
              ayanamsa: settings.ayanamsa,
              panchangamSystem: settings.panchangamSystem
            }}
            submitLabel="calculateHoroscope"
            lang={settings.language}
          />
        </>
      )}

      {loading && (
        <div className="spinner-container">
          <div className="spinner"></div>
          <p>{t('calculatingHoroscope', settings.language)}</p>
        </div>
      )}

      {error && (
        <div className="card" style={{ borderLeft: '4px solid var(--danger)' }}>
          <p style={{ color: 'var(--danger)', fontWeight: 'bold' }}>{t('errorLoadingPanchangam', settings.language)}</p>
          <p>{error}</p>
          <button onClick={handleResetToNewChart} className="btn-primary" style={{ marginTop: '10px' }}>{t('retry', settings.language)}</button>
        </div>
      )}

      {!loading && report && (
        <div>
          {/* Header Card */}
          <div className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '15px' }}>
            <div>
              <h3 style={{ margin: '0 0 5px', color: 'var(--accent-gold)' }}>{report.name}</h3>
              <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
                {t('born', settings.language)}: {report.dateOfBirth} at {report.timeOfBirth} ({t('localMeanTime', settings.language)}: {report.localMeanTime})
              </p>
              <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                {t('lagna', settings.language)}: {report.birthProfile?.lagna} | {t('rashi', settings.language)}: {report.birthProfile?.rashi || report.birthProfile?.rasi} | {t('star', settings.language)}: {report.birthProfile?.nakshatra} ({t('pada', settings.language)}: {report.birthProfile?.nakshatraPada}) | {t('ayanamsa', settings.language)}: {getAyanamsaLabel(report.ayanamsa || formPayload?.ayanamsa, settings.language)} | {t('panchangamSystem', settings.language)}: {getPanchangamSystemLabel(report.panchangamSystem || formPayload?.panchangamSystem, settings.language)}
              </p>
              {saveSuccessMsg && (
                <p style={{ fontSize: '12px', color: '#27ae60', fontWeight: 'bold', margin: '4px 0 0 0' }}>
                  ✓ {saveSuccessMsg}
                </p>
              )}
            </div>
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              <button
                onClick={handleSaveCurrentProfile}
                className="btn-primary"
                style={{
                  background: isProfileAlreadySaved(formPayload) ? 'rgba(39, 174, 96, 0.2)' : '#27ae60',
                  borderColor: '#27ae60',
                  color: isProfileAlreadySaved(formPayload) ? '#2ecc71' : '#fff'
                }}
              >
                {isProfileAlreadySaved(formPayload)
                  ? `✓ ${t('saved', settings.language) || 'Saved'}`
                  : `💾 ${t('saveProfile', settings.language) || 'Save Profile'}`}
              </button>
              <button 
                onClick={handleDownloadPdf} 
                disabled={pdfLoading}
                className="btn-primary"
                style={{ opacity: pdfLoading ? 0.75 : 1, cursor: pdfLoading ? 'wait' : 'pointer' }}
              >
                {pdfLoading ? `⏳ ${t('generatingPdf', settings.language)}` : `📥 ${t('downloadPdf', settings.language)}`}
              </button>
              <button onClick={handleResetToNewChart} className="btn-primary" style={{ background: 'none', border: '1px solid var(--border)', color: 'var(--text-primary)' }}>
                {t('newChart', settings.language)}
              </button>
            </div>
          </div>

          {/* Sub Navigation with smooth mobile horizontal scroll */}
          <div 
            className="tabs-header" 
            style={{ 
              display: 'flex', 
              overflowX: 'auto', 
              whiteSpace: 'nowrap', 
              scrollSnapType: 'x mandatory', 
              WebkitOverflowScrolling: 'touch', 
              gap: '8px', 
              paddingBottom: '6px',
              scrollbarWidth: 'thin'
            }}
          >
            <button 
              className={`tab-btn ${activeSubTab === 'charts' ? 'active' : ''}`}
              onClick={() => setActiveSubTab('charts')}
            >
              {t('chartsTab', settings.language)}
            </button>
            {dailyEnabled && (
              <button 
                className={`tab-btn ${activeSubTab === 'daily' ? 'active' : ''}`}
                onClick={() => setActiveSubTab('daily')}
              >
                {t('dailyBalanTab', settings.language)}
              </button>
            )}
            <button 
              className={`tab-btn ${activeSubTab === 'dasa' ? 'active' : ''}`}
              onClick={() => setActiveSubTab('dasa')}
            >
              {t('dasaTab', settings.language)}
            </button>
            <button 
              className={`tab-btn ${activeSubTab === 'shadbala' ? 'active' : ''}`}
              onClick={() => setActiveSubTab('shadbala')}
            >
              {t('shadbalaTab', settings.language)}
            </button>
            <button 
              className={`tab-btn ${activeSubTab === 'diagnostics' ? 'active' : ''}`}
              onClick={() => setActiveSubTab('diagnostics')}
            >
              {t('diagnosticsTab', settings.language)}
            </button>
            {lifeEnabled && (
              <button 
                className={`tab-btn ${activeSubTab === 'predictions' ? 'active' : ''}`}
                onClick={() => setActiveSubTab('predictions')}
              >
                {t('aiBalanTab', settings.language)}
              </button>
            )}
          </div>

          {/* Sub Tab contents */}
          {activeSubTab === 'charts' && renderChartsTab()}
          {activeSubTab === 'dasa' && renderDasaTab()}
          {activeSubTab === 'shadbala' && renderShadbalaTab()}
          {activeSubTab === 'diagnostics' && renderDiagnosticsTab()}
          {activeSubTab === 'predictions' && (
            <AiPredictionsView
              report={report}
              formPayload={formPayload}
              language={settings.language}
              onGenerate={handleGeneratePredictions}
              predictions={predictions}
              loading={predLoading}
              error={predError}
            />
          )}
          {activeSubTab === 'daily' && (
            <DailyBalanView
              report={report}
              formPayload={formPayload}
              language={settings.language}
              onGenerateDaily={handleGenerateDailyBalan}
              dailyBalan={dailyBalan}
              loading={dailyLoading}
              error={dailyError}
            />
          )}
        </div>
      )}
    </div>
  );
}

export default HoroscopePage;
