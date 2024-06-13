# HOTELIER

## Introduction

HOTELIER is a project developed as part of the Laboratory III module of the Networks course for the academic year 2023/24. The objective of this project is to implement a review service for hotels, inspired by the functionality of platforms like TripAdvisor.

## Features

- User registration and login
- Hotel search by name and city
- Submission of reviews by registered and logged-in users
- Periodic calculation and update of hotel rankings based on review quality, quantity, and recency
- Assignment of badges to users based on the number of reviews submitted

## Project Structure

The project consists of two main components:

- **HOTELIER Client**: Manages user interaction through a command-line interface (CLI)
- **HOTELIER Server**: Handles client requests, stores information about users and hotels, and updates rankings

## Design Choices

### 1. Client-Server Architecture

The project is designed with a client-server architecture to separate user interface management from data processing and storage.

### 2. Communication Protocols

Communication between the client and server is handled through predefined protocols to ensure efficient and secure data exchange.

### 3. Data Persistence

Data persistence is managed to ensure that user and hotel information, as well as reviews, are reliably stored and retrieved.

### 4. Ranking Algorithm

A custom algorithm is implemented to calculate hotel rankings based on review metrics.

### 5. Concurrency Management

Concurrency management is employed to handle multiple client requests efficiently.

### 6. User Experience Levels

Users are assigned experience levels based on the number of reviews they submit, categorized into five levels: Reviewer, Expert Reviewer, Contributor, Expert Contributor, and Super Contributor.

## Classes and Data Structures

### Server-side

- `Hotel.java`
- `User.java`
- `HotelReviews.java`
- `Review.java`
- `Ratings.java`
- `Chart.java`
- `HotelInChart.java`
- `ServerGroupProperties.java`

### Client-side

- `ClientGroupProperties.java`

## Instruction Manual

### Compilation and Execution

#### Server

Steps to compile and execute the server component:

`mvn -P run-server exec:java`

Steps to create server jar file and execute:

`mvn -P package-server package`
`java -jar target/HotelierServer.jar`

#### Client

Steps to compile and execute the client component:

`mvn -P run-client exec:java`

Steps to create client jar file and execute:

`mvn -P package-client package`
`java -jar target/HotelierClient.jar`

### External Dependencies

List and description of any external libraries or tools used in the project.

## How to Use

### User Registration

Instructions on how users can register and log in to the system.

### Hotel Search

Guidelines for searching hotels by name or city.

### Submitting Reviews

Steps for registered users to submit reviews for hotels.

### Viewing Rankings

Instructions on how to view hotel rankings and user badges.

## Contact

For any questions or further information, please contact the project developer Lorenzo Bandini.

---

This README provides an overview of the project, its structure, and usage instructions to help users and developers understand and interact with the system effectively.
