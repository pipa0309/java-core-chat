package myChat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Server {
    private static final int PORT = 8189;
    private final Map<String, ClientHandler> clients;
    private AuthenticateService authService;

    public Server() {
        this.clients = new HashMap<>();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started");
            authService = new MyAuthService();
            System.out.println("Waiting connected...");

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                authService.start();
                new ClientHandler(socket, this, authService);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error start server", e);
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public synchronized void broadcast(String mess) {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            entry.getValue().sendMessage(mess);
        }
    }

    public synchronized void broadcastByName(String mess, String name) {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            if (entry.getValue().getName().equals(name)) {
                entry.getValue().sendMessage(mess);
            }
        }
    }

    public synchronized void subscribe(String name, ClientHandler clientHandler) {
        clients.putIfAbsent(name, clientHandler);
    }

    public synchronized void unsubscribe(String name) {
        clients.remove(name);
    }

    public boolean isContainsClient(String name) {
        return clients.containsKey(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Server server = (Server) o;
        return Objects.equals(clients, server.clients) && Objects.equals(authService, server.authService);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clients, authService);
    }
}
