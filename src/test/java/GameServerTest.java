import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameServerTest {
    private static final String baseUri = "http://0.0.0.0:5555/";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void startGame() throws IOException, InterruptedException {
        assertEquals(200, postRequest("start-game", "").statusCode());
        assertEquals(400, postRequest("start-game", "").statusCode());
    }

    @Test
    void endGame() throws IOException, InterruptedException {
        assertEquals(200, postRequest("end-game", "").statusCode());
        assertEquals(400, postRequest("end-game", "").statusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "randomtext", "250", "-250", "-2147483649", "2147483648"})
    void guessBadInputs(String input) throws IOException, InterruptedException {
        postRequest("start-game", "");
        assertEquals(400, postRequest("guess", input).statusCode());
        postRequest("end-game", "");
    }
    @Test
    void validInputEndedGame() throws IOException, InterruptedException {
        postRequest("end-game", "");
        assertEquals(400, postRequest("guess", "50").statusCode());
    }

    @Test
    void guessValidInputs() throws IOException, InterruptedException {
        for (int i = 1; i <= 100; i++) {
            postRequest("start-game", "");
            assertEquals(200, postRequest("guess", String.valueOf(i)).statusCode());
            postRequest("end-game", "");
        }
    }

    private static HttpResponse<String> getRequest(String uri) throws IOException, InterruptedException {
        URI HTTP_SERVER_URI = URI.create(baseUri + uri);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(HTTP_SERVER_URI)
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> postRequest(String uri, String bodyMessage) throws IOException, InterruptedException {
        URI HTTP_SERVER_URI = URI.create(baseUri + uri);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(bodyMessage);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(bodyPublisher)
                .uri(HTTP_SERVER_URI)
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}