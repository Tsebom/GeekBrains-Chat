package server;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DataAuthService implements AuthService{
    private Server server;

    public DataAuthService(Server server) {
        this.server = server;
    }

    @Override
    public String getNickNameByLoginAndPassword(String login, String password) {
        ResultSet result;

        try {
            result = server.getStatement().executeQuery("SELECT * FROM UsersOFAuthorization");

            //checking there is the user with the login and the password
            while (result.next()) {
                if (result.getString(2).equals(login) && result.getString(3).equals(password)) {
                    return result.getString(4);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        ResultSet result;

        try {
            result = server.getStatement().executeQuery("SELECT * FROM UsersOFAuthorization");

            //checking there is the user with the login and the nickname
            while (result.next()) {
                if (result.getString(2).equals(login) || result.getString(4).equals(nickname)) {
                    return false;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            server.getAddUser().setString(1, login);
            server.getAddUser().setString(2, password);
            server.getAddUser().setString(3, nickname);
            server.getAddUser().executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }
}
