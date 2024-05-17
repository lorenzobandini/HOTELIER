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

/**
 * The HotelierUpdaterChart class is responsible for updating hotel charts based
 * on reviews and scores.
 * It periodically sends multicast messages and updates the hotel charts stored
 * in a JSON file.
 */
public class HotelierUpdaterChart implements Runnable {

    // ANSI escape codes
    String reset = "\u001B[0m";
    String blue = "\u001B[34m";

    private int timerUpdates;
    private Gson gson;
    private MulticastSocket multicastSocket;
    private int multicastPort;
    private String multicastAddress;
    private Object lockHotels;
    private Object lockReviews;

    /**
     * Constructs a new HotelierUpdaterChart with the specified parameters.
     *
     * @param timerUpdates     the interval in seconds between updates
     * @param gson             the Gson instance for JSON processing
     * @param multicastSocket  the MulticastSocket for sending messages
     * @param multicastPort    the port number for multicast messages
     * @param multicastAddress the address for multicast messages
     * @param lockHotels       the lock object for synchronizing hotel data access
     * @param lockReviews      the lock object for synchronizing review data access
     */
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

    /**
     * The run method initiates the periodic tasks for sending multicast messages
     * and updating hotel charts.
     */
    @Override
    public void run() {

        // Create a scheduled executor for sending multicast messages
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        // Send a multicast message every 250 milliseconds for maintaining connected
        // clients
        executor.scheduleAtFixedRate(() -> {
            try {
                String message = "Awake";
                sendMulticastMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 250, TimeUnit.MILLISECONDS);

        // Create the initial hotel charts
        createHotelierChart();

        // Update the hotel charts every timerUpdates seconds
        executor.scheduleAtFixedRate(() -> {
            try {

                // Get the list of hotels and charts
                List<Hotel> hotels = getListHotels();
                List<Chart> charts = getListCharts();

                // For each chart, update the hotels and sort them by score
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

                // Save the updated charts to a JSON file
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

    /**
     * Creates the initial hotel charts based on the current list of hotels and
     * their scores.
     * The charts are saved to a JSON file.
     */
    public void createHotelierChart() {

        // Synchronize access to the hotels JSON file
        synchronized (lockHotels) {
            try {

                // Get the list of hotels and create a list of charts
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

                // Save the charts to a JSON file
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

    /**
     * Calculates the score of a given hotel based on its reviews.
     *
     * @param hotel the Hotel object for which the score is calculated
     * @return the calculated score
     */
    private float calculateScore(Hotel hotel) {

        // Synchronize access to the reviews JSON file
        synchronized (lockReviews) {
            try {

                // Get the list of reviews and calculate the score for the given hotel
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
                            float alpha = 0.01f;

                            // Calculate the review score
                            float reviewScore = (scores.getCleaning() + scores.getPosition() + scores.getQuality()
                                    + scores.getServices()) * globalScore;

                            // Decrease the review score based on the weeks since the review
                            reviewScore *=  Math.exp(-alpha * daysSinceReview / 7);

                            // Add more weight to the global score
                            reviewScore *= globalScore;

                            // Add the review score to the total score of all reviews
                            totalScore += reviewScore;

                            // Count the number of reviews
                            count++;
                        }
                    }
                }

                // Return the calculated score increased by the logarithm of the number of
                // reviews
                return (float) (count > 0 ? (totalScore / count) * (Math.log(1 + count)) : 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    /**
     * Sends a multicast message to the specified multicast address and port.
     *
     * @param message the message to be sent
     */
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

    /**
     * Retrieves the list of hotels from the Hotels.json file.
     *
     * <p>
     * This method reads the Hotels.json file and deserializes it into a list of
     * Hotel objects using Gson. If the file is empty,
     * an empty list is returned.
     *
     * @return the list of hotels
     * @throws IOException if there is an error reading from the Hotels.json file
     */
    private List<Hotel> getListHotels() throws IOException {

        // Deserialize the JSON file into a list of Hotel objects
        File file = new File("src/main/resources/Hotels.json");
        Type hotelListType = new TypeToken<ArrayList<Hotel>>() {
        }.getType();
        List<Hotel> hotels = new ArrayList<>();

        // Check if the file is empty then read the file
        if (file.length() != 0) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                hotels = this.gson.fromJson(br, hotelListType);
            }
        }
        return hotels;
    }

    /**
     * Retrieves the list of hotel reviews from the Reviews.json file.
     *
     * <p>
     * This method reads the Reviews.json file and deserializes it into a list of
     * HotelReviews objects using Gson. If the file is empty,
     * an empty list is returned.
     *
     * @return the list of hotel reviews
     * @throws IOException if there is an error reading from the Reviews.json file
     */
    private List<HotelReviews> getListReviews() throws IOException {

        // Deserialize the JSON file into a list of HotelReviews objects
        File file = new File("src/main/resources/Reviews.json");
        Type reviewsListType = new TypeToken<ArrayList<HotelReviews>>() {
        }.getType();
        List<HotelReviews> reviews = new ArrayList<>();

        // Check if the file is empty then read the file
        if (file.length() != 0) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                reviews = this.gson.fromJson(br, reviewsListType);
            }
        }

        return reviews;
    }

    /**
     * Retrieves the list of charts from the Charts.json file.
     *
     * <p>
     * This method reads the Charts.json file and deserializes it into a list of
     * Chart objects using Gson. If the file is empty,
     * an empty list is returned.
     *
     * @return the list of charts
     * @throws IOException if there is an error reading from the Charts.json file
     */
    private List<Chart> getListCharts() throws IOException {

        // Deserialize the JSON file into a list of Chart objects
        File file = new File("src/main/resources/Charts.json");
        Type chartListType = new TypeToken<ArrayList<Chart>>() {
        }.getType();
        List<Chart> charts = new ArrayList<>();

        // Check if the file is empty then read the file
        if (file.length() != 0) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                charts = this.gson.fromJson(br, chartListType);
            }
        }

        return charts;
    }

}
