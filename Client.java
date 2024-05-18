
import javax.crypto.Cipher;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static KeyPair clientKeyPair;

    public static void main(String[] args) throws Exception {
        clientKeyPair = RSAKeyPairGenerator.generateKeyPair();
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        // Exchange public keys
        PublicKey serverPublicKey = (PublicKey) in.readObject();
        out.writeObject(clientKeyPair.getPublic());
        System.out.println("Public key exchange complete. You can now send secure messages.");

        // Send messages
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("Enter message: ");
            String message = reader.readLine();
            String encryptedMessage = encryptMessage(message, serverPublicKey);
            out.writeObject(encryptedMessage);
            System.out.println("Encrypted message sent to server: " + encryptedMessage);
        }
    }

    private static String encryptMessage(String message, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(bytes);
    }
}
