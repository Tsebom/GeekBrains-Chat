package server;

public interface AuthService {
    /**
     * The method to get nickname by login and password
     * @param login - a login
     * @param password - a password
     * @return - nickname if login and password are correct otherwise null if not
     */
    String getNickNameByLoginAndPassword(String login, String password);

    /**
     * Get data for registration and execute the registration
     * @param login - a login
     * @param password - a password
     * @param nickname - a nickname
     * @return - true if registration is successful otherwise false if not
     */
    boolean registration(String login, String password, String nickname);
}
