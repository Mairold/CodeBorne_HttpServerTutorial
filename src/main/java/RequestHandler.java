import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

class RequestHandler implements HttpHandler {

    private static int randomNumber;
    private static boolean gameStatus;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Response response = new Response();
        switch (exchange.getRequestURI().getPath()) {
            case "/start" -> response = handleStart(exchange);
            case "/userGuess" -> response = userGuess(query, exchange);
            case "/stop" -> response = stop();
        }

        exchange.sendResponseHeaders(response.statusCode, response.responseJson.toString().length());

        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(response.responseJson.toString().getBytes());
        }
    }


    private Response stop() {
        gameStatus = false;
        return new Response(200);

    }

    public enum NumberGuess {
        LESS, EQUAL, GREATER
    }

    public Response handleStart(HttpExchange exchange) {
        if ("GET".equals(exchange.getRequestMethod())) {
            ObjectNode responseJson = new ObjectMapper().createObjectNode();
            responseJson.put("GameStatus", "Started");
            responseJson.put("messageENG", "******");
            randomNumber = new Random().nextInt(1, 101);
            gameStatus = true;

            return new Response(200, responseJson);
        }
        return null;
    }

    public Response userGuess(String query, HttpExchange exchange) {
        try {
            int userInput = Integer.parseInt(query.substring(4));
            if ("GET".equals(exchange.getRequestMethod())) {
                return numberControl(userInput);
            }
        } catch (NumberFormatException e) {
            return new Response(400, new ObjectMapper().createObjectNode().put("errMessage", "Invalid input"));
        }
        return null;
    }

    private Response numberControl(int userGuess) {
        ObjectNode responseJson = new ObjectMapper().createObjectNode();
        Response response = new Response(200);
        if (userGuess <= 0 || userGuess > 100 || !gameStatus) {
            responseJson.put("errMessage", "Number out of bounds!");
            response.statusCode = 400;
        } else if (randomNumber == userGuess) {
            gameStatus = false;
            responseJson.put("Answer", String.valueOf(NumberGuess.EQUAL));
        } else if (userGuess < randomNumber) {
            responseJson.put("Answer", String.valueOf(NumberGuess.GREATER));
        } else {
            responseJson.put("Answer", String.valueOf(NumberGuess.LESS));
        }
        return response;

    }
}
