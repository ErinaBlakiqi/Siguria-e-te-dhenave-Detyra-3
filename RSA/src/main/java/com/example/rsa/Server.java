package com.example.rsa;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.*;

import com.example.rsa.RSAKeyGenerater;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Server extends Application {
    private ServerSocket serverSocket;
    private KeyPair keyPair;
    private ConcurrentHashMap<Socket, PublicKey> clients;
    private TextArea messageArea;
    private TextField messageField;
    private ListView<String> clientList;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Server");

        messageArea = new TextArea();
        messageArea.setEditable(false);

        messageField = new TextField();
        messageField.setPromptText("Enter broadcast message");

        clientList = new ListView<>();

        Button startButton = new Button("Start Server");
        startButton.setOnAction(e -> startServer());

        Button sendButton = new Button("Send Broadcast");
        sendButton.setOnAction(e -> sendBroadcastMessage());

        Button viewKeysButton = new Button("View Keys");
        viewKeysButton.setOnAction(e -> viewKeys());

        VBox vbox = new VBox(10, startButton, new Label("Connected Clients:"), clientList, new Label("Messages:"), messageArea, messageField, sendButton, viewKeysButton);
        Scene scene = new Scene(vbox, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                int port = 1234; // Change this to desired port number
                serverSocket = new ServerSocket(port);
                messageArea.appendText("Server started and listening for connections on port " + port + "...\n");
                keyPair = new RSAKeyGenerater().getKeyPair();
                clients = new ConcurrentHashMap<>();

                while (true) {
                    Socket socket = serverSocket.accept();
                    messageArea.appendText("New client connected. Exchanging public keys...\n");

                    // Exchange public keys with the client
                    exchangePublicKeys(socket);

                    // Start a new thread to handle client communication
                    new ClientHandler(socket).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
                messageArea.appendText("Error starting server: " + e.getMessage() + "\n");
            }
        }).start();
    }

    private void exchangePublicKeys(Socket socket) throws Exception {
        // Send server's public key to the client
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(keyPair.getPublic());

        // Receive client's public key
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        PublicKey clientPublicKey = (PublicKey) in.readObject();

        // Store client's public key
        clients.put(socket, clientPublicKey);

        String clientAddress = socket.getInetAddress().getHostAddress();
        clientList.getItems().add(clientAddress);

        messageArea.appendText("Public key exchange complete with client " + clientAddress + ".\n");
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private DataInputStream input;
        private DataOutputStream output;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // Listen for incoming messages from the client
                while (true) {
                    String encryptedMessage = input.readUTF();
                    String decryptedMessage = decryptMessage(encryptedMessage);
                    messageArea.appendText("Received message from client: " + decryptedMessage + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String decryptMessage(String encryptedMessage) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            return new String(decryptedBytes);
        }

        public void sendMessage(String message) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, clients.get(socket));
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            output.writeUTF(Base64.getEncoder().encodeToString(encryptedBytes));
            output.flush();
        }
    }

    private void sendBroadcastMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            for (Socket socket : clients.keySet()) {
                try {
                    new ClientHandler(socket).sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            messageArea.appendText("Broadcast message sent: " + message + "\n");
            messageField.clear();
        }
    }

    private void viewKeys() {
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Server Public Key");
        alert.setHeaderText(null);
        alert.setContentText(publicKey);
        alert.showAndWait();
    }
}
