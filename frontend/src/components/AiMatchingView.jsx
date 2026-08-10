import React from 'react';
import { t } from '../i18n/translations';

function AiMatchingView({ aiData, loading, onGenerate, language }) {
  if (loading) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '40px 20px' }}>
        <div className="spinner" style={{ margin: '0 auto 15px auto' }}></div>
        <h4 style={{ color: 'var(--accent-saffron)', margin: '0 0 10px 0' }}>
          {t('analyzingCompatibility', language)}
        </h4>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px', maxWidth: '500px', margin: '0 auto' }}>
          Evaluating planetary alignments, D9 Navamsa harmony, Kuja Dosha nullifications, Nadi/Gana balance, and long-term marital synergy...
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
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px', maxWidth: '600px', margin: '0 auto 20px auto' }}>
          Unlock a comprehensive, AI-powered Vedic compatibility analysis synthesizing dual-horoscope planetary configurations, Navamsa (D9) harmony, Dosha nullifications, and authentic remedies.
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

  return (
    <div>
      {/* AI Score & Verdict Banner */}
      <div className="card matching-header" style={{ marginBottom: '20px' }}>
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
          ✨ {t('aiMatchingTitle', language)} • 3-Hour Cached Analysis
        </div>
        <button
          onClick={onGenerate}
          className="btn-primary"
          style={{ marginTop: '15px', background: 'none', border: '1px solid var(--border)', color: 'var(--text-primary)', padding: '6px 14px', fontSize: '12px' }}
        >
          🔄 Refresh AI Analysis
        </button>
      </div>

      {/* Executive Summary */}
      {aiData.executiveSummary && (
        <div className="card" style={{ borderLeft: '4px solid var(--accent-saffron)' }}>
          <h4 style={{ margin: '0 0 10px 0', color: 'var(--accent-saffron)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span>📜</span> {t('executiveSummary', language)}
          </h4>
          <p style={{ margin: 0, fontSize: '15px', lineHeight: '1.7', whiteSpace: 'pre-line', color: 'var(--text-primary)' }}>
            {aiData.executiveSummary}
          </p>
        </div>
      )}

      {/* 5 Domain Deep Dive Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '16px', marginBottom: '20px' }}>
        {domainList.map(({ key, data, icon, label }) => {
          if (!data) return null;
          return (
            <div key={key} className="card" style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px', borderBottom: '1px solid var(--border)', paddingBottom: '8px' }}>
                <h4 style={{ margin: 0, fontSize: '15px', color: 'var(--accent-saffron)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <span>{icon}</span> {label || data.title}
                </h4>
                {data.scoreOrStatus && (
                  <span style={{ fontSize: '12px', fontWeight: 'bold', padding: '2px 8px', borderRadius: '4px', backgroundColor: 'rgba(232, 93, 4, 0.1)', color: 'var(--accent-warm)' }}>
                    {data.scoreOrStatus}
                  </span>
                )}
              </div>
              <p style={{ margin: '0 0 10px 0', fontSize: '14px', lineHeight: '1.6', flexGrow: 1, color: 'var(--text-primary)' }}>
                {data.analysis}
              </p>
              {data.astrologicalBasis && (
                <div style={{ fontSize: '12px', color: 'var(--text-secondary)', backgroundColor: 'var(--bg-primary)', padding: '8px', borderRadius: '6px', border: '1px dashed var(--border)' }}>
                  <strong>Astrological Basis:</strong> {data.astrologicalBasis}
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Key Strengths & Cautions in 2 Columns */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px', marginBottom: '20px' }}>
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
          ⚡ Powered by Google Gemini ({aiData.tokenUsage.modelUsed || 'gemini-3.6-flash'}) • Tokens: {aiData.tokenUsage.totalTokens} • Cost: ₹{aiData.tokenUsage.estimatedCostInr?.toFixed(2) || '0.00'}
        </div>
      )}
    </div>
  );
}

export default AiMatchingView;
