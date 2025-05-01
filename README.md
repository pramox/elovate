# Elovate - Enhanced Matchmaking and Rating System

## Overview

**Elovate** is a matchmaking and rating system application designed to enhance competitive gaming experiences. Similar to platforms like **Faceit**, Elovate uses the **Glicko rating system** to match players based on their skill levels, ensuring fair and balanced gameplay. The backend is written in **Java**, utilizing the **Glicko algorithm** for player rating, while the matchmaking algorithm ensures that players are paired in a reasonable amount of time.

The system also supports **JWT** and **OAuth** for secure authentication, and the frontend is built with **Angular** to provide a modern and responsive user interface.

## Core Features

### Glicko Rating System
- **Glicko Algorithm**: This system is used to rate players based on their performance. It is a more advanced version of Elo, incorporating rating uncertainty and volatility to provide more accurate matchmaking over time.
  
### Matchmaking Algorithm
- **Efficient Player Matching**: The matchmaking system pairs players with similar skill levels while minimizing wait times.
- Designed to handle different player counts and ensure the best possible match quality in a reasonable amount of time.

### Authentication and Security
- **JWT (JSON Web Tokens)**: Secure login with JWT for user authentication.
- **OAuth**: OAuth integration for allowing users to log in using external accounts securely.

### Modern Frontend
- **Angular**: The frontend is developed with Angular, ensuring a dynamic and responsive UI for a smooth user experience.
  
### Backend
- **Java-based Backend**: The backend is written in Java, handling matchmaking, player ratings, and user authentication.
  
### Deployment
- **Kubernates**

