import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;

class RequestHandler implements HttpHandler {

    private static int randomNumber;
    private static boolean gameStatus;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Response response = new Response();
        switch (exchange.getRequestURI().getPath()) {
            case "/start":
                switch (exchange.getRequestMethod()) {
                    case "GET" -> response = handleStart(exchange);
                }
            case "/userGuess":
                switch (exchange.getRequestMethod()) {
                    case "POST" -> response = userGuess(requestBody, exchange);
                }
            case "/stop":
                switch (exchange.getRequestMethod()) {
                    case "GET" -> response = stop();
                }
        }

        exchange.sendResponseHeaders(response.statusCode, response.responseBody == null ? -1 : response.responseBody.length());
        System.out.println(LocalDateTime.now() + " " + exchange.getRequestMethod() + " " + exchange.getRequestURI() + " --> " + exchange.getResponseCode());
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(response.responseBody.getBytes());
        }
    }


    private Response stop() {
        gameStatus = false;
        return new Response(200);

    }

    public enum NumberGuess {
        LESS, EQUAL, BIGGER
    }

    public Response handleStart(HttpExchange exchange) {
        if ("GET".equals(exchange.getRequestMethod())) {
            randomNumber = new Random().nextInt(1, 101);
            gameStatus = true;
            return new Response(200, "GameStatus > Started");
        }
        return null;
    }

    public Response userGuess(String requestBody, HttpExchange exchange) {
        try {
            return numberControl(Integer.parseInt(requestBody));
        } catch (NumberFormatException e) {
            return new Response(400, "Invalid input");
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
