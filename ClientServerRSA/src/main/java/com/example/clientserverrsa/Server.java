package com.example.clientserverrsa;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.crypto.Cipher;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Server extends Application {
    private static final int PORT = 12345;
    private static Map<String, PublicKey> clientPublicKeys = new HashMap<>();
    private static KeyPair serverKeyPair;
    private TextArea messageArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        serverKeyPair = RSAKeyPairGenerator.generateKeyPair();

        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setPrefHeight(500);

        root.getChildren().add(messageArea);

        Scene scene = new Scene(root, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Server");
        primaryStage.show();

        new Thread(() -> {
            try {
                startServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startServer() throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        messageArea.appendText("Server started and listening for connections...\n");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket).start();
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                // Exchange public keys
                out.writeObject(serverKeyPair.getPublic());
                PublicKey clientPublicKey = (PublicKey) in.readObject();
                clientPublicKeys.put(clientSocket.getRemoteSocketAddress().toString(), clientPublicKey);
                messageArea.appendText("Public key exchange complete with " + clientSocket.getRemoteSocketAddress() + "\n");

                // Handle incoming messages
                while (true) {
                    String encryptedMessage = (String) in.readObject();
                    String decryptedMessage = decryptMessage(encryptedMessage);
                    messageArea.appendText("Received from " + clientSocket.getRemoteSocketAddress() + ": " + decryptedMessage + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String decryptMessage(String encryptedMessage) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, serverKeyPair.getPrivate());
            byte[] bytes = Base64.getDecoder().decode(encryptedMessage);
            return new String(cipher.doFinal(bytes));
        }
    }
}

