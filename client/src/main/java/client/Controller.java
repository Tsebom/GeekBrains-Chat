package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    @FXML
    public ListView<String> listUsers;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean isConfirmAuth;//true if authorization is confirmed
    private String nickname;

    private Stage stage;
    private Stage regStage;
    private RegController regController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) conversation.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                // close connection
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setConfirmAuth(false);
    }
    
    /**
     * Turn over from authorization window to chat window
     * @param confirmAuth - true if authorization is confirmed
     */
    public void setConfirmAuth(boolean confirmAuth) {
        this.isConfirmAuth = confirmAuth;
        //hide or reveal authorizationPanel
        authorizationPanel.setVisible(!isConfirmAuth);
        authorizationPanel.setManaged(!isConfirmAuth);
        //hide or reveal speakPanel and listUsers
        speakPane.setVisible(isConfirmAuth);
        speakPane.setManaged(isConfirmAuth);
        listUsers.setVisible(isConfirmAuth);
        listUsers.setManaged(isConfirmAuth);

        if(!isConfirmAuth) {
            nickname = "";
        }
        setTitle(nickname);
        conversation.clear();
    }

    /**
     * Set connection to server
     */
    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out=  new DataOutputStream(socket.getOutputStream());

            //the thread for working with server
            new Thread(() -> {
                try {
                    //authorization loop
                    while (true) {
                        String msg = in.readUTF();

                        //service message
                        if (msg.startsWith("/")) {
                            //turn off a connection
                            if (msg.equals("/end")) {
                                break;
                            }
                            //confirm authorization
                            if (msg.startsWith("/auth_ok")) {
                                nickname = msg.split("\\s+")[1];
                                setConfirmAuth(true);
                                //create or reade history file and output history
                                HistoryFile.createHistoryFile(loginField.getText().trim());
                                conversation.appendText(HistoryFile.readHistory());
                                break;
                            }
                            //confirm registration
                            if (msg.startsWith("/reg_ok")) {
                                regController.showResult("/reg_ok");
                            }
                            if (msg.startsWith("/reg_no")) {
                                regController.showResult("/reg_no");
                            }
                        } else {
                            conversation.appendText(msg + "\n");
                        }
                    }

                    //working loop
                    while (isConfirmAuth) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/")) {
                            //turn off a connection
                            if (msg.equals("/end")) {
                                break;
                            }
                            //overload the list of users that were being authorization
                            if (msg.startsWith("/clientlist")) {
                                String[] token = msg.split("\\s+");
                                Platform.runLater(() -> {
                                    listUsers.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        listUsers.getItems().add(token[i]);
                                    }
                                });
                            }
                            //change nickname
                            if (msg.startsWith("/change_nickname ")) {
                                String[] token = msg.split("\\s+", 2);
                                if (token.length < 2) {
                                    continue;
                                }
                                nickname = token[1].trim();
                                setTitle(nickname);
                            }
                        } else {
                            conversation.appendText(msg + "\n");
                            //write history
                            HistoryFile.writeHistory(msg + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Disconnected");
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

    /**
     * Set title for window of chat
     * @param nickname - nickname of user
     */
    private void setTitle(String nickname) {
        Platform.runLater(() -> {
            if (nickname.equals("")) {
                stage.setTitle("GBChat");
            } else {
                stage.setTitle(nickname);
            }
        });
    }

    /**
     * Create blank for personal message
     * @param mouseEvent
     */
    public void clickUser(MouseEvent mouseEvent) {
        String recipient = listUsers.getSelectionModel().getSelectedItem();
        textMassage.setText("/w " + recipient + " ");
    }

    /**
     * Initialization window of registration
     */
    private void initRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("GBChat registration");
            regStage.setScene(new Scene(root, 400, 320));

            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);

            regController = fxmlLoader.getController();
            regController.setController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start to registration
     * @param actionEvent
     */
    public void registration(ActionEvent actionEvent) {
        if (regStage == null) {
            initRegWindow();
        }
        regStage.show();
    }

    /**
     * Send data registration to server
     * @param login
     * @param password
     * @param nickname
     */
    public void sendRegistrationDataToServer(String login, String password, String nickname) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/reg %s %s %s", login, password, nickname);
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}