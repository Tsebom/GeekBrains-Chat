package server;

import org.sqlite.SQLiteException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Realization of interface AuthService for DataBase SQLite
 */
public class DataAuthService implements AuthService{
    private Server server;

    public DataAuthService(Server server) {
        this.server = server;
    }

    @Override
    public String getNickNameByLoginAndPassword(String login, String password) {
        ResultSet result;

        try {
            result = server.getStatement().executeQuery("SELECT nickname FROM UsersOFAuthorization WHERE " +
                    "login = '" + login + "' AND password = '" + password + "'");
            if (result.next()) {
                return result.getString(1);
            }
        }catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        ResultSet result;

        try {
            result = server.getStatement().executeQuery("SELECT * FROM UsersOFAuthorization WHERE " +
                    "login = '" + login + "' AND nickname = '" + nickname + "'");
            if (!result.next()) {
                try {
                    server.getAddUser().setString(1, login);
                    server.getAddUser().setString(2, password);
                    server.getAddUser().setString(3, nickname);
                    server.getAddUser().executeUpdate();
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }catch (SQLException throwables) {

            throwables.printStackTrace();
        }
        return false;
    }
}
