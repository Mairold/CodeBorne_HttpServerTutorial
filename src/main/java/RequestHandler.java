import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;

class RequestHandler implements HttpHandler {

    private static Integer randomNumber;
    private static boolean gameStatus;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Response response = new Response();
        switch (exchange.getRequestURI().getPath()) {
            case "/start-game":
                switch (exchange.getRequestMethod()) {
                    case "GET" -> response = handleStart();
                }
            case "/guess":
                switch (exchange.getRequestMethod()) {
                    case "POST" -> response = handleGuess(requestBody);
                }
            case "/end-game":
                switch (exchange.getRequestMethod()) {
                    case "GET" -> response = handleStop();
                }
        }

        log(exchange);
        sendResponse(exchange, response);
    }

    private static void log(HttpExchange exchange) {
        System.out.println(LocalDateTime.now() + " " + exchange.getRequestMethod() + " " + exchange.getRequestURI() + " --> " + exchange.getResponseCode());
    }

    private static void sendResponse(HttpExchange exchange, Response response) throws IOException {
        exchange.sendResponseHeaders(response.statusCode, response.responseBody == null ? -1 : response.responseBody.length());
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(response.responseBody.getBytes());
        }
    }

    public enum NumberGuess {
        LESS, EQUAL, BIGGER;

    }

    private Response handleStart() {
        if (!gameStatus) {
            randomNumber = new Random().nextInt(1, 101);
            gameStatus = true;
            return new Response(200, "GameStatus > Started");
        } else {
            return new Response(400, "Game already running");
        }
    }

    private Response handleGuess(String requestBody) {
        if (!gameStatus) return new Response(400,"Game is not active");
        try {
            return numberControl(Integer.parseInt(requestBody));
        } catch (NumberFormatException e) {
            return new Response(400, "Invalid input");
        }
    }

    private Response handleStop() {
        if (gameStatus) {
            randomNumber = null;
            gameStatus = false;
            return new Response(200);
        } else {
            return new Response(400,"Game is not active");
        }
    }

    private Response numberControl(int userGuess) {
        Response response = new Response(200);
        if (userGuess <= 0 || userGuess > 100 || !gameStatus) {
            response.responseBody = "Number out of bounds!";
            response.statusCode = 400;
        } else if (randomNumber == userGuess) {
            gameStatus = false;
            response.responseBody = String.valueOf(NumberGuess.EQUAL);
        } else if (userGuess < randomNumber) {
            response.responseBody = String.valueOf(NumberGuess.BIGGER);
        } else {
            response.responseBody = String.valueOf(NumberGuess.LESS);
        }
        return response;
    }
}
