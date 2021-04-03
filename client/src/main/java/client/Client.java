package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static Socket socket;
    private static DataInputStream in;
    private static DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private static final int PORT = 8189;

    public Client() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);

            Scanner scanner = new Scanner(System.in);
            in = new DataInputStream(socket.getInputStream());
            out=  new DataOutputStream(socket.getOutputStream());

            // the thread of waiting message from client console
            new Thread(() -> {
                while (true) {
                    try {
                        out.writeUTF(scanner.nextLine());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // the thread of waiting message from server console
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
    }
}
