import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RSAKeyPairGenerator {
    /*
     * generates an RSA key pair (public and private keys).
     * returns a KeyPair containing the generated RSA public and private keys
     * throws NoSuchAlgorithmException if the RSA algorithm is not available
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {

        // create a SecureRandom instance for secure random number generation
        SecureRandom secureRandom = new SecureRandom();

        // seed the SecureRandom instance with the current time in milliseconds
        secureRandom.setSeed(System.currentTimeMillis());

        // get an instance of KeyPairGenerator for RSA algorithm
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

        // initialize the KeyPairGenerator with a key size of 1024 bits and the secure random instance
        keyGen.initialize(1024, secureRandom);

        // generate and return the RSA key pair
        return keyGen.generateKeyPair();
    }
}
