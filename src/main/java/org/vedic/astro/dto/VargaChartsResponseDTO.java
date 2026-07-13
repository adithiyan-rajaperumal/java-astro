package org.vedic.astro.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VargaChartsResponseDTO {

    private String name;
    private String localMeanTime;
    private double julianDayUT;
    private ChartResponseDTO.BirthProfile birthProfile;

    // The 12 Localized UI-Ready Canvas Arrays
    private List<ChartResponseDTO.PositionDetail> rasiChart;           // D1 (Rasi)
    private List<ChartResponseDTO.PositionDetail> horaiChart;          // D2 (Hora)
    private List<ChartResponseDTO.PositionDetail> trakonamChart;       // D3 (Drekkana)
    private List<ChartResponseDTO.PositionDetail> sapthamsamChart;      // D7 (Saptamsa)
    private List<ChartResponseDTO.PositionDetail> amsamChart;           // D9 (Navamsa)
    private List<ChartResponseDTO.PositionDetail> dhasamsamChart;       // D10 (Dasamsa)
    private List<ChartResponseDTO.PositionDetail> thvathamsamChart;    // D12 (Dwadasamsa)
    private List<ChartResponseDTO.PositionDetail> vimsamsamChart;       // D20 (Vimsamsa)
    private List<ChartResponseDTO.PositionDetail> sadhurvimsamsamChart; // D24 (Chaturvimsamsa)
    private List<ChartResponseDTO.PositionDetail> tridhamsamChart;      // D30 (Trimsamsa)
    private List<ChartResponseDTO.PositionDetail> shastiyamsamChart;    // D60 (Shashtyamsa)
    private List<ChartResponseDTO.PositionDetail> bavamChart;
}
