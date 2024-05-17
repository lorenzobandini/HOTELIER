package com.unipi.lorenzobandini.hotelier;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unipi.lorenzobandini.hotelier.model.*;

public class HotelierUpdaterChart implements Runnable {

    String reset = "\u001B[0m";
    String blue = "\u001B[34m";

    private int timerUpdates;
    private Gson gson;
    private MulticastSocket multicastSocket;
    private int multicastPort;
    private String multicastAddress;
    private Object lockHotels;
    private Object lockReviews;

    public HotelierUpdaterChart(int timerUpdates, Gson gson, MulticastSocket multicastSocket, int multicastPort,
            String multicastAddress, Object lockHotels, Object lockReviews) {
        this.timerUpdates = timerUpdates;
        this.gson = gson;
        this.multicastSocket = multicastSocket;
        this.multicastPort = multicastPort;
        this.multicastAddress = multicastAddress;
        this.lockHotels = lockHotels;
        this.lockReviews = lockReviews;
    }

    @Override
    public void run() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(() -> {
            try {
                String message = "Awake";
                sendMulticastMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 250, TimeUnit.MILLISECONDS);

        createHotelierChart();

        executor.scheduleAtFixedRate(() -> {
            try {
                List<Hotel> hotels = getListHotels();
                List<Chart> charts = getListCharts();
                for (Chart chart : charts) {
                    String topHotelBeforeUpdate = chart.getTopHotelInChart().getName();
                    for (Hotel hotel : hotels) {
                        if (chart.getCity().equals(hotel.getCity())) {
                            chart.updateHotel(hotel.getName(), calculateScore(hotel));
                        }
                    }

                    chart.getHotels().sort((h1, h2) -> Float.compare(h2.getScore(), h1.getScore()));

                    if (!topHotelBeforeUpdate.equals(chart.getTopHotelInChart().getName())) {
                        String message = (blue + "The top hotel in the chart for " + chart.getCity()
                                + " has changed to "
                                + chart.getTopHotelInChart().getName() + reset);
                        sendMulticastMessage(message);
                    }
                }

                File file = new File("src/main/resources/Charts.json");
                FileWriter fileWriter = new FileWriter(file);
                this.gson.toJson(charts, fileWriter);
                fileWriter.flush();
                fileWriter.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, timerUpdates, timerUpdates, TimeUnit.SECONDS);

    }

    public void createHotelierChart() {
        synchronized (lockHotels) {
            try {
                List<Hotel> hotels = getListHotels();
                List<Chart> charts = new ArrayList<>();
                for (Hotel hotel : hotels) {
                    boolean found = false;
                    for (Chart chart : charts) {
                        if (chart.getCity().equals(hotel.getCity())) {
                            chart.addHotel(hotel.getName(), calculateScore(hotel));
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Chart chart = new Chart(hotel.getCity());
                        chart.addHotel(hotel.getName(), calculateScore(hotel));
                        charts.add(chart);
                    }
                }
                File file = new File("src/main/resources/Charts.json");
                FileWriter fileWriter = new FileWriter(file);
                this.gson.toJson(charts, fileWriter);
                fileWriter.flush();
                fileWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private float calculateScore(Hotel hotel) {
        synchronized (lockReviews) {
            try {
                List<HotelReviews> allReviews = getListReviews();
                float totalScore = 0;
                int count = 0;
                for (HotelReviews hotelReviews : allReviews) {
                    if (hotelReviews.getHotelName().equals(hotel.getName())) {
                        for (Review review : hotelReviews.getReviews()) {
                            LocalDate dateReview = review.getDate();
                            float globalScore = review.getGlobalScore();
                            Ratings scores = review.getRatings();
                            long daysSinceReview = ChronoUnit.DAYS.between(dateReview, LocalDate.now());

                            float reviewScore = (scores.getCleaning() + scores.getPosition() + scores.getQuality()
                                    + scores.getServices()) * globalScore;

                            reviewScore -= (daysSinceReview / 30) * reviewScore * 0.1;

                            reviewScore *= globalScore;

                            totalScore += reviewScore;
                            count++;
                        }
                    }
                }

                return (float) (count > 0 ? (totalScore / count) * (Math.log(1 + count)) : 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    private void sendMulticastMessage(String message) {
        byte[] buffer = message.getBytes();

        try {
            InetAddress groupForPacket = InetAddress.getByName(this.multicastAddress);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, groupForPacket, this.multicastPort);

            multicastSocket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Hotel> getListHotels() throws IOException {
        File file = new File("src/main/resources/Hotels.json");
        Type hotelListType = new TypeToken<ArrayList<Hotel>>() {
        }.getType();
        List<Hotel> hotels = new ArrayList<>();

        if (file.length() != 0) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                hotels = this.gson.fromJson(br, hotelListType);
            }
        }
        return hotels;
    }

    private List<HotelReviews> getListReviews() throws IOException {
        File file = new File("src/main/resources/Reviews.json");
        Type reviewsListType = new TypeToken<ArrayList<HotelReviews>>() {
        }.getType();
        List<HotelReviews> reviews = new ArrayList<>();

        if (file.length() != 0) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                reviews = this.gson.fromJson(br, reviewsListType);
            }
        }

        return reviews;
    }

    private List<Chart> getListCharts() throws IOException {
        File file = new File("src/main/resources/Charts.json");
        Type chartListType = new TypeToken<ArrayList<Chart>>() {
        }.getType();
        List<Chart> charts = new ArrayList<>();

        if (file.length() != 0) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                charts = this.gson.fromJson(br, chartListType);
            }
        }

        return charts;
    }

}
