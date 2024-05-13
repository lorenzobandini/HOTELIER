package com.unipi.lorenzobandini.hotelier;

public class User{
    private String username;
    private String hashPassword;
    private boolean isLogged;
    private String badge;
    private int reviewCount;

    public User(String username, String hashPassword, boolean isLogged, String badge, int reviewCount){
        this.username = username;
        this.hashPassword = hashPassword;
        this.isLogged = isLogged;
        this.badge = badge;
        this.reviewCount = reviewCount;
    }

    public String getUsername(){
        return this.username;
    }

    public String getHashPassword(){
        return this.hashPassword;
    }

    public boolean isLogged(){
        return this.isLogged;
    }

    public void setLogged(boolean isLogged){
        this.isLogged = isLogged;
    }

    public String getBadge(){
        return this.badge;
    }

    public void setBadge(String badge){
        this.badge = badge;
    }

    public int getReviewCount(){
        return this.reviewCount;
    }

    public void addReview(){
        this.reviewCount++;
        setBadge();
    }

    private void setBadge(){
        if(this.reviewCount >= 5 && this.reviewCount < 10){
            this.badge = "Recensore Esperto";
        }else if(this.reviewCount >= 10 && this.reviewCount < 20){
            this.badge = "Contributore";
        }else if(this.reviewCount >= 20 && this.reviewCount < 30){
            this.badge = "Contributore Esperto";
        }else if(this.reviewCount >= 30){
            this.badge = "Contributore Super";
        }
    }
}