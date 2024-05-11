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
import java.lang.reflect.Type;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class HotelierClientHandler implements Runnable {

    private Socket clientSocket;
    private Gson gson;

    private String currentUsername;
    private boolean isLogged = false;
    
    public HotelierClientHandler(Socket clientSocket, Gson gson){
        this.clientSocket = clientSocket;
        this.gson = gson;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String username, password, hotelName, city, clientMessage;

                writer.println(homeMessage());

                while((clientMessage = reader.readLine()) != null){
                    switch (clientMessage) {
                        case "1": //register
                            if(this.isLogged){
                                writer.println("You have to logout to register");
                                break;
                            }
                            writer.println("Insert username for registration:");
                            username = reader.readLine();
                            writer.println("Insert password for registration:");
                            password = reader.readLine();
                            register(username, password, writer);
                            break;

                        case "2": //login
                            if(this.isLogged){
                                writer.println("You already logged in");
                                break;
                            }
                            writer.println("Insert username for login:");
                            username = reader.readLine();
                            writer.println("Insert password for login:");
                            password = reader.readLine();
                            login(username, password, writer);
                            break;

                        case "3": //logout
                            if(!this.isLogged){
                                writer.println("You have to login to logout");
                                break;
                            }
                            logout(this.currentUsername, writer);
                            this.currentUsername = null;
                            break;

                        case "4": //searchHotel
                            writer.println("Insert the hotel name of the hotel you want to search:");
                            hotelName = reader.readLine();
                            writer.println("Insert the city of the hotel you want to search:");
                            city = reader.readLine();
                            searchHotel(hotelName, city);
                            break;

                        case "5":   //searchAllHotels
                            writer.println("Insert the city of the hotels you want to search:");
                            city = reader.readLine();
                            searchAllHotels(city);
                            break;

                        case "6":   //insertReview
                            if(!this.isLogged){
                                writer.println("You have to login to insert a review");
                                break;
                            }
                            writer.println("Insert the hotel name of the hotel you want to review:");
                            hotelName = reader.readLine();
                            writer.println("Insert the city of the hotel you want to review:");
                            city = reader.readLine();
                            writer.println("Insert the global score of the hotel you want to review from 1 to 5:");
                            int globalScore = Integer.parseInt(reader.readLine());
                            writer.println("Now you have to insert the scores of the hotel from 1 to 5");
                            
                            int[] scores = new int[4];
                            writer.println("Insert the score of the cleaning of the hotel from 1 to 5");
                            scores[0] = Integer.parseInt(reader.readLine());
                            writer.println("Insert the score of the position of the hotel from 1 to 5");
                            scores[1] = Integer.parseInt(reader.readLine());
                            writer.println("Insert the score of the services of the hotel from 1 to 5");
                            scores[2] = Integer.parseInt(reader.readLine());
                            writer.println("Insert the score of the quality of the hotel from 1 to 5");
                            scores[3] = Integer.parseInt(reader.readLine());
                            
                            insertReview(hotelName, city, globalScore, scores);
                            break;

                        case "7":   //showMyBadges
                            if(!this.isLogged){
                                writer.println("You have to login to see your badge");
                                break;
                            }
                            showMyBadge(writer);
                            break;

                        case "8":   //exit
                            writer.println("Type exit to confirm the exit");
                            if(reader.readLine().equals("exit")){
                                if(this.isLogged){
                                    logout(this.currentUsername, writer);
                                }
                                clientMessage = "exit";
                                break;
                            }
                            writer.println("Exit aborted");
                            break;

                        default:
                            writer.println("Command not found");
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
            return (this.currentUsername + ", welcome to the Hotel Booking System HOTELIER!\nThe commands are:\n[1] register <username> <password>\n[2] login <username> <password>\n[3] logout <username>\n[4] search a hotel <hotelName> <city>\n[5] search all the hotels in a city <city>\n[6] insert a review for a hotel <hotelName> <city> <globalScore> <scores>\n[7] show my badge\n[8] exit");
        }else{
            return "Welcome to the Hotel Booking System HOTELIER!\nThe commands that you can do are (for some of these you will have to login):\n[1] register <username> <password>\n[2] login <username> <password>\n[3] logout <username> (login required)\n[4] search a hotel <hotelName> <city>\n[5] search all the hotels in a city <city>\n[6] insert a review for a hotel <hotelName> <city> <globalScore> <scores> (login required)\n[7] show my badge (login required)\n[8] exit";
        }

    }

    private synchronized void register (String username, String password, PrintWriter writer) throws NoSuchAlgorithmException, IOException{

        User user = new User(username, hashPassword(password), false, "Recensore", 0);

        List<User> users = getListUsers();
        File file = new File("src/main/resources/Users.json");

        for (User existingUser : users) {
            if (existingUser.getUsername().equals(username)) {
                writer.println("Username already exists! Chose another one!");
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
        writer.println("User registered successfully!");
    }

    private synchronized void login (String username, String password, PrintWriter writer) throws NoSuchAlgorithmException, IOException {
        
        File file = new File("src/main/resources/Users.json");
        List<User> users = getListUsers();

        for (User existingUser : users) {
            if (existingUser.getUsername().equals(username)) {
                if (existingUser.getHashPassword().equals(hashPassword(password))) {
                    existingUser.setLogged(true);
                    FileWriter fileWriter = new FileWriter(file);
                    gson.toJson(users, fileWriter);
                    fileWriter.flush();
                    fileWriter.close();
                    this.isLogged = true;
                    this.currentUsername = username;
                    writer.println("Login successful");
                    return;
                } else {
                    writer.println("Incorrect password");
                    return;
                }
            }
        }

        writer.println("Username not found");
    }

    private synchronized void logout (String username, PrintWriter writer) throws IOException {    

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
                writer.println("Logout successful");
                return;
            }
        }
    }

    private void searchHotel(String hotelName, String city) {
        
    }

    private void searchAllHotels(String city) {
        
    }

    private void insertReview(String hotelName, String city, int globalScore, int[] scores) {
        
    }

    private void showMyBadge(PrintWriter writer) throws IOException {
    
        List<User> users = getListUsers();
    
        for (User existingUser : users) {
            if (existingUser.getUsername().equals(this.currentUsername)) {
                writer.println("Your badge is: " + existingUser.getBadge());
                return;
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