package org.vedic.astro.service.impl;

import org.springframework.stereotype.Service;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.ZodiacUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traditional Varga (Divisional Chart) Engine.
 * Computes Parashari divisional chart placements directly from absolute
 * longitudes.
 */
@Service
public class VargaCalculationService {

    @FunctionalInterface
    public interface SpeedProvider {
        double getSpeed(String planetName);
    }

    public enum VargaType {
        D1_RASI(1),
        D2_HORA(2),
        D3_DREKKANA(3),
        D4_CHATURTHAMSA(4),
        D7_SAPTAMSA(7),
        D9_NAVAMSA(9),
        D10_DASAMSA(10),
        D12_DWADASAMSA(12),
        D16_SHODASAMSA(16),
        D20_VIMSAMSA(20),
        D24_CHATURVIMSAMSA(24),
        D27_SAPTAVIMSAMSA(27),
        D30_TRIMSAMSA(30),
        D60_SHASTIAMSA(60);

        private final int divisionCount;

        VargaType(int divisionCount) {
            this.divisionCount = divisionCount;
        }

        public int getDivisionCount() {
            return divisionCount;
        }
    }

    /**
     * Converts raw longitude map (Map<String, Double>) into D1 PlanetaryPosition
     * Map.
     */
    public Map<String, PlanetaryPosition> generateD1MapFromLongitudes(
            Map<String, Double> longitudes,
            SpeedProvider speedProvider) {
        Map<String, PlanetaryPosition> d1Map = new LinkedHashMap<>();

        for (Map.Entry<String, Double> entry : longitudes.entrySet()) {
            String name = entry.getKey();
            double absLong = (entry.getValue() + 360.0) % 360.0;
            double speed = speedProvider.getSpeed(name);
            int signNumber = (int) (absLong / 30.0) + 1;

            PlanetaryPosition pos = PlanetaryPosition.builder()
                    .name(name)
                    .absoluteLongitude(absLong)
                    .signNumber(signNumber)
                    .signName(ZodiacUtils.getSignName(signNumber))
                    .rashi(ZodiacUtils.getVedicRashi(signNumber))
                    .nakshatra(ZodiacUtils.getNakshatraName(absLong))
                    .pada(ZodiacUtils.getNakshatraPada(absLong))
                    .degreeInSign(absLong % 30.0)
                    .speed(speed)
                    .build();

            d1Map.put(name, pos);
        }

        return d1Map;
    }

    /**
     * Generates a divisional chart map from an existing D1 map.
     */
    public Map<String, PlanetaryPosition> generateVargaChart(
            Map<String, PlanetaryPosition> d1Map,
            VargaType vargaType) {
        Map<String, PlanetaryPosition> vargaMap = new LinkedHashMap<>();

        for (Map.Entry<String, PlanetaryPosition> entry : d1Map.entrySet()) {
            String name = entry.getKey();
            PlanetaryPosition basePos = entry.getValue();

            int vargaSignNumber = calculateVargaSignNumber(basePos.getAbsoluteLongitude(), vargaType);
            double vargaDegreeInSign = calculateVargaDegreeInSign(basePos.getAbsoluteLongitude(), vargaType);

            PlanetaryPosition vargaPos = PlanetaryPosition.builder()
                    .name(name)
                    .absoluteLongitude(basePos.getAbsoluteLongitude())
                    .signNumber(vargaSignNumber)
                    .signName(ZodiacUtils.getSignName(vargaSignNumber))
                    .rashi(ZodiacUtils.getVedicRashi(vargaSignNumber))
                    .nakshatra(ZodiacUtils.getNakshatraName(basePos.getAbsoluteLongitude()))
                    .pada(ZodiacUtils.getNakshatraPada(basePos.getAbsoluteLongitude()))
                    .degreeInSign(vargaDegreeInSign)
                    .speed(basePos.getSpeed())
                    .build();

            vargaMap.put(name, vargaPos);
        }

        return vargaMap;
    }

    public int calculateVargaSignNumber(double absoluteLongitude, VargaType vargaType) {
        double normLong = (absoluteLongitude + 360.0) % 360.0;
        int baseSignNumber = ((int) (normLong / 30.0)) % 12 + 1;
        double degreeInSign = normLong % 30.0;
        boolean isOddSign = (baseSignNumber % 2 != 0);

        return switch (vargaType) {
            case D1_RASI -> baseSignNumber;

            case D2_HORA -> {
                if (isOddSign) {
                    yield (degreeInSign < 15.0) ? 5 : 4;
                } else {
                    yield (degreeInSign < 15.0) ? 4 : 5;
                }
            }

            case D3_DREKKANA -> {
                int part = (int) (degreeInSign / 10.0);
                int shift = switch (part) {
                    case 0 -> 0;
                    case 1 -> 4;
                    default -> 8;
                };
                yield (baseSignNumber + shift - 1) % 12 + 1;
            }

            case D4_CHATURTHAMSA -> {
                int slice = (int) (degreeInSign / 7.5);
                yield (baseSignNumber + (slice * 3) - 1) % 12 + 1;
            }

            case D7_SAPTAMSA -> {
                int slice = Math.min(6, Math.max(0, (int) (degreeInSign / (30.0 / 7.0))));
                int startSign = isOddSign ? baseSignNumber : (baseSignNumber + 6 - 1) % 12 + 1;
                yield (startSign + slice - 1) % 12 + 1;
            }

            case D9_NAVAMSA -> {
                int slice = Math.min(8, Math.max(0, (int) (degreeInSign / (30.0 / 9.0))));
                int startSign = switch (baseSignNumber) {
                    case 1, 5, 9 -> 1;
                    case 2, 6, 10 -> 10;
                    case 3, 7, 11 -> 7;
                    default -> 4;
                };
                yield (startSign + slice - 1) % 12 + 1;
            }

            case D10_DASAMSA -> {
                int slice = Math.min(9, Math.max(0, (int) (degreeInSign / 3.0)));
                int startSign = isOddSign ? baseSignNumber : (baseSignNumber + 8 - 1) % 12 + 1;
                yield (startSign + slice - 1) % 12 + 1;
            }

            case D12_DWADASAMSA -> {
                int slice = Math.min(11, Math.max(0, (int) (degreeInSign / 2.5)));
                yield (baseSignNumber + slice - 1) % 12 + 1;
            }

            case D16_SHODASAMSA -> {
                int slice = Math.min(15, Math.max(0, (int) (degreeInSign / 1.875)));
                int startSign = switch (baseSignNumber) {
                    case 1, 4, 7, 10 -> 1;
                    case 2, 5, 8, 11 -> 5;
                    default -> 9;
                };
                yield (startSign + slice - 1) % 12 + 1;
            }

            case D20_VIMSAMSA -> {
                int slice = Math.min(19, Math.max(0, (int) (degreeInSign / 1.5)));
                int startSign = switch (baseSignNumber) {
                    case 1, 4, 7, 10 -> 1;
                    case 2, 5, 8, 11 -> 9;
                    default -> 5;
                };
                yield (startSign + slice - 1) % 12 + 1;
            }

            case D24_CHATURVIMSAMSA -> {
                int slice = Math.min(23, Math.max(0, (int) (degreeInSign / 1.25)));
                int startSign = isOddSign ? 5 : 4;
                yield (startSign + slice - 1) % 12 + 1;
            }

            case D27_SAPTAVIMSAMSA -> {
                int slice = Math.min(26, Math.max(0, (int) (degreeInSign / (30.0 / 27.0))));
                int startSign = switch (baseSignNumber) {
                    case 1, 5, 9 -> 1;
                    case 2, 6, 10 -> 4;
                    case 3, 7, 11 -> 7;
                    default -> 10;
                };
                yield (startSign + slice - 1) % 12 + 1;
            }

            case D30_TRIMSAMSA -> {
                if (isOddSign) {
                    if (degreeInSign < 5.0)
                        yield 1;
                    if (degreeInSign < 10.0)
                        yield 11;
                    if (degreeInSign < 18.0)
                        yield 9;
                    if (degreeInSign < 25.0)
                        yield 3;
                    yield 2;
                } else {
                    if (degreeInSign < 5.0)
                        yield 2;
                    if (degreeInSign < 12.0)
                        yield 3;
                    if (degreeInSign < 20.0)
                        yield 9;
                    if (degreeInSign < 25.0)
                        yield 11;
                    yield 1;
                }
            }

            case D60_SHASTIAMSA -> {
                int slice = Math.min(59, Math.max(0, (int) (degreeInSign / 0.5)));
                yield (baseSignNumber + slice - 1) % 12 + 1;
            }
        };
    }

    private double calculateVargaDegreeInSign(double absoluteLongitude, VargaType vargaType) {
        if (vargaType == VargaType.D1_RASI) {
            return absoluteLongitude % 30.0;
        }
        double sliceSize = 30.0 / vargaType.getDivisionCount();
        return (absoluteLongitude % sliceSize) * vargaType.getDivisionCount();
    }
}