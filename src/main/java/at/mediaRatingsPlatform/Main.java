package at.mediaRatingsPlatform;

import at.mediaRatingsPlatform.dao.*;
import at.mediaRatingsPlatform.handler.*;
import at.mediaRatingsPlatform.service.*;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting MediaRatingsPlatform server...");

        // --- Initialize DAOs ---
        UserDao userDao = new UserDao();
        RatingDao ratingDao = new RatingDao();
        MediaDao mediaDao = new MediaDao();
        FavoriteDao favoriteDao = new FavoriteDao();
        ProfileDao profileDao = new ProfileDao();

        // --- Initialize Services ---
        LeaderBoardService leaderBoardService = new LeaderBoardService(userDao, ratingDao);
        AuthService authService = new AuthService(userDao);
        MediaService mediaService = new MediaService(mediaDao);
        RatingService ratingService = new RatingService(ratingDao, mediaDao);
        ProfileService profileService = new ProfileService(profileDao, userDao);
        FavoriteService favoriteService = new FavoriteService(favoriteDao, mediaDao, profileDao);
        RecommendationService recommendationService = new RecommendationService(mediaDao, ratingDao);

        // --- Create HTTP server on port 8080 ---
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // --- Register endpoints ---
        server.createContext("/api/users/register", new RegisterHandler(authService));
        server.createContext("/api/users/login", new LoginHandler(authService));
        server.createContext("/api/media", new MediaHandler(authService, mediaService));
        server.createContext("/api/ratings", new RatingHandler(authService, ratingService));
        server.createContext("/api/ratings/", new RatingHandler(authService, ratingService));
        server.createContext("/api/users/profile", new ProfileHandler(profileService));
        server.createContext("/api/favorites", new FavoriteHandler(authService, favoriteService));
        server.createContext("/api/leaderboard", new LeaderboardHandler(leaderBoardService));
        server.createContext("/api/recommendations", new RecommendationHandler(authService, recommendationService));

        // --- Thread pool for handling requests ---
        server.setExecutor(Executors.newFixedThreadPool(10));

        // --- Start server ---
        server.start();
        System.out.println("Server running on http://localhost:8080");
    }
}
