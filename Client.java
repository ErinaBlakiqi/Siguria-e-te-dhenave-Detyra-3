import javax.crypto.Cipher;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;

public class Client {
    // server details
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static KeyPair clientKeyPair; // key pair for the client

    public static void main(String[] args) throws Exception {
        // generate a key pair for the client
        clientKeyPair = RSAKeyPairGenerator.generateKeyPair();

        // establish a socket connection to the server
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

        // create output and input streams for communication
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        // exchange public keys with the server
        PublicKey serverPublicKey = (PublicKey) in.readObject(); // Read server's public key
        out.writeObject(clientKeyPair.getPublic()); // Send client's public key to the server
        System.out.println("Public key exchange complete. You can now send secure messages.");

        // BufferedReader - reads messages from the console
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // continuously read messages from the console and send them to the server
        while (true) {
            System.out.print("Enter message: ");
            String message = reader.readLine(); // read message - console
            String encryptedMessage = encryptMessage(message, serverPublicKey); // encrypt the message
            out.writeObject(encryptedMessage); // send the encrypted message to the server
            System.out.println("Encrypted message sent to server: " + encryptedMessage);
        }
    }

    /*
     * Encrypts a message using the given public key.
     * message -  the message to be encrypted
     * publicKey - the public key used for encryption
     * returns the encrypted message as a Base64 encoded string
     * throws Exception if any cryptographic operation fails
     */
    private static String encryptMessage(String message, PublicKey publicKey) throws Exception {

        // get a Cipher instance for RSA encryption
        Cipher cipher = Cipher.getInstance("RSA");

        // initialize the cipher in encryption mode with the public key
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // encrypt the message and encode it in Base64
        byte[] bytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(bytes);
    }
}
