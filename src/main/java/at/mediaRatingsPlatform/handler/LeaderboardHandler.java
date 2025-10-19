package at.mediaRatingsPlatform.handler;


import at.mediaRatingsPlatform.util.JsonUtil;
import at.mediaRatingsPlatform.service.LeaderBoardService;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class LeaderboardHandler extends AbstractHandler implements HttpHandler {
    private final LeaderBoardService service;

    public LeaderboardHandler(LeaderBoardService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        // Check if method is valid
        if (!validateMethod(ex,HttpMethodEnum.GET)) {
            return; // 405 already handled by validateMethod
        }

        handleSafely(ex, () -> {
            List<Map<String, Object>> leaderboard = service.getLeaderBoard();
            respond(ex, 200, leaderboard);
        });
    }
}