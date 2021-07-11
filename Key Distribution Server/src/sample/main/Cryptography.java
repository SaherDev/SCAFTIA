package sample.main;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class Cryptography {
    public final static String AES="AES";
    public final static String SHA256 = "SHA-256";
    public final static String AES_GCM = "AES/GCM/NoPadding";
    private final static int GCM_TAG_LENGTH = 16;
    private final static int IV_16_LENGTH = 16;
    private final static int SESSION_KEY_LENGTH = 32;
    private static MessageDigest mesDigest;
    private static Cipher crypto;

    public static String encryptMessage(String password, IvParameterSpec iv ,String aad,byte[] message){

        String cipherText ;

        try {
            mesDigest = MessageDigest.getInstance(SHA256);
            crypto = Cipher.getInstance(AES_GCM);
            SecretKeySpec key = new SecretKeySpec(mesDigest.digest(password.getBytes(StandardCharsets.UTF_8)), AES);
            GCMParameterSpec ivSpec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, iv.getIV());
            crypto.init(Cipher.ENCRYPT_MODE, key,ivSpec);
            crypto.updateAAD(aad.getBytes());
           byte[] cipher = crypto.doFinal(message);
            cipherText= Helper.bytesToString(cipher);
        }  catch (InvalidKeyException e) {
            System.out.println("Error: Invalid key when encrypting: " + e.getMessage());
           return  "";
        } catch (BadPaddingException e) {
            System.out.println("Error in padding when encrypting: " + e.getMessage());
            return  "";
        } catch (IllegalBlockSizeException e) {
            System.out.println("Error in block size when encrypting: " + e.getMessage());
            return  "";
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Error in IV when encrypting: " + e.getMessage());
            return  "";
        } catch (NoSuchAlgorithmException e) {
            System.out.println( e.getMessage());
            return  "";
        } catch (NoSuchPaddingException e) {
            System.out.println( e.getMessage());
            return  "";
        }

        return cipherText;
    }


    //generate random IvParameterSpec 128
    public static IvParameterSpec generate16IV(){
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] iv = new byte[IV_16_LENGTH];
        randomSecureRandom.nextBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        return ivParams;
    }

    //generate random session key 256
    public static byte[]  generateSessionkey(){
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] sessionKey = new byte[SESSION_KEY_LENGTH];
        randomSecureRandom.nextBytes(sessionKey);
        return sessionKey;
    }

    //generate nonce number
    public  static String createNonce() {
        byte[] nonce = new byte[64];
        SecureRandom rand = new SecureRandom();
        rand.nextBytes(nonce);
        return ByteBuffer.wrap(nonce).getLong()+"";
    }

}
