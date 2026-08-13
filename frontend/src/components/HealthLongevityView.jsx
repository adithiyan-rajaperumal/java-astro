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

  const formatPairTitle = (key) => {
    switch (key) {
      case 'pair1_lagnaLord_and_8thLord':
        return language === 'ta' ? '1. லக்னாதிபதி & 8-ஆம் அதிபதி' : '1. Lagna Lord & 8th Lord';
      case 'pair2_moon_and_saturn':
        return language === 'ta' ? '2. சந்திரன் & ஆயுள்காரகன் சனி' : '2. Moon & Saturn (Ayushkaraka)';
      case 'pair3_lagna_and_moon':
        return language === 'ta' ? '3. லக்னம் & சந்திரன்' : '3. Lagna & Moon';
      case 'majorityConsensus':
        return language === 'ta' ? 'பெரும்பான்மை முடிவு' : 'Majority Consensus';
      default:
        return key.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
    }
  };

  const translateModality = (text) => {
    if (!text || language !== 'ta') return text;
    return text
      .replace(/CHARA/g, 'சர')
      .replace(/STHIRA/g, 'ஸ்திர')
      .replace(/DWISVABHAVA/g, 'உபய')
      .replace(/Lagna/g, 'லக்னம்')
      .replace(/Moon/g, 'சந்திரன்')
      .replace(/Saturn/g, 'சனி')
      .replace(/Sun/g, 'சூரியன்')
      .replace(/Mars/g, 'செவ்வாய்')
      .replace(/Mercury/g, 'புதன்')
      .replace(/Jupiter/g, 'குரு')
      .replace(/Venus/g, 'சுக்கிரன்')
      .replace(/Rahu/g, 'ராகு')
      .replace(/Ketu/g, 'கேது');
  };

  const renderSpanBadge = (span) => {
    if (!span) return null;
    const isP = span === 'Poornayu';
    const isM = span === 'Madhyayu';
    const bg = isP ? 'rgba(46, 204, 113, 0.15)' : isM ? 'rgba(241, 196, 15, 0.15)' : 'rgba(231, 76, 60, 0.15)';
    const col = isP ? '#2ecc71' : isM ? '#f1c40f' : '#e74c3c';
    const border = isP ? 'rgba(46, 204, 113, 0.3)' : isM ? 'rgba(241, 196, 15, 0.3)' : 'rgba(231, 76, 60, 0.3)';
    const text = isP ? t('poornayu', language) : isM ? t('madhyayu', language) : t('alpayu', language);

    return (
      <span style={{
        fontSize: '11px',
        fontWeight: 'bold',
        padding: '3px 9px',
        borderRadius: '12px',
        background: bg,
        color: col,
        border: `1px solid ${border}`,
        whiteSpace: 'nowrap'
      }}>
        {text}
      </span>
    );
  };

  const translatePrakriti = (prakriti) => {
    if (!prakriti || language !== 'ta') return prakriti;
    const map = {
      'Kapha-Pitta': 'கப-பித்தம் (Kapha-Pitta)',
      'Vata-Pitta': 'வாத-பித்தம் (Vata-Pitta)',
      'Pitta-Vata': 'பித்த-வாதம் (Pitta-Vata)',
      'Pitta-Kapha': 'பித்த-கபம் (Pitta-Kapha)',
      'Vata-Kapha': 'வாத-கபம் (Vata-Kapha)',
      'Kapha-Vata': 'கப-வாதம் (Kapha-Vata)',
      'Pitta Dominant': 'பித்த பிரதானம் (Pitta Dominant)',
      'Vata Dominant': 'வாத பிரதானம் (Vata Dominant)',
      'Kapha Dominant': 'கப பிரதானம் (Kapha Dominant)'
    };
    return map[prakriti] || prakriti;
  };

  const translateLagnaElement = (elem) => {
    if (!elem || language !== 'ta') return elem;
    return elem
      .replace(/Agni \(Fire\)/g, 'அக்னி (நெருப்பு / Fire)')
      .replace(/Prithvi \(Earth\)/g, 'பிருத்வி (பூமி / Earth)')
      .replace(/Vayu \(Air\)/g, 'வாயு (காற்று / Air)')
      .replace(/Jala \(Water\)/g, 'ஜலம் (நீர் / Water)')
      .replace(/Mesha/g, 'மேஷம்')
      .replace(/Vrishabha/g, 'ரிஷபம்')
      .replace(/Mithuna/g, 'மிதுனம்')
      .replace(/Kataka/g, 'கடகம்')
      .replace(/Simha/g, 'சிம்மம்')
      .replace(/Kanya/g, 'கன்னி')
      .replace(/Tula/g, 'துலாம்')
      .replace(/Vrishchika/g, 'விருச்சிகம்')
      .replace(/Dhanus/g, 'தனுசு')
      .replace(/Makara/g, 'மகரம்')
      .replace(/Kumbha/g, 'கும்பம்')
      .replace(/Meena/g, 'மீனம்');
  };

  const translateRogaSthana = (sign, lord) => {
    if (!sign) return '';
    if (language !== 'ta') return `${sign} (${lord || ''})`;
    let translatedSign = sign
      .replace(/Mesha/g, 'மேஷம்')
      .replace(/Vrishabha/g, 'ரிஷபம்')
      .replace(/Mithuna/g, 'மிதுனம்')
      .replace(/Kataka/g, 'கடகம்')
      .replace(/Simha/g, 'சிம்மம்')
      .replace(/Kanya/g, 'கன்னி')
      .replace(/Tula/g, 'துலாம்')
      .replace(/Vrishchika/g, 'விருச்சிகம்')
      .replace(/Dhanus/g, 'தனுசு')
      .replace(/Makara/g, 'மகரம்')
      .replace(/Kumbha/g, 'கும்பம்')
      .replace(/Meena/g, 'மீனம்')
      .replace(/\(House 6\)/g, '(6-ஆம் பாவகம்)');

    let translatedLord = (lord || '')
      .replace(/Sun/gi, 'சூரியன்')
      .replace(/Moon/gi, 'சந்திரன்')
      .replace(/Mars/gi, 'செவ்வாய்')
      .replace(/Mercury/gi, 'புதன்')
      .replace(/Jupiter/gi, 'குரு')
      .replace(/Venus/gi, 'சுக்கிரன்')
      .replace(/Saturn/gi, 'சனி')
      .replace(/Rahu/gi, 'ராகு')
      .replace(/Ketu/gi, 'கேது');

    return `${translatedSign} (${translatedLord})`;
  };

  const translateOrganVulnerability = (text) => {
    if (!text || language !== 'ta') return text;

    // 12 Rashi in 6th house matches
    if (text.includes('Mesha / Aries in 6th')) {
      return 'தலைப்பகுதி, மூளை இரத்த ஓட்டம் மற்றும் அழற்சி தலைவலி உணர்திறன் (மேஷம் / 6-ஆம் பாவகம்)';
    }
    if (text.includes('Vrishabha / Taurus in 6th')) {
      return 'தொண்டை, குரல்வளை, தைராய்டு சுரப்பி மற்றும் முக திசு உணர்திறன் (ரிஷபம் / 6-ஆம் பாவகம்)';
    }
    if (text.includes('Mithuna / Gemini in 6th')) {
      return 'சுவாசக்குழாய், நரம்பு மண்டலம் மற்றும் தோள்பட்டை/கை நரம்பு இறுக்கம் (மிதுனம் / 6-ஆம் பாவகம்)';
    }
    if (text.includes('Kataka / Cancer in 6th')) {
      return 'நெஞ்சு/இரைப்பை செரிமானம், சளி சவ்வு மற்றும் உணர்ச்சி சார்ந்த மன அழுத்தம் (கடகம் / 6-ஆம் பாவகம்)';
    }
    if (text.includes('Simha / Leo in 6th')) {
      return 'மேல் வயிறு, ஜாடராக்னி (செரிமான தீ) மற்றும் இதய சுற்றோட்ட பலம் (சிம்மம் / 6-ஆம் பாவகம்)';
    }
    if (text.includes('Kanya / Virgo in 6th')) {
      return 'குடல் பகுதி, சத்து உறிஞ்சுதல் மற்றும் குடல் நுண்ணுயிரி சமநிலை (கன்னி / 6-ஆம் பாவகம்)';
    }
    if (text.includes('Tula / Libra in 6th')) {
      return 'சிறுநீரக மண்டலம், இடுப்பு முதுகுத்தண்டு மற்றும் நீர் வடிகட்டுதல் சமநிலை (துலாம் / 6-ஆம் பாவகம்)';
    }
    if (text.includes('Vrishchika / Scorpio in 6th')) {
      return 'இடுப்பு கூம்பு பகுதி, கழிவு வெளியேற்ற பாதைகள் மற்றும் இனப்பெருக்க திசு நலம் (விருச்சிகம் / 6-ஆம் பாவகம்)';
    }
    if (text.includes('Dhanus / Sagittarius in 6th')) {
      return 'கல்லீரல் வளர்சிதை மாற்றம், தமனி சுழற்சி மற்றும் தொடை/இடுப்பு தசை வலிமை (தனுசு / 6-ஆம் பாவகம்)';
    }
    if (text.includes('Makara / Capricorn in 6th')) {
      return 'முழங்கால் மூட்டுகள், எலும்பு அடர்த்தி மற்றும் மூட்டு திரவ ஒழுங்குமுறை (மகரம் / 6-ஆம் பாவகம்)';
    }
    if (text.includes('Kumbha / Aquarius in 6th')) {
      return 'கால் கணுக்கால், நரம்பு சுழற்சி மற்றும் புற இரத்த ஓட்ட நலம் (கும்பம் / 6-ஆம் பாவகம்)';
    }
    if (text.includes('Meena / Pisces in 6th')) {
      return 'நிணநீர் வடிகால், ஆழ்ந்த தூக்க சமநிலை மற்றும் பாத நரம்பு பலம் (மீனம் / 6-ஆம் பாவகம்)';
    }

    // Roga Lord matches
    if (text.includes('Sun as Roga Lord')) {
      return 'இதய பலம், கண் பார்வை தெளிவு மற்றும் எலும்பு தாது உறிஞ்சுதல் (சூரியன் - ரோகாதிபதி)';
    }
    if (text.includes('Moon as Roga Lord')) {
      return 'உடல் திரவ சமநிலை, நிணநீர் ஒழுங்குமுறை மற்றும் மன அமைதி சமநிலை (சந்திரன் - ரோகாதிபதி)';
    }
    if (text.includes('Mars as Roga Lord')) {
      return 'இரத்த சுத்தி, தசை அழற்சி மற்றும் பித்த உஷ்ணம் தணித்தல் (செவ்வாய் - ரோகாதிபதி)';
    }
    if (text.includes('Mercury as Roga Lord')) {
      return 'தோல் பாதுகாப்பு அரண், நரம்பு மண்டலம் மற்றும் செரிமான என்சைம் சமநிலை (புதன் - ரோகாதிபதி)';
    }
    if (text.includes('Jupiter as Roga Lord')) {
      return 'கல்லீரல் கொழுப்பு வளர்சிதை மாற்றம் மற்றும் தமனி ஆரோக்கியம் (குரு - ரோகாதிபதி)';
    }
    if (text.includes('Venus as Roga Lord')) {
      return 'சிறுநீரக நீரேற்றம், நாளமில்லா சுரப்பி சமநிலை மற்றும் இனப்பெருக்க திசு ஆரோக்கியம் (சுக்கிரன் - ரோகாதிபதி)';
    }
    if (text.includes('Saturn as Roga Lord')) {
      return 'மூட்டு இயக்கம், வறட்சி தவிர்த்தல், தசைநார் நெகிழ்வுத்தன்மை (சனி - ரோகாதிபதி)';
    }

    // 8th House Lord match
    if (text.includes('Longevity resilience & chronic vitality maintenance governed by 8th Lord')) {
      let lordMatch = text.replace(/Longevity resilience & chronic vitality maintenance governed by 8th Lord /g, '');
      lordMatch = translateLagnaElement(translateModality(lordMatch));
      return `8-ஆம் அதிபதி ${lordMatch} அமைப்பால் நீண்ட ஆயுள் மற்றும் நோய் எதிர்ப்பு ஆற்றல் பராமரிப்பு`;
    }

    return text;
  };

  const translateLifestyleDirective = (text) => {
    if (!text || language !== 'ta') return text;
    if (text.includes('cooling, grounding, fresh whole foods')) {
      return 'இயற்கையான இனிப்பு, கசப்பு மற்றும் துவர்ப்பு சுவை கொண்ட குளிர்ச்சியான, புத்துணர்ச்சியூட்டும் முழு உணவுகளை உட்கொள்ளவும்.';
    }
    if (text.includes('Limit pungent spices, sour citrus excess')) {
      return 'காரமான மசாலாக்கள், அதிக புளிப்பு/சிட்ரஸ், எண்ணெயில் பொரித்த உணவுகள் மற்றும் இரவு நேர கனமான உணவுகளைத் தவிர்க்கவும்.';
    }
    if (text.includes('Favor warm, nourishing, easily digestible')) {
      return 'மிதமான நெய், நல்லெண்ணெய் சேர்த்த சூடான, எளிதில் செரிமானமாகும் சத்தான சமைத்த உணவுகளை உட்கொள்ளவும்.';
    }
    if (text.includes('Maintain consistent meal schedules')) {
      return 'வழக்கமான நேரத்திற்கு உணவருந்தவும்; உலர்ந்த, குளிர்ந்த, பச்சையான மற்றும் கார்பனேற்றப்பட்ட உணவுகளைத் தவிர்க்கவும்.';
    }
    if (text.includes('Favor light, warm, dry, and mildly spiced')) {
      return 'செரிமான அக்னியைத் தூண்ட இலகுவான, சூடான மற்றும் மிதமான மசாலா சேர்த்த உணவுகளை உட்கொள்ளவும்.';
    }
    if (text.includes('Minimize heavy dairy, refined sugars')) {
      return 'கனமான பால் பொருட்கள், சர்க்கரை, குளிர்பானங்கள் மற்றும் உணவுக்குப் பின் உடனடியாக உறங்குவதைத் தவிர்க்கவும்.';
    }
    if (text.includes('Incorporate gentle daily Pranayama')) {
      return 'ஓஜஸ் (உயிர் நோய் எதிர்ப்பு சக்தி) பாதுகாக்க தினசரி நாடி சுத்தி/சீத்தளி பிராணாயாமம் மற்றும் சீரான தூக்க முறையைப் பின்பற்றவும்.';
    }
    return text;
  };

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
          <div style={{ marginBottom: '18px' }}>
            <h4 style={{ fontSize: '13px', color: 'var(--accent-gold)', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              📊 {t('threePairsTitle', language)}
            </h4>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '12px' }}>
              {Object.entries(ayurdaya.threePairsDetails).map(([pairKey, detail], idx) => {
                const isObj = typeof detail === 'object' && detail !== null;
                const isConsensus = pairKey === 'majorityConsensus';

                return (
                  <div key={idx} style={{
                    background: isConsensus ? 'rgba(212,175,55,0.06)' : 'rgba(255,255,255,0.02)',
                    border: isConsensus ? '1px solid rgba(212,175,55,0.3)' : '1px solid var(--border)',
                    borderRadius: '8px',
                    padding: '12px',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                    gap: '8px'
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '6px' }}>
                      <strong style={{ color: 'var(--accent-gold)', fontSize: '12px' }}>
                        {formatPairTitle(pairKey)}
                      </strong>
                      {isObj && detail.derivedSpan && renderSpanBadge(detail.derivedSpan)}
                      {!isObj && renderSpanBadge(String(detail))}
                    </div>
                    {isObj && detail.planets && (
                      <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                        🪐 <span style={{ color: 'var(--text-primary)', fontWeight: '500' }}>{translateModality(detail.planets)}</span>
                      </div>
                    )}
                  </div>
                );
              })}
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
              {translatePrakriti(health?.dominantPrakriti) || (language === 'ta' ? 'வாத-பித்தம் (Vata-Pitta)' : 'Vata-Pitta')}
            </strong>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
            <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
              🔥 {t('lagnaElement', language)}
            </span>
            <strong style={{ fontSize: '15px', color: '#e67e22' }}>
              {translateLagnaElement(health?.lagnaElement) || (language === 'ta' ? 'அக்னி (நெருப்பு / Fire)' : 'Agni (Fire)')}
            </strong>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
            <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
              🏥 {t('rogaSthana', language)}
            </span>
            <strong style={{ fontSize: '14px', color: 'var(--text-primary)' }}>
              {translateRogaSthana(health?.rogaSthanaSign, health?.rogaLord) || (language === 'ta' ? 'கன்னி (6-ஆம் பாவகம்)' : 'Kanya (House 6)')}
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
                    padding: '6px 12px',
                    borderRadius: '12px',
                    background: 'rgba(231, 76, 60, 0.1)',
                    color: '#e74c3c',
                    border: '1px solid rgba(231, 76, 60, 0.25)',
                    lineHeight: '1.4'
                  }}>
                    • {translateOrganVulnerability(organ)}
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
              <ul style={{ margin: 0, paddingLeft: '18px', fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.6' }}>
                {health.dietaryAndLifestyleDirectives.map((directive, idx) => (
                  <li key={idx} style={{ marginBottom: '6px' }}>{translateLifestyleDirective(directive)}</li>
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
