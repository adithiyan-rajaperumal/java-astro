import React from 'react';
import { t } from '../i18n/translations';

export default function HealthLongevityView({ chartData, language }) {
  if (!chartData) return null;

  const health = chartData.ayurvedicHealth;
  const ayurdaya = chartData.ayurdayaProfile;

  // Fallbacks if backend hasn't computed yet
  const vataPct = health?.doshaPercentages?.Vata || 33;
  const pittaPct = health?.doshaPercentages?.Pitta || 34;
  const kaphaPct = health?.doshaPercentages?.Kapha || 33;

  const isPoornayu = ayurdaya?.longevityClassification === 'Poornayu';
  const isMadhyayu = ayurdaya?.longevityClassification === 'Madhyayu';

  const longevityBadgeBg = isPoornayu
    ? 'rgba(46, 204, 113, 0.15)'
    : isMadhyayu
    ? 'rgba(241, 196, 15, 0.15)'
    : 'rgba(231, 76, 60, 0.15)';

  const longevityBadgeColor = isPoornayu
    ? '#2ecc71'
    : isMadhyayu
    ? '#f1c40f'
    : '#e74c3c';

  const longevityBadgeBorder = isPoornayu
    ? 'rgba(46, 204, 113, 0.3)'
    : isMadhyayu
    ? 'rgba(241, 196, 15, 0.3)'
    : 'rgba(231, 76, 60, 0.3)';

  const classificationText = isPoornayu
    ? t('poornayu', language)
    : isMadhyayu
    ? t('madhyayu', language)
    : t('alpayu', language);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>

      {/* 1. AYURDAYA LONGEVITY HERO CARD */}
      <div className="card" style={{ background: 'linear-gradient(135deg, rgba(255,255,255,0.03) 0%, rgba(212,175,55,0.04) 100%)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '10px', marginBottom: '16px' }}>
          <h3 style={{ margin: 0, color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            🪐 {t('longevityTitle', language)}
          </h3>
          <span style={{
            fontSize: '13px',
            fontWeight: 'bold',
            padding: '6px 14px',
            borderRadius: '20px',
            background: longevityBadgeBg,
            color: longevityBadgeColor,
            border: `1px solid ${longevityBadgeBorder}`,
            boxShadow: '0 2px 8px rgba(0,0,0,0.2)'
          }}>
            ✨ {classificationText}
          </span>
        </div>

        {/* Longevity Key Stats */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '12px', marginBottom: '18px' }}>
          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              🎯 {t('longevityCeiling', language)}
            </div>
            <div style={{ fontSize: '18px', fontWeight: 'bold', color: 'var(--accent-gold)' }}>
              {ayurdaya?.estimatedLifespanCeiling ? `~${ayurdaya.estimatedLifespanCeiling} ${language === 'ta' ? 'வயது' : 'Years'}` : '75 - 90+ Years'}
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              📜 {t('classicalRationale', language)}
            </div>
            <div style={{ fontSize: '13px', color: 'var(--text-primary)', lineHeight: '1.4' }}>
              {ayurdaya?.lifespanRange || 'Brihat Parashara & Jaimini Sutras'}
            </div>
          </div>
        </div>

        {/* 3-Pair Modality Table */}
        {ayurdaya?.threePairsDetails && Object.keys(ayurdaya.threePairsDetails).length > 0 && (
          <div style={{ marginBottom: '16px' }}>
            <h4 style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '8px' }}>
              📊 {t('threePairsTitle', language)}
            </h4>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '10px' }}>
              {Object.entries(ayurdaya.threePairsDetails).map(([pairKey, detail], idx) => (
                <div key={idx} style={{ background: 'rgba(255,255,255,0.015)', border: '1px solid var(--border)', borderRadius: '6px', padding: '10px', fontSize: '12px' }}>
                  <strong style={{ color: 'var(--accent-gold)', display: 'block', marginBottom: '4px' }}>
                    {pairKey}
                  </strong>
                  <div style={{ color: 'var(--text-primary)' }}>
                    {String(detail)}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Kakshya Vriddhi Adjustments */}
        {ayurdaya?.kakshyaAdjustments && ayurdaya.kakshyaAdjustments.length > 0 && (
          <div style={{ marginBottom: '14px' }}>
            <h4 style={{ fontSize: '12px', color: 'var(--accent-gold)', marginBottom: '6px' }}>
              ✨ {t('kakshyaAdjustments', language)}:
            </h4>
            <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
              {ayurdaya.kakshyaAdjustments.map((adj, i) => (
                <li key={i} style={{ marginBottom: '4px' }}>{adj}</li>
              ))}
            </ul>
          </div>
        )}

        {/* Maraka Caution Period */}
        {ayurdaya?.criticalMarakaWindow && (
          <div style={{ background: 'rgba(230, 126, 34, 0.08)', border: '1px solid rgba(230, 126, 34, 0.3)', borderRadius: '6px', padding: '10px' }}>
            <strong style={{ fontSize: '12px', color: '#e67e22', display: 'block', marginBottom: '4px' }}>
              ⚠️ {t('marakaPeriods', language)}:
            </strong>
            <p style={{ fontSize: '12px', margin: 0, color: 'var(--text-primary)', lineHeight: '1.4' }}>
              {ayurdaya.criticalMarakaWindow}
            </p>
          </div>
        )}
      </div>

      {/* 2. AYURVEDIC CONSTITUTION (PRAKRITI) & DOSHA BREAKDOWN */}
      <div className="card">
        <h3 style={{ margin: '0 0 16px 0', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
          🌿 {t('prakritiTitle', language)}
        </h3>

        {/* Prakriti Overview Badges */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px', marginBottom: '18px' }}>
          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
            <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
              🧬 {t('dominantPrakriti', language)}
            </span>
            <strong style={{ fontSize: '15px', color: '#3498db' }}>
              {health?.dominantPrakriti || 'Vata-Pitta'}
            </strong>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
            <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
              🔥 {t('lagnaElement', language)}
            </span>
            <strong style={{ fontSize: '15px', color: '#e67e22' }}>
              {health?.lagnaElement || 'Agni (Fire)'}
            </strong>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
            <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
              🏥 {t('rogaSthana', language)}
            </span>
            <strong style={{ fontSize: '14px', color: 'var(--text-primary)' }}>
              {health?.rogaSthanaSign ? `${health.rogaSthanaSign} (${health.rogaLord || ''})` : 'Kanya'}
            </strong>
          </div>
        </div>

        {/* Visual Dosha Proportion Progress Bars */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '20px' }}>
          {/* VATA BAR */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
              <span style={{ color: '#3498db', fontWeight: 'bold' }}>💨 {t('vata', language)}</span>
              <span style={{ color: '#3498db', fontWeight: 'bold' }}>{vataPct}%</span>
            </div>
            <div style={{ width: '100%', height: '8px', background: 'rgba(255,255,255,0.08)', borderRadius: '4px', overflow: 'hidden' }}>
              <div style={{ width: `${vataPct}%`, height: '100%', background: 'linear-gradient(90deg, #2980b9, #3498db)', borderRadius: '4px', transition: 'width 0.6s ease' }} />
            </div>
          </div>

          {/* PITTA BAR */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
              <span style={{ color: '#e74c3c', fontWeight: 'bold' }}>🔥 {t('pitta', language)}</span>
              <span style={{ color: '#e74c3c', fontWeight: 'bold' }}>{pittaPct}%</span>
            </div>
            <div style={{ width: '100%', height: '8px', background: 'rgba(255,255,255,0.08)', borderRadius: '4px', overflow: 'hidden' }}>
              <div style={{ width: `${pittaPct}%`, height: '100%', background: 'linear-gradient(90deg, #c0392b, #e74c3c)', borderRadius: '4px', transition: 'width 0.6s ease' }} />
            </div>
          </div>

          {/* KAPHA BAR */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
              <span style={{ color: '#2ecc71', fontWeight: 'bold' }}>💧 {t('kapha', language)}</span>
              <span style={{ color: '#2ecc71', fontWeight: 'bold' }}>{kaphaPct}%</span>
            </div>
            <div style={{ width: '100%', height: '8px', background: 'rgba(255,255,255,0.08)', borderRadius: '4px', overflow: 'hidden' }}>
              <div style={{ width: `${kaphaPct}%`, height: '100%', background: 'linear-gradient(90deg, #27ae60, #2ecc71)', borderRadius: '4px', transition: 'width 0.6s ease' }} />
            </div>
          </div>
        </div>

        {/* 3. ORGAN VULNERABILITIES & DIETARY DIRECTIVES */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '16px' }}>

          {/* Organ Vulnerabilities */}
          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <h4 style={{ margin: '0 0 10px 0', fontSize: '13px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '6px' }}>
              🩺 {t('organVulnerabilitiesTitle', language)}
            </h4>
            {health?.calculatedOrganVulnerabilities && health.calculatedOrganVulnerabilities.length > 0 ? (
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                {health.calculatedOrganVulnerabilities.map((organ, idx) => (
                  <span key={idx} style={{
                    fontSize: '12px',
                    padding: '4px 10px',
                    borderRadius: '12px',
                    background: 'rgba(231, 76, 60, 0.1)',
                    color: '#e74c3c',
                    border: '1px solid rgba(231, 76, 60, 0.25)'
                  }}>
                    • {organ}
                  </span>
                ))}
              </div>
            ) : (
              <p style={{ fontSize: '12px', color: 'var(--text-secondary)', margin: 0 }}>
                {language === 'ta' ? 'குறிப்பிடத்தக்க பாதிப்புகள் இல்லை' : 'No acute organ vulnerability detected.'}
              </p>
            )}
          </div>

          {/* Ayurvedic Diet & Lifestyle Directives */}
          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <h4 style={{ margin: '0 0 10px 0', fontSize: '13px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '6px' }}>
              🥗 {t('dietLifestyleTitle', language)}
            </h4>
            {health?.dietaryAndLifestyleDirectives && health.dietaryAndLifestyleDirectives.length > 0 ? (
              <ul style={{ margin: 0, paddingLeft: '18px', fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
                {health.dietaryAndLifestyleDirectives.map((directive, idx) => (
                  <li key={idx} style={{ marginBottom: '4px' }}>{directive}</li>
                ))}
              </ul>
            ) : (
              <p style={{ fontSize: '12px', color: 'var(--text-secondary)', margin: 0 }}>
                {language === 'ta' ? 'சமச்சீர் சாத்விக உணவு பரிந்துரைக்கப்படுகிறது.' : 'Balanced Sattvic diet recommended.'}
              </p>
            )}
          </div>

        </div>

      </div>

    </div>
  );
}
