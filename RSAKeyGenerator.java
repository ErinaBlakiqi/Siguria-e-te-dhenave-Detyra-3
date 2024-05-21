import java.security.*;

public class RSAKeyGenerator {
    private KeyPair keyPair;
    /*
     * generates an RSA key pair (public and private keys).
     * returns a KeyPair containing the generated RSA public and private keys
     * throws NoSuchAlgorithmException if the RSA algorithm is not available
     */
    // create a SecureRandom instance for secure random number generation

    public RSAKeyGenerator() {
        try {
            // create a SecureRandom instance for secure random number generation
            SecureRandom secureRandom = new SecureRandom();
            // seed the SecureRandom instance with the current time in milliseconds
            secureRandom.setSeed(System.currentTimeMillis());

            // get an instance of KeyPairGenerator for RSA algorithm
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            
            // initialize the KeyPairGenerator with a key size of 2048 bits and the secure random instance
            keyGen.initialize(2048,secureRandom);
            keyPair = keyGen.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
}
