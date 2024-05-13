package com.unipi.lorenzobandini.hotelier.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Chart{
    private String città;
    private Map<String, Integer> hotels = new LinkedHashMap<>();

    public Chart(String città) {
        this.città = città;
    }

    public String getCittà() {
        return città;
    }

    public void setCittà(String città) {
        this.città = città;
    }

    public Map<String, Integer> getHotels() {
        return hotels;
    }

    public void setHotels(Map<String, Integer> hotels) {
        this.hotels = hotels;
    }
}