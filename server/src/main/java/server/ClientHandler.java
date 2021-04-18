package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;

            in = new DataInputStream(socket.getInputStream());
            out=  new DataOutputStream(socket.getOutputStream());

            //the thread for interaction with a client
            new Thread(() -> {
                try {
                    socket.setSoTimeout(120000);// set time of waiting for passive user
                    //authorization loop
                    while (true) {
                        String msg = in.readUTF();
                        //turn off a connection
                        if (msg.equals("/end")) {
                            out.writeUTF("/end");
                            throw new RuntimeException("Client decided close connection");
                        }
                        //start authorization
                        if (msg.startsWith("/auth")) {
                            String[] token = msg.split("\\s+", 3);
                            //check for empty field
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server
                                    .getAuthService()
                                    .getNickNameByLoginAndPassword(token[1], token[2]);
                            //checking authorization this user
                            if (newNick != null) {
                                login = token[1];
                                //checking connecting with this login to not open more one connection for this user
                                if (!server.isLoginUse(login)) {
                                    nickname = newNick;
                                    sendMsg("/auth_ok " + nickname);
                                    server.addClient(this);
                                    System.out.println("authorization " + nickname + " has completed: " +
                                            socket.getRemoteSocketAddress());
                                    break;
                                } else {
                                    sendMsg("This login is already connected");
                                }
                            } else {
                                sendMsg("The login or the password is not correct");
                            }
                        }

                        //registration
                        if (msg.startsWith("/reg")) {
                            String[] token = msg.split("\\s+", 4);

                            //check for empty field
                            if (token.length < 4) {
                                continue;
                            }
                            boolean checkReg = server.getAuthService().registration(token[1], token[2], token[3]);

                            if (checkReg) {
                                sendMsg("/reg_ok");
                            } else {
                                sendMsg("/reg_no");
                            }
                        }
                    }
                    socket.setSoTimeout(0);// dropping time of waiting for passive user
                    //working loop
                    while (true) {
                        String msg = in.readUTF();

                        if (msg.startsWith("/")) {
                            //turn off a connection
                            if (msg.equals("/end")) {
                                out.writeUTF(msg);
                                break;
                            }
                            //personal message
                            if (msg.startsWith("/w")) {
                                String[] token = msg.split("\\s+", 3);
                                server.personalMsg(this, token[1].trim(), token[2].trim());
                            }
                            //change nickname
                            if (msg.startsWith("/change_nickname ")) {
                                String[] token = msg.split("\\s+", 2);
                                if (token.length < 2) {
                                    continue;
                                }
                                if (server.isChangeNickname(this, token[1].trim())){
                                    nickname = token[1].trim();
                                    out.writeUTF("/change_nickname " + nickname);
                                    server.broadCastClientList();
                                }else {
                                    out.writeUTF("Change nickname is failed");
                                }
                            }
                        } else {
                            //send message for all users
                            server.broadcastMsg(this, msg);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    try {
                        out.writeUTF("/end");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        server.removeClient(this);//remove this user from list authorization
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

    /**
     * Get the nickname this user
     * @return - The nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Get the login this user
     * @return - The login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Send a message
     * @param msg - a message that is sending
     */
    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
