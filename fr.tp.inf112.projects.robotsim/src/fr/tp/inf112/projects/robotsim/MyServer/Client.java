package fr.tp.inf112.projects.robotsim.MyServer;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    public static void main(String[] args) {

        try (
                Socket socket = new Socket("localhost", 8000)
        ) {

            InputStream inpStr = socket.getInputStream();
            Reader strReader = new InputStreamReader(inpStr);
            BufferedReader buffReader = new BufferedReader(strReader);

            OutputStream outStr = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outStr, true);

            writer.println("test");

            String serverResponse = buffReader.readLine();
            System.out.println("Response from server: " + serverResponse);

        } catch (UnknownHostException e) {
            System.err.println("Unknown hostname");
        } catch (IOException e) {
            System.err.println("Unable to connect to the server.");
        }
    }
}
