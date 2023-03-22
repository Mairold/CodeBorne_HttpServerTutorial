import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class GuessTheNumber {


    public static void main(String[] args) throws IOException {
        String ip = args.length > 0 ? args[0] : "0.0.0.0";
        int portNumber;
        try {
            portNumber = args.length > 1 ? Integer.parseInt(args[1]) : 6666;
        } catch (NumberFormatException e) {
            System.out.println("Entered port number format is not correct");
            return;
        }
        InetSocketAddress addr = new InetSocketAddress(ip, portNumber);
        HttpServer httpServer = HttpServer.create(addr, 0);
        RequestHandler handler = new RequestHandler();
        httpServer.createContext("/", handler);
        httpServer.start();
        System.out.println("Server started, listening at: " + addr);
    }
}