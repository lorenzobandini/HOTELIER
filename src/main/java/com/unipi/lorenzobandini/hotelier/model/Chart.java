package com.unipi.lorenzobandini.hotelier.model;

import java.util.ArrayList;
import java.util.List;

public class Chart {
    private String city;
    private List<HotelInChart> hotels = new ArrayList<>();

    public Chart(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void addHotel(String name, float score) {
        hotels.add(new HotelInChart(name, score));    
    }

    public void updateHotel(String name, float score) {
        for (HotelInChart hotel : hotels) {
            if (hotel.getName().equals(name)) {
                hotel.setScore(score);
                break;
            }
        }
    }

    public HotelInChart getTopHotelInChart() {
        return hotels.get(0);
    }

    public List<HotelInChart> getHotels() {
        return hotels;
    }
}