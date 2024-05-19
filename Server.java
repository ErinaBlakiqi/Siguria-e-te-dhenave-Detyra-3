import javax.crypto.Cipher; // Importimi i klasës Cipher për kriptimin dhe dekriptimin
import java.io.*; // Importimi i klasave për hyrje dhe dalje të të dhënave
import java.net.*;
import java.security.*;
import java.util.Base64; // Importimi i klasës për kodimin dhe dekodimin Base64
import java.util.HashMap; 
import java.util.Map;

public class Server {
    private static final int PORT = 12345; // Definimi i numrit të portit për serverin
    private static Map<String, PublicKey> clientPublicKeys = new HashMap<>(); // Ruajtja e çelësave publikë të klientëve
    private static KeyPair serverKeyPair; // Variabël për ruajtjen e çiftit të çelësit të serverit

    public static void main(String[] args) throws Exception {
        serverKeyPair = RSAKeyPairGenerator.generateKeyPair(); // Gjenerimi i çiftit të çelësit RSA për serverin
        ServerSocket serverSocket = new ServerSocket(PORT); // Krijimi i një ServerSocket që dëgjon në portin e specifikuar
        System.out.println("Serveri u nis dhe po pret lidhjet...");

        while (true) {
            Socket clientSocket = serverSocket.accept(); // Pranimi i lidhjeve hyrëse nga klientët
            new ClientHandler(clientSocket).start(); // Nisja e një thread-i të ri për të trajtuar klientin
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket; // Socket për komunikimin me klientin
        private ObjectOutputStream out; // Stream për dërgimin e të dhënave te klienti
        private ObjectInputStream in; // Stream për pranimin e të dhënave nga klienti

        public ClientHandler(Socket socket) {
            this.clientSocket = socket; // Inicializimi i clientSocket me socket-in e dhënë
        }

        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream()); // Inicializimi i stream-it të daljes
                in = new ObjectInputStream(clientSocket.getInputStream()); // Inicializimi i stream-it të hyrjes

                // Shkëmbimi i çelësave publikë
                out.writeObject(serverKeyPair.getPublic()); // Dërgimi i çelësit publik të serverit te klienti
                PublicKey clientPublicKey = (PublicKey) in.readObject(); // Leximi i çelësit publik të klientit nga stream-i i hyrjes
                clientPublicKeys.put(clientSocket.getRemoteSocketAddress().toString(), clientPublicKey); // Ruajtja e çelësit publik të klientit
                System.out.println("Shkëmbimi i çelësave publikë përfundoi me " + clientSocket.getRemoteSocketAddress()); // Printimi i një mesazhi që konfirmon shkëmbimin e çelësave

                // Trajtimi i mesazheve hyrëse
                while (true) {
                    String encryptedMessage = (String) in.readObject(); // Leximi i mesazhit të enkriptuar nga klienti
                    String decryptedMessage = decryptMessage(encryptedMessage); // Dekriptimi i mesazhit të marrë
                    System.out.println("Mesazhi i pranuar: " + decryptedMessage); // Printimi i mesazhit të dekriptuar
                }
            } catch (Exception e) {
                e.printStackTrace(); // Printimi i stack trace në rast të ndonjë përjashtimi
            }
        }

        private String decryptMessage(String encryptedMessage) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA"); 
            cipher.init(Cipher.DECRYPT_MODE, serverKeyPair.getPrivate()); // Inicializimi i Cipher për dekriptim me çelësin privat të serverit
            byte[] bytes = Base64.getDecoder().decode(encryptedMessage); // Dekodimi i mesazhit të koduar Base64
            return new String(cipher.doFinal(bytes)); // Dekriptimi i mesazhit dhe konvertimi i tij në string
        }
    }
}
