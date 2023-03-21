public class Response {

    public int statusCode;
    public String responseBody;

    public Response(int statusCode) {
        this.statusCode = statusCode;
    }

    public Response(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public Response(String responseBody) {
        this.responseBody = responseBody;
    }

    public Response() {
    }
}
