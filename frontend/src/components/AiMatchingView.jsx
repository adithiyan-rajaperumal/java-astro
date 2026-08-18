import React from 'react';
import { t } from '../i18n/translations';
import { useTextToSpeech } from '../utils/useTextToSpeech';

function AiMatchingView({ aiData, loading, onGenerate, language = 'en' }) {
  const tts = useTextToSpeech({ language });

  if (loading) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '40px 20px' }}>
        <div className="spinner" style={{ margin: '0 auto 15px auto' }}></div>
        <h4 style={{ color: 'var(--accent-saffron)', margin: '0 0 10px 0' }}>
          {t('analyzingCompatibility', language)}
        </h4>
        <p style={{ color: 'var(--text-secondary)', fontSize: '13.5px', maxWidth: '500px', margin: '0 auto', lineHeight: '1.5' }}>
          {t('aiMatchingLoadingDesc', language)}
        </p>
      </div>
    );
  }

  if (!aiData || !aiData.enabled) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '30px 20px' }}>
        <div style={{ fontSize: '42px', marginBottom: '15px' }}>✨</div>
        <h3 className="title-gold" style={{ marginTop: 0 }}>
          {t('aiMatchingTitle', language)}
        </h3>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px', maxWidth: '600px', margin: '0 auto 20px auto', lineHeight: '1.5' }}>
          {t('aiMatchingBannerDesc', language)}
        </p>
        {aiData?.message && (
          <p style={{ color: 'var(--warning)', fontSize: '13px', marginBottom: '15px' }}>
            {aiData.message}
          </p>
        )}
        <button onClick={onGenerate} className="btn-primary" style={{ padding: '12px 28px', fontSize: '15px' }}>
          ✨ {t('generateAiMatching', language)}
        </button>
      </div>
    );
  }

  const getVerdictClass = (verdict = '') => {
    const v = verdict.toLowerCase();
    if (v.includes('excellent')) return 'excellent';
    if (v.includes('good')) return 'good';
    if (v.includes('average') || v.includes('moderate')) return 'average';
    return 'not_recommended';
  };

  const domainList = [
    { key: 'emotionalMental', data: aiData.emotionalMentalHarmony, icon: '💖', label: t('emotionalMentalHarmony', language) },
    { key: 'healthLongevity', data: aiData.healthLongevityNadi, icon: '🌿', label: t('healthLongevityNadi', language) },
    { key: 'careerFinance', data: aiData.careerFinancialSynergy, icon: '💼', label: t('careerFinancialSynergy', language) },
    { key: 'progenyFamily', data: aiData.progenyFamilyLineage, icon: '👶', label: t('progenyFamilyLineage', language) },
    { key: 'doshaParity', data: aiData.doshaPapasamyaParity, icon: '⚖️', label: t('doshaPapasamyaParity', language) },
  ];

  const buildSpeechText = () => {
    if (!aiData) return '';
    const parts = [];

    // Title & Overall Score
    parts.push(`${t('aiMatchingTitle', language)}. ${t('overallCompatibility', language)}: ${aiData.compatibilityPercentage ? aiData.compatibilityPercentage.toFixed(0) : '0'}%. ${aiData.overallVerdict || ''}`);

    // Executive Summary
    if (aiData.executiveSummary) {
      parts.push(`${t('executiveSummary', language)}: ${aiData.executiveSummary}`);
    }

    // 5 Domain Analyses
    domainList.forEach(({ label, data }) => {
      if (data && data.analysis) {
        let domainText = `${label || data.title}: ${data.scoreOrStatus ? data.scoreOrStatus + '. ' : ''}${data.analysis}`;
        if (data.astrologicalBasis) {
          domainText += ` ${t('astrologicalBasisLabel', language)}: ${data.astrologicalBasis}`;
        }
        parts.push(domainText);
      }
    });

    // Key Strengths
    if (aiData.keyStrengths && aiData.keyStrengths.length > 0) {
      parts.push(`${t('keyStrengthsTitle', language)}: ${aiData.keyStrengths.join('. ')}`);
    }

    // Growth Areas & Cautions
    if (aiData.growthAreasAndCautions && aiData.growthAreasAndCautions.length > 0) {
      parts.push(`${t('cautionsTitle', language)}: ${aiData.growthAreasAndCautions.join('. ')}`);
    }

    // Remedies
    if (aiData.authenticVedicRemedies && aiData.authenticVedicRemedies.length > 0) {
      parts.push(`${t('remediesTitle', language)}: ${aiData.authenticVedicRemedies.join('. ')}`);
    }

    return parts.join('\n\n');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', width: '100%', maxWidth: '100%', boxSizing: 'border-box' }}>
      {/* Top Status & Audio Control Bar */}
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
        boxShadow: 'var(--shadow)',
        width: '100%',
        boxSizing: 'border-box'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
          <span>✨ <strong style={{ color: 'var(--accent-warm)' }}>{t('aiMatchingTitle', language)}</strong></span>
          <span>💾 <strong style={{ color: 'var(--accent-warm)' }}>{t('cached3HourNotice', language)}</strong></span>
          {aiData.tokenUsage && (
            <>
              <span>⚡ <strong>{aiData.tokenUsage.totalTokens?.toLocaleString()}</strong> {t('tokensCount', language) || 'tokens'}</span>
              {(aiData.tokenUsage.estimatedCostUsd > 0 || aiData.tokenUsage.estimatedCostInr > 0) && (
                <span>💵 <strong>₹{aiData.tokenUsage.estimatedCostInr?.toFixed(2)}</strong></span>
              )}
              <span>🤖 <code style={{ color: 'var(--accent-saffron)' }}>{aiData.tokenUsage.modelUsed || 'gemini-3.7-flash'}</code></span>
            </>
          )}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
          {/* TTS Audio Controls */}
          {tts.isSupported && (
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              background: 'var(--bg-primary)',
              border: '1px solid var(--border)',
              borderRadius: '8px',
              padding: '4px 8px',
              boxShadow: '0 1px 4px rgba(0,0,0,0.04)'
            }}>
              <span style={{ fontSize: '12px', fontWeight: 'bold', color: 'var(--accent-saffron)', display: 'flex', alignItems: 'center', gap: '4px', marginRight: '4px' }}>
                🎙️ {tts.isPlaying ? (tts.isPaused ? '⏸' : '🔊') : ''}
              </span>
              {!tts.hasVoiceForLanguage && language !== 'en' && (
                <span
                  title={t('ttsNoVoiceWarning', language)}
                  style={{ cursor: 'help', fontSize: '12px', color: 'var(--warning)', marginRight: '2px' }}
                >
                  ⚠️
                </span>
              )}
              {!tts.isPlaying ? (
                <button
                  onClick={() => tts.speak(buildSpeechText())}
                  className="btn-primary"
                  style={{ padding: '5px 12px', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '4px' }}
                  title={t('ttsReadAiMatching', language)}
                >
                  ▶ {t('ttsPlay', language)}
                </button>
              ) : (
                <>
                  {tts.isPaused ? (
                    <button
                      onClick={tts.resume}
                      className="btn-primary"
                      style={{ padding: '5px 12px', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '4px' }}
                      title={t('ttsResume', language)}
                    >
                      ▶ {t('ttsResume', language)}
                    </button>
                  ) : (
                    <button
                      onClick={tts.pause}
                      style={{
                        background: 'var(--accent-gold)',
                        color: '#fff',
                        border: 'none',
                        borderRadius: '6px',
                        padding: '5px 12px',
                        fontSize: '12px',
                        cursor: 'pointer',
                        fontWeight: '600'
                      }}
                      title={t('ttsPause', language)}
                    >
                      ⏸ {t('ttsPause', language)}
                    </button>
                  )}
                  <button
                    onClick={tts.stop}
                    style={{
                      background: 'var(--danger)',
                      color: '#fff',
                      border: 'none',
                      borderRadius: '6px',
                      padding: '5px 12px',
                      fontSize: '12px',
                      cursor: 'pointer',
                      fontWeight: '600'
                    }}
                    title={t('ttsStop', language)}
                  >
                    ⏹ {t('ttsStop', language)}
                  </button>
                </>
              )}
            </div>
          )}

          <button
            onClick={onGenerate}
            className="btn-primary"
            style={{
              padding: '6px 14px',
              fontSize: '12px',
              background: 'none',
              border: '1px solid var(--border)',
              color: 'var(--text-primary)',
              cursor: 'pointer'
            }}
          >
            🔄 {t('refreshAiAnalysis', language)}
          </button>
        </div>
      </div>

      {/* AI Score & Verdict Banner */}
      <div className="card matching-header">
        <div className="score-circle">
          <span className="number">
            {aiData.compatibilityPercentage ? aiData.compatibilityPercentage.toFixed(0) : '0'}%
          </span>
          <span className="label">
            {t('overallCompatibility', language)}
          </span>
        </div>
        <div className={`verdict-badge ${getVerdictClass(aiData.overallVerdict)}`}>
          {aiData.overallVerdict || 'EVALUATED'}
        </div>
        <div style={{ marginTop: '12px', fontSize: '13px', color: 'var(--text-secondary)' }}>
          ✨ {t('aiMatchingTitle', language)} • {t('cached3HourNotice', language)}
        </div>
      </div>

      {/* Executive Summary */}
      {aiData.executiveSummary && (
        <div className="card" style={{ borderLeft: '4px solid var(--accent-saffron)' }}>
          <h4 style={{ margin: '0 0 10px 0', color: 'var(--accent-saffron)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span>📜</span> {t('executiveSummary', language)}
          </h4>
          <p style={{ margin: 0, fontSize: '14px', lineHeight: '1.6', whiteSpace: 'pre-line', color: 'var(--text-primary)' }}>
            {aiData.executiveSummary}
          </p>
        </div>
      )}

      {/* 5 Domain Deep Dive Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '14px' }}>
        {domainList.map(({ key, data, icon, label }) => {
          if (!data) return null;
          return (
            <div key={key} className="card" style={{ display: 'flex', flexDirection: 'column', height: '100%', padding: '14px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px', borderBottom: '1px solid var(--border)', paddingBottom: '8px' }}>
                <h4 style={{ margin: 0, fontSize: '14px', color: 'var(--accent-saffron)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <span>{icon}</span> {label || data.title}
                </h4>
                {data.scoreOrStatus && (
                  <span style={{ fontSize: '11.5px', fontWeight: 'bold', padding: '2px 8px', borderRadius: '4px', backgroundColor: 'rgba(232, 93, 4, 0.1)', color: 'var(--accent-warm)' }}>
                    {data.scoreOrStatus}
                  </span>
                )}
              </div>
              <p style={{ margin: '0 0 10px 0', fontSize: '13px', lineHeight: '1.55', flexGrow: 1, color: 'var(--text-primary)' }}>
                {data.analysis}
              </p>
              {data.astrologicalBasis && (
                <div style={{ fontSize: '11.5px', color: 'var(--text-secondary)', backgroundColor: 'var(--bg-primary)', padding: '8px', borderRadius: '6px', border: '1px dashed var(--border)' }}>
                  <strong>{t('astrologicalBasisLabel', language)}:</strong> {data.astrologicalBasis}
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Key Strengths & Cautions in 2 Columns */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px' }}>
        {aiData.keyStrengths && aiData.keyStrengths.length > 0 && (
          <div className="card" style={{ borderLeft: '4px solid var(--success)' }}>
            <h4 style={{ margin: '0 0 12px 0', color: 'var(--success)', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span>✅</span> {t('keyStrengthsTitle', language)}
            </h4>
            <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '14px', lineHeight: '1.6', color: 'var(--text-primary)' }}>
              {aiData.keyStrengths.map((s, idx) => (
                <li key={idx} style={{ marginBottom: '6px' }}>{s}</li>
              ))}
            </ul>
          </div>
        )}

        {aiData.growthAreasAndCautions && aiData.growthAreasAndCautions.length > 0 && (
          <div className="card" style={{ borderLeft: '4px solid var(--warning)' }}>
            <h4 style={{ margin: '0 0 12px 0', color: 'var(--warning)', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span>⚠️</span> {t('cautionsTitle', language)}
            </h4>
            <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '14px', lineHeight: '1.6', color: 'var(--text-primary)' }}>
              {aiData.growthAreasAndCautions.map((c, idx) => (
                <li key={idx} style={{ marginBottom: '6px' }}>{c}</li>
              ))}
            </ul>
          </div>
        )}
      </div>

      {/* Authentic Vedic Remedies */}
      {aiData.authenticVedicRemedies && aiData.authenticVedicRemedies.length > 0 && (
        <div className="card" style={{ borderLeft: '4px solid var(--accent-gold)' }}>
          <h4 style={{ margin: '0 0 12px 0', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span>☸</span> {t('remediesTitle', language)}
          </h4>
          <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '14px', lineHeight: '1.6', color: 'var(--text-primary)' }}>
            {aiData.authenticVedicRemedies.map((r, idx) => (
              <li key={idx} style={{ marginBottom: '6px' }}>{r}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Token Usage Footer */}
      {aiData.tokenUsage && (
        <div style={{ textAlign: 'right', fontSize: '11px', color: 'var(--text-secondary)', marginTop: '10px' }}>
          ⚡ Powered by Google Gemini ({aiData.tokenUsage.modelUsed || 'gemini-3.7-flash'}) • Tokens: {aiData.tokenUsage.totalTokens} • Cost: ₹{aiData.tokenUsage.estimatedCostInr?.toFixed(2) || '0.00'}
        </div>
      )}
    </div>
  );
}

export default AiMatchingView;
