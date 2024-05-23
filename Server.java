import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private ServerSocket serverSocket; // ServerSocket për pranimin e lidhjeve
    private KeyPair keyPair; // Çifti i çelësave RSA për serverin
    private ConcurrentHashMap<Socket, PublicKey> clients; // Map për ruajtjen e çelësave publike të klientëve

    // Konstruktori për klasën Server, inicializon ServerSocket-in, çelësat dhe mapën e klientëve
    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port); // Fillon serverin në portin e specifikuar
            System.out.println("Server started and listening for connections...");
            RSAKeyGenerator keyGenerator = new RSAKeyGenerator(); // Krijon një instancë të RSAKeyGenerator
            keyPair = new KeyPair(keyGenerator.getPublicKey(), keyGenerator.getPrivateKey()); // Merr çelësat nga RSAKeyGenerator
            clients = new ConcurrentHashMap<>(); // Inicializon mapën për ruajtjen e çelësave publike të klientëve

            new Thread(new ConsoleInputHandler()).start(); // Fillon një thread për të trajtuar inputin nga console-a

            while (true) {
                Socket socket = serverSocket.accept(); // Pranon një lidhje të re nga klienti
                System.out.println("New client connected. Exchanging public keys...");

                // Bën shkëmbimin e çelësave publike me klientin
                exchangePublicKeys(socket);

                // Fillon një thread të ri për të trajtuar komunikimin me klientin
                new ClientHandler(socket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Gjeneron çiftin e çelësave RSA
    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    // Bën shkëmbimin e çelësave publike me klientin
    private void exchangePublicKeys(Socket socket) throws Exception {
        // Dërgon çelësin publik të serverit te klienti
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(keyPair.getPublic());

        // Merr çelësin publik të klientit
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        PublicKey clientPublicKey = (PublicKey) in.readObject();

        // Ruajt çelësin publik të klientit
        clients.put(socket, clientPublicKey);

        System.out.println("Public key exchange complete. Ready to receive encrypted messages from clients.");
    }

    // Klasë e brendshme për trajtimin e komunikimit me klientin
    private class ClientHandler extends Thread {
        private Socket socket;
        private DataInputStream input;
        private DataOutputStream output;

        // Konstruktori inicializon input dhe output streams
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
                // Dëgjon për mesazhe të ardhura nga klienti
                while (true) {
                    String encryptedMessage = input.readUTF(); // Lexon mesazhin e enkriptuar nga klienti
                    String decryptedMessage = decryptMessage(encryptedMessage); // Dekripton mesazhin
                    System.out.println("Received message from client: " + decryptedMessage); // Printon mesazhin e dekriptuar
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Dekripton një mesazh të enkriptuar duke përdorur çelësin privat të serverit
        private String decryptMessage(String encryptedMessage) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            return new String(decryptedBytes);
        }

        // Enkripton dhe dërgon një mesazh te klienti
        public void sendMessage(String message) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, clients.get(socket)); // Enkripton mesazhin me çelësin publik të klientit
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            output.writeUTF(Base64.getEncoder().encodeToString(encryptedBytes)); // Dërgon mesazhin e enkriptuar
            output.flush();
        }
    }

    // Dërgon një mesazh të transmetuar në të gjithë klientët e lidhur
    public void sendBroadcastMessage(String message) {
        for (Socket socket : clients.keySet()) {
            try {
                new ClientHandler(socket).sendMessage(message); // Dërgon mesazhin duke përdorur ClientHandler
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




                    // Klasë e brendshme për trajtimin e inputit nga console-a
    private class ConsoleInputHandler implements Runnable {
        @Override
        public void run() {
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String command;
            try {
                while (true) {
                    System.out.print("Enter command: ");
                    command = consoleReader.readLine(); // Lexon komandën nga console-a
                    processCommand(command); // Proceson komandën e dhënë
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Proceson komandat e dhëna në console
        private void processCommand(String command) {
            try {
                if (command.equalsIgnoreCase("exit")) {
                    System.exit(0); // Mbyll serverin
                } else if (command.equalsIgnoreCase("viewkeys")) {
                    System.out.println("Server Public Key: " + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())); // Shfaq çelësin publik të serverit
                } else if (command.startsWith("send ")) {

                    String message = command.substring(5); // Ekstrakton mesazhin nga komanda
                    sendBroadcastMessage(message); // Dërgon mesazhin në të gjithë klientët
                } else if (command.equalsIgnoreCase("help")) {
                    displayHelp(); // Shfaq mesazhin ndihmës
                } else {
                    System.out.println("Unknown command. Type 'help' to see available commands.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Shfaq komandat e disponueshme
        private void displayHelp() {
            System.out.println("Available commands:");
            System.out.println("  exit       - Close the server.");
            System.out.println("  viewkeys   - Display the server's public key.");
            System.out.println("  send <msg> - Send an encrypted broadcast message to all clients.");
            System.out.println("  help       - Display this help message.");
        }
    }

    public static void main(String[] args) {
        int port = 1234; // Ndrysho këtë në numrin e portit të dëshiruar
        new Server(port); // Fillon serverin
    }
}
