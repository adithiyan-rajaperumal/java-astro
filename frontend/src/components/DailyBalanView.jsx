import { t } from '../i18n/translations';

function DailyBalanView({
  report,
  formPayload,
  language = 'en',
  onGenerateDaily,
  dailyBalan,
  loading,
  error
}) {
  const todayStr = new Date().toISOString().split('T')[0];

  if (!dailyBalan && !loading) {
    return (
      <div className="card" style={{
        textAlign: 'center',
        padding: '40px 20px',
        background: 'var(--bg-card)',
        border: '1px solid var(--border)',
        borderRadius: '12px'
      }}>
        <h3 style={{ color: 'var(--accent-saffron, #d35400)', marginBottom: '12px', fontSize: '1.4rem' }}>
          ✨ {t('dailyBalanTitle', language)}
        </h3>
        <p style={{ maxWidth: '680px', margin: '0 auto 25px', color: 'var(--text-secondary)', lineHeight: '1.7', fontSize: '15px' }}>
          {t('dailyBalanSubtitle', language)}
        </p>

        <button
          onClick={() => onGenerateDaily(todayStr, false)}
          className="btn-primary"
          style={{
            padding: '12px 32px',
            fontSize: '15px',
            fontWeight: '600',
            display: 'inline-flex',
            alignItems: 'center',
            gap: '8px',
            cursor: 'pointer',
            borderRadius: '8px'
          }}
        >
          ✨ {t('generateDailyBalan', language)}
        </button>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="card" style={{
        textAlign: 'center',
        padding: '50px 20px',
        background: 'var(--bg-card)',
        border: '1px solid var(--border)',
        borderRadius: '12px'
      }}>
        <div className="spinner" style={{ margin: '0 auto 20px' }}></div>
        <h4 style={{ color: 'var(--accent-saffron, #d35400)', fontSize: '1.2rem', marginBottom: '8px' }}>
          {t('generatingDailyBalan', language)}
        </h4>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px', margin: 0 }}>
          {t('calculating', language)}
        </p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="card" style={{
        borderLeft: '4px solid var(--danger, #e74c3c)',
        background: 'rgba(231, 76, 60, 0.05)',
        border: '1px solid var(--border)',
        borderRadius: '8px',
        padding: '20px'
      }}>
        <h4 style={{ color: 'var(--danger, #e74c3c)', margin: '0 0 8px', fontSize: '1.1rem' }}>
          ⚠️ {t('calculationFaulted', language)}
        </h4>
        <p style={{ color: 'var(--text-secondary)', margin: '0 0 15px', fontSize: '14px' }}>{error}</p>
        <button onClick={() => onGenerateDaily(todayStr, true)} className="btn-primary" style={{ padding: '8px 20px' }}>
          🔄 {t('retry', language)}
        </button>
      </div>
    );
  }

  if (dailyBalan && dailyBalan.enabled === false) {
    return (
      <div className="card" style={{
        borderLeft: '4px solid var(--danger, #e74c3c)',
        background: 'rgba(231, 76, 60, 0.05)',
        border: '1px solid var(--border)',
        borderRadius: '8px',
        padding: '20px'
      }}>
        <h4 style={{ color: 'var(--danger, #e74c3c)', margin: '0 0 8px', fontSize: '1.1rem' }}>
          ⚠️ {t('calculationFaulted', language)}
        </h4>
        <p style={{ color: 'var(--text-secondary)', margin: '0 0 15px', fontSize: '14px' }}>
          {dailyBalan.message || t('aiPredictionUnavailable', language)}
        </p>
        <button onClick={() => onGenerateDaily(todayStr, true)} className="btn-primary" style={{ padding: '8px 20px' }}>
          🔄 {t('retry', language)}
        </button>
      </div>
    );
  }

  // Synthesize daily narrative fallback if direct field is empty
  const narrativeText = dailyBalan.dailyNarrative || [
    dailyBalan.generalOutlook,
    dailyBalan.careerWork,
    dailyBalan.financeWealth,
    dailyBalan.healthVitality,
    dailyBalan.relationshipFamily
  ].filter(Boolean).join(' ');

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* 1. Status & Metadata Bar */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '12px',
        background: 'var(--bg-card, #ffffff)',
        border: '1px solid var(--border, #f0e2d0)',
        borderRadius: '10px',
        padding: '12px 18px',
        fontSize: '13px',
        color: 'var(--text-secondary)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px', flexWrap: 'wrap' }}>
          <span>📅 <strong style={{ color: 'var(--accent-saffron, #d35400)' }}>{dailyBalan.targetDate || todayStr}</strong></span>
          <span style={{
            background: 'rgba(46, 204, 113, 0.12)',
            color: '#27ae60',
            padding: '2px 8px',
            borderRadius: '4px',
            fontWeight: '600'
          }}>
            💾 {t('cachedNoticeDaily', language)}
          </span>
          {dailyBalan.rasi && (
            <span>🌙 {t('rashi', language)}: <strong style={{ color: 'var(--text-primary)' }}>{dailyBalan.rasi}</strong></span>
          )}
          {dailyBalan.nakshatra && (
            <span>⭐ {t('nakshatra', language)}: <strong style={{ color: 'var(--text-primary)' }}>{dailyBalan.nakshatra}</strong></span>
          )}
          {dailyBalan.runningDasaBhukthi && (
            <span>🪐 {t('dasaPeriod', language)}: <strong style={{ color: 'var(--text-primary)' }}>{dailyBalan.runningDasaBhukthi}</strong></span>
          )}
          {dailyBalan.tokenUsage && (
            <span>⚡ <strong>{dailyBalan.tokenUsage.totalTokens?.toLocaleString()}</strong> {t('tokensCount', language)}</span>
          )}
        </div>
        <button
          onClick={() => onGenerateDaily(todayStr, true)}
          className="btn-primary"
          style={{
            padding: '6px 14px',
            fontSize: '13px',
            background: 'none',
            border: '1px solid var(--border, #f0e2d0)',
            color: 'var(--text-primary)',
            cursor: 'pointer',
            borderRadius: '6px'
          }}
        >
          🔄 {t('regenerateDailyBalan', language)}
        </button>
      </div>

      {/* 2. Chandrashtama Alert Banner (Conditional) */}
      {dailyBalan.chandrashtama && (
        <div style={{
          background: 'rgba(231, 76, 60, 0.08)',
          border: '1px solid rgba(231, 76, 60, 0.35)',
          borderLeft: '4px solid #e74c3c',
          borderRadius: '8px',
          padding: '16px 20px',
          display: 'flex',
          gap: '14px',
          alignItems: 'flex-start'
        }}>
          <div style={{ fontSize: '24px', lineHeight: 1 }}>⚠️</div>
          <div>
            <h4 style={{ margin: '0 0 6px', color: '#c0392b', fontSize: '15px', fontWeight: 'bold' }}>
              {t('chandrashtamaAlert', language)}
            </h4>
            <p style={{ margin: 0, fontSize: '13.5px', color: 'var(--text-primary)', lineHeight: '1.6' }}>
              {t('chandrashtamaAlertDesc', language)}
            </p>
          </div>
        </div>
      )}

      {/* 3. Comprehensive Daily Forecast Paragraph */}
      {narrativeText && (
        <div className="card" style={{
          background: 'var(--bg-card, #ffffff)',
          border: '1px solid var(--border, #f0e2d0)',
          borderRadius: '12px',
          padding: '24px'
        }}>
          <h3 style={{
            margin: '0 0 14px',
            color: 'var(--accent-saffron, #d35400)',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            fontSize: '1.15rem',
            fontWeight: '600'
          }}>
            ✨ {t('dailyForecastParagraphTitle', language)}
          </h3>
          <p style={{
            lineHeight: '1.85',
            fontSize: '14.5px',
            margin: 0,
            color: 'var(--text-primary)',
            textAlign: 'justify'
          }}>
            {narrativeText}
          </p>
        </div>
      )}

      {/* 4. Daily Auspicious Factors Bar */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
        gap: '14px'
      }}>
        {dailyBalan.luckyColor && (
          <div style={{
            background: 'var(--bg-card, #ffffff)',
            border: '1px solid var(--border, #f0e2d0)',
            borderRadius: '10px',
            padding: '14px 16px',
            textAlign: 'center'
          }}>
            <span style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px', fontWeight: '500' }}>
              🎨 {t('luckyColor', language)}
            </span>
            <strong style={{ fontSize: '14px', color: 'var(--accent-saffron, #d35400)' }}>
              {dailyBalan.luckyColor}
            </strong>
          </div>
        )}
        {dailyBalan.luckyNumber && (
          <div style={{
            background: 'var(--bg-card, #ffffff)',
            border: '1px solid var(--border, #f0e2d0)',
            borderRadius: '10px',
            padding: '14px 16px',
            textAlign: 'center'
          }}>
            <span style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px', fontWeight: '500' }}>
              🔢 {t('luckyNumber', language)}
            </span>
            <strong style={{ fontSize: '14px', color: '#27ae60' }}>
              {dailyBalan.luckyNumber}
            </strong>
          </div>
        )}
        {dailyBalan.favorableDirection && (
          <div style={{
            background: 'var(--bg-card, #ffffff)',
            border: '1px solid var(--border, #f0e2d0)',
            borderRadius: '10px',
            padding: '14px 16px',
            textAlign: 'center'
          }}>
            <span style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px', fontWeight: '500' }}>
              🧭 {t('favorableDirection', language)}
            </span>
            <strong style={{ fontSize: '14px', color: '#2980b9' }}>
              {dailyBalan.favorableDirection}
            </strong>
          </div>
        )}
        {dailyBalan.bestTimeWindow && (
          <div style={{
            background: 'var(--bg-card, #ffffff)',
            border: '1px solid var(--border, #f0e2d0)',
            borderRadius: '10px',
            padding: '14px 16px',
            textAlign: 'center'
          }}>
            <span style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px', fontWeight: '500' }}>
              ⏰ {t('bestTimeWindow', language)}
            </span>
            <strong style={{ fontSize: '14px', color: 'var(--accent-gold, #b8860b)' }}>
              {dailyBalan.bestTimeWindow}
            </strong>
          </div>
        )}
      </div>

      {/* 5. Daily Vedic Remedy Box */}
      {dailyBalan.dailyRemedy && (
        <div className="card" style={{
          background: 'var(--bg-primary, #fffaf4)',
          border: '1px solid var(--accent-saffron, rgba(211, 84, 0, 0.25))',
          borderRadius: '12px',
          padding: '18px 22px'
        }}>
          <h4 style={{
            margin: '0 0 10px',
            color: 'var(--accent-saffron, #d35400)',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            fontSize: '1rem',
            fontWeight: '600'
          }}>
            🪔 {t('dailyRemedy', language)}
          </h4>
          <p style={{
            margin: 0,
            fontSize: '14px',
            color: 'var(--text-primary)',
            lineHeight: '1.7'
          }}>
            {dailyBalan.dailyRemedy}
          </p>
        </div>
      )}
    </div>
  );
}

export default DailyBalanView;
