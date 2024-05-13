package com.unipi.lorenzobandini.hotelier.model;

import java.time.LocalDate;

public class Review{
    private String reviewer;
    private int globalScore;
    private LocalDate date = LocalDate.now();
    private int[] scores;

    public Review(String reviewer, int globalScore, int[] scores){
        this.reviewer = reviewer;
        this.globalScore = globalScore;
        this.scores = scores;
    }

    public String getReviewer(){
        return this.reviewer;
    }

    public int getGlobalScore(){
        return this.globalScore;
    }

    public int[] getScores(){
        return this.scores;
    }

    public LocalDate getDate(){
        return this.date;
    }
}