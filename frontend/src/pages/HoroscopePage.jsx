import { useState } from 'react';
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

  const handleFormSubmit = async (payload) => {
    setLoading(true);
    setError(null);
    setFormPayload(payload);
    try {
      const response = await fetch('/api/v1/astrology/comprehensive', {
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
    const d1 = report.birthPlanetaryPositions || [];
    // If vargaChartsSuite has D9, usually index 1 or we can map d9 if available, or just map D1
    const d9 = report.vargaChartsSuite?.[1] || d1;

    return (
      <div>
        <div className="grid-2">
          <div className="card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <h3 className="title-gold">D1 Rasi Chart</h3>
            <IndianChart positions={d1} style="south" title="D1 Rasi" lang={settings.language} />
          </div>
          <div className="card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <h3 className="title-gold">D9 Navamsa Chart</h3>
            <IndianChart positions={d9} style="south" title="D9 Navamsa" lang={settings.language} />
          </div>
        </div>

        <div className="card">
          <h3 className="title-gold">Planetary Positions</h3>
          <div className="horai-table-container">
            <table className="horai-table">
              <thead>
                <tr>
                  <th>Planet</th>
                  <th>Sign</th>
                  <th>Degree</th>
                </tr>
              </thead>
              <tbody>
                {d1.map((p, idx) => (
                  <tr key={idx}>
                    <td style={{ fontWeight: 'bold' }}>{p.displayName || p.planetKey}</td>
                    <td>{p.rashiName}</td>
                    <td>{p.formattedDegree}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  };

  const renderDasaTab = () => {
    if (!report || !report.vimshottariTimeline) return null;
    return (
      <div className="card">
        <h3 className="title-gold">Vimshottari Dasa Periods</h3>
        <div className="dasa-accordion">
          {report.vimshottariTimeline.map((dasa, idx) => {
            const isExpanded = expandedDasa === idx;
            const today = new Date().toISOString().split('T')[0];
            const isCurrent = today >= dasa.startDate && today <= dasa.endDate;
            return (
              <div key={idx} className="dasa-item" style={isCurrent ? { borderLeft: '4px solid var(--accent-gold)' } : {}}>
                <div 
                  className={`dasa-header ${isExpanded ? 'active' : ''}`}
                  onClick={() => setExpandedDasa(isExpanded ? null : idx)}
                >
                  <span style={{ fontWeight: 'bold', color: isCurrent ? 'var(--accent-warm)' : 'var(--accent-gold)' }}>
                    ☀️ {dasa.planetName} Maha Dasa {isCurrent ? '(Active)' : ''}
                  </span>
                  <span>
                    {dasa.startDate} to {dasa.endDate}
                  </span>
                </div>
                {isExpanded && dasa.bhukthis && (
                  <div className="dasa-body">
                    <div className="bhukthi-grid">
                      {dasa.bhukthis.map((bhukthi, bidx) => {
                        const isBhukthiCurrent = today >= bhukthi.startDate && today <= bhukthi.endDate;
                        return (
                          <div 
                            key={bidx} 
                            className="bhukthi-card" 
                            style={isBhukthiCurrent ? { borderColor: 'var(--accent-gold)', backgroundColor: 'var(--bg-card-hover)' } : {}}
                          >
                            <div style={{ fontWeight: 'bold', color: isBhukthiCurrent ? 'var(--accent-gold)' : 'var(--accent-warm)' }}>
                              {bhukthi.planetName} Bhukthi {isBhukthiCurrent ? '(Active)' : ''}
                            </div>
                            <div>{bhukthi.startDate} to {bhukthi.endDate}</div>
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
                <th>Sthana</th>
                <th>Dig</th>
                <th>Kala</th>
                <th>Cheshta</th>
                <th>Total</th>
                <th>{t('status', settings.language)}</th>
              </tr>
            </thead>
            <tbody>
              {Object.entries(report.shadbalaStrengths.planetStrengths).map(([planet, strength], idx) => {
                const isStrong = strength.strengthCategory?.toLowerCase().includes('strong');
                const isWeak = strength.strengthCategory?.toLowerCase().includes('weak');
                return (
                  <tr key={idx}>
                    <td style={{ fontWeight: 'bold' }}>{planet}</td>
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
                      {strength.strengthCategory}
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
                  Impact: {yoga.impactLevel}
                </p>
              </div>
            ))}
            {(!diag.activeYogas || diag.activeYogas.length === 0) && (
              <p>No major planetary Yogas detected in this horoscope configuration.</p>
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
              let badgeText = 'None';
              if (active) {
                badgeClass = 'active';
                badgeText = dosha.severity || t('active', settings.language);
              } else if (nullified) {
                badgeClass = 'cancelled';
                badgeText = t('cancelled', settings.language);
              }

              return (
                <div key={idx} className="dosha-card">
                  <span className={`dosha-badge ${badgeClass}`}>{badgeText}</span>
                  <h4 style={{ margin: '0 0 10px', width: '70%', color: 'var(--text-primary)' }}>{dosha.name}</h4>
                  
                  {nullified && (
                    <p style={{ fontSize: '13px', color: 'var(--success)', marginTop: '5px' }}>
                      <strong>Nullification:</strong> {dosha.nullificationReason}
                    </p>
                  )}
                  {active && (
                    <p style={{ fontSize: '13px', color: 'var(--accent-warm)', marginTop: '5px' }}>
                      <strong>Remedy:</strong> {dosha.remedySuggestion}
                    </p>
                  )}
                  {!active && !nullified && (
                    <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>No affliction detected.</p>
                  )}
                </div>
              );
            })}
          </div>
        </div>

        {diag.horoscopicSpecialities && diag.horoscopicSpecialities.length > 0 && (
          <div className="card">
            <h3 className="title-gold">Special Features</h3>
            <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '14px' }}>
              {diag.horoscopicSpecialities.map((s, idx) => (
                <li key={idx} style={{ marginBottom: '5px' }}>{s}</li>
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
          <p>Running comprehensive horoscope calculation engine...</p>
        </div>
      )}

      {error && (
        <div className="card" style={{ borderLeft: '4px solid var(--danger)' }}>
          <p style={{ color: 'var(--danger)', fontWeight: 'bold' }}>Calculation Faulted</p>
          <p>{error}</p>
          <button onClick={() => setReport(null)} className="btn-primary" style={{ marginTop: '10px' }}>Try Again</button>
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
                {t('lagna', settings.language)}: {report.birthProfile?.lagna} | {t('rashi', settings.language)}: {report.birthProfile?.rasi} | {t('star', settings.language)}: {report.birthProfile?.nakshatra} ({t('pada', settings.language)}: {report.birthProfile?.nakshatraPada})
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
