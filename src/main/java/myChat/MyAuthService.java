package myChat;

import java.util.ArrayList;
import java.util.List;

public class MyAuthService implements AuthenticateService {
    private final List<User> users;
    private boolean flagAuth;

    public MyAuthService() {
        this.users = new ArrayList<>();

        users.add(new User("login1", "pass1", "nick1"));
        users.add(new User("login2", "pass2", "nick2"));
        users.add(new User("login3", "pass3", "nick3"));
    }

    @Override
    public void start() {
        System.out.println("Auth start");
    }

    @Override
    public void stop() {
        System.out.println("Auth done");
    }

    @Override
    public String getNickByLoginAndPass(String login, String pass) {
        for (User user : users) {
            if (user.login.equals(login) && user.pass.equals(pass)) {
                return user.nick;
            }
        }
        return null;
    }

    private static class User {
        private final String login;
        private final String pass;
        private final String nick;

        public User(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }
}
