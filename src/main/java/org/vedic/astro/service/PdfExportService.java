package org.vedic.astro.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.dto.ComprehensiveReportDTO;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfExportService {

    private final TranslationService ts;
    private final TemplateEngine templateEngine;

    public PdfExportService(TranslationService ts, TemplateEngine templateEngine) {
        this.ts = ts;
        this.templateEngine = templateEngine;
    }

    public byte[] generateAstrologyReport(ComprehensiveReportDTO data) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            String lang = locale.getLanguage();
            boolean isHi = "hi".equalsIgnoreCase(lang);

            // Prepare list of Varga chart titles
            String[] vargaKeys = {
                    "pdf.chart.d1", "pdf.chart.d2", "pdf.chart.d3", "pdf.chart.bhava",
                    "pdf.chart.d7", "pdf.chart.d9", "pdf.chart.d10", "pdf.chart.d12",
                    "pdf.chart.d20", "pdf.chart.d24", "pdf.chart.d30", "pdf.chart.d60"
            };
            List<String> vargaTitles = new ArrayList<>();
            for (String key : vargaKeys) {
                vargaTitles.add(ts.getLabel(key));
            }

            // Coordinates for North Indian template houses
            float[][] targets = {
                    {80f,115f}, {40f,135f}, {20f,100f}, {45f,80f}, {20f,60f}, {40f,25f},
                    {80f,45f}, {120f,25f}, {140f,60f}, {115f,80f}, {140f,100f}, {120f,135f}
            };

            // Compile sign mappings for South Indian cells and North Indian templates
            List<Map<Integer, Map<String, Object>>> chartMap = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                List<ChartResponseDTO.PositionDetail> planets = data.getVargaChartsSuite().get(i);
                Map<Integer, Map<String, Object>> signMap = new HashMap<>();

                for (int currentSign = 1; currentSign <= 12; currentSign++) {
                    final int sigVal = currentSign;

                    boolean hasLagna = planets.stream()
                            .anyMatch(p -> "LAGNA".equalsIgnoreCase(p.getPlanetKey()) && p.getSignNumber() == sigVal);

                    List<String> planetNames = planets.stream()
                            .filter(p -> p.getSignNumber() == sigVal && !"LAGNA".equalsIgnoreCase(p.getPlanetKey()))
                            .map(ChartResponseDTO.PositionDetail::getDisplayName)
                            .collect(Collectors.toList());

                    String planetsText = String.join(" ", planetNames);

                    // Compute target coordinates for the North Indian Diamond chart
                    int lagnaSign = planets.stream()
                            .filter(p -> "LAGNA".equalsIgnoreCase(p.getPlanetKey()))
                            .findFirst().map(ChartResponseDTO.PositionDetail::getSignNumber).orElse(1);
                    int targetIdx = (currentSign - lagnaSign + 12) % 12;
                    float cx = targets[targetIdx][0];
                    float cy = 160f - targets[targetIdx][1]; // Invert PDF to SVG space

                    Map<String, Object> houseData = new HashMap<>();
                    houseData.put("isLagna", hasLagna);
                    houseData.put("planets", planetNames);
                    houseData.put("planetsText", planetsText);
                    houseData.put("x", cx);
                    houseData.put("y", cy);

                    signMap.put(currentSign, houseData);
                }
                chartMap.add(signMap);
            }

            // Generate Lagna Diagonal Slash PNG dynamically
            BufferedImage img = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(0, 0, 120, 120);
            g2.dispose();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            String lagnaSlashBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            Context context = new Context(locale);
            context.setVariable("data", data);
            context.setVariable("isHi", isHi);
            context.setVariable("vargaTitles", vargaTitles);
            context.setVariable("chartMap", chartMap);
            context.setVariable("lagnaSlashBase64", lagnaSlashBase64);

            String htmlContent = templateEngine.process("astrology_report_template", context);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // Set base URL to classpath root
            builder.withHtmlContent(htmlContent, new ClassPathResource("/").getURL().toExternalForm());

            // Load and register all Noto Sans font families for multi-lingual fallback
            String[] fontFiles = {
                    "NotoSans-Regular.ttf",
                    "NotoSansTamil-Regular.ttf",
                    "NotoSansDevanagari-Regular.ttf",
                    "NotoSansTelugu-Regular.ttf",
                    "NotoSansKannada-Regular.ttf",
                    "NotoSansMalayalam-Regular.ttf"
            };

            String[] fontFamilies = {
                    "Noto Sans",
                    "Noto Sans Tamil",
                    "Noto Sans Devanagari",
                    "Noto Sans Telugu",
                    "Noto Sans Kannada",
                    "Noto Sans Malayalam"
            };

            for (int k = 0; k < fontFiles.length; k++) {
                final String fFile = fontFiles[k];
                final String fFamily = fontFamilies[k];
                builder.useFont(() -> getFontStream(new ClassPathResource("fonts/" + fFile)), fFamily);
            }

            builder.toStream(out);
            builder.run();

            System.out.println("[PDF EXPORT] OpenHTMLtoPDF compiled successfully using multi-font fallback with locale: " + locale);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate astrology PDF report using OpenHTMLtoPDF: " + e.getMessage(), e);
        }
    }

    private InputStream getFontStream(ClassPathResource resource) {
        try {
            return resource.getInputStream();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load font from classpath resource: " + resource.getPath(), e);
        }
    }
}