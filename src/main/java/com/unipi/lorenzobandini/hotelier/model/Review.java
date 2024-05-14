package com.unipi.lorenzobandini.hotelier.model;

import java.time.LocalDate;

public class Review{
    private String reviewer;
    private LocalDate date = LocalDate.now();
    private float globalScore;
    private Ratings scores;

    public Review(String reviewer, float globalScore, Ratings scores){
        this.reviewer = reviewer;
        this.globalScore = Math.round(globalScore*10)/10.0f;
        this.scores = scores;
    }

    public String getReviewer(){
        return this.reviewer;
    }

    public float getGlobalScore(){
        return this.globalScore;
    }


    public LocalDate getDate(){
        return this.date;
    }

    public Ratings getRatings(){
        return this.scores;
    }

}