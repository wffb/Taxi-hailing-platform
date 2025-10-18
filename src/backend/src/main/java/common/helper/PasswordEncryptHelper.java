package common.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordEncryptHelper {

    // generate a random salt value
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }


    // hash the password with the salt value
    public static String hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hashedPassword = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hashedPassword);
    }

    // encrypt the password with the salt value
    public static String encryptPassword(String password) throws NoSuchAlgorithmException {
        byte[] salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + hashedPassword;
    }

    // verify the password with the stored salt value
    public static boolean verifyPassword(String password, String storedPassword) throws NoSuchAlgorithmException {

        String[] parts = storedPassword.split(":");
        if(parts.length!= 2)
            return false;

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        String hashedPassword = hashPassword(password, salt);
        return hashedPassword.equals(parts[1]);
    }

}
