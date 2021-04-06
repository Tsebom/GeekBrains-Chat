package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextField textMassage;
    @FXML
    public TextArea conversation;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox authorizationPanel;
    @FXML
    public HBox speakPane;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean isConfirmAuth;//true if authorization is confirmed
    private String nickname;

    private Stage stage;

    public void setConfirmAuth(boolean confirmAuth) {
        this.isConfirmAuth = confirmAuth;
        //hide or reveal authorizationPanel
        authorizationPanel.setVisible(!isConfirmAuth);
        authorizationPanel.setManaged(!isConfirmAuth);
        //hide or reveal speakPanel
        speakPane.setVisible(isConfirmAuth);
        speakPane.setManaged(isConfirmAuth);

        if(!isConfirmAuth) {
            nickname = "";
        }
        setTitle(nickname);
        conversation.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) conversation.getScene().getWindow();
        });
        setConfirmAuth(false);
    }

    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out=  new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //authorization loop
                    while (true) {
                        String msg = in.readUTF();

                        if (msg.startsWith("/")) {
                            if (msg.equals("/end")) {
                                System.out.println("Disconnected");
                                break;
                            }
                            if (msg.startsWith("/auth_ok")) {
                                nickname = msg.split("\\s+")[1];
                                setConfirmAuth(true);
                                break;
                            }

                        } else {
                            conversation.appendText(msg + "\n");
                        }

                    }

                    //working loop
                    while (isConfirmAuth){
                        String msg = in.readUTF();

                        if (msg.equals("/end")) {
                            System.out.println("Disconnected");
                            break;
                        }
                        conversation.appendText(msg + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setConfirmAuth(false);
                    try {
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
     * Processing action click
     */
    @FXML
    public void click() {
        if (!textMassage.getText().equals("")) {
            try {
                out.writeUTF(textMassage.getText());
                if (textMassage.getText().equals("clear"))
                    conversation.clear();
                textMassage.clear();
                textMassage.requestFocus();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send login and password to server to run authorization
     */
    @FXML
    public void authorization(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        String msg = String.format("/auth %s %s",
                loginField.getText().trim(), passwordField.getText().trim());

        try {
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nickname) {
        Platform.runLater(() -> {
            if (nickname.equals("")) {
                stage.setTitle("GBChat");
            } else {
                stage.setTitle(nickname);
            }

        });
    }
}