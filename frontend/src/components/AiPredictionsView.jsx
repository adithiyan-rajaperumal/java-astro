import React from 'react';
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
      <div className="card" style={{ textAlign: 'center', padding: '40px 20px', background: 'var(--bg-card)' }}>
        <h3 style={{ color: 'var(--accent-saffron)', marginBottom: '12px' }}>
          ✨ {t('aiBalanTab', language)}
        </h3>
        <p style={{ maxWidth: '680px', margin: '0 auto 25px', color: 'var(--text-secondary)', lineHeight: '1.6', fontSize: '14px' }}>
          {language === 'ta'
            ? 'உங்கள் ஜாதகத்தின் 12 வர்க்க சக்கரங்கள் (D1, D9, D10, D12, D30...), ஷட்பலம் மற்றும் விம்சோத்தரி திசா புக்தி அடிப்படையில் AI சுயமாக கணிக்கும் ஆழமான வாழ்நாள் பலன்கள்.'
            : 'Authentic astrological predictions synthesized from 12 Divisional Varga charts (D1, D9, D10, D12, D30...), Shadbala planetary matrices, and Vimshottari Dasa-Bhukthi timelines.'}
        </p>

        <div style={{ display: 'flex', justifyContent: 'center' }}>
          <button
            onClick={() => onGenerate(false)}
            className="btn-primary"
            style={{
              padding: '14px 32px',
              fontSize: '16px',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '10px'
            }}
          >
            ✨ {t('generateAiBalan', language) || 'Generate AI Life Predictions'}
          </button>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '50px 20px', background: 'var(--bg-card)' }}>
        <div className="spinner" style={{ margin: '0 auto 20px' }}></div>
        <h4 style={{ color: 'var(--accent-saffron)', marginBottom: '8px' }}>{t('generatingAiBalan', language)}</h4>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
          {t('horoscopeCalculating', language)}
        </p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="card" style={{ borderLeft: '4px solid var(--danger)', background: 'rgba(231, 76, 60, 0.06)' }}>
        <h4 style={{ color: 'var(--danger)', margin: '0 0 8px' }}>
          ⚠️ {t('calculationFaulted', language)}
        </h4>
        <p style={{ color: 'var(--text-secondary)', margin: '0 0 15px', fontSize: '14px' }}>{error}</p>
        <button onClick={() => onGenerate(true)} className="btn-primary" style={{ padding: '8px 20px' }}>
          🔄 {t('retry', language)}
        </button>
      </div>
    );
  }

  if (predictions && predictions.enabled === false) {
    return (
      <div className="card" style={{ borderLeft: '4px solid var(--danger)', background: 'rgba(231, 76, 60, 0.06)' }}>
        <h4 style={{ color: 'var(--danger)', margin: '0 0 8px' }}>
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

  const personality = predictions?.personalityAndBehavior;
  const milestones = predictions?.retrospectivePastMilestones || [];
  const longevity = predictions?.aiLongevityAnalysis;
  const yearlyList = predictions?.yearlyPredictions || [];
  const is10Year = predictions?.forecastMode === 'TEN_YEARS' || yearlyList.length <= 15;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Top Status & Generation Bar */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '12px',
        background: 'var(--bg-card)',
        border: '1px solid var(--border)',
        borderRadius: '10px',
        padding: '12px 18px',
        fontSize: '13px',
        color: 'var(--text-secondary)',
        boxShadow: 'var(--shadow)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px', flexWrap: 'wrap' }}>
          <span style={{
            fontSize: '12px',
            fontWeight: 'bold',
            padding: '3px 10px',
            borderRadius: '12px',
            background: 'rgba(255, 107, 0, 0.08)',
            color: 'var(--accent-warm)',
            border: '1px solid var(--accent-gold)'
          }}>
            {is10Year ? '⚡ ' + t('tenYearMode', language) : '🪐 ' + t('lifetimeMode', language)}
          </span>
          <span>💾 <strong style={{ color: 'var(--accent-warm)' }}>{t('cachedNotice30Days', language)}</strong></span>
          {predictions?.tokenUsage && (
            <>
              <span>⚡ <strong>{predictions.tokenUsage.totalTokens?.toLocaleString()}</strong> {t('tokensCount', language) || 'tokens'}</span>
              {(predictions.tokenUsage.estimatedCostUsd > 0 || predictions.tokenUsage.estimatedCostInr > 0) && (
                <span>💵 <strong>${predictions.tokenUsage.estimatedCostUsd?.toFixed(4)} / ₹{predictions.tokenUsage.estimatedCostInr?.toFixed(2)}</strong></span>
              )}
              <span>🤖 <code style={{ color: 'var(--accent-saffron)' }}>{predictions.tokenUsage.modelUsed}</code></span>
            </>
          )}
        </div>

        <div>
          <button
            onClick={() => onGenerate(true)}
            className="btn-primary"
            style={{
              padding: '8px 18px',
              fontSize: '13px'
            }}
          >
            ✨ {t('regenerateAiBalan', language)}
          </button>
        </div>
      </div>

      {/* 1. Personality & Behavioral Profile Card */}
      {personality?.coreTemperament && (
        <div className="card" style={{
          borderLeft: '4px solid var(--accent-saffron)',
          borderRadius: '10px',
          padding: '20px',
          background: 'var(--bg-card)'
        }}>
          <h3 style={{ margin: '0 0 12px', color: 'var(--accent-saffron)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '17px' }}>
            🧠 {t('aiPersonalityBehaviorTitle', language)}
          </h3>
          <p style={{ fontSize: '14px', lineHeight: '1.7', color: 'var(--text-primary)', margin: 0 }}>
            {personality.coreTemperament}
          </p>
        </div>
      )}

      {/* 2. Retrospective Past Milestones Card */}
      {milestones.length > 0 && (
        <div className="card" style={{ borderRadius: '10px', padding: '20px', background: 'var(--bg-card)' }}>
          <h3 style={{ margin: '0 0 16px', color: 'var(--accent-saffron)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '17px' }}>
            🕰️ {t('retrospectiveMilestonesTitle', language)}
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '14px' }}>
            {milestones.map((m, idx) => (
              <div
                key={idx}
                style={{
                  background: 'var(--bg-primary)',
                  borderLeft: '4px solid var(--accent-gold)',
                  borderTop: '1px solid var(--border)',
                  borderRight: '1px solid var(--border)',
                  borderBottom: '1px solid var(--border)',
                  borderRadius: '0 8px 8px 0',
                  padding: '14px 16px'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px', flexWrap: 'wrap', gap: '6px' }}>
                  <strong style={{ color: 'var(--accent-warm)', fontSize: '14px' }}>
                    {m.milestoneTitle}
                  </strong>
                  {m.approxPeriod && (
                    <span style={{ fontSize: '12px', color: 'var(--text-secondary)', background: 'rgba(255, 107, 0, 0.08)', border: '1px solid rgba(255, 107, 0, 0.2)', padding: '2px 8px', borderRadius: '4px', fontWeight: '500' }}>
                      📅 {m.approxPeriod}
                    </span>
                  )}
                </div>
                <p style={{ margin: 0, fontSize: '13px', lineHeight: '1.6', color: 'var(--text-primary)' }}>
                  {m.eventNarrative}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 3. AI Shastric Longevity & Active Yogas Analysis Card */}
      {longevity && (
        <div className="card" style={{
          borderLeft: '4px solid var(--accent-gold)',
          borderRadius: '10px',
          padding: '20px',
          background: 'var(--bg-card)'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', flexWrap: 'wrap', gap: '10px' }}>
            <h3 style={{ margin: 0, color: 'var(--accent-saffron)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '17px' }}>
              ⏳ {t('aiLongevityAnalysisTitle', language)}
            </h3>

            {longevity.calculatedAyulCeiling > 0 && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{
                  fontSize: '14px',
                  fontWeight: 'bold',
                  padding: '4px 14px',
                  borderRadius: '16px',
                  background: 'rgba(255, 107, 0, 0.1)',
                  color: 'var(--accent-warm)',
                  border: '1px solid var(--accent-gold)'
                }}>
                  🎯 {t('aiCalculatedAyulCeiling', language)}: {longevity.calculatedAyulCeiling} {t('yearsSuffix', language)} ({longevity.classification || 'Poornayu'})
                </span>
              </div>
            )}
          </div>

          {longevity.primarySpanRationale && (
            <div style={{
              background: 'var(--bg-primary)',
              border: '1px solid var(--border)',
              borderRadius: '8px',
              padding: '12px 16px',
              marginBottom: '16px'
            }}>
              <p style={{ margin: 0, fontSize: '13px', lineHeight: '1.6', color: 'var(--text-primary)' }}>
                📜 <strong>{t('classicalLongevityRationale', language)}:</strong> {longevity.primarySpanRationale}
              </p>
            </div>
          )}

          {/* Active Yogas & Active Doshas Subgrid */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '16px' }}>
            {/* Active Auspicious Yogas */}
            {longevity.activeYogasIdentified && longevity.activeYogasIdentified.length > 0 && (
              <div style={{
                background: 'rgba(46, 125, 50, 0.04)',
                border: '1px solid rgba(46, 125, 50, 0.25)',
                borderRadius: '8px',
                padding: '14px'
              }}>
                <h4 style={{ margin: '0 0 10px', color: 'var(--success)', fontSize: '14px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  ✨ {t('activeYogasIdentified', language)}
                </h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {longevity.activeYogasIdentified.map((y, idx) => (
                    <div key={idx} style={{ background: '#ffffff', border: '1px solid rgba(46, 125, 50, 0.15)', padding: '8px 10px', borderRadius: '6px' }}>
                      <strong style={{ color: 'var(--accent-saffron)', fontSize: '13px', display: 'block', marginBottom: '2px' }}>
                        {y.yogaName}
                      </strong>
                      <span style={{ fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.4' }}>
                        {y.effect}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Active Doshas & Remedial Guidance */}
            {longevity.activeDoshasIdentified && longevity.activeDoshasIdentified.length > 0 && (
              <div style={{
                background: 'rgba(232, 93, 4, 0.04)',
                border: '1px solid rgba(232, 93, 4, 0.25)',
                borderRadius: '8px',
                padding: '14px'
              }}>
                <h4 style={{ margin: '0 0 10px', color: 'var(--accent-warm)', fontSize: '14px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  🛡️ {t('activeDoshasIdentified', language)}
                </h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {longevity.activeDoshasIdentified.map((d, idx) => (
                    <div key={idx} style={{ background: '#ffffff', border: '1px solid rgba(232, 93, 4, 0.15)', padding: '8px 10px', borderRadius: '6px' }}>
                      <strong style={{ color: 'var(--danger)', fontSize: '13px', display: 'block', marginBottom: '2px' }}>
                        {d.doshaName}
                      </strong>
                      <span style={{ fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.4' }}>
                        {d.remedialAdvice}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* 4. Year-by-Year Predictions (Uncapped Rich Narrative Stream) */}
      {yearlyList.length > 0 && (() => {
        const sYr = predictions?.startYear || yearlyList[0]?.year || '';
        const eYr = predictions?.endYear || yearlyList[yearlyList.length - 1]?.year || '';
        const sAge = predictions?.startAge ?? yearlyList[0]?.age;
        const eAge = predictions?.endAge ?? yearlyList[yearlyList.length - 1]?.age;
        const yearRangeText = sYr && eYr ? ` (${sYr} – ${eYr}${sAge !== undefined && eAge !== undefined ? ` • ${language === 'ta' ? `வயது ${sAge} - ${eAge}` : `Age ${sAge} to ${eAge}`}` : ''})` : '';

        return (
          <div className="card" style={{ borderRadius: '10px', padding: '20px', background: 'var(--bg-card)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px', flexWrap: 'wrap', gap: '10px' }}>
              <h3 style={{ margin: 0, color: 'var(--accent-saffron)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '17px' }}>
                📜 {t('yearlyPredictionsStreamTitle', language)}{yearRangeText}
              </h3>
              <span style={{
                fontSize: '12px',
                fontWeight: 'bold',
                padding: '4px 12px',
                borderRadius: '12px',
                background: 'rgba(255, 107, 0, 0.08)',
                color: 'var(--accent-warm)',
                border: '1px solid var(--accent-gold)'
              }}>
                {is10Year ? `⚡ ${t('tenYearMode', language)}` : `🪐 ${t('lifetimeMode', language)} (${yearlyList.length} ${t('yearsSuffix', language)})`}
              </span>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '16px' }}>
              {yearlyList.map((yp, idx) => {
                const narrative = yp.annualNarrative || yp.detailedPrediction || '';

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
                      gap: '12px',
                      boxShadow: 'var(--shadow)'
                    }}
                  >
                    {/* Header */}
                    <div style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      borderBottom: '1px solid var(--border)',
                      paddingBottom: '8px',
                      flexWrap: 'wrap',
                      gap: '6px'
                    }}>
                      <span style={{ fontSize: '15px', fontWeight: 'bold', color: 'var(--accent-saffron)' }}>
                        🌟 {yp.year} ({t('yearAge', language)}: {yp.age})
                      </span>
                      {yp.dasaBhukthi && (
                        <span style={{
                          fontSize: '12px',
                          color: 'var(--text-secondary)',
                          background: 'rgba(255, 107, 0, 0.06)',
                          border: '1px solid rgba(255, 107, 0, 0.2)',
                          padding: '2px 8px',
                          borderRadius: '4px',
                          fontWeight: '500'
                        }}>
                          🪐 {yp.dasaBhukthi}
                        </span>
                      )}
                    </div>

                    {/* Uncapped Rich Narrative */}
                    {narrative && (
                      <div style={{
                        background: 'var(--bg-primary)',
                        border: '1px solid var(--border)',
                        borderRadius: '6px',
                        padding: '12px'
                      }}>
                        <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-primary)', lineHeight: '1.7', textAlign: 'justify' }}>
                          {narrative}
                        </p>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        );
      })()}
    </div>
  );
}

export default AiPredictionsView;
