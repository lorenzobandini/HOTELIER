package com.unipi.lorenzobandini.hotelier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class HotelierClientHandler implements Runnable {

    private Socket clientSocket;

    private String username;
    private boolean isLogged = false;
    
    public HotelierClientHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
                
                String password, hotelName, city;
                
                String clientMessage;

                writer.println(homeMessage());

                while((clientMessage = reader.readLine()) != null){
                    switch (clientMessage) {
                        case "1": //register
                            if(this.isLogged){
                                writer.println("You have to logout to register");
                                break;
                            }
                            writer.println("Insert username for registration:");
                            this.username = reader.readLine();
                            writer.println("Insert password for registration:");
                            password = reader.readLine();
                            register(username, password);
                            break;

                        case "2": //login
                            if(this.isLogged){
                                writer.println("You already logged in");
                                break;
                            }
                            writer.println("Insert username for login:");
                            this.username = reader.readLine();
                            writer.println("Insert password for login:");
                            password = reader.readLine();
                            login(this.username, password);
                            break;

                        case "3": //logout
                            if(!this.isLogged){
                                writer.println("You have to login to logout");
                                break;
                            }
                            logout(this.username);
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
                            
                            int[] scores = new int[5];
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
                                writer.println("You have to login to see your badges");
                                break;
                            }
                            showMyBadges();
                            break;

                        case "8":   //exit
                            writer.println("Type exit to confirm the exit");
                            if(reader.readLine().equals("exit")){
                                writer.println("Thank you for using our Hotel Booking System HOTELIER! Bye bye!");
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
            }
    }

    private String homeMessage(){
        if(this.isLogged){
            return "Welcome to the Hotel Booking System HOTELIER!\nThe commands are:\n[1] register <username> <password>\n[2] login <username> <password>\n[3] logout <username>\n[4] search a hotel <hotelName> <city>\n[5] search all the hotels in a city <city>\n[6] insert a review for a hotel <hotelName> <city> <globalScore> <scores>\n[7] show my badge\n[8] exit";
        }else{
            return "Welcome to the Hotel Booking System HOTELIER!\nThe commands that you can do are (for some of these you will have to login):\n[1] register <username> <password>\n[2] login <username> <password>\n[3] logout <username> (login required)\n[4] search a hotel <hotelName> <city>\n[5] search all the hotels in a city <city>\n[6] insert a review for a hotel <hotelName> <city> <globalScore> <scores> (login required)\n[7] show my badge (login required)\n[8] exit";
        }

    }

    private void register (String username, String password) {

    }

    private void login (String username, String password) {

    }

    private void logout (String username) {

    }

    private void searchHotel(String hotelName, String city) {
        
    }

    private void searchAllHotels(String city) {
        
    }

    private void insertReview(String hotelName, String city, int globalScore, int[] scores) {
        
    }

    private void showMyBadges() {
        
    }
}
