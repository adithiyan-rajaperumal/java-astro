package org.vedic.astro.util;

import java.util.HashMap;
import java.util.Map;

/**
 * High-performance 6-language astrological translation dictionary and helper
 * for PDF exports and backend localizations across Tamil (ta), Hindi (hi),
 * Telugu (te), Kannada (kn), Malayalam (ml), and English (en).
 */
public class AstrologicalTranslationHelper {

    private static final Map<String, Map<String, String>> DICT = new HashMap<>();

    static {
        // --- DEITIES ---
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

        // --- GEMSTONES ---
        add("Ruby (Manickam)", "மாணிக்கம் (Ruby)", "माणिक्य (Ruby)", "కెంపు (Ruby)", "ಮಾಣಿಕ್ಯ (Ruby)", "മാണിക്യം (Ruby)", "Ruby (Manickam)");
        add("Pearl (Muthu)", "முத்து (Pearl)", "मोती (Pearl)", "ముత్యం (Pearl)", "ಮುತ್ತು (Pearl)", "മുത്ത് (Pearl)", "Pearl (Muthu)");
        add("Red Coral (Pavalam)", "பவளம் (Red Coral)", "मूंगा (Red Coral)", "పగడము (Red Coral)", "ಹವಳ (Red Coral)", "പവിഴം (Red Coral)", "Red Coral (Pavalam)");
        add("Emerald (Maragatham)", "மரகதம் (Emerald)", "पन्ना (Emerald)", "మరకతం (Emerald)", "ಪಚ್ಚೆ (Emerald)", "മരതകം (Emerald)", "Emerald (Maragatham)");
        add("Yellow Sapphire (Pushparagam)", "புஷ்பராகம் (Yellow Sapphire)", "पुखराज (Yellow Sapphire)", "పుష్యరాగం (Yellow Sapphire)", "ಪುಷ್ಯರಾಗ (Yellow Sapphire)", "പുഷ്യരാഗം (Yellow Sapphire)", "Yellow Sapphire (Pushparagam)");
        add("Diamond (Vairam)", "வைரம் (Diamond)", "हीरा (Diamond)", "వజ్రం (Diamond)", "ವಜ್ರ (Diamond)", "വൈരം (Diamond)", "Diamond (Vairam)");
        add("Blue Sapphire (Neelam)", "நீலம் (Blue Sapphire)", "नीलम (Blue Sapphire)", "నీలం (Blue Sapphire)", "ನೀಲಂ (Blue Sapphire)", "നീലക്കല്ല് (Blue Sapphire)", "Blue Sapphire (Neelam)");
        add("Hessonite / Gomed (Gomedhakam)", "கோமேதகம் (Hessonite)", "गोमेद (Hessonite)", "గోమేధికం (Hessonite)", "ಗೋಮೇಧಿಕ (Hessonite)", "ഗോമേദകം (Hessonite)", "Hessonite / Gomed");
        add("Cat's Eye (Vaidooryam)", "வைடூரியம் (Cat's Eye)", "लहसुनिया (Cat's Eye)", "వైడూర్యం (Cat's Eye)", "ವೈಡೂರ್ಯ (Cat's Eye)", "വൈഡൂര്യം (Cat's Eye)", "Cat's Eye (Vaidooryam)");

        // --- METALS ---
        add("Gold", "தங்கம் (Gold)", "स्वर्ण / सोना (Gold)", "బంగారం (Gold)", "ಚಿನ್ನ (Gold)", "സ്വർണ്ണം (Gold)", "Gold");
        add("Silver", "வெள்ளி (Silver)", "चांदी (Silver)", "వెండి (Silver)", "ಬೆಳ್ಳಿ (Silver)", "വെള്ളി (Silver)", "Silver");
        add("Copper", "செம்பு (Copper)", "तांबा (Copper)", "రాగి (Copper)", "ತಾಮ್ರ (Copper)", "ചെമ്പ് (Copper)", "Copper");
        add("Panchadhatu", "ஐம்பொன் (Panchadhatu)", "पंचधातु (Panchadhatu)", "పంచలోహం (Panchadhatu)", "ಪಂಚಲೋಹ (Panchadhatu)", "പഞ്ചലോഹം (Panchadhatu)", "Panchadhatu");
        add("Iron / Lead", "இரும்பு / ஈயம்", "लोहा / सीसा", "ఇనుము / సీసం", "ಕಬ್ಬಿಣ / ಸೀಸ", "ഇരുമ്പ് / ഈയം", "Iron / Lead");

        // --- FINGERS ---
        add("Ring Finger", "மோதிர விரல்", "अनामिका (Ring Finger)", "ఉంగరపు వేలు", "ಉಂಗುರದ ಬೆರಳು", "മോതിരവിരൽ", "Ring Finger");
        add("Index Finger", "ஆள்காட்டி விரல்", "तर्जनी (Index Finger)", "చూపుడు వేలు", "ತೋರುಬೆರಳು", "ചൂണ്ടുവിരൽ", "Index Finger");
        add("Little Finger", "சுண்டு விரல்", "कनिष्ठिका (Little Finger)", "చిటికెన వేలు", "ಕಿರುಬೆರಳು", "ചെറുവിരൽ", "Little Finger");
        add("Middle Finger", "நடு விரல்", "मध्यमा (Middle Finger)", "మధ్య వేలు", "ಮಧ್ಯದ ಬೆರಳು", "നടുവിരൽ", "Middle Finger");

        // --- DIRECTIONS ---
        add("East", "கிழக்கு", "पूर्व (East)", "తూర్పు", "ಪೂರ್ವ", "കിഴക്ക്", "East");
        add("West", "மேற்கு", "पश्चिम (West)", "పడమర", "ಪಶ್ಚಿಮ", "പടിഞ്ഞാറ്", "West");
        add("North", "வடக்கு", "उत्तर (North)", "ఉత్తరం", "ಉತ್ತರ", "വടക്ക്", "North");
        add("South", "தெற்கு", "दक्षिण (South)", "దక్షిణం", "ದಕ್ಷಿಣ", "തെക്ക്", "South");
        add("North-East (Ishanya)", "வடகிழக்கு (ஈசான்யம்)", "ईशान कोण (उत्तर-पूर्व)", "ఈశాన్యం (ఉత్తర-తూర్పు)", "ಈಶಾನ್ಯ (ಉತ್ತರ-ಪೂರ್ವ)", "വടക്കുകിഴക്ക് (ഈശാനകോൺ)", "North-East (Ishanya)");
        add("South-East (Agni)", "தென்கிழக்கு (அக்னி)", "आग्नेय कोण (दक्षिण-पूर्व)", "ఆగ్నేయం (దక్షిణ-తూర్పు)", "ಆಗ್ನೇಯ (ದಕ್ಷಿಣ-ಪೂರ್ವ)", "തെക്കുകിഴക്ക് (ആഗ്നേയകോൺ)", "South-East (Agni)");
        add("South-West (Niruthi)", "தென்மேற்கு (நிருதி)", "नैऋत्य कोण (दक्षिण-पश्चिम)", "నైరుతి (దక్షిణ-పడమర)", "ನೈಋತ್ಯ (ದಕ್ಷಿಣ-ಪಶ್ಚಿಮ)", "തെക്കുപടിഞ്ഞാറ് (നിര്യതികോൺ)", "South-West (Niruthi)");
        add("North-West (Vayu)", "வடமேற்கு (வாயு)", "वायव्य कोण (उत्तर-पश्चिम)", "వాయవ్యం (ఉత్తర-పడమర)", "ವಾಯವ್ಯ (ಉತ್ತರ-ಪಶ್ಚಿಮ)", "വടക്കുപടിഞ്ഞാറ് (വായുകോൺ)", "North-West (Vayu)");

        // --- PLANETS ---
        add("Sun", "சூரியன்", "सूर्य", "సూర్యుడు", "ಸೂರ್ಯ", "സൂര്യൻ", "Sun");
        add("Moon", "சந்திரன்", "चन्द्र", "చంద్రుడు", "ಚಂದ್ರ", "ചന്ദ്രൻ", "Moon");
        add("Mars", "செவ்வாய்", "मंगल", "కుజుడు", "ಮಂಗಳ", "ചൊവ്വ", "Mars");
        add("Mercury", "புதன்", "बुध", "బుధుడు", "ಬುಧ", "ബുധൻ", "Mercury");
        add("Jupiter", "குரு", "बृहस्पति / गुरु", "గురుడు", "ಗುರು", "വ്യാഴം / ഗുരു", "Jupiter");
        add("Venus", "சுக்கிரன்", "शुक्र", "శుక్రుడు", "ಶುಕ್ರ", "ശുക്രൻ", "Venus");
        add("Saturn", "சனி", "शनि", "శని", "ಶನಿ", "ശനി", "Saturn");
        add("Rahu", "ராகு", "राहु", "రాహువు", "ರಾಹು", "രാഹു", "Rahu");
        add("Ketu", "கேது", "केतु", "కేతువు", "ಕೇತು", "കേതു", "Ketu");

        // --- LONGEVITY CLASSIFICATIONS ---
        add("Poornayu", "பூர்ணாயுள் (Poornayu: 75+ ஆண்டுகள்)", "पूर्णायु (75+ वर्ष)", "పూర్ణాయుష్షు (75+ సంవత్సరాలు)", "ಪೂರ್ಣಾಯುಷ್ಯ (75+ ವರ್ಷಗಳು)", "പൂർണ്ണായുസ്സ് (75+ വർഷങ്ങൾ)", "Poornayu (Full Longevity: 75+ Yrs)");
        add("Madhyayu", "மத்தியாயுள் (Madhyayu: 36–75 ஆண்டுகள்)", "मध्‍यायु (36–75 वर्ष)", "మధ్యాయుష్షు (36–75 సంవత్సరాలు)", "ಮಧ್ಯಾಯುಷ್ಯ (36–75 ವರ್ಷಗಳು)", "മദ്ധ്യായുസ്സ് (36–75 വർഷങ്ങൾ)", "Madhyayu (Medium Longevity: 36–75 Yrs)");
        add("Alpayu", "அல்பாயுள் (Alpayu: 0–35 ஆண்டுகள்)", "अल्पायु (0–35 वर्ष)", "అల్పాయుష్షు (0–35 సంవత్సరాలు)", "ಅಲ್ಪಾಯುಷ್ಯ (0–35 ವರ್ಷಗಳು)", "അല്പായുസ്സ് (0–35 വർഷങ്ങൾ)", "Alpayu (Short Longevity: 0–35 Yrs)");
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

        // Fallback partial matching
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
}
