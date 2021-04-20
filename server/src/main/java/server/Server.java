package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class Server {
    private static ServerSocket server;
    private static Socket socket;

    private static final int PORT = 8189;
    private List<ClientHandler> clients;//list authorization
    private AuthService authService;

    private static Connection connection;
    private static Statement statement;

    private static PreparedStatement addUser;//prepare statement for add new user to RegBase

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        //authService = new SimpleAuthService();
        authService = new DataAuthService(this);

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server has started");
            connectDataBase();
            System.out.println("Server has connected to RegBase");
            setAllPrepareStatement();

            while (true) {
                socket = server.accept();
                System.out.println("Client has connected: " + socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                disconnectDataBase();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set connect to RegBase
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private static void connectDataBase() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:RegBase.db");
        statement = connection.createStatement();
    }

    /**
     * Close connect to RegBase
     */
    private static void disconnectDataBase(){
        try {
            statement.close();
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Set prepare statement for update RegBase
     * @throws SQLException
     */
    private void setAllPrepareStatement() throws SQLException {
        addUser = connection.prepareStatement("INSERT INTO UsersOFAuthorization (login, password, nickname)" +
                " VALUES ( ? , ? , ? )");
    }

    /**
     * Send broadcast message with list of available users
     */
    protected void broadCastClientList() {
        StringBuilder stringBuilder = new StringBuilder("/clientlist");
        for (ClientHandler c : clients) {
            stringBuilder.append(" ").append(c.getNickname());
        }

        String msg = stringBuilder.toString();

        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public static Statement getStatement() {
        return statement;
    }

    public static PreparedStatement getAddUser() {
        return addUser;
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
     * Change nickname in DataBase SQLite
     * @param clientHandler - user who make request for change nickname
     * @param nickname - new nickname
     * @return - true if nickname changed or false if not
     */
    public boolean isChangeNickname(ClientHandler clientHandler, String nickname) {
        ResultSet result;
        try {
            result = statement.executeQuery("SELECT * FROM UsersOFAuthorization " +
                    "WHERE nickname = '" + nickname + "'");

            if (!result.next()) {
                result = statement.executeQuery("SELECT id FROM UsersOFAuthorization " +
                        "WHERE nickname = '" + clientHandler.getNickname() + "'");
                result.next();
                statement.executeUpdate("UPDATE UsersOFAuthorization SET nickname = '" + nickname +
                        "' WHERE id = " + result.getInt(1));
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
}