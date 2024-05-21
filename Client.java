import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.util.Base64;

public class Client {
    private Socket socket;
    private PublicKey serverPublicKey;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private DataInputStream input;
    private DataOutputStream output;

    public Client(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to server. Exchanging public keys...");
            exchangePublicKeys();

            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            new Thread(new ServerListener()).start();

            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String command;
            while (true) {
                System.out.print("Enter command: ");
                command = consoleReader.readLine();
                processCommand(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        System.out.println("Public key exchange complete. You can now send secure messages.");
    }

    private void processCommand(String command) {
        try {
            if (command.equalsIgnoreCase("exit")) {
                socket.close();
                System.exit(0);
            } else if (command.equalsIgnoreCase("viewkeys")) {
                System.out.println("Client Public Key: " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            } else if (command.startsWith("send ")) {
                String message = command.substring(5);
                sendMessage(message);
            } else if (command.equalsIgnoreCase("help")) {
                displayHelp();
            } else {
                System.out.println("Unknown command. Type 'help' to see available commands.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        String encryptedMessage = Base64.getEncoder().encodeToString(encryptedBytes);
        System.out.println("Encrypted message sent to server: " + encryptedMessage);
        output.writeUTF(encryptedMessage);
        output.flush();
    }

    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("  exit       - Close the connection and exit the client.");
        System.out.println("  viewkeys   - Display the client's public key.");
        System.out.println("  send <msg> - Send an encrypted message to the server.");
        System.out.println("  help       - Display this help message.");
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String encryptedMessage = input.readUTF();
                    String decryptedMessage = decryptMessage(encryptedMessage);
                    System.out.println("Received message from server: " + decryptedMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String decryptMessage(String encryptedMessage) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            return new String(decryptedBytes);
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost"; // Change this to server's address
        int serverPort = 1234; // Change this to server's port number
        new Client(serverAddress, serverPort);
    }
}
