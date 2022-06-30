package myChat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClientHandler {
    private final Socket socket;
    private final Server server;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String name;
    private final AuthenticateService authService;
    private TimerAuth timerAuth;
    private Thread thread;
    public boolean authOk;
    private static final int LIMIT_SECONDS = 120;

    public String getName() {
        return name;
    }

    public ClientHandler(Socket socket, Server server, AuthenticateService authService) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.authService = authService;

            thread = new Thread(() -> {
                try {
                    timerAuth = new TimerAuth(LIMIT_SECONDS, this);
                    timer();

                    authOk = authorize();
                    if (authOk) {
                        System.out.println("DONE");
                        timerAuth.stopTimer();
                    } else {
                        System.out.println("NOT AUTHORIZE CLIENT");
                        thread.interrupt();
                    }

                    readMessage();
                } catch (IOException e) {
                    e.printStackTrace();

                } finally {
                    closeConnection();
                }
            });
            thread.setDaemon(true);
            thread.start();


        } catch (IOException e) {
            throw new RuntimeException("ERROR CONNECT TO CLIENT", e);
        }
    }

    private boolean authorize() throws EOFException {
        try {
            while (thread.isAlive() && !socket.isClosed()) {
                String inputMessage = in.readUTF();

                if (inputMessage.startsWith("/auth")) {
                    String[] tokens = inputMessage.split("\\s+");
                    String nick = authService.getNickByLoginAndPass(tokens[1], tokens[2]);


                    if (nick != null && !server.isContainsClient(nick)) {
                        this.name = nick;
                        server.subscribe(name, this);
                        sendMessage("/authOk " + nick);
                        server.broadcast(name + " CONNECTED TO CHAT");
                        authService.stop();
                        return true;
                    } else {
                        sendMessage("INCORRECT LOGIN/PASS");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void timer() {
        timerAuth.startTimer();
    }

    public void readMessage() throws EOFException {
        try {
            while (thread.isAlive() && !socket.isClosed()) {
                String inputStr = in.readUTF();
                if ("/end".equalsIgnoreCase(inputStr)) {
                    server.broadcastByName("/end", this.name);
                    server.unsubscribe(name);
                    String messageFromLeftChat = this.name + " LEFT CHAT";
                    server.broadcast(messageFromLeftChat);
                    throw new RuntimeException(messageFromLeftChat);
                }
                if (inputStr.startsWith("/w")) {
                    String[] split = inputStr.split("\\s+");
                    sendToClientByName(split);
                } else {
                    server.broadcast("MESSAGE FROM " + this.name + ": " + "'" + inputStr + "'");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToClientByName(String[] split) {
        List<String> messByName = Arrays.stream(split)
                .skip(2)
                .collect(Collectors.toList());

        String messToClient = messByName.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));
        String outMessageForName = "MESSAGE FROM " + this.name + ": " + "'" + messToClient + "'";
        server.broadcastByName(outMessageForName, split[1]);
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        server.unsubscribe(name);

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

