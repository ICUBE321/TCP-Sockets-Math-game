/**
 * This GameClient acts as the player of the Math game application.
 * 
 * @author Izien Iremiren
 * @since 2020-01-10
 */

import java.io.*;
import java.net.*;

public class GameClient {

    PrintWriter out;
    BufferedReader in;
    BufferedReader stdIn;
    Socket echoSocket;
    String ip;
    int portno;

    public GameClient(String host, int port) {
        try {
            ip = host;
            portno = port;
            echoSocket = new Socket(host, port);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            System.out.println(in.readLine());
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println(in.readLine());

            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + ip);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Game closed");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
 
        String hostName = "localhost";
        int portNumber = 3500;
        GameClient socket = new GameClient(hostName, portNumber);
        socket.run();
    }
}