package com.unipi.lorenzobandini.hotelier.model;

import java.util.ArrayList;
import java.util.List;

public class HotelReviews{
    private String hotelName;
    private String city;
    private int numReviews = 0;
    private List<Review> reviews = new ArrayList<>();

    public HotelReviews(String hotelName, String city){
        this.hotelName = hotelName;
        this.city = city;
    }

    public String getHotelName(){
        return this.hotelName;
    }

    public String getCity(){
        return this.city;
    }

    public List<Review> getReviews(){
        return this.reviews;
    }

    public int getNumReviews(){
        return this.numReviews;
    }

    public void addReview(Review review){
        this.reviews.add(review);
        this.numReviews++;
    }
    
}