package server;

@FunctionalInterface
public interface AuthService {
    /**
     * The method to get nickname by login and password
     * @param login - a login
     * @param password - a password
     * @return - nickname if login and password are correct otherwise null if not
     */
    String getNickNameByLoginAndPassword(String login, String password);
}
