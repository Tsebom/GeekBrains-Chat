package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class Server {
    private static ServerSocket server;
    private static Socket socket;

    private static final int PORT = 8189;
    protected List<ClientHandler> clients;
    private AuthService authService;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();
        
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server has started");

            while (true) {
                socket = server.accept();
                System.out.println("Client has connected: " + socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void broadcastMsg(ClientHandler sender, String msg) {
        String message = String.format("%s %s", sender.getNickname(), msg);
        for (ClientHandler c :
                clients) {
            c.sendMsg(message);
        }
    }

    public void addClient(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }
}