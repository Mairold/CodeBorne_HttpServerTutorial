import com.fasterxml.jackson.databind.JsonNode;

public class Response {

    public int statusCode;
    public JsonNode responseJson;

    public Response(int statusCode) {
        this.statusCode = statusCode;
    }

    public Response(int statusCode, JsonNode responseJson) {
        this.statusCode = statusCode;
        this.responseJson = responseJson;
    }

    public Response(JsonNode responseJson) {
        this.responseJson = responseJson;
    }

    public Response() {
    }
}
