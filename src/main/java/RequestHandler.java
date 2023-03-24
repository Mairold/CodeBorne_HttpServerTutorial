import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class RequestHandler implements HttpHandler {

    public enum NumberGuess {
        LESS, EQUAL, BIGGER
    }

    public enum GameResult {
        WIN, LOSS
    }

    private Integer randomNumber;
    private boolean gameStatus;
    private int gameCounter = 0;
    private int guessCounter = 0;
    private Map<Integer, OneGame> gameResults = new HashMap<>();



    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Response response = new Response();
        String methodAndPath = exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath();
        try {
            switch (methodAndPath) {
                case "GET /status" -> response = handleStatus();
                case "POST /start-game" -> response = handleStart();
                case "POST /guess" -> response = handleGuess(requestBody);
                case "POST /end-game" -> response = handleStop();
                case "GET /stats" -> response = handleStatistics();
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

    private Response handleStatistics() {
        String responseBody =  "JSON";
        return new Response(200, responseBody);
    }

    private Response handleStatus() {
        return gameStatus ? new Response(200, "true") : new Response(200, "false");
    }

    private Response handleStart() {
        if (!gameStatus) {
            randomNumber = new Random().nextInt(1, 101);
            gameStatus = true;
            gameCounter++;
            return new Response(200, "GameStatus > Started.");
        } else {
            return new Response(400, "Game already running.");
        }
    }

    private Response handleGuess(String requestBody) {
        if (!gameStatus) return new Response(400, "Game is not active.");
        try {
            return numberControl(Integer.parseInt(requestBody));
        } catch (NumberFormatException e) {
            return new Response(400, "Invalid input.");
        }
    }

    private Response handleStop() {

        if (gameStatus) {
            randomNumber = null;
            gameStatus = false;
            gameResults.put(gameCounter, new OneGame(guessCounter, GameResult.LOSS));
            guessCounter = 0;
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

    private Response numberControl(int userGuess) {
        Response response = new Response(200);
        if (userGuess <= 0 || userGuess > 100 || !gameStatus) {
            response.responseBody = "Number out of bounds!";
            response.statusCode = 400;
        } else if (randomNumber == userGuess) {
            gameStatus = false;
            randomNumber = null;
            guessCounter++;
            gameResults.put(gameCounter, new OneGame(guessCounter, GameResult.WIN));
            System.out.println(gameResults.toString());
            guessCounter = 0;
            response.responseBody = String.valueOf(NumberGuess.EQUAL);
        } else if (userGuess < randomNumber) {
            guessCounter++;
            response.responseBody = String.valueOf(NumberGuess.LESS);
        } else {
            guessCounter++;
            response.responseBody = String.valueOf(NumberGuess.BIGGER);
        }
        return response;
    }
}
