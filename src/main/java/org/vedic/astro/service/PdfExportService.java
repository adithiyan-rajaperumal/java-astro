package org.vedic.astro.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.dto.ComprehensiveReportDTO;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PdfExportService {

    private static final ThreadLocal<BaseFont> currentEngBf = new ThreadLocal<>();
    private final TranslationService ts;
    public PdfExportService(TranslationService ts) { this.ts = ts; }

    public byte[] generateAstrologyReport(ComprehensiveReportDTO data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // 1. FOOLPROOF FONT STREAM INJECTION FROM CLASS-PATH REGISTRY
            Locale locale = LocaleContextHolder.getLocale();
            String lang = locale.getLanguage();
            String fontFile = switch (lang) {
                case "ta" -> "Bamini.ttf";
                case "hi" -> "NotoSansDevanagari-Regular.ttf";
                case "te" -> "NotoSansTelugu-Regular.ttf";
                case "kn" -> "NotoSansKannada-Regular.ttf";
                case "ml" -> "NotoSansMalayalam-Regular.ttf";
                default -> "NotoSans-Regular.ttf";
            };

            ClassPathResource resource = new ClassPathResource("fonts/" + fontFile);
            byte[] fontBytes = resource.getInputStream().readAllBytes();
            BaseFont bf = BaseFont.createFont(fontFile, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, BaseFont.CACHED, fontBytes, null);

            BaseFont engBf;
            if ("ta".equalsIgnoreCase(lang)) {
                ClassPathResource engResource = new ClassPathResource("fonts/NotoSans-Regular.ttf");
                byte[] engFontBytes = engResource.getInputStream().readAllBytes();
                engBf = BaseFont.createFont("NotoSans-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, BaseFont.CACHED, engFontBytes, null);
            } else {
                engBf = bf;
            }
            currentEngBf.set(engBf);

            boolean isEnglish = "en".equalsIgnoreCase(lang);
            int boldStyle = isEnglish ? Font.BOLD : Font.NORMAL;

            Font tFont = new Font(bf, 18, boldStyle);
            Font sFont = new Font(bf, 13, boldStyle);
            Font bFont = new Font(bf, 9, Font.NORMAL);
            Font boldB = new Font(bf, 9, boldStyle);
            Font chartFont = new Font(bf, 8, Font.NORMAL);

            Font engTFont = new Font(engBf, 18, boldStyle);
            Font engSFont = new Font(engBf, 13, boldStyle);
            Font engBFont = new Font(engBf, 9, Font.NORMAL);
            Font engBoldB = new Font(engBf, 9, boldStyle);
            Font engChartFont = new Font(engBf, 8, Font.NORMAL);
            boolean isHi = "hi".equalsIgnoreCase(lang);
            boolean isKn = "kn".equalsIgnoreCase(lang);

            // Kannada needs smaller chart fonts to prevent overlap in tight grid cells
            Font chartFontActual = isKn ? new Font(bf, 7, Font.NORMAL) : chartFont;
            Font chartBoldActual = isKn ? new Font(bf, 8, boldStyle) : boldB;

            // Document Main Headline
            Paragraph title = buildMixedParagraph(ts.getLabel("pdf.report.title"), tFont, engTFont);
            title.setAlignment(Element.ALIGN_CENTER); title.setSpacingAfter(14);
            document.add(title);

            // UPGRADED METADATA LAYOUT MATRIX: 4-Column design prevents text wrapping splits entirely
            PdfPTable info = new PdfPTable(4);
            info.setWidthPercentage(100);
            info.setSpacingAfter(12);
            info.setWidths(new float[]{18f, 32f, 18f, 32f});

            Font nameFont = containsTamil(data.getName()) ? bFont : engBFont;
            info.addCell(buildTableCell(ts.getLabel("pdf.info.name"), boldB, Element.ALIGN_LEFT));
            info.addCell(buildTableCell(org.vedic.astro.util.IndicPreShaper.shape(data.getName()), nameFont, Element.ALIGN_LEFT));
            info.addCell(buildTableCell(ts.getLabel("pdf.info.timezone"), boldB, Element.ALIGN_LEFT));
            info.addCell(buildTableCell(data.getResolvedTimezone(), engBFont, Element.ALIGN_LEFT));

            info.addCell(buildTableCell(ts.getLabel("pdf.info.dob"), boldB, Element.ALIGN_LEFT));
            info.addCell(buildTableCell(data.getDateOfBirth(), engBFont, Element.ALIGN_LEFT));
            info.addCell(buildTableCell(ts.getLabel("pdf.info.tob"), boldB, Element.ALIGN_LEFT));
            info.addCell(buildTableCell(data.getTimeOfBirth(), engBFont, Element.ALIGN_LEFT));

            info.addCell(buildTableCell(ts.getLabel("pdf.info.lmt"), boldB, Element.ALIGN_LEFT));
            info.addCell(buildTableCell(data.getLocalMeanTime(), engBFont, Element.ALIGN_LEFT));
            info.addCell(buildTableCell(ts.getLabel("pdf.info.ayanamsa"), boldB, Element.ALIGN_LEFT));
            String ayanKey = "ayanamsa." + (data.getAyanamsa() != null ? data.getAyanamsa().toUpperCase() : "LAHIRI");
            info.addCell(buildTableCell(ts.getLabel(ayanKey), bFont, Element.ALIGN_LEFT));

            info.addCell(buildTableCell(ts.getLabel("pdf.info.lat"), boldB, Element.ALIGN_LEFT));
            info.addCell(buildTableCell(String.valueOf(data.getLatitude()), engBFont, Element.ALIGN_LEFT));
            info.addCell(buildTableCell(ts.getLabel("pdf.info.long"), boldB, Element.ALIGN_LEFT));
            info.addCell(buildTableCell(String.valueOf(data.getLongitude()), engBFont, Element.ALIGN_LEFT));
            document.add(info);

            // UPGRADED PANCHANGAM BAR: 2-Column layout expands width, stopping label fragmentation
            PdfPTable panchangamTable = new PdfPTable(2);
            panchangamTable.setWidthPercentage(100);
            panchangamTable.setSpacingAfter(14);
            panchangamTable.setWidths(new float[]{50f, 50f});

            // Row 1: Lagna & Rashi (Left), Nakshatra & Pada (Right)
            panchangamTable.addCell(buildTableCell(ts.getLabel("profile.lagna") + ": " + data.getBirthProfile().getLagna() + "  |  " + ts.getLabel("profile.rashi") + ": " + data.getBirthProfile().getRashi(), boldB, Element.ALIGN_LEFT));
            panchangamTable.addCell(buildTableCell(ts.getLabel("profile.nakshatra") + ": " + data.getBirthProfile().getNakshatra() + "  |  " + ts.getLabel("profile.pada") + ": " + data.getBirthProfile().getNakshatraPada(), boldB, Element.ALIGN_LEFT));

            // Row 2: Thithi (Left), Yogam (Right)
            panchangamTable.addCell(buildTableCell(ts.getLabel("pdf.panchangam.thithi") + ": " + data.getThithi(), bFont, Element.ALIGN_LEFT));
            panchangamTable.addCell(buildTableCell(ts.getLabel("pdf.panchangam.yogam") + ": " + data.getYogam(), bFont, Element.ALIGN_LEFT));

            // Row 3: Karanam (Spans both columns)
            PdfPCell karanamCell = buildTableCell(ts.getLabel("pdf.panchangam.karanam") + ": " + data.getKaranam(), bFont, Element.ALIGN_LEFT);
            karanamCell.setColspan(2);
            panchangamTable.addCell(karanamCell);

            document.add(panchangamTable);

            // UPGRADED COORDS MATRIX: Structured widths protect multi-character Tamil strings
            document.add(buildMixedParagraph(ts.getLabel("pdf.pos.title"), sFont, engSFont)); document.add(new Paragraph(" ", bFont));
            PdfPTable posTab = new PdfPTable(5);
            posTab.setWidthPercentage(100);
            posTab.setSpacingAfter(15);
            posTab.setWidths(new float[]{14f, 16f, 12f, 22f, 36f}); // Allots 36% safety zone to degree text strings

            posTab.addCell(buildTableCell(ts.getLabel("pdf.pos.hdr.key"), boldB, Element.ALIGN_CENTER));
            posTab.addCell(buildTableCell(ts.getLabel("pdf.pos.hdr.name"), boldB, Element.ALIGN_CENTER));
            posTab.addCell(buildTableCell(ts.getLabel("pdf.pos.hdr.sign"), boldB, Element.ALIGN_CENTER));
            posTab.addCell(buildTableCell(ts.getLabel("pdf.pos.hdr.rashi"), boldB, Element.ALIGN_CENTER));
            posTab.addCell(buildTableCell(ts.getLabel("pdf.pos.hdr.long"), boldB, Element.ALIGN_CENTER));

            for (var p : data.getBirthPlanetaryPositions()) {
                posTab.addCell(buildTableCell(p.getPlanetKey(), engBFont, Element.ALIGN_CENTER));
                posTab.addCell(buildTableCell(p.getDisplayName(), bFont, Element.ALIGN_CENTER));
                posTab.addCell(buildTableCell(String.valueOf(p.getSignNumber()), engBFont, Element.ALIGN_CENTER));
                posTab.addCell(buildTableCell(p.getRashiName(), bFont, Element.ALIGN_CENTER));
                posTab.addCell(buildTableCell(p.getFormattedDegree(), engBFont, Element.ALIGN_CENTER));
            }
            document.add(posTab);

            // ==========================================
            // SECTION 3: REWRITTEN 2-COLUMN 12-CHART SUITE GRID
            // ==========================================
            document.newPage();
            document.add(buildMixedParagraph(ts.getLabel("pdf.chart.suite.title"), sFont, engSFont));
            document.add(new Paragraph(" ", bFont));

            PdfPTable masterGrid = new PdfPTable(2); masterGrid.setWidthPercentage(100); masterGrid.setSplitRows(true);
            String[] vargaKeys = {
                    "pdf.chart.d1", "pdf.chart.d2", "pdf.chart.d3", "pdf.chart.bhava",
                    "pdf.chart.d7", "pdf.chart.d9", "pdf.chart.d10", "pdf.chart.d12",
                    "pdf.chart.d20", "pdf.chart.d24", "pdf.chart.d30", "pdf.chart.d60"
            };
            String[] chartMapKeys = {
                    "d1", "d2", "d3", "bhava", "d7", "d9", "d10", "d12", "d20", "d24", "d30", "d60"
            };

            for (int i = 0; i < 12; i++) {
                PdfPCell layoutCell = new PdfPCell(); layoutCell.setBorder(PdfPCell.NO_BORDER); layoutCell.setPadding(6);
                String resolvedTitleText = ts.getLabel(vargaKeys[i]);

                List<ChartResponseDTO.PositionDetail> planets = data.getVargaChartsMap() != null ? data.getVargaChartsMap().get(chartMapKeys[i]) : null;
                if (planets != null) {
                    if (isHi) {
                        Paragraph chartLabel = buildMixedParagraph(resolvedTitleText, boldB, engBoldB);
                        chartLabel.setSpacingAfter(4);
                        layoutCell.addElement(chartLabel);
                        layoutCell.addElement(buildNorthIndianTemplateImage(writer, planets, chartFontActual, engBf));
                    } else {
                        layoutCell.addElement(buildCleanSouthIndianGrid(planets, resolvedTitleText, chartFontActual, chartBoldActual, isKn));
                    }
                }
                masterGrid.addCell(layoutCell);
            }
            masterGrid.completeRow();
            document.add(masterGrid);

            // UPGRADED SHADBALA GRID: Broad status channels eliminate wrapping text splits
            document.newPage(); document.add(buildMixedParagraph(ts.getLabel("pdf.shadbala.title"), sFont, engSFont)); document.add(new Paragraph(" ", bFont));
            PdfPTable sb = new PdfPTable(7);
            sb.setWidthPercentage(100);
            sb.setSpacingAfter(15);
            sb.setWidths(new float[]{16f, 12f, 12f, 12f, 12f, 14f, 22f}); // 22% handles long words like "மிகவும் பலம்" cleanly

            sb.addCell(buildTableCell(ts.getLabel("pdf.shadbala.hdr.planet"), boldB, Element.ALIGN_CENTER));
            sb.addCell(buildTableCell(ts.getLabel("pdf.shadbala.hdr.sthana"), boldB, Element.ALIGN_CENTER));
            sb.addCell(buildTableCell(ts.getLabel("pdf.shadbala.hdr.dig"), boldB, Element.ALIGN_CENTER));
            sb.addCell(buildTableCell(ts.getLabel("pdf.shadbala.hdr.kala"), boldB, Element.ALIGN_CENTER));
            sb.addCell(buildTableCell(ts.getLabel("pdf.shadbala.hdr.cheshta"), boldB, Element.ALIGN_CENTER));
            sb.addCell(buildTableCell(ts.getLabel("pdf.shadbala.hdr.total"), boldB, Element.ALIGN_CENTER));
            sb.addCell(buildTableCell(ts.getLabel("pdf.shadbala.hdr.status"), boldB, Element.ALIGN_CENTER));

            data.getShadbalaStrengths().getPlanetStrengths().forEach((p, s) -> {
                String localizedPName = ts.getLabel("planet." + p.toUpperCase() + ".short");
                String localizedStatus = ts.getLabel("shadbala.status." + s.getStrengthCategory().toLowerCase().replaceAll("\\s+", ""));
                sb.addCell(buildTableCell(localizedPName, bFont, Element.ALIGN_CENTER));
                sb.addCell(buildTableCell(String.format("%.1f", s.getSthanaBala()), engBFont, Element.ALIGN_CENTER));
                sb.addCell(buildTableCell(String.format("%.1f", s.getDigBala()), engBFont, Element.ALIGN_CENTER));
                sb.addCell(buildTableCell(String.format("%.1f", s.getKalaBala()), engBFont, Element.ALIGN_CENTER));
                sb.addCell(buildTableCell(String.format("%.1f", s.getCheshtaBala()), engBFont, Element.ALIGN_CENTER));
                sb.addCell(buildTableCell(String.format("%.2f", s.getTotalShadbalaRupas()), engBFont, Element.ALIGN_CENTER));
                sb.addCell(buildTableCell(localizedStatus, bFont, Element.ALIGN_CENTER));
            });
            document.add(sb);

            // UPGRADED DASA TIMELINE: Defined widths prevent tracking timeline faults
            document.newPage(); document.add(buildMixedParagraph(ts.getLabel("pdf.dasa.title"), sFont, engSFont)); document.add(new Paragraph(" ", bFont));
            PdfPTable ds = new PdfPTable(4);
            ds.setWidthPercentage(100);
            ds.setSplitRows(true);
            ds.setHeaderRows(1);
            ds.setWidths(new float[]{20f, 25f, 27f, 28f});

            ds.addCell(buildTableCell(ts.getLabel("pdf.dasa.hdr.mahadasa"), boldB, Element.ALIGN_CENTER));
            ds.addCell(buildTableCell(ts.getLabel("pdf.dasa.hdr.bhukthi"), boldB, Element.ALIGN_CENTER));
            ds.addCell(buildTableCell(ts.getLabel("pdf.dasa.hdr.start"), boldB, Element.ALIGN_CENTER));
            ds.addCell(buildTableCell(ts.getLabel("pdf.dasa.hdr.end"), boldB, Element.ALIGN_CENTER));

            for (var d : data.getVimshottariTimeline()) {
                String locMaha = ts.getLabel("planet." + d.getPlanetName().toUpperCase() + ".short");
                PdfPCell m = buildTableCell(locMaha + " " + ts.getLabel("pdf.dasa.label.mahadasa"), boldB, Element.ALIGN_LEFT);
                m.setBackgroundColor(java.awt.Color.LIGHT_GRAY); m.setColspan(2); ds.addCell(m);

                PdfPCell s = buildTableCell(d.getStartDate().toString(), engBoldB, Element.ALIGN_CENTER); s.setBackgroundColor(java.awt.Color.LIGHT_GRAY); ds.addCell(s);
                PdfPCell e = buildTableCell(d.getEndDate().toString(), engBoldB, Element.ALIGN_CENTER); e.setBackgroundColor(java.awt.Color.LIGHT_GRAY); ds.addCell(e);

                for (var b : d.getBhukthis()) {
                    String locBhuk = ts.getLabel("planet." + b.getPlanetName().toUpperCase() + ".short");
                    ds.addCell(buildTableCell("", bFont, Element.ALIGN_CENTER));
                    ds.addCell(buildTableCell(locBhuk, bFont, Element.ALIGN_LEFT));
                    ds.addCell(buildTableCell(b.getStartDate().toString(), engBFont, Element.ALIGN_CENTER));
                    ds.addCell(buildTableCell(b.getEndDate().toString(), engBFont, Element.ALIGN_CENTER));
                }
            }
            document.add(ds);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            currentEngBf.remove();
        }
        return out.toByteArray();
    }

    public byte[] generateMarriageMatchingReport(org.vedic.astro.matching.dto.MatchingResponseDTO data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            Locale locale = LocaleContextHolder.getLocale();
            String lang = locale.getLanguage();
            String fontFile = switch (lang) {
                case "ta" -> "Bamini.ttf";
                case "hi" -> "NotoSansDevanagari-Regular.ttf";
                case "te" -> "NotoSansTelugu-Regular.ttf";
                case "kn" -> "NotoSansKannada-Regular.ttf";
                case "ml" -> "NotoSansMalayalam-Regular.ttf";
                default -> "NotoSans-Regular.ttf";
            };

            ClassPathResource resource = new ClassPathResource("fonts/" + fontFile);
            byte[] fontBytes = resource.getInputStream().readAllBytes();
            BaseFont bf = BaseFont.createFont(fontFile, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, BaseFont.CACHED, fontBytes, null);

            BaseFont engBf;
            if ("ta".equalsIgnoreCase(lang)) {
                ClassPathResource engResource = new ClassPathResource("fonts/NotoSans-Regular.ttf");
                byte[] engFontBytes = engResource.getInputStream().readAllBytes();
                engBf = BaseFont.createFont("NotoSans-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, BaseFont.CACHED, engFontBytes, null);
            } else {
                engBf = bf;
            }
            currentEngBf.set(engBf);

            boolean isEnglish = "en".equalsIgnoreCase(lang);
            int boldStyle = isEnglish ? Font.BOLD : Font.NORMAL;

            Font tFont = new Font(bf, 18, boldStyle);
            Font sFont = new Font(bf, 13, boldStyle);
            Font bFont = new Font(bf, 9, Font.NORMAL);
            Font boldB = new Font(bf, 9, boldStyle);
            Font chartFont = new Font(bf, 8, Font.NORMAL);

            Font engTFont = new Font(engBf, 18, boldStyle);
            Font engSFont = new Font(engBf, 13, boldStyle);
            Font engBFont = new Font(engBf, 9, Font.NORMAL);
            Font engBoldB = new Font(engBf, 9, boldStyle);
            boolean isHi = "hi".equalsIgnoreCase(lang);
            boolean isKn = "kn".equalsIgnoreCase(lang);

            Font chartFontActual = isKn ? new Font(bf, 7, Font.NORMAL) : chartFont;
            Font chartBoldActual = isKn ? new Font(bf, 8, boldStyle) : boldB;

            Paragraph title = buildMixedParagraph(ts.getLabel("matching.pdf.title"), tFont, engTFont);
            title.setAlignment(Element.ALIGN_CENTER); title.setSpacingAfter(14);
            document.add(title);

            PdfPTable profiles = new PdfPTable(4);
            profiles.setWidthPercentage(100);
            profiles.setSpacingAfter(12);
            profiles.setWidths(new float[]{20f, 30f, 20f, 30f});

            PdfPCell hBoy = buildTableCell(ts.getLabel("matching.pdf.boy"), boldB, Element.ALIGN_CENTER);
            hBoy.setColspan(2); hBoy.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            profiles.addCell(hBoy);

            PdfPCell hGirl = buildTableCell(ts.getLabel("matching.pdf.girl"), boldB, Element.ALIGN_CENTER);
            hGirl.setColspan(2); hGirl.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            profiles.addCell(hGirl);

            profiles.addCell(buildTableCell(ts.getLabel("pdf.info.name"), boldB, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(data.getBoyProfile().getName(), bFont, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(ts.getLabel("pdf.info.name"), boldB, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(data.getGirlProfile().getName(), bFont, Element.ALIGN_LEFT));

            profiles.addCell(buildTableCell(ts.getLabel("pdf.info.dob"), boldB, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(data.getBoyProfile().getDateOfBirth(), engBFont, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(ts.getLabel("pdf.info.dob"), boldB, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(data.getGirlProfile().getDateOfBirth(), engBFont, Element.ALIGN_LEFT));

            profiles.addCell(buildTableCell(ts.getLabel("pdf.info.tob"), boldB, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(data.getBoyProfile().getTimeOfBirth(), engBFont, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(ts.getLabel("pdf.info.tob"), boldB, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(data.getGirlProfile().getTimeOfBirth(), engBFont, Element.ALIGN_LEFT));

            profiles.addCell(buildTableCell(ts.getLabel("profile.lagna"), boldB, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(data.getBoyProfile().getBirthProfile().getLagna(), bFont, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(ts.getLabel("profile.lagna"), boldB, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(data.getGirlProfile().getBirthProfile().getLagna(), bFont, Element.ALIGN_LEFT));

            profiles.addCell(buildTableCell(ts.getLabel("profile.rashi"), boldB, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(data.getBoyProfile().getBirthProfile().getRashi(), bFont, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(ts.getLabel("profile.rashi"), boldB, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(data.getGirlProfile().getBirthProfile().getRashi(), bFont, Element.ALIGN_LEFT));

            profiles.addCell(buildTableCell(ts.getLabel("profile.nakshatra"), boldB, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(data.getBoyProfile().getBirthProfile().getNakshatra() + " (" + data.getBoyProfile().getBirthProfile().getNakshatraPada() + ")", bFont, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(ts.getLabel("profile.nakshatra"), boldB, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(data.getGirlProfile().getBirthProfile().getNakshatra() + " (" + data.getGirlProfile().getBirthProfile().getNakshatraPada() + ")", bFont, Element.ALIGN_LEFT));

            profiles.addCell(buildTableCell(ts.getLabel("pdf.info.ayanamsa"), boldB, Element.ALIGN_LEFT));
            String boyAyan = "ayanamsa." + (data.getBoyProfile().getAyanamsa() != null ? data.getBoyProfile().getAyanamsa().toUpperCase() : "LAHIRI");
            profiles.addCell(buildTableCell(ts.getLabel(boyAyan), bFont, Element.ALIGN_LEFT));
            profiles.addCell(buildTableCell(ts.getLabel("pdf.info.ayanamsa"), boldB, Element.ALIGN_LEFT));
            String girlAyan = "ayanamsa." + (data.getGirlProfile().getAyanamsa() != null ? data.getGirlProfile().getAyanamsa().toUpperCase() : "LAHIRI");
            profiles.addCell(buildTableCell(ts.getLabel(girlAyan), bFont, Element.ALIGN_LEFT));

            document.add(profiles);

            PdfPTable scoreTable = new PdfPTable(2);
            scoreTable.setWidthPercentage(100);
            scoreTable.setSpacingAfter(14);
            scoreTable.setWidths(new float[]{50f, 50f});

            scoreTable.addCell(buildTableCell(ts.getLabel("matching.pdf.score") + ": " + String.format("%.1f", data.getTotalScore()) + " / " + String.format("%.1f", data.getMaxScore()) + " (" + String.format("%.1f", data.getPercentage()) + "%)", boldB, Element.ALIGN_LEFT));
            scoreTable.addCell(buildTableCell(ts.getLabel("matching.pdf.verdict") + ": " + data.getVerdict(), boldB, Element.ALIGN_LEFT));

            document.add(scoreTable);

            if (data.getWarnings() != null && !data.getWarnings().isEmpty()) {
                document.add(buildMixedParagraph(ts.getLabel("matching.pdf.warnings"), sFont, engSFont));
                for (String warning : data.getWarnings()) {
                    Paragraph wp = buildMixedParagraph("• " + warning, bFont, engBFont);
                    wp.setSpacingAfter(4);
                    document.add(wp);
                }
                document.add(new Paragraph(" ", bFont));
            }

            document.add(buildMixedParagraph(ts.getLabel("pdf.chart.suite.title") + " (D1 Rasi)", sFont, engSFont));
            document.add(new Paragraph(" ", bFont));

            PdfPTable chartGrid = new PdfPTable(2);
            chartGrid.setWidthPercentage(100);
            chartGrid.setSpacingAfter(15);

            PdfPCell boyCell = new PdfPCell(); boyCell.setBorder(PdfPCell.NO_BORDER); boyCell.setPadding(6);
            if (isHi) {
                boyCell.addElement(buildMixedParagraph(data.getBoyProfile().getName() + " - D1", boldB, engBoldB));
                boyCell.addElement(buildNorthIndianTemplateImage(writer, data.getBoyProfile().getD1Chart(), chartFontActual, engBf));
            } else {
                boyCell.addElement(buildCleanSouthIndianGrid(data.getBoyProfile().getD1Chart(), data.getBoyProfile().getName() + " - D1", chartFontActual, chartBoldActual, isKn));
            }
            chartGrid.addCell(boyCell);

            PdfPCell girlCell = new PdfPCell(); girlCell.setBorder(PdfPCell.NO_BORDER); girlCell.setPadding(6);
            if (isHi) {
                girlCell.addElement(buildMixedParagraph(data.getGirlProfile().getName() + " - D1", boldB, engBoldB));
                girlCell.addElement(buildNorthIndianTemplateImage(writer, data.getGirlProfile().getD1Chart(), chartFontActual, engBf));
            } else {
                girlCell.addElement(buildCleanSouthIndianGrid(data.getGirlProfile().getD1Chart(), data.getGirlProfile().getName() + " - D1", chartFontActual, chartBoldActual, isKn));
            }
            chartGrid.addCell(girlCell);

            document.add(chartGrid);

            document.newPage();
            document.add(buildMixedParagraph(ts.getLabel("matching.pdf.koota"), sFont, engSFont));
            document.add(new Paragraph(" ", bFont));

            PdfPTable kTable = new PdfPTable(5);
            kTable.setWidthPercentage(100);
            kTable.setWidths(new float[]{20f, 12f, 12f, 16f, 40f});

            kTable.addCell(buildTableCell(ts.getLabel("matching.pdf.koota"), boldB, Element.ALIGN_CENTER));
            kTable.addCell(buildTableCell(ts.getLabel("matching.pdf.max"), boldB, Element.ALIGN_CENTER));
            kTable.addCell(buildTableCell(ts.getLabel("matching.pdf.scored"), boldB, Element.ALIGN_CENTER));
            kTable.addCell(buildTableCell(ts.getLabel("matching.pdf.status"), boldB, Element.ALIGN_CENTER));
            kTable.addCell(buildTableCell(ts.getLabel("matching.pdf.notes"), boldB, Element.ALIGN_CENTER));

            for (var k : data.getKootas()) {
                kTable.addCell(buildTableCell(k.getName(), bFont, Element.ALIGN_LEFT));
                kTable.addCell(buildTableCell(String.valueOf(k.getMaxPoints()), engBFont, Element.ALIGN_CENTER));
                kTable.addCell(buildTableCell(String.valueOf(k.getScoredPoints()), engBFont, Element.ALIGN_CENTER));
                
                String statusText = switch (k.getStatus()) {
                    case MATCHED -> ts.getLabel("matching.status.matched");
                    case MATCHED_VIA_NULLIFICATION -> ts.getLabel("matching.status.nullified");
                    case NOT_MATCHED -> ts.getLabel("matching.status.notmatched");
                };
                kTable.addCell(buildTableCell(statusText, bFont, Element.ALIGN_CENTER));

                String note = (k.getStatus() == org.vedic.astro.matching.dto.KootaResultDTO.MatchStatus.MATCHED_VIA_NULLIFICATION)
                        ? k.getNullificationReason() : k.getDescription();
                kTable.addCell(buildTableCell(note, bFont, Element.ALIGN_LEFT));
            }

            document.add(kTable);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            currentEngBf.remove();
        }
        return out.toByteArray();
    }

    // =========================================================================
    // SAFE CELL BUILDING ENGINE FOR COMPLEX VERTICAL INDIC FONTS
    // =========================================================================
    private Phrase buildMixedPhrase(String text, Font tamFont, Font engFont) {
        Phrase phrase = new Phrase();
        if (text == null || text.isEmpty()) {
            return phrase;
        }
        phrase.setLeading(tamFont.getSize() + 5f);
        boolean isTa = "ta".equalsIgnoreCase(LocaleContextHolder.getLocale().getLanguage());
        int i = 0;
        int len = text.length();
        StringBuilder currentSegment = new StringBuilder();
        boolean isCurrentTamil = false;

        while (i < len) {
            char c = text.charAt(i);
            boolean isTamil = isTa && (c >= '\u0B80' && c <= '\u0BFF');

            if (i == 0) {
                isCurrentTamil = isTamil;
            }

            if (isTamil == isCurrentTamil) {
                currentSegment.append(c);
            } else {
                String segmentStr = currentSegment.toString();
                if (isCurrentTamil) {
                    phrase.add(new Chunk(org.vedic.astro.util.BaminiConverter.convert(segmentStr), tamFont));
                } else {
                    phrase.add(new Chunk(segmentStr, engFont));
                }
                currentSegment.setLength(0);
                currentSegment.append(c);
                isCurrentTamil = isTamil;
            }
            i++;
        }

        if (currentSegment.length() > 0) {
            String segmentStr = currentSegment.toString();
            if (isCurrentTamil) {
                phrase.add(new Chunk(org.vedic.astro.util.BaminiConverter.convert(segmentStr), tamFont));
            } else {
                phrase.add(new Chunk(segmentStr, engFont));
            }
        }
        return phrase;
    }

    private Paragraph buildMixedParagraph(String text, Font tamFont, Font engFont) {
        return new Paragraph(buildMixedPhrase(text, tamFont, engFont));
    }

    private PdfPCell buildTableCell(String text, Font font, int alignment) {
        BaseFont engBase = currentEngBf.get();
        Font engFont = (engBase != null) ? new Font(engBase, font.getSize(), font.getStyle(), font.getColor()) : font;
        Phrase phrase = buildMixedPhrase(text, font, engFont);

        // Explicit leading safety margin prevents top/bottom modifier clipping
        phrase.setLeading(font.getSize() + 5f);

        PdfPCell cell = new PdfPCell(phrase);
        cell.setPaddingTop(6f);
        cell.setPaddingBottom(7f);
        cell.setPaddingLeft(6f);
        cell.setPaddingRight(6f);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPTable buildCleanSouthIndianGrid(List<ChartResponseDTO.PositionDetail> planets, String titleText, Font baseFont, Font titleFont, boolean isKn) {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        table.addCell(buildBoxCell(planets, 12, baseFont, isKn));
        table.addCell(buildBoxCell(planets, 1, baseFont, isKn));
        table.addCell(buildBoxCell(planets, 2, baseFont, isKn));
        table.addCell(buildBoxCell(planets, 3, baseFont, isKn));

        table.addCell(buildBoxCell(planets, 11, baseFont, isKn));

        BaseFont engBase = currentEngBf.get();
        Font engFont = (engBase != null) ? new Font(engBase, titleFont.getSize(), titleFont.getStyle(), titleFont.getColor()) : titleFont;
        Phrase titlePhrase = buildMixedPhrase(titleText, titleFont, engFont);
        float titleLeadingExtra = isKn ? 7f : 4f;
        titlePhrase.setLeading(titleFont.getSize() + titleLeadingExtra);
        PdfPCell centerBlock = new PdfPCell(titlePhrase);
        centerBlock.setColspan(2); centerBlock.setRowspan(2);
        centerBlock.setHorizontalAlignment(Element.ALIGN_CENTER);
        centerBlock.setVerticalAlignment(Element.ALIGN_MIDDLE);
        centerBlock.setPadding(4);
        centerBlock.setFixedHeight(isKn ? 116f : 104f);
        table.addCell(centerBlock);

        table.addCell(buildBoxCell(planets, 4, baseFont, isKn));
        table.addCell(buildBoxCell(planets, 10, baseFont, isKn));
        table.addCell(buildBoxCell(planets, 5, baseFont, isKn));

        table.addCell(buildBoxCell(planets, 9, baseFont, isKn));
        table.addCell(buildBoxCell(planets, 8, baseFont, isKn));
        table.addCell(buildBoxCell(planets, 7, baseFont, isKn));
        table.addCell(buildBoxCell(planets, 6, baseFont, isKn));

        return table;
    }

    private PdfPCell buildBoxCell(List<ChartResponseDTO.PositionDetail> planets, int targetSign, Font font, boolean isKn) {
        // Collect planetary abbreviations on clean vertical lines inside the grid boxes
        String inlinePlanets = planets.stream()
                .filter(p -> p.getSignNumber() == targetSign)
                .map(ChartResponseDTO.PositionDetail::getDisplayName)
                .collect(Collectors.joining("\n"));

        BaseFont engBase = currentEngBf.get();
        Font engFont = (engBase != null) ? new Font(engBase, font.getSize(), font.getStyle(), font.getColor()) : font;
        Phrase phrase = buildMixedPhrase(inlinePlanets, font, engFont);
        float leadingExtra = isKn ? 7f : 4f;
        phrase.setLeading(font.getSize() + leadingExtra);

        PdfPCell cell = new PdfPCell(phrase);
        cell.setFixedHeight(isKn ? 58f : 52f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(3f);
        cell.setPaddingBottom(3f);

        boolean isLagna = planets.stream()
                .anyMatch(p -> "LAGNA".equalsIgnoreCase(p.getPlanetKey()) && p.getSignNumber() == targetSign);
        if (isLagna) {
            cell.setCellEvent(new LagnaCellEvent());
        }

        return cell;
    }

    private Image buildNorthIndianTemplateImage(PdfWriter wr, List<ChartResponseDTO.PositionDetail> planets, Font font, BaseFont engBaseFont) throws Exception {
        PdfContentByte cb = wr.getDirectContent();
        PdfTemplate template = cb.createTemplate(160f, 160f);
        template.setColorStroke(java.awt.Color.BLACK); template.setLineWidth(0.8f);

        template.rectangle(0, 0, 160f, 160f);
        template.moveTo(0, 0); template.lineTo(160f, 160f);
        template.moveTo(0, 160f); template.lineTo(160f, 0);
        template.moveTo(80f, 0); template.lineTo(0, 80f); template.lineTo(80f, 160f); template.lineTo(160f, 80f); template.lineTo(80f, 0);
        template.stroke();

        float[][] targets = {{80f,115f}, {40f,135f}, {20f,100f}, {45f,80f}, {20f,60f}, {40f,25f}, {80f,45f}, {120f,25f}, {140f,60f}, {115f,80f}, {140f,100f}, {120f,135f}};
        int lagnaSign = planets.stream().filter(p -> "LAGNA".equalsIgnoreCase(p.getPlanetKey())).findFirst().orElseThrow().getSignNumber();
        BaseFont indicBf = font.getBaseFont();

        // Use NotoSans (English) for sign numbers to avoid Devanagari metric issues
        BaseFont numBf = (engBaseFont != null) ? engBaseFont : indicBf;
        float signNumSize = 6f;
        float planetTextSize = 7f;

        for (int i = 0; i < 12; i++) {
            int curSign = ((lagnaSign - 1 + i) % 12) + 1;
            float cx = targets[i][0]; float cy = targets[i][1];

            // Sign number: use English font, center dynamically based on actual glyph width
            String signStr = String.valueOf(curSign);
            float signWidth = numBf.getWidthPoint(signStr, signNumSize);
            template.beginText();
            template.setFontAndSize(numBf, signNumSize);
            template.setTextMatrix(cx - (signWidth / 2f), cy + 8f);
            template.showText(signStr);
            template.endText();

            // Planet text: use Indic font, center dynamically, placed well below sign number
            String pText = planets.stream()
                    .filter(p -> p.getSignNumber() == curSign && !"LAGNA".equalsIgnoreCase(p.getPlanetKey()))
                    .map(ChartResponseDTO.PositionDetail::getDisplayName)
                    .collect(Collectors.joining(" "));
            if (!pText.isEmpty()) {
                float pWidth = indicBf.getWidthPoint(pText, planetTextSize);
                template.beginText();
                template.setFontAndSize(indicBf, planetTextSize);
                template.setTextMatrix(cx - (pWidth / 2f), cy - 6f);
                template.showText(pText);
                template.endText();
            }
        }
        return Image.getInstance(template);
    }

    private boolean containsTamil(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '\u0B80' && c <= '\u0BFF') return true;
        }
        return false;
    }

    private static class LagnaCellEvent implements PdfPCellEvent {
        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte cb = canvases[PdfPTable.BASECANVAS];
            cb.saveState();
            cb.setLineWidth(0.6f);
            cb.setColorStroke(java.awt.Color.BLACK);
            cb.moveTo(position.getLeft(), position.getTop());
            cb.lineTo(position.getRight(), position.getBottom());
            cb.stroke();
            cb.restoreState();
        }
    }
}