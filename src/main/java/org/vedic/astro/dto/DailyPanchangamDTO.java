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
    List<TimeSlotDTO> raghuKalam,
    List<TimeSlotDTO> emagandam,
    List<TimeSlotDTO> kulikai,
    List<HoraTimeSlotDTO> horais,
    String chandrastamamRashi,
    int netram,
    double jeevan,
    boolean muhurthamDay,
    boolean vasthuDay
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
        String label
    ) {}

    public record HoraTimeSlotDTO(
        int hour,
        String start,
        String end,
        String planet,
        String localizedPlanet
    ) {}
}
