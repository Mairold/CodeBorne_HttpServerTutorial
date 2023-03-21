import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class GuessTheNumber {


    public static void main(String[] args) throws IOException {
        InetSocketAddress addr = new InetSocketAddress("0.0.0.0", 5555);
        HttpServer httpServer = HttpServer.create(addr, 0);
        RequestHandler handler = new RequestHandler();
        httpServer.createContext("/", handler);
        httpServer.start();
        System.out.println("Server started, listening at: " + addr);

    }

}