package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private static ServerSocket server;
    private static Socket socket;
    private static final int PORT = 8189;

    private static Scanner scanner;
    private static DataInputStream in;
    private static DataOutputStream out;


    public Server() {
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server has started");

            socket = server.accept();
            System.out.println("Client has connected");

            scanner = new Scanner(System.in);
            in = new DataInputStream(socket.getInputStream());
            out=  new DataOutputStream(socket.getOutputStream());

            // the thread of waiting message from server console
            new Thread(() -> {
                while (true) {
                    try {
                        out.writeUTF(scanner.nextLine());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // the thread of waiting message from client console
            new Thread(() -> {
                while (true) {
                    try {
                        System.out.println(in.readUTF());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
