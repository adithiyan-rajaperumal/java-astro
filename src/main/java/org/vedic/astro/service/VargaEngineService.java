package org.vedic.astro.service;

import org.springframework.stereotype.Service;

@Service
public class VargaEngineService {

    public int calculateVargaSign(int dNo, int baseSign, double degreeInSign, double absoluteLong) {
        return switch (dNo) {
            case 1 -> baseSign; // D1 Rasi

            case 2 -> { // D2 Horai (Hora)
                if (baseSign % 2 != 0) { // Odd Sign
                    yield (degreeInSign < 15.0) ? 5 : 4; // First half Leo(5), Second half Cancer(4)
                } else { // Even Sign
                    yield (degreeInSign < 15.0) ? 4 : 5; // First half Cancer(4), Second half Leo(5)
                }
            }

            case 3 -> { // D3 Trakonam (Drekkana)
                int part = (int) (degreeInSign / 10.0);
                yield ((baseSign - 1 + (part * 4)) % 12) + 1;
            }

            case 7 -> { // D7 Sapthamsam (Saptamsa)
                int part = (int) (degreeInSign / (30.0 / 7.0));
                yield (baseSign % 2 != 0) ? ((baseSign - 1 + part) % 12) + 1
                        : ((baseSign + 5 + part) % 12) + 1;
            }

            case 9 -> ((int) (absoluteLong * 9.0 / 30.0) % 12) + 1; // D9 Amsam

            case 10 -> { // D10 Dhasamsam (Dasamsa)
                int part = (int) (degreeInSign / 3.0);
                yield (baseSign % 2 != 0) ? ((baseSign - 1 + part) % 12) + 1
                        : ((baseSign + 8 + part) % 12) + 1;
            }

            case 12 -> { // D12 Thvathamsam (Dwadasamsa)
                int part = (int) (degreeInSign / 2.5);
                yield ((baseSign - 1 + part) % 12) + 1;
            }

            case 20 -> { // D20 Vimsamsam (Vimsamsa)
                int part = (int) (degreeInSign / 1.5);
                int startSign = (baseSign % 3 == 1) ? 1 : (baseSign % 3 == 2 ? 9 : 5);
                yield ((startSign - 1 + part) % 12) + 1;
            }

            case 24 -> { // D24 Sadhurvimsamsam (Chaturvimsamsa)
                int part = (int) (degreeInSign / 1.25);
                int startSign = (baseSign % 2 != 0) ? 5 : 4;
                yield ((startSign - 1 + part) % 12) + 1;
            }

            case 30 -> { // D30 Tridhamsam (Trimsamsa)
                if (baseSign % 2 != 0) { // Odd Sign ownership ranges
                    if (degreeInSign < 5.0) yield 1;       // Mars (Aries)
                    else if (degreeInSign < 10.0) yield 11; // Saturn (Aquarius)
                    else if (degreeInSign < 18.0) yield 9;  // Jupiter (Sagittarius)
                    else if (degreeInSign < 25.0) yield 3;  // Mercury (Gemini)
                    else yield 7;                           // Venus (Libra)
                } else { // Even Sign ownership ranges
                    if (degreeInSign < 5.0) yield 2;       // Venus (Taurus)
                    else if (degreeInSign < 12.0) yield 6;  // Mercury (Virgo)
                    else if (degreeInSign < 20.0) yield 12; // Jupiter (Pisces)
                    else if (degreeInSign < 25.0) yield 10; // Saturn (Capricorn)
                    else yield 8;                           // Mars (Scorpio)
                }
            }

            case 60 -> { // D60 Shastiyamsam (Shashtyamsa)
                int part = (int) (degreeInSign / 0.5);
                yield ((baseSign - 1 + part) % 12) + 1;
            }

            default -> baseSign;
        };
    }

    /**
     * Determines the precise dynamic house placement for Bhava Chalit chart rendering.
     * Computes the distance from the closest house cusps calculated by the Ephemeris engine.
     */
    public int calculateBhavaHouse(double planetLong, double[] cusps) {
        for (int i = 1; i <= 12; i++) {
            double start = cusps[i];
            double end = cusps[i == 12 ? 1 : i + 1];

            if (start < end) {
                if (planetLong >= start && planetLong < end) return i;
            } else { // Handles the 360-degree boundary overlap point
                if (planetLong >= start || planetLong < end) return i;
            }
        }
        return (int) (planetLong / 30.0) + 1; // Fallback to standard sign placement
    }
}
