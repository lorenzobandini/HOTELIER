package com.unipi.lorenzobandini.hotelier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class HotelierClientHandler implements Runnable {

    String reset = "\u001B[0m";
    String yellow = "\u001B[33m";
    String green = "\u001B[32m";
    String red = "\u001B[31m";
    String blue = "\u001B[34m";

    private Socket clientSocket;
    private Gson gson;

    private String currentUsername;
    private boolean isLogged = false;

    private final Object lockUsers = new Object();
    private final Object lockHotels = new Object();

    public HotelierClientHandler(Socket clientSocket, Gson gson){
        this.clientSocket = clientSocket;
        this.gson = gson;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String username, password, hotelName, city, clientMessage;
                writer.println(yellow +
                "██╗  ██╗ ██████╗ ████████╗███████╗██╗     ██╗███████╗██████╗ \n" +
                "██║  ██║██╔═══██╗╚══██╔══╝██╔════╝██║     ██║██╔════╝██╔══██╗\n" +
                "███████║██║   ██║   ██║   █████╗  ██║     ██║█████╗  ██████╔╝\n" +
                "██╔══██║██║   ██║   ██║   ██╔══╝  ██║     ██║██╔══╝  ██╔══██╗\n" +
                "██║  ██║╚██████╔╝   ██║   ███████╗███████╗██║███████╗██║  ██║\n" +
                "╚═╝  ╚═╝ ╚═════╝    ╚═╝   ╚══════╝╚══════╝╚═╝╚══════╝╚═╝  ╚═╝\n" +
                "                                                              "+reset);
                writer.println(homeMessage());

                while((clientMessage = reader.readLine()) != null){
                    switch (clientMessage) {
                        case "1": //register
                            if(this.isLogged){
                                writer.println(red+"You have to logout to register" + reset);
                                break;
                            }
                            writer.println(yellow + "Insert username for registration:" + reset);
                            username = reader.readLine();
                            writer.println(yellow +"Insert password for registration:" + reset);
                            password = reader.readLine();
                            register(username, password, writer);
                            break;

                        case "2": //login
                            if(this.isLogged){
                                writer.println(red + "You already logged in" + reset);
                                break;
                            }
                            writer.println(yellow +"Insert username for login:" + reset);
                            username = reader.readLine();
                            writer.println(yellow + "Insert password for login:" + reset);
                            password = reader.readLine();
                            login(username, password, writer);
                            break;

                        case "3": //logout
                            if(!this.isLogged){
                                writer.println(red +"You have to login to logout"+reset);
                                break;
                            }
                            logout(this.currentUsername, writer);
                            this.currentUsername = null;
                            break;

                        case "4": //searchHotel
                            writer.println(yellow +"Insert the hotel name of the hotel you want to search:"+ reset);
                            hotelName = reader.readLine();
                            writer.println(yellow +"Insert the city of the hotel you want to search:" + reset);
                            city = reader.readLine();
                            searchHotel(hotelName, city, writer);
                            break;

                        case "5":   //searchAllHotels
                            writer.println(yellow + "Insert the city of the hotels you want to search:" + reset);
                            city = reader.readLine();
                            searchAllHotels(city, writer);
                            break;

                        case "6":   //insertReview
                            if(!this.isLogged){
                                writer.println(red + "You have to login to insert a review"+ reset);
                                break;
                            }
                            writer.println(yellow +"Insert the hotel name of the hotel you want to review:" + reset);
                            hotelName = reader.readLine();
                            writer.println(yellow +"Insert the city of the hotel you want to review:" + reset);
                            city = reader.readLine();
                            writer.println(yellow +"Insert the global score of the hotel you want to review from 1 to 5:" + reset);
                            int globalScore = Integer.parseInt(reader.readLine());
                            writer.println(yellow +"Now you have to insert the scores of the hotel from 1 to 5" + reset);
                            
                            int[] scores = new int[4];
                            writer.println(yellow +"Insert the score of the cleaning of the hotel from 1 to 5" + reset);
                            scores[0] = Integer.parseInt(reader.readLine());
                            writer.println(yellow +"Insert the score of the position of the hotel from 1 to 5" + reset);
                            scores[1] = Integer.parseInt(reader.readLine());
                            writer.println(yellow +"Insert the score of the services of the hotel from 1 to 5" + reset);
                            scores[2] = Integer.parseInt(reader.readLine());
                            writer.println(yellow +"Insert the score of the quality of the hotel from 1 to 5" + reset);
                            scores[3] = Integer.parseInt(reader.readLine());
                            
                            insertReview(hotelName, city, globalScore, scores);
                            break;

                        case "7":   //showMyBadges
                            if(!this.isLogged){
                                writer.println(red +"You have to login to see your badge" + reset);
                                break;
                            }
                            showMyBadge(writer);
                            break;

                        case "8":   //exit
                            writer.println(yellow +"Type exit to confirm the exit" + reset);
                            if(reader.readLine().equals("exit")){
                                if(this.isLogged){
                                    logout(this.currentUsername, writer);
                                }
                                clientMessage = "exit";
                                break;
                            }
                            writer.println(red +"Exit aborted" + reset);
                            break;

                        default:
                            writer.println(red +"Command not found"+ reset);
                            break;
                    }
                    if(clientMessage.equals("exit")){
                        break;
                    }
                    writer.println(homeMessage());
                }

                reader.close();
                writer.close();
                clientSocket.close();
                System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " disconnected");

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
    }

    private String homeMessage(){
        if(this.isLogged){
            return (yellow+ this.currentUsername + ", welcome to the Hotel Booking System HOTELIER!\nThe commands are:\n[1] register <username> <password>\n[2] login <username> <password>\n[3] logout <username>\n[4] search a hotel <hotelName> <city>\n[5] search all the hotels in a city <city>\n[6] insert a review for a hotel <hotelName> <city> <globalScore> <scores>\n[7] show my badge\n[8] exit"+ reset);
        }else{
            return (yellow + "Welcome to the Hotel Booking System HOTELIER!\nThe commands that you can do are (for some of these you will have to login):\n[1] register <username> <password>\n[2] login <username> <password>\n[3] logout <username> (login required)\n[4] search a hotel <hotelName> <city>\n[5] search all the hotels in a city <city>\n[6] insert a review for a hotel <hotelName> <city> <globalScore> <scores> (login required)\n[7] show my badge (login required)\n[8] exit"+ reset);
        }

    }

    private void register (String username, String password, PrintWriter writer) throws NoSuchAlgorithmException, IOException{
        synchronized (lockUsers) {
            if(username.equals("") || password.equals("")){
                writer.println(red +"Username or password cannot be empty!"+ reset);
                return;
            }
            User user = new User(username, hashPassword(password), false, "Recensore", 0);

            List<User> users = getListUsers();
            File file = new File("src/main/resources/Users.json");

            for (User existingUser : users) {
                if (existingUser.getUsername().equals(username)) {
                    writer.println(red +"Username already exists! Chose another one!" + reset);
                    return;
                }
            }

            // Aggiungi il nuovo utente alla lista
            users.add(user);

            // Riscrivi il file con la lista aggiornata
            FileWriter fileWriter = new FileWriter(file);
            this.gson.toJson(users, fileWriter);
            fileWriter.flush();
            fileWriter.close();
            writer.println(green +"User registered successfully!" + reset);
        }
    }

    private void login (String username, String password, PrintWriter writer) throws NoSuchAlgorithmException, IOException {
        synchronized (lockUsers) {

            File file = new File("src/main/resources/Users.json");
            List<User> users = getListUsers();

            for (User existingUser : users) {
                if (existingUser.getUsername().equals(username)) {
                    if(existingUser.isLogged()){
                        writer.println(red +"User already logged in" + reset);
                        return;
                    }
                    if (existingUser.getHashPassword().equals(hashPassword(password))) {
                        existingUser.setLogged(true);
                        FileWriter fileWriter = new FileWriter(file);
                        gson.toJson(users, fileWriter);
                        fileWriter.flush();
                        fileWriter.close();
                        this.isLogged = true;
                        this.currentUsername = username;
                        writer.println(green +"Login successful"+reset);
                        return;
                    } else {
                        writer.println(red+"Incorrect password"+reset);
                        return;
                    }
                }
            }
        writer.println(red+"Username not found"+reset);
        }
    }

    private void logout (String username, PrintWriter writer) throws IOException {    
        synchronized (lockUsers) {
            File file = new File("src/main/resources/Users.json");
            List<User> users = getListUsers();
        
            for (User existingUser : users) {
                if (existingUser.getUsername().equals(username)) {
                    // Imposta isLogged a false e aggiorna il file JSON
                    existingUser.setLogged(false);
                    FileWriter fileWriter = new FileWriter(file);
                    this.gson.toJson(users, fileWriter);
                    fileWriter.flush();
                    fileWriter.close();
                    this.isLogged = false;
                    this.currentUsername = null;
                    writer.println(green+"Logout successful"+reset);
                    return;
                }
            }
        }
    }

    private void searchHotel(String hotelName, String city, PrintWriter writer) throws IOException {
        synchronized (lockHotels) {
            List<Hotel> hotels = getListHotels();

            for (Hotel hotel : hotels) {
                if (hotel.getName().equals(hotelName) && hotel.getCity().equals(city)) {
                    printHotelStat(hotel, writer);
                    writer.println(yellow +"---------------------------------------------" + reset);
                    return;
                }
            }
            writer.println(red+"Hotel not found"+reset);
            
        }
    }

    private void searchAllHotels(String city, PrintWriter writer) throws IOException {
        synchronized(lockHotels) {
            List<Hotel> hotels = getListHotels();
            for (Hotel hotel : hotels) {
                if (hotel.getCity().equals(city)) {
                    printHotelStat(hotel, writer);
                }
            }
        writer.println(yellow +"---------------------------------------------" + reset);
        }
        
    }

    private void insertReview(String hotelName, String city, int globalScore, int[] scores) {
        
    }

    private void showMyBadge(PrintWriter writer) throws IOException {
        synchronized (lockUsers) {
            List<User> users = getListUsers();
        
            for (User existingUser : users) {
                if (existingUser.getUsername().equals(this.currentUsername)) {
                    writer.println(blue+"Your badge is: " + existingUser.getBadge()+reset);
                    return;
                }
            }
        }
    }

    private List<User> getListUsers() throws IOException {

        File file = new File("src/main/resources/Users.json");
        BufferedReader br = new BufferedReader(new FileReader(file));
        Type userListType = new TypeToken<ArrayList<User>>(){}.getType();
        List<User> users = new ArrayList<>();
        if (file.length() != 0) {
            users = this.gson.fromJson(br, userListType);
        }
        return users;
    }

    private List<Hotel> getListHotels() throws IOException {

        File file = new File("src/main/resources/Hotels.json");
        BufferedReader br = new BufferedReader(new FileReader(file));
        Type hotelListType = new TypeToken<ArrayList<Hotel>>(){}.getType();
        List<Hotel> hotels = new ArrayList<>();
        if (file.length() != 0) {
            hotels = this.gson.fromJson(br, hotelListType);
        }
        return hotels;
    }

    private void printHotelStat(Hotel hotel, PrintWriter writer) {
        writer.println(yellow +"---------------------------------------------" + reset);
        writer.println(yellow +"Hotel found: " + blue + hotel.getName()+ reset);
        writer.println(yellow +"Description: " + blue + hotel.getDescription()+ reset);
        writer.println(yellow +"City: " + blue +hotel.getCity()+ reset);
        writer.println(yellow +"Phone: " + blue + hotel.getPhone()+ reset);
        writer.println(yellow +"Services:" + reset);
        for (String service : hotel.getServices()) {
            writer.println(blue + "    " + service + reset);
        }
        writer.println(yellow + "Rate: " + blue + hotel.getRate() + reset);
        writer.println(yellow +"Ratings:"+reset);
        Map<String, Integer> ratings = hotel.getRatings();
        for (Map.Entry<String, Integer> entry : ratings.entrySet()) {
            writer.println(blue + "    " + entry.getKey() + ": " + entry.getValue() + reset);
        }
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toLowerCase();
    }
}

class User{
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
    }
}

class Hotel {
    private int id;
    private String name;
    private String description;
    private String city;
    private String phone;
    private List<String> services;
    private int rate;
    private Map<String, Integer> ratings;

    public Hotel(int id, String name, String description, String city, String phone, List<String> services, int rate, Map<String, Integer> ratings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = services;
        this.rate = rate;
        this.ratings = ratings;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public Map<String, Integer> getRatings() {
        return ratings;
    }

    public void setRatings(Map<String, Integer> ratings) {
        this.ratings = ratings;
    }

    // Getters e setters per ogni campo...
}