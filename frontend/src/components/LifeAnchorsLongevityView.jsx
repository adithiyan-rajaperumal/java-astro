import React from 'react';
import { t } from '../i18n/translations';

// =========================================================================
// 6-LANGUAGE ASTROLOGICAL & AYURVEDIC DICTIONARIES
// =========================================================================

const I18N_TERMS = {
  // Rashis
  'Mesha': { en: 'Mesha / Aries', ta: 'மேஷம்', hi: 'मेष', te: 'మేషం', kn: 'ಮೇಷ', ml: 'മേടം' },
  'Vrishabha': { en: 'Vrishabha / Taurus', ta: 'ரிஷபம்', hi: 'वृषभ', te: 'వృషభం', kn: 'ವೃಷಭ', ml: 'ഇടവം' },
  'Mithuna': { en: 'Mithuna / Gemini', ta: 'மிதுனம்', hi: 'मिथुन', te: 'మిథునం', kn: 'ಮಿಥುನ', ml: 'ಮಿഥുനം' },
  'Kataka': { en: 'Kataka / Cancer', ta: 'கடகம்', hi: 'कर्क', te: 'కర్కాటకం', kn: 'ಕರ್ಕಾಟಕ', ml: 'കർക്കടകം' },
  'Simha': { en: 'Simha / Leo', ta: 'சிம்மம்', hi: 'सिंह', te: 'సింహం', kn: 'ಸಿಂಹ', ml: 'ചിങ്ങം' },
  'Kanya': { en: 'Kanya / Virgo', ta: 'கன்னி', hi: 'कन्या', te: 'కన్య', kn: 'ಕನ್ಯಾ', ml: 'കന്നി' },
  'Tula': { en: 'Tula / Libra', ta: 'துலாம்', hi: 'तुला', te: 'తులా', kn: 'ತುಲಾ', ml: 'തുലാം' },
  'Vrishchika': { en: 'Vrishchika / Scorpio', ta: 'விருச்சிகம்', hi: 'वृश्चिक', te: 'వృశ్చికం', kn: 'ವೃಶ್ಚಿಕ', ml: 'വൃശ്ചികം' },
  'Dhanus': { en: 'Dhanus / Sagittarius', ta: 'தனுசு', hi: 'धनु', te: 'ధనుస్సు', kn: 'ಧನು', ml: 'ധനു' },
  'Makara': { en: 'Makara / Capricorn', ta: 'மகரம்', hi: 'मकर', te: 'మకరం', kn: 'ಮಕರ', ml: 'മകരം' },
  'Kumbha': { en: 'Kumbha / Aquarius', ta: 'கும்பம்', hi: 'कुम्भ', te: 'కుంభం', kn: 'ಕುಂಭ', ml: 'കുംഭം' },
  'Meena': { en: 'Meena / Pisces', ta: 'மீனம்', hi: 'मीन', te: 'మీనం', kn: 'ಮೀನ', ml: 'മീനം' },

  // Planets
  'Sun': { en: 'Sun', ta: 'சூரியன்', hi: 'सूर्य', te: 'సూర్యుడు', kn: 'ಸೂರ್ಯ', ml: 'സൂര്യൻ' },
  'Moon': { en: 'Moon', ta: 'சந்திரன்', hi: 'चन्द्र', te: 'చంద్రుడు', kn: 'ಚಂದ್ರ', ml: 'ചന്ദ്രൻ' },
  'Mars': { en: 'Mars', ta: 'செவ்வாய்', hi: 'मंगल', te: 'కుజుడు / మంగళ', kn: 'ಮಂಗಳ', ml: 'ചൊവ്വ' },
  'Mercury': { en: 'Mercury', ta: 'புதன்', hi: 'बुध', te: 'బుధుడు', kn: 'ಬುಧ', ml: 'ബുധൻ' },
  'Jupiter': { en: 'Jupiter', ta: 'குரு', hi: 'गुरु / बृहस्पति', te: 'గురుడు', kn: 'ಗುರು', ml: 'വ്യാഴം / ഗുരു' },
  'Venus': { en: 'Venus', ta: 'சுக்கிரன்', hi: 'शुक्र', te: 'శుక్రుడు', kn: 'ಶುಕ್ರ', ml: 'ശുക്രൻ' },
  'Saturn': { en: 'Saturn', ta: 'சனி', hi: 'शनि', te: 'శని', kn: 'ಶನಿ', ml: 'ശനി' },
  'Rahu': { en: 'Rahu', ta: 'ராகு', hi: 'राहु', te: 'రాహువు', kn: 'ರಾಹು', ml: 'രാഹു' },
  'Ketu': { en: 'Ketu', ta: 'கேது', hi: 'केतु', te: 'కేతువు', kn: 'ಕೇತು', ml: 'കേതു' },
  'Lagna': { en: 'Lagna', ta: 'லக்னம்', hi: 'लग्न', te: 'లగ్నం', kn: 'ಲಗ್ನ', ml: 'ലഗ്നം' },

  // Elements
  'Agni (Fire)': {
    en: 'Agni (Fire)',
    ta: 'அக்னி (நெருப்பு / Fire)',
    hi: 'अग्नि (अग्नि तत्व / Fire)',
    te: 'అగ్ని (అగ్ని తత్త్వం / Fire)',
    kn: 'ಅಗ್ನಿ (ಅಗ್ನಿ ತತ್ವ / Fire)',
    ml: 'അഗ്നി (അഗ്നി തത്വം / Fire)'
  },
  'Prithvi (Earth)': {
    en: 'Prithvi (Earth)',
    ta: 'பிருத்வி (பூமி / Earth)',
    hi: 'पृथ्वी (पृथ्वी तत्व / Earth)',
    te: 'పృథ్వి (భూమి తత్త్వం / Earth)',
    kn: 'ಪೃಥ್ವಿ (ಭೂಮಿ ತತ್ವ / Earth)',
    ml: 'പൃഥ്വി (ഭൂമി തത്വം / Earth)'
  },
  'Vayu (Air)': {
    en: 'Vayu (Air)',
    ta: 'வாயு (காற்று / Air)',
    hi: 'वायु (वायु तत्व / Air)',
    te: 'వాయు (వాయు తత్త్వం / Air)',
    kn: 'ವಾಯು (ವಾಯು ತತ್ವ / Air)',
    ml: 'വായു (വായു തത്വം / Air)'
  },
  'Jala (Water)': {
    en: 'Jala (Water)',
    ta: 'ஜலம் (நீர் / Water)',
    hi: 'जल (जल तत्व / Water)',
    te: 'జలం (నీరు తత్త్వం / Water)',
    kn: 'ಜಲ (ಜಲ ತತ್ವ / Water)',
    ml: 'ജലം (ജല തത്വം / Water)'
  },

  // Modalities
  'CHARA': { en: 'Movable (Chara)', ta: 'சர', hi: 'चर', te: 'చర', kn: 'ಚರ', ml: 'ചര' },
  'STHIRA': { en: 'Fixed (Sthira)', ta: 'ஸ்திர', hi: 'स्थिर', te: 'స్థిర', kn: 'ಸ್ಥಿರ', ml: 'സ്ഥിര' },
  'DWISVABHAVA': { en: 'Dual (Dwisvabhava)', ta: 'உபய', hi: 'द्विस्वभाव', te: 'ద్విస్వభావ', kn: 'ದ್ವಿಸ್ವಭಾವ', ml: 'ദ്വിസ്വഭാവ' },

  // Prakritis
  'Vata-Pitta': { en: 'Vata-Pitta', ta: 'வாத-பித்தம் (Vata-Pitta)', hi: 'वात-पित्त (Vata-Pitta)', te: 'వాత-పిత్తం (Vata-Pitta)', kn: 'ವಾತ-ಪಿತ್ತ (Vata-Pitta)', ml: 'വാത-പിത്തം (Vata-Pitta)' },
  'Pitta-Vata': { en: 'Pitta-Vata', ta: 'பித்த-வாதம் (Pitta-Vata)', hi: 'पित्त-वात (Pitta-Vata)', te: 'పిత్త-వాతం (Pitta-Vata)', kn: 'ಪಿತ್ತ-ವಾತ (Pitta-Vata)', ml: 'പിത്ത-വാതം (Pitta-Vata)' },
  'Kapha-Pitta': { en: 'Kapha-Pitta', ta: 'கப-பித்தம் (Kapha-Pitta)', hi: 'कफ-पित्त (Kapha-Pitta)', te: 'కఫ-పిత్తం (Kapha-Pitta)', kn: 'ಕಫ-ಪಿತ್ತ (Kapha-Pitta)', ml: 'കഫ-പിത്തം (Kapha-Pitta)' },
  'Pitta-Kapha': { en: 'Pitta-Kapha', ta: 'பித்த-கபம் (Pitta-Kapha)', hi: 'पित्त-कफ (Pitta-Kapha)', te: 'పిత్త-కఫం (Pitta-Kapha)', kn: 'ಪಿತ್ತ-ಕಫ (Pitta-Kapha)', ml: 'ಪಿത്ത-കഫം (Pitta-Kapha)' },
  'Vata-Kapha': { en: 'Vata-Kapha', ta: 'வாத-கபம் (Vata-Kapha)', hi: 'वात-कफ (Vata-Kapha)', te: 'వాత-కఫం (Vata-Kapha)', kn: 'ವಾತ-ಕಫ (Vata-Kapha)', ml: 'വാത-കഫം (Vata-Kapha)' },
  'Kapha-Vata': { en: 'Kapha-Vata', ta: 'கப-வாதம் (Kapha-Vata)', hi: 'कफ-वात (Kapha-Vata)', te: 'కఫ-వాతం (Kapha-Vata)', kn: 'ಕಫ-ವಾತ (Kapha-Vata)', ml: 'കഫ-വാതം (Kapha-Vata)' },
  'Pitta Dominant': { en: 'Pitta Dominant', ta: 'பித்த பிரதானம் (Pitta)', hi: 'पित्त प्रधान (Pitta)', te: 'పిత్త ప్రధానం (Pitta)', kn: 'ಪಿತ್ತ ಪ್ರಧಾನ (Pitta)', ml: 'പിത്ത പ്രധാനം (Pitta)' },
  'Vata Dominant': { en: 'Vata Dominant', ta: 'வாத பிரதானம் (Vata)', hi: 'वात प्रधान (Vata)', te: 'వాత ప్రధానం (Vata)', kn: 'ವಾತ ಪ್ರಧಾನ (Vata)', ml: 'വാತ പ്രധാനം (Vata)' },
  'Kapha Dominant': { en: 'Kapha Dominant', ta: 'கப பிரதானம் (Kapha)', hi: 'कफ प्रधान (Kapha)', te: 'ಕಫ ಪ್ರಧಾನ (Kapha)', ml: 'കഫ പ്രധാനം (Kapha)' }
};

const ORGAN_VULNERABILITIES_I18N = {
  'Mesha / Aries in 6th': {
    en: 'Head region, cranial circulation & inflammatory headaches (Aries in 6th House)',
    ta: 'தலைப்பகுதி, மூளை இரத்த ஓட்டம் மற்றும் அழற்சி தலைவலி உணர்திறன் (மேஷம் / 6-ஆம் பாவகம்)',
    hi: 'सिर का क्षेत्र, कपाल परिसंचरण एवं दाहक सिरदर्द संवेदनशीलता (मेष / षष्ठ भाव)',
    te: 'శిరస్సు ప్రాంతం, మెదడు రక్త ప్రసరణ మరియు తలనొప్పి సున్నితత్వం (మేషం / 6వ స్థానం)',
    kn: 'ತಲೆಯ ಭಾಗ, ಮೆದುಳಿನ ರಕ್ತ ಪರಿಚಲನೆ ಮತ್ತು ತಲೆನೋವಿನ ಸೂಕ್ಷ್ಮತೆ (ಮೇಷ / 6ನೇ ಮನೆ)',
    ml: 'തല പ്രദേശം, മസ്തിഷ്ക രക്തചംക്രമണം, തലവേദന സംവേദനക്ഷമത (മേടം / 6-ാം ഭാവം)'
  },
  'Vrishabha / Taurus in 6th': {
    en: 'Throat, vocal cords, thyroid & facial tissue sensitivity (Taurus in 6th House)',
    ta: 'தொண்டை, குரல்வளை, தைராய்டு சுரப்பி மற்றும் முக திசு உணர்திறன் (ரிஷபம் / 6-ஆம் பாவகம்)',
    hi: 'गला, स्वर रज्जु, थायरॉइड ग्रंथि एवं मुख ऊतक संवेदनशीलता (वृषभ / षष्ठ भाव)',
    te: 'గొంతు, స్వరపేటిక, థైరాయిడ్ మరియు ముఖ కణజాల సున్నితత్వం (వృషభం / 6వ స్థానం)',
    kn: 'ಗಂಟಲು, ಧ್ವನಿಪೆಟ್ಟಿಗೆ, ಥೈರಾಯ್ಡ್ ಮತ್ತು ಮುಖದ ಅಂಗಾಂಶಗಳ ಸೂಕ್ಷ್ಮತೆ (ವೃಷಭ / 6ನೇ ಮನೆ)',
    ml: 'തൊണ്ട, വോക്കൽ കോഡ്, തൈറോയ്ഡ്, മുഖ കോശ സംവേദനക്ഷമത (ഇടവം / 6-ാം ഭാവം)'
  },
  'Mithuna / Gemini in 6th': {
    en: 'Respiratory tracts, nervous coordination & shoulder tension (Gemini in 6th House)',
    ta: 'சுவாசக்குழாய், நரம்பு மண்டலம் மற்றும் தோள்பட்டை/கை நரம்பு இறுக்கம் (மிதுனம் / 6-ஆம் பாவகம்)',
    hi: 'श्वसन मार्ग, तंत्रिका तंत्र समन्वय एवं कंधे/भुजाओं का तनाव (मिथुन / षष्ठ भाव)',
    te: 'శ్వాసకోశ మార్గాలు, నాడీ వ్యవస్థ మరియు భుజాల ఒత్తిడి (మిథునం / 6వ స్థానం)',
    kn: 'ಉಸಿರಾಟದ ನಾಳಗಳು, ನರಮಂಡಲ ಮತ್ತು ಭುಜದ ಬಿಗಿತ (ಮಿಥುನ / 6ನೇ ಮನೆ)',
    ml: 'ശ്വസന നാളങ്ങൾ, നാഡീവ്യൂഹം, തോൾ/കൈ ഞരമ്പ് പിരിമുറുക്കം (മിഥുനം / 6-ാം ഭാവം)'
  },
  'Kataka / Cancer in 6th': {
    en: 'Gastric digestion, mucous balance & emotional stress sensitivity (Cancer in 6th House)',
    ta: 'நெஞ்சு/இரைப்பை செரிமானம், சளி சவ்வு மற்றும் உணர்ச்சி சார்ந்த மன அழுத்தம் (கடகம் / 6-ஆம் பாவகம்)',
    hi: 'जठर पाचन, श्लेष्मा संतुलन एवं भावनात्मक तनाव संवेदनशीलता (कर्क / षष्ठ भाव)',
    te: 'జీర్ణకోశం, కఫ సమతుల్యత మరియు భావోద్వేగ ఒత్తిడి సున్నితత్వం (కర్కాటకం / 6వ స్థానం)',
    kn: 'ಜೀರ್ಣಾಂಗ, ಲೋಳೆಯ ಸಮತೋಲನ ಮತ್ತು ಭಾವನಾತ್ಮಕ ಒತ್ತಡದ ಸೂಕ್ಷ್ಮತೆ (ಕರ್ಕಾಟಕ / 6ನೇ ಮನೆ)',
    ml: 'ദഹനേന്ദ്രിയം, ശ്ലേഷ്മ സന്തുലിതാവസ്ഥ, വൈകാരിക സമ്മർദ്ദം (കർക്കടകം / 6-ാം ഭാവം)'
  },
  'Simha / Leo in 6th': {
    en: 'Upper abdomen, digestive fire (Jatharagni) & cardiovascular vitality (Leo in 6th House)',
    ta: 'மேல் வயிறு, ஜாடராக்னி (செரிமான தீ) மற்றும் இதய சுற்றோட்ட பலம் (சிம்மம் / 6-ஆம் பாவகம்)',
    hi: 'ऊपरी पेट, जठराग्नि (पाचन अग्नि) एवं हृदय संवहनी जीवन शक्ति (सिंह / षष्ठ भाव)',
    te: 'ఎగువ ఉదరం, జఠరాగ్ని (జీర్ణక్రియ) మరియు హృదయ రక్త ప్రసరణ (సింహం / 6వ స్థానం)',
    kn: 'ಮೇಲ್ಹೊಟ್ಟೆ, ಜಠರಾಗ್ನಿ (ಜೀರ್ಣ ಶಕ್ತಿ) ಮತ್ತು ಹೃದಯ ರಕ್ತಪರಿಚಲನೆ (ಸಿಂಹ / 6ನೇ ಮನೆ)',
    ml: 'മേൽവയർ, ജഠരാഗ്നി (ദഹന ശക്തി), ഹൃദയ രക്തചംക്രമണം (ചിങ്ങം / 6-ാം ഭാവം)'
  },
  'Kanya / Virgo in 6th': {
    en: 'Intestinal assimilation, nutrient uptake & gut microbiome balance (Virgo in 6th House)',
    ta: 'குடல் பகுதி, சத்து உறிஞ்சுதல் மற்றும் குடல் நுண்ணுயிரி சமநிலை (கன்னி / 6-ஆம் பாவகம்)',
    hi: 'आंतों का अवशोषण, पोषक तत्व ग्रहण एवं आंत माइक्रोबायोम संतुलन (कन्या / षष्ठ भाव)',
    te: 'పేగుల శోషణ, పోషకాల గ్రహణ మరియు జీర్ణ వ్యవస్థ సమతుల్యత (కన్య / 6వ స్థానం)',
    kn: 'ಕರುಳಿನ ಹೀರಿಕೊಳ್ಳುವಿಕೆ, ಪೋಷಕಾಂಶ ಗ್ರಹಣ ಮತ್ತು ಜೀರ್ಣಾಂಗ ಸೂಕ್ಷ್ಮತೆ (ಕನ್ಯಾ / 6ನೇ ಮನೆ)',
    ml: 'കുടൽ ആഗിരണം, പോഷക സ്വാംശീകരണം, ദഹന സന്തുലിതാവസ്ഥ (കന്നി / 6-ാം ഭാവം)'
  },
  'Tula / Libra in 6th': {
    en: 'Renal system, lumbar spine & fluid filtration equilibrium (Libra in 6th House)',
    ta: 'சிறுநீரக மண்டலம், இடுப்பு முதுகுத்தண்டு மற்றும் நீர் வடிகட்டுதல் சமநிலை (துலாம் / 6-ஆம் பாவகம்)',
    hi: 'वृक्क (गुर्दा) प्रणाली, कटि मेरुदंड एवं द्रव निस्यंदन संतुलन (तुला / षष्ठ भाव)',
    te: 'మూత్రపిండ వ్యవస్థ, నడుము వెన్నెముక మరియు ద్రవ శుద్ధి సమతుల్యత (తులా / 6వ స్థానం)',
    kn: 'ಮೂತ್ರಪಿಂಡ ವ್ಯವಸ್ಥೆ, ಸೊಂಟದ ಬೆನ್ನುಮೂಳೆ ಮತ್ತು ದ್ರವ ಶುದ್ಧೀಕರಣ ಸಮತೋಲನ (ತುಲಾ / 6ನೇ ಮನೆ)',
    ml: 'വൃക്ക വ്യവസ്ഥ, നട്ടെല്ല്, ദ്രാവക ശുദ്ധീകരണ സന്തുലിതാവസ്ഥ (തുലാം / 6-ാം ഭാവം)'
  },
  'Vrishchika / Scorpio in 6th': {
    en: 'Pelvic region, excretory pathways & reproductive tissue wellness (Scorpio in 6th House)',
    ta: 'இடுப்பு கூம்பு பகுதி, கழிவு வெளியேற்ற பாதைகள் மற்றும் இனப்பெருக்க திசு நலம் (விருச்சிகம் / 6-ஆம் பாவகம்)',
    hi: 'श्रोणि क्षेत्र, उत्सर्जन मार्ग एवं प्रजनन ऊतक स्वास्थ्य (वृश्चिक / षष्ठ भाव)',
    te: 'కటి ప్రాంతం, విసర్జన మార్గాలు మరియు పునరుత్పత్తి కణజాల ఆరోగ్యం (వృశ్చికం / 6వ స్థానం)',
    kn: 'ಶ್ರೋಣಿಯ ಭಾಗ, ವಿಸರ್ಜನಾ ಮಾರ್ಗಗಳು ಮತ್ತು ಪ್ರಜನನ ಅಂಗಾಂಶಗಳ ಕ್ಷೇಮ (ವೃಶ್ಚಿಕ / 6ನೇ ಮನೆ)',
    ml: 'പെൽവിക് പ്രദേശം, വിസർജ്ജന പാതകൾ, പ്രത്യുൽപ്പാദന കോശ ആരോഗ്യം (വൃശ്ചികം / 6-ാം ഭാവം)'
  },
  'Dhanus / Sagittarius in 6th': {
    en: 'Hepatic metabolism, arterial circulation & thigh muscle vitality (Sagittarius in 6th House)',
    ta: 'கல்லீரல் வளர்சிதை மாற்றம், தமனி சுழற்சி மற்றும் தொடை/இடுப்பு தசை வலிமை (தனுசு / 6-ஆம் பாவகம்)',
    hi: 'यकृत चयापचय, धमनी परिसंचरण एवं जांघ/कमर की पेशी शक्ति (धनु / षष्ठ भाव)',
    te: 'కాలేయ జీవక్రియ, ధమని రక్త ప్రసరణ మరియు తొడల కండరాల బలం (ధనుస్సు / 6వ స్థానం)',
    kn: 'ಯಕೃತ್ತಿನ ಚಯಾಪಚಯ, ಅಪಧಮನಿ ಪರಿಚಲನೆ ಮತ್ತು ತೊಡೆಯ ಸ್ನಾಯು ಶಕ್ತಿ (ಧನು / 6ನೇ ಮನೆ)',
    ml: 'കരൾ ഉപാപചയം, ധമനി രക്തചംക്രമണം, തുടയിലെ പേശീബലം (ധനു / 6-ാം ഭാവം)'
  },
  'Makara / Capricorn in 6th': {
    en: 'Knee joints, skeletal density & synovial lubrication regulation (Capricorn in 6th House)',
    ta: 'முழங்கால் மூட்டுகள், எலும்பு அடர்த்தி மற்றும் மூட்டு திரவ ஒழுங்குமுறை (மகரம் / 6-ஆம் பாவகம்)',
    hi: 'घुटने के जोड़, अस्थि घनत्व एवं श्लेषक स्नेहन नियमन (मकर / षष्ठ भाव)',
    te: 'మోకాళ్ళ కీళ్ళు, ఎముకల సాంద్రత మరియు కీళ్ళ ద్రవ నియంత్రణ (మకరం / 6వ స్థానం)',
    kn: 'ಮೊಣಕಾಲು ಕೀಲುಗಳು, ಮೂಳೆ ಸಾಂದ್ರತೆ ಮತ್ತು ಕೀಲು ದ್ರವ ನಿಯಂತ್ರಣ (ಮಕರ / 6ನೇ ಮನೆ)',
    ml: 'മുട്ടുകണ്ണുകൾ, അസ്ഥി സാന്ദ്രത, സന്ധി ദ്രാവക നിയന്ത്രണം (മകരം / 6-ാം ഭാവം)'
  },
  'Kumbha / Aquarius in 6th': {
    en: 'Ankles, neurological impulses & peripheral circulation (Aquarius in 6th House)',
    ta: 'கால் கணுக்கால், நரம்பு சுழற்சி மற்றும் புற இரத்த ஓட்ட நலம் (கும்பம் / 6-ஆம் பாவகம்)',
    hi: 'टखने, तंत्रिका आवेग एवं परिधीय परिसंचरण (कुम्भ / षष्ठ भाव)',
    te: 'చీలమండలు, నాడీ ప్రచోదనాలు మరియు రక్త ప్రసరణ ఆరోగ్యం (కుంభం / 6వ స్థానం)',
    kn: 'ಹಿಮ್ಮಡಿಯ ಕೀಲುಗಳು, ನರ ಪ್ರಚೋದನೆಗಳು ಮತ್ತು ಬಾಹ್ಯ ರಕ್ತ ಪರಿಚಲನೆ (ಕುಂಭ / 6ನೇ ಮನೆ)',
    ml: 'കണങ്കാലുകൾ, നാഡീ പ്രേരണകൾ, രക്തചംക്രമണ ആരോഗ്യം (കുംഭം / 6-ാം ഭാവം)'
  },
  'Meena / Pisces in 6th': {
    en: 'Lymphatic drainage, restorative sleep equilibrium & foot nervous resilience (Pisces in 6th House)',
    ta: 'நிணநீர் வடிகால், ஆழ்ந்த தூக்க சமநிலை மற்றும் பாத நரம்பு பலம் (மீனம் / 6-ஆம் பாவகம்)',
    hi: 'लसीका जल निकासी, सुखद निद्रा संतुलन एवं पैरों की तंत्रिका शक्ति (मीन / षष्ठ भाव)',
    te: 'శోషరస ప్రసరణ, ప్రశాంత నిద్ర మరియు పాదాల నాడీ శక్తి (మీనం / 6వ స్థానం)',
    kn: 'ದುಗ್ಧರಸ ಪರಿಚಲನೆ, ಗಾಢ ನಿದ್ರೆಯ ಸಮತೋಲನ ಮತ್ತು ಪಾದಗಳ ನರ ಬಲ (ಮೀನ / 6ನೇ ಮನೆ)',
    ml: 'ലിംഫറ്റിക് ഡ്രെയിനേജ്, സുഖനിദ്ര, പാദങ്ങളുടെ നാഡീബലം (മീനം / 6-ാം ഭാവം)'
  },

  'Sun as Roga Lord': {
    en: 'Cardiac vitality, ocular acuity & bone mineral absorption (Sun - Roga Lord)',
    ta: 'இதய பலம், கண் பார்வை தெளிவு மற்றும் எலும்பு தாது உறிஞ்சுதல் (சூரியன் - ரோகாதிபதி)',
    hi: 'हृदय जीवन शक्ति, नेत्र ज्योति एवं अस्थि खनिज अवशोषण (सूर्य - रोगाधिपति)',
    te: 'గుండె ఆరోగ్యం, కంటి చూపు మరియు ఎముకల ఖనిజ శోషణ (సూర్యుడు - రోగాధిపతి)',
    kn: 'ಹೃದಯದ ಶಕ್ತಿ, ದೃಷ್ಟಿ ಸ್ಪಷ್ಟತೆ ಮತ್ತು ಮೂಳೆಯ ಖನಿಜ ಹೀರಿಕೊಳ್ಳುವಿಕೆ (ಸೂರ್ಯ - ರೋಗಾಧಿಪತಿ)',
    ml: 'ഹൃദയാരോഗ്യം, കാഴ്ചശക്തി, അസ്ഥി ധാതു ആഗിരണം (സൂര്യൻ - രോഗാധിപൻ)'
  },
  'Moon as Roga Lord': {
    en: 'Body fluid homeostasis, lymphatic regulation & mental peace equilibrium (Moon - Roga Lord)',
    ta: 'உடல் திரவ சமநிலை, நிணநீர் ஒழுங்குமுறை மற்றும் மன அமைதி சமநிலை (சந்திரன் - ரோகாதிபதி)',
    hi: 'शरीर द्रव समस्थिति, लसीका नियमन एवं मानसिक शांति संतुलन (चन्द्र - रोगाधिपति)',
    te: 'శరీర ద్రవ సమతుల్యత, శోషరస నియంత్రణ మరియు మానసిక ప్రశాంతత (చంద్రుడు - రోగాధిపతి)',
    kn: 'ದೇಹದ ದ್ರವ ಸಮತೋಲನ, ದುಗ್ಧರಸ ನಿಯಂತ್ರಣ ಮತ್ತು ಮಾನಸಿಕ ಶಾಂತಿ (ಚಂದ್ರ - ರೋಗಾಧಿಪತಿ)',
    ml: 'ശരീര ദ്രാവക സന്തുലിതാവസ്ഥ, മാനസിക ശാന്തി (ചന്ദ്രൻ - രോഗാധിപൻ)'
  },
  'Mars as Roga Lord': {
    en: 'Blood purification, muscular inflammation & bilious heat moderation (Mars - Roga Lord)',
    ta: 'இரத்த சுத்தி, தசை அழற்சி மற்றும் பித்த உஷ்ணம் தணித்தல் (செவ்வாய் - ரோகாதிபதி)',
    hi: 'रक्त शुद्धि, मांसपेशियों की सूजन एवं पित्त उष्मा शमन (मंगल - रोगाधिपति)',
    te: 'రక్త శుద్ధి, కండరాల వాపు మరియు పిత్త వేడి నియంత్రణ (కుజుడు - రోగాధిపతి)',
    kn: 'ರಕ್ತ ಶುದ್ಧೀಕರಣ, ಸ್ನಾಯು ಉರಿಯೂತ ಮತ್ತು ಪಿತ್ತ ಉಷ್ಣ ನಿಯಂತ್ರಣ (ಮಂಗಳ - ರೋಗಾಧಿಪತಿ)',
    ml: 'രക്തശുദ്ധി, പേശി വീക്കം, പിത്ത ചൂട് നിയന്ത്രണം (ചൊവ്വ - രോഗാധിപൻ)'
  },
  'Mercury as Roga Lord': {
    en: 'Skin barrier, neurological network & digestive enzyme equilibrium (Mercury - Roga Lord)',
    ta: 'தோல் பாதுகாப்பு அரண், நரம்பு மண்டலம் மற்றும் செரிமான என்சைம் சமநிலை (புதன் - ரோகாதிபதி)',
    hi: 'त्वचा संरक्षण, तंत्रिका तंत्र एवं पाचक एंजाइम संतुलन (बुध - रोगाधिपति)',
    te: 'చర్మ రక్షణ, నాడీ వ్యవస్థ మరియు జీర్ణ ఎంజైమ్ల సమతుల్యత (బుధుడు - రోగాధిపతి)',
    kn: 'ಚರ್ಮದ ರಕ್ಷಣೆ, ನರಮಂಡಲ ಮತ್ತು ಜೀರ್ಣಕಾರಿ ಕಿಣ್ವಗಳ ಸಮತೋಲನ (ಬುಧ - ರೋಗಾಧಿಪತಿ)',
    ml: 'ചർമ്മ സംരക്ഷണം, നാഡീവ്യൂഹം, ദഹന എൻസൈം സന്തുലിതാവസ്ഥ (ബുധൻ - രോഗാധിപൻ)'
  },
  'Jupiter as Roga Lord': {
    en: 'Hepatic lipid metabolism & arterial elasticity (Jupiter - Roga Lord)',
    ta: 'கல்லீரல் கொழுப்பு வளர்சிதை மாற்றம் மற்றும் தமனி ஆரோக்கியம் (குரு - ரோகாதிபதி)',
    hi: 'यकृत लिपिड चयापचय एवं धमनी स्वास्थ्य (गुरु - रोगाधिपति)',
    te: 'కాలేయ కొవ్వు జీవక్రియ మరియు ధమనుల ఆరోగ్యం (గురుడు - రోగాధిపతి)',
    kn: 'ಯಕೃತ್ತಿನ ಕೊಬ್ಬಿನ ಚಯಾಪಚಯ ಮತ್ತು ಅಪಧಮನಿಗಳ ಕ್ಷೇಮ (ಗುರು - ರೋಗಾಧಿಪತಿ)',
    ml: 'കരൾ കൊഴുപ്പ് ഉപാപചയം, ധമനി ആരോഗ്യം (വ്യാഴം - രോഗാധിപൻ)'
  },
  'Venus as Roga Lord': {
    en: 'Renal hydration, endocrine harmony & reproductive tissue wellness (Venus - Roga Lord)',
    ta: 'சிறுநீரக நீரேற்றம், நாளமில்லா சுரப்பி சமநிலை மற்றும் இனப்பெருக்க திசு ஆரோக்கியம் (சுக்கிரன் - ரோகாதிபதி)',
    hi: 'वृक्क जलयोजन, अंतःस्रावी सामंजस्य एवं प्रजनन ऊतक स्वास्थ्य (शुक्र - रोगाधिपति)',
    te: 'మూత్రపిండ హైడ్రేషన్, హార్మోన్ల సమతుల్యత మరియు పునరుత్పత్తి ఆరోగ్యం (శుక్రుడు - రోగాధిపతి)',
    kn: 'ಮೂತ್ರಪಿಂಡ ಹೈಡ್ರೇಶನ್, ಅಂತಃಸ್ರಾವಕ ಸಮತೋಲನ ಮತ್ತು ಪ್ರಜನನ ಆರೋಗ್ಯ (ಶುಕ್ರ - ರೋಗಾಧಿಪತಿ)',
    ml: 'വൃക്ക ജലാംശം, എൻഡോക്രൈൻ സന്തുലിതാവസ്ഥ, പ്രജനന ആരോഗ്യം (ശുക്രൻ - രോഗാധിപൻ)'
  },
  'Saturn as Roga Lord': {
    en: 'Joint mobility, dryness prevention & ligamentous flexibility (Saturn - Roga Lord)',
    ta: 'மூட்டு இயக்கம், வறட்சி தவிர்த்தல், தசைநார் நெகிழ்வுத்தன்மை (சனி - ரோகாதிபதி)',
    hi: 'जोड़ों की गतिशीलता, रूखापन निवारण एवं स्नायु लचीलापन (शनि - रोगाधिपति)',
    te: 'కీళ్ళ కదలిక, పొడిబారడం నివారణ మరియు స్నాయువుల వశ్యత (శని - రోగాధిపతి)',
    kn: 'ಕೀಲುಗಳ ಚಲನಶೀಲತೆ, ಶುಷ್ಕತೆ ತಡೆಗಟ್ಟುವಿಕೆ ಮತ್ತು ಅಸ್ಥಿರಜ್ಜುಗಳ ನಮ್ಯತೆ (ಶನಿ - ರೋಗಾಧಿಪತಿ)',
    ml: 'സന്ധി ചലനാത്മകത, വരൾച്ച തടയൽ, ലിഗമെന്റ് വഴക്കം (ശനി - രോഗാധിപൻ)'
  }
};

const LIFESTYLE_DIRECTIVES_I18N = {
  'cooling, grounding, fresh whole foods': {
    en: 'Favor cooling, grounding, fresh whole foods with natural sweet, bitter, and astringent tastes to pacify metabolic heat.',
    ta: 'இயற்கையான இனிப்பு, கசப்பு மற்றும் துவர்ப்பு சுவை கொண்ட குளிர்ச்சியான, புத்துணர்ச்சியூட்டும் முழு உணவுகளை உட்கொள்ளவும்.',
    hi: 'पित्त शांत करने के लिए प्राकृतिक मीठे, कड़वे और कसैले स्वाद वाले शीतल और ताजे सात्विक भोजन का सेवन करें।',
    te: 'పిత్తాన్ని తగ్గించడానికి సహజ తీపి, చేదు మరియు వగరు రుచులు గల చల్లని, తాజా సాత్విక ఆహారాన్ని తీసుకోండి.',
    kn: 'ಪಿತ್ತ ಶಮನಕ್ಕಾಗಿ ನೈಸರ್ಗಿಕ ಸಿಹಿ, ಕಹಿ ಮತ್ತು ಒಗರು ರುಚಿಯ ತಂಪಾದ, ತಾಜಾ ಸಾತ್ವಿಕ ಆಹಾರವನ್ನು ಸೇವಿಸಿ.',
    ml: 'പിത്തം ശമിപ്പിക്കാൻ സ്വാഭാവിക മധുരം, കയ്പ്, ചവർപ്പ് രുചികളുള്ള തണുത്തതും പുതിയതുമായ സാത്വിക ഭക്ഷണം കഴിക്കുക.'
  },
  'Limit pungent spices, sour citrus excess': {
    en: 'Limit pungent spices, sour citrus excess, deep-fried items, and late-night heavy meals.',
    ta: 'காரமான மசாலாக்கள், அதிக புளிப்பு/சிட்ரஸ், எண்ணெயில் பொரித்த உணவுகள் மற்றும் இரவு நேர கனமான உணவுகளைத் தவிர்க்கவும்.',
    hi: 'तीखे मसाले, अत्यधिक खट्टे फल, तले हुए पदार्थ और देर रात के भारी भोजन से परहेज करें।',
    te: 'ఘాటైన మసాలాలు, అధిక పులుపు, వేయించిన పదార్థాలు మరియు రాత్రి వేళల్లో భారీ భోజనాన్ని నివారించండి.',
    kn: 'ಖಾರವಾದ ಮಸಾಲೆಗಳು, ಅತಿಯಾದ ಹುಳಿ, ಎಣ್ಣೆಯಲ್ಲಿ ಕರಿದ ಪದಾರ್ಥಗಳು ಮತ್ತು ತಡರಾತ್ರಿಯ ಭಾರವಾದ ಊಟವನ್ನು ತಪ್ಪಿಸಿ.',
    ml: 'എരിവുള്ള മസാലകൾ, അമിത പുളി, വറുത്ത സാധനങ്ങൾ, രാത്രി വൈകിയുള്ള കനത്ത ഭക്ഷണം എന്നിവ ഒഴിവാക്കുക.'
  },
  'Favor warm, nourishing, easily digestible': {
    en: 'Favor warm, nourishing, easily digestible cooked meals with moderate healthy fats (ghee/sesame).',
    ta: 'மிதமான நெய், நல்லெண்ணெய் சேர்த்த சூடான, எளிதில் செரிமானமாகும் சத்தான சமைத்த உணவுகளை உட்கொள்ளவும்.',
    hi: 'मध्यम घी या तिल के तेल के साथ गर्म, पौष्टिक और सुपाच्य पके हुए भोजन को प्राथमिकता दें।',
    te: 'మితమైన నెయ్యి లేదా నువ్వుల నూనెతో కూడిన వెచ్చని, పోషకమైన, సులభంగా జీర్ణమయ్యే ఆహారాన్ని తీసుకోండి.',
    kn: 'ಮಿತವಾದ ತುಪ್ಪ ಅಥವಾ ಎಳ್ಳೆಣ್ಣೆಯೊಂದಿಗೆ ಬೆಚ್ಚಗಿನ, ಪೌಷ್ಟಿಕ ಮತ್ತು ಸುಲಭವಾಗಿ ಜೀರ್ಣವಾಗುವ ಆಹಾರವನ್ನು ಸೇವಿಸಿ.',
    ml: 'മിതമായ നെയ്യ് അല്ലെങ്കിൽ നല്ലെണ്ണ ചേർത്ത ചൂടുള്ളതും എളുപ്പത്തിൽ ദഹിക്കുന്നതുമായ പോഷകാഹാരം കഴിക്കുക.'
  },
  'Maintain consistent meal schedules': {
    en: 'Maintain consistent meal schedules; avoid dry, cold, raw, and carbonated items.',
    ta: 'வழக்கமான நேரத்திற்கு உணவருந்தவும்; உலர்ந்த, குளிர்ந்த, பச்சையான மற்றும் கார்பனேற்றப்பட்ட உணவுகளைத் தவிர்க்கவும்.',
    hi: 'नियमित समय पर भोजन करें; सूखे, ठंडे, कच्चे और कार्बोनेटेड पदार्थों से बचें।',
    te: 'సరైన సమయానికి భోజనం చేయండి; పొడి, చల్లని, పచ్చి మరియు కార్బోనేటేడ్ పదార్థాలను నివారించండి.',
    kn: 'ನಿಯಮಿತ ಸಮಯಕ್ಕೆ ಊಟ ಮಾಡಿ; ಒಣಗಿದ, ತಣ್ಣನೆಯ, ಹಸಿ ಮತ್ತು ಕಾರ್ಬೊನೇಟೆಡ್ ಪದಾರ್ಥಗಳನ್ನು ತಪ್ಪಿಸಿ.',
    ml: 'ചിട്ടയായ സമയത്ത് ഭക്ഷണം കഴിക്കുക; ഉണങ്ങിയതും തണുത്തതും പച്ചയായതുമായ ഭക്ഷണങ്ങൾ ഒഴിവാക്കുക.'
  },
  'Favor light, warm, dry, and mildly spiced': {
    en: 'Favor light, warm, dry, and mildly spiced foods to kindle digestive fire.',
    ta: 'செரிமான அக்னியைத் தூண்ட இலகுவான, சூடான மற்றும் மிதமான மசாலா சேர்த்த உணவுகளை உட்கொள்ளவும்.',
    hi: 'जठराग्नि को प्रदीप्त करने के लिए हल्का, गर्म और हल्के मसालों वाला भोजन करें।',
    te: 'జీర్ణక్రియను మెరుగుపరచడానికి తేలికపాటి, వెచ్చని మరియు మితమైన మసాలాలు గల ఆహారాన్ని తీసుకోండి.',
    kn: 'ಜೀರ್ಣಶಕ್ತಿಯನ್ನು ಹೆಚ್ಚಿಸಲು ಹಗುರವಾದ, ಬೆಚ್ಚಗಿನ ಮತ್ತು ಮಿತವಾದ ಮಸಾಲೆಗಳ ಆಹಾರವನ್ನು ಸೇವಿಸಿ.',
    ml: 'ദഹനശക്തി വർദ്ധിപ്പിക്കുന്നതിന് ലഘുവായതും ചൂടുള്ളതുമായ ഭക്ഷണം കഴിക്കുക.'
  },
  'Minimize heavy dairy, refined sugars': {
    en: 'Minimize heavy dairy, refined sugars, iced beverages, and daytime sleep.',
    ta: 'கனமான பால் பொருட்கள், சர்க்கரை, குளிர்பானங்கள் மற்றும் உணவுக்குப் பின் உடனடியாக உறங்குவதைத் தவிர்க்கவும்.',
    hi: 'भारी डेयरी उत्पाद, परिष्कृत चीनी, ठंडे पेय और दिन में सोने से बचें।',
    te: 'భారీ పాల ఉత్పత్తులు, చక్కర, చల్లని పానీయాలు మరియు పగటి నిద్రను నివారించండి.',
    kn: 'ಭಾರವಾದ ಹಾಲಿನ ಉತ್ಪನ್ನಗಳು, ಸಕ್ಕರೆ, ತಂಪು ಪಾನೀಯಗಳು ಮತ್ತು ಹಗಲು ನಿದ್ರೆಯನ್ನು ತಪ್ಪಿಸಿ.',
    ml: 'കനത്ത പാലുൽപ്പന്നങ്ങൾ, പഞ്ചസാര, തണുത്ത പാനീയങ്ങൾ, പകൽ ഉറക്കം എന്നിവ ഒഴിവാക്കുക.'
  },
  'Incorporate gentle daily Pranayama': {
    en: 'Incorporate gentle daily Pranayama (Nadi Shodhana/Sheetali) and prioritize regular sleep rhythm to cultivate Ojas.',
    ta: 'ஓஜஸ் (உயிர் நோய் எதிர்ப்பு சக்தி) பாதுகாக்க தினசரி நாடி சுத்தி/சீத்தளி பிராணாயாமம் மற்றும் சீரான தூக்க முறையைப் பின்பற்றவும்.',
    hi: 'ओजस संरक्षण के लिए नित्य नाड़ी शोधन/शीतली प्राणायाम और नियमित निद्रा चक्र का पालन करें।',
    te: 'ఓజస్సు (రోగనిరోధక శక్తి) పెంపొందించడానికి రోజువారీ నాడీ శోధన/శీతలీ ప్రాణాయామం మరియు క్రమమైన నిద్రను పాటించండి.',
    kn: 'ಓಜಸ್ಸು ರಕ್ಷಣೆಗಾಗಿ ದಿನನಿತ್ಯ ನಾಡಿ ಶೋಧನ/ಶೀತಲೀ ಪ್ರಾಣಾಯಾಮ ಮತ್ತು ನಿಯಮಿತ ನಿದ್ರೆಯ ಕ್ರಮವನ್ನು ಅನುಸರಿಸಿ.',
    ml: 'ഓജസ്സ് സംരക്ഷിക്കാൻ ദിവസവും നാഡീ ശോധന/ശീതളീ പ്രാണായാമവും ചിട്ടയായ ഉറക്കവും ശീലമാക്കുക.'
  }
};

export default function LifeAnchorsLongevityView({ chartData, language = 'en' }) {
  if (!chartData) return null;

  const anchors = chartData.lifeAnchors;
  const health = chartData.ayurvedicHealth;
  const ayurdaya = chartData.ayurdayaProfile;

  // Ayurvedic Dosha Fallbacks
  const vataPct = health?.doshaPercentages?.Vata || 33;
  const pittaPct = health?.doshaPercentages?.Pitta || 34;
  const kaphaPct = health?.doshaPercentages?.Kapha || 33;

  // Longevity Styling
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
      case 'pair3_lagna_and_moon':
        return language === 'ta' ? '3. லக்னம் & சந்திரன்' :
               language === 'hi' ? '3. लग्न और चन्द्र' :
               language === 'te' ? '3. లగ్నం & చంద్రుడు' :
               language === 'kn' ? '3. ಲಗ್ನ & ಚಂದ್ರ' :
               language === 'ml' ? '3. ലഗ്നവും ചന്ദ്രനും' :
               '3. Lagna & Moon';
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
    if (!text || language === 'en') return text;
    let res = text;
    ['CHARA', 'STHIRA', 'DWISVABHAVA'].forEach(m => {
      if (I18N_TERMS[m]?.[language]) {
        res = res.replaceAll(m, I18N_TERMS[m][language]);
      }
    });
    ['Sun', 'Moon', 'Mars', 'Mercury', 'Jupiter', 'Venus', 'Saturn', 'Rahu', 'Ketu', 'Lagna'].forEach(p => {
      if (I18N_TERMS[p]?.[language]) {
        res = res.replaceAll(p, I18N_TERMS[p][language]);
      }
    });
    ['Mesha', 'Vrishabha', 'Mithuna', 'Kataka', 'Simha', 'Kanya', 'Tula', 'Vrishchika', 'Dhanus', 'Makara', 'Kumbha', 'Meena'].forEach(r => {
      if (I18N_TERMS[r]?.[language]) {
        res = res.replaceAll(r, I18N_TERMS[r][language]);
      }
    });
    return res;
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
    if (!prakriti) return '';
    return I18N_TERMS[prakriti]?.[language] || I18N_TERMS[prakriti]?.['en'] || prakriti;
  };

  const translateLagnaElement = (elem) => {
    if (!elem) return '';
    let res = elem;
    ['Agni (Fire)', 'Prithvi (Earth)', 'Vayu (Air)', 'Jala (Water)'].forEach(e => {
      if (I18N_TERMS[e]?.[language]) {
        res = res.replaceAll(e, I18N_TERMS[e][language]);
      }
    });
    ['Mesha', 'Vrishabha', 'Mithuna', 'Kataka', 'Simha', 'Kanya', 'Tula', 'Vrishchika', 'Dhanus', 'Makara', 'Kumbha', 'Meena'].forEach(r => {
      if (I18N_TERMS[r]?.[language]) {
        res = res.replaceAll(r, I18N_TERMS[r][language]);
      }
    });
    return res;
  };

  const translateRogaSthana = (sign, lord) => {
    if (!sign) return '';
    const localizedSign = I18N_TERMS[sign]?.[language] || sign;
    const localizedLord = I18N_TERMS[lord]?.[language] || lord || '';
    return `${localizedSign} (${localizedLord})`;
  };

  const translateOrganVulnerability = (text) => {
    if (!text) return '';
    for (const [key, map] of Object.entries(ORGAN_VULNERABILITIES_I18N)) {
      if (text.includes(key)) {
        return map[language] || map['en'] || text;
      }
    }
    if (text.includes('Longevity resilience & chronic vitality maintenance governed by 8th Lord')) {
      let lordMatch = text.replace(/Longevity resilience & chronic vitality maintenance governed by 8th Lord /g, '');
      lordMatch = translateLagnaElement(translateModality(lordMatch));
      if (language === 'ta') return `8-ஆம் அதிபதி ${lordMatch} அமைப்பால் நீண்ட ஆயுள் மற்றும் நோய் எதிர்ப்பு ஆற்றல் பராமரிப்பு`;
      if (language === 'hi') return `अष्टमेश ${lordMatch} व्यवस्था द्वारा दीर्घायु एवं रोग प्रतिरोधक शक्ति का संतुलन`;
      if (language === 'te') return `8వ అధిపతి ${lordMatch} ద్వారా దీర్ఘాయుష్షు మరియు రోగనిరోధక శక్తి నిర్వహణ`;
      if (language === 'kn') return `8ನೇ ಅಧಿಪತಿ ${lordMatch} ಪ್ರಭಾವದಿಂದ ದೀರ್ಘಾಯುಷ್ಯ ಮತ್ತು ರೋಗನಿರೋಧಕ ಶಕ್ತಿ ಸಂರಕ್ಷಣೆ`;
      if (language === 'ml') return `8-ാം അധിപൻ ${lordMatch} വഴിയുള്ള ദീർഘായുസ്സും പ്രതിരോധശേഷി സംരക്ഷണവും`;
      return `Longevity resilience & chronic vitality maintenance governed by 8th Lord ${lordMatch}`;
    }
    return text;
  };

  const translateLifestyleDirective = (text) => {
    if (!text) return '';
    for (const [key, map] of Object.entries(LIFESTYLE_DIRECTIVES_I18N)) {
      if (text.includes(key)) {
        return map[language] || map['en'] || text;
      }
    }
    return text;
  };

  const num = anchors?.numerology;
  const deities = anchors?.deities;
  const gemology = anchors?.gemology;
  const dir = anchors?.directions;
  const struct = anchors?.structuralAnchors;
  const luckyDay = anchors?.luckyDay;
  const luckyDates = anchors?.luckyDates;

  // Localized Deity Rationale
  const ishtaRationale = language === 'ta'
    ? (deities?.ishtaDevataRationaleTamil || deities?.ishtaDevataRationale)
    : (deities?.ishtaDevataRationaleEnglish || deities?.ishtaDevataRationale);

  const dharmaRationale = language === 'ta'
    ? (deities?.dharmaDevataRationaleTamil || deities?.dharmaDevataRationale)
    : (deities?.dharmaDevataRationaleEnglish || deities?.dharmaDevataRationale || t('dharmaDevataDefaultDesc', language));

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '22px' }}>

      {/* ========================================================================= */}
      {/* 1. SPIRITUAL & DEITY ANCHORS (DEIVA PULLIGAL)                             */}
      {/* ========================================================================= */}
      <div className="card" style={{ background: 'linear-gradient(135deg, rgba(255,255,255,0.03) 0%, rgba(212,175,55,0.06) 100%)', border: '1px solid rgba(212,175,55,0.25)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '10px', marginBottom: '16px' }}>
          <h3 style={{ margin: 0, color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            🕉️ {t('spiritualAnchorsTitle', language)}
          </h3>
          <span style={{ fontSize: '12px', padding: '4px 12px', borderRadius: '14px', background: 'rgba(212,175,55,0.15)', color: 'var(--accent-gold)', border: '1px solid rgba(212,175,55,0.3)', fontWeight: 'bold' }}>
            {deities?.karakamsaSignD9 ? `${t('karakamsaAnchor', language)}: ${I18N_TERMS[deities.karakamsaSignD9]?.[language] || deities.karakamsaSignD9} (D9)` : t('karakamsaAnchor', language)}
          </span>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '14px', marginBottom: '16px' }}>
          {/* Ishta Devata */}
          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              🙏 {t('ishtaDevataHeader', language)}
            </div>
            <div style={{ fontSize: '17px', fontWeight: 'bold', color: 'var(--accent-gold)', marginBottom: '6px' }}>
              {language === 'ta' ? deities?.ishtaDevataTamil : deities?.ishtaDevata}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
              {ishtaRationale}
            </div>
          </div>

          {/* Kula Devata */}
          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
              <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
                🪔 {t('kulaDevataHeader', language)}
              </span>
              <span style={{
                fontSize: '11px',
                fontWeight: 'bold',
                padding: '2px 8px',
                borderRadius: '10px',
                background: deities?.kulaDevataBlessingStatus === 'BLESSED' ? 'rgba(46, 204, 113, 0.15)' : 'rgba(231, 76, 60, 0.15)',
                color: deities?.kulaDevataBlessingStatus === 'BLESSED' ? '#2ecc71' : '#e74c3c',
                border: deities?.kulaDevataBlessingStatus === 'BLESSED' ? '1px solid rgba(46, 204, 113, 0.3)' : '1px solid rgba(231, 76, 60, 0.3)'
              }}>
                {deities?.kulaDevataBlessingStatus === 'BLESSED' ? t('blessedStatus', language) : t('remedyAdvised', language)}
              </span>
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
              {deities?.kulaDevataRemedy}
            </div>
          </div>

          {/* Dharma Devata */}
          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              ⚖️ {t('dharmaDevataHeader', language)}
            </div>
            <div style={{ fontSize: '16px', fontWeight: 'bold', color: '#3498db', marginBottom: '4px' }}>
              {language === 'ta' ? deities?.dharmaDevataTamil : deities?.dharmaDevata}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
              {dharmaRationale}
            </div>
          </div>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* 2. VEDIC GEMOLOGY ENGINE (LUCKY RATNAM & RULES)                          */}
      {/* ========================================================================= */}
      <div className="card">
        <h3 style={{ margin: '0 0 16px 0', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
          💎 {t('gemologyTitle', language)}
        </h3>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '14px', marginBottom: '16px' }}>
          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              👑 {t('primaryGem', language)}
            </div>
            <div style={{ fontSize: '18px', fontWeight: 'bold', color: 'var(--accent-gold)', marginBottom: '4px' }}>
              {language === 'ta' ? (gemology?.primaryGemstoneTamil || gemology?.primaryGemstone) : gemology?.primaryGemstone}
            </div>
            <div style={{ fontSize: '12px', color: '#3498db' }}>
              {t('substitute', language)}: {gemology?.secondarySubstitute}
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              💍 {t('metalAndFinger', language)}
            </div>
            <div style={{ fontSize: '13px', fontWeight: 'bold', color: 'var(--text-primary)', marginBottom: '4px' }}>
              {gemology?.recommendedMetal}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
              👉 {gemology?.recommendedFinger}
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              ⏰ {t('activationTimingDay', language)}
            </div>
            <div style={{ fontSize: '13px', fontWeight: 'bold', color: '#2ecc71', marginBottom: '4px' }}>
              {gemology?.activationDayAndTiming}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
              {gemology?.astrologicalRationale}
            </div>
          </div>
        </div>

        {/* Forbidden Companion Gems Anti-Pattern */}
        {gemology?.forbiddenCompanionGems && gemology.forbiddenCompanionGems.length > 0 && (
          <div style={{ background: 'rgba(231, 76, 60, 0.08)', border: '1px solid rgba(231, 76, 60, 0.25)', borderRadius: '8px', padding: '12px' }}>
            <strong style={{ fontSize: '12px', color: '#e74c3c', display: 'block', marginBottom: '6px' }}>
              ⚠️ {t('incompatibleGemsTitle', language)}
            </strong>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
              {gemology.forbiddenCompanionGems.map((gem, idx) => (
                <span key={idx} style={{
                  fontSize: '11px',
                  padding: '4px 10px',
                  borderRadius: '10px',
                  background: 'rgba(231, 76, 60, 0.15)',
                  color: '#e74c3c',
                  border: '1px solid rgba(231, 76, 60, 0.3)'
                }}>
                  ⛔ {gem}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* ========================================================================= */}
      {/* 3. NUMEROLOGY & LUCKY ELEMENTS                                            */}
      {/* ========================================================================= */}
      <div className="card">
        <h3 style={{ margin: '0 0 16px 0', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
          🔢 {t('numerologyTitle', language)}
        </h3>

        {/* Numbers Summary Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '14px', marginBottom: '18px' }}>
          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px', textAlign: 'center' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              🚗 {t('driverNo', language)}
            </div>
            <div style={{ fontSize: '28px', fontWeight: 'bold', color: 'var(--accent-gold)' }}>
              {num?.radicalDriverNumber}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
              {t('rulerLabel', language)}: {I18N_TERMS[num?.radicalRulingPlanet]?.[language] || num?.radicalRulingPlanet}
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px', textAlign: 'center' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              🎯 {t('conductorNo', language)}
            </div>
            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#3498db' }}>
              {num?.destinyConductorNumber}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
              {t('rulerLabel', language)}: {I18N_TERMS[num?.destinyRulingPlanet]?.[language] || num?.destinyRulingPlanet}
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px', textAlign: 'center' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              🪐 {t('planetNo', language)}
            </div>
            <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#2ecc71' }}>
              {num?.astrologicalPlanetNumber}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
              {t('rulerLabel', language)}: {I18N_TERMS[num?.astrologicalPlanetName]?.[language] || num?.astrologicalPlanetName}
            </div>
          </div>
        </div>

        {/* Number Compatibility Matrix */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '12px', marginBottom: '18px' }}>
          <div style={{ background: 'rgba(46, 204, 113, 0.05)', border: '1px solid rgba(46, 204, 113, 0.25)', borderRadius: '8px', padding: '12px' }}>
            <div style={{ fontSize: '11px', color: '#2ecc71', fontWeight: 'bold', marginBottom: '6px' }}>
              💚 {t('friendlyNumbers', language)}
            </div>
            <div style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--text-primary)' }}>
              {num?.friendlyNumbers?.join(', ')}
            </div>
          </div>

          <div style={{ background: 'rgba(241, 196, 15, 0.05)', border: '1px solid rgba(241, 196, 15, 0.25)', borderRadius: '8px', padding: '12px' }}>
            <div style={{ fontSize: '11px', color: '#f1c40f', fontWeight: 'bold', marginBottom: '6px' }}>
              💛 {t('neutralNumbers', language)}
            </div>
            <div style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--text-primary)' }}>
              {num?.neutralNumbers?.join(', ')}
            </div>
          </div>

          <div style={{ background: 'rgba(231, 76, 60, 0.05)', border: '1px solid rgba(231, 76, 60, 0.25)', borderRadius: '8px', padding: '12px' }}>
            <div style={{ fontSize: '11px', color: '#e74c3c', fontWeight: 'bold', marginBottom: '6px' }}>
              💔 {t('enemyNumbers', language)}
            </div>
            <div style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--text-primary)' }}>
              {num?.enemyNumbers?.join(', ')}
            </div>
          </div>
        </div>

        {/* Conflict Resolution Notes */}
        {num?.conflictResolutionNotes && (
          <div style={{ background: 'rgba(212,175,55,0.08)', border: '1px solid rgba(212,175,55,0.3)', borderRadius: '8px', padding: '12px', marginBottom: '18px' }}>
            <div style={{ fontSize: '12px', color: 'var(--accent-gold)', lineHeight: '1.4' }}>
              🌉 {num.conflictResolutionNotes}
            </div>
          </div>
        )}

        {/* Monthly Lucky Dates Matrix */}
        <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px', marginBottom: '16px' }}>
          <h4 style={{ margin: '0 0 10px 0', fontSize: '13px', color: 'var(--accent-gold)' }}>
            📅 {t('monthlyLuckyDatesTitle', language)}
          </h4>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '10px' }}>
            <div>
              <span style={{ fontSize: '11px', color: '#2ecc71', display: 'block', marginBottom: '4px', fontWeight: 'bold' }}>
                🌟 {t('primaryLuckyDates', language)}
              </span>
              <span style={{ fontSize: '13px', color: 'var(--text-primary)', fontWeight: '500' }}>
                {luckyDates?.primaryLuckyDates?.join(', ')}
              </span>
            </div>

            <div>
              <span style={{ fontSize: '11px', color: '#3498db', display: 'block', marginBottom: '4px', fontWeight: 'bold' }}>
                ✨ {t('secondaryFriendlyDates', language)}
              </span>
              <span style={{ fontSize: '13px', color: 'var(--text-primary)' }}>
                {luckyDates?.secondaryFriendlyDates?.join(', ')}
              </span>
            </div>

            <div>
              <span style={{ fontSize: '11px', color: '#e74c3c', display: 'block', marginBottom: '4px', fontWeight: 'bold' }}>
                ⛔ {t('datesToAvoid', language)}
              </span>
              <span style={{ fontSize: '13px', color: 'var(--text-primary)' }}>
                {luckyDates?.datesToAvoid?.join(', ')}
              </span>
            </div>
          </div>

          {luckyDates?.transitCautionNotes && (
            <div style={{ marginTop: '10px', fontSize: '11px', color: '#e67e22' }}>
              ⚠️ {luckyDates.transitCautionNotes}
            </div>
          )}
        </div>

        {/* Lucky Weekday & Auspicious Directions */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '14px' }}>
          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              ☀️ {t('luckyWeekdayTitle', language)}
            </div>
            <div style={{ fontSize: '15px', fontWeight: 'bold', color: 'var(--accent-gold)', marginBottom: '4px' }}>
              {luckyDay?.vedicWeekdayName}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
              {luckyDay?.luckySignifications}
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              🧭 {t('auspiciousDirectionsTitle', language)}
            </div>
            <div style={{ fontSize: '13px', color: 'var(--text-primary)', marginBottom: '4px' }}>
              🏡 <strong>{t('permanentVastu', language)}:</strong> {dir?.permanentVastuDirection}
            </div>
            <div style={{ fontSize: '13px', color: 'var(--text-primary)' }}>
              ✈️ <strong>{t('travelDirection', language)}:</strong> {dir?.travelDirection}
            </div>
          </div>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* 4. STRUCTURAL ASTROLOGICAL ANCHORS                                        */}
      {/* ========================================================================= */}
      <div className="card">
        <h3 style={{ margin: '0 0 16px 0', color: 'var(--accent-gold)', display: 'flex', alignItems: 'center', gap: '8px' }}>
          🏛️ {t('structuralAnchorsTitle', language)}
        </h3>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '14px' }}>
          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              💪 {t('physicalVitalityAnchor', language)}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
              {struct?.physicalVitalityAnchor}
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              🌟 {t('socialStatusAnchor', language)}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
              {struct?.arudhaLagna}
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              🧠 {t('mindResilienceAnchor', language)}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
              {struct?.mindAnchorResilience}
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              💰 {t('karmaAnchor', language)}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-primary)', lineHeight: '1.5' }}>
              {struct?.karmaAnchorHouse}
            </div>
          </div>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* 5. AYURVEDIC HEALTH & CLASSICAL LONGEVITY (AYURDAYA)                       */}
      {/* ========================================================================= */}
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
              {ayurdaya?.estimatedLifespanCeiling ? `~${ayurdaya.estimatedLifespanCeiling} ${language === 'ta' ? 'வயது' : language === 'hi' ? 'वर्ष' : language === 'te' ? 'సంవత్సరాలు' : language === 'kn' ? 'ವರ್ಷಗಳು' : language === 'ml' ? 'വയസ്സ്' : 'Years'}` : '75 - 90+ Years'}
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

      {/* Ayurvedic Constitution & Dosha Bars */}
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
              {translatePrakriti(health?.dominantPrakriti) || 'Vata-Pitta'}
            </strong>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
            <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
              🔥 {t('lagnaElement', language)}
            </span>
            <strong style={{ fontSize: '15px', color: '#e67e22' }}>
              {translateLagnaElement(health?.lagnaElement) || 'Agni (Fire)'}
            </strong>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
            <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
              🏥 {t('rogaSthana', language)}
            </span>
            <strong style={{ fontSize: '14px', color: 'var(--text-primary)' }}>
              {translateRogaSthana(health?.rogaSthanaSign, health?.rogaLord) || 'Kanya (House 6)'}
            </strong>
          </div>
        </div>

        {/* Visual Dosha Proportion Progress Bars */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '20px' }}>
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
              <span style={{ color: '#3498db', fontWeight: 'bold' }}>💨 {t('vata', language)}</span>
              <span style={{ color: '#3498db', fontWeight: 'bold' }}>{vataPct}%</span>
            </div>
            <div style={{ width: '100%', height: '8px', background: 'rgba(255,255,255,0.08)', borderRadius: '4px', overflow: 'hidden' }}>
              <div style={{ width: `${vataPct}%`, height: '100%', background: 'linear-gradient(90deg, #2980b9, #3498db)', borderRadius: '4px', transition: 'width 0.6s ease' }} />
            </div>
          </div>

          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
              <span style={{ color: '#e74c3c', fontWeight: 'bold' }}>🔥 {t('pitta', language)}</span>
              <span style={{ color: '#e74c3c', fontWeight: 'bold' }}>{pittaPct}%</span>
            </div>
            <div style={{ width: '100%', height: '8px', background: 'rgba(255,255,255,0.08)', borderRadius: '4px', overflow: 'hidden' }}>
              <div style={{ width: `${pittaPct}%`, height: '100%', background: 'linear-gradient(90deg, #c0392b, #e74c3c)', borderRadius: '4px', transition: 'width 0.6s ease' }} />
            </div>
          </div>

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

        {/* Organ Vulnerabilities & Diet */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '16px' }}>
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
                {language === 'ta' ? 'குறிப்பிடத்தக்க பாதிப்புகள் இல்லை' : language === 'hi' ? 'कोई गंभीर अंग संवेदनशीलता नहीं पाई गई' : language === 'te' ? 'తీవ్రమైన అవయవ సమస్యలు ఏవీ కనుగొనబడలేదు' : language === 'kn' ? 'ಯಾವುದೇ ತೀವ್ರ ಅಂಗ ದೋಷಗಳು ಕಂಡುಬಂದಿಲ್ಲ' : language === 'ml' ? 'കാര്യമായ അവയവ രോഗസാധ്യതകളൊന്നും കണ്ടെത്തിയില്ല' : 'No acute organ vulnerability detected.'}
              </p>
            )}
          </div>

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
                {language === 'ta' ? 'சமச்சீர் சாத்விக உணவு பரிந்துரைக்கப்படுகிறது.' : language === 'hi' ? 'संतुलित सात्विक आहार की सिफारिश की जाती है।' : language === 'te' ? 'సమతుల్య సాత్విక ఆహారం సిఫార్సు చేయబడింది.' : language === 'kn' ? 'ಸಮತೋಲಿತ ಸಾತ್ವಿಕ ಆಹಾರವನ್ನು ಶಿಫಾರಸು ಮಾಡಲಾಗಿದೆ.' : language === 'ml' ? 'സന്തുലിതമായ സാത്വിക ഭക്ഷണം ശുപാർശ ചെയ്യുന്നു.' : 'Balanced Sattvic diet recommended.'}
              </p>
            )}
          </div>
        </div>

      </div>

    </div>
  );
}
