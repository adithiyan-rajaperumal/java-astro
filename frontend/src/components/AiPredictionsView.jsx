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
  if (!predictions && !loading) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '40px 20px' }}>
        <h3 style={{ color: 'var(--accent-gold)', marginBottom: '10px' }}>
          {t('aiBalanTab', language)}
        </h3>
        <p style={{ maxWidth: '650px', margin: '0 auto 25px', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
          {language === 'ta'
            ? 'உங்கள் ஜாதகத்தின் 12 வர்க்கங்கள் (D1, D9, D10, D12, D30) மற்றும் விம்சோத்தரி திசா புக்தி அடிப்படையில் கணிக்கப்பட்ட துல்லியமான வாழ்நாள் பலன்கள்.'
            : 'Authentic lifetime predictions synthesized from 12-Varga charts (D1, D9, D10, D12, D30) and running Vimshottari Dasa-Bhukthi timelines.'}
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
  const anchors = predictions?.auspiciousAnchors;
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
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px', flexWrap: 'wrap' }}>
          <span>💾 <strong style={{ color: 'var(--accent-gold)' }}>{t('cachedNotice30Days', language)}</strong></span>
          {predictions?.tokenUsage && (
            <>
              <span>⚡ <strong>{predictions.tokenUsage.totalTokens?.toLocaleString()}</strong> {t('tokensCount', language) || 'tokens'}</span>
              {(predictions.tokenUsage.estimatedCostUsd > 0 || predictions.tokenUsage.estimatedCostInr > 0) && (
                <span>💵 <strong>${predictions.tokenUsage.estimatedCostUsd?.toFixed(4)} / ₹{predictions.tokenUsage.estimatedCostInr?.toFixed(2)}</strong></span>
              )}
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

      {/* Auspicious Life Anchors Card */}
      {anchors && (
        <div className="card" style={{
          background: 'linear-gradient(135deg, rgba(255,215,0,0.06), rgba(30,30,45,0.6))',
          border: '1px solid rgba(255,215,0,0.3)',
          borderRadius: '10px',
          padding: '18px'
        }}>
          <h3 style={{ margin: '0 0 14px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            💎 {t('auspiciousAnchorsTitle', language)}
          </h3>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '14px' }}>
            {anchors.lifeGemstone && (
              <div style={{ background: 'rgba(255,215,0,0.04)', border: '1px solid rgba(255,215,0,0.2)', borderRadius: '8px', padding: '12px' }}>
                <span style={{ fontSize: '11px', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                  💍 {t('lifeGemstone', language)}
                </span>
                <div style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--accent-gold)', marginTop: '4px' }}>
                  {anchors.lifeGemstone}
                </div>
              </div>
            )}

            {anchors.favorableColors && (
              <div style={{ background: 'rgba(46,204,113,0.04)', border: '1px solid rgba(46,204,113,0.2)', borderRadius: '8px', padding: '12px' }}>
                <span style={{ fontSize: '11px', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                  🎨 {t('favorableColors', language)}
                </span>
                <div style={{ fontSize: '14px', fontWeight: 'bold', color: '#2ecc71', marginTop: '4px' }}>
                  {anchors.favorableColors}
                </div>
              </div>
            )}

            {anchors.luckyNumbers && (
              <div style={{ background: 'rgba(52,152,219,0.04)', border: '1px solid rgba(52,152,219,0.2)', borderRadius: '8px', padding: '12px' }}>
                <span style={{ fontSize: '11px', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                  🔢 {t('luckyNumbers', language)}
                </span>
                <div style={{ fontSize: '14px', fontWeight: 'bold', color: '#3498db', marginTop: '4px' }}>
                  {anchors.luckyNumbers}
                </div>
              </div>
            )}

            {anchors.favorableDays && (
              <div style={{ background: 'rgba(155,89,182,0.04)', border: '1px solid rgba(155,89,182,0.2)', borderRadius: '8px', padding: '12px' }}>
                <span style={{ fontSize: '11px', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                  📅 {t('favorableDays', language)}
                </span>
                <div style={{ fontSize: '14px', fontWeight: 'bold', color: '#9b59b6', marginTop: '4px' }}>
                  {anchors.favorableDays}
                </div>
              </div>
            )}

            {anchors.ishtaDevata && (
              <div style={{ background: 'rgba(230,126,34,0.04)', border: '1px solid rgba(230,126,34,0.2)', borderRadius: '8px', padding: '12px' }}>
                <span style={{ fontSize: '11px', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                  🪔 {t('ishtaDevata', language)}
                </span>
                <div style={{ fontSize: '14px', fontWeight: 'bold', color: '#e67e22', marginTop: '4px' }}>
                  {anchors.ishtaDevata}
                </div>
              </div>
            )}

            {anchors.favorableDirections && (
              <div style={{ background: 'rgba(26,188,156,0.04)', border: '1px solid rgba(26,188,156,0.2)', borderRadius: '8px', padding: '12px' }}>
                <span style={{ fontSize: '11px', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                  🧭 {t('favorableDirections', language)}
                </span>
                <div style={{ fontSize: '14px', fontWeight: 'bold', color: '#1abc9c', marginTop: '4px' }}>
                  {anchors.favorableDirections}
                </div>
              </div>
            )}
          </div>
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

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '15px' }}>
            {personality.keyStrengths && personality.keyStrengths.length > 0 && (
              <div style={{ background: 'rgba(46, 204, 113, 0.05)', border: '1px solid rgba(46, 204, 113, 0.2)', borderRadius: '8px', padding: '14px' }}>
                <h4 style={{ margin: '0 0 8px', color: '#2ecc71', fontSize: '14px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  ✅ {t('keyStrengths', language)}
                </h4>
                <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '13px', lineHeight: '1.6', color: 'var(--text-primary)' }}>
                  {personality.keyStrengths.map((st, i) => (
                    <li key={i}>{st}</li>
                  ))}
                </ul>
              </div>
            )}

            {personality.vulnerabilitiesAndKarmicLessons && personality.vulnerabilitiesAndKarmicLessons.length > 0 && (
              <div style={{ background: 'rgba(230, 126, 34, 0.05)', border: '1px solid rgba(230, 126, 34, 0.25)', borderRadius: '8px', padding: '14px' }}>
                <h4 style={{ margin: '0 0 8px', color: '#e67e22', fontSize: '14px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  ⚖️ {t('karmicLessons', language)}
                </h4>
                <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '13px', lineHeight: '1.6', color: 'var(--text-primary)' }}>
                  {personality.vulnerabilitiesAndKarmicLessons.map((vl, i) => (
                    <li key={i}>{vl}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Ayurvedic Health Analysis */}
      {health && (
        <div className="card">
          <h3 style={{ margin: '0 0 12px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            🌿 {t('healthAnalysisTitle', language)}
          </h3>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '15px', marginBottom: '14px' }}>
            {health.ayurvedicConstitution && (
              <div style={{ background: 'rgba(52, 152, 219, 0.05)', border: '1px solid rgba(52, 152, 219, 0.2)', borderRadius: '8px', padding: '14px' }}>
                <h4 style={{ margin: '0 0 6px', color: '#3498db', fontSize: '14px' }}>
                  🌀 {t('ayurvedicConstitution', language)}
                </h4>
                <p style={{ margin: 0, fontSize: '13px', lineHeight: '1.5', color: 'var(--text-primary)' }}>
                  {health.ayurvedicConstitution}
                </p>
              </div>
            )}

            {health.longevityVitalitySummary && (
              <div style={{ background: 'rgba(155, 89, 182, 0.05)', border: '1px solid rgba(155, 89, 182, 0.2)', borderRadius: '8px', padding: '14px' }}>
                <h4 style={{ margin: '0 0 6px', color: '#9b59b6', fontSize: '14px' }}>
                  ⚡ {t('longevityVitality', language)}
                </h4>
                <p style={{ margin: 0, fontSize: '13px', lineHeight: '1.5', color: 'var(--text-primary)' }}>
                  {health.longevityVitalitySummary}
                </p>
              </div>
            )}
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '15px' }}>
            {health.organVulnerabilities && health.organVulnerabilities.length > 0 && (
              <div style={{ background: 'rgba(231, 76, 60, 0.05)', border: '1px solid rgba(231, 76, 60, 0.2)', borderRadius: '8px', padding: '14px' }}>
                <h4 style={{ margin: '0 0 8px', color: '#e74c3c', fontSize: '14px' }}>
                  🩺 {t('organVulnerabilities', language)}
                </h4>
                <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '13px', lineHeight: '1.6', color: 'var(--text-primary)' }}>
                  {health.organVulnerabilities.map((v, i) => (
                    <li key={i}>{v}</li>
                  ))}
                </ul>
              </div>
            )}

            {health.recommendedDietAndLifestyle && health.recommendedDietAndLifestyle.length > 0 && (
              <div style={{ background: 'rgba(46, 204, 113, 0.05)', border: '1px solid rgba(46, 204, 113, 0.2)', borderRadius: '8px', padding: '14px' }}>
                <h4 style={{ margin: '0 0 8px', color: '#2ecc71', fontSize: '14px' }}>
                  🥗 {t('recommendedDietLifestyle', language)}
                </h4>
                <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '13px', lineHeight: '1.6', color: 'var(--text-primary)' }}>
                  {health.recommendedDietAndLifestyle.map((d, i) => (
                    <li key={i}>{d}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      )}

      {/* AI Classical Yogas */}
      {aiYogas.length > 0 && (
        <div className="card">
          <h3 style={{ margin: '0 0 14px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            ✨ {t('classicalYogasTitle', language)}
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '12px' }}>
            {aiYogas.map((y, idx) => (
              <div
                key={idx}
                style={{
                  background: 'rgba(255, 215, 0, 0.03)',
                  border: '1px solid rgba(255, 215, 0, 0.2)',
                  borderRadius: '8px',
                  padding: '12px 16px'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                  <strong style={{ color: 'var(--accent-gold)', fontSize: '14px' }}>{y.name}</strong>
                  {y.formingPlanets && (
                    <span style={{ fontSize: '11px', background: 'rgba(255,255,255,0.06)', padding: '2px 6px', borderRadius: '4px' }}>
                      {y.formingPlanets}
                    </span>
                  )}
                </div>
                <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                  {y.impact}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* AI Doshams Analysis */}
      {aiDoshams.length > 0 && (
        <div className="card">
          <h3 style={{ margin: '0 0 14px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            🛡️ {t('doshamsAnalysisTitle', language)}
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '12px' }}>
            {aiDoshams.map((d, idx) => (
              <div
                key={idx}
                style={{
                  background: 'rgba(255, 255, 255, 0.02)',
                  border: '1px solid var(--border)',
                  borderRadius: '8px',
                  padding: '14px'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                  <strong style={{ color: 'var(--text-primary)', fontSize: '14px' }}>{d.name}</strong>
                  <span style={{
                    fontSize: '11px',
                    fontWeight: 'bold',
                    padding: '2px 8px',
                    borderRadius: '4px',
                    background: (d.status?.toLowerCase().includes('nullified') || d.status?.toLowerCase().includes('நிவர்த்தி'))
                      ? 'rgba(46, 204, 113, 0.15)'
                      : 'rgba(231, 76, 60, 0.15)',
                    color: (d.status?.toLowerCase().includes('nullified') || d.status?.toLowerCase().includes('நிவர்த்தி'))
                      ? '#2ecc71'
                      : '#e74c3c'
                  }}>
                    {d.status}
                  </span>
                </div>
                {d.nullificationFactor && (
                  <p style={{ margin: '0 0 6px', fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
                    <strong>{t('nullificationReason', language)}:</strong> {d.nullificationFactor}
                  </p>
                )}
                {d.remedy && (
                  <p style={{ margin: 0, fontSize: '12px', color: 'var(--accent-gold)', lineHeight: '1.4' }}>
                    <strong>{t('remedyTitle', language)}:</strong> {d.remedy}
                  </p>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Past Key Turning Points */}
      {pastPhases.length > 0 && (
        <div className="card">
          <h3 style={{ margin: '0 0 14px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            ⏳ {t('pastTurningPointsTitle', language)}
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {pastPhases.map((phase, idx) => (
              <div
                key={idx}
                style={{
                  background: 'rgba(255, 255, 255, 0.02)',
                  borderLeft: '4px solid var(--accent-gold)',
                  borderTop: '1px solid var(--border)',
                  borderRight: '1px solid var(--border)',
                  borderBottom: '1px solid var(--border)',
                  borderRadius: '0 8px 8px 0',
                  padding: '14px 16px'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px', flexWrap: 'wrap', gap: '8px' }}>
                  <strong style={{ color: 'var(--accent-gold)', fontSize: '14px' }}>
                    {phase.phaseTitle || phase.title || `${t('phaseLabel', language)} ${idx + 1}`}
                  </strong>
                  <div style={{ display: 'flex', gap: '8px', fontSize: '12px', color: 'var(--text-secondary)' }}>
                    <span>📅 {phase.periodOrAge || phase.period}</span>
                    {phase.dasaBhukthi && (
                      <span>🪐 {phase.dasaBhukthi}</span>
                    )}
                  </div>
                </div>
                <p style={{ margin: '0 0 6px', fontSize: '13px', lineHeight: '1.6', color: 'var(--text-primary)' }}>
                  {phase.livedExperience || phase.description}
                </p>
                {phase.astrologicalBasis && (
                  <div style={{ fontSize: '11px', color: 'var(--text-secondary)', opacity: 0.85 }}>
                    🪐 <strong>{t('astrologicalBasis', language)}:</strong> {phase.astrologicalBasis}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Year-by-Year Lifetime Predictions (Unified Narrative) */}
      {lifetimeList.length > 0 && (
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', flexWrap: 'wrap', gap: '10px' }}>
            <h3 style={{ margin: 0, color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
              🔮 {t('lifetimeForecastTitle', language)}
            </h3>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '15px' }}>
            {lifetimeList.map((fp, idx) => {
              const narrativeText = fp.detailedPrediction || [fp.careerAndFinance, fp.healthAndFamily, fp.careerProfession, fp.wealthFinance, fp.healthVitality, fp.marriageFamily, fp.parentsKids].filter(Boolean).join(' ');

              return (
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
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid rgba(255,255,255,0.06)', paddingBottom: '8px', flexWrap: 'wrap', gap: '6px' }}>
                    <span style={{ fontSize: '15px', fontWeight: 'bold', color: 'var(--accent-gold)' }}>
                      🌟 {fp.year} ({t('yearAge', language)}: {fp.age})
                    </span>
                    <span style={{ fontSize: '12px', color: 'var(--text-secondary)', background: 'rgba(255,215,0,0.1)', padding: '2px 8px', borderRadius: '4px', fontWeight: '500' }}>
                      {fp.dasaBhukthi}
                    </span>
                  </div>

                  {/* Yearly Theme Headline */}
                  {fp.yearlyTheme && (
                    <div style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--text-primary)', lineHeight: '1.4' }}>
                      🎯 {fp.yearlyTheme}
                    </div>
                  )}

                  {/* Detailed Unified Narrative Paragraph */}
                  {narrativeText && (
                    <div style={{ background: 'rgba(255, 255, 255, 0.02)', border: '1px solid var(--border)', borderRadius: '6px', padding: '12px' }}>
                      <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-primary)', lineHeight: '1.6' }}>
                        {narrativeText}
                      </p>
                    </div>
                  )}

                  {/* Astrological Basis */}
                  {fp.astrologicalBasis && (
                    <div style={{ fontSize: '11px', color: 'var(--accent-gold)', opacity: 0.9 }}>
                      🪐 <strong>{t('astrologicalBasis', language)}:</strong> {fp.astrologicalBasis}
                    </div>
                  )}

                  {/* Cautions & Remedies */}
                  {(fp.cautionsAndRemedies || fp.favorableVsCaution || fp.remediesGuidance) && (
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
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}

export default AiPredictionsView;
