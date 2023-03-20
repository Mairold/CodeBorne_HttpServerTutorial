import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

class RequestHandler implements HttpHandler {

    private static int randomNumber;
    private static boolean gameStatus;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
          switch (exchange.getRequestURI().getPath()) {
            case "/start" -> {
                try {
                    handleStart(exchange);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            case "/userGuess" -> {
                try {
                    userGuess(exchange);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            case "/stop" -> {
                try {
                    stop(exchange);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void stop(HttpExchange exchange) throws JSONException, IOException {
        gameStatus = false;
        exchange.sendResponseHeaders(200,-1);
    }

    public enum NumberGuess {LESS, EQUAL, GREATER}

    public void handleStart(HttpExchange exchange) throws IOException, JSONException {
        if ("GET".equals(exchange.getRequestMethod())) {
            System.out.println(exchange.getRequestURI());
            JSONObject response = new JSONObject();
            response.put("GameStatus", "Started");
            response.put("messageENG", "******");
            randomNumber = new Random().nextInt(1, 101);
            gameStatus = true;
            System.out.println(randomNumber);
            exchange.sendResponseHeaders(200, response.toString().length());

            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(response.toString().getBytes());
            }
        }
    }

    public void userGuess(HttpExchange exchange) throws IOException, JSONException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String queryParam = exchange.getRequestURI().getQuery().substring(4);

            JSONObject response = numberControl(queryParam, exchange);
            exchange.sendResponseHeaders(200, response.toString().length());

            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(response.toString().getBytes());
            }
        }
    }

    private JSONObject numberControl(String queryParam, HttpExchange exchange) throws JSONException, IOException {
        JSONObject response = new JSONObject();
        try {
            int userGuess = Integer.parseInt(queryParam);
            if (userGuess <= 0 || userGuess > 100 || !gameStatus) {
                exchange.sendResponseHeaders(400, 0);
            } else if (randomNumber == userGuess) {
                gameStatus = false;
                return response.put("Answer", NumberGuess.EQUAL);
            } else if (userGuess < randomNumber) {
                return response.put("Answer", NumberGuess.GREATER);
            } else {
                return response.put("Answer", NumberGuess.LESS);
            }

        } catch (NumberFormatException numberFormatException) {
            exchange.sendResponseHeaders(400, 0);
        }
        return response;
    }
}
