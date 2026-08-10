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
        <p style={{ maxWidth: '600px', margin: '0 auto 25px', color: 'var(--text-secondary)' }}>
          {language === 'ta'
            ? 'சுவிஸ் எபிமெரிஸ் வானியல் தரவுகள், நவாம்சம், பாவகங்கள் மற்றும் விம்சோத்தரி திசா புக்தி காலங்களின் அடிப்படையில் கடந்த கால நிகழ்வுகளின் சரிபார்ப்பு மற்றும் வருடாந்திர வாழ்நாள் பலன்களைப் பெறுங்கள்.'
            : 'Synthesize your planetary charts, D9 Navamsha, Bhavas, Shadbala, and Vimshottari Dasa timeline to generate chronological past life verification milestones and year-by-year future predictions.'}
        </p>
        <button
          onClick={onGenerate}
          className="btn-primary"
          style={{ padding: '12px 28px', fontSize: '16px' }}
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
          {language === 'ta'
            ? 'லக்னாதிபதி பலம், யோகங்கள் மற்றும் திசா புக்தி காலங்களைக் கணித்து பலன்கள் தொகுக்கப்படுகிறது...'
            : 'Analyzing Lagna lord dignity, active yogas, transits, and Dasa timeline...'}
        </p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="card" style={{ borderLeft: '4px solid var(--danger)', background: 'rgba(231, 76, 60, 0.08)' }}>
        <h4 style={{ color: 'var(--danger)', margin: '0 0 8px' }}>
          ⚠️ {language === 'ta' ? 'AI கணிப்பு சேவை கிடைக்கவில்லை' : 'AI Prediction Service Unavailable'}
        </h4>
        <p style={{ color: 'var(--text-secondary)', margin: '0 0 15px', fontSize: '14px' }}>{error}</p>
        <button onClick={onGenerate} className="btn-primary">
          🔄 {t('retry', language)}
        </button>
      </div>
    );
  }

  if (predictions && predictions.enabled === false) {
    return (
      <div className="card" style={{ borderLeft: '4px solid #e74c3c', background: 'rgba(231, 76, 60, 0.08)' }}>
        <h4 style={{ color: '#e74c3c', margin: '0 0 8px' }}>
          ⚠️ {language === 'ta' ? 'AI கணிப்பு சேவை கிடைக்கவில்லை' : 'AI Prediction Service Unavailable'}
        </h4>
        <p style={{ color: 'var(--text-secondary)', margin: '0 0 15px', fontSize: '14px' }}>
          {predictions.message || (language === 'ta' 
            ? 'AI ஜோதிட கணிப்பு சேவை தற்போது கிடைக்கவில்லை. API அமைப்புகளை சரிபார்க்கவும்.' 
            : 'AI prediction service is currently unavailable. Please verify API key configuration or network connectivity.')}
        </p>
        <button onClick={onGenerate} className="btn-primary" style={{ padding: '8px 20px' }}>
          🔄 {t('retry', language) || 'Retry'}
        </button>
      </div>
    );
  }

  const aiYogas = predictions?.aiYogas || [];
  const aiDoshams = predictions?.aiDoshams || [];
  const pastMilestones = predictions?.pastMilestones || [];
  const futurePredictions = predictions?.futurePredictions || [];

  return (
    <div>
      {/* Token Usage & Cost Analytics Badge */}
      {predictions?.tokenUsage && (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '14px',
          flexWrap: 'wrap',
          background: 'rgba(255, 215, 0, 0.06)',
          border: '1px solid rgba(255, 215, 0, 0.3)',
          borderRadius: '8px',
          padding: '10px 16px',
          marginBottom: '16px',
          fontSize: '13px',
          color: 'var(--text-secondary)'
        }}>
          <div>
            ⚡ <strong style={{ color: 'var(--accent-gold)' }}>{predictions.tokenUsage.totalTokens?.toLocaleString()}</strong> {language === 'ta' ? 'டோக்கன்கள்' : 'Tokens'}
            <span style={{ opacity: 0.75 }}> (Input: {predictions.tokenUsage.promptTokens?.toLocaleString()} | Output: {predictions.tokenUsage.completionTokens?.toLocaleString()})</span>
          </div>
          <div>
            💰 <strong style={{ color: '#2ecc71' }}>${predictions.tokenUsage.estimatedCostUsd?.toFixed(5)} USD</strong>
            <span style={{ opacity: 0.85 }}> (~₹{predictions.tokenUsage.estimatedCostInr?.toFixed(3)} INR)</span>
          </div>
          {predictions.tokenUsage.modelUsed && (
            <div>
              🤖 <span style={{ fontFamily: 'monospace', color: 'var(--text-primary)' }}>{predictions.tokenUsage.modelUsed}</span>
            </div>
          )}
        </div>
      )}

      {/* Overall Summary Card */}
      {predictions?.overallSummary && (
        <div className="card" style={{ background: 'linear-gradient(135deg, rgba(255,215,0,0.08), rgba(20,20,30,0.6))', border: '1px solid var(--accent-gold)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
            <h3 style={{ margin: 0, color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
              🌟 {t('overallSummaryTitle', language)}
            </h3>
            <button
              onClick={onGenerate}
              className="btn-primary"
              style={{ padding: '6px 14px', fontSize: '12px', background: 'none', border: '1px solid var(--border)', color: 'var(--text-primary)' }}
            >
              🔄 {t('regenerateAiBalan', language)}
            </button>
          </div>
          <p style={{ lineHeight: '1.7', fontSize: '14px', margin: 0, color: 'var(--text-primary)' }}>
            {predictions.overallSummary}
          </p>
        </div>
      )}

      {/* AI Classical Yogas */}
      {aiYogas.length > 0 && (
        <div className="card">
          <div style={{ borderBottom: '1px solid var(--border)', paddingBottom: '10px', marginBottom: '15px' }}>
            <h3 style={{ margin: '0 0 5px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
              ✨ {language === 'ta' ? 'ஜோதிட யோகங்கள் (Classical Vedic Yogas)' : 'Classical Vedic Yogas & Formations'} ({aiYogas.length})
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: 0 }}>
              {language === 'ta' ? 'சுவிஸ் எபிமெரிஸ் கிரக நிலைகள் மற்றும் சாஸ்திர விதிகளின்படி கண்டறியப்பட்ட யோகங்கள்.' : 'Major auspicious planetary combinations and Raja Yogas calculated from chart dignities.'}
            </p>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '12px' }}>
            {aiYogas.map((y, idx) => (
              <div key={idx} style={{ background: 'rgba(255, 215, 0, 0.04)', border: '1px solid rgba(255, 215, 0, 0.25)', borderRadius: '8px', padding: '12px' }}>
                <h4 style={{ margin: '0 0 6px', fontSize: '14px', color: 'var(--accent-gold)' }}>
                  👑 {y.name}
                </h4>
                {y.formingPlanets && (
                  <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    🪐 <strong>{language === 'ta' ? 'காரக கிரகங்கள்: ' : 'Forming Planets: '}</strong>{y.formingPlanets}
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

      {/* AI Doshams & Shastric Nullifications */}
      {aiDoshams.length > 0 && (
        <div className="card">
          <div style={{ borderBottom: '1px solid var(--border)', paddingBottom: '10px', marginBottom: '15px' }}>
            <h3 style={{ margin: '0 0 5px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
              🛡️ {language === 'ta' ? 'தோஷங்கள் & சாஸ்திர நிவர்த்திகள் (Doshas & Nullifications)' : 'Vedic Doshams, Nullification & Shastric Remedies'} ({aiDoshams.length})
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: 0 }}>
              {language === 'ta' ? 'தோஷங்கள், அவற்றின் சாஸ்திர நிவர்த்தி காரணங்கள் மற்றும் எளிய பரிகார வழிகாட்டுதல்.' : 'Planetary afflictions, classical cancellation rules, and authentic Vedic remedies.'}
            </p>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '12px' }}>
            {aiDoshams.map((d, idx) => {
              const isNullified = d.status && (d.status.toLowerCase().includes('nullif') || d.status.includes('நிவர்த்தி'));
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
                        {language === 'ta' ? 'நிவர்த்தி காரணம்: ' : 'Nullification Factor: '}
                      </strong>
                      {d.nullificationFactor}
                    </div>
                  )}

                  {d.remedy && (
                    <div style={{ fontSize: '12px', color: 'var(--text-secondary)', background: 'rgba(255,215,0,0.05)', padding: '6px 8px', borderRadius: '4px', lineHeight: '1.4' }}>
                      🪔 <strong>{language === 'ta' ? 'பரிகாரம்: ' : 'Remedy: '}</strong>{d.remedy}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Phase 1: Past Life Verification Milestones */}
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
              return (
                <div
                  key={idx}
                  style={{
                    background: isVerified ? 'rgba(39, 174, 96, 0.08)' : 'var(--bg-card)',
                    border: `1px solid ${isVerified ? '#27ae60' : 'var(--border)'}`,
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

      {/* Phase 2: Future Year-by-Year Predictions */}
      {futurePredictions.length > 0 && (
        <div className="card">
          <div style={{ borderBottom: '1px solid var(--border)', paddingBottom: '12px', marginBottom: '15px' }}>
            <h3 style={{ margin: '0 0 10px', color: 'var(--accent-gold)' }}>
              🔭 {t('futurePredictionsTitle', language)}
            </h3>

            {/* Filter Chips */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
              {[
                { id: 'ALL', label: t('filterAll', language) },
                { id: 'CAREER', label: `💼 ${t('filterCareer', language)}` },
                { id: 'HEALTH', label: `🌿 ${t('filterHealth', language)}` },
                { id: 'FAMILY', label: `👨‍👩‍👧 ${t('filterFamily', language)}` },
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
            {futurePredictions.map((fp, idx) => (
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

                {(activeFilter === 'ALL' || activeFilter === 'CAREER') && fp.careerFinance && (
                  <div>
                    <strong style={{ fontSize: '12px', color: 'var(--accent-gold)', display: 'block', marginBottom: '2px' }}>
                      💼 {t('filterCareer', language)}:
                    </strong>
                    <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                      {fp.careerFinance}
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

                {(activeFilter === 'ALL' || activeFilter === 'FAMILY') && fp.familyMarriage && (
                  <div>
                    <strong style={{ fontSize: '12px', color: '#e74c3c', display: 'block', marginBottom: '2px' }}>
                      👨‍👩‍👧 {t('filterFamily', language)}:
                    </strong>
                    <p style={{ fontSize: '13px', margin: 0, color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                      {fp.familyMarriage}
                    </p>
                  </div>
                )}

                {(activeFilter === 'ALL' || activeFilter === 'REMEDIES') && fp.remediesGuidance && (
                  <div style={{ background: 'rgba(255,215,0,0.05)', padding: '8px', borderRadius: '6px' }}>
                    <strong style={{ fontSize: '12px', color: 'var(--accent-gold)', display: 'block', marginBottom: '2px' }}>
                      🪔 {t('filterRemedies', language)}:
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
