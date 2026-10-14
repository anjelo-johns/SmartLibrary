import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Standalone CLI tool to generate a PBKDF2 hash compatible with SmartLibrary.
 * Can be run directly:
 *   java HashPassword.java <password>
 */
public class HashPassword {
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH_BITS = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static void main(String[] args) {
        if (args.length < 1 || args[0].trim().isEmpty()) {
            System.err.println("Usage: java HashPassword.java <plain-password>");
            System.exit(1);
        }
        String plain = args[0];
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hashBytes = pbkdf2(plain.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        String result = ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hashBytes);

        System.out.println("==================================================");
        System.out.println("Password : " + plain);
        System.out.println("PBKDF2   : " + result);
        System.out.println("==================================================");
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }
}
