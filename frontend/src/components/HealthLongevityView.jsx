import React, { useState } from 'react';
import { t } from '../i18n/translations';

const ORGAN_VULNERABILITIES_I18N = {
  'Mars in House': {
    en: 'Acute inflammatory spikes, muscular strain & bile heat sensitivity (Mars in Dusthana)',
    ta: 'தீவிர அழற்சி, தசைப்பிடிப்பு மற்றும் பித்த உஷ்ண உணர்திறன் (செவ்வாய் மறைவு ஸ்தானத்தில்)',
    hi: 'तीव्र सूजन, मांसपेशियों में खिंचाव व पित्त उष्मा संवेदनशीलता (मंगल त्रिक भाव में)',
    te: 'తీవ్రమైన వాపు, కండరాల ఒత్తిడి మరియు పిత్త వేడి సున్నితత్వం (కుజుడు దుస్థానంలో)',
    kn: 'ತೀವ್ರ ಉರಿಯೂತ, ಸ್ನಾಯು ಸೆಳೆತ ಮತ್ತು ಪಿತ್ತ ಉಷ್ಣ ಸೂಕ್ಷ್ಮತೆ (ಮಂಗಳ ದುಃಸ್ಥಾನದಲ್ಲಿ)',
    ml: 'തീവ്രമായ വീക്കം, പേശി വലിവ്, പിത്ത ഉഷ്ണ സംവേദനക്ഷമത (ചൊവ്വ ദുരിത ഭാവത്തിൽ)'
  },
  'Saturn in House': {
    en: 'Joint stiffness, chronic dryness, sciatica or tendon fatigue (Saturn in Dusthana)',
    ta: 'மூட்டு விறைப்பு, நாள்பட்ட வறட்சி, நரம்புத் தளர்ச்சி & தசைநார் சோர்வு (சனி மறைவு ஸ்தானத்தில்)',
    hi: 'जोड़ों की जकड़न, पुराना रूखापन, साइटिका या स्नायु थकान (शनि त्रिक भाव में)',
    te: 'కీళ్ల బిగుతు, దీర్ఘకాలిక పొడిబారడం, సయాటికా లేదా స్నాయువు అలసట (శని దుస్థానంలో)',
    kn: 'ಕೀಲು ಬಿಗಿತ, ದೀರ್ಘಕಾಲದ ಶುಷ್ಕತೆ, ಸಿಯಾಟಿಕಾ ಅಥವಾ ಸ್ನಾಯು ಆಯಾಸ (ಶನಿ ದುಃಸ್ಥಾನದಲ್ಲಿ)',
    ml: 'സന്ധി മുറുക്കം, വരൾച്ച, സയാറ്റിക്ക അല്ലെങ്കിൽ ടെൻഡോൺ ക്ഷീണം (ശനി ദുരിത ഭാവത്തിൽ)'
  },
  'Rahu in House': {
    en: 'Environmental allergies, food sensitivities & psychosomatic sleep disturbances (Rahu in Dusthana)',
    ta: 'சுற்றுச்சூழல் ஒவ்வாமை, உணவு உணர்திறன் & தூக்க சுழற்சி மாறுபாடுகள் (ராகு மறைவு ஸ்தானத்தில்)',
    hi: 'पर्यावरणीय एलर्जी, खाद्य संवेदनशीलता एवं अनिद्रा/मानसिक तनाव (राहु त्रिक भाव में)',
    te: 'పర్యావరణ అలెర్జీలు, ఆహార సున్నితత్వం & నిద్రలేమి (రాహువు దుస్థానంలో)',
    kn: 'ಪರಿಸರ ಅಲರ್ಜಿಗಳು, ಆಹಾರ ಸೂಕ್ಷ್ಮತೆ ಮತ್ತು ನಿದ್ರಾಹೀನತೆ (ರಾಹು ದುಃಸ್ಥಾನದಲ್ಲಿ)',
    ml: 'പരിസ്ഥിതി അലർജികൾ, ഭക്ഷണ സംവേദനക്ഷമത, ഉറക്കക്കുറവ് (രാഹു ദുരിത ഭാവത്തിൽ)'
  },
  'Ketu in House': {
    en: 'Sharp intestinal heat, unexpected digestive hypersensitivity & subtle energy depletion (Ketu in Dusthana)',
    ta: 'தீவிர குடல் உஷ்ணம், எதிர்பாராத செரிமான ஒவ்வாமை & ஆற்றல் குறைவு (கேது மறைவு ஸ்தானத்தில்)',
    hi: 'आंतों की तीक्ष्ण गर्मी, अप्रत्याशित पाचन संवेदनशीलता व ऊर्जा ह्रास (केतु त्रिक भाव में)',
    te: 'ప్రేగుల వేడి, ఊహించని జీర్ణ సున్నితత్వం & శక్తి క్షీణత (కేతువు దుస్థానంలో)',
    kn: 'ಕರುಳಿನ ತೀವ್ರ ಉಷ್ಣ, ಅನಿರೀಕ್ಷಿತ ಜೀರ್ಣಕಾರಿ ಸೂಕ್ಷ್ಮತೆ & ಶಕ್ತಿ ಕುಸಿತ (ಕೇತು ದುಃಸ್ಥಾನದಲ್ಲಿ)',
    ml: 'കുടലിലെ തീവ്രമായ ചൂട്, ദഹന സംവേദനക്ഷമത, ഊർജ്ജക്കുറവ് (കേതു ദുരിത ഭാവത്തിൽ)'
  },
  'Sun in House': {
    en: 'Cardiovascular stamina under stress, eyesight sensitivity & bone calcium absorption (Sun in Dusthana)',
    ta: 'இதய உழைப்பு, கண் பார்வை உணர்திறன் & எலும்பு கால்சியம் உறிஞ்சுதல் (சூரியன் மறைவு ஸ்தானத்தில்)',
    hi: 'तनाव में हृदय क्षमता, नेत्र संवेदनशीलता एवं अस्थि कैल्शियम अवशोषण (सूर्य त्रिक भाव में)',
    te: 'గుండె సామర్థ్యం, కంటి చూపు సున్నితత్వం & ఎముకల కాల్షియం శోషణ (సూర్యుడు దుస్థానంలో)',
    kn: 'ಹೃದಯದ ಸಾಮರ್ಥ್ಯ, ದೃಷ್ಟಿ ಸೂಕ್ಷ್ಮತೆ ಮತ್ತು ಮೂಳೆಯ ಕ್ಯಾಲ್ಸಿಯಂ ಹೀರಿಕೊಳ್ಳುವಿಕೆ (ಸೂರ್ಯ ದುಃಸ್ಥಾನದಲ್ಲಿ)',
    ml: 'ഹൃദയ ശേഷി, കാഴ്ച സംവേദനക്ഷമത, അസ്ഥി കാൽസ്യം ആഗിരണം (സൂര്യൻ ദുരിത ഭാവത്തിൽ)'
  },
  'Moon in House': {
    en: 'Lymphatic sluggishness, fluid retention & emotional psychosomatic digestion (Moon in Dusthana)',
    ta: 'நிணநீர் மந்தம், நீர்க்கட்டு & உணர்ச்சிவச செரிமான மாறுபாடுகள் (சந்திரன் மறைவு ஸ்தானத்தில்)',
    hi: 'लसीका मंदता, जल संचय एवं भावनात्मक पाचन असंतुलन (चन्द्र त्रिक भाव में)',
    te: 'శోషరస మందకొడితనం, శరీరంలో నీరు చేరడం & భావోద్వేగ జీర్ణ మార్పులు (చంద్రుడు దుస్థానంలో)',
    kn: 'ದುಗ್ಧರಸ ಮಂದತೆ, ದೇಹದಲ್ಲಿ ನೀರು ಶೇಖರಣೆ ಮತ್ತು ಭಾವನಾತ್ಮಕ ಜೀರ್ಣಕಾರಿ ಅಸಮತೋಲನ (ಚಂದ್ರ ದುಃಸ್ಥಾನದಲ್ಲಿ)',
    ml: 'ലിംഫ് മന്ദത, ശരീരത്തിൽ നീർക്കെട്ട്, വൈകാരിക ദഹന വ്യതിയാനം (ചന്ദ്രൻ ദുരിത ഭാവത്തിൽ)'
  },
  'Venus in House': {
    en: 'Renal hydration balance, endocrine equilibrium & urinary tract health (Venus in Dusthana)',
    ta: 'சிறுநீரக நீரேற்றம், நாளமில்லா சுரப்பி சமநிலை & சிறுநீர்ப்பாதை ஆரோக்கியம் (சுக்கிரன் மறைவு ஸ்தானத்தில்)',
    hi: 'वृक्क जलयोजन, अंतःस्रावी संतुलन एवं मूत्र प्रणाली स्वास्थ्य (शुक्र त्रिक भाव में)',
    te: 'మూత్రపిండ హైడ్రేషన్, హార్మోన్ల సమతుల్యత & మూత్రనాళ ఆరోగ్యం (శుక్రుడు దుస్థానంలో)',
    kn: 'ಮೂತ್ರಪಿಂಡ ಹೈಡ್ರೇಶನ್, ಅಂತಃಸ್ರಾವಕ ಸಮತೋಲನ & ಮೂತ್ರನಾಳದ ಕ್ಷೇಮ (ಶುಕ್ರ ದುಃಸ್ಥಾನದಲ್ಲಿ)',
    ml: 'വൃക്ക ജലാംശം, എൻഡോക്രൈൻ സന്തുലിതാവസ്ഥ, മൂത്രാശയ ആരോഗ്യം (ശുക്രൻ ദുരിത ഭാവത്തിൽ)'
  },
  'Jupiter in House': {
    en: 'Hepatic liver metabolism, lipid balance & arterial circulation (Jupiter in Dusthana)',
    ta: 'கல்லீரல் கொழுப்பு வளர்சிதை மாற்றம், லிபிட் சமநிலை & தமனி சுழற்சி (குரு மறைவு ஸ்தானத்தில்)',
    hi: 'यकृत चयापचय, लिपिड संतुलन एवं धमनी परिसंचरण (गुरु त्रिक भाव में)',
    te: 'కాలేయ జీవక్రియ, లిపిడ్ సమతుల్యత & రక్తనాళాల ప్రసరణ (గురుడు దుస్థానంలో)',
    kn: 'ಯಕೃತ್ತಿನ ಚಯಾಪಚಯ, ಲಿಪಿಡ್ ಸಮತೋಲನ & ಅಪಧಮನಿ ಪರಿಚಲನೆ (ಗುರು ದುಃಸ್ಥಾನದಲ್ಲಿ)',
    ml: 'കരൾ ഉപാപചയം, ലിപിഡ് സന്തുലിതാവസ്ഥ, ധമനി ചംക്രമണം (വ്യാഴം ദുരിത ഭാവത്തിൽ)'
  },
  'Mercury in House': {
    en: 'Enteric nervous system, skin barrier resilience & respiratory bronchial reactivity (Mercury in Dusthana)',
    ta: 'குடல் நரம்பு மண்டலம், சருமப் பாதுகாப்பு அரண் & சுவாசக்குழாய் உணர்திறன் (புதன் மறைவு ஸ்தானத்தில்)',
    hi: 'आंतों का तंत्रिका तंत्र, त्वचा सुरक्षा एवं श्वसन नली संवेदनशीलता (बुध त्रिक भाव में)',
    te: 'ప్రేగుల నాడీ వ్యవస్థ, చర్మ రక్షణ & శ్వాసనాళ సున్నితత్వం (బుధుడు దుస్థానంలో)',
    kn: 'ಕರುಳಿನ ನರಮಂಡಲ, ಚರ್ಮ ರಕ್ಷಣೆ & ಶ್ವಾಸನಾಳದ ಸೂಕ್ಷ್ಮತೆ (ಬುಧ ದುಃಸ್ಥಾನದಲ್ಲಿ)',
    ml: 'കുടൽ നാഡീവ്യൂഹം, ചർമ്മ സംരക്ഷണം, ശ്വാസകോശ സംവേദനക്ഷമത (ബുധൻ ദുരിത ഭാവത്തിൽ)'
  },
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
  },
  'Acute inflammatory spikes, muscular strain & bile heat sensitivity': {
    en: 'Acute inflammatory spikes, muscular strain & bile heat sensitivity',
    ta: 'கடுமையான அழற்சி/சூடு, தசை வலிமை அழுத்தம் மற்றும் பித்த உஷ்ணம்',
    hi: 'तीव्र सूजन, मांसपेशियों में खिंचाव एवं पित्त उष्मा संवेदनशीलता',
    te: 'తీవ్రమైన మంట/వేడి, కండరాల ఒత్తిడి మరియు పిత్త సున్నితత్వం',
    kn: 'ತೀವ್ರ ಉರಿಯೂತ, ಸ್ನಾಯುಗಳ ಸೆಳೆತ ಮತ್ತು ಪಿತ್ತ ಉಷ್ಣ ಸಂವೇದನೆ',
    ml: 'തീവ്രമായ വീക്കം, പേശി ആയാസം, പിത്ത ചൂട് സംവേദനക്ഷമത'
  },
  'Joint stiffness, chronic dryness, sciatica or tendon fatigue': {
    en: 'Joint stiffness, chronic dryness, sciatica or tendon fatigue',
    ta: 'மூட்டு விறைப்பு, நாள்பட்ட வறட்சி, நரம்பு அல்லது தசைநார் சோர்வு',
    hi: 'जोड़ों की जकड़न, पुराना सूखापन, सायटिका या स्नायु थकान',
    te: 'కీళ్ళ బిగుతు, దీర్ఘకాలిక పొడిబారడం మరియు నాడీ అలసట',
    kn: 'ಕೀಲುಗಳ ಬಿಗಿತ, ದೀರ್ಘಕಾಲದ ಶುಷ್ಕತೆ ಮತ್ತು ಸ್ನಾಯುರಜ್ಜು ಆಯಾಸ',
    ml: 'സന്ധി കാഠിന്യം, വിട്ടുമാറാത്ത വരൾച്ച, സയാറ്റിക്ക, സ്നായു ക്ഷീണം'
  },
  'Environmental allergies, food sensitivities & psychosomatic sleep disturbances': {
    en: 'Environmental allergies, food sensitivities & psychosomatic sleep disturbances',
    ta: 'சுற்றுச்சூழல் ஒவ்வாமை, உணவு உணர்திறன் மற்றும் தூக்க சமநிலையின்மை',
    hi: 'पर्यावरणीय एलर्जी, भोजन संवेदनशीलता एवं मनोदैहिक निद्रा विकार',
    te: 'పర్యావరణ అలెర్జీలు, ఆహార సున్నితత్వం మరియు నిద్రలేమి',
    kn: 'ಪರಿಸರ ಅಲರ್ಜಿಗಳು, ಆಹಾರ ಸೂಕ್ಷ್ಮತೆ ಮತ್ತು ನಿದ್ರಾಹೀನತೆ',
    ml: 'പരിസ്ഥിതി അലർജികൾ, ഭക്ഷണ സംവേദനക്ഷമത, ഉറക്ക അസ്വസ്ഥതകൾ'
  },
  'Sharp intestinal heat, unexpected digestive hypersensitivity & subtle energy depletion': {
    en: 'Sharp intestinal heat, unexpected digestive hypersensitivity & subtle energy depletion',
    ta: 'குடல் சூடு, திடீர் செரிமான உணர்திறன் மற்றும் ஆற்றல் குறைவு',
    hi: 'तीव्र आंतों की गर्मी, अप्रत्याशित पाचन संवेदनशीलता एवं ऊर्जा ह्रास',
    te: 'పేగులలో తీవ్ర వేడి, అకస్మాత్తుగా జీర్ణ సున్నితత్వం మరియు శక్తి క్షీణత',
    kn: 'ಕರುಳಿನ ತೀವ್ರ ಉಷ್ಣತೆ, ಅನಿರೀಕ್ಷಿತ ಜೀರ್ಣ ಸೂಕ್ಷ್ಮತೆ ಮತ್ತು ಶಕ್ತಿ ಕುಂಠಿತ',
    ml: 'കുടലിലെ ചൂട്, ദഹന സംവേദനക്ഷമത, ഊർജ്ജക്ഷയം'
  },
  'Cardiovascular stamina under stress, eyesight sensitivity & bone calcium absorption': {
    en: 'Cardiovascular stamina under stress, eyesight sensitivity & bone calcium absorption',
    ta: 'இதய ஆற்றல், பார்வை நரம்பு உணர்திறன் மற்றும் எலும்பு கால்சியம் சமநிலை',
    hi: 'हृदय सहनशक्ति, दृष्टि संवेदनशीलता एवं अस्थि कैल्शियम अवशोषण',
    te: 'గుండె సామర్థ్యం, దృష్టి సున్నితత్వం మరియు ఎముకల కాల్షియం శోషణ',
    kn: 'ಹೃದಯದ ಸಾಮರ್ಥ್ಯ, ದೃಷ್ಟಿ ಸೂಕ್ಷ್ಮತೆ ಮತ್ತು ಮೂಳೆಗಳ ಕ್ಯಾಲ್ಸಿಯಂ ಹೀರಿಕೊಳ್ಳುವಿಕೆ',
    ml: 'ഹൃദയ ക്ഷമത, കാഴ്ച സംവേദനക്ഷമത, അസ്ഥി കാൽസ്യം ആഗിരണം'
  },
  'Lymphatic sluggishness, fluid retention & emotional psychosomatic digestion': {
    en: 'Lymphatic sluggishness, fluid retention & emotional psychosomatic digestion',
    ta: 'நிணநீர் மந்தநிலை, உடல் நீர் சமநிலையின்மை மற்றும் மன அழுத்த செரிமானம்',
    hi: 'लसिका सुस्ती, तरल प्रतिधारण एवं भावनात्मक मनोदैहिक पाचन',
    te: 'శోషరస మందగమనం, శరీర ద్రవాల నిలుపుదల మరియు మానసిక జీర్ణ సమస్యలు',
    kn: 'ದುಗ್ಧರಸ ಮಂದಗತಿ, ದೇಹದ ದ್ರವ ಧಾರಣೆ ಮತ್ತು ಭಾವನಾತ್ಮಕ ಜೀರ್ಣ ಸಮಸ್ಯೆಗಳು',
    ml: 'ലിംഫാറ്റിക് മന്ദത, ശരീരത്തിൽ ദ്രാവകം കെട്ടിക്കിടക്കൽ, വൈകാരിക ദഹനം'
  },
  'Renal hydration balance, endocrine equilibrium & urinary tract health': {
    en: 'Renal hydration balance, endocrine equilibrium & urinary tract health',
    ta: 'சிறுநீரக நீரேற்றம், நாளமில்லா சுரப்பி சமநிலை மற்றும் சிறுநீரக நலன்',
    hi: 'वृक्क जलयोजन संतुलन, अंतःस्रावी संतुलन एवं मूत्र मार्ग स्वास्थ्य',
    te: 'మూత్రపిండాల హైడ్రేషన్, హార్మోన్ల సమతుల్యత మరియు మూత్ర వ్యవస్థ ఆరోగ్యం',
    kn: 'ಮೂತ್ರಪಿಂಡದ ಜಲಸಮತೋಲನ, ಅಂತಃಸ್ರಾವಕ ಸಮತೋಲನ ಮತ್ತು ಮೂತ್ರನಾಳದ ಕ್ಷೇಮ',
    ml: 'വൃക്ക ജലാംശ സന്തുലനം, എൻഡോക്രൈൻ സന്തുലിതാവസ്ഥ, മൂത്രാശയ ആരോഗ്യം'
  },
  'Hepatic liver metabolism, lipid balance & arterial circulation': {
    en: 'Hepatic liver metabolism, lipid balance & arterial circulation',
    ta: 'கல்லீரல் வளர்சிதை மாற்றம், கொழுப்பு சமநிலை மற்றும் தமனி ரத்த ஓட்டம்',
    hi: 'यकृत चयापचय, लिपिड संतुलन एवं धमनी परिसंचरण',
    te: 'కాలేయ జీవక్రియ, కొవ్వు సమతుల్యత మరియు ధమని రక్త ప్రసరణ',
    kn: 'ಯಕೃತ್ತಿನ ಚಯಾಪಚಯ, ಲಿಪಿಡ್ ಸಮತೋಲನ ಮತ್ತು ಅಪಧಮನಿ ರಕ್ತ ಪರಿಚಲನೆ',
    ml: 'കരൾ ഉപാപചയം, കൊഴുപ്പ് സന്തുലനം, ധമനി രക്തചംക്രമണം'
  },
  'Enteric nervous system, skin barrier resilience & respiratory bronchial reactivity': {
    en: 'Enteric nervous system, skin barrier resilience & respiratory bronchial reactivity',
    ta: 'குடல் நரம்பு மண்டலம், சரும பாதுகாப்பு அரண் மற்றும் சுவாச மூச்சுக்குழாய் நலன்',
    hi: 'आंत्र तंत्रिका तंत्र, त्वचा अवरोधक क्षमता एवं श्वसन ब्रोंकियल प्रतिक्रिया',
    te: 'జీర్ణ నాడీ వ్యవస్థ, చర్మ రక్షణ మరియు శ్వాసనాళాల సున్నితత్వం',
    kn: 'ಜೀರ್ಣ ನರಮಂಡಲ, ಚರ್ಮದ ರಕ್ಷಣೆ ಮತ್ತು ಶ್ವಾಸಕೋಶದ ಸೂಕ್ಷ್ಮತೆ',
    ml: 'ദഹന നാഡീവ്യൂഹം, ചർമ്മ സംരക്ഷണം, ശ്വാസകോശ പ്രതികരണം'
  },
  'Metabolic sensitivity and immune caution': {
    en: 'Metabolic sensitivity and immune caution',
    ta: 'வளர்சிதை மாற்ற உணர்திறன் மற்றும் நோய் எதிர்ப்பு விழிப்புணர்வு',
    hi: 'चयापचय संवेदनशीलता एवं प्रतिरक्षा सतर्कता',
    te: 'జీవక్రియ సున్నితత్వం మరియు రోగనిరోధక జాగ్రత్త',
    kn: 'ಚಯಾಪಚಯ ಸೂಕ್ಷ್ಮತೆ ಮತ್ತು ರೋಗನಿರೋಧಕ ಜಾಗರೂಕತೆ',
    ml: 'ഉപാപചയ സംവേദനക്ഷമതയും പ്രതിരോധ ജാഗ്രതയും'
  },
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

export default function HealthLongevityView({ chartData, language }) {
  const [expandedShoolaIndex, setExpandedShoolaIndex] = useState(null);

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

  const formatPairTitle = (key, ruleApplied) => {
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
               language === 'ml' ? '3. ലಗ್നവും ഹോರಾ ലഗ്നവും' :
               '3. Lagna & Hora Lagna';
      case 'majorityConsensus':
        if (ruleApplied && (ruleApplied.includes('Vishesha') || ruleApplied.includes('Asamvada'))) {
          switch (language) {
            case 'ta': return 'ஒருங்கிணைந்த முடிவு';
            case 'hi': return 'संश्लेषित निर्णय';
            case 'te': return 'సంశ్లేషిత నిర్ణయం';
            case 'kn': return 'ಸಂಶ್ಲೇಷಿತ ನಿರ್ಧಾರ';
            case 'ml': return 'സംശ്ലേഷിത തീരുമാനം';
            default: return 'Synthesis Result';
          }
        }
        if (ruleApplied && (ruleApplied.includes('Tri-Samvada') || ruleApplied.includes('Unanimous'))) {
          switch (language) {
            case 'ta': return 'ஒருமனதான முடிவு';
            case 'hi': return 'सर्वसम्मत निर्णय';
            case 'te': return 'ఏకగ్రీవ నిర్ణయం';
            case 'kn': return 'ಸರ್ವಾನುಮತದ ನಿರ್ಧಾರ';
            case 'ml': return 'ഏകകണ്ഠമായ തീരുമാനം';
            default: return 'Unanimous Consensus';
          }
        }
        return language === 'ta' ? 'பெரும்பான்மை முடிவு' :
               language === 'hi' ? 'बहुमत निर्णय' :
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
    switch (language) {
      case 'ta':
        res = res
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
        break;
      case 'hi':
        res = res
          .replace(/CHARA/g, 'चर')
          .replace(/STHIRA/g, 'स्थिर')
          .replace(/DWISVABHAVA/g, 'द्विस्वभाव')
          .replace(/Lagna/g, 'लग्न')
          .replace(/Hora Lagna/g, 'होरा लग्न')
          .replace(/Moon/g, 'चन्द्र')
          .replace(/Saturn/g, 'शनि')
          .replace(/Sun/g, 'सूर्य')
          .replace(/Mars/g, 'मंगल')
          .replace(/Mercury/g, 'बुध')
          .replace(/Jupiter/g, 'गुरु')
          .replace(/Venus/g, 'शुक्र')
          .replace(/Rahu/g, 'राहु')
          .replace(/Ketu/g, 'केतु');
        break;
      case 'te':
        res = res
          .replace(/CHARA/g, 'చర')
          .replace(/STHIRA/g, 'స్థిర')
          .replace(/DWISVABHAVA/g, 'ద్విస్వభావ')
          .replace(/Lagna/g, 'లగ్నం')
          .replace(/Hora Lagna/g, 'హోరా లగ్నం')
          .replace(/Moon/g, 'చంద్రుడు')
          .replace(/Saturn/g, 'శని')
          .replace(/Sun/g, 'సూర్యుడు')
          .replace(/Mars/g, 'కుజుడు')
          .replace(/Mercury/g, 'బుధుడు')
          .replace(/Jupiter/g, 'గురు')
          .replace(/Venus/g, 'శుక్రుడు')
          .replace(/Rahu/g, 'రాహువు')
          .replace(/Ketu/g, 'కేతువు');
        break;
      case 'kn':
        res = res
          .replace(/CHARA/g, 'ಚರ')
          .replace(/STHIRA/g, 'ಸ್ಥಿರ')
          .replace(/DWISVABHAVA/g, 'ದ್ವಿಸ್ವಭಾವ')
          .replace(/Lagna/g, 'ಲಗ್ನ')
          .replace(/Hora Lagna/g, 'ಹೋರಾ ಲಗ್ನ')
          .replace(/Moon/g, 'ಚಂದ್ರ')
          .replace(/Saturn/g, 'ಶನಿ')
          .replace(/Sun/g, 'ಸೂರ್ಯ')
          .replace(/Mars/g, 'ಮಂಗಳ')
          .replace(/Mercury/g, 'ಬುಧ')
          .replace(/Jupiter/g, 'ಗುರು')
          .replace(/Venus/g, 'ಶುಕ್ರ')
          .replace(/Rahu/g, 'ರಾಹು')
          .replace(/Ketu/g, 'ಕೇತು');
        break;
      case 'ml':
        res = res
          .replace(/CHARA/g, 'ചര')
          .replace(/STHIRA/g, 'സ്ഥിര')
          .replace(/DWISVABHAVA/g, 'ദ്വിസ്വഭാവ')
          .replace(/Lagna/g, 'ലഗ്നം')
          .replace(/Hora Lagna/g, 'ഹോരാ ലഗ്നം')
          .replace(/Moon/g, 'ചന്ദ്രൻ')
          .replace(/Saturn/g, 'ശനി')
          .replace(/Sun/g, 'സൂര്യൻ')
          .replace(/Mars/g, 'ചൊവ്വ')
          .replace(/Mercury/g, 'ബുധൻ')
          .replace(/Jupiter/g, 'ഗുരു')
          .replace(/Venus/g, 'ശുക്രൻ')
          .replace(/Rahu/g, 'രാഹു')
          .replace(/Ketu/g, 'കേതു');
        break;
    }
    return res;
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
    const isA = span === 'Alpayu';
    if (!isP && !isM && !isA) return null;
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
    if (!tier || language === 'en') return tier;
    let res = tier;
    switch (language) {
      case 'ta':
        res = res
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
        break;
      case 'hi':
        res = res
          .replace(/Balarishta \/ Adhama Alpayu/g, 'बालारिष्ट / अधम अल्पायु')
          .replace(/Madhyama Alpayu/g, 'मध्यम अल्पायु')
          .replace(/Uttama Alpayu/g, 'उत्तम अल्पायु')
          .replace(/Adhama Madhyayu/g, 'अधम मध्यायु')
          .replace(/Madhyama Madhyayu/g, 'मध्यम मध्यायु')
          .replace(/Uttama Madhyayu/g, 'उत्तम मध्यायु')
          .replace(/Adhama Poornayu/g, 'अधम पूर्णायु')
          .replace(/Madhyama Poornayu/g, 'मध्यम पूर्णायु')
          .replace(/Paramayu \/ Deerghayu/g, 'परमायु / दीर्घायु')
          .replace(/Years/g, 'वर्ष');
        break;
      case 'te':
        res = res
          .replace(/Balarishta \/ Adhama Alpayu/g, 'బాలారిష్టం / అధమ అల్పాయుష్షు')
          .replace(/Madhyama Alpayu/g, 'మధ్యమ అల్పాయుష్షు')
          .replace(/Uttama Alpayu/g, 'ఉత్తమ అల్పాయుష్షు')
          .replace(/Adhama Madhyayu/g, 'అధమ మధ్యాయుష్షు')
          .replace(/Madhyama Madhyayu/g, 'మధ్యమ మధ్యాయుష్షు')
          .replace(/Uttama Madhyayu/g, 'ఉత్తమ మధ్యాయుష్షు')
          .replace(/Adhama Poornayu/g, 'అధమ పూర్ణాయుష్షు')
          .replace(/Madhyama Poornayu/g, 'మధ్యమ పూర్ణాయుష్షు')
          .replace(/Paramayu \/ Deerghayu/g, 'పరమాయుష్షు / దీర్ఘాయుష్షు')
          .replace(/Years/g, 'సంవత్సరాలు');
        break;
      case 'kn':
        res = res
          .replace(/Balarishta \/ Adhama Alpayu/g, 'ಬಾಲಾರಿಷ್ಟ / ಅಧಮ ಅಲ್ಪಾಯುಷ್ಯ')
          .replace(/Madhyama Alpayu/g, 'ಮಧ್ಯಮ ಅಲ್ಪಾಯುಷ್ಯ')
          .replace(/Uttama Alpayu/g, 'ಉತ್ತಮ ಅಲ್ಪಾಯುಷ್ಯ')
          .replace(/Adhama Madhyayu/g, 'ಅಧಮ ಮಧ್ಯಾಯುಷ್ಯ')
          .replace(/Madhyama Madhyayu/g, 'ಮಧ್ಯಮ ಮಧ್ಯಾಯುಷ್ಯ')
          .replace(/Uttama Madhyayu/g, 'ಉತ್ತಮ ಮಧ್ಯಾಯುಷ್ಯ')
          .replace(/Adhama Poornayu/g, 'ಅಧಮ ಪೂರ್ಣಾಯುಷ್ಯ')
          .replace(/Madhyama Poornayu/g, 'ಮಧ್ಯಮ ಪೂರ್ಣಾಯುಷ್ಯ')
          .replace(/Paramayu \/ Deerghayu/g, 'ಪರಮಾಯುಷ್ಯ / ದೀರ್ಘಾಯುಷ್ಯ')
          .replace(/Years/g, 'ವರ್ಷಗಳು');
        break;
      case 'ml':
        res = res
          .replace(/Balarishta \/ Adhama Alpayu/g, 'ബാലാരിഷ്ടം / അധമ അല്പായുസ്സ്')
          .replace(/Madhyama Alpayu/g, 'മധ്യമ അല്പായുസ്സ്')
          .replace(/Uttama Alpayu/g, 'ഉത്തമ അല്പായുസ്സ്')
          .replace(/Adhama Madhyayu/g, 'അധമ മദ്ധ്യായുസ്സ്')
          .replace(/Madhyama Madhyayu/g, 'മധ്യമ മദ്ധ്യായുസ്സ്')
          .replace(/Uttama Madhyayu/g, 'ഉത്തമ മദ്ധ്യായുസ്സ്')
          .replace(/Adhama Poornayu/g, 'അധമ പൂർണ്ണായുസ്സ്')
          .replace(/Madhyama Poornayu/g, 'മധ്യമ പൂർണ്ണായുസ്സ്')
          .replace(/Paramayu \/ Deerghayu/g, 'പരമായുസ്സ് / ദീർഘായുസ്സ്')
          .replace(/Years/g, 'വർഷങ്ങൾ');
        break;
    }
    return res;
  };

  const translateRashi = (rashi) => {
    if (!rashi) return '';
    if (language === 'en') return rashi;
    const map = {
      'Aries': { ta: 'மேஷம்', hi: 'मेष', te: 'మేషం', kn: 'ಮೇಷ', ml: 'മേടം' },
      'Mesha': { ta: 'மேஷம்', hi: 'मेष', te: 'మేషం', kn: 'ಮೇಷ', ml: 'മേടം' },
      'Taurus': { ta: 'ரிஷபம்', hi: 'वृषभ', te: 'వృషభం', kn: 'ವೃಷಭ', ml: 'ഇടവം' },
      'Vrishabha': { ta: 'ரிஷபம்', hi: 'वृषभ', te: 'వృషభం', kn: 'ವೃಷಭ', ml: 'ഇടവം' },
      'Gemini': { ta: 'மிதுனம்', hi: 'मिथुन', te: 'మిథునం', kn: 'ಮಿಥುನ', ml: 'മിഥുനം' },
      'Mithuna': { ta: 'மிதுனம்', hi: 'मिथुन', te: 'మిథునం', kn: 'ಮಿಥುನ', ml: 'മിഥുനം' },
      'Cancer': { ta: 'கடகம்', hi: 'कर्क', te: 'కర్కాటకం', kn: 'ಕರ್ಕಾಟಕ', ml: 'കർക്കടകം' },
      'Kataka': { ta: 'கடகம்', hi: 'कर्क', te: 'కర్కాటకం', kn: 'ಕರ್ಕಾಟಕ', ml: 'കർക്കടകം' },
      'Karka': { ta: 'கடகம்', hi: 'कर्क', te: 'కర్కాటకం', kn: 'ಕರ್ಕಾಟಕ', ml: 'കർക്കടകം' },
      'Leo': { ta: 'சிம்மம்', hi: 'सिंह', te: 'సింహం', kn: 'ಸಿಂಹ', ml: 'ചിങ്ങം' },
      'Simha': { ta: 'சிம்மம்', hi: 'सिंह', te: 'సింహం', kn: 'ಸಿಂಹ', ml: 'ചിങ്ങം' },
      'Virgo': { ta: 'கன்னி', hi: 'कन्या', te: 'కన్య', kn: 'ಕನ್ಯಾ', ml: 'കന്നി' },
      'Kanya': { ta: 'கன்னி', hi: 'कन्या', te: 'ಕన్య', kn: 'ಕನ್ಯಾ', ml: 'ಕന്നി' },
      'Libra': { ta: 'துலாம்', hi: 'तुला', te: 'తులా', kn: 'ತುಲಾ', ml: 'തുലാം' },
      'Tula': { ta: 'துலாம்', hi: 'तुला', te: 'తులా', kn: 'ತುಲಾ', ml: 'തുലാം' },
      'Scorpio': { ta: 'விருச்சிகம்', hi: 'वृश्चिक', te: 'వృశ్చికం', kn: 'ವೃಶ್ಚಿಕ', ml: 'വൃശ്ചികം' },
      'Vrishchika': { ta: 'விருச்சிகம்', hi: 'वृश्चिक', te: 'వృశ్చికం', kn: 'ವೃಶ್ಚಿಕ', ml: 'വൃശ്ചികം' },
      'Sagittarius': { ta: 'தனுசு', hi: 'धनु', te: 'ధనుస్సు', kn: 'ಧನುಸ್ಸು', ml: 'ധനു' },
      'Dhanus': { ta: 'தனுசு', hi: 'धनु', te: 'ధనుస్సు', kn: 'ಧನುಸ್ಸು', ml: 'ധനു' },
      'Dhanu': { ta: 'தனுசு', hi: 'धनु', te: 'ధనుస్సు', kn: 'ಧನುಸ್ಸು', ml: 'ധനു' },
      'Capricorn': { ta: 'மகரம்', hi: 'मकर', te: 'మకరం', kn: 'ಮಕರ', ml: 'മകരം' },
      'Makara': { ta: 'மகரம்', hi: 'मकर', te: 'మకరం', kn: 'ಮಕರ', ml: 'മകരം' },
      'Aquarius': { ta: 'கும்பம்', hi: 'कुंभ', te: 'కుంభం', kn: 'ಕುಂಭ', ml: 'കുംഭം' },
      'Kumbha': { ta: 'கும்பம்', hi: 'कुंभ', te: 'కుంభం', kn: 'ಕುಂಭ', ml: 'കുംഭം' },
      'Pisces': { ta: 'மீனம்', hi: 'मीन', te: 'మీనం', kn: 'ಮೀನ', ml: 'മീനം' },
      'Meena': { ta: 'மீனம்', hi: 'मीन', te: 'ಮೀನ', kn: 'ಮೀನ', ml: 'മീനം' }
    };
    const match = map[rashi];
    return (match && match[language]) ? match[language] : rashi;
  };

  const translatePlanet = (planet) => {
    if (!planet) return '';
    if (language === 'en') return planet;
    const map = {
      'Sun': { ta: 'சூரியன்', hi: 'सूर्य', te: 'సూర్యుడు', kn: 'ಸೂರ್ಯ', ml: 'സൂര്യൻ' },
      'Surya': { ta: 'சூரியன்', hi: 'सूर्य', te: 'సూర్యుడు', kn: 'ಸೂರ್ಯ', ml: 'സൂര്യൻ' },
      'Moon': { ta: 'சந்திரன்', hi: 'चन्द्र', te: 'చంద్రుడు', kn: 'ಚಂದ್ರ', ml: 'ചന്ദ്രൻ' },
      'Chandra': { ta: 'சந்திரன்', hi: 'चन्द्र', te: 'చంద్రుడు', kn: 'ಚಂದ್ರ', ml: 'ചന്ദ്രൻ' },
      'Mars': { ta: 'செவ்வாய்', hi: 'मंगल', te: 'కుజుడు', kn: 'ಮಂಗಳ', ml: 'ചൊവ്വ' },
      'Kuja': { ta: 'செவ்வாய்', hi: 'मंगल', te: 'కుజుడు', kn: 'ಮಂಗಳ', ml: 'ചൊവ്വ' },
      'Mangal': { ta: 'செவ்வாய்', hi: 'मंगल', te: 'కుజుడు', kn: 'ಮಂಗಳ', ml: 'ചൊവ്വ' },
      'Mercury': { ta: 'புதன்', hi: 'बुध', te: 'బుధుడు', kn: 'ಬುಧ', ml: 'ബുധൻ' },
      'Budha': { ta: 'புதன்', hi: 'बुध', te: 'బుధుడు', kn: 'ಬುಧ', ml: 'ബുധൻ' },
      'Jupiter': { ta: 'குரு', hi: 'गुरु', te: 'గురు', kn: 'ಗುರು', ml: 'ഗുരു' },
      'Guru': { ta: 'குரு', hi: 'गुरु', te: 'గురు', kn: 'ಗುರು', ml: 'ഗുരു' },
      'Venus': { ta: 'சுக்கிரன்', hi: 'शुक्र', te: 'శుక్రుడు', kn: 'ಶುಕ್ರ', ml: 'ശുക്രൻ' },
      'Shukra': { ta: 'சுக்கிரன்', hi: 'शुक्र', te: 'శుక్రుడు', kn: 'ಶುಕ್ರ', ml: 'ശുക്രൻ' },
      'Saturn': { ta: 'சனி', hi: 'शनि', te: 'శని', kn: 'ಶನಿ', ml: 'ശനി' },
      'Shani': { ta: 'சனி', hi: 'शनि', te: 'శని', kn: 'ಶನಿ', ml: 'ശനി' },
      'Rahu': { ta: 'ராகு', hi: 'राहु', te: 'రాహువు', kn: 'ರಾಹು', ml: 'രാഹു' },
      'Ketu': { ta: 'கேது', hi: 'केतु', te: 'కేతువు', kn: 'ಕೇತು', ml: 'കേതു' }
    };
    const match = map[planet];
    return (match && match[language]) ? match[language] : planet;
  };

  const translateProgression = (direction) => {
    if (!direction || language === 'en') return direction;
    const isDirect = direction.includes('Direct') || direction.includes('Savya');
    const isReverse = direction.includes('Reverse') || direction.includes('Apasavya');

    switch (language) {
      case 'ta':
        return isDirect ? 'நேர்முறை - சவ்யம் (Direct / Savya)' : (isReverse ? 'எதிர்முறை - அபசவ்யம் (Reverse / Apasavya)' : direction);
      case 'hi':
        return isDirect ? 'प्रत्यक्ष - सव्य (Direct / Savya)' : (isReverse ? 'विलोम - अपसव्य (Reverse / Apasavya)' : direction);
      case 'te':
        return isDirect ? 'సవ్య దిశ (Direct / Savya)' : (isReverse ? 'అపసవ్య దిశ (Reverse / Apasavya)' : direction);
      case 'kn':
        return isDirect ? 'ಸವ್ಯ ದಿಕ್ಕು (Direct / Savya)' : (isReverse ? 'ಅಪಸವ್ಯ ದಿಕ್ಕು (Reverse / Apasavya)' : direction);
      case 'ml':
        return isDirect ? 'സവ്യ ദിശ (Direct / Savya)' : (isReverse ? 'അപസവ്യ ദിശ (Reverse / Apasavya)' : direction);
    }
    return direction;
  };

  const translateRule = (rule) => {
    if (!rule || language === 'en') return rule;
    const isTriSamvada = rule.includes('Tri-Samvada') || rule.includes('Unanimous');
    const isDwiSamvada = rule.includes('Dwi-Samvada') || rule.includes('Majority');
    const isVishesha1 = rule.includes('Vishesha Sutra 1') || rule.includes('Chandra-Kendra');
    const isVishesha2 = rule.includes('Vishesha Sutra 2') || rule.includes('Atmakaraka-Kendra');
    const isOddTie = rule.includes('Asamvada (Odd') || rule.includes('Odd Lagna');
    const isEvenTie = rule.includes('Asamvada (Even') || rule.includes('Even Lagna');
    const isAsamvada = rule.includes('Asamvada');

    switch (language) {
      case 'ta':
        if (isTriSamvada) return 'திரி-சம்வாதம் (முழு ஒருமனதான முடிவு)';
        if (isDwiSamvada) return 'துவி-சம்வாதம் (பெரும்பான்மை முடிவு)';
        if (isVishesha1) return 'விசேஷ சூத்திரம் 1 (சந்திர-கேந்திர சூத்திரம்)';
        if (isVishesha2) return 'விசேஷ சூத்திரம் 2 (ஆத்மகாரக-கேந்திர சூத்திரம்)';
        if (isOddTie) return 'அசம்வாதம் (ஒற்றைப்படை லக்ன விதி)';
        if (isEvenTie) return 'அசம்வாதம் (இரட்டைப்படை லக்ன விதி)';
        if (isAsamvada) return 'அசம்வாதம் (விசேஷ விதிவிலக்கு)';
        break;
      case 'hi':
        if (isTriSamvada) return 'त्रि-संवाद (पूर्ण सर्वसम्मत निर्णय)';
        if (isDwiSamvada) return 'द्वि-संवाद (बहुमत सर्वसम्मत निर्णय)';
        if (isVishesha1) return 'विशेष सूत्र 1 (चन्द्र-केंद्र सूत्र)';
        if (isVishesha2) return 'विशेष सूत्र 2 (आत्मकारक-केंद्र सूत्र)';
        if (isOddTie) return 'असंवाद (विषम लग्न टाई-ब्रेकर नियम)';
        if (isEvenTie) return 'असंवाद (सम लग्न टाई-ब्रेकर नियम)';
        if (isAsamvada) return 'असंवाद (विशेष अपवाद नियम)';
        break;
      case 'te':
        if (isTriSamvada) return 'త్రి-సంవాదం (పూర్తి ఏకాభిప్రాయం)';
        if (isDwiSamvada) return 'ద్వి-సంవాదం (మెజారిటీ నిర్ణయం)';
        if (isVishesha1) return 'విశేష సూత్రం 1 (చంద్ర-కేంద్ర సూత్రం)';
        if (isVishesha2) return 'విశేష సూత్రం 2 (ఆత్మకారక-కేంద్ర సూత్రం)';
        if (isOddTie) return 'అసంవాదం (విషమ లగ్న నియమం)';
        if (isEvenTie) return 'అసంవాదం (సమ లగ్న నియమం)';
        if (isAsamvada) return 'అసంవాదం (విశేష మినహాయింపు)';
        break;
      case 'kn':
        if (isTriSamvada) return 'ತ್ರಿ-ಸಂವಾದ (ಪೂರ್ಣ ಒಮ್ಮತದ ನಿರ್ಧಾರ)';
        if (isDwiSamvada) return 'ದ್ವಿ-ಸಂವಾದ (ಬಹುಮತದ ನಿರ್ಧಾರ)';
        if (isVishesha1) return 'ವಿಶೇಷ ಸೂತ್ರ 1 (ಚಂದ್ರ-ಕೇಂದ್ರ ಸೂತ್ರ)';
        if (isVishesha2) return 'ವಿಶೇಷ ಸೂತ್ರ 2 (ಆತ್ಮಕಾರಕ-ಕೇಂದ್ರ ಸೂತ್ರ)';
        if (isOddTie) return 'ಅಸಂವಾದ (ವಿಷಮ ಲಗ್ನ ನಿಯಮ)';
        if (isEvenTie) return 'ಅಸಂವಾದ (ಸಮ ಲಗ್ನ ನಿಯಮ)';
        if (isAsamvada) return 'ಅಸಂವಾದ (ವಿಶೇಷ ವಿನಾಯಿತಿ)';
        break;
      case 'ml':
        if (isTriSamvada) return 'ത്രി-സംവാദം (പൂർണ്ണ ഏകകണ്ഠമായ തീരുമാനം)';
        if (isDwiSamvada) return 'ദ്വി-സംവാദം (ഭൂരിപക്ഷ തീരുമാനം)';
        if (isVishesha1) return 'വിശേഷ സൂത്രം 1 (ചന്ദ്ര-കേന്ദ്ര സൂത്രം)';
        if (isVishesha2) return 'വിശേഷ സൂത്രം 2 (ആത്മകാരക-കേന്ദ്ര സൂത്രം)';
        if (isOddTie) return 'അസംവാദം (വിഷമ ലഗ്ന നിയമം)';
        if (isEvenTie) return 'അസംവാദം (സമ ലഗ്ന നിയമം)';
        if (isAsamvada) return 'അസംവാദം (വിശേഷ ഇളവ്)';
        break;
    }
    return rule;
  };

  const translateOverrideReason = (reason) => {
    if (!reason || language === 'en') return reason;
    let res = reason;

    switch (language) {
      case 'ta':
        return res
          .replace(/All 3 Jaimini pairs agree unanimously on/g, 'அனைத்து 3 ஜெமினி இணைகளும் ஒருமனதாக தேர்வு செய்தவை:')
          .replace(/All 3 pairs differ; Moon in Lagna \(1st house\): Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, '3 இணைகளும் மாறுபடுகின்றன; சந்திரன் லக்னத்தில் (1-ஆம் பாவகம்) அமர்ந்துள்ளதால்: இணை 2 (சந்திரன் + சனி) முதன்மை அதிகாரத்தைப் பெறுகிறது (ஜெமினி உபதேச சூத்திரம் 2.1.23).')
          .replace(/All 3 pairs differ; Moon in 7th house: Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, '3 இணைகளும் மாறுபடுகின்றன; சந்திரன் 7-ஆம் பாவகத்தில் அமர்ந்துள்ளதால்: இணை 2 (சந்திரன் + சனி) முதன்மை அதிகாரத்தைப் பெறுகிறது (ஜெமினி உபதேச சூத்திரம் 2.1.23).')
          .replace(/Moon in Lagna \(1st house\): Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'சந்திரன் லக்னத்தில் (1-ஆம் பாவகம்) அமர்ந்துள்ளதால்: இணை 2 (சந்திரன் + சனி) முதன்மை அதிகாரத்தைப் பெறுகிறது (ஜெமினி உபதேச சூத்திரம் 2.1.23).')
          .replace(/Moon in 7th house: Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'சந்திரன் 7-ஆம் பாவகத்தில் அமர்ந்துள்ளதால்: இணை 2 (சந்திரன் + சனி) முதன்மை அதிகாரத்தைப் பெறுகிறது (ஜெமினி உபதேச சூத்திரம் 2.1.23).')
          .replace(/All 3 pairs differ; Atmakaraka in Lagna \(1st house\):/g, '3 இணைகளும் மாறுபடுகின்றன; ஆத்மகாரகன் லக்னத்தில் (1-ஆம் பாவகம்) உள்ளதால்:')
          .replace(/All 3 pairs differ; Atmakaraka in 7th house:/g, '3 இணைகளும் மாறுபடுகின்றன; ஆத்மகாரகன் 7-ஆம் பாவகத்தில் உள்ளதால்:')
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

      case 'hi':
        return res
          .replace(/All 3 Jaimini pairs agree unanimously on/g, 'सभी 3 जैमिनी युग्म सर्वसम्मति से सहमत हैं:')
          .replace(/All 3 pairs differ; Moon in Lagna \(1st house\): Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'तीनों युग्म भिन्न हैं; चन्द्रमा लग्न (प्रथम भाव) में होने से: युग्म 2 (चन्द्र + शनि) को प्राथमिकता प्राप्त है (जैमिनी उपदेश सूत्र 2.1.23)।')
          .replace(/All 3 pairs differ; Moon in 7th house: Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'तीनों युग्म भिन्न हैं; चन्द्रमा 7वें भाव में होने से: युग्म 2 (चन्द्र + शनि) को प्राथमिकता प्राप्त है (जैमिनी उपदेश सूत्र 2.1.23)।')
          .replace(/Moon in Lagna \(1st house\): Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'चन्द्रमा लग्न (प्रथम भाव) में होने से: युग्म 2 (चन्द्र + शनि) को प्राथमिकता प्राप्त है (जैमिनी उपदेश सूत्र 2.1.23)।')
          .replace(/Moon in 7th house: Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'चन्द्रमा 7वें भाव में होने से: युग्म 2 (चन्द्र + शनि) को प्राथमिकता प्राप्त है (जैमिनी उपदेश सूत्र 2.1.23)।')
          .replace(/All 3 pairs differ; Atmakaraka in Lagna \(1st house\):/g, 'तीनों युग्म भिन्न हैं; आत्मकारक लग्न (प्रथम भाव) में होने से:')
          .replace(/All 3 pairs differ; Atmakaraka in 7th house:/g, 'तीनों युग्म भिन्न हैं; आत्मकारक 7वें भाव में होने से:')
          .replace(/Atmakaraka in Lagna \(1st house\):/g, 'आत्मकारक लग्न (प्रथम भाव) में होने से:')
          .replace(/Atmakaraka in 7th house:/g, 'आत्मकारक 7वें भाव में होने से:')
          .replace(/Atmakaraka in Kendra:/g, 'आत्मकारक केंद्र में होने से:')
          .replace(/Odd Lagna gives precedence to Lagna-Hora Lagna \(Pair 3\)\./g, 'विषम लग्न अनुसार लग्न - होरा लग्न (युग्म 3) को प्राथमिकता मिलती है।')
          .replace(/Even Lagna gives precedence to Lagna Lord-8th Lord \(Pair 1\)\./g, 'सम लग्न अनुसार लग्नेश - अष्टमेश (युग्म 1) को प्राथमिकता मिलती है।')
          .replace(/Majority consensus: 2 of 3 pairs agree on/g, 'बहुमत निर्णय: 3 में से 2 युग्म सहमत हैं:')
          .replace(/All 3 pairs indicate distinct spans:/g, 'तीनों युग्म अलग-अलग आयु वर्ग दर्शाते हैं:')
          .replace(/Poornayu/g, 'पूर्णायु')
          .replace(/Madhyayu/g, 'मध्यायु')
          .replace(/Alpayu/g, 'अल्पायु');

      case 'te':
        return res
          .replace(/All 3 Jaimini pairs agree unanimously on/g, 'అన్ని 3 జైమిని జతలు ఏకగ్రీవంగా అంగీకరించినవి:')
          .replace(/All 3 pairs differ; Moon in Lagna \(1st house\): Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, '3 జతలు భిన్నంగా ఉన్నాయి; చంద్రుడు లగ్నం (1వ స్థానం) లో ఉండటం వల్ల: జత 2 (చంద్రుడు + శని) ప్రాధాన్యతను కలిగి ఉంది (జైమిని ఉపదేశ సూత్రం 2.1.23).')
          .replace(/All 3 pairs differ; Moon in 7th house: Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, '3 జతలు భిన్నంగా ఉన్నాయి; చంద్రుడు 7వ స్థానంలో ఉండటం వల్ల: జత 2 (చంద్రుడు + శని) ప్రాధాన్యతను కలిగి ఉంది (జైమిని ఉపదేశ సూత్రం 2.1.23).')
          .replace(/Moon in Lagna \(1st house\): Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'చంద్రుడు లగ్నం (1వ స్థానం) లో ఉండటం వల్ల: జత 2 (చంద్రుడు + శని) ప్రాధాన్యతను కలిగి ఉంది (జైమిని ఉపదేశ సూత్రం 2.1.23).')
          .replace(/Moon in 7th house: Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'చంద్రుడు 7వ స్థానంలో ఉండటం వల్ల: జత 2 (చంద్రుడు + శని) ప్రాధాన్యతను కలిగి ఉంది (జైమిని ఉపదేశ సూత్రం 2.1.23).')
          .replace(/All 3 pairs differ; Atmakaraka in Lagna \(1st house\):/g, '3 జతలు భిన్నంగా ఉన్నాయి; ఆత్మకారకుడు లగ్నంలో ఉండటం వల్ల:')
          .replace(/All 3 pairs differ; Atmakaraka in 7th house:/g, '3 జతలు భిన్నంగా ఉన్నాయి; ఆత్మకారకుడు 7వ స్థానంలో ఉండటం వల్ల:')
          .replace(/Atmakaraka in Lagna \(1st house\):/g, 'ఆత్మకారకుడు లగ్నంలో ఉండటం వల్ల:')
          .replace(/Atmakaraka in 7th house:/g, 'ఆత్మకారకుడు 7వ స్థానంలో ఉండటం వల్ల:')
          .replace(/Atmakaraka in Kendra:/g, 'ఆత్మకారకుడు కేంద్రంలో ఉండటం వల్ల:')
          .replace(/Odd Lagna gives precedence to Lagna-Hora Lagna \(Pair 3\)\./g, 'విషమ లగ్నం ప్రకారం లగ్నం - హోరా లగ్నం (జత 3) ప్రాధాన్యత పొందుతుంది.')
          .replace(/Even Lagna gives precedence to Lagna Lord-8th Lord \(Pair 1\)\./g, 'సమ లగ్నం ప్రకారం లగ్నాధిపతి - 8వ అధిపతి (జత 1) ప్రాధాన్యత పొందుతుంది.')
          .replace(/Majority consensus: 2 of 3 pairs agree on/g, 'మెజారిటీ నిర్ణయం: 3 లో 2 జతలు అంగీకరించినవి:')
          .replace(/All 3 pairs indicate distinct spans:/g, '3 జతలు వేర్వేరు ఆయుర్దాయ విభాగాలను సూచిస్తున్నాయి:')
          .replace(/Poornayu/g, 'పూర్ణాయుష్షు')
          .replace(/Madhyayu/g, 'మధ్యాయుష్షు')
          .replace(/Alpayu/g, 'అల్పాయుష్షు');

      case 'kn':
        return res
          .replace(/All 3 Jaimini pairs agree unanimously on/g, 'ಎಲ್ಲಾ 3 ಜೈಮಿನಿ ಜೋಡಿಗಳು ಸರ್ವಾನುಮತದಿಂದ ಒಪ್ಪಿಕೊಂಡಿವೆ:')
          .replace(/All 3 pairs differ; Moon in Lagna \(1st house\): Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, '3 ಜೋಡಿಗಳು ಭಿನ್ನವಾಗಿವೆ; ಚಂದ್ರನು ಲಗ್ನದಲ್ಲಿ (1ನೇ ಮನೆ) ಇರುವುದರಿಂದ: ಜೋಡಿ 2 (ಚಂದ್ರ + ಶನಿ) ಪ್ರಾಶಸ್ತ್ಯ ಪಡೆಯುತ್ತದೆ (ಜೈಮಿನಿ ಉಪದೇಶ ಸೂತ್ರ 2.1.23).')
          .replace(/All 3 pairs differ; Moon in 7th house: Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, '3 ಜೋಡಿಗಳು ಭಿನ್ನವಾಗಿವೆ; ಚಂದ್ರನು 7ನೇ ಮನೆಯಲ್ಲಿರುವುದರಿಂದ: ಜೋಡಿ 2 (ಚಂದ್ರ + ಶನಿ) ಪ್ರಾಶಸ್ತ್ಯ ಪಡೆಯುತ್ತದೆ (ಜೈಮಿನಿ ಉಪದೇಶ ಸೂತ್ರ 2.1.23).')
          .replace(/Moon in Lagna \(1st house\): Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'ಚಂದ್ರನು ಲಗ್ನದಲ್ಲಿ (1ನೇ ಮನೆ) ಇರುವುದರಿಂದ: ಜೋಡಿ 2 (ಚಂದ್ರ + ಶನಿ) ಪ್ರಾಶಸ್ತ್ಯ ಪಡೆಯುತ್ತದೆ (ಜೈಮಿನಿ ಉಪದೇಶ ಸೂತ್ರ 2.1.23).')
          .replace(/Moon in 7th house: Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'ಚಂದ್ರನು 7ನೇ ಮನೆಯಲ್ಲಿರುವುದರಿಂದ: ಜೋಡಿ 2 (ಚಂದ್ರ + ಶನಿ) ಪ್ರಾಶಸ್ತ್ಯ ಪಡೆಯುತ್ತದೆ (ಜೈಮಿನಿ ಉಪದೇಶ ಸೂತ್ರ 2.1.23).')
          .replace(/All 3 pairs differ; Atmakaraka in Lagna \(1st house\):/g, '3 ಜೋಡಿಗಳು ಭಿನ್ನವಾಗಿವೆ; ಆತ್ಮಕಾರಕನು ಲಗ್ನದಲ್ಲಿರುವುದರಿಂದ:')
          .replace(/All 3 pairs differ; Atmakaraka in 7th house:/g, '3 ಜೋಡಿಗಳು ಭಿನ್ನವಾಗಿವೆ; ಆತ್ಮಕಾರಕನು 7ನೇ ಮನೆಯಲ್ಲಿರುವುದರಿಂದ:')
          .replace(/Atmakaraka in Lagna \(1st house\):/g, 'ಆತ್ಮಕಾರಕನು ಲಗ್ನದಲ್ಲಿರುವುದರಿಂದ:')
          .replace(/Atmakaraka in 7th house:/g, 'ಆತ್ಮಕಾರಕನು 7ನೇ ಮನೆಯಲ್ಲಿರುವುದರಿಂದ:')
          .replace(/Atmakaraka in Kendra:/g, 'ಆತ್ಮಕಾರಕನು ಕೇಂದ್ರದಲ್ಲಿರುವುದರಿಂದ:')
          .replace(/Odd Lagna gives precedence to Lagna-Hora Lagna \(Pair 3\)\./g, 'ವಿಷಮ ಲಗ್ನದಂತೆ ಲಗ್ನ - ಹೋರಾ ಲಗ್ನ (ಜೋಡಿ 3) ಪ್ರಾಶಸ್ತ್ಯ ಪಡೆಯುತ್ತದೆ.')
          .replace(/Even Lagna gives precedence to Lagna Lord-8th Lord \(Pair 1\)\./g, 'ಸಮ ಲಗ್ನದಂತೆ ಲಗ್ನಾಧಿಪತಿ - 8ನೇ ಅಧಿಪತಿ (ಜೋಡಿ 1) ಪ್ರಾಶಸ್ತ್ಯ ಪಡೆಯುತ್ತದೆ.')
          .replace(/Majority consensus: 2 of 3 pairs agree on/g, 'ಬಹುಮತದ ನಿರ್ಧಾರ: 3 ರಲ್ಲಿ 2 ಜೋಡಿಗಳು ಒಪ್ಪಿಕೊಂಡಿವೆ:')
          .replace(/All 3 pairs indicate distinct spans:/g, '3 ಜೋಡಿಗಳು ವಿಭಿನ್ನ ಆಯುಷ್ಯ ವರ್ಗಗಳನ್ನು ತೋರಿಸುತ್ತವೆ:')
          .replace(/Poornayu/g, 'ಪೂರ್ಣಾಯುಷ್ಯ')
          .replace(/Madhyayu/g, 'ಮಧ್ಯಾಯುಷ್ಯ')
          .replace(/Alpayu/g, 'ಅಲ್ಪಾಯುಷ್ಯ');

      case 'ml':
        return res
          .replace(/All 3 Jaimini pairs agree unanimously on/g, 'എല്ലാ 3 ജൈമിനി ജോഡികളും ഏകകണ്ഠമായി തിരഞ്ഞെടുത്തത്:')
          .replace(/All 3 pairs differ; Moon in Lagna \(1st house\): Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, '3 ജോഡികളും വ്യത്യസ്തമാണ്; ചന്ദ്രൻ ലഗ്നത്തിൽ (ഒന്നാം ഭാവം) നിൽക്കുന്നതിനാൽ: ജോഡി 2 (ചന്ദ്രൻ + ശനി) പ്രാമുഖ്യം നേടുന്നു (ജൈമിനി ഉപദേശ സൂത്രം 2.1.23).')
          .replace(/All 3 pairs differ; Moon in 7th house: Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, '3 ജോഡികളും വ്യത്യസ്തമാണ്; ചന്ദ്രൻ 7-ാം ഭാവത്തിൽ നിൽക്കുന്നതിനാൽ: ജോഡി 2 (ചന്ദ്രൻ + ശനി) പ്രാമുഖ്യം നേടുന്നു (ജൈമിനി ഉപദേശ സൂത്രം 2.1.23).')
          .replace(/Moon in Lagna \(1st house\): Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'ചന്ദ്രൻ ലഗ്നത്തിൽ (ഒന്നാം ഭാവം) നിൽക്കുന്നതിനാൽ: ജോഡി 2 (ചന്ദ്രൻ + ശനി) പ്രാമുഖ്യം നേടുന്നു (ജൈമിനി ഉപദേശ സൂത്രം 2.1.23).')
          .replace(/Moon in 7th house: Pair 2 \(Moon \+ Saturn\) holds overriding authority \(Jaimini Upadesha Sutra 2.1.23\)\./g, 'ചന്ദ്രൻ 7-ാം ഭാവത്തിൽ നിൽക്കുന്നതിനാൽ: ജോഡി 2 (ചന്ദ്രൻ + ശനി) പ്രാമുഖ്യം നേടുന്നു (ജൈമിനി ഉപദേശ സൂത്രം 2.1.23).')
          .replace(/All 3 pairs differ; Atmakaraka in Lagna \(1st house\):/g, '3 ജോഡികളും വ്യത്യസ്തമാണ്; ആത്മകാരകൻ ലഗ്നത്തിൽ നിൽക്കുന്നതിനാൽ:')
          .replace(/All 3 pairs differ; Atmakaraka in 7th house:/g, '3 ജോഡികളും വ്യത്യസ്തമാണ്; ആത്മകാരകൻ 7-ാം ഭാവത്തിൽ നിൽക്കുന്നതിനാൽ:')
          .replace(/Atmakaraka in Lagna \(1st house\):/g, 'ആത്മകാരകൻ ലഗ്നത്തിൽ നിൽക്കുന്നതിനാൽ:')
          .replace(/Atmakaraka in 7th house:/g, 'ആത്മകാരകൻ 7-ാം ഭാവത്തിൽ നിൽക്കുന്നതിനാൽ:')
          .replace(/Atmakaraka in Kendra:/g, 'ആത്മകാരകൻ കേന്ദ്രത്തിൽ നിൽക്കുന്നതിനാൽ:')
          .replace(/Odd Lagna gives precedence to Lagna-Hora Lagna \(Pair 3\)\./g, 'വിഷമ ലഗ്ന പ്രകാരം ലഗ്നം - ഹോരാ ലഗ്നം (ജോഡി 3) പ്രാമുഖ്യം നേടുന്നു.')
          .replace(/Even Lagna gives precedence to Lagna Lord-8th Lord \(Pair 1\)\./g, 'സമ ലഗ്ന പ്രകാരം ലഗ്നാധിപൻ - 8-ാം നാഥൻ (ജോഡി 1) പ്രാമുഖ്യം നേടുന്നു.')
          .replace(/Majority consensus: 2 of 3 pairs agree on/g, 'ഭൂരിപക്ഷ തീരുമാനം: 3-ൽ 2 ജോഡികൾ തിരഞ്ഞെടുത്തത്:')
          .replace(/All 3 pairs indicate distinct spans:/g, '3 ജോഡികളും വ്യത്യസ്ത ആയുർദായ വിഭാഗങ്ങളെ കാണിക്കുന്നു:')
          .replace(/Poornayu/g, 'പൂർണ്ണായുസ്സ്')
          .replace(/Madhyayu/g, 'മദ്ധ്യായുസ്സ്')
          .replace(/Alpayu/g, 'അല്പായുസ്സ്');
    }
    return res;
  };

  const translateKakshyaAdjustment = (adj) => {
    if (!adj || language === 'en') return adj;
    let res = adj;

    switch (language) {
      case 'ta':
      res = res
          .replace(/All 3 Jaimini pairs agree unanimously on Poornayu\./g, 'அனைத்து 3 ஜெமினி இணைகளும் ஒருமனதாக பூரணாயுளை தேர்வு செய்தவை.')
          .replace(/All 3 Jaimini pairs agree unanimously on Madhyayu\./g, 'அனைத்து 3 ஜெமினி இணைகளும் ஒருமனதாக மத்தியாயுளை தேர்வு செய்தவை.')
          .replace(/All 3 Jaimini pairs agree unanimously on Alpayu\./g, 'அனைத்து 3 ஜெமினி இணைகளும் ஒருமனதாக அல்பாயுளை தேர்வு செய்தவை.')
          .replace(/Majority consensus: 2 of 3 pairs agree on Poornayu\./g, 'பெரும்பான்மை முடிவு: 3-ல் 2 இணைகள் பூரணாயுளை முடிவு செய்தவை.')
          .replace(/Majority consensus: 2 of 3 pairs agree on Madhyayu\./g, 'பெரும்பான்மை முடிவு: 3-ல் 2 இணைகள் மத்தியாயுளை முடிவு செய்தவை.')
          .replace(/Majority consensus: 2 of 3 pairs agree on Alpayu\./g, 'பெரும்பான்மை முடிவு: 3-ல் 2 இணைகள் அல்பாயுளை முடிவு செய்தவை.')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(elevating longevity compartment from Alpayu to Madhyayu baseline ~68 yrs\)\./g, 'குரு கேந்திர/திரிகோணத்தில் சுப பலத்துடன் அமர்ந்து கக்ஷ்ய விருத்தி அருள்கிறார் (அல்பாயுளிலிருந்து மத்தியாயுள் ~68 வயதுக்கு உயர்வு).')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(elevating longevity compartment from Madhyayu to Poornayu baseline ~82 yrs\)\./g, 'குரு கேந்திர/திரிகோணத்தில் சுப பலத்துடன் அமர்ந்து கக்ஷ்ய விருத்தி அருள்கிறார் (மத்தியாயுளிலிருந்து பூரணாயுள் ~82 வயதுக்கு உயர்வு).')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(\+(\d+) years\)\./g, 'குரு கேந்திர/திரிகோணத்தில் சுப பலத்துடன் அமர்ந்து கக்ஷ்ய விருத்தி அருள்கிறார் (+$1 ஆண்டுகள்).')
          .replace(/Atmakaraka \((.*?)\) in Kendra\/Trikona\/Exalted confers Kakshya Vriddhi \(elevating longevity compartment from Alpayu to Madhyayu\)\./g, 'ஆத்மகாரகன் ($1) கேந்திர/திரிகோணம்/உச்சத்தில் அமர்ந்து கக்ஷ்ய விருத்தி அருள்கிறார் (அல்பாயுளிலிருந்து மத்தியாயுளுக்கு உயர்வு).')
          .replace(/Atmakaraka \((.*?)\) in Kendra\/Trikona\/Exalted reinforces longevity vitality \(\+(\d+) years\)\./g, 'ஆத்மகாரகன் ($1) கேந்திர/திரிகோணம்/உச்சத்தில் அமர்ந்து ஆயுள் பலத்தை அதிகரிக்கிறார் (+$2 ஆண்டுகள்).')
          .replace(/Ayushkaraka Saturn in Own\/Exalted sign reinforces longevity \(\+(\d+) years\)\./g, 'ஆயுள்காரகன் சனி ஆட்சி/உச்ச பலத்துடன் அமர்ந்து ஆயுளை உறுதிப்படுத்துகிறார் (+$1 ஆண்டுகள்).')
          .replace(/Lagna Lord strong in own\/exalted\/Kendra\/Trikona adds physical vitality \(\+(\d+) years\)\./g, 'லக்னாதிபதி ஆட்சி/உச்சம்/கேந்திர/திரிகோணத்தில் பலம் பெற்று தேக ஆரோக்கியத்தை அதிகரிக்கிறார் (+$1 ஆண்டுகள்).')
          .replace(/Lagna Lord strong in own\/exalted sign adds physical vitality \(\+(\d+) years\)\./g, 'லக்னாதிபதி ஆட்சி/உச்ச பலம் பெற்றுள்ளதால் உடல் ஆயுள் பலம் (+$1 ஆண்டுகள்) கூடுகிறது.')
          .replace(/Ayushkaraka Saturn possesses Neecha Bhanga \(cancellation of debility into longevity stability\)\./g, 'ஆயுள்காரகன் சனி நீசபங்க ராஜயோகம் பெற்று ஆயுள் நிலைத்தன்மையை அருள்கிறார் (+2 ஆண்டுகள்).')
          .replace(/Ayushkaraka Saturn in debility applies Kakshya Hrasa reduction \(-(\d+) years\)\./g, 'ஆயுள்காரகன் சனி நீசமடைந்து கக்ஷ்ய ஹிராஸ குறைப்பை ஏற்படுத்துகிறார் (-$1 ஆண்டுகள்).')
          .replace(/Lagna Lord debilitated in Dusthana applies Kakshya Hrasa \(-(\d+) years\)\./g, 'லக்னாதிபதி துஸ்தானத்தில் நீசமடைந்து கக்ஷ்ய ஹிராஸ குறைப்பை ஏற்படுத்துகிறார் (-$1 ஆண்டுகள்).')
          .replace(/Lagna Lord in Dusthana \(6\/8\/12\) advises mindful health regimen\./g, 'லக்னாதிபதி துஸ்தானத்தில் (6/8/12) இருப்பதால் ஆரோக்கியத்தில் கூடுதல் கவனம் தேவை (-2 ஆண்டுகள்).')
          .replace(/Lagna hemmed between malefics in 12th & 2nd \(Papakarthari Yoga\) cautions physical vitality \(-(\d+) years\)\./g, 'லக்னம் பாபகர்த்தரி யோகத்தில் (12 & 2-ல் பாவ கிரகங்கள்) சிக்கியுள்ளதால் உடல் ஆரோக்கியத்தில் எச்சரிக்கை தேவை (-$1 ஆண்டுகள்).')
          .replace(/Moon hemmed between malefics in 12th & 2nd \(Papakarthari Yoga on Moon\) cautions vitality \(-(\d+) years\)\./g, 'சந்திரன் பாபகர்த்தரி யோகத்தில் (12 & 2-ல் பாவ கிரகங்கள்) சிக்கியுள்ளதால் மன/உடல் நலனில் எச்சரிக்கை தேவை (-$1 ஆண்டுகள்).')
          .replace(/Malefics in Kendras with no benefics in Kendras applies Kakshya Hrasa \(-(\d+) years\)\./g, 'கேந்திரங்களில் சுப கிரகங்களின்றி பாவ கிரகங்கள் மட்டுமே இருப்பதால் கக்ஷ்ய ஹிராஸம் உண்டாகிறது (-$1 ஆண்டுகள்).');
      break;

    case 'hi':
      res = res
          .replace(/All 3 Jaimini pairs agree unanimously on Poornayu\./g, 'सभी 3 जैमिनी युग्म सर्वसम्मति से पूर्णायु पर सहमत हैं।')
          .replace(/All 3 Jaimini pairs agree unanimously on Madhyayu\./g, 'सभी 3 जैमिनी युग्म सर्वसम्मति से मध्यायु पर सहमत हैं।')
          .replace(/All 3 Jaimini pairs agree unanimously on Alpayu\./g, 'सभी 3 जैमिनी युग्म सर्वसम्मति से अल्पायु पर सहमत हैं।')
          .replace(/Majority consensus: 2 of 3 pairs agree on Poornayu\./g, 'बहुमत निर्णय: 3 में से 2 युग्म पूर्णायु पर सहमत हैं।')
          .replace(/Majority consensus: 2 of 3 pairs agree on Madhyayu\./g, 'बहुमत निर्णय: 3 में से 2 युग्म मध्यायु पर सहमत हैं।')
          .replace(/Majority consensus: 2 of 3 pairs agree on Alpayu\./g, 'बहुमत निर्णय: 3 में से 2 युग्म अल्पायु पर सहमत हैं।')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(elevating longevity compartment from Alpayu to Madhyayu baseline ~68 yrs\)\./g, 'गुरु शुभ केंद्र/त्रिकोण में स्थित होकर कक्ष्या वृद्धि प्रदान करते हैं (अल्पायु से मध्यायु आधार ~68 वर्ष तक पदोन्नति)।')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(elevating longevity compartment from Madhyayu to Poornayu baseline ~82 yrs\)\./g, 'गुरु शुभ केंद्र/त्रिकोण में स्थित होकर कक्ष्या वृद्धि प्रदान करते हैं (मध्यायु से पूर्णायु आधार ~82 वर्ष तक पदोन्नति)।')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(\+(\d+) years\)\./g, 'गुरु शुभ केंद्र/त्रिकोण में स्थित होकर कक्ष्या वृद्धि प्रदान करते हैं (+$1 वर्ष)।')
          .replace(/Atmakaraka \((.*?)\) in Kendra\/Trikona\/Exalted confers Kakshya Vriddhi \(elevating longevity compartment from Alpayu to Madhyayu\)\./g, 'आत्मकारक ($1) केंद्र/त्रिकोण/उच्च में स्थित होकर कक्ष्या वृद्धि प्रदान करते हैं (अल्पायु से मध्यायु में पदोन्नति)।')
          .replace(/Atmakaraka \((.*?)\) in Kendra\/Trikona\/Exalted reinforces longevity vitality \(\+(\d+) years\)\./g, 'आत्मकारक ($1) केंद्र/त्रिकोण/उच्च में स्थित होकर प्राणशक्ति को सुदृढ़ करते हैं (+$2 वर्ष)।')
          .replace(/Ayushkaraka Saturn in Own\/Exalted sign reinforces longevity \(\+(\d+) years\)\./g, 'आयुष्कारक शनि स्व/उच्च राशि में स्थित होकर दीर्घायु को सुदृढ़ करते हैं (+$1 वर्ष)।')
          .replace(/Lagna Lord strong in own\/exalted\/Kendra\/Trikona adds physical vitality \(\+(\d+) years\)\./g, 'लग्नेश स्व/उच्च/केंद्र/त्रिकोण में बलिष्ठ होकर शारीरिक प्राणशक्ति बढ़ाते हैं (+$1 वर्ष)।')
          .replace(/Lagna Lord strong in own\/exalted sign adds physical vitality \(\+(\d+) years\)\./g, 'लग्नेश स्व/उच्च राशि में बलिष्ठ होकर शारीरिक प्राणशक्ति बढ़ाते हैं (+$1 वर्ष)।')
          .replace(/Ayushkaraka Saturn possesses Neecha Bhanga \(cancellation of debility into longevity stability\)\./g, 'आयुष्कारक शनि को नीचभंग राजयोग प्राप्त होने से आयु में स्थिरता आती है (+2 वर्ष)।')
          .replace(/Ayushkaraka Saturn in debility applies Kakshya Hrasa reduction \(-(\d+) years\)\./g, 'आयुष्कारक शनि नीच राशि में होने से कक्ष्या ह्रास कमी होती है (-$1 वर्ष)।')
          .replace(/Lagna Lord debilitated in Dusthana applies Kakshya Hrasa \(-(\d+) years\)\./g, 'लग्नेश दुस्थान में नीच होने से कक्ष्या ह्रास कमी होती है (-$1 वर्ष)।')
          .replace(/Lagna Lord in Dusthana \(6\/8\/12\) advises mindful health regimen\./g, 'लग्नेश दुस्थान (6/8/12) में स्थित होने से स्वास्थ्य के प्रति सजगता आवश्यक है (-2 वर्ष)।')
          .replace(/Lagna hemmed between malefics in 12th & 2nd \(Papakarthari Yoga\) cautions physical vitality \(-(\d+) years\)\./g, 'लग्न पापकर्तरी योग में होने से शारीरिक स्वास्थ्य में सावधानी आवश्यक है (-$1 वर्ष)।')
          .replace(/Moon hemmed between malefics in 12th & 2nd \(Papakarthari Yoga on Moon\) cautions vitality \(-(\d+) years\)\./g, 'चन्द्रमा पापकर्तरी योग में होने से मानसिक/शारीरिक स्वास्थ्य में सावधानी आवश्यक है (-$1 वर्ष)।')
          .replace(/Malefics in Kendras with no benefics in Kendras applies Kakshya Hrasa \(-(\d+) years\)\./g, 'केंद्रों में शुभ ग्रहों के बिना केवल पाप ग्रह होने से कक्ष्या ह्रास होता है (-$1 वर्ष)।');
      break;

    case 'te':
      res = res
          .replace(/All 3 Jaimini pairs agree unanimously on Poornayu\./g, 'అన్ని 3 జైమిని జతలు ఏకగ్రీవంగా పూర్ణాయుష్షును నిర్ణయించాయి.')
          .replace(/All 3 Jaimini pairs agree unanimously on Madhyayu\./g, 'అన్ని 3 జైమిని జతలు ఏకగ్రీవంగా మధ్యాయుష్షును నిర్ణయించాయి.')
          .replace(/All 3 Jaimini pairs agree unanimously on Alpayu\./g, 'అన్ని 3 జైమిని జతలు ఏకగ్రీవంగా అల్పాయుష్షును నిర్ణయించాయి.')
          .replace(/Majority consensus: 2 of 3 pairs agree on Poornayu\./g, 'మెజారిటీ నిర్ణయం: 3 లో 2 జతలు పూర్ణాయుష్షును నిర్ణయించాయి.')
          .replace(/Majority consensus: 2 of 3 pairs agree on Madhyayu\./g, 'మెజారిటీ నిర్ణయం: 3 లో 2 జతలు మధ్యాయుష్షును నిర్ణయించాయి.')
          .replace(/Majority consensus: 2 of 3 pairs agree on Alpayu\./g, 'మెజారిటీ నిర్ణయం: 3 లో 2 జతలు అల్పాయుష్షును నిర్ణయించాయి.')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(elevating longevity compartment from Alpayu to Madhyayu baseline ~68 yrs\)\./g, 'గురు గ్రహం కేంద్ర/త్రికోణంలో ఉండి కక్ష్యా వృద్ధిని ప్రసాదిస్తుంది (అల్పాయుష్షు నుండి మధ్యాయుష్షు ~68 సంవత్సరాల స్థాయికి పెంపు).')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(elevating longevity compartment from Madhyayu to Poornayu baseline ~82 yrs\)\./g, 'గురు గ్రహం కేంద్ర/త్రికోణంలో ఉండి కక్ష్యా వృద్ధిని ప్రసాదిస్తుంది (మధ్యాయుష్షు నుండి పూర్ణాయుష్షు ~82 సంవత్సరాల స్థాయికి పెంపు).')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(\+(\d+) years\)\./g, 'గురు గ్రహం కేంద్ర/త్రికోణంలో ఉండి కక్ష్యా వృద్ధిని ప్రసాదిస్తుంది (+$1 సంవత్సరాలు).')
          .replace(/Atmakaraka \((.*?)\) in Kendra\/Trikona\/Exalted confers Kakshya Vriddhi \(elevating longevity compartment from Alpayu to Madhyayu\)\./g, 'ఆత్మకారకుడు ($1) కేంద్ర/త్రికోణం/ఉచ్ఛంలో ఉండి కక్ష్యా వృద్ధిని ప్రసాదిస్తాడు (అల్పాయుష్షు నుండి మధ్యాయుష్షుకు పెంపు).')
          .replace(/Atmakaraka \((.*?)\) in Kendra\/Trikona\/Exalted reinforces longevity vitality \(\+(\d+) years\)\./g, 'ఆత్మకారకుడు ($1) కేంద్ర/త్రికోణం/ఉచ్ఛంలో ఉండి ఆయుష్షు బలాన్ని పెంచుతాడు (+$2 సంవత్సరాలు).')
          .replace(/Ayushkaraka Saturn in Own\/Exalted sign reinforces longevity \(\+(\d+) years\)\./g, 'ఆయుష్కారకుడు శని స్వ/ఉచ్ఛ క్షేత్రంలో ఉండి ఆయుష్షును బలపరుస్తాడు (+$1 సంవత్సరాలు).')
          .replace(/Lagna Lord strong in own\/exalted\/Kendra\/Trikona adds physical vitality \(\+(\d+) years\)\./g, 'లగ్నాధిపతి స్వ/ఉచ్ఛ/కేంద్ర/త్రికోణంలో ఉండి శరీర బలాన్ని పెంచుతాడు (+$1 సంవత్సరాలు).')
          .replace(/Lagna Lord strong in own\/exalted sign adds physical vitality \(\+(\d+) years\)\./g, 'లగ్నాధిపతి స్వ/ఉచ్ఛ క్షేత్రంలో ఉండి శరీర బలాన్ని పెంచుతాడు (+$1 సంవత్సరాలు).')
          .replace(/Ayushkaraka Saturn possesses Neecha Bhanga \(cancellation of debility into longevity stability\)\./g, 'ఆయుష్కారకుడు శనికి నీచభంగ రాజయోగం వల్ల ఆయుష్షు స్థిరపడుతుంది (+2 సంవత్సరాలు).')
          .replace(/Ayushkaraka Saturn in debility applies Kakshya Hrasa reduction \(-(\d+) years\)\./g, 'ఆయుష్కారకుడు శని నీచంలో ఉండటం వల్ల కక్ష్యా హ్రాస తగ్గింపు వర్తిస్తుంది (-$1 సంవత్సరాలు).')
          .replace(/Lagna Lord debilitated in Dusthana applies Kakshya Hrasa \(-(\d+) years\)\./g, 'లగ్నాధిపతి దుస్థానంలో నీచంలో ఉండటం వల్ల కక్ష్యా హ్రాస తగ్గింపు వర్తిస్తుంది (-$1 సంవత్సరాలు).')
          .replace(/Lagna Lord in Dusthana \(6\/8\/12\) advises mindful health regimen\./g, 'లగ్నాధిపతి దుస్థానంలో (6/8/12) ఉండటం వల్ల ఆరోగ్యంపై ప్రత్యేక శ్రద్ధ అవసరం (-2 సంవత్సరాలు).')
          .replace(/Lagna hemmed between malefics in 12th & 2nd \(Papakarthari Yoga\) cautions physical vitality \(-(\d+) years\)\./g, 'లగ్నం పాపకర్తరి యోగంలో ఉండటం వల్ల శరీర ఆరోగ్యంపై జాగ్రత్త అవసరం (-$1 సంవత్సరాలు).')
          .replace(/Moon hemmed between malefics in 12th & 2nd \(Papakarthari Yoga on Moon\) cautions vitality \(-(\d+) years\)\./g, 'చంద్రుడు పాపకర్తరి యోగంలో ఉండటం వల్ల మానసిక/శరీర ఆరోగ్యంపై జాగ్రత్త అవసరం (-$1 సంవత్సరాలు).')
          .replace(/Malefics in Kendras with no benefics in Kendras applies Kakshya Hrasa \(-(\d+) years\)\./g, 'కేంద్రాలలో శుభ గ్రహాలు లేకుండా కేవలం పాప గ్రహాలు ఉండటం వల్ల కక్ష్యా హ్రాసం కలుగుతుంది (-$1 సంవత్సరాలు).');
      break;

    case 'kn':
      res = res
          .replace(/All 3 Jaimini pairs agree unanimously on Poornayu\./g, 'ಎಲ್ಲಾ 3 ಜೈಮಿನಿ ಜೋಡಿಗಳು ಸರ್ವಾನುಮತದಿಂದ ಪೂರ್ಣಾಯುಷ್ಯವನ್ನು ನಿರ್ಧರಿಸಿವೆ.')
          .replace(/All 3 Jaimini pairs agree unanimously on Madhyayu\./g, 'ಎಲ್ಲಾ 3 ಜೈಮಿನಿ ಜೋಡಿಗಳು ಸರ್ವಾನುಮತದಿಂದ ಮಧ್ಯಾಯುಷ್ಯವನ್ನು ನಿರ್ಧರಿಸಿವೆ.')
          .replace(/All 3 Jaimini pairs agree unanimously on Alpayu\./g, 'ಎಲ್ಲಾ 3 ಜೈಮಿನಿ ಜೋಡಿಗಳು ಸರ್ವಾನುಮತದಿಂದ ಅಲ್ಪಾಯುಷ್ಯವನ್ನು ನಿರ್ಧರಿಸಿವೆ.')
          .replace(/Majority consensus: 2 of 3 pairs agree on Poornayu\./g, 'ಬಹುಮತದ ನಿರ್ಧಾರ: 3 ರಲ್ಲಿ 2 ಜೋಡಿಗಳು ಪೂರ್ಣಾಯುಷ್ಯವನ್ನು ಒಪ್ಪಿಕೊಂಡಿವೆ.')
          .replace(/Majority consensus: 2 of 3 pairs agree on Madhyayu\./g, 'ಬಹುಮತದ ನಿರ್ಧಾರ: 3 ರಲ್ಲಿ 2 ಜೋಡಿಗಳು ಮಧ್ಯಾಯುಷ್ಯವನ್ನು ಒಪ್ಪಿಕೊಂಡಿವೆ.')
          .replace(/Majority consensus: 2 of 3 pairs agree on Alpayu\./g, 'ಬಹುಮತದ ನಿರ್ಧಾರ: 3 ರಲ್ಲಿ 2 ಜೋಡಿಗಳು ಅಲ್ಪಾಯುಷ್ಯವನ್ನು ಒಪ್ಪಿಕೊಂಡಿವೆ.')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(elevating longevity compartment from Alpayu to Madhyayu baseline ~68 yrs\)\./g, 'ಗುರು ಕೇಂದ್ರ/ತ್ರಿಕೋಣದಲ್ಲಿ ಶುಭ ಬಲ ಹೊಂದಿದ್ದು ಕಕ್ಷ್ಯಾ ವೃದ್ಧಿ ನೀಡುತ್ತಾನೆ (ಅಲ್ಪಾಯುಷ್ಯದಿಂದ ಮಧ್ಯಾಯುಷ್ಯ ~68 ವರ್ಷಗಳಿಗೆ ಏರಿಕೆ).')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(elevating longevity compartment from Madhyayu to Poornayu baseline ~82 yrs\)\./g, 'ಗುರು ಕೇಂದ್ರ/ತ್ರಿಕೋಣದಲ್ಲಿ ಶುಭ ಬಲ ಹೊಂದಿದ್ದು ಕಕ್ಷ್ಯಾ ವೃದ್ಧಿ ನೀಡುತ್ತಾನೆ (ಮಧ್ಯಾಯುಷ್ಯದಿಂದ ಪೂರ್ಣಾಯುಷ್ಯ ~82 ವರ್ಷಗಳಿಗೆ ಏರಿಕೆ).')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(\+(\d+) years\)\./g, 'ಗುರು ಕೇಂದ್ರ/ತ್ರಿಕೋಣದಲ್ಲಿ ಶುಭ ಬಲ ಹೊಂದಿದ್ದು ಕಕ್ಷ್ಯಾ ವೃದ್ಧಿ ನೀಡುತ್ತಾನೆ (+$1 ವರ್ಷಗಳು).')
          .replace(/Atmakaraka \((.*?)\) in Kendra\/Trikona\/Exalted confers Kakshya Vriddhi \(elevating longevity compartment from Alpayu to Madhyayu\)\./g, 'ಆತ್ಮಕಾರಕ ($1) ಕೇಂದ್ರ/ತ್ರಿಕೋಣ/ಉಚ್ಛದಲ್ಲಿದ್ದು ಕಕ್ಷ್ಯಾ ವೃದ್ಧಿ ನೀಡುತ್ತಾನೆ (ಅಲ್ಪಾಯುಷ್ಯದಿಂದ ಮಧ್ಯಾಯುಷ್ಯಕ್ಕೆ ಏರಿಕೆ).')
          .replace(/Atmakaraka \((.*?)\) in Kendra\/Trikona\/Exalted reinforces longevity vitality \(\+(\d+) years\)\./g, 'ಆತ್ಮಕಾರಕ ($1) ಕೇಂದ್ರ/ತ್ರಿಕೋಣ/ಉಚ್ಛದಲ್ಲಿದ್ದು ಆಯುಷ್ಯ ಬಲವನ್ನು ಹೆಚ್ಚಿಸುತ್ತಾನೆ (+$2 ವರ್ಷಗಳು).')
          .replace(/Ayushkaraka Saturn in Own\/Exalted sign reinforces longevity \(\+(\d+) years\)\./g, 'ಆಯುಷ್ಯಕಾರಕ ಶನಿ ಸ್ವ/ಉಚ್ಛ ರಾಶಿಯಲ್ಲಿದ್ದು ಆಯುಷ್ಯವನ್ನು ಬಲಪಡಿಸುತ್ತಾನೆ (+$1 ವರ್ಷಗಳು).')
          .replace(/Lagna Lord strong in own\/exalted\/Kendra\/Trikona adds physical vitality \(\+(\d+) years\)\./g, 'ಲಗ್ನಾಧಿಪತಿ ಸ್ವ/ಉಚ್ಛ/ಕೇಂದ್ರ/ತ್ರಿಕೋಣದಲ್ಲಿದ್ದು ದೈಹಿಕ ಚೈತನ್ಯವನ್ನು ಹೆಚ್ಚಿಸುತ್ತಾನೆ (+$1 ವರ್ಷಗಳು).')
          .replace(/Lagna Lord strong in own\/exalted sign adds physical vitality \(\+(\d+) years\)\./g, 'ಲಗ್ನಾಧಿಪತಿ ಸ್ವ/ಉಚ್ಛ ರಾಶಿಯಲ್ಲಿದ್ದು ದೈಹಿಕ ಚೈತನ್ಯವನ್ನು ಹೆಚ್ಚಿಸುತ್ತಾನೆ (+$1 ವರ್ಷಗಳು).')
          .replace(/Ayushkaraka Saturn possesses Neecha Bhanga \(cancellation of debility into longevity stability\)\./g, 'ಆಯುಷ್ಯಕಾರಕ ಶನಿಗೆ ನೀಚಭಂಗ ರಾಜಯೋಗವಿರುವುದರಿಂದ ಆಯುಷ್ಯ ಸ್ಥಿರತೆ ಲಭಿಸುತ್ತದೆ (+2 ವರ್ಷಗಳು).')
          .replace(/Ayushkaraka Saturn in debility applies Kakshya Hrasa reduction \(-(\d+) years\)\./g, 'ಆಯುಷ್ಯಕಾರಕ ಶನಿ ನೀಚನಾಗಿರುವುದರಿಂದ ಕಕ್ಷ್ಯಾ ಹ್ರಾಸ ಇಳಿಕೆ ಉಂಟಾಗುತ್ತದೆ (-$1 ವರ್ಷಗಳು).')
          .replace(/Lagna Lord debilitated in Dusthana applies Kakshya Hrasa \(-(\d+) years\)\./g, 'ಲಗ್ನಾಧಿಪತಿ ದುಃಸ್ಥಾನದಲ್ಲಿ ನೀಚನಾಗಿದ್ದರಿಂದ ಕಕ್ಷ್ಯಾ ಹ್ರಾಸ ಇಳಿಕೆ ಉಂಟಾಗುತ್ತದೆ (-$1 ವರ್ಷಗಳು).')
          .replace(/Lagna Lord in Dusthana \(6\/8\/12\) advises mindful health regimen\./g, 'ಲಗ್ನಾಧಿಪತಿ ದುಃಸ್ಥಾನದಲ್ಲಿದ್ದು (6/8/12) ಆರೋಗ್ಯದಲ್ಲಿ ಜಾಗರೂಕತೆ ಅಗತ್ಯ (-2 ವರ್ಷಗಳು).')
          .replace(/Lagna hemmed between malefics in 12th & 2nd \(Papakarthari Yoga\) cautions physical vitality \(-(\d+) years\)\./g, 'ಲಗ್ನವು ಪಾಪಕರ್ತರಿ ಯೋಗದಲ್ಲಿದ್ದು ಆರೋಗ್ಯದ ಬಗ್ಗೆ ಎಚ್ಚರಿಕೆ ಅಗತ್ಯ (-$1 ವರ್ಷಗಳು).')
          .replace(/Moon hemmed between malefics in 12th & 2nd \(Papakarthari Yoga on Moon\) cautions vitality \(-(\d+) years\)\./g, 'ಚಂದ್ರನು ಪಾಪಕರ್ತರಿ ಯೋಗದಲ್ಲಿದ್ದು ಮಾನಸಿಕ/ದೈಹಿಕ ಆರೋಗ್ಯದ ಬಗ್ಗೆ ಎಚ್ಚರಿಕೆ ಅಗತ್ಯ (-$1 ವರ್ಷಗಳು).')
          .replace(/Malefics in Kendras with no benefics in Kendras applies Kakshya Hrasa \(-(\d+) years\)\./g, 'ಕೇಂದ್ರಗಳಲ್ಲಿ ಶುಭ ಗ್ರಹಗಳಿಲ್ಲದೆ ಕೇವಲ ಪಾಪ ಗ್ರಹಗಳಿರುವುದರಿಂದ ಕಕ್ಷ್ಯಾ ಹ್ರಾಸ ಉಂಟಾಗುತ್ತದೆ (-$1 ವರ್ಷಗಳು).');
      break;

    case 'ml':
      res = res
          .replace(/All 3 Jaimini pairs agree unanimously on Poornayu\./g, 'എല്ലാ 3 ജൈമിനി ജോഡികളും ഏകകണ്ഠമായി പൂർണ്ണായുസ്സ് തിരഞ്ഞെടുത്തു.')
          .replace(/All 3 Jaimini pairs agree unanimously on Madhyayu\./g, 'എല്ലാ 3 ജൈമിനി ജോഡികളും ഏകകണ്ഠമായി മദ്ധ്യായുസ്സ് തിരഞ്ഞെടുത്തു.')
          .replace(/All 3 Jaimini pairs agree unanimously on Alpayu\./g, 'എല്ലാ 3 ജൈമിനി ജോഡികളും ഏകകണ്ഠമായി അല്പായുസ്സ് തിരഞ്ഞെടുത്തു.')
          .replace(/Majority consensus: 2 of 3 pairs agree on Poornayu\./g, 'ഭൂരിപക്ഷ തീരുമാനം: 3-ൽ 2 ജോഡികൾ പൂർണ്ണായുസ്സ് തിരഞ്ഞെടുത്തു.')
          .replace(/Majority consensus: 2 of 3 pairs agree on Madhyayu\./g, 'ഭൂരിപക്ഷ തീരുമാനം: 3-ൽ 2 ജോഡികൾ മദ്ധ്യായുസ്സ് തിരഞ്ഞെടുത്തു.')
          .replace(/Majority consensus: 2 of 3 pairs agree on Alpayu\./g, 'ഭൂരിപക്ഷ തീരുമാനം: 3-ൽ 2 ജോഡികൾ അല്പായുസ്സ് തിരഞ്ഞെടുത്തു.')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(elevating longevity compartment from Alpayu to Madhyayu baseline ~68 yrs\)\./g, 'ഗുരു കേന്ദ്ര/ത്രികോണത്തിൽ ശുഭ ബലത്തോടെ സ്ഥിതി ചെയ്തു കക്ഷ്യാ വൃദ്ധി നൽകുന്നു (അല്പായുസ്സിൽ നിന്ന് മദ്ധ്യായുസ്സ് ~68 വർഷത്തിലേക്ക് ഉയർച്ച).')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(elevating longevity compartment from Madhyayu to Poornayu baseline ~82 yrs\)\./g, 'ഗുരു കേന്ദ്ര/ത്രികോണത്തിൽ ശുഭ ബലത്തോടെ സ്ഥിതി ചെയ്തു കക്ഷ്യാ വൃദ്ധി നൽകുന്നു (മദ്ധ്യായുസ്സിൽ നിന്ന് പൂർണ്ണായുസ്സ് ~82 വർഷത്തിലേക്ക് ഉയർച്ച).')
          .replace(/Jupiter benefic Kendra\/Trikona placement confers Kakshya Vriddhi \(\+(\d+) years\)\./g, 'ഗുരു ശുഭ കേന്ദ്ര/ത്രികോണത്തിൽ സ്ഥിതി ചെയ്തു കക്ഷ്യാ വൃദ്ധി നൽകുന്നു (+$1 വർഷങ്ങൾ).')
          .replace(/Atmakaraka \((.*?)\) in Kendra\/Trikona\/Exalted confers Kakshya Vriddhi \(elevating longevity compartment from Alpayu to Madhyayu\)\./g, 'ആത്മകാരകൻ ($1) കേന്ദ്ര/ത്രികോണ/ഉച്ചത്തിൽ നിന്ന് കക്ഷ്യാ വൃദ്ധി നൽകുന്നു (അല്പായുസ്സിൽ നിന്ന് മദ്ധ്യായുസ്സിലേക്ക് ഉയർച്ച).')
          .replace(/Atmakaraka \((.*?)\) in Kendra\/Trikona\/Exalted reinforces longevity vitality \(\+(\d+) years\)\./g, 'ആത്മകാരകൻ ($1) കേന്ദ്ര/ത്രികോണ/ഉച്ചത്തിൽ നിന്ന് ആയുർബലം വർദ്ധിപ്പിക്കുന്നു (+$2 വർഷങ്ങൾ).')
          .replace(/Ayushkaraka Saturn in Own\/Exalted sign reinforces longevity \(\+(\d+) years\)\./g, 'ആയുഷ്കാരകനായ ശനി സ്വ/ഉച്ച ക്ഷേത്രത്തിൽ നിന്ന് ആയുർദൈർഘ്യം ഉറപ്പാക്കുന്നു (+$1 വർഷങ്ങൾ).')
          .replace(/Lagna Lord strong in own\/exalted\/Kendra\/Trikona adds physical vitality \(\+(\d+) years\)\./g, 'ലഗ്നാധിപൻ സ്വ/ഉച്ച/കേന്ദ്ര/ത്രികോണത്തിൽ ശക്തനായി ശാരീരിക ഓജസ്സ് വർദ്ധിപ്പിക്കുന്നു (+$1 വർഷങ്ങൾ).')
          .replace(/Lagna Lord strong in own\/exalted sign adds physical vitality \(\+(\d+) years\)\./g, 'ലഗ്നാധിപൻ സ്വ/ഉച്ച ക്ഷേത്രത്തിൽ ശക്തനായി ശാരീരിക ഓജസ്സ് വർദ്ധിപ്പിക്കുന്നു (+$1 വർഷങ്ങൾ).')
          .replace(/Ayushkaraka Saturn possesses Neecha Bhanga \(cancellation of debility into longevity stability\)\./g, 'ആയുഷ്കാരകനായ ശനിക്ക് നീചഭംഗ രാജയോഗമുള്ളതിനാൽ ആയുസ്സിന് സ്ഥിരത ലഭിക്കുന്നു (+2 വർഷങ്ങൾ).')
          .replace(/Ayushkaraka Saturn in debility applies Kakshya Hrasa reduction \(-(\d+) years\)\./g, 'ആയുഷ്കാരകനായ ശനി നീചനായതിനാൽ കക്ഷ്യാ ഹ്രാസ കുറവ് ബാധകമാകുന്നു (-$1 വർഷങ്ങൾ).')
          .replace(/Lagna Lord debilitated in Dusthana applies Kakshya Hrasa \(-(\d+) years\)\./g, 'ലഗ്നാധിപൻ ദുസ്ഥാനത്ത് നീചനായതിനാൽ കക്ഷ്യാ ഹ്രാസ കുറവ് ബാധകമാകുന്നു (-$1 വർഷങ്ങൾ).')
          .replace(/Lagna Lord in Dusthana \(6\/8\/12\) advises mindful health regimen\./g, 'ലഗ്നാധിപൻ ദുസ്ഥാനങ്ങളിൽ (6/8/12) നിൽക്കുന്നതിനാൽ ആരോഗ്യത്തിൽ പ്രത്യേക ശ്രദ്ധ ആവശ്യമാണ് (-2 വർഷങ്ങൾ).')
          .replace(/Lagna hemmed between malefics in 12th & 2nd \(Papakarthari Yoga\) cautions physical vitality \(-(\d+) years\)\./g, 'ലഗ്നം പാപകർത്താരി യോഗത്തിലായതിനാൽ ആരോഗ്യ ശ്രദ്ധ ആവശ്യമാണ് (-$1 വർഷങ്ങൾ).')
          .replace(/Moon hemmed between malefics in 12th & 2nd \(Papakarthari Yoga on Moon\) cautions vitality \(-(\d+) years\)\./g, 'ചന്ദ്രൻ പാപകർത്താരി യോഗത്തിലായതിനാൽ മാനസിക/ശാരീരിക ആരോഗ്യ ശ്രദ്ധ ആവശ്യമാണ് (-$1 വർഷങ്ങൾ).')
          .replace(/Malefics in Kendras with no benefics in Kendras applies Kakshya Hrasa \(-(\d+) years\)\./g, 'കേന്ദ്രങ്ങളിൽ ശുഭന്മാരില്ലാതെ പാപ ഗ്രഹങ്ങൾ മാത്രമുള്ളതിനാൽ കക്ഷ്യാ ഹ്രാസം ഉണ്ടാകുന്നു (-$1 വർഷങ്ങൾ).');
      break;
  }

    const KAKSHYA_PLANETS = {
      ta: { Sun: 'சூரியன்', Moon: 'சந்திரன்', Mars: 'செவ்வாய்', Mercury: 'புதன்', Jupiter: 'குரு', Venus: 'சுக்கிரன்', Saturn: 'சனி', Rahu: 'ராகு', Ketu: 'கேது' },
      hi: { Sun: 'सूर्य', Moon: 'चन्द्र', Mars: 'मंगल', Mercury: 'बुध', Jupiter: 'गुरु', Venus: 'शुक्र', Saturn: 'शनि', Rahu: 'राहु', Ketu: 'केतु' },
      te: { Sun: 'సూర్యుడు', Moon: 'చంద్రుడు', Mars: 'కుజుడు', Mercury: 'బుధుడు', Jupiter: 'గురువు', Venus: 'శుక్రుడు', Saturn: 'శని', Rahu: 'రాహువు', Ketu: 'కేతువు' },
      kn: { Sun: 'ಸೂರ್ಯ', Moon: 'ಚಂದ್ರ', Mars: 'ಮಂಗಳ', Mercury: 'ಬುಧ', Jupiter: 'ಗುರು', Venus: 'ಶುಕ್ರ', Saturn: 'ಶನಿ', Rahu: 'ರಾಹು', Ketu: 'ಕೇತು' },
      ml: { Sun: 'സൂര്യൻ', Moon: 'ചന്ദ്രൻ', Mars: 'ചൊവ്വ', Mercury: 'ബുധൻ', Jupiter: 'ഗുരു', Venus: 'ശുക്രൻ', Saturn: 'ശനി', Rahu: 'രാഹു', Ketu: 'കേതു' }
    };

    if (KAKSHYA_PLANETS[language]) {
      Object.entries(KAKSHYA_PLANETS[language]).forEach(([eng, loc]) => {
        res = res.replaceAll(`(${eng})`, `(${loc})`);
      });
    }

    return res;
  };

  const translateRiskCategory = (risk) => {
    if (!risk) return '';
    switch (risk) {
      case 'CRITICAL_TRISHOOLA_RUDRA':
        return language === 'ta' ? 'அதிதீவிர திரிசூல-ருத்ர காலம் (Critical)' :
               language === 'hi' ? 'अति-गंभीर त्रिशूल-रुद्र काल (Critical)' :
               language === 'te' ? 'అత్యంత తీవ్ర త్రిశూల-రుద్ర కాలం (Critical)' :
               language === 'kn' ? 'ಅತಿ ಗಂಭೀರ ತ್ರಿಶೂಲ-ರುದ್ರ ಕಾಲ (Critical)' :
               language === 'ml' ? 'അതീവ ഗുരുതര ത്രിശൂല-രുദ്ര കാലം (Critical)' :
               'Critical (Trishoola + Rudra)';
      case 'HIGH_TRISHOOLA':
        return language === 'ta' ? 'திரிசூல காலம் (High Risk)' :
               language === 'hi' ? 'त्रिशूल काल (उच्च जोखिम)' :
               language === 'te' ? 'త్రిశూల కాలం (అధిక ప్రమాదం)' :
               language === 'kn' ? 'ತ್ರಿಶೂಲ ಕಾಲ (ಹೆಚ್ಚಿನ ಅಪಾಯ)' :
               language === 'ml' ? 'ത്രിശൂല കാലം (ഉയർന്ന അപകട സാധ്യത)' :
               'High Risk (Trishoola)';
      case 'HIGH_RUDRA':
        return language === 'ta' ? 'ருத்ர காலம் (High Risk)' :
               language === 'hi' ? 'रुद्र काल (उच्च जोखिम)' :
               language === 'te' ? 'రుద్ర కాలం (అధిక ప్రమాదం)' :
               language === 'kn' ? 'ರುದ್ರ ಕಾಲ (ಹೆಚ್ಚಿನ ಅಪಾಯ)' :
               language === 'ml' ? 'രുദ്ര കാലം (ഉയർന്ന അപകട സാധ്യത)' :
               'High Risk (Rudra)';
      case 'MODERATE':
        return language === 'ta' ? 'சாதாரண காலம் (Moderate)' :
               language === 'hi' ? 'सामान्य काल (Moderate)' :
               language === 'te' ? 'సాధారణ కాలం (Moderate)' :
               language === 'kn' ? 'ಸಾಮಾನ್ಯ ಕಾಲ (Moderate)' :
               language === 'ml' ? 'സാധാരണ കാലം (Moderate)' :
               'Moderate';
      default:
        return risk;
    }
  };

  const translatePrakriti = (prakriti) => {
    if (!prakriti || language === 'en') return prakriti;
    const map = {
      'Kapha-Pitta': { ta: 'கப-பித்தம்', hi: 'कफ-पित्त', te: 'కఫ-పిత్త', kn: 'ಕಫ-ಪಿತ್ತ', ml: 'കഫ-പിത്ത' },
      'Vata-Pitta': { ta: 'வாத-பித்தம்', hi: 'वात-पित्त', te: 'వాత-పిత్త', kn: 'ವಾತ-ಪಿತ್ತ', ml: 'വാത-പിത്ത' },
      'Pitta-Vata': { ta: 'பித்த-வாதம்', hi: 'पित्त-वात', te: 'పిత్త-వాత', kn: 'ಪಿತ್ತ-ವಾತ', ml: 'പിത്ത-വാത' },
      'Pitta-Kapha': { ta: 'பித்த-கபம்', hi: 'पित्त-कफ', te: 'పిత్త-కఫ', kn: 'ಪಿತ್ತ-ಕಫ', ml: 'പിത്ത-കഫ' },
      'Vata-Kapha': { ta: 'வாத-கபம்', hi: 'वात-कफ', te: 'వాత-కఫ', kn: 'ವಾತ-ಕಫ', ml: 'വാത-കഫ' },
      'Kapha-Vata': { ta: 'கப-வாதம்', hi: 'कफ-वात', te: 'ಕಫ-వాత', kn: 'ಕಫ-ವಾತ', ml: 'കഫ-വാത' },
      'Pitta Dominant': { ta: 'பித்த பிரதானம்', hi: 'पित्त प्रधान', te: 'పిత్త ప్రధానం', kn: 'ಪಿತ್ತ ಪ್ರಧಾನ', ml: 'പിത്ത പ്രധാനം' },
      'Vata Dominant': { ta: 'வாத பிரதானம்', hi: 'वात प्रधान', te: 'వాత ప్రధానం', kn: 'ವಾತ ಪ್ರಧಾನ', ml: 'വാത ಪ್ರಧാനം' },
      'Kapha Dominant': { ta: 'கப பிரதானம்', hi: 'कफ प्रधान', te: 'కఫ ప్రధానం', kn: 'ಕಫ ಪ್ರಧಾನ', ml: 'കಫ ಪ್ರಧാനം' }
    };
    const match = map[prakriti];
    return (match && match[language]) ? match[language] : prakriti;
  };

  const translateLagnaElement = (elem) => {
    if (!elem || language === 'en') return elem;
    let res = elem;
    switch (language) {
      case 'ta':
        res = res
          .replace(/Agni \(Fire\)/g, 'அக்னி (நெருப்பு)')
          .replace(/Prithvi \(Earth\)/g, 'பிருத்வி (பூமி)')
          .replace(/Vayu \(Air\)/g, 'வாயு (காற்று)')
          .replace(/Jala \(Water\)/g, 'ஜலம் (நீர்)');
        break;
      case 'hi':
        res = res
          .replace(/Agni \(Fire\)/g, 'अग्नि (अग्नि तत्व)')
          .replace(/Prithvi \(Earth\)/g, 'पृथ्वी (पृथ्वी तत्व)')
          .replace(/Vayu \(Air\)/g, 'वायु (वायु तत्व)')
          .replace(/Jala \(Water\)/g, 'जल (जल तत्व)');
        break;
      case 'te':
        res = res
          .replace(/Agni \(Fire\)/g, 'అగ్ని (అగ్ని తత్త్వం)')
          .replace(/Prithvi \(Earth\)/g, 'పృథ్వి (భూ తత్త్వం)')
          .replace(/Vayu \(Air\)/g, 'వాయు (వాయు తత్త్వం)')
          .replace(/Jala \(Water\)/g, 'జల (జల తత్త్వం)');
        break;
      case 'kn':
        res = res
          .replace(/Agni \(Fire\)/g, 'ಅಗ್ನಿ (ಅಗ್ನಿ ತತ್ವ)')
          .replace(/Prithvi \(Earth\)/g, 'ಪೃಥ್ವಿ (ಭೂ ತತ್ವ)')
          .replace(/Vayu \(Air\)/g, 'ವಾಯು (ವಾಯು ತತ್ವ)')
          .replace(/Jala \(Water\)/g, 'ಜಲ (ಜಲ ತತ್ವ)');
        break;
      case 'ml':
        res = res
          .replace(/Agni \(Fire\)/g, 'അഗ്നി (അഗ്നി തത്വം)')
          .replace(/Prithvi \(Earth\)/g, 'പൃഥ്വി (ഭൂ തത്വം)')
          .replace(/Vayu \(Air\)/g, 'വായു (വായു തത്വം)')
          .replace(/Jala \(Water\)/g, 'ജലം (ജല തത്വം)');
        break;
    }
    return translateRashi(res);
  };

  const translateStartingSignReason = (reason) => {
    if (!reason || language === 'en') return reason;
    let res = reason;
    const rashiMap = {
      'Aries': { ta: 'மேஷம்', hi: 'मेष', te: 'మేషం', kn: 'ಮೇಷ', ml: 'മേടം' },
      'Mesha': { ta: 'மேஷம்', hi: 'मेष', te: 'మేషం', kn: 'ಮೇಷ', ml: 'മേടം' },
      'Taurus': { ta: 'ரிஷபம்', hi: 'वृषभ', te: 'వృషభం', kn: 'ವೃಷಭ', ml: 'ഇടവം' },
      'Vrishabha': { ta: 'ரிஷபம்', hi: 'वृषभ', te: 'వృషభం', kn: 'ವೃಷಭ', ml: 'ഇടവം' },
      'Gemini': { ta: 'மிதுனம்', hi: 'मिथुन', te: 'మిథునం', kn: 'ಮಿಥುನ', ml: 'മിഥുനം' },
      'Mithuna': { ta: 'மிதுனம்', hi: 'मिथुन', te: 'మిథునం', kn: 'ಮಿಥುನ', ml: 'മിഥുനം' },
      'Cancer': { ta: 'கடகம்', hi: 'कर्क', te: 'కర్కాటకం', kn: 'ಕರ್ಕಾಟಕ', ml: 'കർക്കടകം' },
      'Kataka': { ta: 'கடகம்', hi: 'कर्क', te: 'కర్కాటకం', kn: 'ಕರ್ಕಾಟಕ', ml: 'കർക്കടകം' },
      'Karka': { ta: 'கடகம்', hi: 'कर्क', te: 'కర్కాటకం', kn: 'ಕರ್ಕಾಟಕ', ml: 'കർക്കടകം' },
      'Leo': { ta: 'சிம்மம்', hi: 'सिंह', te: 'సింహం', kn: 'ಸಿಂಹ', ml: 'ചിങ്ങം' },
      'Simha': { ta: 'சிம்மம்', hi: 'सिंह', te: 'సింహం', kn: 'ಸಿಂಹ', ml: 'ചിങ്ങം' },
      'Virgo': { ta: 'கன்னி', hi: 'कन्या', te: 'కన్య', kn: 'ಕನ್ಯಾ', ml: 'കന്നി' },
      'Kanya': { ta: 'கன்னி', hi: 'कन्या', te: 'ಕన్య', kn: 'ಕನ್ಯಾ', ml: 'കന്നി' },
      'Libra': { ta: 'துலாம்', hi: 'तुला', te: 'తులా', kn: 'ತುಲಾ', ml: 'തുലാം' },
      'Tula': { ta: 'துலாம்', hi: 'तुला', te: 'తులా', kn: 'ತುಲಾ', ml: 'തുಲാം' },
      'Scorpio': { ta: 'விருச்சிகம்', hi: 'वृश्चिक', te: 'వృశ్చికం', kn: 'ವೃಶ್ಚಿಕ', ml: 'വൃശ്ചികം' },
      'Vrishchika': { ta: 'விருச்சிகம்', hi: 'वृश्चिक', te: 'వృశ్చికం', kn: 'ವೃಶ್ಚಿಕ', ml: 'വൃശ്ചികം' },
      'Sagittarius': { ta: 'தனுசு', hi: 'धनु', te: 'ధనుస్సు', kn: 'ಧನುಸ್ಸು', ml: 'ധനു' },
      'Dhanus': { ta: 'தனுசு', hi: 'धनु', te: 'ధనుస్సు', kn: 'ಧನುಸ್ಸು', ml: 'ധനു' },
      'Dhanu': { ta: 'தனுசு', hi: 'धनु', te: 'ధనుస్సు', kn: 'ಧನುಸ್ಸು', ml: 'ധനു' },
      'Capricorn': { ta: 'மகரம்', hi: 'மकर', te: 'మకరం', kn: 'ಮಕರ', ml: 'മകരം' },
      'Makara': { ta: 'மகரம்', hi: 'मकर', te: 'మకరం', kn: 'ಮಕರ', ml: 'മകരം' },
      'Aquarius': { ta: 'கும்பம்', hi: 'कुंभ', te: 'కుంభం', kn: 'ಕುಂಭ', ml: 'കുംഭം' },
      'Kumbha': { ta: 'கும்பம்', hi: 'कुंभ', te: 'కుంభం', kn: 'ಕುಂಭ', ml: 'കുംഭം' },
      'Pisces': { ta: 'மீனம்', hi: 'मीन', te: 'మీనం', kn: 'ಮೀನ', ml: 'മീനം' },
      'Meena': { ta: 'மீனம்', hi: 'मीन', te: 'మీనం', kn: 'ಮೀನ', ml: 'മീനം' }
    };

    Object.entries(rashiMap).forEach(([rashi, transObj]) => {
      if (transObj[language]) {
        res = res.replaceAll(rashi, transObj[language]);
      }
    });

    switch (language) {
      case 'ta':
        return res
          .replace(/Lagna sign \(([^)]+)\) selected: more conjoined planets \((\d+) vs (\d+)\)/g, 'லக்ன ராசி ($1) தேர்வு செய்யப்பட்டது: அதிக இணைந்த கிரகங்கள் ($2 vs $3)')
          .replace(/7th House sign \(([^)]+)\) selected: more conjoined planets \((\d+) vs (\d+)\)/g, '7-ஆம் பாவ ராசி ($1) தேர்வு செய்யப்பட்டது: அதிக இணைந்த கிரகங்கள் ($2 vs $3)')
          .replace(/Lagna sign \(([^)]+)\) selected: higher planetary dignity \((\d+) exalted\/own vs (\d+)\)/g, 'லக்ன ராசி ($1) தேர்வு செய்யப்பட்டது: உயர்ந்த கிரக பலம்/ஆட்சி/உச்சம் ($2 vs $3)')
          .replace(/7th House sign \(([^)]+)\) selected: higher planetary dignity \((\d+) exalted\/own vs (\d+)\)/g, '7-ஆம் பாவ ராசி ($1) தேர்வு செய்யப்பட்டது: உயர்ந்த கிரக பலம்/ஆட்சி/உச்சம் ($2 vs $3)')
          .replace(/Lagna sign \(([^)]+)\) selected: stronger Jupiter\/benefic influence/g, 'லக்ன ராசி ($1) தேர்வு செய்யப்பட்டது: வலிமையான குரு/சுப பார்வை செல்வாக்கு')
          .replace(/7th House sign \(([^)]+)\) selected: stronger Jupiter\/benefic influence/g, '7-ஆம் பாவ ராசி ($1) தேர்வு செய்யப்பட்டது: வலிமையான குரு/சுப பார்வை செல்வாக்கு')
          .replace(/Lagna sign \(([^)]+)\) selected by default \(equal strength with 7th house\)/g, 'லக்ன ராசி ($1) இயல்புநிலையாக தேர்வு செய்யப்பட்டது (7-ஆம் பாவகத்துடன் சம பலம்)')
          .replace(/Lagna \(Sign (\d+)\) has higher planetary strength than 7th house \(Sign (\d+)\)\./g, 'லக்னம் (ராசி $1) 7-ஆம் பாவகத்தை (ராசி $2) விட அதிக கிரக பலம் பெற்றுள்ளது.')
          .replace(/7th house \(Sign (\d+)\) has higher planetary strength than Lagna \(Sign (\d+)\)\./g, '7-ஆம் பாவகம் (ராசி $1) லக்னத்தை (ராசி $2) விட அதிக கிரக பலம் பெற்றுள்ளது.')
          .replace(/Lagna and 7th house have equal strength; Lagna chosen by default\./g, 'லக்னம் மற்றும் 7-ஆம் பாவகம் சம பலத்துடன் உள்ளன; விதிப்படி லக்னம் தேர்ந்தெடுக்கப்பட்டது.');
      case 'hi':
        return res
          .replace(/Lagna sign \(([^)]+)\) selected: more conjoined planets \((\d+) vs (\d+)\)/g, 'लग्न राशि ($1) चयनित: अधिक युत ग्रह ($2 vs $3)')
          .replace(/7th House sign \(([^)]+)\) selected: more conjoined planets \((\d+) vs (\d+)\)/g, '7वां भाव राशि ($1) चयनित: अधिक युत ग्रह ($2 vs $3)')
          .replace(/Lagna sign \(([^)]+)\) selected: higher planetary dignity \((\d+) exalted\/own vs (\d+)\)/g, 'लग्न राशि ($1) चयनित: उच्चतर ग्रह गरिमा (स्व/उच्च $2 vs $3)')
          .replace(/7th House sign \(([^)]+)\) selected: higher planetary dignity \((\d+) exalted\/own vs (\d+)\)/g, '7वां भाव राशि ($1) चयनित: उच्चतर ग्रह गरिमा (स्व/उच्च $2 vs $3)')
          .replace(/Lagna sign \(([^)]+)\) selected: stronger Jupiter\/benefic influence/g, 'लग्न राशि ($1) चयनित: प्रबल गुरु/शुभ प्रभाव')
          .replace(/7th House sign \(([^)]+)\) selected: stronger Jupiter\/benefic influence/g, '7वां भाव राशि ($1) चयनित: प्रबल गुरु/शुभ प्रभाव')
          .replace(/Lagna sign \(([^)]+)\) selected by default \(equal strength with 7th house\)/g, 'लग्न राशि ($1) स्वतः चयनित (7वें भाव के साथ समान बल)')
          .replace(/Lagna \(Sign (\d+)\) has higher planetary strength than 7th house \(Sign (\d+)\)\./g, 'लग्न (राशि $1) 7वें भाव (राशि $2) की तुलना में अधिक ग्रह बल रखता है।')
          .replace(/7th house \(Sign (\d+)\) has higher planetary strength than Lagna \(Sign (\d+)\)\./g, '7वां भाव (राशि $1) लग्न (राशि $2) की तुलना में अधिक ग्रह बल रखता है।')
          .replace(/Lagna and 7th house have equal strength; Lagna chosen by default\./g, 'लग्न एवं 7वें भाव का बल समान है; नियमानुसार लग्न चुना गया।');
      case 'te':
        return res
          .replace(/Lagna sign \(([^)]+)\) selected: more conjoined planets \((\d+) vs (\d+)\)/g, 'లగ్న రాశి ($1) ఎంపిక చేయబడింది: ఎక్కువ కలిసి ఉన్న గ్రహాలు ($2 vs $3)')
          .replace(/7th House sign \(([^)]+)\) selected: more conjoined planets \((\d+) vs (\d+)\)/g, '7వ స్థాన రాశి ($1) ఎంపిక చేయబడింది: ఎక్కువ కలిసి ఉన్న గ్రహాలు ($2 vs $3)')
          .replace(/Lagna sign \(([^)]+)\) selected: higher planetary dignity \((\d+) exalted\/own vs (\d+)\)/g, 'లగ్న రాశి ($1) ఎంపిక చేయబడింది: ఉన్నత గ్రహ బలం (స్వ/ఉచ్చ $2 vs $3)')
          .replace(/7th House sign \(([^)]+)\) selected: higher planetary dignity \((\d+) exalted\/own vs (\d+)\)/g, '7వ స్థాన రాశి ($1) ఎంపిక చేయబడింది: ఉన్నత గ్రహ బలం (స్వ/ఉచ్చ $2 vs $3)')
          .replace(/Lagna sign \(([^)]+)\) selected: stronger Jupiter\/benefic influence/g, 'లగ్న రాశి ($1) ఎంపిక చేయబడింది: బలమైన గురు/శుభ ప్రభావం')
          .replace(/7th House sign \(([^)]+)\) selected: stronger Jupiter\/benefic influence/g, '7వ స్థాన రాశి ($1) ఎంపిక చేయబడింది: బలమైన గురు/శుభ ప్రభావం')
          .replace(/Lagna sign \(([^)]+)\) selected by default \(equal strength with 7th house\)/g, 'లగ్న రాశి ($1) సాధారణంగా ఎంపిక చేయబడింది (7వ స్థానంతో సమాన బలం)')
          .replace(/Lagna \(Sign (\d+)\) has higher planetary strength than 7th house \(Sign (\d+)\)\./g, 'లగ్నం (రాశి $1) 7వ స్థానం (రాశి $2) కంటే ఎక్కువ గ్రహ బలం కలిగి ఉంది.')
          .replace(/7th house \(Sign (\d+)\) has higher planetary strength than Lagna \(Sign (\d+)\)\./g, '7వ స్థానం (రాశి $1) లగ్నం (రాశి $2) కంటే ఎక్కువ గ్రహ బలం కలిగి ఉంది.')
          .replace(/Lagna and 7th house have equal strength; Lagna chosen by default\./g, 'లగ్నం మరియు 7వ స్థానం సమాన బలం కలిగి ఉన్నాయి; నిబంధన ప్రకారం లగ్నం ఎంపిక చేయబడింది.');
      case 'kn':
        return res
          .replace(/Lagna sign \(([^)]+)\) selected: more conjoined planets \((\d+) vs (\d+)\)/g, 'ಲಗ್ನ ರಾಶಿ ($1) ಆಯ್ಕೆಯಾಗಿದೆ: ಹೆಚ್ಚು ಸಂಯೋಜಿತ ಗ್ರಹಗಳು ($2 vs $3)')
          .replace(/7th House sign \(([^)]+)\) selected: more conjoined planets \((\d+) vs (\d+)\)/g, '7ನೇ ಮನೆ ರಾಶಿ ($1) ಆಯ್ಕೆಯಾಗಿದೆ: ಹೆಚ್ಚು ಸಂಯೋಜಿತ ಗ್ರಹಗಳು ($2 vs $3)')
          .replace(/Lagna sign \(([^)]+)\) selected: higher planetary dignity \((\d+) exalted\/own vs (\d+)\)/g, 'ಲಗ್ನ ರಾಶಿ ($1) ಆಯ್ಕೆಯಾಗಿದೆ: ಉನ್ನತ ಗ್ರಹ ಬಲ (ಸ್ವ/ಉಚ್ಚ $2 vs $3)')
          .replace(/7th House sign \(([^)]+)\) selected: higher planetary dignity \((\d+) exalted\/own vs (\d+)\)/g, '7ನೇ ಮನೆ ರಾಶಿ ($1) ಆಯ್ಕೆಯಾಗಿದೆ: ಉನ್ನತ ಗ್ರಹ ಬಲ (ಸ್ವ/ಉಚ್ಚ $2 vs $3)')
          .replace(/Lagna sign \(([^)]+)\) selected: stronger Jupiter\/benefic influence/g, 'ಲಗ್ನ ರಾಶಿ ($1) ಆಯ್ಕೆಯಾಗಿದೆ: ಬಲವಾದ ಗುರು/ಶುಭ ಪ್ರಭಾವ')
          .replace(/7th House sign \(([^)]+)\) selected: stronger Jupiter\/benefic influence/g, '7ನೇ ಮನೆ ರಾಶಿ ($1) ಆಯ್ಕೆಯಾಗಿದೆ: ಬಲವಾದ ಗುರು/ಶುಭ ಪ್ರಭಾವ')
          .replace(/Lagna sign \(([^)]+)\) selected by default \(equal strength with 7th house\)/g, 'ಲಗ್ನ ರಾಶಿ ($1) ಪೂರ್ವನಿಯೋಜಿತವಾಗಿ ಆಯ್ಕೆಯಾಗಿದೆ (7ನೇ ಮನೆಯೊಂದಿಗೆ ಸಮಾನ ಬಲ)')
          .replace(/Lagna \(Sign (\d+)\) has higher planetary strength than 7th house \(Sign (\d+)\)\./g, 'ಲಗ್ನವು (ರಾಶಿ $1) 7ನೇ ಮನೆಗಿಂತ (ರಾಶಿ $2) ಹೆಚ್ಚು ಗ್ರಹ ಬಲವನ್ನು ಹೊಂದಿದೆ.')
          .replace(/7th house \(Sign (\d+)\) has higher planetary strength than Lagna \(Sign (\d+)\)\./g, '7ನೇ ಮನೆಯು (ರಾಶಿ $1) ಲಗ್ನಕ್ಕಿಂತ (ರಾಶಿ $2) ಹೆಚ್ಚು ಗ್ರಹ ಬಲವನ್ನು ಹೊಂದಿದೆ.')
          .replace(/Lagna and 7th house have equal strength; Lagna chosen by default\./g, 'ಲಗ್ನ ಮತ್ತು 7ನೇ ಮನೆ ಸಮಾನ ಬಲ ಹೊಂದಿವೆ; ನಿಯಮದಂತೆ ಲಗ್ನ ಆಯ್ಕೆಯಾಗಿದೆ.');
      case 'ml':
        return res
          .replace(/Lagna sign \(([^)]+)\) selected: more conjoined planets \((\d+) vs (\d+)\)/g, 'ലഗ്ന രാശി ($1) തിരഞ്ഞെടുക്കപ്പെട്ടു: കൂടുതൽ സംയോജിത ഗ്രഹങ്ങൾ ($2 vs $3)')
          .replace(/7th House sign \(([^)]+)\) selected: more conjoined planets \((\d+) vs (\d+)\)/g, '7-ാം ഭാവ രാശി ($1) തിരഞ്ഞെടുക്കപ്പെട്ടു: കൂടുതൽ സംയോജിത ഗ്രഹങ്ങൾ ($2 vs $3)')
          .replace(/Lagna sign \(([^)]+)\) selected: higher planetary dignity \((\d+) exalted\/own vs (\d+)\)/g, 'ലഗ്ന രാശി ($1) തിരഞ്ഞെടുക്കപ്പെട്ടു: ഉയർന്ന ഗ്രഹ ബലം (സ്വ/ഉച്ച $2 vs $3)')
          .replace(/7th House sign \(([^)]+)\) selected: higher planetary dignity \((\d+) exalted\/own vs (\d+)\)/g, '7-ാം ഭാവ രാശി ($1) തിരഞ്ഞെടുക്കപ്പെട്ടു: ഉയർന്ന ഗ്രഹ ബലം (സ്വ/ഉച്ച $2 vs $3)')
          .replace(/Lagna sign \(([^)]+)\) selected: stronger Jupiter\/benefic influence/g, 'ലഗ്ന രാശി ($1) തിരഞ്ഞെടുക്കപ്പെട്ടു: ശക്തമായ വ്യാഴ/ശുഭ സ്വാധീനം')
          .replace(/7th House sign \(([^)]+)\) selected: stronger Jupiter\/benefic influence/g, '7-ാം ഭാവ രാശി ($1) തിരഞ്ഞെടുക്കപ്പെട്ടു: ശക്തമായ വ്യാഴ/ശുഭ സ്വാധീനം')
          .replace(/Lagna sign \(([^)]+)\) selected by default \(equal strength with 7th house\)/g, 'ലഗ്ന രാശി ($1) സ്വതവേ തിരഞ്ഞെടുക്കപ്പെട്ടു (7-ാം ഭാവവുമായി തുല്യ ബലം)')
          .replace(/Lagna \(Sign (\d+)\) has higher planetary strength than 7th house \(Sign (\d+)\)\./g, 'ലഗ്നം (രാശി $1) 7-ാം ഭാവത്തേക്കാൾ (രാശി $2) ഉയർന്ന ഗ്രഹ ബലമുണ്ട്.')
          .replace(/7th house \(Sign (\d+)\) has higher planetary strength than Lagna \(Sign (\d+)\)\./g, '7-ാം ഭാവത്തിന് (രാശി $1) ലഗ്നത്തേക്കാൾ (രാശി $2) ഉയർന്ന ഗ്രഹ ബലമുണ്ട്.')
          .replace(/Lagna and 7th house have equal strength; Lagna chosen by default\./g, 'ലഗ്നത്തിനും 7-ാം ഭാവത്തിനും തുല്യ ബലമാണ്; ലഗ്നം തിരഞ്ഞെടുക്കപ്പെട്ടു.');
    }
    return reason;
  };

  const translateShoolaWindow = (window) => {
    if (!window || language === 'en') return window;
    let res = window;
    const rashiMap = {
      'Aries': { ta: 'மேஷம்', hi: 'मेष', te: 'మేషం', kn: 'ಮೇಷ', ml: 'മേടം' },
      'Mesha': { ta: 'மேஷம்', hi: 'मेष', te: 'మేషం', kn: 'ಮೇಷ', ml: 'മേടം' },
      'Taurus': { ta: 'ரிஷபம்', hi: 'वृषभ', te: 'వృషభం', kn: 'ವೃಷಭ', ml: 'ഇടവം' },
      'Vrishabha': { ta: 'ரிஷபம்', hi: 'वृषभ', te: 'వృషభం', kn: 'ವೃಷಭ', ml: 'ഇടവം' },
      'Gemini': { ta: 'மிதுனம்', hi: 'मिथुन', te: 'మిథునం', kn: 'ಮಿಥುನ', ml: 'മിഥുനം' },
      'Mithuna': { ta: 'மிதுனம்', hi: 'मिथुन', te: 'మిథునం', kn: 'ಮಿಥುನ', ml: 'മിഥുനം' },
      'Cancer': { ta: 'கடகம்', hi: 'कर्क', te: 'కర్కాటకం', kn: 'ಕರ್ಕಾಟಕ', ml: 'కర్ക്കടകം' },
      'Kataka': { ta: 'கடகம்', hi: 'कर्क', te: 'కర్కాటకం', kn: 'ಕರ್ಕಾಟಕ', ml: 'కర్ക്കടകം' },
      'Karka': { ta: 'கடகம்', hi: 'कर्क', te: 'కర్కాటకం', kn: 'ಕರ್ಕಾಟಕ', ml: 'കർക്കടകം' },
      'Leo': { ta: 'சிம்மம்', hi: 'सिंह', te: 'సింహం', kn: 'ಸಿಂಹ', ml: 'ചിങ്ങം' },
      'Simha': { ta: 'சிம்மம்', hi: 'सिंह', te: 'సింహం', kn: 'ಸಿಂಹ', ml: 'ചിങ്ങം' },
      'Virgo': { ta: 'கன்னி', hi: 'कन्या', te: 'కన్య', kn: 'ಕನ್ಯಾ', ml: 'കന്നി' },
      'Kanya': { ta: 'கன்னி', hi: 'कन्या', te: 'కన్య', kn: 'ಕನ್ಯಾ', ml: 'കന്നി' },
      'Libra': { ta: 'துலாம்', hi: 'तुला', te: 'తులా', kn: 'ತುಲಾ', ml: 'തുലാം' },
      'Tula': { ta: 'துலாம்', hi: 'तुला', te: 'తులా', kn: 'ತುಲಾ', ml: 'തുലാം' },
      'Scorpio': { ta: 'விருச்சிகம்', hi: 'वृश्चिक', te: 'వృశ్చికం', kn: 'ವೃಶ್ಚಿಕ', ml: 'വൃശ്ചികം' },
      'Vrishchika': { ta: 'விருச்சிகம்', hi: 'वृश्चिक', te: 'వృశ్చికం', kn: 'ವೃಶ್ಚಿಕ', ml: 'വൃശ്ചികം' },
      'Sagittarius': { ta: 'தனுசு', hi: 'धनु', te: 'ధనుస్సు', kn: 'ಧನುಸ್ಸು', ml: 'ധനു' },
      'Dhanus': { ta: 'தனுசு', hi: 'धनु', te: 'ధనుస్సు', kn: 'ಧನುಸ್ಸು', ml: 'ധനു' },
      'Dhanu': { ta: 'தனுசு', hi: 'धनु', te: 'ధనుస్సు', kn: 'ಧನುಸ್ಸು', ml: 'ധനു' },
      'Capricorn': { ta: 'மகரம்', hi: 'मकर', te: 'మకరం', kn: 'ಮಕರ', ml: 'മകരം' },
      'Makara': { ta: 'மகரம்', hi: 'मकर', te: 'మకరం', kn: 'ಮಕರ', ml: 'മകരം' },
      'Aquarius': { ta: 'கும்பம்', hi: 'कुंभ', te: 'కుంభం', kn: 'ಕುಂಭ', ml: 'കുംഭം' },
      'Kumbha': { ta: 'கும்பம்', hi: 'कुंभ', te: 'కుంభం', kn: 'ಕುಂಭ', ml: 'കുംഭം' },
      'Pisces': { ta: 'மீனம்', hi: 'मीन', te: 'మీనం', kn: 'ಮೀನ', ml: 'മീനം' },
      'Meena': { ta: 'மீனம்', hi: 'मीन', te: 'ಮೀನ', kn: 'ಮೀನ', ml: 'മീനം' }
    };

    Object.entries(rashiMap).forEach(([rashi, transObj]) => {
      if (transObj[language]) {
        res = res.replaceAll(rashi, transObj[language]);
      }
    });

    switch (language) {
      case 'ta':
        return res
          .replace(/Age/g, 'வயது')
          .replace(/represents the primary Trishoola longevity transition window\./g, 'முக்கிய சூல ஆயுள் எச்சரிக்கை காலமாகும்.')
          .replace(/represents a key Rudra health transition window\./g, 'முக்கிய ருத்ர ஆரோக்கிய எச்சரிக்கை காலமாகும்.')
          .replace(/represents the primary longevity transition window\./g, 'முக்கிய ஆயுள் எச்சரிக்கை காலமாகும்.');
      case 'hi':
        return res
          .replace(/Age/g, 'आयु')
          .replace(/represents the primary Trishoola longevity transition window\./g, 'मुख्य त्रिशूल आयु संक्रमण काल है।')
          .replace(/represents a key Rudra health transition window\./g, 'मुख्य रुद्र स्वास्थ्य सतर्कता काल है।')
          .replace(/represents the primary longevity transition window\./g, 'मुख्य आयु संक्रमण काल है।');
      case 'te':
        return res
          .replace(/Age/g, 'వయస్సు')
          .replace(/represents the primary Trishoola longevity transition window\./g, 'ప్రధాన త్రిశూల ఆయుష్షు పరివర్తన కాలం.')
          .replace(/represents a key Rudra health transition window\./g, 'ప్రధాన రుద్ర ఆరోగ్య అప్రమత్త కాలం.')
          .replace(/represents the primary longevity transition window\./g, 'ప్రధాన ఆయుష్షు అప్రమత్త కాలం.');
      case 'kn':
        return res
          .replace(/Age/g, 'ವಯಸ್ಸು')
          .replace(/represents the primary Trishoola longevity transition window\./g, 'ಮುಖ್ಯ ತ್ರಿಶೂಲ ಆಯುಷ್ಯ ಪರಿವರ್ತನೆಯ ಕಾಲಾವಧಿ.')
          .replace(/represents a key Rudra health transition window\./g, 'ಮುಖ್ಯ ರುದ್ರ ಆರೋಗ್ಯ ಎಚ್ಚರಿಕೆಯ ಕಾಲಾವಧಿ.')
          .replace(/represents the primary longevity transition window\./g, 'ಮುಖ್ಯ ಆಯುಷ್ಯ ಎಚ್ಚರಿಕೆಯ ಕಾಲಾವಧಿ.');
      case 'ml':
        return res
          .replace(/Age/g, 'വയസ്സ്')
          .replace(/represents the primary Trishoola longevity transition window\./g, 'പ്രധാന ത്രിശൂല ആയുസ്സ് പരിവർത്തന കാലഘട്ടം.')
          .replace(/represents a key Rudra health transition window\./g, 'പ്രധാന രുദ്ര ആരോഗ്യ ജാഗ്രതാ കാലഘട്ടം.')
          .replace(/represents the primary longevity transition window\./g, 'പ്രധാന ആയുസ്സ് ജാഗ്രതാ കാലഘട്ടം.');
    }
    return res;
  };

  const translateMarakaWindow = (window) => {
    if (!window || language === 'en') return window;
    const p = /(\w+)\s+Mahadasa\s+-\s+(\w+)\s+Bhukthi\s*\(([^)]+)\)(.*)/i;
    const m = window.match(p);
    if (m) {
      const p1 = translatePlanet(m[1]);
      const p2 = translatePlanet(m[2]);
      let timeAndAge = m[3];

      switch (language) {
        case 'ta':
          timeAndAge = timeAndAge.replace(/~Age/g, '~வயது').replace(/to/g, 'முதல்').replace(/, ~வயது/g, ' வரை, ~வயது');
          return `${p1} மகாதிசை - ${p2} புக்தி (${timeAndAge}) முக்கிய மாரக/பாதக எச்சரிக்கைக் காலமாகும்.`;
        case 'hi':
          timeAndAge = timeAndAge.replace(/~Age/g, '~आयु').replace(/to/g, 'से');
          return `${p1} महादशा - ${p2} भुक्ति (${timeAndAge}) मुख्य मारक/बाधक सतर्कता काल है।`;
        case 'te':
          timeAndAge = timeAndAge.replace(/~Age/g, '~వయస్సు').replace(/to/g, 'నుండి');
          return `${p1} మహాదశ - ${p2} భుక్తి (${timeAndAge}) ప్రధాన మారక/బాధక అప్రమత్త కాలం.`;
        case 'kn':
          timeAndAge = timeAndAge.replace(/~Age/g, '~ವಯಸ್ಸು').replace(/to/g, 'ರಿಂದ');
          return `${p1} ಮಹಾದಶಾ - ${p2} ಭುಕ್ತಿ (${timeAndAge}) ಮುಖ್ಯ ಮಾರಕ/ಬಾಧಕ ಎಚ್ಚರಿಕೆಯ ಕಾಲಾವಧಿ.`;
        case 'ml':
          timeAndAge = timeAndAge.replace(/~Age/g, '~വയസ്സ്').replace(/to/g, 'മുതൽ');
          return `${p1} മഹാദശ - ${p2} ഭുക്തി (${timeAndAge}) പ്രധാന മാരക/ബാധക ജാഗ്രതാ കാലഘട്ടം.`;
      }
    }
    return window;
  };

  const translateRogaSthana = (sign, lord) => {
    if (!sign) return '';
    const translatedSign = translateRashi(sign);
    const translatedLord = translatePlanet(lord);
    const houseLabel = language === 'ta' ? '6-ஆம் பாவகம்' :
                       language === 'hi' ? '6ठा भाव' :
                       language === 'te' ? '6వ స్థానం' :
                       language === 'kn' ? '6ನೇ ಮನೆ' :
                       language === 'ml' ? '6-ാം ഭാവം' :
                       'House 6';
    return `${translatedSign} (${translatedLord}) - ${houseLabel}`;
  };

  const translateOrganVulnerability = (text) => {
    if (!text) return '';
    let localizedMain = null;
    for (const [key, map] of Object.entries(ORGAN_VULNERABILITIES_I18N)) {
      if (text.includes(key)) {
        localizedMain = map[language] || map['en'] || key;
        break;
      }
    }

    // Check for Dusthana occupant suffix e.g. "(Mars in House 12)" or "(Saturn in House 6)"
    const houseOccupantMatch = text.match(/\(([^)]+)\s+in\s+House\s+(\d+)\)/i);
    if (houseOccupantMatch) {
      const rawPlanet = houseOccupantMatch[1].trim();
      const houseNum = houseOccupantMatch[2].trim();
      const locPlanet = translatePlanet(rawPlanet) || rawPlanet;
      let suffix = '';
      if (language === 'ta') suffix = ` (${houseNum}-ஆம் வீட்டில் ${locPlanet})`;
      else if (language === 'hi') suffix = ` (${houseNum}वें भाव में ${locPlanet})`;
      else if (language === 'te') suffix = ` (${houseNum}వ స్థానంలో ${locPlanet})`;
      else if (language === 'kn') suffix = ` (${houseNum}ನೇ ಮನೆಯಲ್ಲಿ ${locPlanet})`;
      else if (language === 'ml') suffix = ` (${houseNum}-ാം ഭാവത്തിൽ ${locPlanet})`;
      else suffix = ` (${locPlanet} in House ${houseNum})`;

      if (localizedMain) {
        return `${localizedMain}${suffix}`;
      }
    }

    if (localizedMain) return localizedMain;

    if (text.includes('Longevity resilience & chronic vitality maintenance governed by 8th Lord')) {
      let lordMatch = text.replace(/Longevity resilience & chronic vitality maintenance governed by 8th Lord\s*/g, '').trim();
      const parts = lordMatch.split(/\s+in\s+/i);
      let planetPart = parts[0] ? translatePlanet(parts[0].trim()) : '';
      let signPart = parts[1] ? translateRashi(parts[1].trim()) : '';

      if (language === 'ta') return `8-ஆம் அதிபதி ${planetPart} ${signPart}-ல் உள்ள அமைப்பால் நீண்ட ஆயுள் மற்றும் நோய் எதிர்ப்பு ஆற்றல் பராமரிப்பு`;
      if (language === 'hi') return `अष्टमेश ${planetPart} ${signPart} राशि व्यवस्था द्वारा दीर्घायु एवं रोग प्रतिरोधक शक्ति का संतुलन`;
      if (language === 'te') return `8వ అధిపతి ${planetPart} ${signPart}లో ఉన్నందున దీర్ఘాయుష్షు మరియు రోగనిరోధక శక్తి నిర్వహణ`;
      if (language === 'kn') return `8ನೇ ಅಧಿಪತಿ ${planetPart} ${signPart}ದಲ್ಲಿರುವ ಪ್ರಭಾವದಿಂದ ದೀರ್ಘಾಯುಷ್ಯ ಮತ್ತು ರೋಗನಿರೋಧಕ ಶಕ್ತಿ ಸಂರಕ್ಷಣೆ`;
      if (language === 'ml') return `8-ാം അധിപൻ ${planetPart} ${signPart}-ൽ നിൽക്കുന്ന വഴിയുള്ള ദീർഘായുസ്സും പ്രതിരോധശേഷി സംരക്ഷണവും`;
      return `Longevity resilience & chronic vitality maintenance governed by 8th Lord ${planetPart} in ${signPart}`;
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
              {ayurdaya?.estimatedLifespanCeiling ? `~${ayurdaya.estimatedLifespanCeiling} ${t('yearsUnit', language)}` : `75 - 90+ ${t('yearsUnit', language)}`}
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '8px', padding: '12px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
              📜 {t('classicalRationale', language)}
            </div>
            <div style={{ fontSize: '13px', color: 'var(--text-primary)', lineHeight: '1.4' }}>
              {ayurdaya?.lifespanRange ? (
                language === 'ta' ? ayurdaya.lifespanRange.replace(/Years/g, 'வயது') :
                language === 'hi' ? ayurdaya.lifespanRange.replace(/Years/g, 'वर्ष') :
                language === 'te' ? ayurdaya.lifespanRange.replace(/Years/g, 'సంవత్సరాలు') :
                language === 'kn' ? ayurdaya.lifespanRange.replace(/Years/g, 'ವರ್ಷಗಳು') :
                language === 'ml' ? ayurdaya.lifespanRange.replace(/Years/g, 'വയസ്സ്') :
                ayurdaya.lifespanRange
              ) : 'Brihat Parashara & Jaimini Sutras'}
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
                {translateShoolaWindow(shoola.criticalShoolaWindow)}
              </div>
            </div>
          )}
        </div>

        {/* 3-Pair Modality Table */}
        {(ayurdaya?.jaiminiThreePairs || ayurdaya?.threePairsDetails) && Object.keys(ayurdaya.jaiminiThreePairs || ayurdaya.threePairsDetails).length > 0 && (
          <div style={{ marginBottom: '18px' }}>
            <h4 style={{ fontSize: '13px', color: 'var(--accent-gold)', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              📊 {t('threePairsTitle', language)}
            </h4>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '12px' }}>
              {Object.entries(ayurdaya.jaiminiThreePairs || ayurdaya.threePairsDetails)
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
                          {formatPairTitle(pairKey, ruleApplied)}
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
                {isSpecialRule ? t('visheshaOverrideBadge', language) : t('synthesisConsensusBadge', language)}
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
                  {t('baseSpanLabel', language)}
                </span>
                <strong style={{ fontSize: '13px', color: 'var(--text-primary)' }}>
                  {translateSpan(kakshya.baseSpan)} (~{kakshya.baseCeilingAge} {t('yearsUnit', language)})
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
                  {(kakshya.netYearsAdjustment || 0) >= 0 ? `+${kakshya.netYearsAdjustment || 0}` : kakshya.netYearsAdjustment} {t('yearsUnit', language)}
                </strong>
              </div>

              <div style={{ background: 'rgba(0,0,0,0.2)', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)' }}>
                <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block' }}>
                  {t('adjustedSpanLabel', language)}
                </span>
                <strong style={{ fontSize: '13px', color: 'var(--accent-gold)' }}>
                  {translateSpan(kakshya.adjustedSpan)} (~{kakshya.adjustedCeilingAge} {t('yearsUnit', language)})
                </strong>
              </div>

              {kakshya.atmakaraka && (
                <div style={{ background: 'rgba(0,0,0,0.2)', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border)' }}>
                  <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block' }}>
                    {t('atmakarakaAnchor', language)}
                  </span>
                  <strong style={{ fontSize: '13px', color: '#3498db' }}>
                    {translatePlanet(kakshya.atmakaraka)}
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
              {translateMarakaWindow(ayurdaya.criticalMarakaWindow)}
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
              {t('shoolaTimelineSubtitle', language)}
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
                  {translateStartingSignReason(shoola.startingSignReason)}
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
                  {translateShoolaWindow(shoola.criticalShoolaWindow)}
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
                          {period.startAge} - {period.endAge} {t('yearsUnit', language)}
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
                              📅 {translateRashi(period.signName)} (12 × 9M):
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
              📜 {translateOverrideReason(shoola.classicalRationale)}
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
