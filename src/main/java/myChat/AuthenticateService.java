package myChat;

public interface AuthenticateService {
    void start();
    void stop();

    String getNickByLoginAndPass(String login, String pass);
}
