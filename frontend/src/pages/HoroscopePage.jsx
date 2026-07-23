import { useState, useEffect } from 'react';
import BirthForm from '../components/BirthForm';
import IndianChart from '../components/IndianChart';
import { t } from '../i18n/translations';

function HoroscopePage({ settings }) {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [activeSubTab, setActiveSubTab] = useState('charts');
  const [expandedDasa, setExpandedDasa] = useState(null);
  const [formPayload, setFormPayload] = useState(null);

  useEffect(() => {
    setReport(null);
  }, [settings.language]);

  const handleFormSubmit = async (payload) => {
    setLoading(true);
    setError(null);
    setFormPayload(payload);
    try {
      const response = await fetch('/api/v1/astrology/calculate', {
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

  const handleDownloadPdf = async () => {
    if (!formPayload) return;
    try {
      const response = await fetch(`/api/v1/astrology/download-pdf?systemType=DRIK_TIRUKANITHAM`, {
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

    const today = new Date().toISOString().split('T')[0];

    return (
      <div className="card">
        <h3 className="title-gold">{t('dasaTab', settings.language)}</h3>
        <div className="dasa-accordion">
          {timeline.map((dasa, idx) => {
            const startStr = formatDate(dasa.startDate);
            const endStr = formatDate(dasa.endDate);
            const isCurrent = today >= startStr && today <= endStr;
            const isExpanded = expandedDasa === idx || (expandedDasa === null && (isCurrent || idx === 0));

            return (
              <div key={idx} className="dasa-item" style={isCurrent ? { borderLeft: '4px solid var(--accent-gold)' } : {}}>
                <div 
                  className={`dasa-header ${isExpanded ? 'active' : ''}`}
                  onClick={() => setExpandedDasa(isExpanded ? -1 : idx)}
                >
                  <span style={{ fontWeight: 'bold', color: isCurrent ? 'var(--accent-warm)' : 'var(--accent-gold)' }}>
                    ☀️ {t('planet.' + (dasa.planetName || '').toLowerCase(), settings.language)} {t('mahaDasa', settings.language)} {isCurrent ? `(${t('active', settings.language)})` : ''}
                  </span>
                  <span>
                    {startStr} to {endStr}
                  </span>
                </div>
                {isExpanded && dasa.bhukthis && dasa.bhukthis.length > 0 && (
                  <div className="dasa-body" style={{ padding: '15px 0' }}>
                    <div className="bhukthi-grid">
                      {dasa.bhukthis.map((bhukthi, bidx) => {
                        const bStartStr = formatDate(bhukthi.startDate);
                        const bEndStr = formatDate(bhukthi.endDate);
                        const isBhukthiCurrent = today >= bStartStr && today <= bEndStr;
                        return (
                          <div 
                            key={bidx} 
                            className="bhukthi-card" 
                            style={{
                              padding: '10px 12px',
                              borderRadius: '8px',
                              border: isBhukthiCurrent ? '2px solid var(--accent-gold)' : '1px solid var(--border)',
                              backgroundColor: isBhukthiCurrent ? 'var(--bg-card-hover)' : 'var(--bg-card)'
                            }}
                          >
                            <div style={{ fontWeight: 'bold', color: isBhukthiCurrent ? 'var(--accent-gold)' : 'var(--accent-warm)' }}>
                              {t('planet.' + (bhukthi.planetName || '').toLowerCase(), settings.language)} {t('bhukthi', settings.language)} {isBhukthiCurrent ? `(${t('active', settings.language)})` : ''}
                            </div>
                            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px' }}>
                              {bStartStr} to {bEndStr}
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
    if (!report || !report.shadbalaStrengths?.planetStrengths) return null;
    return (
      <div className="card">
        <h3 className="title-gold">{t('shadbalaTab', settings.language)}</h3>
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
              {Object.entries(report.shadbalaStrengths.planetStrengths).map(([planet, strength], idx) => {
                const isStrong = strength.strengthCategory?.toLowerCase().includes('strong');
                const isWeak = strength.strengthCategory?.toLowerCase().includes('weak');
                return (
                  <tr key={idx}>
                    <td style={{ fontWeight: 'bold' }}>{t('planet.' + planet.toLowerCase(), settings.language)}</td>
                    <td>{strength.sthanaBala.toFixed(2)}</td>
                    <td>{strength.digBala.toFixed(2)}</td>
                    <td>{strength.kalaBala.toFixed(2)}</td>
                    <td>{strength.cheshtaBala.toFixed(2)}</td>
                    <td style={{ fontWeight: 'bold', color: 'var(--accent-gold)' }}>
                      {strength.totalShadbalaRupas.toFixed(2)}
                    </td>
                    <td style={{
                      color: isStrong ? 'var(--success)' : isWeak ? 'var(--danger)' : 'var(--accent-gold)',
                      fontWeight: 'bold'
                    }}>
                      {strength.strengthCategory?.toLowerCase().includes('strong') ? t('veryStrong', settings.language) : strength.strengthCategory?.toLowerCase().includes('weak') ? t('weak', settings.language) : t('optimum', settings.language)}
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
    if (!report || !report.structuralDiagnostics) return null;
    const diag = report.structuralDiagnostics;
    return (
      <div>
        <div className="card">
          <h3 className="title-gold">{t('yogasDetected', settings.language)}</h3>
          <div className="grid-2">
            {diag.activeYogas?.map((yoga, idx) => (
              <div key={idx} className="card" style={{ borderLeft: '4px solid var(--accent-gold)', margin: 0 }}>
                <h4 style={{ margin: '0 0 5px', color: 'var(--accent-gold)' }}>{yoga.name}</h4>
                <p style={{ fontSize: '14px' }}>{yoga.description}</p>
                <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '8px' }}>
                  {t('impact', settings.language)}: {yoga.impactLevel}
                </p>
              </div>
            ))}
            {(!diag.activeYogas || diag.activeYogas.length === 0) && (
              <p>{t('noYogasDetected', settings.language)}</p>
            )}
          </div>
        </div>

        <div className="card">
          <h3 className="title-gold">{t('doshamsEvaluated', settings.language)}</h3>
          <div className="dosha-grid">
            {diag.discoveredDoshams?.map((dosha, idx) => {
              const active = dosha.active;
              const nullified = dosha.nullified;
              let badgeClass = 'none';
              let badgeText = dosha.severity || 'None';
              if (active) {
                badgeClass = 'active';
              } else if (nullified) {
                badgeClass = 'cancelled';
              }

              return (
                <div key={idx} className="dosha-card">
                  <span className={`dosha-badge ${badgeClass}`}>{badgeText}</span>
                  <h4 style={{ margin: '0 0 10px', width: '70%', color: 'var(--text-primary)' }}>{dosha.name}</h4>
                  
                  {nullified && (
                    <p style={{ fontSize: '13px', color: 'var(--success)', marginTop: '5px' }}>
                      <strong>{t('cancelled', settings.language)}:</strong> {dosha.nullificationReason}
                    </p>
                  )}
                  {active && (
                    <p style={{ fontSize: '13px', color: 'var(--accent-warm)', marginTop: '5px' }}>
                      <strong>{t('remedy', settings.language)}:</strong> {dosha.remedySuggestion}
                    </p>
                  )}
                  {!active && !nullified && (
                    <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>{t('noDoshasDetected', settings.language)}</p>
                  )}
                </div>
              );
            })}
          </div>
        </div>

        {diag.horoscopicSpecialities && diag.horoscopicSpecialities.length > 0 && (
          <div className="card">
            <h3 className="title-gold">{t('specialFeatures', settings.language)}</h3>
            <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '14px', color: 'var(--text-primary)' }}>
              {diag.horoscopicSpecialities.map((s, idx) => (
                <li key={idx} style={{ marginBottom: '6px' }}>{s}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    );
  };

  return (
    <div>
      <h2 className="title-gold">{t('horoscope', settings.language)}</h2>
      
      {!report && !loading && (
        <BirthForm
          onSubmit={handleFormSubmit}
          initialValues={{
            location: settings.location,
            ayanamsa: settings.ayanamsa
          }}
          submitLabel="calculateHoroscope"
          lang={settings.language}
        />
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
          <button onClick={() => setReport(null)} className="btn-primary" style={{ marginTop: '10px' }}>{t('retry', settings.language)}</button>
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
                {t('lagna', settings.language)}: {report.birthProfile?.lagna} | {t('rashi', settings.language)}: {report.birthProfile?.rashi || report.birthProfile?.rasi} | {t('star', settings.language)}: {report.birthProfile?.nakshatra} ({t('pada', settings.language)}: {report.birthProfile?.nakshatraPada}) | {t('ayanamsa', settings.language)}: {t('ayanamsa.' + (report.ayanamsa || settings.ayanamsa).toUpperCase(), settings.language) !== ('ayanamsa.' + (report.ayanamsa || settings.ayanamsa).toUpperCase()) ? t('ayanamsa.' + (report.ayanamsa || settings.ayanamsa).toUpperCase(), settings.language) : (report.ayanamsa || settings.ayanamsa)}
              </p>
            </div>
            <div>
              <button onClick={handleDownloadPdf} className="btn-primary">
                📥 {t('downloadPdf', settings.language)}
              </button>
              <button onClick={() => setReport(null)} className="btn-primary" style={{ marginLeft: '10px', background: 'none', border: '1px solid var(--border)', color: 'var(--text-primary)' }}>
                {t('newChart', settings.language)}
              </button>
            </div>
          </div>

          {/* Sub Navigation */}
          <div className="tabs-header">
            <button 
              className={`tab-btn ${activeSubTab === 'charts' ? 'active' : ''}`}
              onClick={() => setActiveSubTab('charts')}
            >
              {t('chartsTab', settings.language)}
            </button>
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
          </div>

          {/* Sub Tab contents */}
          {activeSubTab === 'charts' && renderChartsTab()}
          {activeSubTab === 'dasa' && renderDasaTab()}
          {activeSubTab === 'shadbala' && renderShadbalaTab()}
          {activeSubTab === 'diagnostics' && renderDiagnosticsTab()}
        </div>
      )}
    </div>
  );
}

export default HoroscopePage;
