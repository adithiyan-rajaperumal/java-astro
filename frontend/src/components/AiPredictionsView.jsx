import { useState, useEffect } from 'react';
import { t } from '../i18n/translations';

function AiPredictionsView({
  report,
  formPayload,
  language = 'en',
  onGenerate,
  predictions,
  loading,
  error
}) {
  const [activeFilter, setActiveFilter] = useState('ALL');
  const [verifiedMap, setVerifiedMap] = useState({});

  const toggleVerified = (idx) => {
    setVerifiedMap((prev) => ({
      ...prev,
      [idx]: !prev[idx]
    }));
  };

  if (!predictions && !loading) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '40px 20px' }}>
        <div style={{ fontSize: '48px', marginBottom: '15px' }}>🔮</div>
        <h3 style={{ color: 'var(--accent-gold)', marginBottom: '10px' }}>
          {t('aiBalanTab', language)}
        </h3>
        <p style={{ maxWidth: '650px', margin: '0 auto 25px', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
          {t('pastVerificationDesc', language)}
        </p>
        <button
          onClick={() => onGenerate(false)}
          className="btn-primary"
          style={{ padding: '12px 28px', fontSize: '16px', display: 'inline-flex', alignItems: 'center', gap: '8px' }}
        >
          ✨ {t('generateAiBalan', language)}
        </button>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '50px 20px' }}>
        <div className="spinner" style={{ margin: '0 auto 20px' }}></div>
        <h4 style={{ color: 'var(--accent-gold)' }}>{t('generatingAiBalan', language)}</h4>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
          {t('horoscopeCalculating', language)}
        </p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="card" style={{ borderLeft: '4px solid var(--danger)', background: 'rgba(231, 76, 60, 0.08)' }}>
        <h4 style={{ color: 'var(--danger)', margin: '0 0 8px' }}>
          ⚠️ {t('calculationFaulted', language)}
        </h4>
        <p style={{ color: 'var(--text-secondary)', margin: '0 0 15px', fontSize: '14px' }}>{error}</p>
        <button onClick={() => onGenerate(true)} className="btn-primary">
          🔄 {t('retry', language)}
        </button>
      </div>
    );
  }

  if (predictions && predictions.enabled === false) {
    return (
      <div className="card" style={{ borderLeft: '4px solid #e74c3c', background: 'rgba(231, 76, 60, 0.08)' }}>
        <h4 style={{ color: '#e74c3c', margin: '0 0 8px' }}>
          ⚠️ {t('calculationFaulted', language)}
        </h4>
        <p style={{ color: 'var(--text-secondary)', margin: '0 0 15px', fontSize: '14px' }}>
          {predictions.message || t('aiPredictionUnavailable', language)}
        </p>
        <button onClick={() => onGenerate(true)} className="btn-primary" style={{ padding: '8px 20px' }}>
          🔄 {t('retry', language)}
        </button>
      </div>
    );
  }

  const personality = predictions?.nativePersonality;
  const health = predictions?.healthAnalysis;
  const aiYogas = predictions?.aiYogas || [];
  const aiDoshams = predictions?.aiDoshams || [];
  const pastMilestones = predictions?.pastMilestones || [];
  const lifetimeList = predictions?.lifetimePredictions || predictions?.futurePredictions || [];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Token Usage & Cache Notice Badge */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '12px',
        background: 'rgba(255, 215, 0, 0.05)',
        border: '1px solid rgba(255, 215, 0, 0.25)',
        borderRadius: '8px',
        padding: '10px 16px',
        fontSize: '13px',
        color: 'var(--text-secondary)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
          <span>💾 <strong style={{ color: 'var(--accent-gold)' }}>{t('cachedNotice30Days', language)}</strong></span>
          {predictions?.tokenUsage && (
            <>
              <span>⚡ <strong>{predictions.tokenUsage.totalTokens?.toLocaleString()}</strong> {t('tokensCount', language)}</span>
              <span>🤖 <code style={{ color: 'var(--text-primary)' }}>{predictions.tokenUsage.modelUsed}</code></span>
            </>
          )}
        </div>
        <button
          onClick={() => onGenerate(true)}
          className="btn-primary"
          style={{
            padding: '5px 12px',
            fontSize: '12px',
            background: 'none',
            border: '1px solid var(--border)',
            color: 'var(--text-primary)'
          }}
        >
          🔄 {t('regenerateAiBalan', language)}
        </button>
      </div>

      {/* Overall Astrological Summary Card */}
      {predictions?.overallSummary && (
        <div className="card" style={{
          background: 'linear-gradient(135deg, rgba(255,215,0,0.08), rgba(20,20,30,0.7))',
          border: '1px solid var(--accent-gold)'
        }}>
          <h3 style={{ margin: '0 0 10px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            🌟 {t('overallSummaryTitle', language)}
          </h3>
          <p style={{ lineHeight: '1.7', fontSize: '14px', margin: 0, color: 'var(--text-primary)' }}>
            {predictions.overallSummary}
          </p>
        </div>
      )}

      {/* Native Personality Card */}
      {personality && (
        <div className="card">
          <h3 style={{ margin: '0 0 12px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            🧘 {t('aiPersonalityTitle', language)}
          </h3>
          {personality.coreTemperament && (
            <p style={{ fontSize: '14px', lineHeight: '1.6', color: 'var(--text-primary)', marginBottom: '14px' }}>
              {personality.coreTemperament}
            </p>
          )}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '12px' }}>
            {personality.keyStrengths && personality.keyStrengths.length > 0 && (
              <div style={{ background: 'rgba(39, 174, 96, 0.06)', border: '1px solid rgba(39, 174, 96, 0.3)', borderRadius: '8px', padding: '12px' }}>
                <h4 style={{ margin: '0 0 8px', fontSize: '13px', color: '#2ecc71' }}>
                  💪 {t('aiStrengths', language)}
                </h4>
                <ul style={{ margin: 0, paddingLeft: '18px', fontSize: '13px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
                  {personality.keyStrengths.map((str, idx) => (
                    <li key={idx}>{str}</li>
                  ))}
                </ul>
              </div>
            )}
            {personality.vulnerabilitiesAndKarmicLessons && personality.vulnerabilitiesAndKarmicLessons.length > 0 && (
              <div style={{ background: 'rgba(230, 126, 34, 0.06)', border: '1px solid rgba(230, 126, 34, 0.3)', borderRadius: '8px', padding: '12px' }}>
                <h4 style={{ margin: '0 0 8px', fontSize: '13px', color: '#e67e22' }}>
                  🧭 {t('aiKarmicLessons', language)}
                </h4>
                <ul style={{ margin: 0, paddingLeft: '18px', fontSize: '13px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
                  {personality.vulnerabilitiesAndKarmicLessons.map((les, idx) => (
                    <li key={idx}>{les}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Ayurvedic Health Diagnostics Card */}
      {health && (
        <div className="card" style={{ borderLeft: '4px solid #2ecc71' }}>
          <h3 style={{ margin: '0 0 12px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            🌿 {t('aiHealthTitle', language)}
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '14px' }}>
            {health.ayurvedicConstitution && (
              <div style={{ background: 'rgba(255,255,255,0.03)', padding: '12px', borderRadius: '8px', border: '1px solid var(--border)' }}>
                <strong style={{ fontSize: '12px', color: 'var(--accent-gold)', display: 'block', marginBottom: '4px' }}>
                  🩺 {t('aiAyurvedicConstitution', language)}:
                </strong>
                <span style={{ fontSize: '13px', color: 'var(--text-primary)' }}>{health.ayurvedicConstitution}</span>
              </div>
            )}
            {health.longevityVitalitySummary && (
              <div style={{ background: 'rgba(255,255,255,0.03)', padding: '12px', borderRadius: '8px', border: '1px solid var(--border)' }}>
                <strong style={{ fontSize: '12px', color: '#2ecc71', display: 'block', marginBottom: '4px' }}>
                  🧬 {t('aiLongevitySummary', language)}:
                </strong>
                <span style={{ fontSize: '13px', color: 'var(--text-primary)' }}>{health.longevityVitalitySummary}</span>
              </div>
            )}
          </div>

          {health.organVulnerabilities && health.organVulnerabilities.length > 0 && (
            <div style={{ marginTop: '12px', background: 'rgba(231, 76, 60, 0.05)', border: '1px solid rgba(231, 76, 60, 0.25)', borderRadius: '8px', padding: '12px' }}>
              <strong style={{ fontSize: '12px', color: '#e74c3c', display: 'block', marginBottom: '6px' }}>
                ⚠️ {t('aiOrganVulnerabilities', language)}:
              </strong>
              <ul style={{ margin: 0, paddingLeft: '18px', fontSize: '13px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
                {health.organVulnerabilities.map((org, idx) => (
                  <li key={idx}>{org}</li>
                ))}
              </ul>
            </div>
          )}

          {health.recommendedDietAndLifestyle && health.recommendedDietAndLifestyle.length > 0 && (
            <div style={{ marginTop: '12px', background: 'rgba(46, 204, 113, 0.05)', border: '1px solid rgba(46, 204, 113, 0.25)', borderRadius: '8px', padding: '12px' }}>
              <strong style={{ fontSize: '12px', color: '#2ecc71', display: 'block', marginBottom: '6px' }}>
                🥗 {t('aiDietLifestyle', language)}:
              </strong>
              <ul style={{ margin: 0, paddingLeft: '18px', fontSize: '13px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
                {health.recommendedDietAndLifestyle.map((diet, idx) => (
                  <li key={idx}>{diet}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      {/* Classical Vedic Yogas */}
      {aiYogas.length > 0 && (
        <div className="card">
          <h3 style={{ margin: '0 0 12px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            👑 {t('yogasDetected', language)} ({aiYogas.length})
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '12px' }}>
            {aiYogas.map((y, idx) => (
              <div key={idx} style={{ background: 'rgba(255, 215, 0, 0.04)', border: '1px solid rgba(255, 215, 0, 0.25)', borderRadius: '8px', padding: '12px' }}>
                <h4 style={{ margin: '0 0 6px', fontSize: '14px', color: 'var(--accent-gold)' }}>
                  👑 {y.name}
                </h4>
                {y.formingPlanets && (
                  <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    🪐 <strong>{t('signLord', language)}: </strong>{y.formingPlanets}
                  </div>
                )}
                <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-primary)', lineHeight: '1.5' }}>
                  {y.impact}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Doshams & Nullifications */}
      {aiDoshams.length > 0 && (
        <div className="card">
          <h3 style={{ margin: '0 0 12px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            🛡️ {t('doshamsEvaluated', language)} ({aiDoshams.length})
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '12px' }}>
            {aiDoshams.map((d, idx) => {
              const isNullified = d.status && (d.status.toLowerCase().includes('nullif') || d.status.includes('நிவர்த்தி') || d.status.includes('ನಿವಾರಣೆ'));
              return (
                <div
                  key={idx}
                  style={{
                    background: isNullified ? 'rgba(39, 174, 96, 0.05)' : 'rgba(231, 76, 60, 0.05)',
                    border: `1px solid ${isNullified ? 'rgba(39, 174, 96, 0.4)' : 'rgba(231, 76, 60, 0.4)'}`,
                    borderRadius: '8px',
                    padding: '14px'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <h4 style={{ margin: 0, fontSize: '14px', color: isNullified ? '#27ae60' : '#e74c3c' }}>
                      {isNullified ? '✓ ' : '⚠️ '}{d.name}
                    </h4>
                    <span
                      style={{
                        background: isNullified ? '#27ae60' : '#e74c3c',
                        color: '#fff',
                        fontSize: '11px',
                        padding: '2px 8px',
                        borderRadius: '12px',
                        fontWeight: 'bold'
                      }}
                    >
                      {d.status}
                    </span>
                  </div>

                  {d.nullificationFactor && (
                    <div style={{ fontSize: '13px', color: 'var(--text-primary)', marginBottom: '8px', lineHeight: '1.5' }}>
                      <strong style={{ color: 'var(--accent-gold)' }}>
                        {t('reason', language)}:
                      </strong> {d.nullificationFactor}
                    </div>
                  )}

                  {d.remedy && (
                    <div style={{ fontSize: '12px', color: 'var(--text-secondary)', background: 'rgba(255,215,0,0.05)', padding: '6px 8px', borderRadius: '4px', lineHeight: '1.4' }}>
                      🪔 <strong>{t('remedy', language)}: </strong>{d.remedy}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Past Life Verification Milestones */}
      {pastMilestones.length > 0 && (
        <div className="card">
          <div style={{ borderBottom: '1px solid var(--border)', paddingBottom: '10px', marginBottom: '15px' }}>
            <h3 style={{ margin: '0 0 5px', color: 'var(--accent-gold)' }}>
              📜 {t('pastVerificationTitle', language)}
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: 0 }}>
              {t('pastVerificationDesc', language)}
            </p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '15px' }}>
            {pastMilestones.map((m, idx) => {
              const isVerified = verifiedMap[idx] || m.verified;
              const isChallenging = m.nature === 'CHALLENGING';
              return (
                <div
                  key={idx}
                  style={{
                    background: isVerified ? 'rgba(39, 174, 96, 0.08)' : 'var(--bg-card)',
                    border: `1px solid ${isVerified ? '#27ae60' : (isChallenging ? 'rgba(231, 76, 60, 0.4)' : 'var(--border)')}`,
                    borderRadius: '8px',
                    padding: '14px',
                    transition: 'all 0.2s'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                    <div>
                      <span style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--accent-gold)' }}>
                        📅 {m.year} ({t('yearAge', language)}: {m.age})
                      </span>
                      <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '2px' }}>
                        🪐 {m.dasaBhukthi}
                      </div>
                    </div>
                    <button
                      onClick={() => toggleVerified(idx)}
                      style={{
                        background: isVerified ? '#27ae60' : 'none',
                        color: isVerified ? '#fff' : 'var(--text-secondary)',
                        border: `1px solid ${isVerified ? '#27ae60' : 'var(--border)'}`,
                        borderRadius: '16px',
                        padding: '4px 10px',
                        fontSize: '12px',
                        cursor: 'pointer',
                        fontWeight: 'bold'
                      }}
                    >
                      {isVerified ? t('verifiedCheck', language) : t('confirmMatch', language)}
                    </button>
                  </div>

                  <h4 style={{ margin: '8px 0 4px', fontSize: '14px', color: 'var(--text-primary)' }}>
                    {m.milestoneTitle}
                  </h4>
                  <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '0 0 6px', lineHeight: '1.5' }}>
                    {m.description}
                  </p>
                  {m.astrologicalFactor && (
                    <div style={{ fontSize: '11px', color: 'var(--accent-gold)', opacity: 0.9 }}>
                      ✨ {m.astrologicalFactor}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Future Year-by-Year Lifetime Predictions */}
      {lifetimeList.length > 0 && (
        <div className="card">
          <div style={{ borderBottom: '1px solid var(--border)', paddingBottom: '12px', marginBottom: '15px' }}>
            <h3 style={{ margin: '0 0 10px', color: 'var(--accent-gold)' }}>
              🔭 {t('futurePredictionsTitle', language)}
            </h3>

            {/* Filter Chips */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
              {[
                { id: 'ALL', label: t('filterAll', language) },
                { id: 'CAREER', label: `💼 ${t('careerProfession', language)}` },
                { id: 'WEALTH', label: `💰 ${t('wealthFinance', language)}` },
                { id: 'HEALTH', label: `🌿 ${t('filterHealth', language)}` },
                { id: 'FAMILY', label: `👨‍👩‍👧 ${t('marriageFamily', language)}` },
                { id: 'PARENTS_KIDS', label: `👶 ${t('parentsKids', language)}` },
                { id: 'REMEDIES', label: `🪔 ${t('filterRemedies', language)}` }
              ].map((chip) => (
                <button
                  key={chip.id}
                  onClick={() => setActiveFilter(chip.id)}
                  style={{
                    background: activeFilter === chip.id ? 'var(--accent-gold)' : 'var(--bg-card)',
                    color: activeFilter === chip.id ? '#000' : 'var(--text-primary)',
                    border: '1px solid var(--border)',
                    borderRadius: '20px',
                    padding: '6px 14px',
                    fontSize: '12px',
                    cursor: 'pointer',
                    fontWeight: activeFilter === chip.id ? 'bold' : 'normal'
                  }}
                >
                  {chip.label}
                </button>
              ))}
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '15px' }}>
            {lifetimeList.map((fp, idx) => (
              <div
                key={idx}
                style={{
                  background: 'var(--bg-card)',
                  border: '1px solid var(--border)',
                  borderRadius: '8px',
                  padding: '15px',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '10px'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid rgba(255,255,255,0.06)', paddingBottom: '8px' }}>
                  <span style={{ fontSize: '15px', fontWeight: 'bold', color: 'var(--accent-gold)' }}>
                    🌟 {fp.year} ({t('yearAge', language)}: {fp.age})
                  </span>
                  <span style={{ fontSize: '12px', color: 'var(--text-secondary)', background: 'rgba(255,215,0,0.1)', padding: '2px 8px', borderRadius: '4px' }}>
                    {fp.dasaBhukthi}
                  </span>
                </div>

                {fp.personalMindset && (
                  <div>
                    <strong style={{ fontSize: '12px', color: '#9b59b6', display: 'block', marginBottom: '2px' }}>
                      🧠 {t('personalMindset', language)}:
                    </strong>
                    <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                      {fp.personalMindset}
                    </p>
                  </div>
                )}

                {(activeFilter === 'ALL' || activeFilter === 'CAREER') && (fp.careerProfession || fp.careerFinance) && (
                  <div>
                    <strong style={{ fontSize: '12px', color: 'var(--accent-gold)', display: 'block', marginBottom: '2px' }}>
                      💼 {t('careerProfession', language)}:
                    </strong>
                    <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                      {fp.careerProfession || fp.careerFinance}
                    </p>
                  </div>
                )}

                {(activeFilter === 'ALL' || activeFilter === 'WEALTH') && fp.wealthFinance && (
                  <div>
                    <strong style={{ fontSize: '12px', color: '#f39c12', display: 'block', marginBottom: '2px' }}>
                      💰 {t('wealthFinance', language)}:
                    </strong>
                    <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                      {fp.wealthFinance}
                    </p>
                  </div>
                )}

                {(activeFilter === 'ALL' || activeFilter === 'HEALTH') && fp.healthVitality && (
                  <div>
                    <strong style={{ fontSize: '12px', color: '#2ecc71', display: 'block', marginBottom: '2px' }}>
                      🌿 {t('filterHealth', language)}:
                    </strong>
                    <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                      {fp.healthVitality}
                    </p>
                  </div>
                )}

                {(activeFilter === 'ALL' || activeFilter === 'FAMILY') && (fp.marriageFamily || fp.familyMarriage) && (
                  <div>
                    <strong style={{ fontSize: '12px', color: '#e74c3c', display: 'block', marginBottom: '2px' }}>
                      👨‍👩‍👧 {t('marriageFamily', language)}:
                    </strong>
                    <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                      {fp.marriageFamily || fp.familyMarriage}
                    </p>
                  </div>
                )}

                {(activeFilter === 'ALL' || activeFilter === 'PARENTS_KIDS') && fp.parentsKids && (
                  <div>
                    <strong style={{ fontSize: '12px', color: '#3498db', display: 'block', marginBottom: '2px' }}>
                      👶 {t('parentsKids', language)}:
                    </strong>
                    <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                      {fp.parentsKids}
                    </p>
                  </div>
                )}

                {fp.favorableVsCaution && (
                  <div style={{ background: 'rgba(230, 126, 34, 0.05)', border: '1px solid rgba(230, 126, 34, 0.2)', padding: '6px 10px', borderRadius: '4px' }}>
                    <strong style={{ fontSize: '11px', color: '#e67e22', display: 'block', marginBottom: '2px' }}>
                      ⚖️ {t('favorableVsCaution', language)}:
                    </strong>
                    <p style={{ fontSize: '12px', margin: 0, color: 'var(--text-secondary)', lineHeight: '1.4' }}>
                      {fp.favorableVsCaution}
                    </p>
                  </div>
                )}

                {(activeFilter === 'ALL' || activeFilter === 'REMEDIES') && fp.remediesGuidance && (
                  <div style={{ background: 'rgba(255,215,0,0.05)', padding: '8px', borderRadius: '6px' }}>
                    <strong style={{ fontSize: '12px', color: 'var(--accent-gold)', display: 'block', marginBottom: '2px' }}>
                      🪔 {t('remediesGuidance', language)}:
                    </strong>
                    <p style={{ fontSize: '12px', margin: 0, color: 'var(--text-primary)', lineHeight: '1.4' }}>
                      {fp.remediesGuidance}
                    </p>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

export default AiPredictionsView;
