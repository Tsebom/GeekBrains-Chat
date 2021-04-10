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
    private List<ClientHandler> clients;//list authorization
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

    /**
     * Send message for all users that are connected
     * @param sender - An user who sends message
     * @param msg - A sent message
     */
    public void broadcastMsg(ClientHandler sender, String msg) {
        String message = String.format("%s %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    /**
     * Adding ClientHandler to list authorization
     * @param clientHandler - Added ClientHandler
     */
    public void addClient(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadCastClientList();
    }

    /**
     * Removing ClientHandler from list authorization
     * @param clientHandler - Removed ClientHandler
     */
    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadCastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    /**
     * Checking present a login in list authorization
     * @param login - the login
     * @return - true if login presents in list otherwise false if not
     */
    public boolean isLoginUse(String login) {
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Send personal message
     * @param nickname - nickname of recipient
     * @param msg - A sent message
     */
    public void personalMsg(ClientHandler sender, String nickname, String msg) {
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nickname)) {
                c.sendMsg(sender.getNickname() + " : " + msg);
                if (!c.getNickname().equals(sender.getNickname())) {
                    sender.sendMsg(sender.getNickname() + " : " + msg);
                }
                return;
            }
        }
        sender.sendMsg(nickname + " is not found.");
    }


    /**
     * Send broadcast message with list of available users
     */
    private void broadCastClientList() {
        StringBuilder stringBuilder = new StringBuilder("/clientlist");
        for (ClientHandler c : clients) {
            stringBuilder.append(" ").append(c.getNickname());
        }

        String msg = stringBuilder.toString();

        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }
}