package com.unipi.lorenzobandini.hotelier;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unipi.lorenzobandini.hotelier.model.Chart;
import com.unipi.lorenzobandini.hotelier.model.Hotel;

public class HotelierUpdaterChart implements Runnable {

    private int timerUpdates;
    private Gson gson;
    private MulticastSocket multicastSocket;
    private int multicastPort;
    private String multicastAddress;

    public HotelierUpdaterChart(int timerUpdates, Gson gson, MulticastSocket multicastSocket, int multicastPort,
            String multicastAddress) {
        this.timerUpdates = timerUpdates;
        this.gson = gson;
        this.multicastSocket = multicastSocket;
        this.multicastPort = multicastPort;
        this.multicastAddress = multicastAddress;
    }

    @Override
    public void run() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        byte[] buf = ("Awake").getBytes();
        InetAddress group;
        try {
            group = InetAddress.getByName(this.multicastAddress);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, this.multicastPort);
            executor.scheduleAtFixedRate(() -> {
                try {
                    multicastSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, 0, 250, TimeUnit.MILLISECONDS);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // createHotelierChart();

    }

    // synchronized public void createHotelierChart() {
    // try {
    // // Leggi il file JSON
    // File file = new File("src/main/resources/Hotels.json");
    // BufferedReader br = new BufferedReader(new FileReader(file));
    // Type userListType = new TypeToken<ArrayList<Hotel>>() {
    // }.getType();
    // List<Hotel> hotels = new ArrayList<>();
    // if (file.length() != 0) {
    // hotels = this.gson.fromJson(br, userListType);
    // } else {
    // br.close();
    // return;
    // }

    // // Crea una mappa con le città e il numero di hotel per ogni città
    // Map<String, Integer> cities = new LinkedHashMap<>();
    // for (Hotel hotel : hotels) {
    // if (cities.containsKey(hotel.getCity())) {
    // cities.put(hotel.getCity(), cities.get(hotel.getCity()) + 1);
    // } else {
    // cities.put(hotel.getCity(), 1);
    // }
    // }

    // // Crea un oggetto Chart per ogni città
    // List<Chart> charts = new ArrayList<>();

    // for (Map.Entry<String, Integer> entry : cities.entrySet()) {
    // Chart chart = new Chart(entry.getKey());
    // chart.getHotels().put("Hotels", entry.getValue());
    // charts.add(chart);
    // }

    // // Scrivi le mappe in un file JSON
    // this.gson.toJson(charts);
    // br.close();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    // synchronized public void updateHotelierChart() {
    // try {
    // // Leggi il file JSON
    // File file = new File("src/main/resources/Charts.json");
    // BufferedReader br = new BufferedReader(new FileReader(file));
    // Type chartListType = new TypeToken<ArrayList<Chart>>() {
    // }.getType();
    // List<Chart> charts = new ArrayList<>();
    // if (file.length() != 0) {
    // charts = this.gson.fromJson(br, chartListType);
    // } else {
    // br.close();
    // return;
    // }

    // // Ordina le mappe hotels in ogni oggetto Chart
    // for (Chart chart : charts) {
    // List<Map.Entry<String, Integer>> list = new
    // ArrayList<>(chart.getHotels().entrySet());
    // list.sort(Map.Entry.comparingByValue());

    // // Aggiorna la mappa con l'ordine ordinato
    // Map<String, Integer> sortedHotels = new LinkedHashMap<>();
    // for (Map.Entry<String, Integer> entry : list) {
    // sortedHotels.put(entry.getKey(), entry.getValue());
    // }
    // chart.setHotels(sortedHotels);
    // br.close();
    // return;
    // }

    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
}
