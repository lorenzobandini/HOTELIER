package com.unipi.lorenzobandini.hotelier;

public class Review{
    private String reviewer;
    private int globalScore;
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
}