package com.unipi.lorenzobandini.hotelier;

import java.util.List;
import java.util.Map;

public class Hotel {
    private int id;
    private String name;
    private String description;
    private String city;
    private String phone;
    private List<String> services;
    private int rate;
    private Map<String, Integer> ratings;

    public Hotel(int id, String name, String description, String city, String phone, List<String> services, int rate, Map<String, Integer> ratings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = services;
        this.rate = rate;
        this.ratings = ratings;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public Map<String, Integer> getRatings() {
        return ratings;
    }

    public void setRatings(Map<String, Integer> ratings) {
        this.ratings = ratings;
    }
}
