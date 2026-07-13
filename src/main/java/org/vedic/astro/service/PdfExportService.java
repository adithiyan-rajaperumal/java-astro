package org.vedic.astro.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.dto.ComprehensiveReportDTO;
import org.vedic.astro.dto.ShadbalaDTO;
import org.vedic.astro.model.DasaPeriod;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PdfExportService {

    private final TranslationService ts;

    public PdfExportService(TranslationService ts) {
        this.ts = ts;
    }

    public byte[] generateAstrologyReport(ComprehensiveReportDTO data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // 1. DYNAMIC REGIONAL RESOURCE FONT SETUP
            Locale currentLocale = LocaleContextHolder.getLocale();
            String lang = currentLocale.getLanguage();

            String targetFontFile = switch (lang) {
                case "ta" -> "NotoSansTamil-Regular.ttf";
                case "hi" -> "NotoSansDevanagari-Regular.ttf";
                case "te" -> "NotoSansTelugu-Regular.ttf";
                case "kn" -> "NotoSansKannada-Regular.ttf";
                case "ml" -> "NotoSansMalayalam-Regular.ttf";
                default -> "NotoSans-Regular.ttf";
            };

            String fullFontPath = "src/main/resources/fonts/" + targetFontFile;
            FontFactory.register(fullFontPath, "RegionalFont");

            Font titleFont = FontFactory.getFont("RegionalFont", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 20, Font.BOLD);
            Font sectionFont = FontFactory.getFont("RegionalFont", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 14, Font.BOLD);
            Font bodyFont = FontFactory.getFont("RegionalFont", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9, Font.NORMAL);
            Font boldBody = FontFactory.getFont("RegionalFont", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9, Font.BOLD);

            boolean isHindiDiamond = "hi".equalsIgnoreCase(lang);

            // ==========================================
            // SECTION 1: SYSTEM REPORT HEADLINE & PROFILE CARD
            // ==========================================
            Paragraph mainTitle = new Paragraph(ts.getLabel("pdf.report.title"), titleFont);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            mainTitle.setSpacingAfter(15);
            document.add(mainTitle);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15);

            infoTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.info.name") + ": " + data.getName(), bodyFont)));
            infoTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.info.timezone") + ": " + data.getResolvedTimezone(), bodyFont)));
            infoTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.info.dob") + ": " + data.getDateOfBirth(), bodyFont)));
            infoTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.info.tob") + ": " + data.getTimeOfBirth(), bodyFont)));
            infoTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.info.lat") + ": " + data.getLatitude(), bodyFont)));
            infoTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.info.long") + ": " + data.getLongitude(), bodyFont)));

            for (PdfPCell cell : infoTable.getRows().stream().flatMap(r -> java.util.Arrays.stream(r.getCells())).collect(Collectors.toList())) {
                if (cell != null) cell.setPadding(6);
            }
            document.add(infoTable);

            document.add(new Paragraph(data.getBirthProfile().getLagna() + " | " + data.getBirthProfile().getRashi(), boldBody));
            document.add(new Paragraph(data.getBirthProfile().getNakshatra() + " - " + ts.getLabel("profile.pada") + ": " + data.getBirthProfile().getNakshatraPada(), bodyFont));
            document.add(new Paragraph(" ", bodyFont));

            // ==========================================
            // SECTION 2: PLANETARY COORDINATE TABLE MATRIX
            // ==========================================
            document.add(new Paragraph(ts.getLabel("pdf.pos.title"), sectionFont));
            document.add(new Paragraph(" ", bodyFont));

            PdfPTable posTable = new PdfPTable(5);
            posTable.setWidthPercentage(100);
            posTable.setSpacingAfter(20);

            posTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.pos.hdr.key"), boldBody)));
            posTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.pos.hdr.name"), boldBody)));
            posTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.pos.hdr.sign"), boldBody)));
            posTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.pos.hdr.rashi"), boldBody)));
            posTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.pos.hdr.long"), boldBody)));

            for (ChartResponseDTO.PositionDetail pos : data.getBirthPlanetaryPositions()) {
                posTable.addCell(new PdfPCell(new Phrase(pos.getPlanetKey(), bodyFont)));
                posTable.addCell(new PdfPCell(new Phrase(pos.getDisplayName(), bodyFont)));
                posTable.addCell(new PdfPCell(new Phrase(String.valueOf(pos.getSignNumber()), bodyFont)));
                posTable.addCell(new PdfPCell(new Phrase(pos.getRashiName(), bodyFont)));
                posTable.addCell(new PdfPCell(new Phrase(pos.getFormattedDegree(), bodyFont)));
            }
            document.add(posTable);

            // ==========================================
            // SECTION 3: THE 9 GEOMETRIC CHARTS SELECTION
            // ==========================================
            document.newPage();
            document.add(new Paragraph(ts.getLabel("pdf.chart.suite.title"), sectionFont));
            document.add(new Paragraph(" ", bodyFont));

            String[] vargaKeys = {
                    "pdf.chart.d1", "pdf.chart.d9", "pdf.chart.d10",
                    "pdf.chart.d7", "pdf.chart.d3", "pdf.chart.bhava",
                    "pdf.chart.d2", "pdf.chart.d12", "pdf.chart.d30"
            };

            for (int i = 0; i < 9; i++) {
                if (i > 0 && i % 2 == 0) {
                    document.newPage();
                }

                document.add(new Paragraph(ts.getLabel(vargaKeys[i]), boldBody));
                document.add(new Paragraph(" ", bodyFont));

                List<ChartResponseDTO.PositionDetail> currentChartPlanets = data.getVargaChartsSuite().get(i);

                if (isHindiDiamond) {
                    paintNorthIndianChart(document, writer, currentChartPlanets, bodyFont);
                } else {
                    paintSouthIndianChart(document, currentChartPlanets, bodyFont);
                }
                document.add(new Paragraph(" ", bodyFont));
            }

            // ==========================================
            // SECTION 4: SHADBALA MATRIX STRENGTH ANALYSIS
            // ==========================================
            document.newPage();
            document.add(new Paragraph(ts.getLabel("pdf.shadbala.title"), sectionFont));
            document.add(new Paragraph(" ", bodyFont));

            PdfPTable balaTable = new PdfPTable(7);
            balaTable.setWidthPercentage(100);
            balaTable.setSpacingAfter(20);

            balaTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.shadbala.hdr.planet"), boldBody)));
            balaTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.shadbala.hdr.sthana"), boldBody)));
            balaTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.shadbala.hdr.dig"), boldBody)));
            balaTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.shadbala.hdr.kala"), boldBody)));
            balaTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.shadbala.hdr.cheshta"), boldBody)));
            balaTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.shadbala.hdr.total"), boldBody)));
            balaTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.shadbala.hdr.status"), boldBody)));

            ShadbalaDTO balaData = data.getShadbalaStrengths();
            balaData.getPlanetStrengths().forEach((planet, strength) -> {
                // Fetch the localized planet key name on the fly
                String localizedPlanetName = ts.getLabel("planet." + planet.toUpperCase() + ".short");

                // Formulate the dynamic property search tag for strength classifications
                String localizedStatusTag = "shadbala.status." + strength.getStrengthCategory().toLowerCase().replaceAll("\\s+", "");
                String localizedStatus = ts.getLabel(localizedStatusTag);

                balaTable.addCell(new PdfPCell(new Phrase(localizedPlanetName, bodyFont)));
                balaTable.addCell(new PdfPCell(new Phrase(String.format("%.1f", strength.getSthanaBala()), bodyFont)));
                balaTable.addCell(new PdfPCell(new Phrase(String.format("%.1f", strength.getDigBala()), bodyFont)));
                balaTable.addCell(new PdfPCell(new Phrase(String.format("%.1f", strength.getKalaBala()), bodyFont)));
                balaTable.addCell(new PdfPCell(new Phrase(String.format("%.1f", strength.getCheshtaBala()), bodyFont)));
                balaTable.addCell(new PdfPCell(new Phrase(String.format("%.2f", strength.getTotalShadbalaRupas()), bodyFont)));
                balaTable.addCell(new PdfPCell(new Phrase(localizedStatus, bodyFont)));
            });
            document.add(balaTable);

            // ==========================================
            // SECTION 5: VIMSHOTTARI DASA & BHUKTHI CHRONOLOGY
            // ==========================================
            document.newPage();
            document.add(new Paragraph(ts.getLabel("pdf.dasa.title"), sectionFont));
            document.add(new Paragraph(" ", bodyFont));

            PdfPTable dasaTable = new PdfPTable(4);
            dasaTable.setWidthPercentage(100);

            dasaTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.dasa.hdr.mahadasa"), boldBody)));
            dasaTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.dasa.hdr.bhukthi"), boldBody)));
            dasaTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.dasa.hdr.start"), boldBody)));
            dasaTable.addCell(new PdfPCell(new Phrase(ts.getLabel("pdf.dasa.hdr.end"), boldBody)));

            for (DasaPeriod dasa : data.getVimshottariTimeline()) {
                String localizedMahaLord = ts.getLabel("planet." + dasa.getPlanetName().toUpperCase() + ".short");

                PdfPCell mCell = new PdfPCell(new Phrase(localizedMahaLord + " " + ts.getLabel("pdf.dasa.label.mahadasa"), boldBody));
                mCell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                mCell.setColspan(2);
                dasaTable.addCell(mCell);

                PdfPCell startCell = new PdfPCell(new Phrase(dasa.getStartDate().toString(), boldBody));
                startCell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                dasaTable.addCell(startCell);

                PdfPCell endCell = new PdfPCell(new Phrase(dasa.getEndDate().toString(), boldBody));
                endCell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                dasaTable.addCell(endCell);

                for (DasaPeriod.BhukthiPeriod bhukthi : dasa.getBhukthis()) {
                    String localizedBhukthiLord = ts.getLabel("planet." + bhukthi.getPlanetName().toUpperCase() + ".short");

                    dasaTable.addCell(new PdfPCell(new Phrase("", bodyFont)));
                    dasaTable.addCell(new PdfPCell(new Phrase(localizedBhukthiLord, bodyFont)));
                    dasaTable.addCell(new PdfPCell(new Phrase(bhukthi.getStartDate().toString(), bodyFont)));
                    dasaTable.addCell(new PdfPCell(new Phrase(bhukthi.getEndDate().toString(), bodyFont)));
                }
            }
            document.add(dasaTable);

            // ==========================================
            // SECTION 6: DIAGNOSTICS CARDS (YOGA & DOSHAM)
            // ==========================================
            document.newPage();
            document.add(new Paragraph(ts.getLabel("pdf.diagnostics.title"), sectionFont));
            document.add(new Paragraph(" ", bodyFont));

            for (var dosha : data.getStructuralDiagnostics().getDiscoveredDoshams()) {
                Paragraph p = new Paragraph();
                p.add(new Chunk("• " + dosha.getName() + ": ", boldBody));
                p.add(new Chunk(dosha.isActive() ? "[" + ts.getLabel("dosha.active") + " - " + dosha.getSeverity() + "]"
                        : "[" + ts.getLabel("dosha.inactive") + "]", bodyFont));
                document.add(p);
                if (dosha.isActive()) {
                    Paragraph r = new Paragraph("  " + ts.getLabel("pdf.diagnostics.remedy") + ": " + dosha.getRemedySuggestion(), bodyFont);
                    r.setSpacingAfter(8);
                    document.add(r);
                }
            }

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Fatal failure compiling comprehensive multi-script vector publishing layer", e);
        }

        return out.toByteArray();
    }

    // ==========================================
    // MULTILINGUAL GEOMETRIC CANVAS COMPILERS
    // ==========================================

    private void paintSouthIndianChart(Document doc, List<ChartResponseDTO.PositionDetail> planets, Font font) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(90);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);

        int[][] gridLayout = {
                {12, 1, 2, 3},
                {11, 0, 0, 4},
                {10, 0, 0, 5},
                {9,  8, 7, 6}
        };

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                int targetSign = gridLayout[row][col];
                PdfPCell cell = new PdfPCell();
                cell.setMinimumHeight(60f);

                if (targetSign == 0) {
                    cell.setBorder(PdfPCell.NO_BORDER);
                } else {
                    final int signId = targetSign;
                    String cellContent = planets.stream()
                            .filter(p -> p.getSignNumber() == signId)
                            .map(p -> p.getDisplayName() + " " + p.getFormattedDegree().split("\u00B0")[0] + "'")
                            .collect(Collectors.joining("\n"));

                    Paragraph cellPar = new Paragraph(ts.getLocalizedRashi(signId) + "\n---\n" + cellContent, font);
                    cell.addElement(cellPar);
                }
                table.addCell(cell);
            }
        }
        doc.add(table);
    }

    private void paintNorthIndianChart(Document doc, PdfWriter wr, List<ChartResponseDTO.PositionDetail> planets, Font font) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(90);
        PdfPCell c = new PdfPCell();
        c.setMinimumHeight(150f);
        c.setBorder(PdfPCell.NO_BORDER);
        table.addCell(c);
        doc.add(table);

        // FIX: Using doc.getPageSize().getWidth() to resolve the compilation error
        float vOff = wr.getVerticalPosition(false);
        float cHor = doc.getPageSize().getWidth() / 2f;

        float bY = vOff + 10f; float w = 150f; float h = 150f;
        float sX = cHor - (w / 2f); float eX = cHor + (w / 2f); float sY = bY; float eY = bY + h;

        PdfContentByte cb = wr.getDirectContent(); cb.setColorStroke(java.awt.Color.BLACK); cb.setLineWidth(1.0f);
        cb.rectangle(sX, sY, w, h); cb.moveTo(sX, sY); cb.lineTo(eX, eY); cb.moveTo(sX, eY); cb.lineTo(eX, sY);
        cb.moveTo(cHor, sY); cb.lineTo(sX, sY + (h / 2f)); cb.moveTo(sX, sY + (h / 2f)); cb.lineTo(cHor, eY);
        cb.moveTo(cHor, eY); cb.lineTo(eX, sY + (h / 2f)); cb.moveTo(eX, sY + (h / 2f)); cb.lineTo(cHor, sY); cb.stroke();

        float[][] houseCenters = {{cHor, sY + (h * 0.72f)}, {cHor - (w * 0.25f), sY + (h * 0.85f)}, {cHor - (w * 0.38f), sY + (h * 0.62f)}, {cHor - (w * 0.22f), sY + (h * 0.50f)}, {cHor - (w * 0.38f), sY + (h * 0.38f)}, {cHor - (w * 0.25f), sY + (h * 0.15f)}, {cHor, sY + (h * 0.28f)}, {cHor + (w * 0.25f), sY + (h * 0.15f)}, {cHor + (w * 0.38f), sY + (h * 0.38f)}, {cHor + (w * 0.22f), sY + (h * 0.50f)}, {cHor + (w * 0.38f), sY + (h * 0.62f)}, {cHor + (w * 0.25f), sY + (h * 0.85f)}};
        int lagnaSign = planets.stream().filter(p -> "LAGNA".equalsIgnoreCase(p.getPlanetKey())).findFirst().orElseThrow().getSignNumber();
        BaseFont bf = font.getBaseFont();

        for (int i = 0; i < 12; i++) {
            int curSign = ((lagnaSign - 1 + i) % 12) + 1; float cx = houseCenters[i][0]; float cy = houseCenters[i][1];
            cb.beginText(); cb.setFontAndSize(bf, 6); cb.setTextMatrix(cx - 2f, cy + 6f); cb.showText(String.valueOf(curSign)); cb.endText();
            String pText = planets.stream().filter(p -> p.getSignNumber() == curSign && !"LAGNA".equalsIgnoreCase(p.getPlanetKey())).map(ChartResponseDTO.PositionDetail::getDisplayName).collect(Collectors.joining(" "));
            if (!pText.isEmpty()) { cb.beginText(); cb.setFontAndSize(bf, 8); cb.setTextMatrix(cx - (bf.getWidthPoint(pText, 8) / 2f), cy - 4f); cb.showText(pText); cb.endText(); }
        }
    }
}