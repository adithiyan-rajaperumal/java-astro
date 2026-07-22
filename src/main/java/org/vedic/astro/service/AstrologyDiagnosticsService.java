package org.vedic.astro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vedic.astro.dto.DiagnosticsDTO;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.PlanetDignityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        evaluateYogas(d1Map, yogas);

        return DiagnosticsDTO.builder().activeYogas(yogas).discoveredDoshams(doshams).horoscopicSpecialities(specs).build();
    }

    private DiagnosticsDTO.DoshaDetail evaluateSevvaiDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition lagna = d1Map.get("Lagna");
        PlanetaryPosition moon = d1Map.get("Moon");
        PlanetaryPosition venus = d1Map.get("Venus");
        PlanetaryPosition mars = d1Map.get("Mars");
        PlanetaryPosition jupiter = d1Map.get("Jupiter");

        int marsFromLagna = PlanetDignityUtils.getHouseFromLagna(mars.getSignNumber(), lagna.getSignNumber());
        boolean detected = (marsFromLagna == 1 || marsFromLagna == 2 || marsFromLagna == 4 || marsFromLagna == 7 || marsFromLagna == 8 || marsFromLagna == 12);
        
        boolean nullified = false;
        String reason = null;

        if (detected) {
            int mSign = mars.getSignNumber();
            int jSign = jupiter.getSignNumber();
            int vSign = venus.getSignNumber();

            if (PlanetDignityUtils.isOwnSign("Mars", mSign) || PlanetDignityUtils.isExalted("Mars", mSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.own_exalted");
            } else if (mSign == jSign || PlanetDignityUtils.isAspecting("Jupiter", jSign, mSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.jupiter_aspect");
            } else if (mSign == vSign || PlanetDignityUtils.isAspecting("Venus", vSign, mSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.venus_aspect");
            } else if (mSign == moon.getSignNumber()) { // Conjunction with Moon (Chandra-Mangala)
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.benefic_sign");
            } else if (mSign == 5 || mSign == 4) { // Leo or Cancer
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.benefic_sign");
            } else if (marsFromLagna == 2 && (mSign == 3 || mSign == 6 || mSign == 10 || mSign == 11)) { // 2nd in Gemini/Virgo/Capricorn/Aquarius
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.house_sign_exemption");
            } else if (marsFromLagna == 4 && (mSign == 1 || mSign == 8 || mSign == 4)) { // 4th in Aries/Scorpio/Cancer
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.own_exalted");
            } else if (marsFromLagna == 7 && (mSign == 4 || mSign == 10 || mSign == 2 || mSign == 7)) { // 7th in Cancer/Capricorn/Taurus/Libra
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.own_exalted");
            } else if (marsFromLagna == 8 && (mSign == 9 || mSign == 12 || mSign == 3 || mSign == 6)) { // 8th in Sagittarius/Pisces/Gemini/Virgo
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.house_sign_exemption");
            } else if (marsFromLagna == 12 && (mSign == 2 || mSign == 7 || mSign == 9 || mSign == 12)) { // 12th in Taurus/Libra/Sagittarius/Pisces
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

    private DiagnosticsDTO.DoshaDetail evaluateKalaSarpaDosham(Map<String, PlanetaryPosition> d1Map) {
        PlanetaryPosition rahu = d1Map.get("Rahu");
        PlanetaryPosition ketu = d1Map.get("Ketu");
        PlanetaryPosition jupiter = d1Map.get("Jupiter");
        int lagnaSign = d1Map.get("Lagna").getSignNumber();

        double rLong = rahu.getAbsoluteLongitude();
        double kLong = ketu.getAbsoluteLongitude();
        double mn = Math.min(rLong, kLong);
        double mx = Math.max(rLong, kLong);
        
        boolean inside = true;
        boolean outside = true;
        boolean planetConjunct = false;

        for (var e : d1Map.entrySet()) {
            if ("Lagna".equals(e.getKey()) || "Rahu".equals(e.getKey()) || "Ketu".equals(e.getKey()) || "Uranus".equals(e.getKey()) || "Neptune".equals(e.getKey()) || "Pluto".equals(e.getKey())) continue;
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
            } else if (PlanetDignityUtils.isAspecting("Jupiter", jupiter.getSignNumber(), rahu.getSignNumber()) || PlanetDignityUtils.isAspecting("Jupiter", jupiter.getSignNumber(), ketu.getSignNumber())) {
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
        int lagnaSign = d1Map.get("Lagna").getSignNumber();
        int rahuH = PlanetDignityUtils.getHouseFromLagna(d1Map.get("Rahu").getSignNumber(), lagnaSign);
        int ketuH = PlanetDignityUtils.getHouseFromLagna(d1Map.get("Ketu").getSignNumber(), lagnaSign);
        int jupSign = d1Map.get("Jupiter").getSignNumber();
        int rahuSign = d1Map.get("Rahu").getSignNumber();
        int ketuSign = d1Map.get("Ketu").getSignNumber();
        int venSign = d1Map.get("Venus").getSignNumber();
        int merSign = d1Map.get("Mercury").getSignNumber();

        // Rahu/Ketu in 3, 6, 11 is Upachaya (beneficial) - not a dosham
        boolean detected = (rahuH == 1 || rahuH == 2 || rahuH == 5 || rahuH == 7 || rahuH == 8 || ketuH == 1 || ketuH == 2 || ketuH == 5 || ketuH == 7 || ketuH == 8);
        boolean nullified = false;
        String reason = null;

        if (detected) {
            int jupFromRahu = PlanetDignityUtils.getHouseFromLagna(jupSign, rahuSign);
            if (jupFromRahu == 1 || jupFromRahu == 4 || jupFromRahu == 7 || jupFromRahu == 10) {
                nullified = true;
                reason = ts.getLabel("nullification.sarpam.jupiter_kendra");
            } else if (PlanetDignityUtils.isAspecting("Jupiter", jupSign, rahuSign) || PlanetDignityUtils.isAspecting("Jupiter", jupSign, ketuSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.sarpam.jupiter_aspect");
            } else if (PlanetDignityUtils.isOwnSign("Rahu", rahuSign) || PlanetDignityUtils.isExalted("Rahu", rahuSign) || PlanetDignityUtils.isOwnSign("Ketu", ketuSign) || PlanetDignityUtils.isExalted("Ketu", ketuSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.sarpam.own_exalted");
            } else if (venSign == rahuSign || venSign == ketuSign || merSign == rahuSign || merSign == ketuSign || PlanetDignityUtils.isAspecting("Venus", venSign, rahuSign)) {
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

    private DiagnosticsDTO.DoshaDetail evaluatePithruDosham(Map<String, PlanetaryPosition> d1Map) {
        int lagnaSign = d1Map.get("Lagna").getSignNumber();
        int sunSign = d1Map.get("Sun").getSignNumber();
        int sunH = PlanetDignityUtils.getHouseFromLagna(sunSign, lagnaSign);
        
        boolean sunAfflicted = sunSign == d1Map.get("Rahu").getSignNumber() || sunSign == d1Map.get("Ketu").getSignNumber() || 
                               sunSign == d1Map.get("Saturn").getSignNumber() || 
                               PlanetDignityUtils.isAspecting("Saturn", d1Map.get("Saturn").getSignNumber(), sunSign);
        boolean detected = sunH == 9 && sunAfflicted;
        
        boolean nullified = false;
        String reason = null;
        
        if (detected) {
            int jupSign = d1Map.get("Jupiter").getSignNumber();
            int merSign = d1Map.get("Mercury").getSignNumber();

            if (PlanetDignityUtils.isAspecting("Jupiter", jupSign, sunSign) || jupSign == sunSign) {
                nullified = true;
                reason = ts.getLabel("nullification.pithru.jupiter_aspect");
            } else if (PlanetDignityUtils.isOwnSign("Sun", sunSign) || PlanetDignityUtils.isExalted("Sun", sunSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.pithru.own_exalted");
            } else if (d1Map.get("Venus").getSignNumber() == sunSign || merSign == sunSign) {
                nullified = true;
                reason = ts.getLabel("nullification.pithru.benefic");
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
        int lagnaSign = d1Map.get("Lagna").getSignNumber();
        int fifthHouseSign = ((lagnaSign + 4) % 12) == 0 ? 12 : ((lagnaSign + 4) % 12);
        
        boolean maleficInFifth = d1Map.get("Saturn").getSignNumber() == fifthHouseSign || 
                                 d1Map.get("Rahu").getSignNumber() == fifthHouseSign || 
                                 d1Map.get("Ketu").getSignNumber() == fifthHouseSign || 
                                 d1Map.get("Mars").getSignNumber() == fifthHouseSign;
                                 
        boolean detected = maleficInFifth;
        boolean nullified = false;
        String reason = null;
        
        if (detected) {
            int jupSign = d1Map.get("Jupiter").getSignNumber();
            String lord5 = PlanetDignityUtils.getSignLord(fifthHouseSign);
            PlanetaryPosition pLord5 = d1Map.get(lord5);

            if (jupSign == fifthHouseSign || PlanetDignityUtils.isAspecting("Jupiter", jupSign, fifthHouseSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.putra.jupiter");
            } else if (d1Map.get("Venus").getSignNumber() == fifthHouseSign || d1Map.get("Mercury").getSignNumber() == fifthHouseSign) {
                nullified = true;
                reason = ts.getLabel("nullification.putra.benefic");
            } else if (fifthHouseSign == 2 || fifthHouseSign == 4 || fifthHouseSign == 7) { // Taurus, Cancer, Libra (fertile signs)
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.house_sign_exemption");
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
        int lagnaSign = d1Map.get("Lagna").getSignNumber();
        int seventhHouseSign = ((lagnaSign + 6) % 12) == 0 ? 12 : ((lagnaSign + 6) % 12);
        
        boolean maleficInSeventh = d1Map.get("Saturn").getSignNumber() == seventhHouseSign || 
                                 d1Map.get("Rahu").getSignNumber() == seventhHouseSign || 
                                 d1Map.get("Ketu").getSignNumber() == seventhHouseSign || 
                                 d1Map.get("Mars").getSignNumber() == seventhHouseSign;
        
        boolean detected = maleficInSeventh; 
        boolean nullified = false;
        String reason = null;
        
        if (detected) {
            int venusSign = d1Map.get("Venus").getSignNumber();
            int jupSign = d1Map.get("Jupiter").getSignNumber();
            String lord7 = PlanetDignityUtils.getSignLord(seventhHouseSign);
            PlanetaryPosition pLord7 = d1Map.get(lord7);

            if (PlanetDignityUtils.isOwnSign("Venus", venusSign) || PlanetDignityUtils.isExalted("Venus", venusSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.kalathira.venus_strong");
            } else if (jupSign == seventhHouseSign || PlanetDignityUtils.isAspecting("Jupiter", jupSign, seventhHouseSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.kalathira.jupiter_aspect");
            } else if (d1Map.get("Mercury").getSignNumber() == seventhHouseSign || venusSign == seventhHouseSign) {
                nullified = true;
                reason = ts.getLabel("nullification.putra.benefic");
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
        int lagnaSign = d1Map.get("Lagna").getSignNumber();
        int saturnSign = d1Map.get("Saturn").getSignNumber();
        int satH = PlanetDignityUtils.getHouseFromLagna(saturnSign, lagnaSign);
        
        // Saturn in 3, 6, 11 (Upachaya) is NOT Shani Dosham
        boolean detected = (satH == 1 || satH == 4 || satH == 7 || satH == 8 || satH == 10 || satH == 12);
        boolean nullified = false;
        String reason = null;
        
        if (detected) {
            if (PlanetDignityUtils.isOwnSign("Saturn", saturnSign) || PlanetDignityUtils.isExalted("Saturn", saturnSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.shani.own_exalted");
            } else if (PlanetDignityUtils.isAspecting("Jupiter", d1Map.get("Jupiter").getSignNumber(), saturnSign)) {
                nullified = true;
                reason = ts.getLabel("nullification.shani.jupiter_aspect");
            } else if (lagnaSign == 2 || lagnaSign == 7) { // Yogakaraka for Vrishabha and Tula
                nullified = true;
                reason = ts.getLabel("nullification.shani.yogakaraka");
            } else if ((satH == 4 || satH == 8 || satH == 12) && (saturnSign == 4 || saturnSign == 5 || saturnSign == 12)) { // Cancer, Leo, Pisces exemption
                nullified = true;
                reason = ts.getLabel("nullification.sevvai.house_sign_exemption");
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

    private void evaluateYogas(Map<String, PlanetaryPosition> d1Map, List<DiagnosticsDTO.YogaDetail> yogas) {
        int moonSign = d1Map.get("Moon").getSignNumber();
        int jupSign = d1Map.get("Jupiter").getSignNumber();
        int jupFromMoon = PlanetDignityUtils.getHouseFromLagna(jupSign, moonSign);
        
        if (jupFromMoon == 1 || jupFromMoon == 4 || jupFromMoon == 7 || jupFromMoon == 10) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder()
                    .name(ts.getLabel("yoga.gajakesari"))
                    .description(ts.getLabel("yoga.gajakesari.desc"))
                    .impactLevel(ts.getLabel("severity.high"))
                    .build());
        }
        
        PlanetaryPosition sunPos = d1Map.get("Sun");
        PlanetaryPosition merPos = d1Map.get("Mercury");
        if (sunPos.getSignNumber() == merPos.getSignNumber() && !PlanetDignityUtils.isCombust("Mercury", merPos.getAbsoluteLongitude(), sunPos.getAbsoluteLongitude())) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder()
                    .name(ts.getLabel("yoga.budha_aditya"))
                    .description(ts.getLabel("yoga.budha_aditya.desc"))
                    .impactLevel(ts.getLabel("severity.medium"))
                    .build());
        }
        
        int marsSign = d1Map.get("Mars").getSignNumber();
        if (moonSign == marsSign) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder()
                    .name(ts.getLabel("yoga.chandra_mangal"))
                    .description(ts.getLabel("yoga.chandra_mangal.desc"))
                    .impactLevel(ts.getLabel("severity.medium"))
                    .build());
        }
        
        int lagnaSign = d1Map.get("Lagna").getSignNumber();
        
        // Pancha Mahapurusha Yogas
        int marsH = PlanetDignityUtils.getHouseFromLagna(marsSign, lagnaSign);
        if ((marsH == 1 || marsH == 4 || marsH == 7 || marsH == 10) && (PlanetDignityUtils.isOwnSign("Mars", marsSign) || PlanetDignityUtils.isExalted("Mars", marsSign))) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.ruchaka")).description(ts.getLabel("yoga.ruchaka.desc")).impactLevel(ts.getLabel("severity.high")).build());
        }
        
        int merSign = merPos.getSignNumber();
        int merH = PlanetDignityUtils.getHouseFromLagna(merSign, lagnaSign);
        if ((merH == 1 || merH == 4 || merH == 7 || merH == 10) && (PlanetDignityUtils.isOwnSign("Mercury", merSign) || PlanetDignityUtils.isExalted("Mercury", merSign))) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.bhadra")).description(ts.getLabel("yoga.bhadra.desc")).impactLevel(ts.getLabel("severity.high")).build());
        }
        
        int jupH = PlanetDignityUtils.getHouseFromLagna(jupSign, lagnaSign);
        if ((jupH == 1 || jupH == 4 || jupH == 7 || jupH == 10) && (PlanetDignityUtils.isOwnSign("Jupiter", jupSign) || PlanetDignityUtils.isExalted("Jupiter", jupSign))) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.hamsa")).description(ts.getLabel("yoga.hamsa.desc")).impactLevel(ts.getLabel("severity.high")).build());
        }
        
        int venSign = d1Map.get("Venus").getSignNumber();
        int venH = PlanetDignityUtils.getHouseFromLagna(venSign, lagnaSign);
        if ((venH == 1 || venH == 4 || venH == 7 || venH == 10) && (PlanetDignityUtils.isOwnSign("Venus", venSign) || PlanetDignityUtils.isExalted("Venus", venSign))) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.malavya")).description(ts.getLabel("yoga.malavya.desc")).impactLevel(ts.getLabel("severity.high")).build());
        }
        
        int satSign = d1Map.get("Saturn").getSignNumber();
        int satH = PlanetDignityUtils.getHouseFromLagna(satSign, lagnaSign);
        if ((satH == 1 || satH == 4 || satH == 7 || satH == 10) && (PlanetDignityUtils.isOwnSign("Saturn", satSign) || PlanetDignityUtils.isExalted("Saturn", satSign))) {
            yogas.add(DiagnosticsDTO.YogaDetail.builder().name(ts.getLabel("yoga.sasa")).description(ts.getLabel("yoga.sasa.desc")).impactLevel(ts.getLabel("severity.high")).build());
        }

        // Dharma-Karmadhipati Yoga (9th & 10th Lords conjunction or mutual aspect)
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

        // Kendra-Trikona Rajayogam (Conjunction of Kendra Lord 1,4,7,10 and Trikona Lord 5,9)
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

        // Neechabhanga Raja Yoga check (Fully localized)
        String[] checkPlanets = {"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn"};
        for (String pKey : checkPlanets) {
            PlanetaryPosition p = d1Map.get(pKey);
            if (p != null && PlanetDignityUtils.isDebilitated(pKey, p.getSignNumber())) {
                String lord = PlanetDignityUtils.getSignLord(p.getSignNumber());
                PlanetaryPosition lordPos = d1Map.get(lord);
                if (lordPos != null) {
                    int lordHFromLagna = PlanetDignityUtils.getHouseFromLagna(lordPos.getSignNumber(), lagnaSign);
                    int lordHFromMoon = PlanetDignityUtils.getHouseFromLagna(lordPos.getSignNumber(), moonSign);
                    if (lordHFromLagna == 1 || lordHFromLagna == 4 || lordHFromLagna == 7 || lordHFromLagna == 10 || lordHFromMoon == 1 || lordHFromMoon == 4 || lordHFromMoon == 7 || lordHFromMoon == 10) {
                        String localizedPlanet = ts.getLabel("planet." + pKey.toLowerCase());
                        String localizedLord = ts.getLabel("planet." + lord.toLowerCase());
                        yogas.add(DiagnosticsDTO.YogaDetail.builder()
                                .name(ts.getLabel("yoga.neechabhanga") + " (" + localizedPlanet + ")")
                                .description(ts.getLabel("yoga.neechabhanga.desc") + " (" + localizedPlanet + " / " + localizedLord + ")")
                                .impactLevel(ts.getLabel("severity.high"))
                                .build());
                    }
                }
            }
        // Vipareeta Rajayoga (6th, 8th, 12th Lords in 6th, 8th, or 12th House)
        int[] trikHouses = {6, 8, 12};
        for (int th : trikHouses) {
            int trikSign = ((lagnaSign + th - 2 + 12) % 12) + 1;
            String lord = PlanetDignityUtils.getSignLord(trikSign);
            PlanetaryPosition lordPos = d1Map.get(lord);
            if (lordPos != null) {
                int lordH = PlanetDignityUtils.getHouseFromLagna(lordPos.getSignNumber(), lagnaSign);
                if (lordH == 6 || lordH == 8 || lordH == 12) {
                    yogas.add(DiagnosticsDTO.YogaDetail.builder()
                            .name(ts.getLabel("yoga.vipareeta") + " (" + ts.getLabel("planet." + lord.toLowerCase()) + ")")
                            .description(ts.getLabel("yoga.vipareeta.desc"))
                            .impactLevel(ts.getLabel("severity.high"))
                            .build());
                    break;
                }
            }
        }
    }
}
