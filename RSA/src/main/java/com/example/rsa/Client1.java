package com.example.rsa;

import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.util.Base64;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Client1 extends Application {
    private Socket socket;
    private PublicKey serverPublicKey;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private DataInputStream input;
    private DataOutputStream output;
    private TextArea messageArea;
    private TextField messageField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Client");

        messageArea = new TextArea();
        messageArea.setEditable(false);

        messageField = new TextField();
        messageField.setPromptText("Enter message");

        Button connectButton = new Button("Connect");
        connectButton.setOnAction(e -> connectToServer());

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());

        Button viewKeysButton = new Button("View Keys");
        viewKeysButton.setOnAction(e -> viewKeys());

        VBox vbox = new VBox(10, connectButton, new Label("Messages:"), messageArea, messageField, sendButton, viewKeysButton);
        Scene scene = new Scene(vbox, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                String serverAddress = "localhost"; // Change this to server's address
                int serverPort = 1234; // Change this to server's port number
                socket = new Socket(serverAddress, serverPort);
                messageArea.appendText("Connected to server. Exchanging public keys...\n");
                exchangePublicKeys();

                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());

                new Thread(new ServerListener()).start();
            } catch (Exception e) {
                e.printStackTrace();
                messageArea.appendText("Error connecting to server: " + e.getMessage() + "\n");
            }
        }).start();
    }

    private void exchangePublicKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        // Receive server's public key
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        serverPublicKey = (PublicKey) in.readObject();

        // Send client's public key to the server
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(publicKey);

        messageArea.appendText("Public key exchange complete. You can now send secure messages.\n");
    }

    private void sendMessage() {
        try {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
                byte[] encryptedBytes = cipher.doFinal(message.getBytes());
                String encryptedMessage = Base64.getEncoder().encodeToString(encryptedBytes);
                messageArea.appendText("Encrypted message sent to server: " + encryptedMessage + "\n");
                output.writeUTF(encryptedMessage);
                output.flush();
                messageField.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageArea.appendText("Error sending message: " + e.getMessage() + "\n");
        }
    }

    private void viewKeys() {
        String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Client Public Key");
        alert.setHeaderText(null);
        alert.setContentText(publicKeyString);
        alert.showAndWait();
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String encryptedMessage = input.readUTF();
                    String decryptedMessage = decryptMessage(encryptedMessage);
                    messageArea.appendText("Received message from server: " + decryptedMessage + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
                messageArea.appendText("Error receiving message: " + e.getMessage() + "\n");
            }
        }

        private String decryptMessage(String encryptedMessage) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            return new String(decryptedBytes);
        }
    }
}

