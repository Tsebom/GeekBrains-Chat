package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegController {
    private Controller controller;

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField nickField;
    @FXML
    private TextArea textArea;

    /**
     * Processing click to button "Registration"
     * @param actionEvent
     */
    @FXML
    public void tryToReg(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String nickname = nickField.getText().trim();

        controller.sendRegistrationDataToServer(login, password, nickname);
    }

    /**
     * Show message about status of registration
     * @param result - result of registration
     */
    public void showResult(String result) {
        if (result.equals("/reg_ok")) {
            textArea.appendText("Registration is successfully\n");
        } else {
            textArea.appendText("Registration is failed: login or nickname are already existing\n");
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}
