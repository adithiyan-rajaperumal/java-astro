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
      <div className="card" style={{ borderLeft: '4px solid var(--danger)' }}>
        <h4 style={{ color: 'var(--danger)' }}>Error Generating AI Balan</h4>
        <p>{error}</p>
        <button onClick={onGenerate} className="btn-primary" style={{ marginTop: '10px' }}>
          {t('retry', language)}
        </button>
      </div>
    );
  }

  const pastMilestones = predictions?.pastMilestones || [];
  const futurePredictions = predictions?.futurePredictions || [];

  return (
    <div>
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
