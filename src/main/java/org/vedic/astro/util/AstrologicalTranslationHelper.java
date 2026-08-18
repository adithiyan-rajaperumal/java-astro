package org.vedic.astro.util;

import java.util.*;
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

        add("Goddess Mahalakshmi",
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
        add("Ruby", "மாணிக்கம் (Ruby)", "माणिक्य (Ruby)", "కెంపు (Ruby)", "ಮಾಣಿಕ್ಯ (Ruby)", "മാണിക്യം (Ruby)", "Ruby");
        add("Ruby (Manickam)", "மாணிக்கம் (Ruby)", "माणिक्य (Ruby)", "కెంపు (Ruby)", "ಮಾಣಿಕ್ಯ (Ruby)", "മാണിക്യം (Ruby)", "Ruby (Manickam)");
        add("Pearl", "முத்து (Pearl)", "मोती (Pearl)", "ముత్యం (Pearl)", "ಮುತ್ತು (Pearl)", "മുത്ത് (Pearl)", "Pearl");
        add("Pearl (Muthu)", "முத்து (Pearl)", "मोती (Pearl)", "ముత్యం (Pearl)", "ಮುತ್ತು (Pearl)", "മുത്ത് (Pearl)", "Pearl (Muthu)");
        add("Red Coral", "பவளம் (Red Coral)", "मूंगा (Red Coral)", "పగడము (Red Coral)", "ಹವಳ (Red Coral)", "പവിഴം (Red Coral)", "Red Coral");
        add("Red Coral (Pavalam)", "பவளம் (Red Coral)", "मूंगा (Red Coral)", "పగడము (Red Coral)", "ಹವಳ (Red Coral)", "പവിഴം (Red Coral)", "Red Coral (Pavalam)");
        add("Emerald", "மரகதம் (Emerald)", "पन्ना (Emerald)", "మరకతం (Emerald)", "ಪಚ್ಚೆ (Emerald)", "മരതകം (Emerald)", "Emerald");
        add("Emerald (Maragatham)", "மரகதம் (Emerald)", "पन्ना (Emerald)", "మరకతం (Emerald)", "ಪಚ್ಚೆ (Emerald)", "മരതകം (Emerald)", "Emerald (Maragatham)");
        add("Yellow Sapphire", "புஷ்பராகம் (Yellow Sapphire)", "पुखराज (Yellow Sapphire)", "పుష్యరాగం (Yellow Sapphire)", "ಪುಷ್ಯರಾಗ (Yellow Sapphire)", "പുഷ്യരാഗം (Yellow Sapphire)", "Yellow Sapphire");
        add("Yellow Sapphire (Pushparagam)", "புஷ்பராகம் (Yellow Sapphire)", "पुखराज (Yellow Sapphire)", "పుష్యరాగం (Yellow Sapphire)", "ಪುಷ್ಯರಾಗ (Yellow Sapphire)", "പുഷ്യരാഗം (Yellow Sapphire)", "Yellow Sapphire (Pushparagam)");
        add("Diamond", "வைரம் (Diamond)", "हीरा (Diamond)", "వజ్రం (Diamond)", "ವಜ್ರ (Diamond)", "വൈരം (Diamond)", "Diamond");
        add("Diamond (Vairam)", "வைரம் (Diamond)", "हीरा (Diamond)", "వజ్రం (Diamond)", "ವಜ್ರ (Diamond)", "വൈരം (Diamond)", "Diamond (Vairam)");
        add("Blue Sapphire", "நீலம் (Blue Sapphire)", "नीलम (Blue Sapphire)", "నీలం (Blue Sapphire)", "ನೀಲಂ (Blue Sapphire)", "നീലക്കല്ല് (Blue Sapphire)", "Blue Sapphire");
        add("Blue Sapphire (Neelam)", "நீலம் (Blue Sapphire)", "नीलम (Blue Sapphire)", "నీలం (Blue Sapphire)", "ನೀಲಂ (Blue Sapphire)", "നീലക്കല്ല് (Blue Sapphire)", "Blue Sapphire (Neelam)");
        add("Hessonite", "கோமேதகம் (Hessonite)", "गोमेद (Hessonite)", "గోమేధికం (Hessonite)", "ಗೋಮೇಧಿಕ (Hessonite)", "ഗോമേദകം (Hessonite)", "Hessonite");
        add("Hessonite / Gomed (Gomedhakam)", "கோமேதகம் (Hessonite)", "गोमेद (Hessonite)", "గోమేధికం (Hessonite)", "ಗೋಮೇಧಿಕ (Hessonite)", "ಗೋമേദകം (Hessonite)", "Hessonite / Gomed");
        add("Cat's Eye", "வைடூரியம் (Cat's Eye)", "लहसुनिया (Cat's Eye)", "వైడూర్యం (Cat's Eye)", "ವೈಡೂರ್ಯ (Cat's Eye)", "വൈഡൂര്യം (Cat's Eye)", "Cat's Eye");
        add("Cat's Eye (Vaidooryam)", "வைடூரியம் (Cat's Eye)", "लहसुनिया (Cat's Eye)", "వైడూర్యం (Cat's Eye)", "ವೈಡೂರ್ಯ (Cat's Eye)", "വൈഡൂര്യം (Cat's Eye)", "Cat's Eye (Vaidooryam)");

        // =========================================================================
        // 3. METALS & FINGERS
        // =========================================================================
        add("Gold", "தங்கம் (Gold)", "स्वर्ण / सोना (Gold)", "బంగారం (Gold)", "ಚಿನ್ನ (Gold)", "സ്വർണ്ണം (Gold)", "Gold");
        add("Silver", "வெள்ளி (Silver)", "चांदी (Silver)", "వెండి (Silver)", "ಬೆಳ್ಳಿ (Silver)", "വെള്ളി (Silver)", "Silver");
        add("Copper", "செம்பு (Copper)", "तांबा (Copper)", "రాగి (Copper)", "ತಾಮ್ರ (Copper)", "ചെമ്പ് (Copper)", "Copper");
        add("Panchadhatu", "ஐம்பொன் (Panchadhatu)", "पंचधातु (Panchadhatu)", "పంచలోహం (Panchadhatu)", "ಪಂಚಲೋಹ (Panchadhatu)", "പഞ്ചಲೋഹം (Panchadhatu)", "Panchadhatu");
        add("Iron / Lead", "இரும்பு / ஈயம்", "लोहा / सीसा", "ఇనుము / సీసం", "ಕಬ್ಬಿಣ / ಸೀಸ", "ഇരുമ്പ് / ഈയം", "Iron / Lead");

        add("Ring Finger", "மோதிர விரல்", "अनामिका (Ring Finger)", "ఉంగరపు వేలు", "ಉಂಗುರದ ಬೆರಳು", "മോതിരവിരൽ", "Ring Finger");
        add("Index Finger", "ஆள்காட்டி விரல்", "तर्जनी (Index Finger)", "చూపుడు వేలు", "ತೋರುಬೆರಳು", "ചൂണ്ടുവിരൽ", "Index Finger");
        add("Little Finger", "சுண்டு விரல்", "कनिष्ठिका (Little Finger)", "చిటికెన వేలు", "ಕಿರುಬೆರಳು", "ചെറുവിരൽ", "Little Finger");
        add("Middle Finger", "நடு விரல்", "मध्यमा (Middle Finger)", "మధ్య వేలు", "ಮಧ್ಯದ ಬೆರಳು", "നടുവിരൽ", "Middle Finger");

        // =========================================================================
        // 4. DIRECTIONS
        // =========================================================================
        add("East", "கிழக்கு", "पूर्व (East)", "తూర్పు", "ಪೂರ್ವ", "കിഴക്ക്", "East");
        add("West", "மேற்கு", "पश्चिम (West)", "పడమర", "ಪಶ್ಚಿಮ", "പടിഞ്ഞാറ്", "West");
        add("North", "வடக்கு", "उत्तर (North)", "ఉత్తరం", "ಉತ್ತರ", "വടക്ക്", "North");
        add("South", "தெற்கு", "दक्षिण (South)", "దక్షిణం", "ದಕ್ಷಿಣ", "തെക്ക്", "South");
        add("North-East", "வடகிழக்கு", "ईशान कोण (उत्तर-पूर्व)", "ఈశాన్యం", "ಈಶಾನ್ಯ", "വടക്കുകിഴക്ക്", "North-East");
        add("South-East", "தென்கிழக்கு", "आग्नेय कोण (दक्षिण-पूर्व)", "ఆగ్నేయం", "ಆಗ್ನೇಯ", "തെക്കുകിഴക്ക്", "South-East");
        add("South-West", "தென்மேற்கு", "नैऋत्य कोण (दक्षिण-पश्चिम)", "నైరుతి", "ನೈಋತ್ಯ", "തെക്കുപടിഞ്ഞാറ്", "South-West");
        add("North-West", "வடமேற்கு", "वायव्य कोण (उत्तर-पश्चिम)", "వాయవ్యం", "ವಾಯವ್ಯ", "വടക്കുപടിഞ്ഞാറ്", "North-West");
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
        add("Mercury", "புதன்", "बुध", "బుధుడు", "ಬುಧ", "ബുധൻ", "Mercury");
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
        add("Pitta-Vata", "பித்த-வாத பிரகிருதி (Pitta-Vata)", "पित्त-वात प्रकृति (Pitta-Vata)", "పిత్త-వాత ప్రకృతి (Pitta-Vata)", "ಪಿತ್ತ-ವಾತ ಪ್ರಕೃತಿ (Pitta-Vata)", "പിത്ത-വാത പ്രകൃതി (Pitta-Vata)", "Pitta-Vata");
        add("Pitta-Kapha", "பித்த-கப பிரகிருதி (Pitta-Kapha)", "पित्त-कफ प्रकृति (Pitta-Kapha)", "పిత్త-కఫ ప్రకృతి (Pitta-Kapha)", "ಪಿತ್ತ-ಕಫ ಪ್ರಕೃತಿ (Pitta-Kapha)", "പിത്ത-കഫ പ്രകൃതി (Pitta-Kapha)", "Pitta-Kapha");
        add("Kapha-Pitta", "கப-பித்த பிரகிருதி (Kapha-Pitta)", "कफ-पित्त प्रकृति (Kapha-Pitta)", "కఫ-పిత్త ప్రకృతి (Kapha-Pitta)", "ಕಫ-ಪಿತ್ತ ಪ್ರಕೃತಿ (Kapha-Pitta)", "കഫ-പിത്ത പ്രകൃതി (Kapha-Pitta)", "Kapha-Pitta");
        add("Vata-Kapha", "வாத-கப பிரகிருதி (Vata-Kapha)", "वात-कफ प्रकृति (Vata-Kapha)", "వాత-కఫ ప్రకృతి (Vata-Kapha)", "ವಾತ-ಕಫ ಪ್ರಕೃತಿ (Vata-Kapha)", "വാത-കഫ പ്രകൃതി (Vata-Kapha)", "Vata-Kapha");
        add("Kapha-Vata", "கப-வாத பிரகிருதி (Kapha-Vata)", "कफ-वात प्रकृति (Kapha-Vata)", "కఫ-వాత ప్రకృతి (Kapha-Vata)", "ಕಫ-ವಾತ ಪ್ರಕೃತಿ (Kapha-Vata)", "കഫ-വാത പ്രകൃതി (Kapha-Vata)", "Kapha-Vata");
        add("Sama Prakriti", "சம பிரகிருதி (Tridosha Balanced)", "सम प्रकृति (संतुलित)", "సమ ప్రకృతి (సమతుల్యం)", "ಸಮ ಪ್ರಕೃತಿ (ಸಮತೋಲಿತ)", "സമ പ്രകൃതി (സന്തുലിതം)", "Sama Prakriti (Balanced)");
        add("Sama Prakriti (Tridosha Balanced)", "சம பிரகிருதி (Tridosha Balanced)", "सम प्रकृति (संतुलित)", "సమ ప్రకృతి (సమతుల్యం)", "ಸಮ ಪ್ರಕೃತಿ (ಸಮತೋಲಿತ)", "സമ പ്രകൃതി (സന്തുലിതം)", "Sama Prakriti (Tridosha Balanced)");

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
        add("Krisa Deha (Slender / Lean Frame, Quick Movements & Dry Skin)",
                "கிருஷ தேகம் (ஒல்லியான / நடுத்தர கட்டமைப்பு, சுறுசுறுப்பு)",
                "कृश देह (दुबला-पतला, त्वरित गतिशीलता व शुष्क त्वचा)",
                "కృశ దేహం (సన్నని శరీరం, చురుకైన కదలికలు)",
                "ಕೃಶ ದೇಹ (ತೆಳ್ಳನೆಯ ಶರೀರ, ಚುರುಕು ಚಲನವಲನ)",
                "കൃശ ദേഹം (മെലിഞ്ഞ ശരീരം, വേഗത്തിലുള്ള ചലനങ്ങൾ)",
                "Krisa Deha (Slender / Lean Frame, Quick Movements & Dry Skin)");

        add("Krisha Deha (Slender & Angular)",
                "கிருஷ தேகம் (ஒல்லியான / நடுத்தர கட்டமைப்பு)",
                "कृश देह (दुबला-पतला)",
                "కృశ దేహం (సన్నని శరీరం)",
                "ಕೃಶ ದೇಹ (ತೆಳ್ಳನೆಯ ಶರೀರ)",
                "കൃശ ദേഹം (മെലിഞ്ഞ ശരീരം)",
                "Krisha Deha (Slender & Angular)");

        add("Madhya Deha (Medium Athletic Frame, High Vitality & Warm Complexion)",
                "மத்திய தேகம் (சீரான தடகள கட்டமைப்பு, அதிக சுறுசுறுப்பு)",
                "मध्य देह (मध्यम व सुडौल शारीरिक गठन, उच्च प्राणशक्ति)",
                "మధ్య దేహం (మధ్యస్థ సుదృఢ శరీరం, అధిక జీవశక్తి)",
                "ಮಧ್ಯ ದೇಹ (ಮಧ್ಯಮ ಸುದೃಢ ಕಾಯ, ಅಧಿಕ ಚೈತನ್ಯ)",
                "മദ്ധ്യ ദേഹം (മിതമായ കായിക ഘടന, ഉയർന്ന ഉന്മേഷം)",
                "Madhya Deha (Medium Athletic Frame, High Vitality & Warm Complexion)");

        add("Madhya Deha (Medium & Athletic)",
                "மத்திய தேகம் (சீரான உடலமைப்பு)",
                "मध्य देह (मध्यम व सुडौल)",
                "మధ్య దేహం (మధ్యస్థ సుదృఢ శరీరం)",
                "ಮಧ್ಯ ದೇಹ (ಮಧ್ಯಮ ಗಾತ್ರ)",
                "മദ്ധ്യ ദേഹം (മിതമായ ഘടന)",
                "Madhya Deha (Medium & Athletic)");

        add("Madhya Deha (Balanced Proportions)",
                "மத்திய தேகம் (சமச்சீரான உடலமைப்பு)",
                "मध्य देह (संतुलित शारीरिक गठन)",
                "మధ్య దేహం (సమతుల్య శరీరం)",
                "ಮಧ್ಯ ದೇಹ (ಸಮತೋಲಿತ ಕಾಯ)",
                "മദ്ധ്യ ദേഹം (സന്തുലിത ഘടന)",
                "Madhya Deha (Balanced Proportions)");

        add("Sthula Deha (Solid / Broad Frame, High Endurance & Smooth Complexion)",
                "ஸ்தூல தேகம் (திடகாத்திரமான / அகன்ற உடலமைப்பு, அதிக சகிப்புத்தன்மை)",
                "स्थूल देह (मजबूत व चौड़ा शारीरिक ढांचा, उच्च सहनशक्ति)",
                "స్థూల దేహం (దృఢమైన విశాల శరీరం, అధిక సహనశక్తి)",
                "ಸ್ಥೂಲ ದೇಹ (ದೃಢವಾದ ವಿಶಾಲ ಕಾಯ, ಅಧಿಕ ಸಹಿಷ್ಣುತೆ)",
                "സ്ഥൂല ദേഹം (ബലിഷ്ഠമായ ഘടന, ഉയർന്ന സഹിഷ്ണുത)",
                "Sthula Deha (Solid / Broad Frame, High Endurance & Smooth Complexion)");

        add("Sthula Deha (Broad & Robust)",
                "ஸ்தூல தேகம் (திடகாத்திரமான / அகன்ற உடலமைப்பு)",
                "स्थूल देह (मजबूत व चौड़ा)",
                "స్థూల దేహం (దృఢమైన శరీరం)",
                "ಸ್ಥೂಲ ದೇಹ (ದೃಢವಾದ ಕಾಯ)",
                "സ്ഥൂല ദേഹം (ബലിഷ്ഠമായ ശരീരം)",
                "Sthula Deha (Broad & Robust)");

        add("Vata-Pitta Frame (Lean-Athletic, Quick Reflexes & Energetic Stamina)",
                "வாத-பித்த தேகம் (மெலிந்த-தடகள கட்டமைப்பு, சுறுசுறுப்பு)",
                "वात-पित्त देह (पतला-सुडौल, तीव्र गति व ऊर्जावान)",
                "వాత-పిత్త శరీరం (సన్నని-సుదృఢ శరీరం, అధిక శక్తి)",
                "ವಾತ-ಪಿತ್ತ ಕಾಯ (ತೆಳ್ಳನೆಯ-ಸುದೃಢ, ಚುರುಕು ಚೈತನ್ಯ)",
                "വാത-പിത്ത ശരീരം (മെലിഞ്ഞ കായിക ഘടന, ഉയർന്ന ഊർജ്ജം)",
                "Vata-Pitta Frame (Lean-Athletic, Quick Reflexes & Energetic Stamina)");

        add("Kapha-Pitta Frame (Strong Muscular Build, High Stamina & Solid Structure)",
                "கப-பித்த தேகம் (திடமான தசை வலிமை, அதிக சகிப்புத்தன்மை)",
                "कफ-पित्त देह (मजबूत मांसपेशियां, उच्च सहनशक्ति व सुदृढ़ गठन)",
                "కఫ-పిత్త శరీరం (బలమైన కండరాల శరీరం, అధిక ఓర్పు)",
                "ಕಫ-ಪಿತ್ತ ಕಾಯ (ಬಲಿಷ್ಠ ಸ್ನಾಯುಗಳು, ಗಟ್ಟಿಯಾದ ಮೈಕಟ್ಟು)",
                "കഫ-പിത്ത ശരീരം (ബലമുള്ള പേശികൾ, ഉയർന്ന സഹിഷ്ണുത)",
                "Kapha-Pitta Frame (Strong Muscular Build, High Stamina & Solid Structure)");

        add("Vata-Kapha Frame (Variable Bone Structure, Cold Sensitivity & Steady Endurance)",
                "வாத-கப தேகம் (மாறிவரும் எலும்பு கட்டமைப்பு, நிதானமான சகிப்புத்தன்மை)",
                "वात-कफ देह (परिवर्तनशील अस्थि ढांचा, शीत संवेदनशीलता व स्थिर सहनशक्ति)",
                "వాత-కఫ శరీరం (మారే ఎముకల నిర్మాణం, స్థిరమైన ఓర్పు)",
                "ವಾತ-ಕಫ ಕಾಯ (ಮೂಳೆಗಳ ರಚನೆ, ಸ್ಥಿರ ಸಹಿಷ್ಣುತೆ)",
                "വാത-കഫ ശരീരം (വ്യതിയാനമുള്ള അസ്ഥിഘടന, സ്ഥിരമായ സഹിഷ്ണുത)",
                "Vata-Kapha Frame (Variable Bone Structure, Cold Sensitivity & Steady Endurance)");

        add("Sama Deha (Harmonious & Proportionate Athletic Frame)",
                "சம தேகம் (சமச்சீரான மற்றும் கம்பீரமான உடலமைப்பு)",
                "सम देह (संतुलित, सुडौल व आकर्षक शारीरिक ढांचा)",
                "సమ దేహం (సమతుల్య ఆకర్షణీయ శరీరం)",
                "ಸಮ ದೇಹ (ಸಮತೋಲಿತ ಆಕರ್ಷಕ ಕಾಯ)",
                "സമ ദേഹം (സന്തുലിതവും ആകർഷകവുമായ കായിക ഘടന)",
                "Sama Deha (Harmonious & Proportionate Athletic Frame)");

        // Dhatus
        add("Asthi Dhatu (Bone Density & Skeletal Structural Strength)",
                "அஸ்தி தாது (எலும்பு அடர்த்தி & எலும்புக்கூட்டு வலிமை)",
                "अस्थि धातु (हड्डियों का घनत्व व शारीरिक ढांचा)",
                "అస్థి ధాతువు (ఎముకల బలం & శరీర నిర్మాణం)",
                "ಅಸ್ಥಿ ಧಾತು (ಮೂಳೆಗಳ ಸಾಂದ್ರತೆ & ದೃಢತೆ)",
                "അസ്ഥി ധാതു (എല്ലുകളുടെ സാന്ദ്രതയും ഘടനയും)",
                "Asthi Dhatu (Bone Density & Skeletal Structural Strength)");

        add("Asthi & Majja Dhatu (Bones & Bone Marrow)",
                "அஸ்தி & மஜ்ஜா தாது (எலும்பு மற்றும் மஜ்ஜை)",
                "अस्थि व मज्जा धातु (हड्डियां व मज्जा)",
                "అస్థి & మజ్జా ధాతువు (ఎముకలు మరియు మజ్జ)",
                "ಅಸ್ಥಿ ಮತ್ತು ಮಜ್ಜಾ ಧಾತು (ಮೂಳೆಗಳು)",
                "അസ്ഥി & മജ്ജാ ധാതു (എല്ലുകൾ)",
                "Asthi & Majja Dhatu (Bones & Bone Marrow)");

        add("Rakta & Rasa Dhatu (Blood Plasma, Bodily Fluids & Lymphatic Flow)",
                "ரக்த & ரஸ தாது (இரத்த பிளாஸ்மா & நிணநீர் ஓட்டம்)",
                "रक्त व रस धातु (रक्त प्लाज्मा, शारीरिक द्रव व लसीका)",
                "రక్త & రస ధాతువు (రక్త ప్లాస్మా & శరీర ద్రవాలు)",
                "ರಕ್ತ ಮತ್ತು ರಸ ಧಾತು (ರಕ್ತ ಪ್ಲಾಸ್ಮಾ & ದ್ರವಗಳು)",
                "രക്ത & രസ ധാതു (രക്ത പ്ലാസ്മയും ലിംഫും)",
                "Rakta & Rasa Dhatu (Blood Plasma, Bodily Fluids & Lymphatic Flow)");

        add("Rasa & Rakta Dhatu (Vital Plasma & Fluids)",
                "ரஸ & ரத்த தாது (இரத்தம் மற்றும் உடலின் முக்கிய திரவங்கள்)",
                "रस व रक्त धातु (प्लाज्मा व रक्त)",
                "రస & రక్త ధాతువు (రక్తము మరియు రసము)",
                "ರಸ ಮತ್ತು ರಕ್ತ ಧಾತು (ರಕ್ತ)",
                "രസ & രക്ത ധാതു (രക്തം)",
                "Rasa & Rakta Dhatu (Vital Plasma & Fluids)");

        add("Majja & Mamsa Dhatu (Bone Marrow, Muscle Tone & Vital Red Blood Cells)",
                "மஜ்ஜா & மாம்ச தாது (எலும்பு மஜ்ஜை, தசை வலிமை & சிவப்பணுக்கள்)",
                "मज्जा व मांस धातु (अस्थि मज्जा, मांसपेशियां व लाल रक्त कण)",
                "మజ్జా & మాంస ధాతువు (ఎముక మజ్జ, కండరాల బలం)",
                "ಮಜ್ಜಾ ಮತ್ತು ಮಾಂಸ ಧಾತು (ಮೂಳೆ ಮಜ್ಜಾ & ಸ್ನಾಯುಗಳು)",
                "മജ്ജാ & മാംസ ധാതു (മജ്ജയും പേശീബലവും)",
                "Majja & Mamsa Dhatu (Bone Marrow, Muscle Tone & Vital Red Blood Cells)");

        add("Tvak & Rasa Dhatu (Skin Barrier, Plasma & Neural Fluid Channels)",
                "த்வக் & ரஸ தாது (தோல் பாதுகாப்பு, நரம்பு திரவப் பாதைகள்)",
                "त्वचा व रस धातु (त्वचा की सुरक्षा, प्लाज्मा व तंत्रिका द्रव)",
                "త్వక్ & రస ధాతువు (చర్మ రక్షణ, నాడీ ద్రవాలు)",
                "ತ್ವಕ್ ಮತ್ತು ರಸ ಧಾತು (ಚರ್ಮ ರಕ್ಷಣೆ & ನರ ದ್ರವಗಳು)",
                "ത്വക് & രസ ധാതു (ചർമ്മ സംരക്ഷണവും നാഡീ ദ്രവങ്ങളും)",
                "Tvak & Rasa Dhatu (Skin Barrier, Plasma & Neural Fluid Channels)");

        add("Meda Dhatu (Adipose Tissue, Healthy Fats & Glandular Nourishment)",
                "மேதஸ் தாது (கொழுப்பு திசு, ஆரோக்கியமான கொழுப்புகள் & நாளமில்லா சுரப்பிகள்)",
                "मेद धातु (वसा ऊतक, स्वस्थ वसा व ग्रंथि पोषण)",
                "మేదో ధాతువు (కొవ్వు కణజాలం & గ్రంధుల పోషణ)",
                "ಮೇದಸ್ಸು ಧಾತು (ಕೊಬ್ಬಿನ ಅಂಗಾಂಶ & ಗ್ರಂಥಿ ಪೋಷಣೆ)",
                "മേദസ്സ് ധാതു (കൊഴുപ്പ് കോശങ്ങളും ഗ്രന്ഥി പോഷണവും)",
                "Meda Dhatu (Adipose Tissue, Healthy Fats & Glandular Nourishment)");

        add("Meda & Shukra Dhatu (Adipose & Vital Essence)",
                "மேதஸ் & சுக்ர தாது (கொழுப்பு மற்றும் சுக்கிலம்)",
                "मेद व शुक्र धातु (वसा व ओज)",
                "మేదస్సు & శుక్ర ధాతువు",
                "ಮೇದಸ್ಸು ಮತ್ತು ಶುಕ್ರ ಧಾತು",
                "മേദസ്സും ശുക്ര ധാതുവും",
                "Meda & Shukra Dhatu (Adipose & Vital Essence)");

        add("Shukra Dhatu (Reproductive Tissue, Vitality & Ojas Immunity)",
                "சுக்ர தாது (இனப்பெருக்க திசு, ஓஜஸ் மற்றும் நோய் எதிர்ப்பு சக்தி)",
                "शुक्र धातु (प्रजनन ऊतक, ओजस व रोग प्रतिरोधक क्षमता)",
                "శుక్ర ధాతువు (పునరుత్పత్తి కణజాలం, ఓజస్సు & రోగనిరోధక శక్తి)",
                "ಶುಕ್ರ ಧಾತು (ಪ್ರಜನನ ಅಂಗಾಂಶ, ಓಜಸ್ಸು & ರೋಗನಿರೋಧಕ ಶಕ್ತಿ)",
                "ശുക്ര ധാതു (പ്രത്യുൽപാദന കോശങ്ങൾ, ഓജസ്സും പ്രതിരോധശേഷിയും)",
                "Shukra Dhatu (Reproductive Tissue, Vitality & Ojas Immunity)");

        add("Snayu & Asthi Dhatu (Nerves, Tendons, Ligaments & Joint Lubrication)",
                "ஸ்நாயு & அஸ்தி தாது (நரம்புகள், தசைநார்கள் & மூட்டு திரவம்)",
                "स्नायु व अस्थि धातु (तंत्रिकाएं, कंडराएं व जोड़ों का स्नेहन)",
                "స్నాయు & అస్థి ధాతువు (నరాలు, కీళ్ల కందెన & స్నాయువులు)",
                "ಸ್ನಾಯು ಮತ್ತು ಅಸ್ಥಿ ಧಾತು (ನರಗಳು, ಮೂಳೆ ಕೀಲುಗಳು)",
                "സ്നായു & അസ്ഥി ധാതു (നാഡികൾ, സന്ധികൾ)",
                "Snayu & Asthi Dhatu (Nerves, Tendons, Ligaments & Joint Lubrication)");

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
        if (lower.contains("muruga") || lower.contains("kartikeya") || lower.contains("subramanya") || lower.contains("narasimha")) return translate("Lord Murugan / Lord Narasimha / Kartikeya", lang);
        if (lower.contains("vishnu") || lower.contains("narayana") || lower.contains("venkateshwara")) return translate("Lord Vishnu / Lord Venkateshwara", lang);
        if (lower.contains("ganesha") || lower.contains("ganapati")) return translate("Lord Ganesha / Ganapati", lang);
        if (lower.contains("lakshmi")) return translate("Goddess Mahalakshmi / Goddess Lakshmi", lang);
        if (lower.contains("hanuman") || lower.contains("bhairava")) return translate("Lord Hanuman / Lord Bhairava / Lord Rudra", lang);
        if (lower.contains("durga")) return translate("Goddess Durga / Chamundeshwari", lang);

        return deity;
    }

    public static String translateKulaDevata(String remedy, String lang) {
        if (remedy == null || remedy.isBlank()) return "";
        if ("en".equalsIgnoreCase(lang)) return remedy;

        String lower = remedy.toLowerCase();
        if (lower.contains("supreme kula devata blessings") || lower.contains("kula vriddhi")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "5-ஆம் அதிபதி/வீட்டில் சுப கிரக பார்வை உள்ளதால், பூரண குலதெய்வ அருள் மற்றும் குல விருத்தி கிட்டும்.";
                case "hi" -> "पंचम भाव व भावेश पर शुभ ग्रहों के प्रभाव से पूर्ण कुलदेवता कृपा एवं कुल वृद्धि प्राप्त होगी।";
                case "te" -> "5వ భావం మరియు అధిపతిపై శుభ గ్రహ అనుగ్రహం వలన సంపూర్ణ కులదైవ ఆశీస్సులు మరియు వంశాభివృద్ధి కలుగుతాయి.";
                case "kn" -> "5ನೇ ಮನೆ ಮತ್ತು ಅಧಿಪತಿಯ ಮೇಲೆ ಶುಭ ಗ್ರಹಗಳ ಅನುಗ್ರಹದಿಂದ ಸಂಪೂರ್ಣ ಕುಲದೇವರ ಆಶೀರ್ವಾದ ಮತ್ತು ವಂಶಾಭಿವೃದ್ಧಿ ಪ್ರಾಪ್ತಿಯಾಗುತ್ತದೆ.";
                case "ml" -> "5-ാം ഭാവത്തിലും നാഥനിലും ശുഭ ഗ്രഹങ്ങളുടെ കൃപ ഉള്ളതിനാൽ പൂർണ്ണ കുലദേവതാ അനുഗ്രഹവും വംശവൃദ്ധിയും ഉണ്ടാകും.";
                default -> remedy;
            };
        }
        if (lower.contains("ancestral blessings require attention") || lower.contains("ghee lamp at the kula devata")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "குலதெய்வ வழிபாடு அவசியம். அமாவாசை தினங்களில் பித்ரு தர்ப்பணம் மற்றும் குலதெய்வக் கோயிலில் நெய் தீபம் ஏற்றி வழிபடுவது நன்று.";
                case "hi" -> "कुलदेवता पूजन आवश्यक है। अमावस्या पर पितृ तर्पण और कुलदेवता मंदिर में घी का दीपक जलाना शुभ है।";
                case "te" -> "కులదైవ పూజ అవసరం. అమావాస్య రోజులలో పితృ తర్పణం మరియు కులదైవ ఆలయంలో నేతి దీపం వెలిగించడం శ్రేయస్కరం.";
                case "kn" -> "ಕುಲದೇವರ ಪೂಜೆ ಅಗತ್ಯ. ಅಮಾವಾಸ್ಯೆಯಂದು ಪಿತೃ ತರ್ಪಣ ಮತ್ತು ಕುಲದೇವರ ದೇವಸ್ಥಾನದಲ್ಲಿ ತುಪ್ಪದ ದೀಪ ಬೆಳಗಿಸುವುದು ಉತ್ತಮ.";
                case "ml" -> "കുലദേവതാ പൂജ അത്യന്താപേക്ഷിതമാണ്. അമാവാസിയിൽ പിതൃതർപ്പണവും കുലദേവതാ ക്ഷേത്രത്തിൽ നെയ്യ് വിളക്കും സമർപ്പിക്കുക.";
                default -> remedy;
            };
        }
        if (lower.contains("kula devata and ancestral blessings are protective") || lower.contains("family harmony")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "குலதெய்வ அருள் குடும்பத்தில் அமைதி, ஒற்றுமை மற்றும் சந்ததி நலனைப் பாதுகாக்கிறது.";
                case "hi" -> "कुलदेवता व पितृ कृपा परिवार में सुख-शांति, सौहार्द और संतान कल्याण की रक्षा करती है।";
                case "te" -> "కులదైవ ఆశీస్సులు కుటుంబంలో శాంతి, ఐక్యత మరియు సంతాన క్షేమాన్ని కాపాడతాయి.";
                case "kn" -> "ಕುಲದೇವರ ಕೃಪೆಯು ಕುಟುಂಬದಲ್ಲಿ ಶಾಂತಿ, ಸಾಮರಸ್ಯ ಮತ್ತು ಸಂತಾನ ಕ್ಷೇಮವನ್ನು ಕಾಪಾಡುತ್ತದೆ.";
                case "ml" -> "കുലദേവതാ അനുഗ്രഹം കുടുംബത്തിൽ സമാധാനവും ഐക്യവും സന്താന ക്ഷേമവും നൽകുന്നു.";
                default -> remedy;
            };
        }
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
        if ("en".equalsIgnoreCase(lang)) return timing;

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
        String sunriseOnly = switch (lang.toLowerCase()) {
            case "ta" -> "சூரிய உதயம்";
            case "hi" -> "सूर्योदय";
            case "te" -> "సూర్యోదయం";
            case "kn" -> "ಸೂರ್ಯೋದಯ";
            case "ml" -> "സൂര്യോദയം";
            default -> "Sunrise";
        };

        if (!day.isEmpty()) {
            if (lower.contains("shukla")) {
                return day + " " + shuklaStr;
            } else {
                return day + " " + sunriseOnly;
            }
        }
        return timing;
    }

    public static String translateDayName(String dayName, String lang) {
        if (dayName == null || dayName.isBlank()) return "";
        return translate(dayName, lang);
    }

    public static String translateAuspiciousActivities(String act, String lang) {
        if (act == null || act.isBlank()) return "";
        if ("en".equalsIgnoreCase(lang)) return act;

        String lower = act.toLowerCase();
        if (lower.contains("commerce") || lower.contains("documentation") || (lower.contains("education") && lower.contains("investments"))) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "கல்வி, வணிகம், தகவல் தொடர்பு, ஆவணப் பணிகள், முதலீடுகள்";
                case "hi" -> "शिक्षा, व्यापार, संचार, दस्तावेजी कार्य, निवेश";
                case "te" -> "విద్య, వ్యాపారం, కమ్యూనికేషన్, డాక్యుమెంటేషన్, పెట్టుబడులు";
                case "kn" -> "ಶಿಕ್ಷಣ, ವ್ಯಾಪಾರ, ಸಂವಹನ, ದಾಖಲಾತಿ, ಹೂಡಿಕೆಗಳು";
                case "ml" -> "വിദ്യാഭ്യാസം, വ്യാപാരം, ആശയവിനിമയം, രേഖകൾ, നിക്ഷേപങ്ങൾ";
                default -> act;
            };
        }
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
        if ("en".equalsIgnoreCase(lang)) return dir;

        String trans = translate(dir, lang);
        if (!trans.equalsIgnoreCase(dir)) return trans;

        String lower = dir.toLowerCase();
        if (lower.contains("jupiter digbala")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "கிழக்கு / வடகிழக்கு (குரு திக்பலம்)";
                case "hi" -> "पूर्व / ईशान कोण (गुरु दिग्बल)";
                case "te" -> "తూర్పు / ఈశాన్యం (గురు దిగ్బలం)";
                case "kn" -> "ಪೂರ್ವ / ಈಶಾನ್ಯ (ಗುರು ದಿಗ್ಬಲ)";
                case "ml" -> "കിഴക്ക് / വടക്കുകിഴക്ക് (ഗുരു ദിഗ്ബലം)";
                default -> dir;
            };
        }
        if (lower.contains("east (fire)")) return translate("East", lang) + " (" + translate("Agni (Fire)", lang) + ")";
        if (lower.contains("north (water)")) return translate("North", lang) + " (" + translate("Jala (Water)", lang) + ")";
        if (lower.contains("west (air)")) return translate("West", lang) + " (" + translate("Vayu (Air)", lang) + ")";
        if (lower.contains("south (earth)")) return translate("South", lang) + " (" + translate("Prithvi (Earth)", lang) + ")";

        if (lower.contains("north-east") || lower.contains("ishanya") || lower.contains("northeast")) return translate("North-East (Ishanya)", lang);
        if (lower.contains("south-east") || lower.contains("agni") || lower.contains("southeast")) return translate("South-East (Agni)", lang);
        if (lower.contains("south-west") || lower.contains("niruthi") || lower.contains("southwest")) return translate("South-West (Niruthi)", lang);
        if (lower.contains("north-west") || lower.contains("vayu") || lower.contains("northwest")) return translate("North-West (Vayu)", lang);
        if (lower.startsWith("north")) return translate("North", lang);
        if (lower.startsWith("south")) return translate("South", lang);
        if (lower.startsWith("east")) return translate("East", lang);
        if (lower.startsWith("west")) return translate("West", lang);

        return dir;
    }

    public static String translateArudhaLagna(String arudhaLagna, String lang) {
        if (arudhaLagna == null || arudhaLagna.isBlank()) return "";
        if ("en".equalsIgnoreCase(lang)) return arudhaLagna;

        // Pattern: "Kanya (House 10) - Social Status & Professional Recognition Anchor (Arudha Lagna - AL)"
        Pattern p = Pattern.compile("([A-Za-z]+)\\s*\\(House\\s*(\\d+)\\)\\s*-\\s*(.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(arudhaLagna);
        if (m.find()) {
            String rashiName = m.group(1);
            int houseNum = Integer.parseInt(m.group(2));
            int signNum = 1;
            for (int s = 1; s <= 12; s++) {
                if (org.vedic.astro.util.ZodiacUtils.getRashiName(s).equalsIgnoreCase(rashiName)) {
                    signNum = s;
                    break;
                }
            }
            String rashiLoc = translateRashi(signNum, lang);
            return switch (lang.toLowerCase()) {
                case "ta" -> rashiLoc + " (" + houseNum + "-ஆம் வீடு) - சமூக அந்தஸ்து மற்றும் தொழில் அங்கீகாரம் (ஆரூட லக்னம் - AL)";
                case "hi" -> rashiLoc + " (" + houseNum + "वां भाव) - सामाजिक प्रतिष्ठा व व्यावसायिक ख्याति (आरूढ़ लग्न - AL)";
                case "te" -> rashiLoc + " (" + houseNum + "వ భావం) - సామాజిక హోదా & వృత్తి గుర్తింపు (ఆరూఢ లగ్నం - AL)";
                case "kn" -> rashiLoc + " (" + houseNum + "ನೇ ಮನೆ) - ಸಾಮಾನಿಕ ಪ್ರತಿಷ್ಠೆ & ವೃತ್ತಿಪರ ಗುರುತಿಸುವಿಕೆ (ಆರೂಢ ಲಗ್ನ - AL)";
                case "ml" -> rashiLoc + " (" + houseNum + "-ാം ഭാവം) - സാമൂഹിക പദവിയും തൊഴിൽ അംഗീകാരവും (ആരൂഢ ലഗ്നം - AL)";
                default -> arudhaLagna;
            };
        }

        // Fallback: search for sign name
        for (int s = 1; s <= 12; s++) {
            String rashi = org.vedic.astro.util.ZodiacUtils.getRashiName(s);
            if (arudhaLagna.toLowerCase().contains(rashi.toLowerCase())) {
                String loc = translateRashi(s, lang);
                return loc + " (" + rashi + ")";
            }
        }
        return arudhaLagna;
    }

    public static String translateRashiName(String rashiName, String lang) {
        if (rashiName == null || rashiName.isBlank()) return "";
        String lower = rashiName.trim().toLowerCase();
        int sign = switch (lower) {
            case "mesha", "aries" -> 1;
            case "vrishabha", "taurus" -> 2;
            case "mithuna", "gemini" -> 3;
            case "kataka", "cancer" -> 4;
            case "simha", "leo" -> 5;
            case "kanya", "virgo" -> 6;
            case "tula", "libra" -> 7;
            case "vrishchika", "scorpio" -> 8;
            case "dhanus", "sagittarius" -> 9;
            case "makara", "capricorn" -> 10;
            case "kumbha", "aquarius" -> 11;
            case "meena", "pisces" -> 12;
            default -> 1;
        };
        return translateRashi(sign, lang);
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

    public static String translateRasayana(String rasayana, String lang) {
        if (rasayana == null || rasayana.isBlank()) return "";
        if ("en".equalsIgnoreCase(lang)) return rasayana;

        String lower = rasayana.toLowerCase();
        if (lower.contains("triphala") && lower.contains("trikatu")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "திரிபலா, திரிகடுகம் (சுக்கு/மிளகு/திப்பிலி), துளசி, மற்றும் குக்குலு";
                case "hi" -> "त्रिफला, त्रिकटु (सोंठ/काली मिर्च/पिप्पली), तुलसी व गुग्गुल";
                case "te" -> "త్రిఫల, త్రికటు (శొంఠి/మిరియాలు/పిప్పళ్ళు), తులసి మరియు గుగ్గులు";
                case "kn" -> "ತ್ರಿಫಲ, ತ್ರಿಕಟು (ಶುಂಠಿ/ಮೆಣಸು/ಹಿಪ್ಪಲಿ), ತುಳಸಿ ಮತ್ತು ಗುಗ್ಗುಳು";
                case "ml" -> "ത്രിഫല, ത്രികടു (ചുക്ക്/കുരുമുളക്/തിപ്പലി), തുളസി, ഗുഗ്ഗുലു";
                default -> rasayana;
            };
        }
        if (lower.contains("ashwagandha")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "அஸ்வகந்தா, நல்லெண்ணெய் மசாஜ், பலா மற்றும் தசமூலம்";
                case "hi" -> "अश्वगंधा, गुनगुने तिल के तेल की मालिश, बला व दशमूल";
                case "te" -> "అశ్వగంధ, నువ్వుల నూనె మర్దన, బలా మరియు దశమూల";
                case "kn" -> "ಅಶ್ವಗಂಧ, ಎಳ್ಳೆಣ್ಣೆ ಮಸಾಜ್, ಬಲಾ ಮತ್ತು ದಶಮೂಲ";
                case "ml" -> "അശ്വഗന്ധ, എള്ളെണ്ണ മസാജ്, ബല, ദശമൂലം";
                default -> rasayana;
            };
        }
        if (lower.contains("amalaki") || lower.contains("guduchi") || lower.contains("shatavari")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "நெல்லிக்காய் (ஆம்லா), சீந்தில் (குடூச்சி), சதாவரி மற்றும் பிராமி நெய்";
                case "hi" -> "आंवला (आमलकी), गिलोय (गुडूची), शतावरी व ब्राह्मी घृत";
                case "te" -> "ఉసిరి (ఆమలకి), తిప్పతీగ (గుడూచి), శతావరి మరియు బ్రాహ్మీ నెయ్యి";
                case "kn" -> "ನೆಲ್ಲಿಕಾಯಿ (ಆಮಲಕಿ), ಅಮೃತಬಳ್ಳಿ (ಗುಡೂಚಿ), ಶತಾವರಿ ಮತ್ತು ಬ್ರಾಹ್ಮೀ ತುಪ್ಪ";
                case "ml" -> "നെല്ലിക്ക (ആമലകി), ചിറ്റമൃത് (ഗുഡൂചി), ശതാവരി, ബ്രാഹ്മി നെയ്യ്";
                default -> rasayana;
            };
        }
        if (lower.contains("chyawanprash")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "சியவனபிராசம், பிராமி, நெல்லிக்காய் மற்றும் திரிபலா (முத்தோஷ சமநிலை இரசாயனம்)";
                case "hi" -> "च्यवनप्राश, ब्राह्मी, आंवला व त्रिफला (त्रिदोष संतुलन रसायन)";
                case "te" -> "చ్యవనప్రాశ్, బ్రాహ్మి, ఉసిరి మరియు త్రిఫల (త్రిదోష సమతుల్య రసాయనం)";
                case "kn" -> "ಚ್ಯವನಪ್ರಾಶ, ಬ್ರಾಹ್ಮಿ, ನೆಲ್ಲಿಕಾಯಿ ಮತ್ತು ತ್ರಿಫಲ (ತ್ರಿದೋಷ ಸಮತೋಲನ ರಸಾಯನ)";
                case "ml" -> "ച്യവനപ്രാശം, ബ്രാഹ്മി, നെല്ലിക്ക, ത്രിഫല (ത്രിദോഷ സന്തുലിത രസായനം)";
                default -> rasayana;
            };
        }
        return rasayana;
    }

    public static String translateOrganVulnerabilities(List<String> vulnerabilities, String lang) {
        if (vulnerabilities == null || vulnerabilities.isEmpty()) return "";
        if ("en".equalsIgnoreCase(lang)) return String.join(", ", vulnerabilities);

        List<String> translated = new ArrayList<>();
        for (String v : vulnerabilities) {
            translated.add(translateSingleOrganVulnerability(v, lang));
        }
        return String.join("; ", translated);
    }

    private static String translateSingleOrganVulnerability(String v, String lang) {
        if (v == null || v.isBlank()) return "";
        String lower = v.toLowerCase();

        // 1. 8th Lord Longevity resilience
        if (lower.contains("longevity resilience") && lower.contains("8th lord")) {
            Pattern p = Pattern.compile("8th Lord\\s+([A-Za-z]+)\\s+in\\s+([A-Za-z]+)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(v);
            if (m.find()) {
                String lord = translate(m.group(1), lang);
                String rashi = translateRashiName(m.group(2), lang);
                return switch (lang.toLowerCase()) {
                    case "ta" -> "8-ஆம் அதிபதி " + lord + " " + rashi + "-ல் நிற்பதால் நீண்ட ஆயுள் மற்றும் ஆரோக்கியப் பராமரிப்பு உறுதி பெறுகிறது";
                    case "hi" -> "अष्टमेश " + lord + " " + rashi + " में स्थित होने से दीर्घायु व स्वास्थ्य सहनशक्ति प्राप्त होती है";
                    case "te" -> "8వ అధిపతి " + lord + " " + rashi + "లో ఉండటం వల్ల దీర్ఘాయుష్షు మరియు ఆరోగ్య స్థిరత్వం కలుగుతాయి";
                    case "kn" -> "8ನೇ ಅಧಿಪತಿ " + lord + " " + rashi + "ನಲ್ಲಿರುವುದರಿಂದ ದೀರ್ಘಾಯುಷ್ಯ ಮತ್ತು ಆರೋಗ್ಯ ರಕ್ಷಣೆ ದೊರೆಯುತ್ತದೆ";
                    case "ml" -> "8-ാം നാഥൻ " + lord + " " + rashi + "-ൽ നിൽക്കുന്നതിനാൽ ദീർഘായുസ്സും ആരോഗ്യ സ്ഥിരതയും ലഭിക്കുന്നു";
                    default -> v;
                };
            }
        }

        // 2. Throat, vocal cords (Taurus/Vrishabha in 6th)
        if (lower.contains("throat") || lower.contains("vocal cords") || lower.contains("vrishabha")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "தொண்டை, தைராய்டு சுரப்பி, குரல்வளை மற்றும் முகப் பகுதி நரம்பு உணர்திறன்";
                case "hi" -> "गला, थायरॉयड, स्वर रज्जु व मुख ऊतक संवेदनशीलता";
                case "te" -> "గొంతు, థైరాయిడ్, స్వర తంత్రులు & ముఖ కణజాల సున్నితత్వం";
                case "kn" -> "ಗಂಟಲು, ಥೈರಾಯ್ಡ್, ಧ್ವನಿ ತಂತುಗಳು ಮತ್ತು ಮುಖದ ಸಂವೇದನೆ";
                case "ml" -> "തൊണ്ട, തൈറോയ്ഡ്, സ്വരപേടകം, മുഖ സംവേദനക്ഷമത";
                default -> v;
            };
        }

        // 3. Head region (Aries/Mesha in 6th)
        if (lower.contains("head region") || lower.contains("cerebral") || lower.contains("mesha")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "தலைப்பகுதி, மூளை இரத்த ஓட்டம் மற்றும் தலைவலி உணர்திறன்";
                case "hi" -> "सिर का क्षेत्र, मस्तिष्क रक्त परिसंचरण व सिरदर्द संवेदनशीलता";
                case "te" -> "తల భాగం, మెదడు రక్త ప్రసరణ & తలనొప్పి సున్నితత్వం";
                case "kn" -> "ತಲೆಯ ಭಾಗ, ಮೆದುಳಿನ ರಕ್ತ ಪರಿಚಲನೆ & ತಲೆನೋವು";
                case "ml" -> "തല, തലച്ചോറിലെ രക്തചംക്രമണം, തലവേദന";
                default -> v;
            };
        }

        // 4. Kidney hydration (Venus as Roga Lord / in House 6/8)
        if (lower.contains("kidney") || lower.contains("venus as roga lord") || lower.contains("renal")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "சிறுநீரக நீர் சமநிலை, நாளமில்லா சுரப்பிகள் மற்றும் இனப்பெருக்க மண்டல நலம்";
                case "hi" -> "गुर्दे का जलयोजन, अंतःस्रावी संतुलन व प्रजनन स्वास्थ्य";
                case "te" -> "కిడ్నీ హైడ్రేషన్, ఎండోక్రైన్ బ్యాలెన్స్ & పునరుత్పత్తి వ్యవస్థ";
                case "kn" -> "ಮೂತ್ರಪಿಂಡದ ಸಮತೋಲನ, ಹಾರ್ಮೋನ್ ಸಮತೋಲನ & ಸಂತಾನೋತ್ಪತ್ತಿ ಕ್ಷೇಮ";
                case "ml" -> "വൃക്കകളുടെ പ്രവർത്തനം, ഹോർമോൺ സന്തുലിതാവസ്ഥ, പ്രജനന ആരോഗ്യം";
                default -> v;
            };
        }

        // 5. Cardiovascular / Sun in House 6/8/12 or Sun as Roga Lord
        if (lower.contains("cardiovascular") || lower.contains("sun as roga lord") || lower.contains("cardiac stamina")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "இதய நலம், கண் பார்வை தெளிவு மற்றும் எலும்பு தாது உறிஞ்சுதல்";
                case "hi" -> "हृदय स्वास्थ्य, नेत्र दृष्टि व अस्थि खनिज अवशोषण";
                case "te" -> "గుండె ఆరోగ్యం, కంటి చూపు స్పష్టత & ఎముకల ఖనిజ శోషణ";
                case "kn" -> "ಹೃದಯದ ಆರೋಗ್ಯ, ಕಂಟಿ ದೃಷ್ಟಿ & ಮೂಳೆಗಳ ಖನಿಜ ಹೀರಿಕೊಳ್ಳುವಿಕೆ";
                case "ml" -> "ഹൃദയാരോഗ്യം, കാഴ്ചശക്തി, അസ്ഥി ധാതുക്കളുടെ ആഗിരണം";
                default -> v;
            };
        }

        // 6. Lymphatic sluggishness / Moon in House 6/8/12 or Moon as Roga Lord
        if (lower.contains("lymphatic") || lower.contains("moon in house") || lower.contains("moon as roga lord")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "நிணநீர் சுழற்சி, திரவ தேக்கம் மற்றும் மனநிலை சமநிலை";
                case "hi" -> "लसीका परिसंचरण, जल प्रतिधारण व मानसिक संतुलन";
                case "te" -> "శోషరస ప్రసరణ, శరీర ద్రవాలు & మానసిక సమతుల్యత";
                case "kn" -> "ದುಗ್ಧನಾಳ ಪರಿಚಲನೆ, ದ್ರವ ಸಮತೋಲನ & ಮಾನಸಿಕ ಶಾಂತಿ";
                case "ml" -> "ലിംഫറ്റിക് പ്രവർത്തനം, ശരീരത്തിലെ ജലാംശം, മാനസിക സന്തുലിതാവസ്ഥ";
                default -> v;
            };
        }

        // 7. Enteric nervous system / Mercury in House 6/8/12 or Mercury as Roga Lord
        if (lower.contains("enteric nervous system") || lower.contains("skin barrier") || lower.contains("mercury")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "குடல் நரம்பு மண்டலம், தோல் பாதுகாப்பு மற்றும் செரிமான என்சைம்கள்";
                case "hi" -> "आंतों का तंत्रिका तंत्र, त्वचा सुरक्षा व पाचक एंजाइम";
                case "te" -> "జీర్ణ నాడీ వ్యవస్థ, చర్మ సంరక్షణ & జీర్ణ ఎంజైములు";
                case "kn" -> "ಜೀರ್ಣಾಂಗ ನರಮಂಡಲ, ಚರ್ಮ ರಕ್ಷಣೆ & ಜೀರ್ಣಕಾರಿ ಕಿಣ್ವಗಳು";
                case "ml" -> "ദഹന നാഡീവ്യൂഹം, ചർമ്മ സംരക്ഷണം, ദഹന എൻസൈമുകൾ";
                default -> v;
            };
        }

        // 8. Joints, chronic dryness / Saturn in House 6/8/12 or Saturn as Roga Lord
        if (lower.contains("joint mobility") || lower.contains("tendon flexibility") || lower.contains("saturn")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "மூட்டு இயக்கம், தசைநார் நெகிழ்வுத்தன்மை மற்றும் நரம்புத் தேய்மானம்";
                case "hi" -> "जोड़ों की गतिशीलता, कंडरा लचीलापन व तंत्रिका संवेदनशीलता";
                case "te" -> "కీళ్ల కదలిక, స్నాయువుల సరళత & నాడీ సున్నితత్వం";
                case "kn" -> "ಕೀಲುಗಳ ಚಲನಶೀಲತೆ, ಸ್ನಾಯುಗಳ ನಮ್ಯತೆ & ನರಗಳ ಸಂವೇದನೆ";
                case "ml" -> "സന്ധികളുടെ ചലനശേഷി, പേശീ വഴക്കം, നാഡീ ആരോഗ്യം";
                default -> v;
            };
        }

        // 9. Blood purification, inflammation / Mars in House 6/8/12 or Mars as Roga Lord
        if (lower.contains("blood purification") || lower.contains("muscular inflammation") || lower.contains("mars")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "இரத்த சுத்திகரிப்பு, தசை அழற்சி மற்றும் பித்த உஷ்ணம்";
                case "hi" -> "रक्त शुद्धि, मांसपेशियों में सूजन व पित्त संतुलन";
                case "te" -> "రక్త శుద్ధి, కండరాల వాపు & పిత్త వేడి";
                case "kn" -> "ರಕ್ತ ಶುದ್ಧೀಕರಣ, ಸ್ನಾಯು ಉರಿಯೂತ & ಪಿತ್ತ ಸಮತೋಲನ";
                case "ml" -> "രക്തശുദ്ധീകരണം, പേശിവീക്കം, പിത്ത ചൂട്";
                default -> v;
            };
        }

        // 10. Liver / Jupiter in House 6/8/12 or Jupiter as Roga Lord
        if (lower.contains("hepatic") || lower.contains("liver") || lower.contains("jupiter")) {
            return switch (lang.toLowerCase()) {
                case "ta" -> "கல்லீரல் வளர்சிதை மாற்றம், கொழுப்பு செரிமானம் மற்றும் தமனி ஆரோக்கியம்";
                case "hi" -> "यकृत (लिवर) चयापचय, वसा पाचन व धमनी स्वास्थ्य";
                case "te" -> "కాలేయ జీవక్రియ, కొవ్వుల జీర్ణం & రక్తనాళాల ఆరోగ్యం";
                case "kn" -> "ಯಕೃತ್ತಿನ ಚಯಾಪಚಯ, ಕೊಬ್ಬಿನ ಜೀರ್ಣಕ್ರಿಯೆ & ಅಪಧಮನಿ ಕ್ಷೇಮ";
                case "ml" -> "കരളിന്റെ പ്രവർത്തനം, കൊഴുപ്പ് ദഹനം, ധമനികളുടെ ആരോഗ്യം";
                default -> v;
            };
        }

        return v;
    }

    public static String translateClassification(String classification, String lang) {
        if (classification == null || classification.isBlank()) return "";
        return translate(classification, lang);
    }

    public static String translateLifespanRange(String lifespanRange, String lang) {
        if (lifespanRange == null || lifespanRange.isBlank()) return "";
        if ("en".equalsIgnoreCase(lang)) return lifespanRange;

        String yearsTrans = switch (lang.toLowerCase()) {
            case "ta" -> "ஆண்டுகள்";
            case "hi" -> "वर्ष";
            case "te" -> "సంవత్సరాలు";
            case "kn" -> "ವರ್ಷಗಳು";
            case "ml" -> "വർഷങ്ങൾ";
            default -> "Years";
        };

        return lifespanRange.replace("Years", yearsTrans).replace("years", yearsTrans);
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

            // Localize ~Age and dates
            String ageLoc = switch (lang.toLowerCase()) {
                case "ta" -> timeAndAge.replace("~Age", "~வயது").replace("to", "முதல்").replace(", ~வயது", " வரை, ~வயது");
                case "hi" -> timeAndAge.replace("~Age", "~आयु").replace("to", "से");
                case "te" -> timeAndAge.replace("~Age", "~వయస్సు").replace("to", "నుండి");
                case "kn" -> timeAndAge.replace("~Age", "~ವಯಸ್ಸು").replace("to", "ರಿಂದ");
                case "ml" -> timeAndAge.replace("~Age", "~വയസ്സ്").replace("to", "മുതൽ");
                default -> timeAndAge;
            };

            return switch (lang.toLowerCase()) {
                case "ta" -> p1 + " மகாதிசை - " + p2 + " புக்தி (" + ageLoc + ") முக்கிய மாரக/பாதக எச்சரிக்கைக் காலமாகும்.";
                case "hi" -> p1 + " महादशा - " + p2 + " भुक्ति (" + ageLoc + ") मुख्य मारक/बाधक सतर्कता काल है।";
                case "te" -> p1 + " మహాదశ - " + p2 + " భుక్తి (" + ageLoc + ") ప్రధాన మారక/బాధక అప్రమత్త కాలం.";
                case "kn" -> p1 + " ಮಹಾದಶಾ - " + p2 + " ಭುಕ್ತಿ (" + ageLoc + ") ಮುಖ್ಯ ಮಾರಕ/ಬಾಧಕ ಎಚ್ಚರಿಕೆಯ ಕಾಲಾವಧಿ.";
                case "ml" -> p1 + " മഹാദശ - " + p2 + " ഭുക്തി (" + ageLoc + ") പ്രധാന മാരക/ബാധക ജാഗ്രതാ കാലഘട്ടം.";
                default -> window;
            };
        }
        return window;
    }

    public static String translateRationale(String rationale, String lang) {
        if (rationale == null || rationale.isBlank()) return "";
        if ("en".equalsIgnoreCase(lang)) return rationale;

        String res = rationale;

        // Extract planets if pattern matches
        Pattern p = Pattern.compile("based on Lagna Lord\\s*\\(([^)]+)\\),\\s*8th Lord\\s*\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(rationale);
        String ll = "";
        String l8 = "";
        if (m.find()) {
            ll = translate(m.group(1), lang);
            l8 = translate(m.group(2), lang);
        }

        if (lang.equalsIgnoreCase("ta")) {
            res = res.replace("Determined via Parashara & Jaimini Ayurdaya based on Lagna Lord (" + (m.find(0) ? m.group(1) : "") + "), 8th Lord (" + (m.find(0) ? m.group(2) : "") + "), Moon, Saturn, and Hora Lagna modalities, refined with Kakshya Vriddhi and Shadbala life-force: ",
                    "பராசர & ஜைமினி ஆயுர் கணிதப்படி லக்னாதிபதி (" + ll + "), 8-ஆம் அதிபதி (" + l8 + "), சந்திரன், சனி மற்றும் ஹோரா லக்ன தத்துவங்களின் அடிப்படையில் நிர்ணயிக்கப்பட்ட ஆயுள்: ")
                     .replace("Consensus of Jaimini 3-Pair Method based on Lagna Lord (" + (m.find(0) ? m.group(1) : "") + "), 8th Lord (" + (m.find(0) ? m.group(2) : "") + "), Moon, Saturn, and Hora Lagna modalities, refined with Kakshya Vriddhi and Shadbala life-force: ",
                    "ஜைமினி 3-இணை முறை மற்றும் பராசர ஷட்பல ஆயுள் கணித முடிவு: ")
                     .replace("3-Pair Longevity Span consensus indicates", "3-இணை ஆயுர் கணித ஒருமித்த முடிவு:")
                     .replace("Poornayu", "பூர்ணாயுள் (Poornayu)")
                     .replace("Madhyayu", "மத்தியாயுள் (Madhyayu)")
                     .replace("Alpayu", "அல்பாயுள் (Alpayu)")
                     .replace("Jupiter benefic Kendra/Trikona placement confers Kakshya Vriddhi (+4 years).", "குரு பகவான் கேந்திர/திரிகோணத்தில் சுப பலத்துடன் இருப்பதால் கக்ஷ்ய விருத்தி (+4 ஆண்டுகள்) கூடுகிறது.")
                     .replace("Lagna Lord strong in own/exalted sign adds physical vitality (+4 years).", "லக்னாதிபதி ஆட்சி/உச்ச பலம் பெற்றுள்ளதால் உடல் ஆயுள் பலம் (+4 ஆண்டுகள்) கூடுகிறது.")
                     .replace("Ayushkaraka Saturn in Own/Exalted sign reinforces longevity (+4 years).", "ஆயுஷ்காரகனான சனி பகவான் ஆட்சி/உச்ச பலத்துடன் நீண்ட ஆயுளை (+4 ஆண்டுகள்) அருளுகிறார்.")
                     .replace("Lagna Lord in Dusthana (6/8/12) advises mindful health regimen.", "லக்னாதிபதி மறைவு ஸ்தானத்தில் இருப்பதால் ஆரோக்கியத்தில் விழிப்புணர்வு தேவைப்படுகிறது.")
                     .replace("Ayushkaraka Saturn possesses Neecha Bhanga (cancellation of debility into longevity stability).", "சனி பகவானுக்கு நீசபங்க ராஜயோகம் உள்ளதால் ஆயுள் நிலைத்தன்மை பெறுகிறது.")
                     .replace("Ayushkaraka Saturn in debility applies Kakshya Hrasa reduction (-5 years).", "சனி பகவான் நீசமாக உள்ளதால் கக்ஷ்ய ஹ்ராஸம் (-5 ஆண்டுகள்) ஏற்படுகிறது.")
                     .replace("Lagna Lord debilitated in Dusthana applies Kakshya Hrasa (-4 years).", "லக்னாதிபதி நீசமாக உள்ளதால் கக்ஷ்ய ஹ்ராஸம் (-4 ஆண்டுகள்) ஏற்படுகிறது.")
                     .replace("Lagna hemmed between malefics in 12th & 2nd (Papakarthari Yoga) cautions physical vitality (-3 years).", "லக்னம் பாபகர்த்தாரி யோகத்தில் உள்ளதால் உடலாரோக்கியத்தில் கூடுதல் கவனம் தேவைப்படுகிறது (-3 ஆண்டுகள்).");
        } else if (lang.equalsIgnoreCase("hi")) {
            res = res.replace("Determined via Parashara & Jaimini Ayurdaya based on Lagna Lord (" + (m.find(0) ? m.group(1) : "") + "), 8th Lord (" + (m.find(0) ? m.group(2) : "") + "), Moon, Saturn, and Hora Lagna modalities, refined with Kakshya Vriddhi and Shadbala life-force: ",
                    "पाराशर व जैमिनी आयुर्दाय पद्धति अनुसार लग्नेश (" + ll + "), अष्टमेश (" + l8 + "), चन्द्र, शनि व होरा लग्न आधार पर निर्धारित आयु: ")
                     .replace("Poornayu", "पूर्णायु (Poornayu)")
                     .replace("Madhyayu", "मध्‍यायु (Madhyayu)")
                     .replace("Alpayu", "अल्पायु (Alpayu)")
                     .replace("Jupiter benefic Kendra/Trikona placement confers Kakshya Vriddhi (+4 years).", "गुरु शुभ केंद्र/त्रिकोण में स्थित होकर कक्ष्या वृद्धि (+4 वर्ष) प्रदान करते हैं।")
                     .replace("Lagna Lord strong in own/exalted sign adds physical vitality (+4 years).", "लग्नेश स्व/उच्च राशि में बलिष्ठ होकर शारीरिक प्राणशक्ति (+4 वर्ष) बढ़ाते हैं।")
                     .replace("Ayushkaraka Saturn in Own/Exalted sign reinforces longevity (+4 years).", "आयुष्कारक शनि स्व/उच्च राशि में होकर दीर्घायु (+4 वर्ष) को सुदृढ़ करते हैं।")
                     .replace("Lagna Lord in Dusthana (6/8/12) advises mindful health regimen.", "लग्नेश दुस्थान में स्थित होने से स्वास्थ्य के प्रति सजगता आवश्यक है।")
                     .replace("Ayushkaraka Saturn possesses Neecha Bhanga (cancellation of debility into longevity stability).", "शनि को नीचभंग राजयोग प्राप्त होने से आयु स्थिरता मिलती है।")
                     .replace("Ayushkaraka Saturn in debility applies Kakshya Hrasa reduction (-5 years).", "शनि नीच राशि में होने से कक्ष्या ह्रास (-5 वर्ष) होता है।")
                     .replace("Lagna Lord debilitated in Dusthana applies Kakshya Hrasa (-4 years).", "लग्नेश दुस्थान में नीच होने से कक्ष्या ह्रास (-4 वर्ष) होता है।")
                     .replace("Lagna hemmed between malefics in 12th & 2nd (Papakarthari Yoga) cautions physical vitality (-3 years).", "लग्न पापकर्तरी योग में होने से शारीरिक स्वास्थ्य में सावधानी आवश्यक है (-3 वर्ष)।");
        } else if (lang.equalsIgnoreCase("te")) {
            res = res.replace("Determined via Parashara & Jaimini Ayurdaya based on Lagna Lord (" + (m.find(0) ? m.group(1) : "") + "), 8th Lord (" + (m.find(0) ? m.group(2) : "") + "), Moon, Saturn, and Hora Lagna modalities, refined with Kakshya Vriddhi and Shadbala life-force: ",
                    "పరాశర & జైమిని ఆయుర్దాయ పద్ధతి ప్రకారం లగ్నాధిపతి (" + ll + "), 8వ అధిపతి (" + l8 + "), చంద్రుడు, శని మరియు హోరా లగ్న ఆధారంగా నిర్ణయించిన ఆయుష్షు: ")
                     .replace("Poornayu", "పూర్ణాయుష్షు (Poornayu)")
                     .replace("Madhyayu", "మధ్యాయుష్షు (Madhyayu)")
                     .replace("Alpayu", "అల్పాయుష్షు (Alpayu)")
                     .replace("Jupiter benefic Kendra/Trikona placement confers Kakshya Vriddhi (+4 years).", "గురు గ్రహం కేంద్ర/త్రికోణంలో ఉండి కక్ష్యా వృద్ధిని (+4 సంవత్సరాలు) ప్రసాదిస్తుంది.")
                     .replace("Lagna Lord strong in own/exalted sign adds physical vitality (+4 years).", "లగ్నాధిపతి స్వ/ఉచ్ఛ క్షేత్రంలో ఉండి శరీర బలాన్ని (+4 సంవత్సరాలు) పెంచుతాడు.")
                     .replace("Ayushkaraka Saturn in Own/Exalted sign reinforces longevity (+4 years).", "ఆయుష్కారకుడు శని స్వ/ఉచ్ఛ క్షేత్రంలో ఉండి దీర్ఘాయుష్షును (+4 సంవత్సరాలు) బలోపేతం చేస్తాడు.")
                     .replace("Lagna Lord in Dusthana (6/8/12) advises mindful health regimen.", "లగ్నాధిపతి దుస్థానంలో (6/8/12) ఉండటం వల్ల ఆరోగ్యంపై జాగ్రత్త అవసరం.")
                     .replace("Ayushkaraka Saturn possesses Neecha Bhanga (cancellation of debility into longevity stability).", "శనికి నీచభంగ రాజయోగం ఉండటం వల్ల ఆయుష్షు స్థిరపడుతుంది.")
                     .replace("Ayushkaraka Saturn in debility applies Kakshya Hrasa reduction (-5 years).", "శని నీచంలో ఉండటం వల్ల కక్ష్యా హ్రాసం (-5 సంవత్సరాలు) వర్తిస్తుంది.")
                     .replace("Lagna Lord debilitated in Dusthana applies Kakshya Hrasa (-4 years).", "లగ్నాధిపతి దుస్థానంలో నీచంలో ఉండటం వల్ల కక్ష్యా హ్రాసం (-4 సంవత్సరాలు) వర్తిస్తుంది.")
                     .replace("Lagna hemmed between malefics in 12th & 2nd (Papakarthari Yoga) cautions physical vitality (-3 years).", "లగ్నం పాపకర్తరి యోగంలో ఉండటం వల్ల ఆరోగ్య జాగ్రత్త అవసరం (-3 సంవత్సరాలు).");
        } else if (lang.equalsIgnoreCase("kn")) {
            res = res.replace("Determined via Parashara & Jaimini Ayurdaya based on Lagna Lord (" + (m.find(0) ? m.group(1) : "") + "), 8th Lord (" + (m.find(0) ? m.group(2) : "") + "), Moon, Saturn, and Hora Lagna modalities, refined with Kakshya Vriddhi and Shadbala life-force: ",
                    "ಪರಾಶರ ಮತ್ತು ಜೈಮಿನಿ ಆಯುಷ್ಯ ನಿರ್ಣಯದಂತೆ ಲಗ್ನಾಧಿಪತಿ (" + ll + "), 8ನೇ ಅಧಿಪತಿ (" + l8 + "), ಚಂದ್ರ, ಶನಿ ಮತ್ತು ಹೋರಾ ಲಗ್ನದ ಆಧಾರದ ಮೇಲೆ ನಿರ್ಧರಿಸಿದ ಆಯುಷ್ಯ: ")
                     .replace("Poornayu", "ಪೂರ್ಣಾಯುಷ್ಯ (Poornayu)")
                     .replace("Madhyayu", "ಮಧ್ಯಾಯುಷ್ಯ (Madhyayu)")
                     .replace("Alpayu", "ಅಲ್ಪಾಯುಷ್ಯ (Alpayu)")
                     .replace("Jupiter benefic Kendra/Trikona placement confers Kakshya Vriddhi (+4 years).", "ಗುರು ಗ್ರಹ ಕೇಂದ್ರ/ತ್ರಿಕೋಣದಲ್ಲಿದ್ದು ಕಕ್ಷ್ಯಾ ವೃದ್ಧಿಯನ್ನು (+4 ವರ್ಷಗಳು) ಅನುಗ್ರಹಿಸುತ್ತಾನೆ.")
                     .replace("Lagna Lord strong in own/exalted sign adds physical vitality (+4 years).", "ಲಗ್ನಾಧಿಪತಿ ಸ್ವ/ಉಚ್ಛ ರಾಶಿಯಲ್ಲಿದ್ದು ದೈಹಿಕ ಚೈತನ್ಯವನ್ನು (+4 ವರ್ಷಗಳು) ಹೆಚ್ಚಿಸುತ್ತಾನೆ.")
                     .replace("Ayushkaraka Saturn in Own/Exalted sign reinforces longevity (+4 years).", "ಆಯುಷ್ಯಕಾರಕ ಶನಿ ಸ್ವ/ಉಚ್ಛ ರಾಶಿಯಲ್ಲಿದ್ದು ದೀರ್ಘಾಯುಷ್ಯವನ್ನು (+4 ವರ್ಷಗಳು) ಬಲಪಡಿಸುತ್ತಾನೆ.")
                     .replace("Lagna Lord in Dusthana (6/8/12) advises mindful health regimen.", "ಲಗ್ನಾಧಿಪತಿ ದುಃಸ್ಥಾನದಲ್ಲಿದ್ದು (6/8/12) ಆರೋಗ್ಯ ಕಾಳಜಿ ಅಗತ್ಯವಾಗಿದೆ.")
                     .replace("Ayushkaraka Saturn possesses Neecha Bhanga (cancellation of debility into longevity stability).", "ಶನಿಗೆ ನೀಚಭಂಗ ರಾಜಯೋಗವಿರುವುದರಿಂದ ಆಯುಷ್ಯ ಸ್ಥಿರತೆ ಪ್ರಾಪ್ತಿಯಾಗುತ್ತದೆ.")
                     .replace("Ayushkaraka Saturn in debility applies Kakshya Hrasa reduction (-5 years).", "ಶನಿ ನೀಚನಾಗಿರುವುದರಿಂದ ಕಕ್ಷ್ಯಾ ಹ್ರಾಸ (-5 ವರ್ಷಗಳು) ಉಂಟಾಗುತ್ತದೆ.")
                     .replace("Lagna Lord debilitated in Dusthana applies Kakshya Hrasa (-4 years).", "ಲಗ್ನಾಧಿಪತಿ ದುಃಸ್ಥಾನದಲ್ಲಿ ನೀಚನಾಗಿದ್ದರಿಂದ ಕಕ್ಷ್ಯಾ ಹ್ರಾಸ (-4 ವರ್ಷಗಳು) ಉಂಟಾಗುತ್ತದೆ.")
                     .replace("Lagna hemmed between malefics in 12th & 2nd (Papakarthari Yoga) cautions physical vitality (-3 years).", "ಲಗ್ನವು ಪಾಪಕರ್ತರಿ ಯೋಗದಲ್ಲಿದ್ದು ಆರೋಗ್ಯದ ಬಗ್ಗೆ ಎಚ್ಚರಿಕೆ ಅಗತ್ಯ (-3 ವರ್ಷಗಳು).");
        } else if (lang.equalsIgnoreCase("ml")) {
            res = res.replace("Determined via Parashara & Jaimini Ayurdaya based on Lagna Lord (" + (m.find(0) ? m.group(1) : "") + "), 8th Lord (" + (m.find(0) ? m.group(2) : "") + "), Moon, Saturn, and Hora Lagna modalities, refined with Kakshya Vriddhi and Shadbala life-force: ",
                    "പരാശര & ജൈമിനി ആയുർദായ രീതിയിൽ ലഗ്നാധിപൻ (" + ll + "), 8-ാം നാഥൻ (" + l8 + "), ചന്ദ്രൻ, ശനി, ഹോരാ ലഗ്നം എന്നിവയുടെ അടിസ്ഥാനത്തിൽ നിർണ്ണയിച്ച ആയുസ്സ്: ")
                     .replace("Poornayu", "പൂർണ്ണായുസ്സ് (Poornayu)")
                     .replace("Madhyayu", "മദ്ധ്യായുസ്സ് (Madhyayu)")
                     .replace("Alpayu", "അല്പായുസ്സ് (Alpayu)")
                     .replace("Jupiter benefic Kendra/Trikona placement confers Kakshya Vriddhi (+4 years).", "ഗുരു ശുഭ കേന്ദ്ര/ത്രികോണത്തിൽ സ്ഥിതി ചെയ്യുന്നതിനാൽ കക്ഷ്യാ വൃദ്ധി (+4 വർഷങ്ങൾ) നൽകുന്നു.")
                     .replace("Lagna Lord strong in own/exalted sign adds physical vitality (+4 years).", "ലഗ്നാധിപൻ സ്വ/ഉച്ച ക്ഷേത്രത്തിൽ ശക്തനായി ശാരീരിക ഓജസ്സ് (+4 വർഷങ്ങൾ) വർദ്ധിപ്പിക്കുന്നു.")
                     .replace("Ayushkaraka Saturn in Own/Exalted sign reinforces longevity (+4 years).", "ആയുഷ്കാരകനായ ശനി സ്വ/ഉച്ച ക്ഷേത്രത്തിൽ ദീർഘായുസ്സ് (+4 വർഷങ്ങൾ) ഉറപ്പാക്കുന്നു.")
                     .replace("Lagna Lord in Dusthana (6/8/12) advises mindful health regimen.", "ലഗ്നാധിപൻ ദുസ്ഥാനങ്ങളിൽ (6/8/12) നിൽക്കുന്നതിനാൽ ആരോഗ്യ ശ്രദ്ധ ആവശ്യമാണ്.")
                     .replace("Ayushkaraka Saturn possesses Neecha Bhanga (cancellation of debility into longevity stability).", "ശനിക്ക് നീചഭംഗ രാജയോഗമുള്ളതിനാൽ ആയുസ്സിന് സ്ഥിരത ലഭിക്കുന്നു.")
                     .replace("Lagna Lord debilitated in Dusthana applies Kakshya Hrasa (-4 years).", "ലഗ്നാധിപൻ ദുസ്ഥാനത്ത് നീചനായതിനാൽ കക്ഷ്യാ ഹ്രാസം (-4 വർഷങ്ങൾ) ബാധകമാകുന്നു.")
                     .replace("Lagna hemmed between malefics in 12th & 2nd (Papakarthari Yoga) cautions physical vitality (-3 years).", "ലഗ്നം പാപകർത്താരി യോഗത്തിലായതിനാൽ ആരോഗ്യ ശ്രദ്ധ ആവശ്യമാണ് (-3 വർഷങ്ങൾ).");
        }
        return res;
    }

    public static String translateDasaBhukthi(String dasaStr, String lang) {
        if (dasaStr == null || dasaStr.isBlank()) return "";
        if ("en".equalsIgnoreCase(lang)) return dasaStr;

        // Check if it's "Planet1 - Planet2" or "Planet1 / Planet2"
        String[] parts = dasaStr.split("[-/–—]");
        if (parts.length == 2) {
            String p1 = translatePlanetName(parts[0].trim(), lang);
            String p2 = translatePlanetName(parts[1].trim(), lang);
            return p1 + " - " + p2;
        }

        // Check if it's "Planet Mahadasa" or "Planet Dasa"
        String lower = dasaStr.toLowerCase().trim();
        if (lower.endsWith("mahadasa") || lower.endsWith("maha dasa")) {
            String p = dasaStr.substring(0, dasaStr.toLowerCase().lastIndexOf("maha")).trim();
            String pTrans = translatePlanetName(p, lang);
            String suffix = switch (lang.toLowerCase()) {
                case "ta" -> " மகாதிசை";
                case "hi" -> " महादशा";
                case "te" -> " మహాదశ";
                case "kn" -> " ಮಹಾದಶಾ";
                case "ml" -> " മഹാദശ";
                default -> " Mahadasa";
            };
            return pTrans + suffix;
        }
        if (lower.endsWith("dasa") || lower.endsWith("dasha")) {
            String p = dasaStr.substring(0, dasaStr.toLowerCase().lastIndexOf("das")).trim();
            String pTrans = translatePlanetName(p, lang);
            String suffix = switch (lang.toLowerCase()) {
                case "ta" -> " திசை";
                case "hi" -> " दशा";
                case "te" -> " దశ";
                case "kn" -> " ದಶಾ";
                case "ml" -> " ദശ";
                default -> " Dasa";
            };
            return pTrans + suffix;
        }

        return translatePlanetName(dasaStr.trim(), lang);
    }

    public static String translatePlanetName(String planetName, String lang) {
        if (planetName == null || planetName.isBlank()) return "";
        String clean = planetName.trim().replaceAll("(?i)(dasa|dasha|bhukthi|bhukti|period)", "").trim();
        String lower = clean.toLowerCase();
        return switch (lower) {
            case "sun", "surya" -> switch (lang.toLowerCase()) {
                case "ta" -> "சூரியன்";
                case "hi" -> "सूर्य";
                case "te" -> "సూర్యుడు";
                case "kn" -> "ಸೂರ್ಯ";
                case "ml" -> "സൂര്യൻ";
                default -> "Sun";
            };
            case "moon", "chandra" -> switch (lang.toLowerCase()) {
                case "ta" -> "சந்திரன்";
                case "hi" -> "चन्द्र";
                case "te" -> "చంద్రుడు";
                case "kn" -> "ಚಂದ್ರ";
                case "ml" -> "ചന്ദ്രൻ";
                default -> "Moon";
            };
            case "mars", "mangal", "kuja", "sevvaai", "sevvai" -> switch (lang.toLowerCase()) {
                case "ta" -> "செவ்வாய்";
                case "hi" -> "मंगल";
                case "te" -> "కుజుడు";
                case "kn" -> "ಮಂಗಳ";
                case "ml" -> "ചൊവ്വ";
                default -> "Mars";
            };
            case "mercury", "budha", "budh" -> switch (lang.toLowerCase()) {
                case "ta" -> "புதன்";
                case "hi" -> "बुध";
                case "te" -> "బుధుడు";
                case "kn" -> "ಬುಧ";
                case "ml" -> "ബുധൻ";
                default -> "Mercury";
            };
            case "jupiter", "guru", "brihaspati" -> switch (lang.toLowerCase()) {
                case "ta" -> "குரு";
                case "hi" -> "गुरु";
                case "te" -> "గురుడు";
                case "kn" -> "ಗುರು";
                case "ml" -> "വ്യാഴം";
                default -> "Jupiter";
            };
            case "venus", "shukra", "sukra" -> switch (lang.toLowerCase()) {
                case "ta" -> "சுக்கிரன்";
                case "hi" -> "शुक्र";
                case "te" -> "శుక్రుడు";
                case "kn" -> "ಶುಕ್ರ";
                case "ml" -> "ശുക്രൻ";
                default -> "Venus";
            };
            case "saturn", "shani", "sani" -> switch (lang.toLowerCase()) {
                case "ta" -> "சனி";
                case "hi" -> "शनि";
                case "te" -> "శని";
                case "kn" -> "ಶನಿ";
                case "ml" -> "ശനി";
                default -> "Saturn";
            };
            case "rahu" -> switch (lang.toLowerCase()) {
                case "ta" -> "ராகு";
                case "hi" -> "राहु";
                case "te" -> "రాహువు";
                case "kn" -> "ರಾಹು";
                case "ml" -> "രാഹു";
                default -> "Rahu";
            };
            case "ketu" -> switch (lang.toLowerCase()) {
                case "ta" -> "கேது";
                case "hi" -> "केतु";
                case "te" -> "కేతువు";
                case "kn" -> "ಕೇತು";
                case "ml" -> "കേതു";
                default -> "Ketu";
            };
            default -> clean;
        };
    }
}

