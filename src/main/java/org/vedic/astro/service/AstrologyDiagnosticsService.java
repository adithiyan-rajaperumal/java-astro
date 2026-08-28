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

        // 18 Classical Doshams
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
        doshams.add(evaluatePapakarthariDosham(d1Map));
        doshams.add(evaluateGrahanDosham(d1Map));
        doshams.add(evaluateDaridraYoga(d1Map));
        doshams.add(evaluateDuryoga(d1Map));
        doshams.add(evaluateSarpaDosha(d1Map));
        doshams.add(evaluateKendraAdhipatyaDosham(d1Map));
        doshams.add(evaluateBhadhakadhipatiDosham(d1Map));
        doshams.add(evaluateGandantaDosham(d1Map));

        // 31 Classical Yogas
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
                reason = ts.getLabel("nullification.sevvai.cancer_leo");
            }
            // 2. 11th House Upachaya placement exemption
            else if (marsFromLagna == 11) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.eleventh");
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
            } else if (PlanetDignityUtils.isKendra(rH) || PlanetDignityUtils.isTrikona(rH) || PlanetDignityUtils.isKendra(kH) || PlanetDignityUtils.isTrikona(kH)) {
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
                reason = ts.getLabel("nullification.shani.sasa");
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
    // 4. NEW CLASSICAL DOSHAMS
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
            if (PlanetDignityUtils.isOwnSign("Jupiter", jSign) || PlanetDignityUtils.isExalted("Jupiter", jSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.guru.exalted");
            } else if (jHouse == 5 || jHouse == 9) {
                nullified = true;
                reason = ts.getLabel("nullification.guru.trikona");
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
            if (mHouse == 3 || mHouse == 6 || mHouse == 11) {
                nullified = true;
                reason = ts.getLabel("nullification.angarak.upachaya");
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

    private DiagnosticsDTO.DoshaDetail evaluatePapakarthariDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        if (lagna == null) return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.papakarthari")).detected(false).build();

        int lagnaSign = lagna.getSignNumber();
        int h2Sign = (lagnaSign % 12) + 1;
        int h12Sign = ((lagnaSign + 10 - 1) % 12) + 1;

        boolean maleficIn2 = isMaleficInSign(d1Map, h2Sign);
        boolean maleficIn12 = isMaleficInSign(d1Map, h12Sign);
        boolean detected = maleficIn2 && maleficIn12;
        boolean nullified = false;
        String reason = null;

        if (detected) {
            // Nullified if benefic sits in Lagna
            if (isBeneficInSign(d1Map, lagnaSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.papakartari.lagna_benefic");
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.papakarthari"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.medium")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.papakarthari") : null)
                .build();
    }

    private DiagnosticsDTO.DoshaDetail evaluateGrahanDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition sun = d1Map.get("Sun");
        PlanetaryPosition moon = d1Map.get("Moon");
        PlanetaryPosition rahu = d1Map.get("Rahu");
        PlanetaryPosition ketu = d1Map.get("Ketu");
        PlanetaryPosition jupiter = d1Map.get("Jupiter");

        boolean detected = false;
        boolean nullified = false;
        String reason = null;

        if (sun != null && rahu != null && ketu != null) {
            double sLong = sun.getAbsoluteLongitude();
            double rDiff = Math.abs(sLong - rahu.getAbsoluteLongitude());
            if (rDiff > 180.0) rDiff = 360.0 - rDiff;
            double kDiff = Math.abs(sLong - ketu.getAbsoluteLongitude());
            if (kDiff > 180.0) kDiff = 360.0 - kDiff;
            if (rDiff <= 12.0 || kDiff <= 12.0) detected = true;
        }
        if (!detected && moon != null && rahu != null && ketu != null) {
            double mLong = moon.getAbsoluteLongitude();
            double rDiff = Math.abs(mLong - rahu.getAbsoluteLongitude());
            if (rDiff > 180.0) rDiff = 360.0 - rDiff;
            double kDiff = Math.abs(mLong - ketu.getAbsoluteLongitude());
            if (kDiff > 180.0) kDiff = 360.0 - kDiff;
            if (rDiff <= 12.0 || kDiff <= 12.0) detected = true;
        }

        if (detected && jupiter != null) {
            if (sun != null && PlanetDignityUtils.isAspecting("Jupiter", jupiter.getSignNumber(), sun.getSignNumber())) {
                nullified = true;
                reason = ts.getLabel("nullification.pithru.jupiter_aspect");
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.grahan"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.high")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.grahan") : null)
                .build();
    }

    private DiagnosticsDTO.DoshaDetail evaluateDaridraYoga(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        if (lagna == null) return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.daridra")).detected(false).build();

        int lagnaSign = lagna.getSignNumber();
        int h11Sign = ((lagnaSign + 10 - 1) % 12) + 1;
        String lord11 = PlanetDignityUtils.getSignLord(h11Sign);
        PlanetaryPosition p11 = d1Map.get(lord11);

        boolean detected = false;
        boolean nullified = false;
        String reason = null;

        if (p11 != null) {
            int h11FromLagna = PlanetDignityUtils.getHouseFromLagna(p11.getSignNumber(), lagnaSign);
            if (PlanetDignityUtils.isDusthana(h11FromLagna)) {
                detected = true;
                if (PlanetDignityUtils.isExalted(lord11, p11.getSignNumber())) {
                    nullified = true;
                    reason = ts.getLabel("nullification.eleventh_lord.exalted");
                } else if (d1Map.get("Jupiter") != null && PlanetDignityUtils.isAspecting("Jupiter", d1Map.get("Jupiter").getSignNumber(), p11.getSignNumber())) {
                    nullified = true;
                    reason = ts.getLabel("nullification.sevvai.jupiter_aspect");
                }
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.daridra"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.medium")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.daridra") : null)
                .build();
    }

    private DiagnosticsDTO.DoshaDetail evaluateDuryoga(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        if (lagna == null) return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.duryoga")).detected(false).build();

        int lagnaSign = lagna.getSignNumber();
        int h10Sign = ((lagnaSign + 9 - 1) % 12) + 1;
        String lord10 = PlanetDignityUtils.getSignLord(h10Sign);
        PlanetaryPosition p10 = d1Map.get(lord10);

        boolean detected = false;
        boolean nullified = false;
        String reason = null;

        if (p10 != null) {
            int h10FromLagna = PlanetDignityUtils.getHouseFromLagna(p10.getSignNumber(), lagnaSign);
            if (PlanetDignityUtils.isDusthana(h10FromLagna)) {
                detected = true;
                String lagnaLord = PlanetDignityUtils.getSignLord(lagnaSign);
                if (PlanetDignityUtils.isOwnSign(lord10, p10.getSignNumber())) {
                    nullified = true;
                    reason = ts.getLabel("nullification.tenth_lord.swakshetra");
                } else if (d1Map.containsKey(lagnaLord) && d1Map.get(lagnaLord).getSignNumber() == p10.getSignNumber()) {
                    nullified = true;
                    reason = ts.getLabel("nullification.lagna_lord.conjunct");
                }
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.duryoga"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.medium")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.duryoga") : null)
                .build();
    }

    private DiagnosticsDTO.DoshaDetail evaluateSarpaDosha(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        if (lagna == null) return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.sarpa")).detected(false).build();

        int lagnaSign = lagna.getSignNumber();
        int maleficKendraCount = 0;
        int beneficKendraCount = 0;

        for (int k : new int[]{1, 4, 7, 10}) {
            int kSign = ((lagnaSign + k - 2 + 12) % 12) + 1;
            if (isMaleficInSign(d1Map, kSign)) maleficKendraCount++;
            if (isBeneficInSign(d1Map, kSign)) beneficKendraCount++;
        }

        boolean detected = (maleficKendraCount >= 3 && beneficKendraCount == 0);
        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.sarpa"))
                .detected(detected)
                .nullified(false)
                .active(detected)
                .severity(detected ? ts.getLabel("severity.high") : ts.getLabel("severity.none"))
                .nullificationReason(null)
                .remedySuggestion(detected ? ts.getLabel("remedy.sarpa") : null)
                .build();
    }

    private DiagnosticsDTO.DoshaDetail evaluateKendraAdhipatyaDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        if (lagna == null) return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.kendra_adhipatya")).detected(false).build();

        int lagnaSign = lagna.getSignNumber();
        boolean isDualLagna = (lagnaSign == 3 || lagnaSign == 6 || lagnaSign == 9 || lagnaSign == 12);

        boolean detected = false;
        boolean nullified = false;
        String reason = null;

        String[] naturalBenefics = {"Jupiter", "Venus", "Mercury"};
        for (String b : naturalBenefics) {
            PlanetaryPosition p = d1Map.get(b);
            if (p != null) {
                List<Integer> ruledHouses = getHousesRuledByPlanet(b, lagnaSign);
                boolean rulesKendra = false;
                for (int h : ruledHouses) {
                    if (h == 4 || h == 7 || h == 10) {
                        rulesKendra = true;
                        break;
                    }
                }
                if (rulesKendra) {
                    detected = true;
                    int pHouse = PlanetDignityUtils.getHouseFromLagna(p.getSignNumber(), lagnaSign);
                    if (pHouse == 5 || pHouse == 9) {
                        nullified = true;
                        reason = ts.getLabel("nullification.kendradhipatya.trikona");
                        break;
                    }
                    if (isMaleficInSign(d1Map, p.getSignNumber())) {
                        nullified = true;
                        reason = ts.getLabel("nullification.kendradhipatya.malefic");
                        break;
                    }
                }
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.kendra_adhipatya"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : (isDualLagna ? ts.getLabel("severity.high") : ts.getLabel("severity.medium"))) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.kendra_adhipatya") : null)
                .build();
    }

    private DiagnosticsDTO.DoshaDetail evaluateBhadhakadhipatiDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        if (lagna == null) return DiagnosticsDTO.DoshaDetail.builder().name(ts.getLabel("dosham.bhadhakadhipati")).detected(false).build();

        int lagnaSign = lagna.getSignNumber();
        int bhadhakaHouse = PlanetDignityUtils.getBhadhakaHouse(lagnaSign);
        int bhadhakaSign = ((lagnaSign + bhadhakaHouse - 2 + 12) % 12) + 1;
        String bhadhakaLord = PlanetDignityUtils.getSignLord(bhadhakaSign);
        PlanetaryPosition bPos = d1Map.get(bhadhakaLord);

        boolean detected = false;
        boolean nullified = false;
        String reason = null;

        if (bPos != null) {
            int bHouse = PlanetDignityUtils.getHouseFromLagna(bPos.getSignNumber(), lagnaSign);
            String lagnaLord = PlanetDignityUtils.getSignLord(lagnaSign);
            PlanetaryPosition lPos = d1Map.get(lagnaLord);

            boolean afflictsLagnaLord = (lPos != null && (lPos.getSignNumber() == bPos.getSignNumber() || PlanetDignityUtils.isAspecting(bhadhakaLord, bPos.getSignNumber(), lPos.getSignNumber())));
            if (bHouse == 1 || bHouse == 7 || bHouse == 8 || bHouse == 10 || afflictsLagnaLord) {
                detected = true;

                if (PlanetDignityUtils.isUpachaya(bHouse)) {
                    nullified = true;
                    reason = ts.getLabel("nullification.bhadhakadhipati.upachaya");
                } else if (d1Map.get("Jupiter") != null) {
                    PlanetaryPosition jup = d1Map.get("Jupiter");
                    if (jup.getSignNumber() == bPos.getSignNumber() || PlanetDignityUtils.isAspecting("Jupiter", jup.getSignNumber(), bPos.getSignNumber())) {
                        nullified = true;
                        reason = ts.getLabel("nullification.sevvai.jupiter_aspect");
                    }
                }
            }
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(ts.getLabel("dosham.bhadhakadhipati"))
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.medium")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.bhadhakadhipati") : null)
                .build();
    }

    private DiagnosticsDTO.DoshaDetail evaluateGandantaDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition jupiter = d1Map.get("Jupiter");
        boolean detected = false;
        boolean nullified = false;
        String reason = null;
        List<String> gandantaPlanets = new ArrayList<>();

        for (Map.Entry<String, PlanetaryPosition> entry : d1Map.entrySet()) {
            if ("Lagna".equalsIgnoreCase(entry.getKey())) continue;
            PlanetaryPosition p = entry.getValue();
            if (p != null && PlanetDignityUtils.isGandanta(p.getAbsoluteLongitude())) {
                detected = true;
                gandantaPlanets.add(entry.getKey());
                if (jupiter != null && PlanetDignityUtils.isAspecting("Jupiter", jupiter.getSignNumber(), p.getSignNumber())) {
                    nullified = true;
                    reason = ts.getLabel("nullification.sevvai.jupiter_aspect");
                }
            }
        }

        String name = ts.getLabel("dosham.gandanta");
        if (!gandantaPlanets.isEmpty()) {
            name += " (" + String.join(", ", gandantaPlanets) + ")";
        }

        return DiagnosticsDTO.DoshaDetail.builder()
                .name(name)
                .detected(detected)
                .nullified(nullified)
                .active(detected && !nullified)
                .severity(detected ? (nullified ? ts.getLabel("severity.cancelled") : ts.getLabel("severity.high")) : ts.getLabel("severity.none"))
                .nullificationReason(reason)
                .remedySuggestion(detected && !nullified ? ts.getLabel("remedy.gandanta") : null)
                .build();
    }

    // =========================================================================
    // 5. 31 CLASSICAL VEDIC YOGAS EVALUATION
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
        String lagnaLord = PlanetDignityUtils.getSignLord(lagnaSign);
        PlanetaryPosition pLL = d1Map.get(lagnaLord);

        // A. Gajakesari Yoga (Jupiter in Kendra 1,4,7,10 from Moon)
        if (jupiter != null) {
            int jupFromMoon = PlanetDignityUtils.getHouseFromLagna(jupiter.getSignNumber(), moonSign);
            if (PlanetDignityUtils.isKendra(jupFromMoon) &&
                !PlanetDignityUtils.isDebilitated("Jupiter", jupiter.getSignNumber()) &&
                !PlanetDignityUtils.isCombust("Jupiter", jupiter.getAbsoluteLongitude(), sunLong)) {
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
        if (mars != null && (moon.getSignNumber() == mars.getSignNumber() || ((mars.getSignNumber() - moonSign + 12) % 12 == 6))) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder()
                    .name(ts.getLabel("yoga.chandra_mangal"))
                    .description(ts.getLabel("yoga.chandra_mangal.desc"))
                    .impactLevel(ts.getLabel("severity.medium"))
                    .build());
        }

        // D. Pancha Mahapurusha Yogas (Kendra from Lagna in Own/Exalted sign & NOT combust)
        if (mars != null && !PlanetDignityUtils.isCombust("Mars", mars.getAbsoluteLongitude(), sunLong)) {
            int marsH = PlanetDignityUtils.getHouseFromLagna(mars.getSignNumber(), lagnaSign);
            if (PlanetDignityUtils.isKendra(marsH) && (PlanetDignityUtils.isOwnSign("Mars", mars.getSignNumber()) || PlanetDignityUtils.isExalted("Mars", mars.getSignNumber()))) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.ruchaka")).description(ts.getLabel("yoga.ruchaka.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }
        if (mercury != null && !PlanetDignityUtils.isCombust("Mercury", mercury.getAbsoluteLongitude(), sunLong)) {
            int merH = PlanetDignityUtils.getHouseFromLagna(mercury.getSignNumber(), lagnaSign);
            if (PlanetDignityUtils.isKendra(merH) && (PlanetDignityUtils.isOwnSign("Mercury", mercury.getSignNumber()) || PlanetDignityUtils.isExalted("Mercury", mercury.getSignNumber()))) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.bhadra")).description(ts.getLabel("yoga.bhadra.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }
        if (jupiter != null && !PlanetDignityUtils.isCombust("Jupiter", jupiter.getAbsoluteLongitude(), sunLong)) {
            int jupH = PlanetDignityUtils.getHouseFromLagna(jupiter.getSignNumber(), lagnaSign);
            if (PlanetDignityUtils.isKendra(jupH) && (PlanetDignityUtils.isOwnSign("Jupiter", jupiter.getSignNumber()) || PlanetDignityUtils.isExalted("Jupiter", jupiter.getSignNumber()))) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.hamsa")).description(ts.getLabel("yoga.hamsa.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }
        if (venus != null && !PlanetDignityUtils.isCombust("Venus", venus.getAbsoluteLongitude(), sunLong)) {
            int venH = PlanetDignityUtils.getHouseFromLagna(venus.getSignNumber(), lagnaSign);
            if (PlanetDignityUtils.isKendra(venH) && (PlanetDignityUtils.isOwnSign("Venus", venus.getSignNumber()) || PlanetDignityUtils.isExalted("Venus", venus.getSignNumber()))) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.malavya")).description(ts.getLabel("yoga.malavya.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }
        if (saturn != null && !PlanetDignityUtils.isCombust("Saturn", saturn.getAbsoluteLongitude(), sunLong)) {
            int satH = PlanetDignityUtils.getHouseFromLagna(saturn.getSignNumber(), lagnaSign);
            if (PlanetDignityUtils.isKendra(satH) && (PlanetDignityUtils.isOwnSign("Saturn", saturn.getSignNumber()) || PlanetDignityUtils.isExalted("Saturn", saturn.getSignNumber()))) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.sasa")).description(ts.getLabel("yoga.sasa.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }

        // E. Dharma-Karmadhipati Yoga (9th & 10th Lords conjunction or aspect in Kendra/Trikona)
        int sign9 = ((lagnaSign + 8 - 1) % 12) + 1;
        int sign10 = ((lagnaSign + 9 - 1) % 12) + 1;
        String lord9 = PlanetDignityUtils.getSignLord(sign9);
        String lord10 = PlanetDignityUtils.getSignLord(sign10);

        if (!lord9.equals(lord10) && d1Map.containsKey(lord9) && d1Map.containsKey(lord10)) {
            PlanetaryPosition p9 = d1Map.get(lord9);
            PlanetaryPosition p10 = d1Map.get(lord10);
            int h9 = PlanetDignityUtils.getHouseFromLagna(p9.getSignNumber(), lagnaSign);
            if ((PlanetDignityUtils.isKendra(h9) || PlanetDignityUtils.isTrikona(h9)) && !PlanetDignityUtils.isDusthana(h9)) {
                if (p9.getSignNumber() == p10.getSignNumber() || PlanetDignityUtils.isAspecting(lord9, p9.getSignNumber(), p10.getSignNumber())) {
                    yogas.add(DiagnosticsDTO.YogaDetail.builder()
                            .name(ts.getLabel("yoga.dharma_karmadhipati"))
                            .description(ts.getLabel("yoga.dharma_karmadhipati.desc"))
                            .impactLevel(ts.getLabel("severity.high"))
                            .build());
                }
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

        // G. Lakshmi Yoga
        PlanetaryPosition p9 = d1Map.get(lord9);
        if (p9 != null && pLL != null) {
            int h9 = PlanetDignityUtils.getHouseFromLagna(p9.getSignNumber(), lagnaSign);
            boolean isKT = (PlanetDignityUtils.isKendra(h9) || PlanetDignityUtils.isTrikona(h9));
            boolean isDignified = PlanetDignityUtils.isOwnSign(lord9, p9.getSignNumber()) || PlanetDignityUtils.isExalted(lord9, p9.getSignNumber());
            boolean isLLNotDebilitated = !PlanetDignityUtils.isDebilitated(lagnaLord, pLL.getSignNumber());
            if (isKT && isDignified && isLLNotDebilitated) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder()
                        .name(ts.getLabel("yoga.lakshmi"))
                        .description(ts.getLabel("yoga.lakshmi.desc"))
                        .impactLevel(ts.getLabel("severity.high"))
                        .build());
            }
        }

        // H. Bhagyalakshmi Yoga
        if (p9 != null && jupiter != null && venus != null) {
            int h9 = PlanetDignityUtils.getHouseFromLagna(p9.getSignNumber(), lagnaSign);
            int jH = PlanetDignityUtils.getHouseFromLagna(jupiter.getSignNumber(), lagnaSign);
            int vH = PlanetDignityUtils.getHouseFromLagna(venus.getSignNumber(), lagnaSign);
            boolean p9Dignified = PlanetDignityUtils.isOwnSign(lord9, p9.getSignNumber()) || PlanetDignityUtils.isExalted(lord9, p9.getSignNumber());
            boolean p9InKT = (PlanetDignityUtils.isKendra(h9) || PlanetDignityUtils.isTrikona(h9)) && !PlanetDignityUtils.isDusthana(h9);
            boolean jvInKT = (PlanetDignityUtils.isKendra(jH) || PlanetDignityUtils.isTrikona(jH)) && (PlanetDignityUtils.isKendra(vH) || PlanetDignityUtils.isTrikona(vH));
            boolean notCombust = !PlanetDignityUtils.isCombust(lord9, p9.getAbsoluteLongitude(), sunLong) &&
                                !PlanetDignityUtils.isCombust("Jupiter", jupiter.getAbsoluteLongitude(), sunLong) &&
                                !PlanetDignityUtils.isCombust("Venus", venus.getAbsoluteLongitude(), sunLong);
            if (p9InKT && p9Dignified && jvInKT && notCombust) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder()
                        .name(ts.getLabel("yoga.bhagyalakshmi"))
                        .description(ts.getLabel("yoga.bhagyalakshmi.desc"))
                        .impactLevel(ts.getLabel("severity.high"))
                        .build());
            }
        }

        // I. Rajalakshmi Yoga
        if (jupiter != null && venus != null && mercury != null) {
            int jH = PlanetDignityUtils.getHouseFromLagna(jupiter.getSignNumber(), lagnaSign);
            int vH = PlanetDignityUtils.getHouseFromLagna(venus.getSignNumber(), lagnaSign);
            int mH = PlanetDignityUtils.getHouseFromLagna(mercury.getSignNumber(), lagnaSign);
            int moH = PlanetDignityUtils.getHouseFromLagna(moonSign, lagnaSign);
            boolean allInKT = (PlanetDignityUtils.isKendra(jH) || PlanetDignityUtils.isTrikona(jH)) &&
                              (PlanetDignityUtils.isKendra(vH) || PlanetDignityUtils.isTrikona(vH)) &&
                              (PlanetDignityUtils.isKendra(mH) || PlanetDignityUtils.isTrikona(mH)) &&
                              (PlanetDignityUtils.isKendra(moH) || PlanetDignityUtils.isTrikona(moH));
            if (allInKT && !PlanetDignityUtils.isDebilitated("Venus", venus.getSignNumber()) && !PlanetDignityUtils.isDebilitated("Moon", moonSign)) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder()
                        .name(ts.getLabel("yoga.rajalakshmi"))
                        .description(ts.getLabel("yoga.rajalakshmi.desc"))
                        .impactLevel(ts.getLabel("severity.high"))
                        .build());
            }
        }

        // J. Amala Yoga
        int h10Lagna = ((lagnaSign + 9 - 1) % 12) + 1;
        int h10Moon = ((moonSign + 9 - 1) % 12) + 1;
        boolean amala = false;
        for (String bKey : new String[]{"Jupiter", "Venus"}) {
            PlanetaryPosition bPos = d1Map.get(bKey);
            if (bPos != null && (bPos.getSignNumber() == h10Lagna || bPos.getSignNumber() == h10Moon)) {
                if (!isMaleficInSign(d1Map, bPos.getSignNumber())) {
                    amala = true;
                    break;
                }
            }
        }
        if (amala) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder()
                    .name(ts.getLabel("yoga.amala"))
                    .description(ts.getLabel("yoga.amala.desc"))
                    .impactLevel(ts.getLabel("severity.high"))
                    .build());
        }

        // K. Solar Yogas (Vesi, Vosi, Obhayachari)
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

        // L. Lunar Yogas (Sunapha, Anapha, Dhurudhura, Kemadruma)
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

        // M. Adhi Yoga (Benefics in 6th, 7th, 8th from Moon or Lagna)
        if (jupiter != null && venus != null && mercury != null) {
            int jFromMoon = PlanetDignityUtils.getHouseFromLagna(jupiter.getSignNumber(), moonSign);
            int vFromMoon = PlanetDignityUtils.getHouseFromLagna(venus.getSignNumber(), moonSign);
            int mFromMoon = PlanetDignityUtils.getHouseFromLagna(mercury.getSignNumber(), moonSign);
            if (isAdhiHouse(jFromMoon) && isAdhiHouse(vFromMoon) && isAdhiHouse(mFromMoon)) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.adhi")).description(ts.getLabel("yoga.adhi.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }

        // N. Vasumathi Yoga (All benefics in 3, 6, 10, 11 from Lagna or Moon)
        if (jupiter != null && venus != null && mercury != null) {
            int jH = PlanetDignityUtils.getHouseFromLagna(jupiter.getSignNumber(), lagnaSign);
            int vH = PlanetDignityUtils.getHouseFromLagna(venus.getSignNumber(), lagnaSign);
            int mH = PlanetDignityUtils.getHouseFromLagna(mercury.getSignNumber(), lagnaSign);
            if (PlanetDignityUtils.isUpachaya(jH) && PlanetDignityUtils.isUpachaya(vH) && PlanetDignityUtils.isUpachaya(mH)) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.vasumathi")).description(ts.getLabel("yoga.vasumathi.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }

        // O. Saraswati Yoga
        if (jupiter != null && venus != null && mercury != null) {
            int jH = PlanetDignityUtils.getHouseFromLagna(jupiter.getSignNumber(), lagnaSign);
            int vH = PlanetDignityUtils.getHouseFromLagna(venus.getSignNumber(), lagnaSign);
            int mH = PlanetDignityUtils.getHouseFromLagna(mercury.getSignNumber(), lagnaSign);
            boolean jValid = (PlanetDignityUtils.isKendra(jH) || PlanetDignityUtils.isTrikona(jH) || jH == 2);
            boolean vValid = (PlanetDignityUtils.isKendra(vH) || PlanetDignityUtils.isTrikona(vH) || vH == 2);
            boolean mValid = (PlanetDignityUtils.isKendra(mH) || PlanetDignityUtils.isTrikona(mH) || mH == 2);
            if (jValid && vValid && mValid && !PlanetDignityUtils.isDebilitated("Jupiter", jupiter.getSignNumber())) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.saraswati")).description(ts.getLabel("yoga.saraswati.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }

        // P. Pushkala Yoga
        int moonDispositorSign = moonSign;
        String moonDispLord = PlanetDignityUtils.getSignLord(moonDispositorSign);
        PlanetaryPosition pMoonDisp = d1Map.get(moonDispLord);
        if (pMoonDisp != null && pLL != null) {
            int dispH = PlanetDignityUtils.getHouseFromLagna(pMoonDisp.getSignNumber(), lagnaSign);
            if ((PlanetDignityUtils.isKendra(dispH) || PlanetDignityUtils.isTrikona(dispH)) &&
                pMoonDisp.getSignNumber() == pLL.getSignNumber() &&
                PlanetDignityUtils.isAspecting(moonDispLord, pMoonDisp.getSignNumber(), lagnaSign)) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.pushkala")).description(ts.getLabel("yoga.pushkala.desc")).impactLevel(ts.getLabel("severity.high")).build());
            }
        }

        // Q. Shakata Yoga
        if (jupiter != null) {
            int moonFromJup = PlanetDignityUtils.getHouseFromLagna(moonSign, jupiter.getSignNumber());
            if (PlanetDignityUtils.isDusthana(moonFromJup)) {
                // Check Shakata Bhanga
                int moonH = PlanetDignityUtils.getHouseFromLagna(moonSign, lagnaSign);
                boolean marsProtects = mars != null && (mars.getSignNumber() == moonSign || PlanetDignityUtils.isAspecting("Mars", mars.getSignNumber(), moonSign));
                boolean jupDignified = PlanetDignityUtils.isOwnSign("Jupiter", jupiter.getSignNumber()) || PlanetDignityUtils.isExalted("Jupiter", jupiter.getSignNumber());
                if (!PlanetDignityUtils.isKendra(moonH) && !marsProtects && !jupDignified) {
                    yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.shakata")).description(ts.getLabel("yoga.shakata.desc")).impactLevel(ts.getLabel("severity.medium")).build());
                }
            }
        }

        // R. Pravrajya / Sanyasa Yoga (4+ planets in a single sign)
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

        // S. Neechabhanga Raja Yoga (5 Classical Laws)
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
                    if (PlanetDignityUtils.isKendra(lordHFromLagna) || PlanetDignityUtils.isKendra(lordHFromMoon)) {
                        neechabhanga = true;
                    }
                }
                // Law 2: Exaltation Lord in Kendra from Lagna or Moon
                if (!neechabhanga && exLordPos != null) {
                    int exLordHFromLagna = PlanetDignityUtils.getHouseFromLagna(exLordPos.getSignNumber(), lagnaSign);
                    int exLordHFromMoon = PlanetDignityUtils.getHouseFromLagna(exLordPos.getSignNumber(), moonSign);
                    if (PlanetDignityUtils.isKendra(exLordHFromLagna) || PlanetDignityUtils.isKendra(exLordHFromMoon)) {
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
                // Law 4: Exalted Companion in the exact same sign
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

        // T. Vipareeta Raja Yoga (6th, 8th, 12th Lords in Dusthanas) - WITH LAGNA LORD EXCLUSION
        int[] trikHouses = {6, 8, 12};
        for (int th : trikHouses) {
            int trikSign = ((lagnaSign + th - 2 + 12) % 12) + 1;
            String lord = PlanetDignityUtils.getSignLord(trikSign);

            // CRITICAL EDGE CASE: Lagna Lord (e.g. Mars for Aries/Scorpio, Venus for Taurus/Libra) cannot form VRY
            if (lord.equalsIgnoreCase(lagnaLord)) continue;

            PlanetaryPosition lordPos = d1Map.get(lord);
            if (lordPos != null) {
                int lordH = PlanetDignityUtils.getHouseFromLagna(lordPos.getSignNumber(), lagnaSign);
                if (PlanetDignityUtils.isDusthana(lordH)) {
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

        // U. Parijata Yoga (பாரிஜாத யோகம்)
        String l1Lord = PlanetDignityUtils.getSignLord(lagnaSign);
        PlanetaryPosition l1Pos = d1Map.get(l1Lord);
        if (l1Pos != null) {
            String l1DispositorName = PlanetDignityUtils.getSignLord(l1Pos.getSignNumber());
            PlanetaryPosition l1DispPos = d1Map.get(l1DispositorName);
            if (l1DispPos != null) {
                String l2DispositorName = PlanetDignityUtils.getSignLord(l1DispPos.getSignNumber());
                PlanetaryPosition l2DispPos = d1Map.get(l2DispositorName);
                if (l2DispPos != null) {
                    int h1 = PlanetDignityUtils.getHouseFromLagna(l1DispPos.getSignNumber(), lagnaSign);
                    int h2 = PlanetDignityUtils.getHouseFromLagna(l2DispPos.getSignNumber(), lagnaSign);
                    boolean h1Valid = (PlanetDignityUtils.isKendra(h1) || PlanetDignityUtils.isTrikona(h1)) &&
                            (PlanetDignityUtils.isOwnSign(l1DispositorName, l1DispPos.getSignNumber()) || PlanetDignityUtils.isExalted(l1DispositorName, l1DispPos.getSignNumber()));
                    boolean h2Valid = (PlanetDignityUtils.isKendra(h2) || PlanetDignityUtils.isTrikona(h2)) &&
                            (PlanetDignityUtils.isOwnSign(l2DispositorName, l2DispPos.getSignNumber()) || PlanetDignityUtils.isExalted(l2DispositorName, l2DispPos.getSignNumber()));
                    if (h1Valid && h2Valid) {
                        yogas.add(DiagnosticsDTO.YogaDetail.builder()
                                .name(ts.getLabel("yoga.parijata"))
                                .description(ts.getLabel("yoga.parijata.desc"))
                                .impactLevel(ts.getLabel("severity.high"))
                                .build());
                    }
                }
            }
        }

        // V. Chatussagara Yoga (All 4 Kendra houses 1, 4, 7, 10 occupied by planets)
        boolean k1 = false, k4 = false, k7 = false, k10 = false;
        for (Map.Entry<String, PlanetaryPosition> entry : d1Map.entrySet()) {
            if ("Lagna".equalsIgnoreCase(entry.getKey())) continue;
            PlanetaryPosition p = entry.getValue();
            if (p != null) {
                int h = PlanetDignityUtils.getHouseFromLagna(p.getSignNumber(), lagnaSign);
                if (h == 1) k1 = true;
                else if (h == 4) k4 = true;
                else if (h == 7) k7 = true;
                else if (h == 10) k10 = true;
            }
        }
        if (k1 && k4 && k7 && k10) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder()
                    .name(ts.getLabel("yoga.chatussagara"))
                    .description(ts.getLabel("yoga.chatussagara.desc"))
                    .impactLevel(ts.getLabel("severity.high"))
                    .build());
        }

        // W. Mala Yoga (Garland of Benefics: all 3 benefics in Kendras, no malefics in Kendras)
        if (jupiter != null && venus != null && mercury != null) {
            int jH = PlanetDignityUtils.getHouseFromLagna(jupiter.getSignNumber(), lagnaSign);
            int vH = PlanetDignityUtils.getHouseFromLagna(venus.getSignNumber(), lagnaSign);
            int meH = PlanetDignityUtils.getHouseFromLagna(mercury.getSignNumber(), lagnaSign);
            if (PlanetDignityUtils.isKendra(jH) && PlanetDignityUtils.isKendra(vH) && PlanetDignityUtils.isKendra(meH)) {
                boolean maleficInKendra = false;
                for (String m : new String[]{"Sun", "Mars", "Saturn", "Rahu", "Ketu"}) {
                    PlanetaryPosition mp = d1Map.get(m);
                    if (mp != null) {
                        int mh = PlanetDignityUtils.getHouseFromLagna(mp.getSignNumber(), lagnaSign);
                        if (PlanetDignityUtils.isKendra(mh)) {
                            maleficInKendra = true;
                            break;
                        }
                    }
                }
                if (!maleficInKendra) {
                    yogas.add(DiagnosticsDTO.YogaDetail.builder()
                            .name(ts.getLabel("yoga.mala"))
                            .description(ts.getLabel("yoga.mala.desc"))
                            .impactLevel(ts.getLabel("severity.high"))
                            .build());
                }
            }
        }

        // X. Indra Yoga
        int h5Sign = ((lagnaSign + 5 - 2 + 12) % 12) + 1;
        int h11Sign = ((lagnaSign + 11 - 2 + 12) % 12) + 1;
        String l5Name = PlanetDignityUtils.getSignLord(h5Sign);
        String l11Name = PlanetDignityUtils.getSignLord(h11Sign);
        PlanetaryPosition p5 = d1Map.get(l5Name);
        PlanetaryPosition p11 = d1Map.get(l11Name);
        if (p5 != null && p11 != null) {
            boolean parivartana = (p5.getSignNumber() == h11Sign && p11.getSignNumber() == h5Sign);
            boolean moonIn11 = (p11.getSignNumber() == h5Sign && moon != null && moon.getSignNumber() == h11Sign);
            if (parivartana || moonIn11) {
                yogas.add(DiagnosticsDTO.YogaDetail.builder()
                        .name(ts.getLabel("yoga.indra"))
                        .description(ts.getLabel("yoga.indra.desc"))
                        .impactLevel(ts.getLabel("severity.high"))
                        .build());
            }
        }

        // Y. General Kendra-Trikona Sambandha Yoga
        int[] kendras = {1, 4, 7, 10};
        int[] trikonas = {1, 5, 9};
        boolean sambandhaFound = false;
        for (int k : kendras) {
            if (sambandhaFound) break;
            int kSign = ((lagnaSign + k - 2 + 12) % 12) + 1;
            String kLord = PlanetDignityUtils.getSignLord(kSign);
            PlanetaryPosition kPos = d1Map.get(kLord);
            if (kPos == null) continue;
            int kHouse = PlanetDignityUtils.getHouseFromLagna(kPos.getSignNumber(), lagnaSign);
            if (PlanetDignityUtils.isDusthana(kHouse) || PlanetDignityUtils.isCombust(kLord, kPos.getAbsoluteLongitude(), sunLong)) continue;

            for (int t : trikonas) {
                if (k == t) continue;
                int tSign = ((lagnaSign + t - 2 + 12) % 12) + 1;
                String tLord = PlanetDignityUtils.getSignLord(tSign);
                if (kLord.equalsIgnoreCase(tLord)) continue;
                PlanetaryPosition tPos = d1Map.get(tLord);
                if (tPos == null) continue;
                int tHouse = PlanetDignityUtils.getHouseFromLagna(tPos.getSignNumber(), lagnaSign);
                if (PlanetDignityUtils.isDusthana(tHouse) || PlanetDignityUtils.isCombust(tLord, tPos.getAbsoluteLongitude(), sunLong)) continue;

                if (kPos.getSignNumber() == tPos.getSignNumber() && (PlanetDignityUtils.isKendra(kHouse) || PlanetDignityUtils.isTrikona(kHouse))) {
                    sambandhaFound = true;
                } else if (PlanetDignityUtils.isAspecting(kLord, kPos.getSignNumber(), tPos.getSignNumber()) &&
                           PlanetDignityUtils.isAspecting(tLord, tPos.getSignNumber(), kPos.getSignNumber()) &&
                           (PlanetDignityUtils.isKendra(kHouse) || PlanetDignityUtils.isTrikona(kHouse))) {
                    sambandhaFound = true;
                } else if (kPos.getSignNumber() == tSign && tPos.getSignNumber() == kSign) {
                    sambandhaFound = true;
                }

                if (sambandhaFound) {
                    yogas.add(DiagnosticsDTO.YogaDetail.builder()
                            .name(ts.getLabel("yoga.kendra_trikona_sambandha") + " (" + ts.getLabel("planet." + kLord.toUpperCase()) + " & " + ts.getLabel("planet." + tLord.toUpperCase()) + ")")
                            .description(ts.getLabel("yoga.kendra_trikona_sambandha.desc"))
                            .impactLevel(ts.getLabel("severity.high"))
                            .build());
                    break;
                }
            }
        }
    }

    // Helper functions
    private static List<Integer> getHousesRuledByPlanet(String planet, int lagnaSign) {
        List<Integer> houses = new ArrayList<>();
        for (int h = 1; h <= 12; h++) {
            int sign = ((lagnaSign + h - 2 + 12) % 12) + 1;
            if (planet.equalsIgnoreCase(PlanetDignityUtils.getSignLord(sign))) {
                houses.add(h);
            }
        }
        return houses;
    }

    private static boolean isKujaDoshaHouse(int h) {
        return h == 1 || h == 2 || h == 4 || h == 7 || h == 8 || h == 12;
    }

    private static boolean isMaleficInSign(Map<String, PlanetaryPosition> d1Map, int sign) {
        for (String m : new String[]{"Sun", "Mars", "Saturn", "Rahu", "Ketu"}) {
            PlanetaryPosition p = d1Map.get(m);
            if (p != null && p.getSignNumber() == sign) return true;
        }
        return false;
    }

    private static boolean isBeneficInSign(Map<String, PlanetaryPosition> d1Map, int sign) {
        for (String b : new String[]{"Jupiter", "Venus", "Mercury"}) {
            PlanetaryPosition p = d1Map.get(b);
            if (p != null && p.getSignNumber() == sign) return true;
        }
        return false;
    }

    private static boolean isAdhiHouse(int h) {
        return h == 6 || h == 7 || h == 8;
    }
}
