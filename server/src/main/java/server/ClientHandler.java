package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;

            in = new DataInputStream(socket.getInputStream());
            out=  new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //authorization loop
                    while (true){
                        String msg = in.readUTF();

                        if (msg.equals("/end")) {
                            System.out.println("Disconnected");
                            break;
                        }

                        if (msg.startsWith("/auth")) {
                            String[] token = msg.split("\\s+");
                            String newNick = server
                                    .getAuthService()
                                    .getNickNameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                nickname = newNick;
                                sendMsg("/auth_ok " + nickname);
                                server.addClient(this);
                                System.out.println("authorization " + nickname + " has completed: " +
                                        socket.getRemoteSocketAddress());
                                break;
                            } else {
                                sendMsg("login or password is not correct");
                            }
                        }
                    }
                    //working loop
                    while (true) {
                        String msg = in.readUTF();

                        if (msg.equals("/end")) {
                            out.writeUTF(msg);
                            break;
                        }
                        //personal message
                        if (msg.startsWith("/w")) {
                            String[] personalMessage = msg.split("\\s+", 3);
                            personalMsg(personalMessage[1].trim(), personalMessage[2].trim());
                        } else {
                            server.broadcastMsg(this, msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        server.removeClient(this);
                        System.out.println("Client disconnect: " + socket.getRemoteSocketAddress());
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send personal message
     * @param nickname - nickname of recipient
     * @param msg - the message that is being sent
     */
    private void personalMsg(String nickname, String msg) {
        for (ClientHandler c :
                server.clients) {
            if (c.nickname.equals(nickname)) {
                c.sendMsg(this.nickname + " : " + msg);
                sendMsg(this.nickname + " : " + msg);
            }
        }
    }
}
