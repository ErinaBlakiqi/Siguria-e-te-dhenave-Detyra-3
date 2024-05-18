

import javax.crypto.Cipher;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static final int PORT = 12345;
    private static Map<String, PublicKey> clientPublicKeys = new HashMap<>();
    private static KeyPair serverKeyPair;

    public static void main(String[] args) throws Exception {
        serverKeyPair = RSAKeyPairGenerator.generateKeyPair();
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started and listening for connections...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket).start();
        }
    }

    private static class ClientHandler extends Thread {
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
                System.out.println("Public key exchange complete with " + clientSocket.getRemoteSocketAddress());

                // Handle incoming messages
                while (true) {
                    String encryptedMessage = (String) in.readObject();
                    String decryptedMessage = decryptMessage(encryptedMessage);
                    System.out.println("Received message: " + decryptedMessage);
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
