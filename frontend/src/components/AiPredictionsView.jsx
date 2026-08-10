import { useState } from 'react';
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

  if (!predictions && !loading) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '40px 20px' }}>
        <div style={{ fontSize: '48px', marginBottom: '15px' }}>🔮</div>
        <h3 style={{ color: 'var(--accent-gold)', marginBottom: '10px' }}>
          {t('aiBalanTab', language)}
        </h3>
        <p style={{ maxWidth: '650px', margin: '0 auto 25px', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
          {language === 'ta'
            ? 'உங்கள் ஜாதகத்தின் 12 வர்க்கங்கள் (D1, D9, D10, D30) மற்றும் விம்சோத்தரி திசா புக்தி அடிப்படையில் கணிக்கப்பட்ட துல்லியமான வாழ்நாள் பலன்கள்.'
            : 'Authentic lifetime predictions synthesized from 12-Varga charts (D1, D9, D10, D30) and running Vimshottari Dasa-Bhukthi timelines.'}
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
  const pastPhases = predictions?.pastKeyPhases || [];
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

      {/* Pivotal Past Key Life Phases (Birth to Present) */}
      {pastPhases.length > 0 && (
        <div className="card">
          <div style={{ borderBottom: '1px solid var(--border)', paddingBottom: '10px', marginBottom: '15px' }}>
            <h3 style={{ margin: '0 0 5px', color: 'var(--accent-gold)' }}>
              📜 {t('pastKeyPhasesTitle', language)}
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: 0 }}>
              {language === 'ta'
                ? 'உங்கள் பிறப்பு முதல் இன்று வரை வாழ்ந்த முக்கிய வாழ்க்கை திருப்புமுனைகளும் அனுபவப் பாடங்களும்.'
                : 'Pivotal life phases, challenges overcome, and behavioral milestones lived from birth to present day.'}
            </p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '15px' }}>
            {pastPhases.map((phase, idx) => (
              <div
                key={idx}
                style={{
                  background: 'var(--bg-card)',
                  border: '1px solid var(--border)',
                  borderRadius: '8px',
                  padding: '16px',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '10px'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid rgba(255,255,255,0.06)', paddingBottom: '8px' }}>
                  <span style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--accent-gold)' }}>
                    ⏳ {phase.periodOrAge}
                  </span>
                  <span style={{ fontSize: '12px', color: 'var(--text-secondary)', background: 'rgba(255,215,0,0.1)', padding: '2px 8px', borderRadius: '4px' }}>
                    {phase.dasaBhukthi}
                  </span>
                </div>

                <h4 style={{ margin: '4px 0 0', fontSize: '15px', color: 'var(--text-primary)' }}>
                  🎯 {phase.phaseTitle}
                </h4>

                <p style={{ fontSize: '13px', color: 'var(--text-primary)', margin: 0, lineHeight: '1.6' }}>
                  {phase.livedExperience}
                </p>

                {phase.astrologicalBasis && (
                  <div style={{ fontSize: '12px', color: 'var(--accent-gold)', background: 'rgba(255,215,0,0.04)', padding: '6px 10px', borderRadius: '4px', borderLeft: '2px solid var(--accent-gold)' }}>
                    ✨ <strong>{t('astrologicalBasis', language)}:</strong> {phase.astrologicalBasis}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Year-by-Year Lifetime Predictions */}
      {lifetimeList.length > 0 && (
        <div className="card">
          <div style={{ borderBottom: '1px solid var(--border)', paddingBottom: '12px', marginBottom: '15px' }}>
            <h3 style={{ margin: '0 0 10px', color: 'var(--accent-gold)' }}>
              🔭 {t('futurePredictionsTitle', language)}
            </h3>

            {/* Filter Chips */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
              {[
                { id: 'ALL', label: t('filterAll', language) || 'All' },
                { id: 'CAREER', label: `💼 ${t('careerAndFinance', language) || 'Career & Wealth'}` },
                { id: 'HEALTH', label: `🌿 ${t('healthAndFamily', language) || 'Health & Family'}` },
                { id: 'REMEDIES', label: `🪔 ${t('cautionsAndRemedies', language) || 'Cautions & Remedies'}` }
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

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '15px' }}>
            {lifetimeList.map((fp, idx) => (
              <div
                key={idx}
                style={{
                  background: 'var(--bg-card)',
                  border: '1px solid var(--border)',
                  borderRadius: '8px',
                  padding: '16px',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '12px'
                }}
              >
                {/* Year Card Header */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid rgba(255,255,255,0.06)', paddingBottom: '8px' }}>
                  <span style={{ fontSize: '15px', fontWeight: 'bold', color: 'var(--accent-gold)' }}>
                    🌟 {fp.year} ({t('yearAge', language)}: {fp.age})
                  </span>
                  <span style={{ fontSize: '12px', color: 'var(--text-secondary)', background: 'rgba(255,215,0,0.1)', padding: '2px 8px', borderRadius: '4px' }}>
                    {fp.dasaBhukthi}
                  </span>
                </div>

                {/* Yearly Theme Headline */}
                {fp.yearlyTheme && (
                  <div style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--text-primary)', lineHeight: '1.4' }}>
                    🎯 {fp.yearlyTheme}
                  </div>
                )}

                {/* Astrological Basis */}
                {fp.astrologicalBasis && (
                  <div style={{ fontSize: '11px', color: 'var(--accent-gold)', opacity: 0.9 }}>
                    🪐 <strong>{t('astrologicalBasis', language)}:</strong> {fp.astrologicalBasis}
                  </div>
                )}

                {/* Career, Job & Wealth */}
                {(activeFilter === 'ALL' || activeFilter === 'CAREER') && (fp.careerAndFinance || fp.careerProfession || fp.wealthFinance) && (
                  <div style={{ background: 'rgba(255,215,0,0.03)', border: '1px solid rgba(255,215,0,0.15)', borderRadius: '6px', padding: '10px' }}>
                    <strong style={{ fontSize: '12px', color: '#f39c12', display: 'block', marginBottom: '4px' }}>
                      💼 {t('careerAndFinance', language)}:
                    </strong>
                    <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-primary)', lineHeight: '1.5' }}>
                      {fp.careerAndFinance || `${fp.careerProfession || ''} ${fp.wealthFinance || ''}`.trim()}
                    </p>
                  </div>
                )}

                {/* Health, Family & Parents */}
                {(activeFilter === 'ALL' || activeFilter === 'HEALTH') && (fp.healthAndFamily || fp.healthVitality || fp.marriageFamily || fp.parentsKids) && (
                  <div style={{ background: 'rgba(46, 204, 113, 0.03)', border: '1px solid rgba(46, 204, 113, 0.2)', borderRadius: '6px', padding: '10px' }}>
                    <strong style={{ fontSize: '12px', color: '#2ecc71', display: 'block', marginBottom: '4px' }}>
                      🌿 {t('healthAndFamily', language)}:
                    </strong>
                    <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-primary)', lineHeight: '1.5' }}>
                      {fp.healthAndFamily || `${fp.healthVitality || ''} ${fp.marriageFamily || ''} ${fp.parentsKids || ''}`.trim()}
                    </p>
                  </div>
                )}

                {/* Cautions & Remedies */}
                {(activeFilter === 'ALL' || activeFilter === 'REMEDIES') && (fp.cautionsAndRemedies || fp.favorableVsCaution || fp.remediesGuidance) && (
                  <div style={{ background: 'rgba(230, 126, 34, 0.05)', border: '1px solid rgba(230, 126, 34, 0.25)', borderRadius: '6px', padding: '10px' }}>
                    <strong style={{ fontSize: '12px', color: '#e67e22', display: 'block', marginBottom: '4px' }}>
                      ⚠️ {t('cautionsAndRemedies', language)}:
                    </strong>
                    <p style={{ fontSize: '12px', margin: 0, color: 'var(--text-primary)', lineHeight: '1.4' }}>
                      {fp.cautionsAndRemedies || `${fp.favorableVsCaution || ''} ${fp.remediesGuidance || ''}`.trim()}
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
