import React, { useState } from 'react';
import { t } from '../i18n/translations';

export default function HealthLongevityView({ chartData, language }) {
  if (!chartData) return null;

  const [expandedShoolaIndex, setExpandedShoolaIndex] = useState(null);

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
        return language === 'ta' ? '1. லக்னாதிபதி & 8-ஆம் அதிபதி' :
               language === 'hi' ? '1. लग्नेश और अष्टमेश' :
               language === 'te' ? '1. లగ్నాధిపతి & 8వ అధిపతి' :
               language === 'kn' ? '1. ಲಗ್ನಾಧಿಪತಿ & 8ನೇ ಅಧಿಪತಿ' :
               language === 'ml' ? '1. ലഗ്നാധിപനും 8-ാം അധിപനും' :
               '1. Lagna Lord & 8th Lord';
      case 'pair2_moon_and_saturn':
        return language === 'ta' ? '2. சந்திரன் & ஆயுள்காரகன் சனி' :
               language === 'hi' ? '2. चन्द्र और आयुष्कारक शनि' :
               language === 'te' ? '2. చంద్రుడు & ఆయుష్కారక శని' :
               language === 'kn' ? '2. ಚಂದ್ರ & ಆಯುಷ್ಕಾರಕ ಶನಿ' :
               language === 'ml' ? '2. ചന്ദ്രനും ആയുഷ്കാരകനായ ശനിയും' :
               '2. Moon & Saturn (Ayushkaraka)';
      case 'pair3_lagna_and_horaLagna':
      case 'pair3_lagna_and_moon':
        return language === 'ta' ? '3. லக்னம் & ஹோரா லக்னம்' :
               language === 'hi' ? '3. लग्न और होरा लग्न' :
               language === 'te' ? '3. లగ్నం & హోరా లగ్నం' :
               language === 'kn' ? '3. ಲಗ್ನ & ಹೋರಾ ಲಗ್ನ' :
               language === 'ml' ? '3. ലഗ്നവും ഹോരാ ലഗ്നവും' :
               '3. Lagna & Hora Lagna';
      case 'majorityConsensus':
        return language === 'ta' ? 'பெரும்பான்மை முடிவு' :
               language === 'hi' ? 'बहुमत सर्वसम्मत निर्णय' :
               language === 'te' ? 'మెజారిటీ నిర్ణయం' :
               language === 'kn' ? 'ಬಹುಮತದ ನಿರ್ಧಾರ' :
               language === 'ml' ? 'ഭൂരിപക്ഷ തീരുമാനം' :
               'Majority Consensus';
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
      .replace(/Hora Lagna/g, 'ஹோரா லக்னம்')
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

  const translateSpan = (span) => {
    if (!span) return '';
    if (span === 'Poornayu') return t('poornayu', language);
    if (span === 'Madhyayu') return t('madhyayu', language);
    if (span === 'Alpayu') return t('alpayu', language);
    return span;
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

  const translateKhandaSubTier = (tier) => {
    if (!tier) return '';
    if (language !== 'ta') return tier;
    return tier
      .replace(/Balarishta \/ Adhama Alpayu/g, 'பாலாரிஷ்டம் / அதம அல்பாயுள்')
      .replace(/Madhyama Alpayu/g, 'மத்தியம அல்பாயுள்')
      .replace(/Uttama Alpayu/g, 'உத்தம அல்பாயுள்')
      .replace(/Adhama Madhyayu/g, 'அதம மத்தியாயுள்')
      .replace(/Madhyama Madhyayu/g, 'மத்தியம மத்தியாயுள்')
      .replace(/Uttama Madhyayu/g, 'உத்தம மத்தியாயுள்')
      .replace(/Adhama Poornayu/g, 'அதம பூரணாயுள்')
      .replace(/Madhyama Poornayu/g, 'மத்தியம பூரணாயுள்')
      .replace(/Paramayu \/ Deerghayu/g, 'பரமாயுள் / தீர்க்காயுள்')
      .replace(/Years/g, 'ஆண்டுகள்');
  };

  const translateRashi = (rashi) => {
    if (!rashi) return '';
    if (language === 'ta') {
      const map = {
        'Aries': 'மேஷம் (Aries)', 'Mesha': 'மேஷம் (Mesha)',
        'Taurus': 'ரிஷபம் (Taurus)', 'Vrishabha': 'ரிஷபம் (Vrishabha)',
        'Gemini': 'மிதுனம் (Gemini)', 'Mithuna': 'மிதுனம் (Mithuna)',
        'Cancer': 'கடகம் (Cancer)', 'Kataka': 'கடகம் (Kataka)', 'Karka': 'கடகம் (Karka)',
        'Leo': 'சிம்மம் (Leo)', 'Simha': 'சிம்மம் (Simha)',
        'Virgo': 'கன்னி (Virgo)', 'Kanya': 'கன்னி (Kanya)',
        'Libra': 'துலாம் (Libra)', 'Tula': 'துலாம் (Tula)',
        'Scorpio': 'விருச்சிகம் (Scorpio)', 'Vrishchika': 'விருச்சிகம் (Vrishchika)',
        'Sagittarius': 'தனுசு (Sagittarius)', 'Dhanus': 'தனுசு (Dhanus)', 'Dhanu': 'தனுசு (Dhanu)',
        'Capricorn': 'மகரம் (Capricorn)', 'Makara': 'மகரம் (Makara)',
        'Aquarius': 'கும்பம் (Aquarius)', 'Kumbha': 'கும்பம் (Kumbha)',
        'Pisces': 'மீனம் (Pisces)', 'Meena': 'மீனம் (Meena)'
      };
      return map[rashi] || rashi;
    }
    return rashi;
  };

  const translatePlanet = (planet) => {
    if (!planet) return '';
    if (language === 'ta') {
      const map = {
        'Sun': 'சூரியன் (Sun)', 'Surya': 'சூரியன்',
        'Moon': 'சந்திரன் (Moon)', 'Chandra': 'சந்திரன்',
        'Mars': 'செவ்வாய் (Mars)', 'Kuja': 'செவ்வாய்', 'Mangal': 'செவ்வாய்',
        'Mercury': 'புதன் (Mercury)', 'Budha': 'புதன்',
        'Jupiter': 'குரு (Jupiter)', 'Guru': 'குரு',
        'Venus': 'சுக்கிரன் (Venus)', 'Shukra': 'சுக்கிரன்',
        'Saturn': 'சனி (Saturn)', 'Shani': 'சனி',
        'Rahu': 'ராகு (Rahu)',
        'Ketu': 'கேது (Ketu)'
      };
      return map[planet] || planet;
    }
    return planet;
  };

  const translateProgression = (direction) => {
    if (!direction) return '';
    if (language === 'ta') {
      if (direction.includes('Direct') || direction.includes('Savya')) {
        return 'நேர்முறை - சவ்யம் (Direct / Savya)';
      }
      if (direction.includes('Reverse') || direction.includes('Apasavya')) {
        return 'எதிர்முறை - அபசவ்யம் (Reverse / Apasavya)';
      }
    }
    return direction;
  };

  const translateRule = (rule) => {
    if (!rule) return '';
    if (language === 'ta') {
      if (rule.includes('Tri-Samvada') || rule.includes('Unanimous')) {
        return 'திரி-சம்வாதம் (முழு ஒருமனதான முடிவு)';
      }
      if (rule.includes('Vishesha Sutra 1') || rule.includes('Chandra-Kendra')) {
        return 'விசேஷ சூத்திரம் 1 (சந்திர-கேந்திர சூத்திரம்)';
      }
      if (rule.includes('Vishesha Sutra 2') || rule.includes('Atmakaraka-Kendra')) {
        return 'விசேஷ சூத்திரம் 2 (ஆத்மகாரக-கேந்திர சூத்திரம்)';
      }
      if (rule.includes('Dwi-Samvada') || rule.includes('Majority')) {
        return 'துவி-சம்வாதம் (பெரும்பான்மை முடிவு)';
      }
      if (rule.includes('Asamvada (Odd')) {
        return 'அசம்வாதம் (ஒற்றைப்படை லக்ன விதிவிலக்கு)';
      }
      if (rule.includes('Asamvada (Even')) {
        return 'அசம்வாதம் (இரட்டைப்படை லக்ன விதிவிலக்கு)';
      }
      if (rule.includes('Asamvada')) {
        return 'அசம்வாதம் (விசேஷ விதிவிலக்கு)';
      }
    }
    return rule;
  };

  const translateOverrideReason = (reason) => {
    if (!reason || language !== 'ta') return reason;
    return reason
      .replace(/All 3 Jaimini pairs agree unanimously on/g, 'அனைத்து 3 ஜெமினி இணைகளும் ஒருமனதாக தேர்வு செய்தவை:')
      .replace(/Moon in Lagna \(1st house\): Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'சந்திரன் லக்னத்தில் (1-ஆம் பாவகம்) அமர்ந்துள்ளதால்: இணை 2 (சந்திரன் + சனி) முதன்மை அதிகாரத்தைப் பெறுகிறது (ஜெமினி உபதேச சூத்திரம் 2.1.23).')
      .replace(/Moon in 7th house: Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'சந்திரன் 7-ஆம் பாவகத்தில் அமர்ந்துள்ளதால்: இணை 2 (சந்திரன் + சனி) முதன்மை அதிகாரத்தைப் பெறுகிறது (ஜெமினி உபதேச சூத்திரம் 2.1.23).')
      .replace(/Atmakaraka in Lagna \(1st house\):/g, 'ஆத்மகாரகன் லக்னத்தில் (1-ஆம் பாவகம்) உள்ளதால்:')
      .replace(/Atmakaraka in 7th house:/g, 'ஆத்மகாரகன் 7-ஆம் பாவகத்தில் உள்ளதால்:')
      .replace(/Atmakaraka in Kendra:/g, 'ஆத்மகாரகன் கேந்திரத்தில் உள்ளதால்:')
      .replace(/Odd Lagna gives precedence to Lagna-Hora Lagna \(Pair 3\)\./g, 'ஒற்றைப்படை லக்னம் என்பதால் லக்னம் - ஹோரா லக்னம் (இணை 3) முதன்மை பெறுகிறது.')
      .replace(/Even Lagna gives precedence to Lagna Lord-8th Lord \(Pair 1\)\./g, 'இரட்டைப்படை லக்னம் என்பதால் லக்னாதிபதி - 8-ஆம் அதிபதி (இணை 1) முதன்மை பெறுகிறது.')
      .replace(/Majority consensus: 2 of 3 pairs agree on/g, 'பெரும்பான்மை முடிவு: 3-ல் 2 இணைகள் முடிவு செய்தவை:')
      .replace(/All 3 pairs indicate distinct spans:/g, '3 இணைகளும் வெவ்வேறு ஆயுள் பிரிவுகளைக் காட்டுகின்றன:')
      .replace(/Poornayu/g, 'பூரணாயுள்')
      .replace(/Madhyayu/g, 'மத்தியாயுள்')
      .replace(/Alpayu/g, 'அல்பாயுள்');
  };

  const translateKakshyaAdjustment = (adj) => {
    if (!adj || language !== 'ta') return adj;
    return adj
      .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(elevating longevity compartment from Alpayu to Madhyayu baseline ~68 yrs\)\./g, 'குரு கேந்திர/திரிகோணத்தில் சுப பலத்துடன் அமர்ந்து கக்ஷ்ய விருத்தி அருள்கிறார் (அல்பாயுளிலிருந்து மத்தியாயுள் ~68 வயதுக்கு உயர்வு).')
      .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(elevating longevity compartment from Madhyayu to Poornayu baseline ~82 yrs\)\./g, 'குரு கேந்திர/திரிகோணத்தில் சுப பலத்துடன் அமர்ந்து கக்ஷ்ய விருத்தி அருள்கிறார் (மத்தியாயுளிலிருந்து பூரணாயுள் ~82 வயதுக்கு உயர்வு).')
      .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(\+(\d+) years\)\./g, 'குரு கேந்திர/திரிகோணத்தில் சுப பலத்துடன் அமர்ந்து கக்ஷ்ய விருத்தி அருள்கிறார் (+$1 ஆண்டுகள்).')
      .replace(/Atmakaraka \((.*?)\) in Kendra\/Trikona\/Exalted confers Kakshya Vriddhi \(elevating longevity compartment from Alpayu to Madhyayu\)\./g, 'ஆத்மகாரகன் ($1) கேந்திர/திரிகோணம்/உச்சத்தில் அமர்ந்து கக்ஷ்ய விருத்தி அருள்கிறார் (அல்பாயுளிலிருந்து மத்தியாயுளுக்கு உயர்வு).')
      .replace(/Atmakaraka \((.*?)\) in Kendra\/Trikona\/Exalted reinforces longevity vitality \(\+(\d+) years\)\./g, 'ஆத்மகாரகன் ($1) கேந்திர/திரிகோணம்/உச்சத்தில் அமர்ந்து ஆயுள் பலத்தை அதிகரிக்கிறார் (+$2 ஆண்டுகள்).')
      .replace(/Ayushkaraka Saturn in Own\/Exalted sign reinforces longevity \(\+(\d+) years\)\./g, 'ஆயுள்காரகன் சனி ஆட்சி/உச்ச பலத்துடன் அமர்ந்து ஆயுளை உறுதிப்படுத்துகிறார் (+$1 ஆண்டுகள்).')
      .replace(/Lagna Lord strong in own\/exalted\/Kendra\/Trikona adds physical vitality \(\+(\d+) years\)\./g, 'லக்னாதிபதி ஆட்சி/உச்சம்/கேந்திர/திரிகோணத்தில் பலம் பெற்று தேக ஆரோக்கியத்தை அதிகரிக்கிறார் (+$1 ஆண்டுகள்).')
      .replace(/Ayushkaraka Saturn possesses Neecha Bhanga \(cancellation of debility into longevity stability\)\./g, 'ஆயுள்காரகன் சனி நீசபங்க ராஜயோகம் பெற்று ஆயுள் நிலைத்தன்மையை அருள்கிறார் (+2 ஆண்டுகள்).')
      .replace(/Ayushkaraka Saturn in debility applies Kakshya Hrasa reduction \(-(\d+) years\)\./g, 'ஆயுள்காரகன் சனி நீசமடைந்து கக்ஷ்ய ஹிராஸ குறைப்பை ஏற்படுத்துகிறார் (-$1 ஆண்டுகள்).')
      .replace(/Lagna Lord debilitated in Dusthana applies Kakshya Hrasa \(-(\d+) years\)\./g, 'லக்னாதிபதி துஸ்தானத்தில் நீசமடைந்து கக்ஷ்ய ஹிராஸ குறைப்பை ஏற்படுத்துகிறார் (-$1 ஆண்டுகள்).')
      .replace(/Lagna Lord in Dusthana \(6\/8\/12\) advises mindful health regimen\./g, 'லக்னாதிபதி துஸ்தானத்தில் (6/8/12) இருப்பதால் ஆரோக்கியத்தில் கூடுதல் கவனம் தேவை (-2 ஆண்டுகள்).')
      .replace(/Lagna hemmed between malefics in 12th & 2nd \(Papakarthari Yoga\) cautions physical vitality \(-(\d+) years\)\./g, 'லக்னம் பாபகர்த்தரி யோகத்தில் (12 & 2-ல் பாவ கிரகங்கள்) சிக்கியுள்ளதால் உடல் ஆரோக்கியத்தில் எச்சரிக்கை தேவை (-$1 ஆண்டுகள்).')
      .replace(/Moon hemmed between malefics in 12th & 2nd \(Papakarthari Yoga on Moon\) cautions vitality \(-(\d+) years\)\./g, 'சந்திரன் பாபகர்த்தரி யோகத்தில் (12 & 2-ல் பாவ கிரகங்கள்) சிக்கியுள்ளதால் மன/உடல் நலனில் எச்சரிக்கை தேவை (-$1 ஆண்டுகள்).')
      .replace(/Malefics in Kendras with no benefics in Kendras applies Kakshya Hrasa \(-(\d+) years\)\./g, 'கேந்திரங்களில் சுப கிரகங்களின்றி பாவ கிரகங்கள் மட்டுமே இருப்பதால் கக்ஷ்ய ஹிராஸம் உண்டாகிறது (-$1 ஆண்டுகள்).');
  };

  const translateRiskCategory = (risk) => {
    if (!risk) return '';
    if (language === 'ta') {
      switch (risk) {
        case 'CRITICAL_TRISHOOLA_RUDRA': return 'அதிதீவிர திரிசூல-ருத்ர காலம் (Critical)';
        case 'HIGH_TRISHOOLA': return 'திரிசூல காலம் (High Risk)';
        case 'HIGH_RUDRA': return 'ருத்ர காலம் (High Risk)';
        case 'MODERATE': return 'சாதாரண காலம் (Moderate)';
        default: return risk;
      }
    }
    switch (risk) {
      case 'CRITICAL_TRISHOOLA_RUDRA': return 'Critical (Trishoola + Rudra)';
      case 'HIGH_TRISHOOLA': return 'High Risk (Trishoola)';
      case 'HIGH_RUDRA': return 'High Risk (Rudra)';
      case 'MODERATE': return 'Moderate';
      default: return risk;
    }
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

  // Vishesha Sutras and 3-Pair synthesis data
  const ruleApplied = ayurdaya?.jaiminiThreePairs?.ruleApplied || ayurdaya?.threePairsDetails?.ruleApplied;
  const overrideReason = ayurdaya?.jaiminiThreePairs?.overrideReason || ayurdaya?.threePairsDetails?.overrideReason;
  const isSpecialRule = ruleApplied && (ruleApplied.includes('Vishesha') || ruleApplied.includes('Asamvada'));

  // Kakshya Analysis
  const kakshya = ayurdaya?.kakshyaAnalysis;
  const shoola = ayurdaya?.shoolaDasaInfo;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>

      {/* ========================================================================= */}
      {/* 1. AYURDAYA LONGEVITY HERO CARD                                            */}
      {/* ========================================================================= */}
      <div className="card" style={{ background: 'linear-gradient(135deg, rgba(255,255,255,0.03) 0%, rgba(212,175,55,0.05) 100%)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '10px', marginBottom: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
            <h3 style={{ margin: 0, color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
              🪐 {t('longevityTitle', language)}
            </h3>
            {ayurdaya?.khandaSubTier && (
              <span style={{
                fontSize: '12px',
                fontWeight: '700',
                padding: '4px 12px',
                borderRadius: '16px',
                background: 'rgba(212, 175, 55, 0.12)',
                color: 'var(--accent-gold)',
                border: '1px solid rgba(212, 175, 55, 0.35)',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '6px'
              }}>
                ⏳ {translateKhandaSubTier(ayurdaya.khandaSubTier)}
              </span>
            )}
          </div>
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

        {/* Longevity Key Stats Grid */}
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

          {ayurdaya?.khandaSubTier && (
            <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
                ⏳ {t('khandaSubTier', language)}
              </div>
              <div style={{ fontSize: '13px', fontWeight: '600', color: 'var(--accent-gold)', lineHeight: '1.4' }}>
                {translateKhandaSubTier(ayurdaya.khandaSubTier)}
              </div>
            </div>
          )}

          {shoola?.criticalShoolaWindow && (
            <div style={{ background: 'rgba(212,175,55,0.04)', border: '1px solid rgba(212,175,55,0.3)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ fontSize: '11px', color: 'var(--accent-gold)', marginBottom: '4px' }}>
                🔱 {t('criticalLongevityWindow', language)}
              </div>
              <div style={{ fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.4', fontWeight: '500' }}>
                {shoola.criticalShoolaWindow}
              </div>
            </div>
          )}
        </div>

        {/* 3-Pair Modality Table */}
        {ayurdaya?.threePairsDetails && Object.keys(ayurdaya.threePairsDetails).length > 0 && (
          <div style={{ marginBottom: '18px' }}>
            <h4 style={{ fontSize: '13px', color: 'var(--accent-gold)', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              📊 {t('threePairsTitle', language)}
            </h4>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '12px' }}>
              {Object.entries(ayurdaya.threePairsDetails)
                .filter(([pairKey]) => pairKey.startsWith('pair') || pairKey === 'majorityConsensus')
                .map(([pairKey, detail], idx) => {
                  const isObj = typeof detail === 'object' && detail !== null;
                  const isConsensus = pairKey === 'majorityConsensus';

                  return (
                    <div key={idx} style={{
                      background: isConsensus ? 'rgba(212,175,55,0.08)' : 'rgba(255,255,255,0.02)',
                      border: isConsensus ? '1px solid rgba(212,175,55,0.35)' : '1px solid var(--border)',
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

        {/* Vishesha Sutras / Applied Synthesis Override Card */}
        {ruleApplied && (
          <div style={{
            marginBottom: '18px',
            background: isSpecialRule ? 'rgba(212, 175, 55, 0.08)' : 'rgba(255, 255, 255, 0.02)',
            border: isSpecialRule ? '1px solid rgba(212, 175, 55, 0.35)' : '1px solid var(--border)',
            borderRadius: '8px',
            padding: '12px 16px',
            display: 'flex',
            flexDirection: 'column',
            gap: '8px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '8px' }}>
              <strong style={{ fontSize: '13px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                📜 {t('visheshaSutraTitle', language)}: <span style={{ color: '#fff' }}>{translateRule(ruleApplied)}</span>
              </strong>
              <span style={{
                fontSize: '11px',
                fontWeight: 'bold',
                padding: '2px 8px',
                borderRadius: '10px',
                background: isSpecialRule ? 'rgba(230, 126, 34, 0.2)' : 'rgba(46, 204, 113, 0.15)',
                color: isSpecialRule ? '#e67e22' : '#2ecc71',
                border: isSpecialRule ? '1px solid rgba(230, 126, 34, 0.3)' : '1px solid rgba(46, 204, 113, 0.3)'
              }}>
                {isSpecialRule ? '⚡ Vishesha Override' : '⚖️ Synthesis Consensus'}
              </span>
            </div>
            {overrideReason && (
              <p style={{ fontSize: '12px', color: 'var(--text-primary)', margin: 0, lineHeight: '1.5' }}>
                {translateOverrideReason(overrideReason)}
              </p>
            )}
          </div>
        )}

        {/* Detailed Kakshya Vriddhi & Hrasa Section */}
        {kakshya && (
          <div style={{ marginBottom: '18px', background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <h4 style={{ margin: '0 0 12px 0', fontSize: '13px', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '6px' }}>
              ✨ {t('kakshyaVriddhiTitle', language)}
            </h4>

            {/* Kakshya Summary Stats */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: '10px', marginBottom: '12px' }}>
              <div style={{ background: 'rgba(0,0,0,0.2)', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)' }}>
                <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block' }}>
                  {language === 'ta' ? 'ஆரம்ப பிரிவு' : 'Base Span'}
                </span>
                <strong style={{ fontSize: '13px', color: 'var(--text-primary)' }}>
                  {translateSpan(kakshya.baseSpan)} (~{kakshya.baseCeilingAge} {language === 'ta' ? 'வயது' : 'Yrs'})
                </strong>
              </div>

              <div style={{ background: 'rgba(0,0,0,0.2)', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)' }}>
                <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block' }}>
                  {t('netYearsAdjustment', language)}
                </span>
                <strong style={{
                  fontSize: '13px',
                  color: (kakshya.netYearsAdjustment || 0) >= 0 ? '#2ecc71' : '#e74c3c'
                }}>
                  {(kakshya.netYearsAdjustment || 0) >= 0 ? `+${kakshya.netYearsAdjustment || 0}` : kakshya.netYearsAdjustment} {language === 'ta' ? 'ஆண்டுகள்' : 'Years'}
                </strong>
              </div>

              <div style={{ background: 'rgba(0,0,0,0.2)', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)' }}>
                <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block' }}>
                  {language === 'ta' ? 'இறுதி பிரிவு' : 'Adjusted Span'}
                </span>
                <strong style={{ fontSize: '13px', color: 'var(--accent-gold)' }}>
                  {translateSpan(kakshya.adjustedSpan)} (~{kakshya.adjustedCeilingAge} {language === 'ta' ? 'வயது' : 'Yrs'})
                </strong>
              </div>

              {kakshya.atmakaraka && (
                <div style={{ background: 'rgba(0,0,0,0.2)', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)' }}>
                  <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block' }}>
                    {t('atmakarakaAnchor', language)}
                  </span>
                  <strong style={{ fontSize: '13px', color: '#3498db' }}>
                    {kakshya.atmakaraka}
                  </strong>
                </div>
              )}
            </div>

            {/* Promotions (Vriddhi) & Reductions (Hrasa) lists */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '12px' }}>
              <div>
                <strong style={{ fontSize: '12px', color: '#2ecc71', display: 'flex', alignItems: 'center', gap: '4px', marginBottom: '6px' }}>
                  🟢 {t('vriddhiFactors', language)} ({kakshya.vriddhiCount || 0}):
                </strong>
                {kakshya.promotions && kakshya.promotions.length > 0 ? (
                  <ul style={{ margin: 0, paddingLeft: '18px', fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
                    {kakshya.promotions.map((p, i) => (
                      <li key={i} style={{ marginBottom: '4px' }}>{translateKakshyaAdjustment(p)}</li>
                    ))}
                  </ul>
                ) : (
                  <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>{t('noVriddhiFactors', language)}</span>
                )}
              </div>

              <div>
                <strong style={{ fontSize: '12px', color: '#e74c3c', display: 'flex', alignItems: 'center', gap: '4px', marginBottom: '6px' }}>
                  🔴 {t('hrasaFactors', language)} ({kakshya.hrasaCount || 0}):
                </strong>
                {kakshya.reductions && kakshya.reductions.length > 0 ? (
                  <ul style={{ margin: 0, paddingLeft: '18px', fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
                    {kakshya.reductions.map((r, i) => (
                      <li key={i} style={{ marginBottom: '4px' }}>{translateKakshyaAdjustment(r)}</li>
                    ))}
                  </ul>
                ) : (
                  <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>{t('noHrasaFactors', language)}</span>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Fallback Kakshya Adjustments if kakshyaAnalysis object not present */}
        {!kakshya && ayurdaya?.kakshyaAdjustments && ayurdaya.kakshyaAdjustments.length > 0 && (
          <div style={{ marginBottom: '14px' }}>
            <h4 style={{ fontSize: '12px', color: 'var(--accent-gold)', marginBottom: '6px' }}>
              ✨ {t('kakshyaAdjustments', language)}:
            </h4>
            <ul style={{ margin: 0, paddingLeft: '20px', fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
              {ayurdaya.kakshyaAdjustments.map((adj, i) => (
                <li key={i} style={{ marginBottom: '4px' }}>{translateKakshyaAdjustment(adj)}</li>
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

      {/* ========================================================================= */}
      {/* 2. MAHARISHI JAIMINI SHOOLA DASA 9-YEAR TIMELINE                          */}
      {/* ========================================================================= */}
      {shoola && (
        <div className="card" style={{ background: 'linear-gradient(135deg, rgba(255,255,255,0.02) 0%, rgba(155,89,182,0.05) 100%)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '10px', marginBottom: '16px' }}>
            <h3 style={{ margin: 0, color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
              🔱 {t('shoolaDasaTitle', language)}
            </h3>
            <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
              12 Mahadasas × 9 Years (108 Years Total)
            </span>
          </div>

          {/* Shoola Dasa Key Parameters Bar */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '12px', marginBottom: '18px' }}>
            <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
                🚩 {t('startingSign', language)}
              </div>
              <div style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--text-primary)' }}>
                {translateRashi(shoola.startingSignName)}
              </div>
              {shoola.startingSignReason && (
                <div style={{ fontSize: '10px', color: 'var(--text-secondary)', marginTop: '4px', lineHeight: '1.3' }}>
                  {shoola.startingSignReason}
                </div>
              )}
            </div>

            <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
                🔄 {t('progressionDirection', language)}
              </div>
              <div style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--accent-gold)' }}>
                {translateProgression(shoola.progressionDirection)}
              </div>
            </div>

            <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
                ⚡ {t('rudraPlanetSign', language)}
              </div>
              <div style={{ fontSize: '14px', fontWeight: 'bold', color: '#e67e22' }}>
                {translateRashi(shoola.rudraSignName)} ({translatePlanet(shoola.rudraPlanetName)})
              </div>
            </div>

            <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
                🔱 {t('trishoolaSigns', language)}
              </div>
              <div style={{ fontSize: '13px', fontWeight: 'bold', color: '#e74c3c' }}>
                {shoola.trishoolaSignNames ? shoola.trishoolaSignNames.map(translateRashi).join(', ') : 'N/A'}
              </div>
            </div>
          </div>

          {/* Shoola Dasa Critical Window Callout */}
          {shoola.criticalShoolaWindow && (
            <div style={{
              marginBottom: '18px',
              background: 'rgba(231, 76, 60, 0.08)',
              border: '1px solid rgba(231, 76, 60, 0.35)',
              borderRadius: '8px',
              padding: '12px 16px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              flexWrap: 'wrap',
              gap: '8px'
            }}>
              <div>
                <strong style={{ fontSize: '12px', color: '#e74c3c', display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '2px' }}>
                  ⚠️ {t('criticalLongevityWindow', language)}:
                </strong>
                <span style={{ fontSize: '13px', fontWeight: '600', color: 'var(--text-primary)' }}>
                  {shoola.criticalShoolaWindow}
                </span>
              </div>
              <span style={{
                fontSize: '11px',
                fontWeight: 'bold',
                padding: '4px 10px',
                borderRadius: '12px',
                background: 'rgba(231, 76, 60, 0.2)',
                color: '#e74c3c',
                border: '1px solid rgba(231, 76, 60, 0.4)'
              }}>
                🎯 {t('criticalWindowBadge', language)}
              </span>
            </div>
          )}

          {/* 12-Period Timeline Table / Grid */}
          <div style={{ overflowX: 'auto', marginBottom: '12px' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '12px', textAlign: 'left' }}>
              <thead>
                <tr style={{ background: 'rgba(255,255,255,0.04)', borderBottom: '1px solid var(--border)' }}>
                  <th style={{ padding: '10px', color: 'var(--accent-gold)' }}>#</th>
                  <th style={{ padding: '10px', color: 'var(--accent-gold)' }}>{t('dasaSign', language)}</th>
                  <th style={{ padding: '10px', color: 'var(--accent-gold)' }}>{t('ageSpan', language)}</th>
                  <th style={{ padding: '10px', color: 'var(--accent-gold)' }}>{t('calendarYears', language)}</th>
                  <th style={{ padding: '10px', color: 'var(--accent-gold)' }}>{t('riskLevel', language)} / Badges</th>
                  <th style={{ padding: '10px', color: 'var(--accent-gold)', textAlign: 'center' }}>Antardasa</th>
                </tr>
              </thead>
              <tbody>
                {shoola.periods && shoola.periods.map((period, pIdx) => {
                  const isCritical = ayurdaya?.estimatedLifespanCeiling >= period.startAge && ayurdaya?.estimatedLifespanCeiling < period.endAge;
                  const isExpanded = expandedShoolaIndex === pIdx;

                  return (
                    <React.Fragment key={pIdx}>
                      <tr style={{
                        borderBottom: '1px solid rgba(255,255,255,0.05)',
                        background: isCritical
                          ? 'rgba(212, 175, 55, 0.08)'
                          : period.isTrishoola
                          ? 'rgba(231, 76, 60, 0.04)'
                          : period.isRudra
                          ? 'rgba(230, 126, 34, 0.03)'
                          : 'transparent',
                        fontWeight: isCritical ? '600' : 'normal'
                      }}>
                        <td style={{ padding: '10px', color: 'var(--text-secondary)' }}>
                          D{period.periodIndex}
                        </td>
                        <td style={{ padding: '10px', color: 'var(--text-primary)' }}>
                          <strong>{translateRashi(period.signName)}</strong>
                        </td>
                        <td style={{ padding: '10px', color: 'var(--text-primary)' }}>
                          {period.startAge} - {period.endAge} {language === 'ta' ? 'வயது' : 'Yrs'}
                        </td>
                        <td style={{ padding: '10px', color: 'var(--text-secondary)' }}>
                          {period.startYear} - {period.endYear}
                        </td>
                        <td style={{ padding: '10px' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap' }}>
                            {period.isTrishoola && (
                              <span style={{
                                fontSize: '10px',
                                fontWeight: 'bold',
                                padding: '2px 8px',
                                borderRadius: '10px',
                                background: 'rgba(231, 76, 60, 0.2)',
                                color: '#e74c3c',
                                border: '1px solid rgba(231, 76, 60, 0.4)'
                              }}>
                                🔱 {t('trishoolaBadge', language)}
                              </span>
                            )}
                            {period.isRudra && (
                              <span style={{
                                fontSize: '10px',
                                fontWeight: 'bold',
                                padding: '2px 8px',
                                borderRadius: '10px',
                                background: 'rgba(230, 126, 34, 0.2)',
                                color: '#e67e22',
                                border: '1px solid rgba(230, 126, 34, 0.4)'
                              }}>
                                ⚡ {t('rudraBadge', language)}
                              </span>
                            )}
                            {isCritical && (
                              <span style={{
                                fontSize: '10px',
                                fontWeight: 'bold',
                                padding: '2px 8px',
                                borderRadius: '10px',
                                background: 'rgba(212, 175, 55, 0.2)',
                                color: 'var(--accent-gold)',
                                border: '1px solid rgba(212, 175, 55, 0.5)',
                                boxShadow: '0 0 8px rgba(212, 175, 55, 0.3)'
                              }}>
                                🎯 {t('criticalWindowBadge', language)}
                              </span>
                            )}
                            <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
                              {translateRiskCategory(period.riskCategory)}
                            </span>
                          </div>
                        </td>
                        <td style={{ padding: '10px', textAlign: 'center' }}>
                          <button
                            onClick={() => setExpandedShoolaIndex(isExpanded ? null : pIdx)}
                            style={{
                              background: isExpanded ? 'rgba(212,175,55,0.2)' : 'rgba(255,255,255,0.05)',
                              border: isExpanded ? '1px solid var(--accent-gold)' : '1px solid var(--border)',
                              color: isExpanded ? 'var(--accent-gold)' : 'var(--text-secondary)',
                              padding: '3px 8px',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '11px'
                            }}
                          >
                            {isExpanded ? '▲ ' + t('hideAntardasas', language) : '▼ 9M'}
                          </button>
                        </td>
                      </tr>

                      {/* Sub-Period (Antardasa) Expansion Drawer */}
                      {isExpanded && period.antardasas && (
                        <tr>
                          <td colSpan="6" style={{ padding: '12px', background: 'rgba(0,0,0,0.3)', borderBottom: '1px solid var(--border)' }}>
                            <div style={{ marginBottom: '8px', fontSize: '11px', fontWeight: 'bold', color: 'var(--accent-gold)' }}>
                              📅 {period.signName} Mahadasas Antardasas (12 × 9-Month Sub-Periods):
                            </div>
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: '8px' }}>
                              {period.antardasas.map((ad, adIdx) => (
                                <div key={adIdx} style={{
                                  background: 'rgba(255,255,255,0.02)',
                                  border: '1px solid var(--border)',
                                  borderRadius: '6px',
                                  padding: '6px 8px',
                                  fontSize: '11px'
                                }}>
                                  <div style={{ fontWeight: 'bold', color: 'var(--text-primary)' }}>
                                    {ad.subIndex}. {translateRashi(ad.signName)}
                                  </div>
                                  <div style={{ color: 'var(--text-secondary)', fontSize: '10px' }}>
                                    {ad.startMonthYear} - {ad.endMonthYear}
                                  </div>
                                </div>
                              ))}
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  );
                })}
              </tbody>
            </table>
          </div>

          {shoola.classicalRationale && (
            <p style={{ fontSize: '11px', color: 'var(--text-secondary)', margin: 0, lineHeight: '1.4', fontStyle: 'italic' }}>
              📜 {shoola.classicalRationale}
            </p>
          )}
        </div>
      )}

      {/* ========================================================================= */}
      {/* 3. AYURVEDIC CONSTITUTION (PRAKRITI) & DOSHA BREAKDOWN                    */}
      {/* ========================================================================= */}
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

        {/* ORGAN VULNERABILITIES & DIETARY DIRECTIVES */}
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
