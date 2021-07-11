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
    public final static String AES_CTR = "AES/CTR/NoPadding";
    public final static String AES_GCM = "AES/GCM/NoPadding";
    public final static String HMACSHA256 = "HmacSHA256";
    private final static int GCM_TAG_LENGTH = 16;
    private final static int IV_16_LENGTH = 16;
    private final static int IV_12_LENGTH = 12;
    private static MessageDigest mesDigest;
    private final static int SESSION_KEY_LENGTH = 32;
    private static Cipher crypto;
    private static Mac hmac;

    public static int getsessionKeyLenght(){
        return SESSION_KEY_LENGTH;
    }
    public static byte[] encryptMessage(String shredPassword, IvParameterSpec iv ,String message){

        byte[] ciphertext = new byte[0];
        try {
            mesDigest = MessageDigest.getInstance(SHA256);
            crypto = Cipher.getInstance(AES_CTR);
            SecretKeySpec key = new SecretKeySpec(mesDigest.digest(shredPassword.getBytes(StandardCharsets.UTF_8)), AES);
            crypto.init(Cipher.ENCRYPT_MODE, key,iv);
            ciphertext = crypto.doFinal(message.getBytes(StandardCharsets.UTF_8));

        } catch (InvalidKeyException e) {
            System.out.println("Error: Invalid key when encrypting: " + e.getMessage());
            return new byte[0];
        } catch (BadPaddingException e) {
            System.out.println("Error in padding when encrypting: " + e.getMessage());
            return new byte[0];
        } catch (IllegalBlockSizeException e) {
            System.out.println("Error in block size when encrypting: " + e.getMessage());
            return new byte[0];
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Error in IV when encrypting: " + e.getMessage());
            return new byte[0];
        } catch (NoSuchAlgorithmException e) {
            System.out.println( e.getMessage());
            return new byte[0];
        } catch (NoSuchPaddingException e) {
            System.out.println( e.getMessage());
            return new byte[0];
        }
        return ciphertext;
    }


    public static byte[]  decryptMessage( String shredPassword, String ivString,String message){

        Cipher crypto;
        byte[]  plaintext =new byte[0];
        try {

            mesDigest = MessageDigest.getInstance(SHA256);
            crypto = Cipher.getInstance(AES_CTR);
            SecretKeySpec key = new SecretKeySpec(mesDigest.digest(shredPassword.getBytes(StandardCharsets.UTF_8)), AES);
            crypto.init(Cipher.ENCRYPT_MODE, key,getIvParameterSpec(ivString));
            plaintext = crypto.doFinal(Helper.stringToBytes(message));
           
         } catch (InvalidKeyException e) {
            System.out.println("Error: Invalid key when deccrypting: " + e.getMessage());
            return new byte[0];
        } catch (BadPaddingException e) {
            System.out.println("Error in padding when deccrypting: " + e.getMessage());
            return new byte[0];
        } catch (IllegalBlockSizeException e) {
            System.out.println("Error in block size when deccrypting: " + e.getMessage());
            return new byte[0];
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Error in IV when deccrypting: " + e.getMessage());
            return new byte[0];
        } catch (NoSuchAlgorithmException e) {
            System.out.println( e.getMessage());
            return new byte[0];
        } catch (NoSuchPaddingException e) {
            System.out.println( e.getMessage());
            return new byte[0];
        }
        return plaintext;
    }

    public static String decryptMessageString( String shredPassword, String ivString,String message){
            byte[]  plaintext =decryptMessage(shredPassword,ivString,message);
            return   new String(plaintext, StandardCharsets.UTF_8);
    }

    public static String encryptMessageGCM(String password, IvParameterSpec iv ,String aad,String message){

        String cipherText ;

        try {
            mesDigest = MessageDigest.getInstance(SHA256);
            crypto = Cipher.getInstance(AES_GCM);
            SecretKeySpec key = new SecretKeySpec(mesDigest.digest(password.getBytes(StandardCharsets.UTF_8)), AES);
            GCMParameterSpec ivSpec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, iv.getIV());
            crypto.init(Cipher.ENCRYPT_MODE, key,ivSpec);
            crypto.updateAAD(aad.getBytes());
            byte[] cipher = crypto.doFinal(message.getBytes(StandardCharsets.UTF_8));
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

    public static byte[] decrypttMessageGCM(String password, String ivString ,String aad,byte[] message){

        byte[] plain ;

        try {
            mesDigest = MessageDigest.getInstance(SHA256);
            crypto = Cipher.getInstance(AES_GCM);
            SecretKeySpec key = new SecretKeySpec(mesDigest.digest(password.getBytes(StandardCharsets.UTF_8)), AES);
            GCMParameterSpec ivSpec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, getIvParameterSpec(ivString).getIV());
            crypto.init(Cipher.DECRYPT_MODE, key,ivSpec);
            crypto.updateAAD(aad.getBytes());
            plain = crypto.doFinal(message);

        }  catch (InvalidKeyException e) {
            System.out.println("Error: Invalid key when encrypting: " + e.getMessage());
            return new byte[0];
        } catch (BadPaddingException e) {
            System.out.println("Error in padding when encrypting: " + e.getMessage());
            return new byte[0];
        } catch (IllegalBlockSizeException e) {
            System.out.println("Error in block size when encrypting: " + e.getMessage());
            return new byte[0];
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Error in IV when encrypting: " + e.getMessage());
            return new byte[0];
        } catch (NoSuchAlgorithmException e) {
            System.out.println( e.getMessage());
            return new byte[0];
        } catch (NoSuchPaddingException e) {
            System.out.println( e.getMessage());
            return new byte[0];
        }

        return plain;
    }

    public static String decryptMessageGCMString( String password, String ivString ,String aad,byte[] message){
        byte[]  plaintext =decrypttMessageGCM(password,ivString,aad,message);
        return   new String(plaintext, StandardCharsets.UTF_8);
    }

    public static byte[] encrypFile(String sessionKey, IvParameterSpec iv ,String aad,byte[] file){

        byte[] ciphertFile = new byte[0];

        try {
            mesDigest = MessageDigest.getInstance(SHA256);
            crypto = Cipher.getInstance(AES_GCM);
            SecretKeySpec key = new SecretKeySpec(mesDigest.digest(sessionKey.getBytes(StandardCharsets.UTF_8)), AES);
            GCMParameterSpec ivSpec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, iv.getIV());
            crypto.init(Cipher.ENCRYPT_MODE, key,ivSpec);
            crypto.updateAAD(aad.getBytes());
            ciphertFile = crypto.doFinal(file);
        }  catch (InvalidKeyException e) {
            System.out.println("Error: Invalid key when encrypting: " + e.getMessage());
            return new byte[0];
        } catch (BadPaddingException e) {
            System.out.println("Error in padding when encrypting: " + e.getMessage());
            return new byte[0];
        } catch (IllegalBlockSizeException e) {
            System.out.println("Error in block size when encrypting: " + e.getMessage());
            return new byte[0];
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Error in IV when encrypting: " + e.getMessage());
            return new byte[0];
        } catch (NoSuchAlgorithmException e) {
            System.out.println( e.getMessage());
            return new byte[0];
        } catch (NoSuchPaddingException e) {
            System.out.println( e.getMessage());
            return new byte[0];
        }

        return ciphertFile;
    }


    public static byte[] decryptFile( String sessionKey, String ivString,String aad,byte[] file){

        MessageDigest mesDigest;
        Cipher crypto;
        byte[]  fileDec;
        try {

            mesDigest = MessageDigest.getInstance(SHA256);
            crypto = Cipher.getInstance(AES_GCM);
            SecretKeySpec key = new SecretKeySpec(mesDigest.digest(sessionKey.getBytes(StandardCharsets.UTF_8)), AES);
            GCMParameterSpec ivSpec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, getIvParameterSpec(ivString).getIV());
            crypto.init(Cipher.DECRYPT_MODE, key,ivSpec);
            crypto.updateAAD(aad.getBytes());
            fileDec = crypto.doFinal(file);

        } catch (InvalidKeyException e) {
            System.out.println("Error: Invalid key when deccrypting: " + e.getMessage());
            return new byte[0];
        } catch (BadPaddingException e) {
            if (e instanceof AEADBadTagException){System.out.println("tag problem ");}
            System.out.println("Error in padding when deccrypting: " + e.getMessage());
            return new byte[0];
        }
        catch (IllegalBlockSizeException e) {
            System.out.println("Error in block size when deccrypting: " + e.getMessage());
            return new byte[0];
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Error in IV when deccrypting: " + e.getMessage());
            return new byte[0];
        } catch (NoSuchAlgorithmException e) {
            System.out.println( e.getMessage());
            return new byte[0];
        } catch (NoSuchPaddingException e) {
            System.out.println( e.getMessage());
            return new byte[0];
        }


        return fileDec;
    }

    public static IvParameterSpec getIvParameterSpec(String iv){
        IvParameterSpec ivf =new IvParameterSpec(Helper.stringToBytes(iv));
        return ivf;
    }

    //generate random IvParameterSpec 128
    public static IvParameterSpec generate16IV(){
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] iv = new byte[IV_16_LENGTH];
        randomSecureRandom.nextBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        return ivParams;
    }


    //generate random IvParameterSpec 96
    public static IvParameterSpec generate12IV(){
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] iv = new byte[IV_12_LENGTH];
        randomSecureRandom.nextBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        return ivParams;
    }

    //generate hmac from macpassword and data
    public static byte[] createHmac(String macPassord,byte[] data){

        byte[] hmac;
        try {
            mesDigest = MessageDigest.getInstance(SHA256);
            Cryptography.hmac = Mac.getInstance(HMACSHA256);

            byte[] bytemacPass= macPassord.getBytes(StandardCharsets.UTF_8);
            SecretKey key = new SecretKeySpec(mesDigest.digest(bytemacPass),HMACSHA256);
            Cryptography.hmac.init(key);
            hmac= Cryptography.hmac.doFinal(data);

        } catch (NoSuchAlgorithmException e) {
            System.out.println( "NoSuchAlgorithmException "+e.getMessage());
            return new byte[0];
        } catch (InvalidKeyException e) {
            System.out.println( "InvalidKeyException "+e.getMessage());
            return new byte[0];
        }

        return hmac;
    }

    //generate nonce
    public  static String createNonce() {
        byte[] nonce = new byte[64];
        SecureRandom rand = new SecureRandom();
        rand.nextBytes(nonce);
        return ByteBuffer.wrap(nonce).getLong()+"";
    }

}
