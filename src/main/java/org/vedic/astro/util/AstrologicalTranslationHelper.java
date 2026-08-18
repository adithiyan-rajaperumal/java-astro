package org.vedic.astro.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-performance 6-language astrological translation dictionary and helper
 * for PDF exports and backend localizations across Tamil (ta), Hindi (hi),
 * Telugu (te), Kannada (kn), Malayalam (ml), and English (en).
 */
public class AstrologicalTranslationHelper {

    private static final Map<String, Map<String, String>> DICT = new HashMap<>();

    static {
        // =========================================================================
        // 1. DEITIES
        // =========================================================================
        add("Lord Shiva / Lord Rama",
                "ஸ்ரீ சிவன் / ராமர்",
                "भगवान शिव / श्री राम",
                "శివుడు / శ్రీరాముడు",
                "ಶಿವ / ಶ್ರೀ ರಾಮ",
                "ശിവൻ / ശ്രീരാമൻ",
                "Lord Shiva / Lord Rama");

        add("Goddess Parvati / Goddess Gauri / Lord Krishna",
                "ஸ்ரீ பார்வதி / கௌரி / கிருஷ்ணர்",
                "माँ पार्वती / गौरी / श्री कृष्ण",
                "పార్వతీ దేవి / గౌరి / శ్రీకృష్ణుడు",
                "ಪಾರ್ವತಿ / ಗೌರಿ / ಶ್ರೀಕೃಷ್ಣ",
                "പാർവ്വതി / ഗൗരി / ശ്രീകൃഷ്ണൻ",
                "Goddess Parvati / Goddess Gauri / Lord Krishna");

        add("Goddess Parvati / Gauri / Krishna",
                "ஸ்ரீ பார்வதி / கௌரி / கிருஷ்ணர்",
                "माँ पार्वती / गौरी / श्री कृष्ण",
                "పార్వతీ దేవి / గౌరి / శ్రీకృష్ణుడు",
                "ಪಾರ್ವತಿ / ಗೌರಿ / ಶ್ರೀಕೃಷ್ಣ",
                "പാർവ്വതി / ഗൗരി / ശ്രീകൃഷ്ണൻ",
                "Goddess Parvati / Gauri / Krishna");

        add("Lord Murugan / Lord Narasimha / Kartikeya",
                "ஸ்ரீ முருகப்பெருமான் / நரசிம்மர் / சுப்பிரமணியர்",
                "भगवान मुरुगन / नृसिंह / कार्तिकेय",
                "సుబ్రహ్మణ్య స్వామి / నరసింహ స్వామి / మురుగన్",
                "ಸುಬ್ರಹ್ಮಣ್ಯ / ನರಸಿಂಹ / ಕಾರ್ತಿಕೇಯ",
                "മുരുകൻ / നരസിംഹം / കാർത്തികേയൻ",
                "Lord Murugan / Lord Narasimha / Kartikeya");

        add("Lord Muruga / Subramanya / Kartikeya",
                "ஸ்ரீ முருகப்பெருமான் / சுப்பிரமணியர்",
                "भगवान मुरुगन / नृसिंह / कार्तिकेय",
                "సుబ్రహ్మణ్య స్వామి / మురుగన్",
                "ಸುಬ್ರಹ್ಮಣ್ಯ / ಕಾರ್ತಿಕೇಯ / ಮುರುಗ",
                "മുരുകൻ / സുബ്രഹ്മണ്യൻ / കാർത്തികേയൻ",
                "Lord Muruga / Subramanya / Kartikeya");

        add("Lord Vishnu / Maha Vishnu / Narayana",
                "ஸ்ரீ மகாவிஷ்ணு / நாராயணன்",
                "भगवान विष्णु / महाविष्णु / नारायण",
                "మహావిష్ణువు / నారాయణుడు",
                "ಮಹಾವಿಷ್ಣು / ನಾರಾಯಣ",
                "മഹാവിഷ്ണു / നാരായണൻ",
                "Lord Vishnu / Maha Vishnu / Narayana");

        add("Lord Vishnu / Lord Venkateshwara",
                "ஸ்ரீ மகாவிஷ்ணு / வேங்கடாஜலபதி",
                "भगवान विष्णु / श्री वेंकटेश्वर",
                "శ్రీ వేంకటేశ్వర స్వామి / విష్ణువు",
                "ಶ್ರೀ ವೆಂಕಟೇಶ್ವರ / ವಿಷ್ಣು",
                "ശ്രീ വെങ്കടേശ്വരൻ / മഹാവിഷ്ണു",
                "Lord Vishnu / Lord Venkateshwara");

        add("Lord Ganesha / Ganapati",
                "ஸ்ரீ மகா கணபதி / விநாயகர்",
                "भगवान गणेश / गणपति",
                "వినాయకుడు / గణపతి",
                "ಗಣೇಶ / ಗಣಪತಿ",
                "മഹാ ഗണപതി / വിനായകൻ",
                "Lord Ganesha / Ganapati");

        add("Lord Ganesha",
                "ஸ்ரீ விநாயகர்",
                "भगवान गणेश",
                "వినాయకుడు",
                "ಗಣೇಶ",
                "ഗണപതി",
                "Lord Ganesha");

        add("Goddess Mahalakshmi / Goddess Lakshmi",
                "ஸ்ரீ மகாலட்சுமி தாயார்",
                "माँ महालक्ष्मी",
                "మహాలక్ష్మి దేవి",
                "ಮಹಾಲಕ್ಷ್ಮಿ",
                "മഹാലക്ഷ്മി",
                "Goddess Mahalakshmi");

        add("Lord Hanuman / Lord Bhairava / Lord Rudra",
                "ஸ்ரீ ஆஞ்சநேயர் / கால பைரவர் / ருத்ரன்",
                "भगवान हनुमान / काल भैरव / रुद्र",
                "హనుమంతుడు / కాలభైరవుడు / రుద్రుడు",
                "ಹನುಮಂತ / ಕಾಲಭೈರವ / ರುದ್ರ",
                "ഹനുമാൻ / കാലഭൈരവൻ / രുദ്രൻ",
                "Lord Hanuman / Lord Bhairava / Lord Rudra");

        add("Goddess Durga / Chamundeshwari",
                "ஸ்ரீ துர்க்கை அம்மன் / சாமுண்டீஸ்வரி",
                "माँ दुर्गा / चामुंडेश्वरी",
                "దుర్గా దేవి / చాముండేశ్వరి",
                "ದುರ್ಗಾ ದೇವಿ / ಚಾಮುಂಡೇಶ್ವರಿ",
                "ദുർഗ്ഗാ ദേവി / ചാമുണ്ഡേശ്വരി",
                "Goddess Durga / Chamundeshwari");

        // =========================================================================
        // 2. GEMSTONES
        // =========================================================================
        add("Ruby (Manickam)", "மாணிக்கம் (Ruby)", "माणिक्य (Ruby)", "కెంపు (Ruby)", "ಮಾಣಿಕ್ಯ (Ruby)", "മാണിക്യം (Ruby)", "Ruby (Manickam)");
        add("Pearl (Muthu)", "முத்து (Pearl)", "मोती (Pearl)", "ముత్యం (Pearl)", "ಮುತ್ತು (Pearl)", "മുത്ത് (Pearl)", "Pearl (Muthu)");
        add("Red Coral (Pavalam)", "பவளம் (Red Coral)", "मूंगा (Red Coral)", "పగడము (Red Coral)", "ಹವಳ (Red Coral)", "പവിഴം (Red Coral)", "Red Coral (Pavalam)");
        add("Emerald (Maragatham)", "மரகதம் (Emerald)", "पन्ना (Emerald)", "మరకతం (Emerald)", "ಪಚ್ಚೆ (Emerald)", "മരതകം (Emerald)", "Emerald (Maragatham)");
        add("Yellow Sapphire (Pushparagam)", "புஷ்பராகம் (Yellow Sapphire)", "पुखराज (Yellow Sapphire)", "పుష్యరాగం (Yellow Sapphire)", "ಪುಷ್ಯರಾಗ (Yellow Sapphire)", "പുഷ്യരാഗം (Yellow Sapphire)", "Yellow Sapphire (Pushparagam)");
        add("Diamond (Vairam)", "வைரம் (Diamond)", "हीरा (Diamond)", "వజ్రం (Diamond)", "ವಜ್ರ (Diamond)", "വൈരം (Diamond)", "Diamond (Vairam)");
        add("Blue Sapphire (Neelam)", "நீலம் (Blue Sapphire)", "नीलम (Blue Sapphire)", "నీలం (Blue Sapphire)", "ನೀಲಂ (Blue Sapphire)", "നീലക്കല്ല് (Blue Sapphire)", "Blue Sapphire (Neelam)");
        add("Hessonite / Gomed (Gomedhakam)", "கோமேதகம் (Hessonite)", "गोमेद (Hessonite)", "గోమేధికం (Hessonite)", "ಗೋಮೇಧಿಕ (Hessonite)", "ഗോമേദകം (Hessonite)", "Hessonite / Gomed");
        add("Cat's Eye (Vaidooryam)", "வைடூரியம் (Cat's Eye)", "लहसुनिया (Cat's Eye)", "వైడూర్యం (Cat's Eye)", "ವೈಡೂರ್ಯ (Cat's Eye)", "വൈഡൂര്യം (Cat's Eye)", "Cat's Eye (Vaidooryam)");

        // =========================================================================
        // 3. METALS & FINGERS
        // =========================================================================
        add("Gold", "தங்கம் (Gold)", "स्वर्ण / सोना (Gold)", "బంగారం (Gold)", "ಚಿನ್ನ (Gold)", "സ്വർണ്ണം (Gold)", "Gold");
        add("Silver", "வெள்ளி (Silver)", "चांदी (Silver)", "వెండి (Silver)", "ಬೆಳ್ಳಿ (Silver)", "വെള്ളി (Silver)", "Silver");
        add("Copper", "செம்பு (Copper)", "तांबा (Copper)", "రాగి (Copper)", "ತಾಮ್ರ (Copper)", "ചെമ്പ് (Copper)", "Copper");
        add("Panchadhatu", "ஐம்பொன் (Panchadhatu)", "पंचधातु (Panchadhatu)", "పంచలోహం (Panchadhatu)", "ಪಂಚಲೋಹ (Panchadhatu)", "പഞ്ചലോഹം (Panchadhatu)", "Panchadhatu");
        add("Iron / Lead", "இரும்பு / ஈயம்", "लोहा / सीसा", "ఇనుము / సీసం", "ಕಬ್ಬಿಣ / ಸೀಸ", "ഇരുമ്പ് / ഈയം", "Iron / Lead");

        add("Ring Finger", "மோதிர விரல்", "अनामिका (Ring Finger)", "ఉంగరపు వేలు", "ಉಂಗುರದ ಬೆರಳು", "മോതിരവിരൽ", "Ring Finger");
        add("Index Finger", "ஆள்காட்டி விரல்", "तर्जनी (Index Finger)", "చూపుడు వేలు", "ತೋರುಬೆರಳು", "ചೂണ്ടുവിരൽ", "Index Finger");
        add("Little Finger", "சுண்டு விரல்", "कनिष्ठिका (Little Finger)", "చిటికెన వేలు", "ಕಿರುಬೆರಳು", "ചെറുവിരൽ", "Little Finger");
        add("Middle Finger", "நடு விரல்", "मध्यमा (Middle Finger)", "మధ్య వేలు", "ಮಧ್ಯದ ಬೆರಳು", "നടുവിരൽ", "Middle Finger");

        // =========================================================================
        // 4. DIRECTIONS
        // =========================================================================
        add("East", "கிழக்கு", "पूर्व (East)", "తూర్పు", "ಪೂರ್ವ", "കിഴക്ക്", "East");
        add("West", "மேற்கு", "पश्चिम (West)", "పడమర", "ಪಶ್ಚಿಮ", "പടിഞ്ഞാറ്", "West");
        add("North", "வடக்கு", "उत्तर (North)", "ఉత్తరం", "ಉತ್ತರ", "വടക്ക്", "North");
        add("South", "தெற்கு", "दक्षिण (South)", "దక్షిణం", "ದಕ್ಷಿಣ", "തെക്ക്", "South");
        add("North-East (Ishanya)", "வடகிழக்கு (ஈசான்யம்)", "ईशान कोण (उत्तर-पूर्व)", "ఈశాన్యం (ఉత్తర-తూర్పు)", "ಈಶಾನ್ಯ (ಉತ್ತರ-ಪೂರ್ವ)", "വടക്കുകിഴക്ക് (ഈശാനകോൺ)", "North-East (Ishanya)");
        add("South-East (Agni)", "தென்கிழக்கு (அக்னி)", "आग्नेय कोण (दक्षिण-पूर्व)", "ఆగ్నేయం (దక్షిణ-తూర్పు)", "ಆಗ್ನೇಯ (ದಕ್ಷಿಣ-ಪೂರ್ವ)", "തെക്കുകിഴക്ക് (ആഗ്നേയകോൺ)", "South-East (Agni)");
        add("South-West (Niruthi)", "தென்மேற்கு (நிருதி)", "नैऋत्य कोण (दक्षिण-पश्चिम)", "నైరుతి (దక్షిణ-పడమర)", "ನೈಋತ್ಯ (ದಕ್ಷಿಣ-ಪಶ್ಚಿಮ)", "തെക്കുപടിഞ്ഞാറ് (നിര്യതികോൺ)", "South-West (Niruthi)");
        add("North-West (Vayu)", "வடமேற்கு (வாயு)", "वायव्य कोण (उत्तर-पश्चिम)", "వాయవ్యం (ఉత్తర-పడమర)", "ವಾಯವ್ಯ (ಉತ್ತರ-ಪಶ್ಚಿಮ)", "വടക്കുപടിഞ്ഞാറ് (വായുകോൺ)", "North-West (Vayu)");

        // =========================================================================
        // 5. PLANETS
        // =========================================================================
        add("Sun", "சூரியன்", "सूर्य", "సూర్యుడు", "ಸೂರ್ಯ", "സൂര്യൻ", "Sun");
        add("Moon", "சந்திரன்", "चन्द्र", "చంద్రుడు", "ಚಂದ್ರ", "ചന്ദ്രൻ", "Moon");
        add("Mars", "செவ்வாய்", "मंगल", "కుజుడు", "ಮಂಗಳ", "ചൊവ്വ", "Mars");
        add("Mercury", "புதன்", "बुध", "బుధుడు", "ಬುಧ", "ಬುಧನ್", "Mercury");
        add("Jupiter", "குரு", "बृहस्पति / गुरु", "గురుడు", "ಗುರು", "വ്യാഴം / ഗുരു", "Jupiter");
        add("Venus", "சுக்கிரன்", "शुक्र", "శుక్రుడు", "ಶುಕ್ರ", "ശുക്രൻ", "Venus");
        add("Saturn", "சனி", "शनि", "శని", "ಶನಿ", "ശനി", "Saturn");
        add("Rahu", "ராகு", "राहु", "రాహువు", "ರಾಹು", "രാഹു", "Rahu");
        add("Ketu", "கேது", "केतु", "కేతువు", "ಕೇತು", "കേതു", "Ketu");

        // =========================================================================
        // 6. DAYS OF WEEK
        // =========================================================================
        add("Sunday", "ஞாயிறு", "रविवार", "ఆదివారం", "ಭಾನುವಾರ", "ഞായറാഴ്ച", "Sunday");
        add("Monday", "திங்கள்", "सोमवार", "సోమవారం", "ಸೋಮವಾರ", "തിങ്കളാഴ്ച", "Monday");
        add("Tuesday", "செவ்வாய்", "मंगलवार", "మంగళవారం", "ಮಂಗಳವಾರ", "ചൊവ്വാഴ്ച", "Tuesday");
        add("Wednesday", "புதன்", "बुधवार", "బుధవారం", "ಬುಧವಾರ", "ബുധനാഴ്ച", "Wednesday");
        add("Thursday", "வியாழன்", "गुरुवार", "గురువారం", "ಗುರುವಾರ", "വ്യാഴാഴ്ച", "Thursday");
        add("Friday", "வெள்ளி", "शुक्रवार", "శుక్రవారం", "ಶುಕ್ರವಾರ", "വെള്ളിയാഴ്ച", "Friday");
        add("Saturday", "சனி", "शनिवार", "శనివారం", "ಶನಿವಾರ", "ശനിയാഴ്ച", "Saturday");

        // =========================================================================
        // 7. LONGEVITY CLASSIFICATIONS
        // =========================================================================
        add("Poornayu", "பூர்ணாயுள் (Poornayu: 75+ ஆண்டுகள்)", "पूर्णायु (75+ वर्ष)", "పూర్ణాయుష్షు (75+ సంవత్సరాలు)", "ಪೂರ್ಣಾಯುಷ್ಯ (75+ ವರ್ಷಗಳು)", "പൂർണ്ണായുസ്സ് (75+ വർഷങ്ങൾ)", "Poornayu (Full Longevity: 75+ Yrs)");
        add("Madhyayu", "மத்தியாயுள் (Madhyayu: 36–75 ஆண்டுகள்)", "मध्‍यायु (36–75 वर्ष)", "మధ్యాయుష్షు (36–75 సంవత్సరాలు)", "ಮಧ್ಯಾಯುಷ್ಯ (36–75 ವರ್ಷಗಳು)", "മദ്ധ്യായുസ്സ് (36–75 വർഷങ്ങൾ)", "Madhyayu (Medium Longevity: 36–75 Yrs)");
        add("Alpayu", "அல்பாயுள் (Alpayu: 0–35 ஆண்டுகள்)", "अल्पायु (0–35 वर्ष)", "అల్పాయుష్షు (0–35 సంవత్సరాలు)", "ಅಲ್ಪಾಯುಷ್ಯ (0–35 ವರ್ಷಗಳು)", "അല്പായുസ്സ് (0–35 വർഷങ്ങൾ)", "Alpayu (Short Longevity: 0–35 Yrs)");

        // =========================================================================
        // 8. AYURVEDIC PRAKRITI & DOSHAS
        // =========================================================================
        add("Vata Dominant", "வாதம் பிரதானம் (Vata Dominant)", "वात प्रधान (Vata Dominant)", "వాత ప్రధానం (Vata Dominant)", "ವಾತ ಪ್ರಧಾನ (Vata Dominant)", "വാതം പ്രധാനം (Vata Dominant)", "Vata Dominant");
        add("Pitta Dominant", "பித்தம் பிரதானம் (Pitta Dominant)", "पित्त प्रधान (Pitta Dominant)", "పిత్త ప్రధానం (Pitta Dominant)", "ಪಿತ್ತ ಪ್ರಧಾನ (Pitta Dominant)", "പിത്തം പ്രധാനം (Pitta Dominant)", "Pitta Dominant");
        add("Kapha Dominant", "கபம் பிரதானம் (Kapha Dominant)", "कफ प्रधान (Kapha Dominant)", "కఫ ప్రధానం (Kapha Dominant)", "ಕಫ ಪ್ರಧಾನ (Kapha Dominant)", "കഫം പ്രധാനം (Kapha Dominant)", "Kapha Dominant");
        add("Vata-Pitta", "வாத-பித்த பிரகிருதி (Vata-Pitta)", "वात-पित्त प्रकृति (Vata-Pitta)", "వాత-పిత్త ప్రకృతి (Vata-Pitta)", "ವಾತ-ಪಿತ್ತ ಪ್ರಕೃತಿ (Vata-Pitta)", "വാത-പിത്ത പ്രകൃതി (Vata-Pitta)", "Vata-Pitta");
        add("Pitta-Kapha", "பித்த-கப பிரகிருதி (Pitta-Kapha)", "पित्त-कफ प्रकृति (Pitta-Kapha)", "పిత్త-కఫ ప్రకృతి (Pitta-Kapha)", "ಪಿತ್ತ-ಕಫ ಪ್ರಕೃತಿ (Pitta-Kapha)", "പിത്ത-കഫ പ്രകൃതി (Pitta-Kapha)", "Pitta-Kapha");
        add("Vata-Kapha", "வாத-கப பிரகிருதி (Vata-Kapha)", "वात-कफ प्रकृति (Vata-Kapha)", "వాత-కఫ ప్రకృతి (Vata-Kapha)", "ವಾತ-ಕಫ ಪ್ರಕೃತಿ (Vata-Kapha)", "വാത-കഫ പ്രകൃതി (Vata-Kapha)", "Vata-Kapha");
        add("Sama Prakriti", "சம பிரகிருதி (Tridosha Balanced)", "सम प्रकृति (संतुलित)", "సమ ప్రకృతి (సమతుల్యం)", "ಸಮ ಪ್ರಕೃತಿ (ಸಮತೋಲಿತ)", "സമ പ്രകൃതി (സന്തുലിതം)", "Sama Prakriti (Balanced)");

        // =========================================================================
        // 9. AGNI & METABOLISM
        // =========================================================================
        add("Vishamagni (Irregular & Fluctuating Metabolism)",
                "விஷமாக்னி (மாறிமாறி இயங்கும் செரிமானம்)",
                "विषमाग्नि (अनियमित पाचन अग्नि)",
                "విషమాగ్ని (అస్థిర జీర్ణశక్తి)",
                "ವಿಷಮಾಗ್ನಿ (ಅಸ್ಥಿರ ಜೀರ್ಣಶಕ್ತಿ)",
                "വിഷമാഗ്നി (മാറിമറിയുന്ന ദഹനം)",
                "Vishamagni (Irregular & Fluctuating Metabolism)");

        add("Tikshnagni (Intense & Hyper-Metabolic Fire)",
                "தீக்ஷ்ணாக்னி (அதிவேக செரிமானம் / உஷ்ணம்)",
                "तीक्ष्णाग्नि (अति तीव्र पाचन अग्नि)",
                "తీక్ష్ణాగ్ని (తీవ్ర జీర్ణశక్తి)",
                "ತೀಕ್ಷ್ಣಾಗ್ನಿ (ತೀವ್ರ ಜೀರ್ಣಶಕ್ತಿ)",
                "തീക്ഷ്ണാഗ്നി (അതിവേഗ ദഹനം)",
                "Tikshnagni (Intense & Hyper-Metabolic Fire)");

        add("Mandagni (Sluggish & Slow Metabolic Agni)",
                "மந்தாக்னி (மந்தமான செரிமானம்)",
                "मंदाग्नि (धीमी पाचन अग्नि)",
                "మందాగ్ని (మందగించిన జీర్ణశక్తి)",
                "ಮಂದಾಗ್ನಿ (ನಿಧಾನ ಜೀರ್ಣಶಕ್ತಿ)",
                "മന്ദാഗ്നി (മന്ദഗതിയിലുള്ള ദഹനം)",
                "Mandagni (Sluggish & Slow Metabolic Agni)");

        add("Samagni (Balanced & Optimal Digestive Fire)",
                "சமாக்னி (சீரான மற்றும் உத்தம செரிமானம்)",
                "समाग्नि (संतुलित पाचन अग्नि)",
                "సమాగ్ని (సమతుల్య జీర్ణశక్తి)",
                "ಸಮಾಗ್ನಿ (ಸಮತೋಲಿತ ಜೀರ್ಣಶಕ್ತಿ)",
                "സമാഗ്നി (സന്തുലിത ദഹനം)",
                "Samagni (Balanced & Optimal Digestive Fire)");

        // =========================================================================
        // 10. BODY BUILD & DHATUS
        // =========================================================================
        add("Krisha Deha (Slender & Angular)",
                "கிருஷ தேகம் (ஒல்லியான / நடுத்தர கட்டமைப்பு)",
                "कृश देह (दुबला-पतला)",
                "కృశ దేహం (సన్నని శరీరం)",
                "ಕೃಶ ದೇಹ (ತೆಳ್ಳನೆಯ ಶರೀರ)",
                "കൃശ ദേഹം (മെലിഞ്ഞ ശരീരം)",
                "Krisha Deha (Slender & Angular)");

        add("Madhya Deha (Medium & Athletic)",
                "மத்திய தேகம் (சீரான உடலமைப்பு)",
                "मध्य देह (मध्यम व सुडौल)",
                "మధ్య దేహం (మధ్యస్థ సుదృఢ శరీరం)",
                "ಮಧ್ಯ ದೇಹ (ಮಧ್ಯಮ ಗಾತ್ರ)",
                "മദ്ധ്യ ദേഹം (മിതമായ ഘടന)",
                "Madhya Deha (Medium & Athletic)");

        add("Sthula Deha (Broad & Robust)",
                "ஸ்தூல தேகம் (திடகாத்திரமான / அகன்ற உடலமைப்பு)",
                "स्थूल देह (मजबूत व चौड़ा)",
                "స్థూల దేహం (దృఢమైన శరీరం)",
                "ಸ್ಥೂಲ ದೇಹ (ದೃಢವಾದ ಕಾಯ)",
                "സ്ഥൂല ദേഹം (ബലിഷ്ഠമായ ശരീരം)",
                "Sthula Deha (Broad & Robust)");

        add("Asthi & Majja Dhatu (Bones & Bone Marrow)",
                "அஸ்தி & மஜ்ஜா தாது (எலும்பு மற்றும் மஜ்ஜை)",
                "अस्थि व मज्जा धातु (हड्डियां व मज्जा)",
                "అస్థి & మజ్జా ధాతువు (ఎముకలు మరియు మజ్జ)",
                "ಅಸ್ಥಿ ಮತ್ತು ಮಜ್ಜಾ ಧಾತು (ಮೂಳೆಗಳು)",
                "അസ്ഥി & മജ്ജാ ധാതു (എല്ലുകൾ)",
                "Asthi & Majja Dhatu (Bones & Bone Marrow)");

        add("Rasa & Rakta Dhatu (Plasma & Blood)",
                "ரஸ & ரத்த தாது (இரத்தம் மற்றும் நிணநீர்)",
                "रस व रक्त धातु (प्लाज्मा व रक्त)",
                "రస & రక్త ధాతువు (రక్తము మరియు రసము)",
                "ರಸ ಮತ್ತು ರಕ್ತ ಧಾತು (ರಕ್ತ)",
                "രസ & രക്ത ധാതു (രക്തം)",
                "Rasa & Rakta Dhatu (Plasma & Blood)");

        add("Meda & Shukra Dhatu (Adipose & Vital Essence)",
                "மேதஸ் & சுக்ர தாது (கொழுப்பு மற்றும் சுக்கிலம்)",
                "मेद व शुक्र धातु (वसा व ओज)",
                "మేదస్సు & శుక్ర ధాతువు",
                "ಮೇದಸ್ಸು ಮತ್ತು ಶುಕ್ರ ಧಾತು",
                "മേദസ്സും ശുക്ര ധാതുവും",
                "Meda & Shukra Dhatu (Adipose & Vital Essence)");

        // =========================================================================
        // 11. ELEMENTS
        // =========================================================================
        add("Agni (Fire)", "அக்னி தத்துவம் (நெருப்பு)", "अग्नि तत्व (अग्नि)", "అగ్ని తత్వం (నిప్పు)", "ಅಗ್ನಿ ತತ್ವ (ಬೆಂಕಿ)", "അഗ്നി തത്വം (തീ)", "Agni (Fire)");
        add("Prithvi (Earth)", "பிரித்வி தத்துவம் (நிலம்)", "पृथ्वी तत्व (भूमि)", "పృథ్వీ తత్వం (భూమి)", "ಪೃಥ್ವಿ ತತ್ವ (ಭೂಮಿ)", "പൃഥ്വി തത്വം (ഭൂമി)", "Prithvi (Earth)");
        add("Vayu (Air)", "வாயு தத்துவம் (காற்று)", "वायु तत्व (पवन)", "వాయు తత్వం (గాలి)", "ವಾಯು ತತ್ವ (ಗಾಳಿ)", "വായു തത്വം (കാറ്റ്)", "Vayu (Air)");
        add("Jala (Water)", "ஜல தத்துவம் (நீர்)", "जल तत्व (जल)", "జల తత్వం (నీరు)", "ಜಲ ತತ್ವ (ನೀರು)", "ജല തത്വം (വെള്ളം)", "Jala (Water)");
    }

    private static void add(String key, String ta, String hi, String te, String kn, String ml, String en) {
        Map<String, String> m = new HashMap<>();
        m.put("ta", ta);
        m.put("hi", hi);
        m.put("te", te);
        m.put("kn", kn);
        m.put("ml", ml);
        m.put("en", en);
        DICT.put(key.toLowerCase().trim(), m);
    }

    public static String translate(String key, String lang) {
        if (key == null || key.isBlank()) return "";
        String effectiveLang = (lang != null && !lang.isBlank()) ? lang.toLowerCase() : "en";
        Map<String, String> m = DICT.get(key.toLowerCase().trim());
        if (m != null && m.containsKey(effectiveLang)) {
            return m.get(effectiveLang);
        }
        return key;
    }

    public static String translateDeity(String deity, String lang) {
        if (deity == null || deity.isBlank()) return "";
        String trans = translate(deity, lang);
        if (!trans.equals(deity)) return trans;

        String lower = deity.toLowerCase();
        if (lower.contains("shiva") || lower.contains("rama")) return translate("Lord Shiva / Lord Rama", lang);
        if (lower.contains("parvati") || lower.contains("gauri") || lower.contains("krishna")) return translate("Goddess Parvati / Goddess Gauri / Lord Krishna", lang);
        if (lower.contains("muruga") || lower.contains("kartikeya") || lower.contains("subramanya")) return translate("Lord Muruga / Subramanya / Kartikeya", lang);
        if (lower.contains("vishnu") || lower.contains("narayana") || lower.contains("venkateshwara")) return translate("Lord Vishnu / Maha Vishnu / Narayana", lang);
        if (lower.contains("ganesha") || lower.contains("ganapati")) return translate("Lord Ganesha / Ganapati", lang);
        if (lower.contains("lakshmi")) return translate("Goddess Mahalakshmi / Goddess Lakshmi", lang);
        if (lower.contains("hanuman") || lower.contains("bhairava")) return translate("Lord Hanuman / Lord Bhairava / Lord Rudra", lang);
        if (lower.contains("durga")) return translate("Goddess Durga / Chamundeshwari", lang);

        return deity;
    }

    public static String translateKulaDevata(String remedy, String lang) {
        if (remedy == null || remedy.isBlank()) return "";
        String lower = remedy.toLowerCase();
        if (lower.contains("ancestral temple") || lower.contains("amavasya") || lower.contains("abhishekam")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "அமாவாசை அல்லது செவ்வாய்க்கிழமைகளில் குலதெய்வக் கோயிலுக்குச் சென்று அபிஷேகம் மற்றும் ஆராதனை செய்க.";
                case "hi" -> "अमावस्या या मंगलवार को कुलदेवता मंदिर जाएं और अभिषेक करें।";
                case "te" -> "అమావాస్య లేదా మంగళవారం కులదైవ ఆలయాన్ని దర్శించి అభిషేకం చేయండి.";
                case "kn" -> "ಅಮಾವಾಸ್ಯೆ ಅಥವಾ ಮಂಗಳವಾರ ಕುಲದೇವರ ದೇವಸ್ಥಾನಕ್ಕೆ ಭೇಟಿ ನೀಡಿ ಅಭಿಷೇಕ ಮಾಡಿ.";
                case "ml" -> "അമാവാസി അല്ലെങ്കിൽ ചൊവ്വാഴ്ച കുലദേവതാ ക്ഷേത്രത്തിൽ ദർശനം നടത്തി അഭിഷേകം ചെയ്യുക.";
                default -> remedy;
            };
        }
        if (lower.contains("ghee lamp") || lower.contains("friday")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "ஒவ்வொரு செவ்வாய் மற்றும் வெள்ளிக்கிழமைகளில் வீட்டில் நெய் தீபம் ஏற்றி குலதெய்வத்தை வழிபடுக.";
                case "hi" -> "प्रत्येक शुक्रवार / मंगलवार को घर पर घी का दीपक जलाकर कुलदेवता की पूजा करें।";
                case "te" -> "ప్రతి శుక్రవారం / మంగళవారం ఇంట్లో నేతి దీపం వెలిగించి కులదైవాన్ని పూజించండి.";
                case "kn" -> "ಪ್ರತಿ ಶುಕ್ರವಾರ / ಮಂಗಳವಾರ ಮನೆಯಲ್ಲಿ ತುಪ್ಪದ ದೀಪ ಬೆಳಗಿಸಿ ಕುಲದೇವರನ್ನು ಆರಾಧಿಸಿ.";
                case "ml" -> "എല്ലാ വെള്ളിയാഴ്ചയും ചൊവ്വാഴ്ചയും വീട്ടിൽ നെയ്യ് വിളക്ക് തെളിയിച്ച് കുലദേവതയെ പ്രാർത്ഥിക്കുക.";
                default -> remedy;
            };
        }
        return remedy;
    }

    public static String translateGemstone(String gem, String lang) {
        if (gem == null || gem.isBlank()) return "";
        String trans = translate(gem, lang);
        if (!trans.equals(gem)) return trans;

        String lower = gem.toLowerCase();
        if (lower.contains("ruby") || lower.contains("manickam")) return translate("Ruby (Manickam)", lang);
        if (lower.contains("pearl") || lower.contains("muthu")) return translate("Pearl (Muthu)", lang);
        if (lower.contains("coral") || lower.contains("pavalam")) return translate("Red Coral (Pavalam)", lang);
        if (lower.contains("emerald") || lower.contains("maragatham")) return translate("Emerald (Maragatham)", lang);
        if (lower.contains("yellow sapphire") || lower.contains("pushparagam") || lower.contains("pukhraj")) return translate("Yellow Sapphire (Pushparagam)", lang);
        if (lower.contains("diamond") || lower.contains("vairam") || lower.contains("heera")) return translate("Diamond (Vairam)", lang);
        if (lower.contains("blue sapphire") || lower.contains("neelam")) return translate("Blue Sapphire (Neelam)", lang);
        if (lower.contains("hessonite") || lower.contains("gomed")) return translate("Hessonite / Gomed (Gomedhakam)", lang);
        if (lower.contains("cat's eye") || lower.contains("vaidooryam") || lower.contains("lahsuniya")) return translate("Cat's Eye (Vaidooryam)", lang);

        return gem;
    }

    public static String translateTiming(String timing, String lang) {
        if (timing == null || timing.isBlank()) return "";
        String lower = timing.toLowerCase();
        String day = "";
        for (String d : new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}) {
            if (lower.contains(d.toLowerCase())) {
                day = translate(d, lang);
                break;
            }
        }
        String shuklaStr = switch (lang.toLowerCase()) {
            case "ta" -> "சூரிய உதயம் (சுக்ல பக்ஷம்)";
            case "hi" -> "सूर्योदय (शुक्ल पक्ष)";
            case "te" -> "సూర్యోదయం (శుక్ల పక్షం)";
            case "kn" -> "ಸೂರ್ಯೋದಯ (ಶುಕ್ಲ ಪಕ್ಷ)";
            case "ml" -> "സൂര്യോദയം (ശുക്ല പക്ഷം)";
            default -> "Sunrise (Shukla Paksha)";
        };
        if (!day.isEmpty()) {
            return day + " " + shuklaStr;
        }
        return timing;
    }

    public static String translateDayName(String dayName, String lang) {
        if (dayName == null || dayName.isBlank()) return "";
        return translate(dayName, lang);
    }

    public static String translateAuspiciousActivities(String act, String lang) {
        if (act == null || act.isBlank()) return "";
        String lower = act.toLowerCase();
        if (lower.contains("spiritual rituals") || lower.contains("leadership") || lower.contains("government")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "ஆன்மீக வழிபாடுகள், தலைமைப் பொறுப்புகள், அரசு மற்றும் நிர்வாக முடிவுகள்";
                case "hi" -> "आध्यात्मिक अनुष्ठान, प्रशासनिक निर्णय, नेतृत्व, सरकारी कार्य";
                case "te" -> "ఆధ్యాత్మిక పూజలు, నాయకత్వ బాధ్యతలు, ప్రభుత్వ మరియు పరిపాలన పనులు";
                case "kn" -> "ಆಧ್ಯಾತ್ಮಿಕ ಪೂಜೆಗಳು, ನಾಯಕತ್ವ ನಿರ್ಧಾರಗಳು, ಸರ್ಕಾರಿ ಕೆಲಸಗಳು";
                case "ml" -> "ആത്മീയ അനുഷ്ഠാനങ്ങൾ, നേതൃത്വ തീരുമാനങ്ങൾ, സർക്കാർ കാര്യങ്ങൾ";
                default -> act;
            };
        }
        if (lower.contains("public relations") || lower.contains("travel") || lower.contains("creative")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "பொதுத் தொடர்பு, பயணங்கள், கலை ஆக்கங்கள், குடும்ப நிகழ்வுகள்";
                case "hi" -> "जनसंपर्क, यात्रा, रचनात्मक कार्य, पारिवारिक मेलजोल";
                case "te" -> "ప్రజా సంబంధాలు, ప్రయాణాలు, సృజనాత్మక పనులు, కుటుంబ కార్యక్రమాలు";
                case "kn" -> "ಸಾರ್ವಜನಿಕ ಸಂಪರ್ಕ, ಪ್ರಯಾಣ, ಸೃಜನಶೀಲ ಕೆಲಸ, ಕುಟುಂಬ ಕೂಟಗಳು";
                case "ml" -> "പൊതുജന സമ്പർക്കം, യാത്ര, സർഗ്ഗാത്മക പ്രവർത്തനങ്ങൾ, കുടുംബ സംഗമങ്ങൾ";
                default -> act;
            };
        }
        if (lower.contains("physical enterprise") || lower.contains("real estate") || lower.contains("competitive")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "நிலம்/வீடு பரிவர்த்தனைகள், துணிச்சலான முயற்சிகள், தொழில்நுட்பப் பணிகள்";
                case "hi" -> "भूमि-भवन सौदे, साहसिक प्रयास, तकनीकी व नेतृत्व संबंधी कार्य";
                case "te" -> "రియల్ ఎస్టేట్ లావాదేవీలు, సాహసోపేత ప్రయత్నాలు, సాంకేతిక పనులు";
                case "kn" -> "ರಿಯಲ್ ಎಸ್ಟೇಟ್ ವ್ಯವಹಾರಗಳು, ಸಾಹಸ ಕಾರ್ಯಗಳು, ತಾಂತ್ರಿಕ ಕೆಲಸಗಳು";
                case "ml" -> "ഭൂമി ഇടപാടുകൾ, സാഹസിക സംരംഭങ്ങൾ, സാങ്കേതിക പ്രവർത്തനങ്ങൾ";
                default -> act;
            };
        }
        if (lower.contains("business") || lower.contains("communication") || lower.contains("education")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "வணிக ஒப்பந்தங்கள், தகவல் தொடர்பு, கணக்கியல், கல்வி தொடக்கம்";
                case "hi" -> "व्यापारिक समझौते, संचार, लेखा, शिक्षा का शुभारंभ";
                case "te" -> "వ్యాపార ఒప్పందాలు, సమాచార మార్పిడి, గణాంకాలు, విద్యా సంబంధ పనులు";
                case "kn" -> "ವ್ಯಾಪಾರ ಒಪ್ಪಂದಗಳು, ಸಂವಹನ, ಲೆಕ್ಕಪತ್ರ, ವಿದ್ಯಾಭ್ಯಾಸ";
                case "ml" -> "വ്യാപാര കരാറുകൾ, ആശയവിനിമയം, അക്കൗണ്ടിംഗ്, വിദ്യാഭ്യാസം";
                default -> act;
            };
        }
        if (lower.contains("higher education") || lower.contains("financial") || lower.contains("marriage")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "உயர் கல்வி, நிதி முதலீடுகள், ஆன்மீக தீட்சை, திருமணப் பேச்சுவார்த்தை";
                case "hi" -> "उच्च शिक्षा, वित्तीय निवेश, आध्यात्मिक दीक्षा, विवाह चर्चाएं";
                case "te" -> "ఉన్నత విద్య, ఆర్థిక పెట్టుబడులు, ఆధ్యాత్మిక దీక్ష, వివాహ చర్చలు";
                case "kn" -> "ಉನ್ನತ ಶಿಕ್ಷಣ, ಹಣಕಾಸು ಹೂಡಿಕೆ, ಆಧ್ಯಾತ್ಮಿಕ ದೀಕ್ಷೆ, ವಿವಾಹ ಮಾತುಕತೆ";
                case "ml" -> "ഉന്നത വിദ്യാഭ്യാസം, സാമ്പത്തിക നിക്ഷേപങ്ങൾ, ആത്മീയ ദീക്ഷ, വിവാഹ ആലോചനകൾ";
                default -> act;
            };
        }
        if (lower.contains("arts") || lower.contains("luxury") || lower.contains("vehicle") || lower.contains("celebrations")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "கலைகள், ஆடை ஆபரணங்கள் வாங்குதல், வாகனம் வாங்குதல், சுப நிகழ்வுகள்";
                case "hi" -> "कला, विलासिता की वस्तुएं, वाहन खरीद, मांगलिक उत्सव";
                case "te" -> "కళలు, విలాస వస్తువులు, వాహన కొనుగోలు, శుభకార్యాలు";
                case "kn" -> "ಕಲೆ, ಆಭರಣ/ವಾಹನ ಖರೀದಿ, ಶುಭ ಸಮಾರಂಭಗಳು";
                case "ml" -> "കലകൾ, ആഡംബര വസ്തുക്കൾ, വാഹനം വാങ്ങൽ, മംഗള കർമ്മങ്ങൾ";
                default -> act;
            };
        }
        if (lower.contains("charity") || lower.contains("meditation") || lower.contains("discipline")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "தான தர்மங்கள், தியானம், நீண்ட காலத் திட்டங்கள், அமைதியான பணிகள்";
                case "hi" -> "दान-पुण्य, ध्यान, दीर्घकालिक योजनाएं, अनुशासनात्मक कार्य";
                case "te" -> "దానధర్మాలు, ధ్యానం, దీర్ఘకాలిక ప్రణాళికలు, క్రమశిక్షణతో కూడిన పనులు";
                case "kn" -> "ದಾನಧರ್ಮ, ಧ್ಯಾನ, ದೀರ್ಘಕಾಲೀನ ಯೋಜನೆಗಳು, ಸೇವಾ ಕಾರ್ಯಗಳು";
                case "ml" -> "ദാനധർമ്മങ്ങൾ, ധ്യാനം, ദീർഘകാല ആസൂത്രണം, സേവന പ്രവർത്തനങ്ങൾ";
                default -> act;
            };
        }
        return act;
    }

    public static String translateDirection(String dir, String lang) {
        if (dir == null || dir.isBlank()) return "";
        String trans = translate(dir, lang);
        if (!trans.equals(dir)) return trans;

        String lower = dir.toLowerCase();
        if (lower.contains("north-east") || lower.contains("ishanya") || lower.contains("northeast")) return translate("North-East (Ishanya)", lang);
        if (lower.contains("south-east") || lower.contains("agni") || lower.contains("southeast")) return translate("South-East (Agni)", lang);
        if (lower.contains("south-west") || lower.contains("niruthi") || lower.contains("southwest")) return translate("South-West (Niruthi)", lang);
        if (lower.contains("north-west") || lower.contains("vayu") || lower.contains("northwest")) return translate("North-West (Vayu)", lang);
        if (lower.equals("north") || lower.contains("north")) return translate("North", lang);
        if (lower.equals("south") || lower.contains("south")) return translate("South", lang);
        if (lower.equals("east") || lower.contains("east")) return translate("East", lang);
        if (lower.equals("west") || lower.contains("west")) return translate("West", lang);

        return dir;
    }

    public static String translateClassification(String classification, String lang) {
        if (classification == null || classification.isBlank()) return "";
        return translate(classification, lang);
    }

    public static String translateArudhaLagna(String arudhaLagna, String lang) {
        if (arudhaLagna == null || arudhaLagna.isBlank()) return "";
        // Extract sign number or name if present
        for (int s = 1; s <= 12; s++) {
            String rashi = org.vedic.astro.util.ZodiacUtils.getRashiName(s);
            if (arudhaLagna.toLowerCase().contains(rashi.toLowerCase())) {
                String loc = translateRashi(s, lang);
                return loc + " (" + rashi + ")";
            }
        }
        return arudhaLagna;
    }

    public static String translateRashi(int sign, String lang) {
        return switch (sign) {
            case 1 -> switch (lang.toLowerCase()) { case "ta" -> "மேஷம்"; case "hi" -> "मेष"; case "te" -> "మేషం"; case "kn" -> "ಮೇಷ"; case "ml" -> "മേടം"; default -> "Aries"; };
            case 2 -> switch (lang.toLowerCase()) { case "ta" -> "ரிஷபம்"; case "hi" -> "वृषभ"; case "te" -> "వృషభం"; case "kn" -> "ವೃಷಭ"; case "ml" -> "ഇടവം"; default -> "Taurus"; };
            case 3 -> switch (lang.toLowerCase()) { case "ta" -> "மிதுனம்"; case "hi" -> "मिथुन"; case "te" -> "మిథునం"; case "kn" -> "ಮಿಥುನ"; case "ml" -> "മിഥുനം"; default -> "Gemini"; };
            case 4 -> switch (lang.toLowerCase()) { case "ta" -> "கடகம்"; case "hi" -> "कर्क"; case "te" -> "కర్కాటకం"; case "kn" -> "ಕರ್ಕಾಟಕ"; case "ml" -> "കർക്കടകം"; default -> "Cancer"; };
            case 5 -> switch (lang.toLowerCase()) { case "ta" -> "சிம்மம்"; case "hi" -> "सिंह"; case "te" -> "సింహం"; case "kn" -> "ಸಿಂಹ"; case "ml" -> "ചിങ്ങം"; default -> "Leo"; };
            case 6 -> switch (lang.toLowerCase()) { case "ta" -> "கன்னி"; case "hi" -> "कन्या"; case "te" -> "కన్య"; case "kn" -> "ಕನ್ಯಾ"; case "ml" -> "കന്നി"; default -> "Virgo"; };
            case 7 -> switch (lang.toLowerCase()) { case "ta" -> "துலாம்"; case "hi" -> "तुला"; case "te" -> "తుల"; case "kn" -> "ತುಲಾ"; case "ml" -> "തുലാം"; default -> "Libra"; };
            case 8 -> switch (lang.toLowerCase()) { case "ta" -> "விருச்சிகம்"; case "hi" -> "वृश्चिक"; case "te" -> "వృశ్చికం"; case "kn" -> "ವೃಶ್ಚಿಕ"; case "ml" -> "വൃശ്ചികം"; default -> "Scorpio"; };
            case 9 -> switch (lang.toLowerCase()) { case "ta" -> "தனுசு"; case "hi" -> "धनु"; case "te" -> "ధనుస్సు"; case "kn" -> "ಧನುಸ್ಸು"; case "ml" -> "ധനു"; default -> "Sagittarius"; };
            case 10 -> switch (lang.toLowerCase()) { case "ta" -> "மகரம்"; case "hi" -> "मकर"; case "te" -> "మకరం"; case "kn" -> "ಮಕರ"; case "ml" -> "മകരം"; default -> "Capricorn"; };
            case 11 -> switch (lang.toLowerCase()) { case "ta" -> "கும்பம்"; case "hi" -> "कुंभ"; case "te" -> "కుంభం"; case "kn" -> "ಕುಂಭ"; case "ml" -> "കുംഭം"; default -> "Aquarius"; };
            case 12 -> switch (lang.toLowerCase()) { case "ta" -> "மீனம்"; case "hi" -> "मीन"; case "te" -> "మీనం"; case "kn" -> "ಮೀನ"; case "ml" -> "മീനം"; default -> "Pisces"; };
            default -> "Aries";
        };
    }

    public static String translateMarakaWindow(String window, String lang) {
        if (window == null || window.isBlank()) return "";
        if ("en".equalsIgnoreCase(lang)) return window;

        // Parse e.g. "Saturn Mahadasa - Saturn Bhukthi (2090-08 to 2093-08, ~Age 88-92) represents the primary classical Maraka/Badhaka cautionary period."
        Pattern p = Pattern.compile("(\\w+)\\s+Mahadasa\\s+-\\s+(\\w+)\\s+Bhukthi\\s*\\(([^)]+)\\)(.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(window);
        if (m.find()) {
            String p1 = translate(m.group(1), lang);
            String p2 = translate(m.group(2), lang);
            String timeAndAge = m.group(3);

            return switch (lang.toLowerCase()) {
                case "ta" -> p1 + " மகாதிசை - " + p2 + " புக்தி (" + timeAndAge + ") முக்கிய மாரக/பாதக எச்சரிக்கைக் காலமாகும்.";
                case "hi" -> p1 + " महादशा - " + p2 + " भुक्ति (" + timeAndAge + ") मुख्य मारक/बाधक सतर्कता काल है।";
                case "te" -> p1 + " మహాదశ - " + p2 + " భుక్తి (" + timeAndAge + ") ప్రధాన మారక/బాధక అప్రమత్త కాలం.";
                case "kn" -> p1 + " ಮಹಾದಶಾ - " + p2 + " ಭುಕ್ತಿ (" + timeAndAge + ") ಮುಖ್ಯ ಮಾರಕ/ಬಾಧಕ ಎಚ್ಚರಿಕೆಯ ಕಾಲಾವಧಿ.";
                case "ml" -> p1 + " മഹാദശ - " + p2 + " ഭുക്തി (" + timeAndAge + ") പ്രധാന മാരക/ബാധക ജാഗ്രതാ കാലഘട്ടം.";
                default -> window;
            };
        }
        return window;
    }

    public static String translateRationale(String rationale, String lang) {
        if (rationale == null || rationale.isBlank()) return "";
        if ("en".equalsIgnoreCase(lang)) return rationale;

        // Translate key components in classical rationale
        String res = rationale;
        if (lang.equalsIgnoreCase("ta")) {
            res = res.replace("3-Pair Longevity Span consensus indicates", "3-இணை ஆயுர் கணித ஒருமித்த முடிவு:")
                     .replace("Poornayu", "பூர்ணாயுள் (Poornayu)")
                     .replace("Madhyayu", "மத்தியாயுள் (Madhyayu)")
                     .replace("Alpayu", "அல்பாயுள் (Alpayu)")
                     .replace("Kakshya Vriddhi", "கக்ஷ்ய விருத்தி (ஆயுள் கூடுதல்)")
                     .replace("Kakshya Hrasa", "கக்ஷ்ய ஹ்ராஸம் (ஆயுள் குறைவு)")
                     .replace("Neecha Bhanga", "நீசபங்க ராஜயோகம்")
                     .replace("Lagna Lord in Dusthana", "லக்னாதிபதி மறைவு ஸ்தானத்தில்")
                     .replace("benefic Kendra/Trikona placement", "சுப கிரக கேந்திர/திரிகோண நிலை")
                     .replace("confers", "அளிக்கிறது:")
                     .replace("represents", "குறிக்கிறது:");
        } else if (lang.equalsIgnoreCase("hi")) {
            res = res.replace("3-Pair Longevity Span consensus indicates", "3-युग्म आयु गणना सहमति:")
                     .replace("Poornayu", "पूर्णायु (Poornayu)")
                     .replace("Madhyayu", "मध्‍यायु (Madhyayu)")
                     .replace("Alpayu", "अल्पायु (Alpayu)")
                     .replace("Kakshya Vriddhi", "कक्ष्या वृद्धि")
                     .replace("Kakshya Hrasa", "कक्ष्या ह्रास")
                     .replace("Neecha Bhanga", "नीचभंग राजयोग");
        } else if (lang.equalsIgnoreCase("te")) {
            res = res.replace("3-Pair Longevity Span consensus indicates", "3-జంటల ఆయుర్దాయ నిర్ణయం:")
                     .replace("Poornayu", "పూర్ణాయుష్షు (Poornayu)")
                     .replace("Madhyayu", "మధ్యాయుష్షు (Madhyayu)")
                     .replace("Alpayu", "అల్పాయుష్షు (Alpayu)")
                     .replace("Kakshya Vriddhi", "కక్ష్యా వృద్ధి")
                     .replace("Kakshya Hrasa", "కక్ష్యా హ్రాసం");
        } else if (lang.equalsIgnoreCase("kn")) {
            res = res.replace("3-Pair Longevity Span consensus indicates", "3-ಜೋಡಿ ಆಯುಷ್ಯ ನಿರ್ಣಯ:")
                     .replace("Poornayu", "ಪೂರ್ಣಾಯುಷ್ಯ (Poornayu)")
                     .replace("Madhyayu", "ಮಧ್ಯಾಯುಷ್ಯ (Madhyayu)")
                     .replace("Alpayu", "ಅಲ್ಪಾಯುಷ್ಯ (Alpayu)")
                     .replace("Kakshya Vriddhi", "ಕಕ್ಷ್ಯಾ ವೃದ್ಧಿ");
        } else if (lang.equalsIgnoreCase("ml")) {
            res = res.replace("3-Pair Longevity Span consensus indicates", "3-ജോഡി ആയുസ്സ് നിർണ്ണയം:")
                     .replace("Poornayu", "പൂർണ്ണായുസ്സ് (Poornayu)")
                     .replace("Madhyayu", "മദ്ധ്യായുസ്സ് (Madhyayu)")
                     .replace("Alpayu", "അല്പായുസ്സ് (Alpayu)");
        }
        return res;
    }
}
