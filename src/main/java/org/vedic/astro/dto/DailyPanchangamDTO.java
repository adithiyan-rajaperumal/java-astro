package org.vedic.astro.dto;

import java.util.List;

public record DailyPanchangamDTO(
    String date,
    String sunrise,
    String sunset,
    String moonrise,
    String moonset,
    PanchangamElementDTO thithi,
    PanchangamElementDTO nakshatra,
    PanchangamElementDTO yogam,
    PanchangamElementDTO karanam,
    String rashi,
    List<TimeSlotDTO> nallaNeram,
    List<TimeSlotDTO> gowriNallaNeram,
    List<TimeSlotDTO> nakshatraYogams,
    List<TimeSlotDTO> raghuKalam,
    List<TimeSlotDTO> emagandam,
    List<TimeSlotDTO> kulikai,
    List<HoraTimeSlotDTO> horais,
    TimeSlotDTO abhijitMuhurtham,
    List<String> chandrastamamNakshatras,
    int netram,
    double jeevan,
    boolean muhurthamDay,
    boolean vasthuDay,
    boolean vasthuAuspicious,
    boolean agniNakshathiram,
    TimeSlotDTO vasthuNeram,
    TimeSlotDTO vasthuPujaNeram
) {
    public record PanchangamElementDTO(
        int number,
        String name,
        String localizedName,
        String endTime, // format e.g. "HH:mm" or "Throughout the day"
        String nextName,
        String nextLocalizedName,
        String nextEndTime
    ) {}

    public record TimeSlotDTO(
        String start,
        String end,
        String label,
        boolean startNextDay,
        boolean endNextDay
    ) {
        public TimeSlotDTO(String start, String end, String label) {
            this(start, end, label, false, false);
        }
    }

    public record HoraTimeSlotDTO(
        int hour,
        String start,
        String end,
        String planet,
        String localizedPlanet
    ) {}
}
