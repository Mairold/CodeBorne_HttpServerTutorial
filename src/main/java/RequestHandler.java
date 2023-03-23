import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Request;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

class RequestHandler implements HttpHandler {

    public enum NumberGuess {
        LESS, EQUAL, BIGGER
    }

    Map<Integer, Game> sessions = new HashMap<>();
    Integer sessionNumber = 0;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Response response = new Response();
        String methodAndPath = exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath();

        int sessionKey = 0;
        if (!(exchange.getRequestURI().getQuery() == null)) {
            sessionKey = Integer.parseInt(exchange.getRequestURI().getQuery().split("=")[1]);
        }
        try {
            switch (methodAndPath) {
                case "GET /status" -> response = handleStatus(sessionKey);
                case "POST /start-game" -> response = handleStart(sessionKey);
                case "POST /guess" -> response = handleGuess(requestBody, sessionKey);
                case "POST /end-game" -> response = handleStop(sessionKey);
                case "GET /stats" -> response = handleStatistics(sessionKey);
                default -> handleNotFound(exchange);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            sendResponse(exchange, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log(exchange);
    }

    private Response handleStatistics(int sessionKey) {
        String responseBody = "Games won: " + sessions.get(sessionKey).gamesWonCounter + "\n" +
                "Games lost: " + sessions.get(sessionKey).gamesEndedWithoutWinCounter + "\n" +
                "Average guesses per game: " + sessions.get(sessionKey).averageGuessesToWinGameCounter + "\n" +
                "Current game guess count: " + sessions.get(sessionKey).currentGameGuessCounter + "\n" +
                "Total guesses: " + sessions.get(sessionKey).totalGuessCounter;
        return new Response(200, responseBody);
    }

    private Response handleStatus(int sessionKey) {
        return new Response(200, "" + sessions.get(sessionKey).gameStatus);
    }

    private Response handleStart(int sessionKey) {
        if (sessionKey == 0) {
            Game game = new Game();
            sessionNumber++;
            sessions.put(sessionNumber, game);
            return new Response(200, "GameStatus > Started. Session:" + sessionNumber);
        } else if (!sessions.get(sessionKey).gameStatus) {
            sessions.get(sessionKey).newGame();
            return new Response(200, "GameStatus > Started. Session:" + sessionKey);
        } else {
            return new Response(400, "Game already running.");
        }
    }

    private Response handleGuess(String requestBody, int sessionKey) {

        if (!sessions.get(sessionKey).gameStatus) return new Response(400, "Game is not active.");
        try {
            return numberControl(Integer.parseInt(requestBody), sessionKey);
        } catch (NumberFormatException e) {
            return new Response(400, "Invalid input.");
        }
    }

    private Response handleStop(int sessionKey) {

        if (sessions.get(sessionKey).gameStatus) {
            sessions.get(sessionKey).randomNumber = null;
            sessions.get(sessionKey).gameStatus = false;
            sessions.get(sessionKey).gamesEndedWithoutWinCounter = sessions.get(sessionKey).gamesEndedWithoutWinCounter + 1;
            sessions.get(sessionKey).currentGameGuessCounter = 0;
            return new Response(200);
        } else {
            return new Response(400, "Game is not active.");
        }
    }

    void handleNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, 0);
        exchange.close();
    }

    private void log(HttpExchange exchange) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        System.out.println(LocalDateTime.now().format(formatter) + " " + exchange.getRequestMethod() + " " + exchange.getRequestURI() + " --> " + exchange.getResponseCode());
    }

    private void sendResponse(HttpExchange exchange, Response response) throws IOException {
        exchange.sendResponseHeaders(response.statusCode, response.responseBody == null ? -1 : response.responseBody.length());
        if (response.responseBody != null) {
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(response.responseBody.getBytes());
            }
        }
    }

    private Response numberControl(int userGuess, int sessionKey) {
        Response response = new Response(200);
        if (userGuess <= 0 || userGuess > 100 || !sessions.get(sessionKey).gameStatus) {
            response.responseBody = "Number out of bounds!";
            response.statusCode = 400;
        } else if (sessions.get(sessionKey).randomNumber == userGuess) {
            sessions.get(sessionKey).gameStatus = false;
            sessions.get(sessionKey).randomNumber = null;
            sessions.get(sessionKey).gamesWonCounter += 1;
            sessions.get(sessionKey).totalGuessCounter += sessions.get(sessionKey).currentGameGuessCounter;
            sessions.get(sessionKey).currentGameGuessCounter = 0;
            sessions.get(sessionKey).averageGuessesToWinGameCounter = sessions.get(sessionKey).totalGuessCounter / sessions.get(sessionKey).gamesWonCounter;
            response.responseBody = String.valueOf(NumberGuess.EQUAL);
        } else if (userGuess < sessions.get(sessionKey).randomNumber) {
            response.responseBody = String.valueOf(NumberGuess.LESS);
            sessions.get(sessionKey).currentGameGuessCounter += 1;
        } else {
            response.responseBody = String.valueOf(NumberGuess.BIGGER);
            sessions.get(sessionKey).currentGameGuessCounter += 1;
        }
        return response;
    }
}
