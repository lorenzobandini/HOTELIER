package com.unipi.lorenzobandini.hotelier.model;

public class HotelInChart {
    private String name;
    private float score;

    public HotelInChart(String name, float score) {
        this.name = name;
        this.score = Math.round(score * 10) / 10.0f;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = Math.round(score * 10) / 10.0f;
    }
}