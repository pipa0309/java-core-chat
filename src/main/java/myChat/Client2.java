package myChat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client2 {
    private static final int PORT = 8189;
    private static final String HOST = "127.0.0.1";
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private Thread threadIn;

    public static void main(String[] args) {
        Client2 client = new Client2();

        client.waitConnection();

        client.inMessage();

        client.outMessage();
    }

    private void waitConnection() {
        try {
            socket = new Socket(HOST, PORT);
            System.out.println("CLIENT2 STARTED\n");

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void inMessage() {
        try {
            threadIn = new Thread(() -> {
                try {
                    while (true) {
                        System.out.println("log: read-in");
                        System.out.println("ENTER MESSAGE >>> ");
                        String inMessFromServer = in.readUTF();

                        if (ifEndIn(inMessFromServer)) break;

                        System.out.println("log: client accept message from server");
                        System.out.println(inMessFromServer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            threadIn.setDaemon(true);
            threadIn.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean ifEndIn(String inMessFromServer) {
        if ("/end".equalsIgnoreCase(inMessFromServer)) {
            System.out.println("log: input end -> start close client");
            System.out.println("CLOSE CONNECT");
            closeClient();
            return true;
        }
        return false;
    }

    private void outMessage() {
        try (Scanner sc = new Scanner(System.in)) {

            System.out.println("ENTER '/auth login password' >>> ");
            while (true) {
                System.out.println("log: write-out");
                if (sc.hasNext()) {
                    String outMessToServer = sc.nextLine();
                    if (!socket.isClosed()) {
                        out.writeUTF(outMessToServer); // 1. клиент выслал сообщение хендлеру
                        System.out.println("ENTER MESSAGE >>> ");
                        if (ifEndOut(outMessToServer)) {
                            closeClient();
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private boolean ifEndOut(String outMessToServer) throws IOException {
        if ("/end".equalsIgnoreCase(outMessToServer)) {
            System.out.println("log: out");
            out.writeUTF("/end");
            System.out.println("log: outMessage 'end'");
            return true;
        }
        return false;
    }

    private void closeClient() {
        System.out.println("log: start close-client");

        if (in != null) {
            try {
                System.out.println("log: close-in");
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (out != null) {
            try {
                System.out.println("log: close-out");
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {
            try {
                System.out.println("log: close-socket");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (threadIn.isAlive() && !threadIn.isInterrupted()) {
            System.out.println("log: close-interrupt");
            threadIn.interrupt();
        }
    }
}
