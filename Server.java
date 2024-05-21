import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.util.Base64;

public class Client {

    private Socket socket; // Socket-i për lidhjen me serverin
    private PublicKey serverPublicKey; // Çelësi publik i serverit
    private PrivateKey privateKey; // Çelësi privat i klientit
    private PublicKey publicKey; // Çelësi publik i klientit
    private DataInputStream input; // Input stream për të marrë të dhëna nga serveri
    private DataOutputStream output; // Output stream për të dërguar të dhëna te serveri


    public Client(String serverAddress, int serverPort) {
        try {
            // Lidhja me serverin
            socket = new Socket(serverAddress, serverPort); // Krijimi i një socket-i për lidhjen me serverin
            System.out.println("Connected to server. Exchanging public keys..."); // Njoftimi për lidhjen me serverin
            // Shkëmbimi i çelësave publike
            exchangePublicKeys();

            // Inicializimi i input/output streams
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());


            new Thread(new ServerListener()).start(); // Startimi i një thread-i për dëgjimin e mesazheve nga serveri

            // Leximi i komandave nga përdoruesi dhe procesimi i tyre
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in)); // Krijimi i një reader për input nga përdoruesi
            String command; // Deklarimi i një variabël për të ruajtur komandën e përdoruesit
            while (true) {
                System.out.print("Enter command: "); // Njoftimi për përdoruesin për të futur një komandë
                command = consoleReader.readLine(); // Leximi i komandës nga përdoruesi
                processCommand(command); // Metoda për procesimin e komandës
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metoda për shkëmbimin e çelësave publike
    private void exchangePublicKeys() throws Exception {
        RSAKeyGenerator keyGenerator = new RSAKeyGenerator(); // Krijimi i një objekti për gjenerimin e çelësave RSA
        privateKey = keyGenerator.getPrivateKey(); // Merrja e çelësit privat të klientit
        publicKey = keyGenerator.getPublicKey(); // Merrja e çelësit publik të klientit

        // Marrja e çelësit publik të serverit
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        serverPublicKey = (PublicKey) in.readObject();

        // Dërgimi i çelësit publik të klientit te serveri
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(publicKey);

        System.out.println("Public key exchange complete. You can now send secure messages."); }

    // Metoda për procesimin e komandave
    private void processCommand(String command) {
        try {
            if (command.equalsIgnoreCase("exit")) {
                socket.close(); // Mbyllja e lidhjes me serverin
                System.exit(0); // Dalja nga aplikacioni
            } else if (command.equalsIgnoreCase("viewkeys")) {
                System.out.println("Client Public Key: " + Base64.getEncoder().encodeToString(publicKey.getEncoded())); // Shfaqja e çelësit publik të klientit
            } else if (command.startsWith("send ")) {
                String message = command.substring(5); // Marrja e mesazhit nga komanda duke hequr "send "
                sendMessage(message); // Dërgimi i mesazhit te serveri
            } else if (command.equalsIgnoreCase("help")) {
                displayHelp();
            } else {
                System.out.println("Unknown command. Type 'help' to see available commands."); // Njoftimi për komandë të panjohur
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metoda për dërgimin e mesazhit të enkriptuar
    private void sendMessage(String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA"); // Inicializimi i një objekti për enkriptim/dekriptim me algoritmin RSA
        cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey); // Inicializimi  për enkriptim dhe çelësin publik të serverit
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        String encryptedMessage = Base64.getEncoder().encodeToString(encryptedBytes); // Konvertimi i mesazhit të enkriptuar në string të koduar Base64
        System.out.println("Encrypted message sent to server: " + encryptedMessage);
        output.writeUTF(encryptedMessage); // Dërgimi i mesazhit të enkriptuar në server
        output.flush(); // Dërgimi i të dhënave nga buffer-i
    }

    // Metoda për shfaqjen e ndihmës
    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("  exit       - Close the connection and exit the client.");
        System.out.println("  viewkeys   - Display the client's public key.");
        System.out.println("  send <msg> - Send an encrypted message to the server.");
        System.out.println("  help       - Display this help message.");
    }

    // Thread-i për dëgjimin e mesazheve nga serveri
    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String encryptedMessage = input.readUTF(); // Leximi i mesazhit të enkriptuar nga serveri
                    String decryptedMessage = decryptMessage(encryptedMessage); // Dekriptimi i mesazhit
                    System.out.println("Received message from server: " + decryptedMessage); // Shfaqja e mesazhit të dekriptuar nga serveri
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Metoda për dekriptimin e mesazhit
        private String decryptMessage(String encryptedMessage) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA"); // Inicializimi i një objekti për enkriptim/dekriptim me algoritmin RSA
            cipher.init(Cipher.DECRYPT_MODE, privateKey); // Inicializimi  për dekriptim dhe çelësin privat të klientit
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage)); // Dekriptimi i mesazhit
            return new String(decryptedBytes); // Kthimi i mesazhit të dekriptuar si string
        }
    }


    public static void main(String[] args) {
        String serverAddress = "localhost"; //  adresa e serverit
        int serverPort = 1234; //  numrin i portit të serverit
        new Client(serverAddress, serverPort); // Krijimi i një instance të klientit me adresën dhe portin e dhënë
    }
}
