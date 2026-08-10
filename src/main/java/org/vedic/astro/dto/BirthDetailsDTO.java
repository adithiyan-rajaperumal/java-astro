package org.vedic.astro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BirthDetailsDTO {
    private String name;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private double latitude;
    private double longitude;
    private String ayanamsa;
    private Object location;
    private String panchangamSystem;
    private String date;
    private String time;

    public BirthDetailsDTO(String name, int year, int month, int day, int hour, int minute, int second, double latitude, double longitude, String ayanamsa) {
        this.name = name;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ayanamsa = ayanamsa;
    }

    public String name() { return name; }
    public int year() { return year; }
    public int month() { return month; }
    public int day() { return day; }
    public int hour() { return hour; }
    public int minute() { return minute; }
    public int second() { return second; }
    public double latitude() { return latitude; }
    public double longitude() { return longitude; }
    public String ayanamsa() { return ayanamsa; }
    public Object location() { return location; }
    public String panchangamSystem() { return panchangamSystem; }
    public String date() { return date; }
    public String time() { return time; }
}

