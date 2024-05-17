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
import com.unipi.lorenzobandini.hotelier.model.Hotel;
import com.unipi.lorenzobandini.hotelier.model.HotelReviews;
import com.unipi.lorenzobandini.hotelier.model.Ratings;
import com.unipi.lorenzobandini.hotelier.model.Review;
import com.unipi.lorenzobandini.hotelier.model.User;

public class HotelierClientHandler implements Runnable {

    // ANSI escape codes
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
    private final Object lockHotels;
    private final Object lockReviews;

    /**
     * Constructs a new HotelierClientHandler.
     *
     * @param clientSocket the socket connected to the client
     * @param gson         the Gson object used for JSON serialization and
     *                     deserialization
     * @param lockHotels   the lock object for synchronizing access to the hotels
     *                     data
     * @param lockReviews  the lock object for synchronizing access to the reviews
     *                     data
     */
    public HotelierClientHandler(Socket clientSocket, Gson gson, Object lockHotels, Object lockReviews) {
        this.clientSocket = clientSocket;
        this.gson = gson;
        this.lockHotels = lockHotels;
        this.lockReviews = lockReviews;
    }

    /**
     * Executes the main logic of the HotelierClientHandler.
     * 
     * <p>
     * This method is invoked when the thread for this HotelierClientHandler is
     * started. It sets up
     * input and output streams for the client socket, and then enters a loop where
     * it reads and processes
     * messages from the client.
     * </p>
     */
    @Override
    public void run() {

        // Initialize input and output streams for single client
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String username, password, hotelName, city, clientMessage;
            writer.println();
            writer.println(yellow +
                    "██╗  ██╗ ██████╗ ████████╗███████╗██╗     ██╗███████╗██████╗ \n" +
                    "██║  ██║██╔═══██╗╚══██╔══╝██╔════╝██║     ██║██╔════╝██╔══██╗\n" +
                    "███████║██║   ██║   ██║   █████╗  ██║     ██║█████╗  ██████╔╝\n" +
                    "██╔══██║██║   ██║   ██║   ██╔══╝  ██║     ██║██╔══╝  ██╔══██╗\n" +
                    "██║  ██║╚██████╔╝   ██║   ███████╗███████╗██║███████╗██║  ██║\n" +
                    "╚═╝  ╚═╝ ╚═════╝    ╚═╝   ╚══════╝╚══════╝╚═╝╚══════╝╚═╝  ╚═╝\n" +
                    "                                                              " + reset);
            writer.println(homeMessage());

            // Main loop for reading client messages and processing them, stops when the
            // client sends "exit"
            while ((clientMessage = reader.readLine()) != null) {
                switch (clientMessage) {

                    case "1": // register
                        if (this.isLogged) {
                            writer.println(red + "You have to logout to register" + reset);
                            break;
                        }
                        writer.println(yellow + "Insert username for registration:" + reset);
                        username = reader.readLine();
                        writer.println(yellow + "Insert password for registration:" + reset);
                        password = reader.readLine();
                        register(username, password, writer);
                        break;

                    case "2": // login
                        if (this.isLogged) {
                            writer.println(red + "You already logged in" + reset);
                            break;
                        }
                        writer.println(yellow + "Insert username for login:" + reset);
                        username = reader.readLine();
                        writer.println(yellow + "Insert password for login:" + reset);
                        password = reader.readLine();
                        login(username, password, writer);
                        break;

                    case "3": // logout
                        if (!this.isLogged) {
                            writer.println(red + "You have to login to logout" + reset);
                            break;
                        }
                        logout(this.currentUsername, writer);
                        this.currentUsername = null;
                        break;

                    case "4": // searchHotel
                        writer.println(yellow + "Insert the city of the hotel you want to search:" + reset);
                        city = reader.readLine();
                        writer.println(yellow + "Insert the hotel name of the hotel you want to search:" + reset);
                        hotelName = reader.readLine();
                        searchHotel(hotelName, city, writer);
                        break;

                    case "5": // searchAllHotels
                        writer.println(yellow + "Insert the city of the hotels you want to search:" + reset);
                        city = reader.readLine();
                        searchAllHotels(city, writer);
                        break;

                    case "6": // insertReview
                        if (!this.isLogged) {
                            writer.println(red + "You have to login to insert a review" + reset);
                            break;
                        }
                        writer.println(yellow + "Insert the city of the hotel you want to review:" + reset);
                        city = reader.readLine();
                        writer.println(yellow + "Insert the hotel name of the hotel you want to review:" + reset);
                        hotelName = reader.readLine();
                        if (!checkHotel(hotelName, city)) {
                            writer.println(red + "Hotel not found!" + reset);
                            break;
                        }

                        // Get the scores for the review
                        int globalScore = getScore("global ", writer, reader);

                        int rateCleaning = getScore("cleaning ", writer, reader);
                        int ratePosition = getScore("position ", writer, reader);
                        int rateServices = getScore("services ", writer, reader);
                        int rateQuality = getScore("quality ", writer, reader);

                        Ratings ratings = new Ratings((float) rateCleaning, (float) ratePosition, (float) rateServices,
                                (float) rateQuality);

                        insertReview(hotelName, city, globalScore, ratings, writer);
                        break;

                    case "7": // showMyBadges
                        if (!this.isLogged) {
                            writer.println(red + "You have to login to see your badge" + reset);
                            break;
                        }
                        showMyBadge(writer);
                        break;

                    case "8": // exit
                        writer.println(yellow + "Type exit to confirm the exit" + reset);
                        if (reader.readLine().equals("exit")) {
                            if (this.isLogged) {
                                logout(this.currentUsername, writer);
                            }
                            clientMessage = "exit";
                            break;
                        }
                        writer.println(red + "Exit aborted" + reset);
                        break;

                    default:
                        writer.println(red + "Command not found" + reset);
                        break;
                }
                if (clientMessage.equals("exit")) {
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

    private String homeMessage() {
        if (this.isLogged) {

            // Message for logged users with all possible commands
            return (yellow + this.currentUsername
                    + ", welcome to the Hotel Booking System HOTELIER!\nThe commands are:\n[1] Register\n[2] Login\n[3] Logout\n[4] Search a hotel\n[5] Search all the hotels in a city\n[6] Insert a review for a hotel\n[7] Show my badge\n[8] Exit"
                    + reset);
        } else {

            // Message for not logged users with only the commands that don't require login
            return (yellow
                    + "Welcome to the Hotel Booking System HOTELIER!\nThe commands that you can do are (for some of these you will have to login):\n[1] Register\n[2] Login\n[3] Logout (login required)\n[4] Search a hotel\n[5] Search all the hotels in a city\n[6] Insert a review for a hotel (login required)\n[7] Show my badge (login required)\n[8] Exit"
                    + reset);
        }

    }

    /**
     * Registers a new user.
     *
     * <p>
     * This method creates a new User object with the provided username and
     * password, checks if the username
     * already exists in the list of users, and if not, adds the new user to the
     * list and writes the updated
     * list to the Users.json file.
     *
     * <p>
     * The method is synchronized on the lockUsers object to prevent concurrent
     * modifications of the users list.
     *
     * @param username the username of the new user
     * @param password the password of the new user
     * @param writer   the PrintWriter to send messages to the client
     * @throws NoSuchAlgorithmException if the password hashing algorithm is not
     *                                  found
     * @throws IOException              if there is an error writing to the
     *                                  Users.json file
     */
    private void register(String username, String password, PrintWriter writer)
            throws NoSuchAlgorithmException, IOException {

        // Synchronized the access to the users JSON file
        synchronized (lockUsers) {
            if (username.equals("") || password.equals("")) {
                writer.println(red + "Username or password cannot be empty!" + reset);
                return;
            }

            // Create a new user object with hashed password
            User user = new User(username, hashPassword(password), false, "Recensore", 0);

            List<User> users = getListUsers();
            File file = new File("src/main/resources/Users.json");

            for (User existingUser : users) {
                if (existingUser.getUsername().equals(username)) {
                    writer.println(red + "Username already exists! Chose another one!" + reset);
                    return;
                }
            }

            // Add the new user to the list
            users.add(user);

            // Write the new user list to the JSON file
            FileWriter fileWriter = new FileWriter(file);
            this.gson.toJson(users, fileWriter);
            fileWriter.flush();
            fileWriter.close();
            writer.println(green + "User registered successfully!" + reset);
        }
    }

    /**
     * Logs in a user.
     *
     * <p>
     * This method retrieves the list of users from the Users.json file, checks if
     * the provided username exists
     * and if the user is not already logged in, and if the provided password
     * matches the stored hashed password.
     * If all checks pass, the user is marked as logged in and the updated users
     * list is written back to the Users.json file.
     *
     * <p>
     * The method is synchronized on the lockUsers object to prevent concurrent
     * modifications of the users list.
     *
     * @param username the username of the user trying to log in
     * @param password the password of the user trying to log in
     * @param writer   the PrintWriter to send messages to the client
     * @throws NoSuchAlgorithmException if the password hashing algorithm is not
     *                                  found
     * @throws IOException              if there is an error reading from or writing
     *                                  to the Users.json file
     */
    private void login(String username, String password, PrintWriter writer)
            throws NoSuchAlgorithmException, IOException {
        // Synchronized the access to the users JSON file
        synchronized (lockUsers) {

            // Get the list of users from the JSON file
            File file = new File("src/main/resources/Users.json");
            List<User> users = getListUsers();

            // Check if the username exists and if the password is correct
            for (User existingUser : users) {
                if (existingUser.getUsername().equals(username)) {
                    if (existingUser.isLogged()) {
                        writer.println(red + "User already logged in" + reset);
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
                        writer.println(green + "Login successful" + reset);
                        return;
                    } else {
                        writer.println(red + "Incorrect password" + reset);
                        return;
                    }
                }
            }
            writer.println(red + "Username not found" + reset);
        }
    }

    /**
     * Logs out a user.
     *
     * <p>
     * This method retrieves the list of users from the Users.json file, finds the
     * user with the provided username,
     * and sets their isLogged status to false. The updated users list is then
     * written back to the Users.json file.
     *
     * <p>
     * The method is synchronized on the lockUsers object to prevent concurrent
     * modifications of the users list.
     *
     * @param username the username of the user trying to log out
     * @param writer   the PrintWriter to send messages to the client
     * @throws IOException if there is an error reading from or writing to the
     *                     Users.json file
     */
    private void logout(String username, PrintWriter writer) throws IOException {

        // Synchronized the access to the users JSON file
        synchronized (lockUsers) {
            File file = new File("src/main/resources/Users.json");
            List<User> users = getListUsers();

            // Find the user in the list and set isLogged to false
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
                    writer.println(green + "Logout successful" + reset);
                    return;
                }
            }
        }
    }

    /**
     * Searches for a hotel by name and city.
     *
     * <p>
     * This method retrieves the list of hotels from the Hotels.json file, and
     * checks if there is a hotel
     * with the provided name and city. If such a hotel is found, its statistics are
     * printed.
     *
     * <p>
     * The method is synchronized on the lockHotels object to prevent concurrent
     * modifications of the hotels list.
     *
     * @param hotelName the name of the hotel to search for
     * @param city      the city where the hotel is located
     * @param writer    the PrintWriter to send messages to the client
     * @throws IOException if there is an error reading from the Hotels.json file
     */
    private void searchHotel(String hotelName, String city, PrintWriter writer) throws IOException {

        // Synchronized the access to the hotels JSON file
        synchronized (lockHotels) {

            // Get the list of hotels from the JSON file
            List<Hotel> hotels = getListHotels();

            // Print the hotel information if found
            for (Hotel hotel : hotels) {
                if (hotel.getName().equals(hotelName) && hotel.getCity().equals(city)) {
                    printHotelStat(hotel, writer);
                    writer.println(yellow + "------------------------------------------------" + reset);
                    return;
                }
            }
            writer.println(red + "Hotel not found" + reset);

        }
    }

    /**
     * Searches for all hotels in a given city.
     *
     * <p>
     * This method retrieves the list of hotels from the Hotels.json file, and
     * checks if their city matches
     * the provided city. For each matching hotel, its statistics are printed. If no
     * hotels are found in the
     * given city, a message is printed to inform the user.
     *
     * <p>
     * The method is synchronized on the lockHotels object to prevent concurrent
     * modifications of the hotels list.
     *
     * @param city   the city where the hotels are located
     * @param writer the PrintWriter to send messages to the client
     * @throws IOException if there is an error reading from the Hotels.json file
     */
    private void searchAllHotels(String city, PrintWriter writer) throws IOException {

        // Synchronized the access to the hotels JSON file
        synchronized (lockHotels) {

            // Get the list of hotels from the JSON file
            List<Hotel> hotels = getListHotels();

            // Flag to check if any hotel was found
            boolean hotelFound = false;

            // Print every hotel in the city
            for (Hotel hotel : hotels) {
                if (hotel.getCity().equals(city)) {
                    printHotelStat(hotel, writer);
                    hotelFound = true;
                }
            }

            // If no hotel was found, print a message
            if (!hotelFound) {
                writer.println(red + "No hotels found in this city" + reset);
            } else {
                writer.println(yellow + "------------------------------------------------" + reset);
            }
        }
    }

    /**
     * Inserts a review for a hotel.
     *
     * <p>
     * This method retrieves the list of reviews from the Reviews.json file, creates
     * a new review with the provided
     * parameters, and adds it to the list of reviews for the specified hotel. If
     * the hotel does not exist in the list,
     * a new HotelReviews object is created. The updated list of reviews is then
     * written back to the Reviews.json file.
     *
     * <p>
     * The method is synchronized on the lockReviews object to prevent concurrent
     * modifications of the reviews list.
     *
     * @param hotelName   the name of the hotel to review
     * @param city        the city where the hotel is located
     * @param globalScore the global score of the review
     * @param ratings     the detailed ratings of the review
     * @param writer      the PrintWriter to send messages to the client
     * @throws IOException if there is an error reading from or writing to the
     *                     Reviews.json file
     */
    private void insertReview(String hotelName, String city, float globalScore, Ratings ratings, PrintWriter writer)
            throws IOException {

        // Synchronized the access to the reviews JSON file
        synchronized (lockReviews) {

            // Get the list of reviews from the JSON file
            List<HotelReviews> allHotelsReviews = getListReviews();

            // Create a new review object with the provided scores
            Review review = new Review(this.currentUsername, globalScore, ratings);

            // Check if the hotel exists and add the review
            for (HotelReviews hotelReviews : allHotelsReviews) {
                if (hotelReviews.getHotelName().equals(hotelName) && hotelReviews.getCity().equals(city)) {
                    hotelReviews.addReview(review);
                    FileWriter fileWriter = new FileWriter("src/main/resources/Reviews.json");
                    this.gson.toJson(allHotelsReviews, fileWriter);
                    fileWriter.flush();
                    fileWriter.close();
                    updateHotelRate(hotelName, city);
                    updateBadge();
                    writer.println(green + "Review added successfully" + reset);
                    return;
                }
            }

            // If the hotel does not exist, create a new HotelReviews object and add the
            // review
            HotelReviews hotelReviews = new HotelReviews(hotelName, city);
            hotelReviews.addReview(review);
            allHotelsReviews.add(hotelReviews);
            FileWriter fileWriter = new FileWriter("src/main/resources/Reviews.json");
            this.gson.toJson(allHotelsReviews, fileWriter);
            fileWriter.flush();
            fileWriter.close();
            updateHotelRate(hotelName, city);
            updateBadge();
            writer.println(green + "Review added successfully" + reset);
        }
    }

    /**
     * Shows the badge of the current user.
     *
     * <p>
     * This method retrieves the list of users from the Users.json file, finds the
     * current user, and prints their badge.
     *
     * <p>
     * The method is synchronized on the lockUsers object to prevent concurrent
     * modifications of the users list.
     *
     * @param writer the PrintWriter to send messages to the client
     * @throws IOException if there is an error reading from the Users.json file
     */
    private void showMyBadge(PrintWriter writer) throws IOException {

        // Synchronized the access to the users JSON file
        synchronized (lockUsers) {

            // Get the list of users from the JSON file
            List<User> users = getListUsers();

            // Print the badge of the current user
            for (User existingUser : users) {
                if (existingUser.getUsername().equals(this.currentUsername)) {
                    writer.println(blue + "Your badge is: " + existingUser.getBadge() + reset);
                    return;
                }
            }
        }
    }

    /**
     * Retrieves the list of users from the Users.json file.
     *
     * <p>
     * This method reads the Users.json file and deserializes it into a list of User
     * objects using Gson. If the file is empty,
     * an empty list is returned.
     *
     * @return the list of users
     * @throws IOException if there is an error reading from the Users.json file
     */
    private List<User> getListUsers() throws IOException {

        // Synchronized the access to the users JSON file
        synchronized (lockUsers) {

            // Deserialize the JSON file into a list of User objects
            File file = new File("src/main/resources/Users.json");
            Type userListType = new TypeToken<ArrayList<User>>() {
            }.getType();
            List<User> users = new ArrayList<>();

            // Check if the file is empty then read the file
            if (file.length() != 0) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    users = this.gson.fromJson(br, userListType);
                }
            }
            return users;
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

        synchronized (lockHotels) {
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

        synchronized (lockReviews) {
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
    }

    /**
     * Prints the statistics of a given hotel.
     *
     * <p>
     * This method receives a Hotel object and a PrintWriter object. It uses the
     * PrintWriter to print the hotel's name,
     * description, city, phone number, services, rate, and ratings for cleaning,
     * position, services, and quality.
     *
     * @param hotel  the Hotel object whose statistics are to be printed
     * @param writer the PrintWriter object used to print the statistics
     */
    private void printHotelStat(Hotel hotel, PrintWriter writer) {
        writer.println(yellow + "---------------------------------------------" + reset);
        writer.println(yellow + "Hotel found: " + blue + hotel.getName() + reset);
        writer.println(yellow + "Description: " + blue + hotel.getDescription() + reset);
        writer.println(yellow + "City: " + blue + hotel.getCity() + reset);
        writer.println(yellow + "Phone: " + blue + hotel.getPhone() + reset);
        writer.println(yellow + "Services:" + reset);
        for (String service : hotel.getServices()) {
            writer.println(blue + "    " + service + reset);
        }
        writer.println(yellow + "Rate: " + blue + hotel.getRate() + reset);
        writer.println(yellow + "Ratings:" + reset);
        writer.println(yellow + "    Cleaning: " + blue + hotel.getRatings().getCleaning() + reset);
        writer.println(yellow + "    Position: " + blue + hotel.getRatings().getPosition() + reset);
        writer.println(yellow + "    Services: " + blue + hotel.getRatings().getServices() + reset);
        writer.println(yellow + "    Quality: " + blue + hotel.getRatings().getQuality() + reset);
    }

    /**
     * Checks if a hotel with the given name and city exists.
     *
     * <p>
     * This method retrieves the list of hotels and checks if there is a hotel with
     * the given name and city.
     * If such a hotel exists, it returns false. If no such hotel exists, it returns
     * true.
     *
     * <p>
     * The method is synchronized on the lockHotels object to prevent concurrent
     * modifications of the hotels list.
     *
     * @param hotelName the name of the hotel to check
     * @param city      the city of the hotel to check
     * @return false if a hotel with the given name and city exists, true otherwise
     * @throws IOException if there is an error reading from the Hotels.json file
     */
    private boolean checkHotel(String hotelName, String city) throws IOException {

        // Synchronized the access to the hotels JSON file
        synchronized (lockHotels) {

            // Get the list of hotels from the JSON file
            List<Hotel> hotels = getListHotels();

            // Check if the hotel exists
            for (Hotel hotel : hotels) {
                if (hotel.getName().equals(hotelName) && hotel.getCity().equals(city)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Updates the badge of the current user.
     *
     * <p>
     * This method retrieves the list of users and finds the user with the same
     * username as the current user.
     * It then increments the number of reviews of the user and updates the
     * Users.json file.
     *
     * <p>
     * The method is synchronized on the lockUsers object to prevent concurrent
     * modifications of the users list.
     *
     * @throws IOException if there is an error reading from or writing to the
     *                     Users.json file
     */
    private void updateBadge() throws IOException {

        // Synchronized the access to the users JSON file
        synchronized (lockUsers) {

            // Get the list of users from the JSON file
            List<User> users = getListUsers();

            // Find the current user and increment the number of reviews
            for (User existingUser : users) {
                if (existingUser.getUsername().equals(this.currentUsername)) {
                    existingUser.addReview();
                    FileWriter fileWriter = new FileWriter("src/main/resources/Users.json");
                    this.gson.toJson(users, fileWriter);
                    fileWriter.flush();
                    fileWriter.close();
                    return;
                }
            }
        }
    }

    /**
     * Updates the rating and reviews of a specific hotel.
     *
     * <p>
     * This method retrieves the list of reviews and hotels and searches for the
     * hotel with the specified name and city.
     * If it finds the hotel, it calculates the global score and the scores for
     * cleaning, position, services, and quality based on the hotel's reviews.
     * If there are reviews, it updates the hotel's rating and reviews with the
     * calculated average scores.
     * Finally, it writes the updated list of hotels to the JSON file.
     *
     * <p>
     * The method is synchronized on the lockReviews and lockHotels objects to
     * prevent concurrent modifications to the reviews and hotels lists.
     *
     * @param hotelName the name of the hotel to update
     * @param city      the city of the hotel to update
     */
    private void updateHotelRate(String hotelName, String city) {

        // Synchronized the access to the hotels and reviews JSON files
        synchronized (lockReviews) {
            synchronized (lockHotels) {

                try {

                    // Get the list of reviews and hotels
                    List<HotelReviews> allHotelsReviews = getListReviews();
                    List<Hotel> hotels = getListHotels();

                    // Update the rate and ratings of the hotel
                    for (Hotel hotel : hotels) {
                        if (hotel.getName().equals(hotelName) && hotel.getCity().equals(city)) {
                            float globalScore = 0.0f;
                            float cleaningScore = 0.0f;
                            float positionScore = 0.0f;
                            float servicesScore = 0.0f;
                            float qualityScore = 0.0f;
                            int reviews = 0;

                            for (HotelReviews hotelReviews : allHotelsReviews) {
                                if (hotelReviews.getHotelName().equals(hotelName)
                                        && hotelReviews.getCity().equals(city)) {
                                    for (Review review : hotelReviews.getReviews()) {
                                        globalScore += review.getGlobalScore();
                                        cleaningScore += review.getRatings().getCleaning();
                                        positionScore += review.getRatings().getPosition();
                                        servicesScore += review.getRatings().getServices();
                                        qualityScore += review.getRatings().getQuality();
                                        reviews++;
                                    }
                                }
                            }

                            if (reviews > 0) {
                                hotel.setRate(globalScore / reviews);
                                hotel.getRatings().setCleaning(cleaningScore / reviews);
                                hotel.getRatings().setPosition(positionScore / reviews);
                                hotel.getRatings().setServices(servicesScore / reviews);
                                hotel.getRatings().setQuality(qualityScore / reviews);
                            }
                        }
                    }

                    // Write the updated hotels list to the JSON file
                    FileWriter fileWriter = new FileWriter("src/main/resources/Hotels.json");
                    this.gson.toJson(hotels, fileWriter);
                    fileWriter.flush();
                    fileWriter.close();
                    fileWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Prompts the user to input a score for a specific score type and validates the
     * input.
     *
     * <p>
     * This method repeatedly prompts the user to input a score for the specified
     * score type until a valid score is entered.
     * A valid score is an integer between 1 and 5 (inclusive). If the entered score
     * is not valid, an error message is displayed.
     *
     * @param scoreType the type of score to input (e.g., "cleaning", "position",
     *                  "services", "quality")
     * @param writer    the PrintWriter to send messages to the user
     * @param reader    the BufferedReader to read the user's input
     * @return the valid score entered by the user
     * @throws IOException if an I/O error occurs when reading the user's input
     */
    private int getScore(String scoreType, PrintWriter writer, BufferedReader reader) throws IOException {
        int score = 0;
        while (score < 1 || score > 5) {
            writer.println(yellow + "Insert the " + scoreType + "score of the hotel from 1 to 5" + reset);
            try {
                score = Integer.parseInt(reader.readLine());
                if (score < 1 || score > 5) {
                    writer.println(red + "Invalid score!" + reset);
                }
            } catch (NumberFormatException e) {
                writer.println(red + "Invalid score!" + reset);
            }
        }
        return score;
    }

    /**
     * Hashes a password using the SHA-256 algorithm.
     *
     * <p>
     * This method takes a password, converts it to bytes, and then hashes it using
     * the SHA-256 algorithm.
     * The resulting hash is then converted to a hexadecimal string and returned in
     * lowercase.
     *
     * @param password the password to hash
     * @return the hashed password as a lowercase hexadecimal string
     * @throws NoSuchAlgorithmException if the SHA-256 algorithm is not available in
     *                                  the environment
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toLowerCase();
    }
}