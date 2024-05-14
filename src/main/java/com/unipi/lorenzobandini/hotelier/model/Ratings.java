package com.unipi.lorenzobandini.hotelier.model;

public class Ratings {
    private float cleaning;
    private float position;
    private float services;
    private float quality;

    public Ratings(float cleaning, float position, float services, float quality) {
        this.cleaning = Math.round(cleaning*10)/10.0f;
        this.position = Math.round(position*10)/10.0f;
        this.services = Math.round(services*10)/10.0f;
        this.quality = Math.round(quality*10)/10.0f;
    }

    public float getCleaning() {
        return cleaning;
    }

    public void setCleaning(float f) {
        this.cleaning = Math.round(f*10)/10.0f;
    }

    public float getPosition() {
        return position;
    }

    public void setPosition(float f) {
        this.position = Math.round(f*10)/10.0f;
    }

    public float getServices() {
        return services;
    }

    public void setServices(float f) {
        this.services = Math.round(f*10)/10.0f;
    }

    public float getQuality() {
        return quality;
    }

    public void setQuality(float f) {
        this.quality = Math.round(f*10)/10.0f;
    }
}