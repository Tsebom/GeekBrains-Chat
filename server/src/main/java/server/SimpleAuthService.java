package server;

import java.util.ArrayList;
import java.util.List;

/**
 * For storage of profiles of users
 */
public class SimpleAuthService implements AuthService{
    private class UserData{
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();

        users.add(new UserData("qwe", "qwe", "qwe"));
        users.add(new UserData("asd", "asd", "asd"));
        users.add(new UserData("zxc", "zxc", "zxc"));

        for (int i = 0; i < 10; i++) {
            users.add(new UserData("login" + i, "password" + i, "nickname" + i));
        }
    }

    @Override
    public String getNickNameByLoginAndPassword(String login, String password) {
        for (UserData u :
                users) {
            if (u.login.equals(login) && u.password.equals(password)) {
                return u.nickname;
            }
        }
        return null;
    }
}
