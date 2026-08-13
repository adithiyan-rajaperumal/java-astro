package org.vedic.astro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vedic.astro.dto.DiagnosticsDTO;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.PlanetDignityUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AstrologyDiagnosticsService {

    private final TranslationService ts;

    public DiagnosticsDTO runHoroscopeDiagnostics(Map<String, PlanetaryPosition> d1Map) {
        List<DiagnosticsDTO.YogaDetail> yogas = new ArrayList<>();
        List<DiagnosticsDTO.DoshaDetail> doshams = new ArrayList<>();
        List<String> specs = new ArrayList<>();

        doshams.add(evaluateSevvaiDosham(d1Map));
        doshams.add(evaluateKalaSarpaDosham(d1Map));
        doshams.add(evaluateSarpamDosham(d1Map));
        doshams.add(evaluatePithruDosham(d1Map));
        doshams.add(evaluatePutraDosham(d1Map));
        doshams.add(evaluateKalathiraDosham(d1Map));
        doshams.add(evaluateShaniDosham(d1Map));
        doshams.add(evaluateGuruChandalaDosham(d1Map));
        doshams.add(evaluateAngarakDosham(d1Map));
        doshams.add(evaluatePunarphooDosham(d1Map));

        evaluateYogas(d1Map, yogas);

        return DiagnosticsDTO.builder().activeYogas(yogas).discoveredDoshams(doshams).horoscopicSpecialities(specs).build();
    }

    // =========================================================================
    // 1. SEVVAI / KUJA DOSHAM (Manglik Affliction - Triple Reference Frame)
    // =========================================================================
    private DiagnosticsDTO.DoshaDetail evaluateSevvaiDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        PlanetaryPosition moon = d1Map.get("Moon");
        PlanetaryPosition venus = d1Map.get("Venus");
        PlanetaryPosition mars = d1Map.get("Mars");
        PlanetaryPosition jupiter = d1Map.get("Jupiter");

        if (mars == null || lagna == null) {
            return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.sevvai")).detected(false).build();
        }

        int marsFromLagna = PlanetDignityUtils.getHouseFromLagna(mars.getSignNumber(), lagna.getSignNumber());
        int marsFromMoon = moon != null ? PlanetDignityUtils.getHouseFromLagna(mars.getSignNumber(), moon.getSignNumber()) : 1;
        int marsFromVenus = venus != null ? PlanetDignityUtils.getHouseFromLagna(mars.getSignNumber(), venus.getSignNumber()) : 1;

        boolean detected = isKujaDoshaHouse(marsFromLagna) || isKujaDoshaHouse(marsFromMoon) || isKujaDoshaHouse(marsFromVenus);

        boolean nullified = false;
        String reason = null;

        if (detected) {
            int mSign = mars.getSignNumber();
            int jSign = jupiter != null ? jupiter.getSignNumber() : 0;
            int vSign = venus != null ? venus.getSignNumber() : 0;
            int lagnaSign = lagna.getSignNumber();

            // 1. Cancer & Leo Lagna Yogakaraka Exemption
            if (lagnaSign == 4 || lagnaSign == 5) {
                nullified = true;
                reason = "Cancer/Leo Yogakaraka Mars Exemption (கடக/சிம்ம லக்ன யோககாரகன் செவ்வாய் விலக்கு)";
            }
            // 2. 11th House Upachaya placement exemption
            else if (marsFromLagna == 11) {
                nullified = true;
                reason = "11th House Upachaya Exemption (11-ஆம் இட லாப செவ்வாய் விலக்கு)";
            }
            // 3. Own sign / Exalted / Debilitated in Cancer
            else if (PlanetDignityUtils.isOwnSign("Mars", mSign) || PlanetDignityUtils.isExalted("Mars", mSign) || mSign == 4) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.own_exalted");
            }
            // 4. Jupiter / Venus aspect or conjunction
            else if (mSign == jSign || (jupiter != null && PlanetDignityUtils.isAspecting("Jupiter", jSign, mSign))) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.jupiter_aspect");
            } else if (mSign == vSign || (venus != null && PlanetDignityUtils.isAspecting("Venus", vSign, mSign))) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.venus_aspect");
            }
            // 5. Chandra-Mangala conjunction
            else if (moon != null && mSign == moon.getSignNumber()) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.benefic_sign");
            }
            // 6. Leo, Sagittarius, or Aquarius sign exemption
            else if (mSign == 5 || mSign == 9 || mSign == 11) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.benefic_sign");
            }
            // 7. Classical House-Sign pairs
            else if (marsFromLagna == 2 && (mSign == 3 || mSign == 6 || mSign == 10 || mSign == 11)) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.house_sign_exemption");
            } else if (marsFromLagna == 4 && (mSign == 1 || mSign == 8 || mSign == 2 || mSign == 7)) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.house_sign_exemption");
            } else if (marsFromLagna == 7 && (mSign == 4 || mSign == 10 || mSign == 2 || mSign == 7)) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.house_sign_exemption");
            } else if (marsFromLagna == 8 && (mSign == 9 || mSign == 12 || mSign == 3 || mSign == 6)) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.house_sign_exemption");
            } else if (marsFromLagna == 12 && (mSign == 2 || mSign == 7 || mSign == 9 || mSign == 12)) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.house_sign_exemption");
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.sevvai"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.high")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.sevvai") : null)
                .build();
    }

    // =========================================================================
    // 2. KALASARPA & SARPAM DOSHAM
    // =========================================================================
    private DiagnosticsDTO.DoshaDetail evaluateKalaSarpaDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition rahu = d1Map.get("Rahu");
        PlanetaryPosition ketu = d1Map.get("Ketu");
        PlanetaryPosition jupiter = d1Map.get("Jupiter");
        PlanetaryPosition lagna = d1Map.get("Lagna");

        if (rahu == null || ketu == null || lagna == null) {
            return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.kalasarpa")).detected(false).build();
        }

        int lagnaSign = lagna.getSignNumber();
        double rLong = rahu.getAbsoluteLongitude();
        double kLong = ketu.getAbsoluteLongitude();
        double mn = Math.min(rLong, kLong);
        double mx = Math.max(rLong, kLong);

        boolean inside = true;
        boolean outside = true;
        boolean planetConjunct = false;

        for (var e : d1Map.entrySet()) {
            if ("Lagna".equals(e.getKey()) || "Rahu".equals(e.getKey()) || "Ketu".equals(e.getKey()) ||
                "Uranus".equals(e.getKey()) || "Neptune".equals(e.getKey()) || "Pluto".equals(e.getKey())) continue;
            double p = e.getValue().getAbsoluteLongitude();
            int sign = e.getValue().getSignNumber();
            if (p < mn || p > mx) inside = false;
            if (p > mn && p < mx) outside = false;
            if (sign == rahu.getSignNumber() || sign == ketu.getSignNumber()) planetConjunct = true;
        }

        boolean detected = inside || outside;
        boolean nullified = false;
        String reason = null;

        if (detected) {
            int rH = PlanetDignityUtils.getHouseFromLagna(rahu.getSignNumber(), lagnaSign);
            int kH = PlanetDignityUtils.getHouseFromLagna(ketu.getSignNumber(), lagnaSign);

            if (planetConjunct) {
                nullified = true;
                reason = ts.getLabel("nullification.kalasarpa.conjunct");
            } else if (jupiter != null && (PlanetDignityUtils.isAspecting("Jupiter", jupiter.getSignNumber(), rahu.getSignNumber()) ||
                    PlanetDignityUtils.isAspecting("Jupiter", jupiter.getSignNumber(), ketu.getSignNumber()))) {
                nullified = true;
                reason = ts.getLabel("nullification.kalasarpa.jupiter_aspect");
            } else if (rH == 1 || rH == 4 || rH == 7 || rH == 10 || rH == 5 || rH == 9 || kH == 1 || kH == 4 || kH == 7 || kH == 10 || kH == 5 || kH == 9) {
                nullified = true;
                reason = ts.getLabel("nullification.sarpam.own_exalted");
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.kalasarpa"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.high")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.kalasarpa") : null)
                .build();
    }

    private DiagnosticsDTO.DoshaDetail evaluateSarpamDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        PlanetaryPosition rahu = d1Map.get("Rahu");
        PlanetaryPosition ketu = d1Map.get("Ketu");
        PlanetaryPosition jupiter = d1Map.get("Jupiter");
        PlanetaryPosition venus = d1Map.get("Venus");
        PlanetaryPosition mercury = d1Map.get("Mercury");

        if (lagna == null || rahu == null || ketu == null) {
            return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.sarpam")).detected(false).build();
        }

        int lagnaSign = lagna.getSignNumber();
        int rahuH = PlanetDignityUtils.getHouseFromLagna(rahu.getSignNumber(), lagnaSign);
        int ketuH = PlanetDignityUtils.getHouseFromLagna(ketu.getSignNumber(), lagnaSign);

        // Nodes in 1, 2, 5, 7, 8 form Nodal Dosham (3, 6, 11 is Upachaya growth)
        boolean detected = (rahuH == 1 || rahuH == 2 || rahuH == 5 || rahuH == 7 || rahuH == 8 ||
                           ketuH == 1 || ketuH == 2 || ketuH == 5 || ketuH == 7 || ketuH == 8);
        boolean nullified = false;
        String reason = null;

        if (detected) {
            int rSign = rahu.getSignNumber();
            int kSign = ketu.getSignNumber();
            int jSign = jupiter != null ? jupiter.getSignNumber() : 0;

            if (jupiter != null && (jSign == rSign || jSign == kSign || PlanetDignityUtils.isAspecting("Jupiter", jSign, rSign) || PlanetDignityUtils.isAspecting("Jupiter", jSign, kSign))) {
                nullified = true;
                reason = ts.getLabel("nullification.sarpam.jupiter_aspect");
            } else if (PlanetDignityUtils.isOwnSign("Rahu", rSign) || PlanetDignityUtils.isExalted("Rahu", rSign) ||
                       PlanetDignityUtils.isOwnSign("Ketu", kSign) || PlanetDignityUtils.isExalted("Ketu", kSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.sarpam.own_exalted");
            } else if (venus != null && (venus.getSignNumber() == rSign || venus.getSignNumber() == kSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.pithru.benefic");
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.sarpam"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.medium")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.sarpam") : null)
                .build();
    }

    // =========================================================================
    // 3. PITRU, PUTRA, KALATHRA, SHANI DOSHAMS
    // =========================================================================
    private DiagnosticsDTO.DoshaDetail evaluatePithruDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        PlanetaryPosition sun = d1Map.get("Sun");
        PlanetaryPosition rahu = d1Map.get("Rahu");
        PlanetaryPosition ketu = d1Map.get("Ketu");
        PlanetaryPosition saturn = d1Map.get("Saturn");
        PlanetaryPosition jupiter = d1Map.get("Jupiter");

        if (lagna == null || sun == null) {
            return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.pithru")).detected(false).build();
        }

        int lagnaSign = lagna.getSignNumber();
        int sunSign = sun.getSignNumber();
        int sunH = PlanetDignityUtils.getHouseFromLagna(sunSign, lagnaSign);

        boolean sunAfflicted = (rahu != null && sunSign == rahu.getSignNumber()) ||
                               (ketu != null && sunSign == ketu.getSignNumber()) ||
                               (saturn != null && (sunSign == saturn.getSignNumber() || PlanetDignityUtils.isAspecting("Saturn", saturn.getSignNumber(), sunSign)));
        boolean detected = (sunH == 9 || sunH == 1 || sunH == 5) && sunAfflicted;

        boolean nullified = false;
        String reason = null;

        if (detected) {
            int jSign = jupiter != null ? jupiter.getSignNumber() : 0;
            if (jupiter != null && (jSign == sunSign || PlanetDignityUtils.isAspecting("Jupiter", jSign, sunSign))) {
                nullified = true;
                reason = ts.getLabel("nullification.pithru.jupiter_aspect");
            } else if (PlanetDignityUtils.isOwnSign("Sun", sunSign) || PlanetDignityUtils.isExalted("Sun", sunSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.pithru.own_exalted");
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.pithru"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.medium")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.pithru") : null)
                .build();
    }

    private DiagnosticsDTO.DoshaDetail evaluatePutraDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        if (lagna == null) return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.putra")).detected(false).build();

        int lagnaSign = lagna.getSignNumber();
        int fifthHouseSign = ((lagnaSign + 4 - 1) % 12) + 1;

        boolean maleficInFifth = (d1Map.get("Saturn") != null && d1Map.get("Saturn").getSignNumber() == fifthHouseSign) ||
                                 (d1Map.get("Rahu") != null && d1Map.get("Rahu").getSignNumber() == fifthHouseSign) ||
                                 (d1Map.get("Ketu") != null && d1Map.get("Ketu").getSignNumber() == fifthHouseSign) ||
                                 (d1Map.get("Mars") != null && d1Map.get("Mars").getSignNumber() == fifthHouseSign);

        boolean detected = maleficInFifth;
        boolean nullified = false;
        String reason = null;

        if (detected) {
            PlanetaryPosition jup = d1Map.get("Jupiter");
            String lord5 = PlanetDignityUtils.getSignLord(fifthHouseSign);
            PlanetaryPosition pLord5 = d1Map.get(lord5);

            if (jup != null && (jup.getSignNumber() == fifthHouseSign || PlanetDignityUtils.isAspecting("Jupiter", jup.getSignNumber(), fifthHouseSign))) {
                nullified = true;
                reason = ts.getLabel("nullification.putra.jupiter");
            } else if (pLord5 != null && (PlanetDignityUtils.isOwnSign(lord5, pLord5.getSignNumber()) || PlanetDignityUtils.isExalted(lord5, pLord5.getSignNumber()))) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.own_exalted");
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.putra"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.medium")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.putra") : null)
                .build();
    }

    private DiagnosticsDTO.DoshaDetail evaluateKalathiraDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        if (lagna == null) return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.kalathira")).detected(false).build();

        int lagnaSign = lagna.getSignNumber();
        int seventhHouseSign = ((lagnaSign + 6 - 1) % 12) + 1;

        boolean maleficInSeventh = (d1Map.get("Saturn") != null && d1Map.get("Saturn").getSignNumber() == seventhHouseSign) ||
                                   (d1Map.get("Rahu") != null && d1Map.get("Rahu").getSignNumber() == seventhHouseSign) ||
                                   (d1Map.get("Ketu") != null && d1Map.get("Ketu").getSignNumber() == seventhHouseSign) ||
                                   (d1Map.get("Mars") != null && d1Map.get("Mars").getSignNumber() == seventhHouseSign);

        boolean detected = maleficInSeventh;
        boolean nullified = false;
        String reason = null;

        if (detected) {
            PlanetaryPosition ven = d1Map.get("Venus");
            PlanetaryPosition jup = d1Map.get("Jupiter");
            String lord7 = PlanetDignityUtils.getSignLord(seventhHouseSign);
            PlanetaryPosition pLord7 = d1Map.get(lord7);

            if (ven != null && (PlanetDignityUtils.isOwnSign("Venus", ven.getSignNumber()) || PlanetDignityUtils.isExalted("Venus", ven.getSignNumber()))) {
                nullified = true;
                reason = ts.getLabel("nullification.kalathira.venus_strong");
            } else if (jup != null && (jup.getSignNumber() == seventhHouseSign || PlanetDignityUtils.isAspecting("Jupiter", jup.getSignNumber(), seventhHouseSign))) {
                nullified = true;
                reason = ts.getLabel("nullification.kalathira.jupiter_aspect");
            } else if (pLord7 != null && (PlanetDignityUtils.isOwnSign(lord7, pLord7.getSignNumber()) || PlanetDignityUtils.isExalted(lord7, pLord7.getSignNumber()))) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.own_exalted");
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.kalathira"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.high")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.kalathira") : null)
                .build();
    }

    private DiagnosticsDTO.DoshaDetail evaluateShaniDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        PlanetaryPosition saturn = d1Map.get("Saturn");
        if (lagna == null || saturn == null) return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.shani")).detected(false).build();

        int lagnaSign = lagna.getSignNumber();
        int satSign = saturn.getSignNumber();
        int satH = PlanetDignityUtils.getHouseFromLagna(satSign, lagnaSign);

        boolean detected = (satH == 1 || satH == 4 || satH == 7 || satH == 8 || satH == 10 || satH == 12);
        boolean nullified = false;
        String reason = null;

        if (detected) {
            if (PlanetDignityUtils.isOwnSign("Saturn", satSign) || PlanetDignityUtils.isExalted("Saturn", satSign)) {
                nullified = true;
                reason = "Sasa Yoga / Exalted Saturn Cancellation (சச யோக / உச்ச சுவக்ஷேத்திர பலம்)";
            } else if (lagnaSign == 2 || lagnaSign == 7) { // Yogakaraka for Taurus and Libra
                nullified = true;
                reason = ts.getLabel("nullification.shani.yogakaraka");
            } else if (d1Map.get("Jupiter") != null && PlanetDignityUtils.isAspecting("Jupiter", d1Map.get("Jupiter").getSignNumber(), satSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.shani.jupiter_aspect");
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.shani"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.medium")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.shani") : null)
                .build();
    }

    // =========================================================================
    // 4. NEW CLASSICAL DOSHAMS: GURU-CHANDALA, ANGARAK, PUNARPHOO
    // =========================================================================
    private DiagnosticsDTO.DoshaDetail evaluateGuruChandalaDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition jupiter = d1Map.get("Jupiter");
        PlanetaryPosition rahu = d1Map.get("Rahu");
        PlanetaryPosition ketu = d1Map.get("Ketu");
        PlanetaryPosition lagna = d1Map.get("Lagna");

        if (jupiter == null || rahu == null || ketu == null || lagna == null) {
            return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.guru_chandala")).detected(false).build();
        }

        int jSign = jupiter.getSignNumber();
        int rSign = rahu.getSignNumber();
        int kSign = ketu.getSignNumber();
        int jHouse = PlanetDignityUtils.getHouseFromLagna(jSign, lagna.getSignNumber());

        boolean detected = (jSign == rSign || jSign == kSign);
        boolean nullified = false;
        String reason = null;

        if (detected) {
            // Nullification: Jupiter in Cancer/Sagittarius/Pisces, or 5th/9th Gyan Yoga conversion
            if (PlanetDignityUtils.isOwnSign("Jupiter", jSign) || PlanetDignityUtils.isExalted("Jupiter", jSign)) {
                nullified = true;
                reason = "Jupiter Exalted/Own Sign Cancellation (குரு உச்சம்/ஆட்சி பெற்றதால் சுப யோகமாக மாறுதல்)";
            } else if (jHouse == 5 || jHouse == 9) {
                nullified = true;
                reason = "5th/9th Trikona Gyan Yoga Conversion (திரிகோண ஸ்தானத்தில் அமைந்ததால் ஞான யோகமாக மாற்றம்)";
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.guru_chandala"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.medium")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.guru_chandala") : null)
                .build();
    }

    private DiagnosticsDTO.DoshaDetail evaluateAngarakDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition mars = d1Map.get("Mars");
        PlanetaryPosition rahu = d1Map.get("Rahu");
        PlanetaryPosition ketu = d1Map.get("Ketu");
        PlanetaryPosition lagna = d1Map.get("Lagna");

        if (mars == null || rahu == null || ketu == null || lagna == null) {
            return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.angarak")).detected(false).build();
        }

        int mSign = mars.getSignNumber();
        int rSign = rahu.getSignNumber();
        int kSign = ketu.getSignNumber();
        int mHouse = PlanetDignityUtils.getHouseFromLagna(mSign, lagna.getSignNumber());

        boolean detected = (mSign == rSign || mSign == kSign);
        boolean nullified = false;
        String reason = null;

        if (detected) {
            // Nullification: Formed in 3, 6, 11 Upachaya (converts to Shatru Jaya competitive courage)
            if (mHouse == 3 || mHouse == 6 || mHouse == 11) {
                nullified = true;
                reason = "Upachaya House 3/6/11 Shatru Jaya Conversion (உபசய ஸ்தானத்தில் அமைந்ததால் சத்ரு ஜெய யோகமாக மாற்றம்)";
            } else if (d1Map.get("Jupiter") != null && PlanetDignityUtils.isAspecting("Jupiter", d1Map.get("Jupiter").getSignNumber(), mSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.jupiter_aspect");
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.angarak"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.medium")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.angarak") : null)
                .build();
    }

    private DiagnosticsDTO.DoshaDetail evaluatePunarphooDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition saturn = d1Map.get("Saturn");
        PlanetaryPosition moon = d1Map.get("Moon");
        PlanetaryPosition jupiter = d1Map.get("Jupiter");

        if (saturn == null || moon == null) {
            return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.punarphoo")).detected(false).build();
        }

        int satSign = saturn.getSignNumber();
        int moonSign = moon.getSignNumber();

        boolean conjunct = (satSign == moonSign);
        boolean mutualAspect = ((satSign - moonSign + 12) % 12 == 6);
        boolean satAspectingMoon = PlanetDignityUtils.isAspecting("Saturn", satSign, moonSign);

        boolean detected = conjunct || mutualAspect || satAspectingMoon;
        boolean nullified = false;
        String reason = null;

        if (detected) {
            if (jupiter != null && (PlanetDignityUtils.isAspecting("Jupiter", jupiter.getSignNumber(), moonSign) || jupiter.getSignNumber() == moonSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.kalathira.jupiter_aspect");
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.punarphoo"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.medium")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.punarphoo") : null)
                .build();
    }

    // =========================================================================
    // 5. CLASSICAL VEDIC YOGAS EVALUATION
    // =========================================================================
    private void evaluateYogas(Map<String, PlanetaryPosition> d1Map, List<DiagnosticsDTO.YogaDetail> yogas) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        PlanetaryPosition sun = d1Map.get("Sun");
        PlanetaryPosition moon = d1Map.get("Moon");
        PlanetaryPosition mars = d1Map.get("Mars");
        PlanetaryPosition mercury = d1Map.get("Mercury");
        PlanetaryPosition jupiter = d1Map.get("Jupiter");
        PlanetaryPosition venus = d1Map.get("Venus");
        PlanetaryPosition saturn = d1Map.get("Saturn");

        if (lagna == null || moon == null) return;
        int lagnaSign = lagna.getSignNumber();
        int moonSign = moon.getSignNumber();
        double sunLong = sun != null ? sun.getAbsoluteLongitude() : 0.0;

        // A. Gajakesari Yoga (Jupiter in Kendra 1,4,7,10 from Moon)
        if (jupiter != null) {
            int jupFromMoon = PlanetDignityUtils.getHouseFromLagna(jupiter.getSignNumber(), moonSign);
            if ((jupFromMoon == 1 || jupFromMoon == 4 || jupFromMoon == 7 || jupFromMoon == 10) &&
                !PlanetDignityUtils.isDebilitated("Jupiter", jupiter.getSignNumber())) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder()
                        .name(ts.getLabel("yoga.gajakesari"))
                        .description(ts.getLabel("yoga.gajakesari.desc"))
                        .impactLevel(ts.getLabel("severity.high"))
                        .build());
            }
        }

        // B. Budhaditya Yoga (Sun & Mercury exact same sign without combustion)
        if (sun != null && mercury != null && sun.getSignNumber() == mercury.getSignNumber()) {
            if (!PlanetDignityUtils.isCombust("Mercury", mercury.getAbsoluteLongitude(), sunLong)) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder()
                        .name(ts.getLabel("yoga.budha_aditya"))
                        .description(ts.getLabel("yoga.budha_aditya.desc"))
                        .impactLevel(ts.getLabel("severity.medium"))
                        .build());
            }
        }

        // C. Chandra-Mangala Yoga
        if (mars != null && moon.getSignNumber() == mars.getSignNumber()) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder()
                    .name(ts.getLabel("yoga.chandra_mangal"))
                    .description(ts.getLabel("yoga.chandra_mangal.desc"))
                    .impactLevel(ts.getLabel("severity.medium"))
                    .build());
        }

        // D. Pancha Mahapurusha Yogas (Kendra from Lagna in Own/Exalted sign & NOT combust)
        if (mars != null && !PlanetDignityUtils.isCombust("Mars", mars.getAbsoluteLongitude(), sunLong)) {
            int marsH = PlanetDignityUtils.getHouseFromLagna(mars.getSignNumber(), lagnaSign);
            if ((marsH == 1 || marsH == 4 || marsH == 7 || marsH == 10) && (PlanetDignityUtils.isOwnSign("Mars", mars.getSignNumber()) || PlanetDignityUtils.isExalted("Mars", mars.getSignNumber()))) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.ruchaka")).description(ts.getLabel("yoga.ruchaka.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }
        if (mercury != null && !PlanetDignityUtils.isCombust("Mercury", mercury.getAbsoluteLongitude(), sunLong)) {
            int merH = PlanetDignityUtils.getHouseFromLagna(mercury.getSignNumber(), lagnaSign);
            if ((merH == 1 || merH == 4 || merH == 7 || merH == 10) && (PlanetDignityUtils.isOwnSign("Mercury", mercury.getSignNumber()) || PlanetDignityUtils.isExalted("Mercury", mercury.getSignNumber()))) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.bhadra")).description(ts.getLabel("yoga.bhadra.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }
        if (jupiter != null && !PlanetDignityUtils.isCombust("Jupiter", jupiter.getAbsoluteLongitude(), sunLong)) {
            int jupH = PlanetDignityUtils.getHouseFromLagna(jupiter.getSignNumber(), lagnaSign);
            if ((jupH == 1 || jupH == 4 || jupH == 7 || jupH == 10) && (PlanetDignityUtils.isOwnSign("Jupiter", jupiter.getSignNumber()) || PlanetDignityUtils.isExalted("Jupiter", jupiter.getSignNumber()))) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.hamsa")).description(ts.getLabel("yoga.hamsa.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }
        if (venus != null && !PlanetDignityUtils.isCombust("Venus", venus.getAbsoluteLongitude(), sunLong)) {
            int venH = PlanetDignityUtils.getHouseFromLagna(venus.getSignNumber(), lagnaSign);
            if ((venH == 1 || venH == 4 || venH == 7 || venH == 10) && (PlanetDignityUtils.isOwnSign("Venus", venus.getSignNumber()) || PlanetDignityUtils.isExalted("Venus", venus.getSignNumber()))) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.malavya")).description(ts.getLabel("yoga.malavya.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }
        if (saturn != null && !PlanetDignityUtils.isCombust("Saturn", saturn.getAbsoluteLongitude(), sunLong)) {
            int satH = PlanetDignityUtils.getHouseFromLagna(saturn.getSignNumber(), lagnaSign);
            if ((satH == 1 || satH == 4 || satH == 7 || satH == 10) && (PlanetDignityUtils.isOwnSign("Saturn", saturn.getSignNumber()) || PlanetDignityUtils.isExalted("Saturn", saturn.getSignNumber()))) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.sasa")).description(ts.getLabel("yoga.sasa.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }

        // E. Dharma-Karmadhipati Yoga (9th & 10th Lords conjunction or aspect)
        int sign9 = ((lagnaSign + 8 - 1) % 12) + 1;
        int sign10 = ((lagnaSign + 9 - 1) % 12) + 1;
        String lord9 = PlanetDignityUtils.getSignLord(sign9);
        String lord10 = PlanetDignityUtils.getSignLord(sign10);

        if (!lord9.equals(lord10) && d1Map.containsKey(lord9) && d1Map.containsKey(lord10)) {
            PlanetaryPosition p9 = d1Map.get(lord9);
            PlanetaryPosition p10 = d1Map.get(lord10);
            if (p9.getSignNumber() == p10.getSignNumber() || PlanetDignityUtils.isAspecting(lord9, p9.getSignNumber(), p10.getSignNumber())) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder()
                        .name(ts.getLabel("yoga.dharma_karmadhipati"))
                        .description(ts.getLabel("yoga.dharma_karmadhipati.desc"))
                        .impactLevel(ts.getLabel("severity.high"))
                        .build());
            }
        }

        // F. Kendra-Trikona Rajayogam
        int[] kendraHouses = {1, 4, 7, 10};
        int[] trikonaHouses = {5, 9};
        boolean rajayogaFound = false;

        for (int kh : kendraHouses) {
            if (rajayogaFound) break;
            int kSign = ((lagnaSign + kh - 2 + 12) % 12) + 1;
            String kLord = PlanetDignityUtils.getSignLord(kSign);
            PlanetaryPosition kPos = d1Map.get(kLord);
            if (kPos == null) continue;

            for (int th : trikonaHouses) {
                int tSign = ((lagnaSign + th - 2 + 12) % 12) + 1;
                String tLord = PlanetDignityUtils.getSignLord(tSign);
                if (tLord.equals(kLord)) continue;
                PlanetaryPosition tPos = d1Map.get(tLord);
                if (tPos == null) continue;

                if (kPos.getSignNumber() == tPos.getSignNumber()) {
                    yogas.add(DiagnosticsDTO.YogaDetail.builder()
                            .name(ts.getLabel("yoga.rajayogam"))
                            .description(ts.getLabel("yoga.rajayogam.desc"))
                            .impactLevel(ts.getLabel("severity.high"))
                            .build());
                    rajayogaFound = true;
                    break;
                }
            }
        }

        // G. Lakshmi Yoga (9th Lord in Kendra/Trikona in own/exalted sign + strong Lagna Lord)
        String lagnaLord = PlanetDignityUtils.getSignLord(lagnaSign);
        PlanetaryPosition pLL = d1Map.get(lagnaLord);
        PlanetaryPosition p9 = d1Map.get(lord9);
        if (p9 != null && pLL != null) {
            int h9 = PlanetDignityUtils.getHouseFromLagna(p9.getSignNumber(), lagnaSign);
            boolean isKendraTrikona = (h9 == 1 || h9 == 4 || h9 == 7 || h9 == 10 || h9 == 5 || h9 == 9);
            boolean isDignified = PlanetDignityUtils.isOwnSign(lord9, p9.getSignNumber()) || PlanetDignityUtils.isExalted(lord9, p9.getSignNumber());
            boolean isLLNotDebilitated = !PlanetDignityUtils.isDebilitated(lagnaLord, pLL.getSignNumber());
            if (isKendraTrikona && isDignified && isLLNotDebilitated) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder()
                        .name(ts.getLabel("yoga.lakshmi"))
                        .description(ts.getLabel("yoga.lakshmi.desc"))
                        .impactLevel(ts.getLabel("severity.high"))
                        .build());
            }
        }

        // H. Amala Yoga (Natural benefic in 10th from Lagna or Moon)
        int h10Lagna = ((lagnaSign + 9 - 1) % 12) + 1;
        int h10Moon = ((moonSign + 9 - 1) % 12) + 1;
        boolean amala = false;
        for (String bKey : new String[]{"Jupiter", "Venus"}) {
            PlanetaryPosition bPos = d1Map.get(bKey);
            if (bPos != null && (bPos.getSignNumber() == h10Lagna || bPos.getSignNumber() == h10Moon)) {
                amala = true;
                break;
            }
        }
        if (amala) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder()
                    .name(ts.getLabel("yoga.amala"))
                    .description(ts.getLabel("yoga.amala.desc"))
                    .impactLevel(ts.getLabel("severity.high"))
                    .build());
        }

        // I. Solar Yogas (Vesi, Vosi, Obhayachari)
        if (sun != null) {
            int sunSign = sun.getSignNumber();
            int h2Sun = (sunSign % 12) + 1;
            int h12Sun = ((sunSign + 10 - 1) % 12) + 1;
            boolean planetIn2 = false;
            boolean planetIn12 = false;
            for (var entry : d1Map.entrySet()) {
                String pKey = entry.getKey();
                if ("Sun".equals(pKey) || "Moon".equals(pKey) || "Rahu".equals(pKey) || "Ketu".equals(pKey) || "Lagna".equals(pKey)) continue;
                int s = entry.getValue().getSignNumber();
                if (s == h2Sun) planetIn2 = true;
                if (s == h12Sun) planetIn12 = true;
            }
            if (planetIn2 && planetIn12) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.obhayachari")).description(ts.getLabel("yoga.obhayachari.desc")).impactLevel(ts.getLabel("severity.high")).build());
            } else if (planetIn2) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.vesi")).description(ts.getLabel("yoga.vesi.desc")).impactLevel(ts.getLabel("severity.medium")).build());
            } else if (planetIn12) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.vosi")).description(ts.getLabel("yoga.vosi.desc")).impactLevel(ts.getLabel("severity.medium")).build());
            }
        }

        // J. Lunar Yogas (Sunapha, Anapha, Dhurudhura, Kemadruma)
        int h2Moon = (moonSign % 12) + 1;
        int h12Moon = ((moonSign + 10 - 1) % 12) + 1;
        boolean planetIn2Moon = false;
        boolean planetIn12Moon = false;
        for (var entry : d1Map.entrySet()) {
            String pKey = entry.getKey();
            if ("Sun".equals(pKey) || "Moon".equals(pKey) || "Rahu".equals(pKey) || "Ketu".equals(pKey) || "Lagna".equals(pKey)) continue;
            int s = entry.getValue().getSignNumber();
            if (s == h2Moon) planetIn2Moon = true;
            if (s == h12Moon) planetIn12Moon = true;
        }
        if (planetIn2Moon && planetIn12Moon) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.dhurudhura")).description(ts.getLabel("yoga.dhurudhura.desc")).impactLevel(ts.getLabel("severity.high")).build());
        } else if (planetIn2Moon) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.sunapha")).description(ts.getLabel("yoga.sunapha.desc")).impactLevel(ts.getLabel("severity.medium")).build());
        } else if (planetIn12Moon) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.anapha")).description(ts.getLabel("yoga.anapha.desc")).impactLevel(ts.getLabel("severity.medium")).build());
        }

        // K. Pravrajya / Sanyasa Yoga (4+ planets in a single sign)
        Map<Integer, Integer> planetCountBySign = new HashMap<>();
        for (var entry : d1Map.entrySet()) {
            if ("Lagna".equals(entry.getKey())) continue;
            planetCountBySign.merge(entry.getValue().getSignNumber(), 1, Integer::sum);
        }
        for (int count : planetCountBySign.values()) {
            if (count >= 4) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.pravrajya")).description(ts.getLabel("yoga.pravrajya.desc")).impactLevel(ts.getLabel("severity.high")).build());
                break;
            }
        }

        // L. Neechabhanga Raja Yoga (5 Classical Laws)
        String[] checkPlanets = {"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn"};
        for (String pKey : checkPlanets) {
            PlanetaryPosition p = d1Map.get(pKey);
            if (p != null && PlanetDignityUtils.isDebilitated(pKey, p.getSignNumber())) {
                String lord = PlanetDignityUtils.getSignLord(p.getSignNumber());
                int exSign = PlanetDignityUtils.getExaltationSign(pKey);
                String exLord = PlanetDignityUtils.getSignLord(exSign);

                PlanetaryPosition lordPos = d1Map.get(lord);
                PlanetaryPosition exLordPos = d1Map.get(exLord);

                boolean neechabhanga = false;

                // Law 1: Dispositor in Kendra from Lagna or Moon
                if (lordPos != null) {
                    int lordHFromLagna = PlanetDignityUtils.getHouseFromLagna(lordPos.getSignNumber(), lagnaSign);
                    int lordHFromMoon = PlanetDignityUtils.getHouseFromLagna(lordPos.getSignNumber(), moonSign);
                    if (lordHFromLagna == 1 || lordHFromLagna == 4 || lordHFromLagna == 7 || lordHFromLagna == 10 ||
                        lordHFromMoon == 1 || lordHFromMoon == 4 || lordHFromMoon == 7 || lordHFromMoon == 10) {
                        neechabhanga = true;
                    }
                }
                // Law 2: Exaltation Lord in Kendra from Lagna or Moon
                if (!neechabhanga && exLordPos != null) {
                    int exLordHFromLagna = PlanetDignityUtils.getHouseFromLagna(exLordPos.getSignNumber(), lagnaSign);
                    int exLordHFromMoon = PlanetDignityUtils.getHouseFromLagna(exLordPos.getSignNumber(), moonSign);
                    if (exLordHFromLagna == 1 || exLordHFromLagna == 4 || exLordHFromLagna == 7 || exLordHFromLagna == 10 ||
                        exLordHFromMoon == 1 || exLordHFromMoon == 4 || exLordHFromMoon == 7 || exLordHFromMoon == 10) {
                        neechabhanga = true;
                    }
                }
                // Law 3: Aspected by or conjunct dispositor/exaltation lord
                if (!neechabhanga && lordPos != null && (lordPos.getSignNumber() == p.getSignNumber() || PlanetDignityUtils.isAspecting(lord, lordPos.getSignNumber(), p.getSignNumber()))) {
                    neechabhanga = true;
                }
                if (!neechabhanga && exLordPos != null && (exLordPos.getSignNumber() == p.getSignNumber() || PlanetDignityUtils.isAspecting(exLord, exLordPos.getSignNumber(), p.getSignNumber()))) {
                    neechabhanga = true;
                }
                // Law 4: Exalted Companion in the exact same sign (e.g. Debilitated Mars + Exalted Jupiter in Cancer)
                if (!neechabhanga) {
                    for (var entry : d1Map.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(pKey) || entry.getKey().equalsIgnoreCase("Lagna")) continue;
                        if (entry.getValue().getSignNumber() == p.getSignNumber() && PlanetDignityUtils.isExalted(entry.getKey(), entry.getValue().getSignNumber())) {
                            neechabhanga = true;
                            break;
                        }
                    }
                }
                // Law 5: Navamsa Exaltation Upgrade (D9)
                if (!neechabhanga) {
                    int navamsaSign = ((p.getSignNumber() - 1) * 9 + (int) (p.getDegreeInSign() / 3.333333)) % 12 + 1;
                    if (PlanetDignityUtils.isExalted(pKey, navamsaSign)) {
                        neechabhanga = true;
                    }
                }

                if (neechabhanga) {
                    String localizedPlanet = ts.getLabel("planet." + pKey.toUpperCase());
                    yogas.add(DiagnosticsDTO.YogaDetail.builder()
                            .name(ts.getLabel("yoga.neechabhanga") + " (" + localizedPlanet + ")")
                            .description(ts.getLabel("yoga.neechabhanga.desc") + " (" + localizedPlanet + ")")
                            .impactLevel(ts.getLabel("severity.high"))
                            .build());
                }
            }
        }

        // M. Vipareeta Raja Yoga (6th, 8th, 12th Lords in Dusthanas 6,8,12) - WITH LAGNA LORD EXCLUSION
        int[] trikHouses = {6, 8, 12};
        for (int th : trikHouses) {
            int trikSign = ((lagnaSign + th - 2 + 12) % 12) + 1;
            String lord = PlanetDignityUtils.getSignLord(trikSign);

            // CRITICAL EDGE CASE: Lagna Lord (e.g. Mars for Aries/Scorpio, Venus for Taurus/Libra) cannot form VRY
            if (lord.equalsIgnoreCase(lagnaLord)) continue;

            PlanetaryPosition lordPos = d1Map.get(lord);
            if (lordPos != null) {
                int lordH = PlanetDignityUtils.getHouseFromLagna(lordPos.getSignNumber(), lagnaSign);
                if (lordH == 6 || lordH == 8 || lordH == 12) {
                    // Check if cancelled by benefic conjunction (Jupiter/Venus)
                    boolean beneficAfflicted = (jupiter != null && jupiter.getSignNumber() == lordPos.getSignNumber()) ||
                                              (venus != null && venus.getSignNumber() == lordPos.getSignNumber() && !lord.equalsIgnoreCase("Venus"));
                    if (!beneficAfflicted) {
                        yogas.add(DiagnosticsDTO.YogaDetail.builder()
                                .name(ts.getLabel("yoga.vipareeta") + " (" + ts.getLabel("planet." + lord.toUpperCase()) + ")")
                                .description(ts.getLabel("yoga.vipareeta.desc"))
                                .impactLevel(ts.getLabel("severity.high"))
                                .build());
                        break;
                    }
                }
            }
        }
    }

    private static boolean isKujaDoshaHouse(int h) {
        return h == 1 || h == 2 || h == 4 || h == 7 || h == 8 || h == 12;
    }
}
