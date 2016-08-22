package it.polimi.se2.meteocal.control;

import it.polimi.se2.meteocal.entity.User;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Control class that takes care of the user's password encryption
 */
public class PasswordEncrypter
{
    /**
    * Method that receives a password and returns it's encryption
    * @param password: the password in clear text
    * @return the encrypted password
    */
    public static String encryptPassword(String password)
    {
        String encryptedPassword = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            BigInteger bigInt = new BigInteger(1, hash);
            encryptedPassword = bigInt.toString(16);
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException ex)
        {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
        return encryptedPassword;
    }
}

