import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

class RequestHandler implements HttpHandler {

    public enum NumberGuess {
        LESS, EQUAL, BIGGER
    }

    private Integer randomNumber;
    private boolean gameStatus;

    private int gamesWonCounter = 0;
    private int gamesEndedWithoutWinCounter = 0;
    private int currentGameGuessCounter = 0;
    private int totalGuessCounter = 0;
    private int averageGuessesToWinGameCounter = 0;


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
        String responseBody = "Games won: " + gamesWonCounter + "\n" +
                "Games lost: " + gamesEndedWithoutWinCounter + "\n" +
                "Average guesses per game: " + averageGuessesToWinGameCounter + "\n" +
                "Current game guess count: " + currentGameGuessCounter + "\n" +
                "Total guesses: " + totalGuessCounter;
        return new Response(200, responseBody);
    }

    private Response handleStatus() {
        return gameStatus ? new Response(200, "true") : new Response(200, "false");
    }

    private Response handleStart() {
        if (!gameStatus) {
            randomNumber = new Random().nextInt(1, 101);
            gameStatus = true;
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
            gamesEndedWithoutWinCounter = gamesEndedWithoutWinCounter + 1;
            currentGameGuessCounter = 0;
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
            gamesWonCounter += 1;
            totalGuessCounter += currentGameGuessCounter;
            currentGameGuessCounter = 0;
            averageGuessesToWinGameCounter = totalGuessCounter / gamesWonCounter;
            response.responseBody = String.valueOf(NumberGuess.EQUAL);
        } else if (userGuess < randomNumber) {
            response.responseBody = String.valueOf(NumberGuess.LESS);
            currentGameGuessCounter += 1;
        } else {
            response.responseBody = String.valueOf(NumberGuess.BIGGER);
            currentGameGuessCounter += 1;
        }
        return response;
    }
}
