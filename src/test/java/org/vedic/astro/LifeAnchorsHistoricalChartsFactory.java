package org.vedic.astro;

import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.util.ZodiacUtils;

import java.util.*;

/**
 * Historical benchmark chart factory providing 10 classical astrological charts
 * with known historical birth times, lagnas, and longevity classifications.
 */
public class LifeAnchorsHistoricalChartsFactory {

    public record HistoricalNative(
            String name,
            String historicalReference,
            int lagnaSign,
            double lagnaDegree,
            Map<String, ChartResponseDTO.PositionDetail> planetMap,
            Map<String, Double> shadbalaRupas,
            String expectedLongevityTier
    ) {}

    public static List<HistoricalNative> get10ClassicalNatives() {
        List<HistoricalNative> list = new ArrayList<>();

        // 1. Swami Vivekananda (1863-01-12, Dhanus Lagna, AK Sun in Dhanus)
        list.add(createNative("Swami Vivekananda", "1863-01-12, Dhanus Lagna, AK Sun in Dhanus", 9, 27.5,
                Map.of(
                        "Sun", pos("Sun", 9, 29.0),
                        "Moon", pos("Moon", 6, 17.5),
                        "Mars", pos("Mars", 10, 6.5),
                        "Mercury", pos("Mercury", 10, 11.5),
                        "Jupiter", pos("Jupiter", 7, 4.0),
                        "Venus", pos("Venus", 10, 7.0),
                        "Saturn", pos("Saturn", 6, 13.5),
                        "Rahu", pos("Rahu", 12, 22.0),
                        "Ketu", pos("Ketu", 6, 22.0)
                ),
                "Madhyayu"));

        // 2. B.V. Raman (1912-08-08, Kumbha Lagna, Saturn in Vrishabha)
        list.add(createNative("B.V. Raman", "1912-08-08, Kumbha Lagna, Saturn in Vrishabha", 11, 8.5,
                Map.of(
                        "Sun", pos("Sun", 4, 22.0),
                        "Moon", pos("Moon", 2, 23.5),
                        "Mars", pos("Mars", 5, 21.0),
                        "Mercury", pos("Mercury", 5, 13.0),
                        "Jupiter", pos("Jupiter", 8, 12.5),
                        "Venus", pos("Venus", 5, 2.0),
                        "Saturn", pos("Saturn", 2, 10.0),
                        "Rahu", pos("Rahu", 12, 14.5),
                        "Ketu", pos("Ketu", 6, 14.5)
                ),
                "Poornayu"));

        // 3. Mahatma Gandhi (1869-10-02, Tula Lagna, Mars+Venus+Mercury in Lagna)
        list.add(createNative("Mahatma Gandhi", "1869-10-02, Tula Lagna, Mars+Venus+Mercury in Lagna", 7, 12.0,
                Map.of(
                        "Sun", pos("Sun", 6, 17.0),
                        "Moon", pos("Moon", 4, 28.0),
                        "Mars", pos("Mars", 7, 18.0),
                        "Mercury", pos("Mercury", 7, 11.5),
                        "Jupiter", pos("Jupiter", 1, 28.0),
                        "Venus", pos("Venus", 7, 24.5),
                        "Saturn", pos("Saturn", 8, 20.0),
                        "Rahu", pos("Rahu", 4, 12.0),
                        "Ketu", pos("Ketu", 10, 12.0)
                ),
                "Poornayu"));

        // 4. Albert Einstein (1879-03-14, Mithuna Lagna, Exalted Venus in 10th)
        list.add(createNative("Albert Einstein", "1879-03-14, Mithuna Lagna, Exalted Venus in 10th", 3, 14.0,
                Map.of(
                        "Sun", pos("Sun", 11, 2.5),
                        "Moon", pos("Moon", 8, 14.0),
                        "Mars", pos("Mars", 9, 27.0),
                        "Mercury", pos("Mercury", 12, 3.0),
                        "Jupiter", pos("Jupiter", 11, 27.5),
                        "Venus", pos("Venus", 12, 17.0),
                        "Saturn", pos("Saturn", 12, 4.0),
                        "Rahu", pos("Rahu", 10, 1.5),
                        "Ketu", pos("Ketu", 4, 1.5)
                ),
                "Poornayu"));

        // 5. Sri Ramana Maharshi (1879-12-30, Tula Lagna, Moon in Punarvasu)
        list.add(createNative("Sri Ramana Maharshi", "1879-12-30, Tula Lagna, Moon in Punarvasu", 7, 2.0,
                Map.of(
                        "Sun", pos("Sun", 9, 16.0),
                        "Moon", pos("Moon", 3, 28.5),
                        "Mars", pos("Mars", 1, 22.0),
                        "Mercury", pos("Mercury", 8, 24.0),
                        "Jupiter", pos("Jupiter", 11, 15.0),
                        "Venus", pos("Venus", 8, 28.0),
                        "Saturn", pos("Saturn", 12, 16.0),
                        "Rahu", pos("Rahu", 9, 29.0),
                        "Ketu", pos("Ketu", 3, 29.0)
                ),
                "Poornayu"));

        // 6. Sri Ramakrishna Paramahamsa (1836-02-18, Kumbha Lagna, Exalted Mars)
        list.add(createNative("Sri Ramakrishna Paramahamsa", "1836-02-18, Kumbha Lagna, Exalted Mars", 11, 4.5,
                Map.of(
                        "Sun", pos("Sun", 11, 6.5),
                        "Moon", pos("Moon", 11, 22.0),
                        "Mars", pos("Mars", 10, 22.0),
                        "Mercury", pos("Mercury", 11, 15.0),
                        "Jupiter", pos("Jupiter", 3, 14.5),
                        "Venus", pos("Venus", 12, 8.5),
                        "Saturn", pos("Saturn", 7, 14.5),
                        "Rahu", pos("Rahu", 4, 2.5),
                        "Ketu", pos("Ketu", 10, 2.5)
                ),
                "Madhyayu"));

        // 7. Rabindranath Tagore (1861-05-07, Meena Lagna, Jupiter in 5th)
        list.add(createNative("Rabindranath Tagore", "1861-05-07, Meena Lagna, Jupiter in 5th", 12, 27.0,
                Map.of(
                        "Sun", pos("Sun", 1, 24.0),
                        "Moon", pos("Moon", 12, 11.5),
                        "Mars", pos("Mars", 2, 20.0),
                        "Mercury", pos("Mercury", 1, 15.0),
                        "Jupiter", pos("Jupiter", 4, 17.5),
                        "Venus", pos("Venus", 12, 12.0),
                        "Saturn", pos("Saturn", 5, 4.0),
                        "Rahu", pos("Rahu", 3, 18.0),
                        "Ketu", pos("Ketu", 9, 18.0)
                ),
                "Poornayu"));

        // 8. Indira Gandhi (1917-11-19, Kataka Lagna, Saturn in Lagna)
        list.add(createNative("Indira Gandhi", "1917-11-19, Kataka Lagna, Saturn in Lagna", 4, 27.5,
                Map.of(
                        "Sun", pos("Sun", 8, 4.0),
                        "Moon", pos("Moon", 10, 5.5),
                        "Mars", pos("Mars", 5, 16.0),
                        "Mercury", pos("Mercury", 8, 13.0),
                        "Jupiter", pos("Jupiter", 2, 15.0),
                        "Venus", pos("Venus", 9, 21.0),
                        "Saturn", pos("Saturn", 4, 21.5),
                        "Rahu", pos("Rahu", 9, 10.5),
                        "Ketu", pos("Ketu", 3, 10.5)
                ),
                "Madhyayu"));

        // 9. Jawaharlal Nehru (1889-11-14, Kataka Lagna, Moon in Lagna)
        list.add(createNative("Jawaharlal Nehru", "1889-11-14, Kataka Lagna, Moon in Lagna", 4, 19.0,
                Map.of(
                        "Sun", pos("Sun", 8, 0.5),
                        "Moon", pos("Moon", 4, 18.0),
                        "Mars", pos("Mars", 6, 9.5),
                        "Mercury", pos("Mercury", 8, 17.0),
                        "Jupiter", pos("Jupiter", 9, 15.0),
                        "Venus", pos("Venus", 7, 7.0),
                        "Saturn", pos("Saturn", 5, 10.5),
                        "Rahu", pos("Rahu", 3, 12.5),
                        "Ketu", pos("Ketu", 9, 12.5)
                ),
                "Poornayu"));

        // 10. Srinivasa Ramanujan (1887-12-22, Kumbha Lagna, Mercury in 10th)
        list.add(createNative("Srinivasa Ramanujan", "1887-12-22, Kumbha Lagna, Mercury in 10th", 11, 10.5,
                Map.of(
                        "Sun", pos("Sun", 9, 8.5),
                        "Moon", pos("Moon", 12, 19.5),
                        "Mars", pos("Mars", 6, 16.0),
                        "Mercury", pos("Mercury", 9, 23.0),
                        "Jupiter", pos("Jupiter", 7, 27.0),
                        "Venus", pos("Venus", 8, 4.5),
                        "Saturn", pos("Saturn", 4, 6.0),
                        "Rahu", pos("Rahu", 4, 18.0),
                        "Ketu", pos("Ketu", 10, 18.0)
                ),
                "Alpayu"));

        return Collections.unmodifiableList(list);
    }

    private static HistoricalNative createNative(
            String name,
            String ref,
            int lagna,
            double lagnaDeg,
            Map<String, ChartResponseDTO.PositionDetail> pmap,
            String tier
    ) {
        Map<String, ChartResponseDTO.PositionDetail> map = new LinkedHashMap<>(pmap);
        if (!map.containsKey("Lagna") && !map.containsKey("LAGNA")) {
            map.put("Lagna", pos("Lagna", lagna, lagnaDeg));
        }

        Map<String, Double> sbala = new LinkedHashMap<>();
        for (String p : List.of("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")) {
            sbala.put(p, 6.5);
        }

        return new HistoricalNative(
                name,
                ref,
                lagna,
                lagnaDeg,
                Collections.unmodifiableMap(map),
                Collections.unmodifiableMap(sbala),
                tier
        );
    }

    private static ChartResponseDTO.PositionDetail pos(String key, int sign, double deg) {
        return ChartResponseDTO.PositionDetail.builder()
                .planetKey(key.toUpperCase())
                .displayName(key)
                .signNumber(sign)
                .rashiName(ZodiacUtils.getSignName(sign))
                .degreeInSign(deg)
                .build();
    }
}
