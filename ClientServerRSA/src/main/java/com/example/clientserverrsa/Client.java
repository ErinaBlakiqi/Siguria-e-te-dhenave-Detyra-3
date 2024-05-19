package com.example.clientserverrsa;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.crypto.Cipher;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;

public class Client extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static KeyPair clientKeyPair;
    private PublicKey serverPublicKey;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private TextArea messageArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        clientKeyPair = RSAKeyPairGenerator.generateKeyPair();

        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        Label label = new Label("Enter message:");
        TextField messageField = new TextField();
        Button sendButton = new Button("Send");
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setPrefHeight(400);

        root.getChildren().addAll(label, messageField, sendButton, messageArea);

        sendButton.setOnAction(event -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                try {
                    String encryptedMessage = encryptMessage(message, serverPublicKey);
                    out.writeObject(encryptedMessage);
                    messageArea.appendText("Sent: " + message + "\n");
                    messageArea.appendText("Encrypted: " + encryptedMessage + "\n");
                    messageField.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Scene scene = new Scene(root, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Client");
        primaryStage.show();

        connectToServer();
    }

    private void connectToServer() throws Exception {
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        // Exchange public keys
        serverPublicKey = (PublicKey) in.readObject();
        out.writeObject(clientKeyPair.getPublic());
        messageArea.appendText("Connected to server. Public key exchange complete.\n");
    }

    private String encryptMessage(String message, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(bytes);
    }
}
