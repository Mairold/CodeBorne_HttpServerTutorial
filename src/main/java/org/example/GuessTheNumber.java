package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Random;

public class GuessTheNumber {

    private static int randomNumber;

    public static void main(String[] args) throws IOException {
        InetSocketAddress addr = new InetSocketAddress("localhost", 5555);
        HttpServer httpServer = HttpServer.create(addr, 0);
        httpServer.createContext("/start", new RequestHandler());
        httpServer.createContext("/userGuess", new RequestHandler());
        httpServer.start();
        System.out.println("Server started, listening at: " + addr);
    }

    private static class RequestHandler implements HttpHandler {


        @Override
        public void handle(HttpExchange exchange) throws IOException {
            switch (exchange.getRequestURI().getPath()) {
                case "/start" -> handleStart(exchange);
                case "/userGuess" -> userGuess(exchange);
            }
        }

        public void handleStart(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                System.out.println(exchange.getRequestURI());
                String response = "Hello! I've generated a random number between 1 and 100. Please guess what?";
                randomNumber = new Random().nextInt(1, 101);
                System.out.println(randomNumber);
                exchange.sendResponseHeaders(200, response.length());

                try (OutputStream responseBody = exchange.getResponseBody()) {
                    responseBody.write(response.getBytes());
                }
            }
        }

        public void userGuess(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String queryParam = exchange.getRequestURI().getQuery().substring(4);
                String response = numberControl(queryParam);
                exchange.sendResponseHeaders(200, response.length());

                try (OutputStream responseBody = exchange.getResponseBody()) {
                    responseBody.write(response.getBytes());
                }
            }
        }

        private String numberControl(String queryParam) {
            if (queryParam.equals("exit")) {
                randomNumber = 0;
                return "Thank you for playing, come again!";
            }
            try {
                int userGuess = Integer.parseInt(queryParam);
                if (userGuess <= 0 || userGuess > 100) {
                    return "The number must be between 1 and 100";
                } else if (randomNumber == userGuess) {
                    String result = "Wow! You are a genius!" + " " + randomNumber + " is correct answer! Please START again";
                    randomNumber = 0;
                    return result;
                } else if (userGuess < randomNumber) {
                    return "The number is bigger than you guessed. Please try again!";
                } else {
                    return "The number is smaller than you guessed. Please try again!";
                }

            } catch (NumberFormatException numberFormatException) {
                return "If you would like to continue the game please provide number between 1 to 100. If you would like to finish the game, please write 'exit'.";
            }
        }
    }
}