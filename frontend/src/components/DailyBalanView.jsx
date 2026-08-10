import { useState } from 'react';
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
  const [selectedDate, setSelectedDate] = useState(() => {
    const today = new Date();
    return today.toISOString().split('T')[0];
  });

  if (!dailyBalan && !loading) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '40px 20px' }}>
        <div style={{ fontSize: '48px', marginBottom: '15px' }}>📅</div>
        <h3 style={{ color: 'var(--accent-gold)', marginBottom: '10px' }}>
          {t('dailyBalanTitle', language)}
        </h3>
        <p style={{ maxWidth: '650px', margin: '0 auto 20px', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
          {language === 'ta'
            ? 'இன்றைய கோச்சார கிரக நிலைகள், ராசி, நட்சத்திரம் மற்றும் விம்சோத்தரி திசா புக்தி அடிப்படையில் உங்களுக்கான தனிப்பயனாக்கப்பட்ட இன்றைய பலன்கள்.'
            : 'Personalized daily astrological forecast synthesized from today’s planetary transits (Gochara), Janma Rasi, Nakshatra, and active Vimshottari Dasa.'}
        </p>

        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '10px', marginBottom: '20px', flexWrap: 'wrap' }}>
          <label style={{ fontSize: '14px', color: 'var(--text-primary)' }}>
            📅 {t('birthDate', language)}:
          </label>
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            style={{
              padding: '8px 12px',
              borderRadius: '6px',
              border: '1px solid var(--border)',
              background: 'var(--bg-card)',
              color: 'var(--text-primary)'
            }}
          />
        </div>

        <button
          onClick={() => onGenerateDaily(selectedDate, false)}
          className="btn-primary"
          style={{ padding: '12px 28px', fontSize: '16px', display: 'inline-flex', alignItems: 'center', gap: '8px' }}
        >
          ✨ {t('generateDailyBalan', language)}
        </button>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '50px 20px' }}>
        <div className="spinner" style={{ margin: '0 auto 20px' }}></div>
        <h4 style={{ color: 'var(--accent-gold)' }}>{t('generatingDailyBalan', language)}</h4>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
          {t('calculating', language)}
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
        <button onClick={() => onGenerateDaily(selectedDate, true)} className="btn-primary">
          🔄 {t('retry', language)}
        </button>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Cache Badge & Refresh Bar */}
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
          <span>📅 <strong style={{ color: 'var(--accent-gold)' }}>{dailyBalan.targetDate || selectedDate}</strong></span>
          <span>💾 <strong style={{ color: '#2ecc71' }}>{t('cachedNoticeDaily', language)}</strong></span>
          {dailyBalan.rasi && (
            <span>🌙 {t('rashi', language)}: <strong style={{ color: 'var(--text-primary)' }}>{dailyBalan.rasi}</strong></span>
          )}
          {dailyBalan.runningDasaBhukthi && (
            <span>🪐 {t('dasaPeriod', language)}: <strong style={{ color: 'var(--text-primary)' }}>{dailyBalan.runningDasaBhukthi}</strong></span>
          )}
        </div>
        <button
          onClick={() => onGenerateDaily(selectedDate, true)}
          className="btn-primary"
          style={{
            padding: '5px 12px',
            fontSize: '12px',
            background: 'none',
            border: '1px solid var(--border)',
            color: 'var(--text-primary)'
          }}
        >
          🔄 {t('regenerateDailyBalan', language)}
        </button>
      </div>

      {/* Chandrashtama Alert if Active */}
      {dailyBalan.chandrashtama && (
        <div style={{
          background: 'rgba(231, 76, 60, 0.12)',
          border: '2px solid #e74c3c',
          borderRadius: '8px',
          padding: '16px',
          display: 'flex',
          gap: '14px',
          alignItems: 'flex-start'
        }}>
          <div style={{ fontSize: '28px', lineHeight: 1 }}>⚠️</div>
          <div>
            <h4 style={{ margin: '0 0 6px', color: '#e74c3c', fontSize: '15px', fontWeight: 'bold' }}>
              {t('chandrashtamaAlert', language)}
            </h4>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
              {t('chandrashtamaAlertDesc', language)}
            </p>
          </div>
        </div>
      )}

      {/* General Outlook Card */}
      {dailyBalan.generalOutlook && (
        <div className="card" style={{
          background: 'linear-gradient(135deg, rgba(255,215,0,0.08), rgba(20,20,30,0.7))',
          border: '1px solid var(--accent-gold)'
        }}>
          <h3 style={{ margin: '0 0 10px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            🌟 {t('generalOutlook', language)}
          </h3>
          <p style={{ lineHeight: '1.7', fontSize: '14px', margin: 0, color: 'var(--text-primary)' }}>
            {dailyBalan.generalOutlook}
          </p>
        </div>
      )}

      {/* Daily Lucky Factors Bar */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
        gap: '12px'
      }}>
        {dailyBalan.luckyColor && (
          <div style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px', textAlign: 'center' }}>
            <span style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
              🎨 {t('luckyColor', language)}
            </span>
            <strong style={{ fontSize: '14px', color: 'var(--accent-gold)' }}>{dailyBalan.luckyColor}</strong>
          </div>
        )}
        {dailyBalan.luckyNumber && (
          <div style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px', textAlign: 'center' }}>
            <span style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
              🔢 {t('luckyNumber', language)}
            </span>
            <strong style={{ fontSize: '14px', color: '#2ecc71' }}>{dailyBalan.luckyNumber}</strong>
          </div>
        )}
        {dailyBalan.favorableDirection && (
          <div style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px', textAlign: 'center' }}>
            <span style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
              🧭 {t('favorableDirection', language)}
            </span>
            <strong style={{ fontSize: '14px', color: '#3498db' }}>{dailyBalan.favorableDirection}</strong>
          </div>
        )}
        {dailyBalan.bestTimeWindow && (
          <div style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px', textAlign: 'center' }}>
            <span style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
              ⏰ {t('bestTimeWindow', language)}
            </span>
            <strong style={{ fontSize: '14px', color: 'var(--accent-gold)' }}>{dailyBalan.bestTimeWindow}</strong>
          </div>
        )}
      </div>

      {/* 4 Pillars of Daily Life */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '15px' }}>
        {dailyBalan.careerWork && (
          <div className="card">
            <h4 style={{ margin: '0 0 8px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '14px' }}>
              💼 {t('careerWork', language)}
            </h4>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
              {dailyBalan.careerWork}
            </p>
          </div>
        )}

        {dailyBalan.financeWealth && (
          <div className="card">
            <h4 style={{ margin: '0 0 8px', color: '#f39c12', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '14px' }}>
              💰 {t('financeWealth', language)}
            </h4>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
              {dailyBalan.financeWealth}
            </p>
          </div>
        )}

        {dailyBalan.healthVitality && (
          <div className="card">
            <h4 style={{ margin: '0 0 8px', color: '#2ecc71', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '14px' }}>
              🌿 {t('healthVitality', language)}
            </h4>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
              {dailyBalan.healthVitality}
            </p>
          </div>
        )}

        {dailyBalan.relationshipFamily && (
          <div className="card">
            <h4 style={{ margin: '0 0 8px', color: '#e74c3c', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '14px' }}>
              👨‍👩‍👧 {t('relationshipFamily', language)}
            </h4>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
              {dailyBalan.relationshipFamily}
            </p>
          </div>
        )}
      </div>

      {/* Daily Vedic Remedy */}
      {dailyBalan.dailyRemedy && (
        <div className="card" style={{ background: 'rgba(255, 215, 0, 0.04)', border: '1px solid rgba(255, 215, 0, 0.3)' }}>
          <h4 style={{ margin: '0 0 8px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '6px' }}>
            🪔 {t('dailyRemedy', language)}
          </h4>
          <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-primary)', lineHeight: '1.6' }}>
            {dailyBalan.dailyRemedy}
          </p>
        </div>
      )}
    </div>
  );
}

export default DailyBalanView;
