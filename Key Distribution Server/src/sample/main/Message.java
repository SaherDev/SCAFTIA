package sample.main;

import javax.crypto.spec.IvParameterSpec;

public class Message {
    public final static String MESSAGE_USER_NOT_FOUND = "USER_NOT_FOUND";
    public final static String INVAILED_NONCE = "INVAILED NONCE";
    public final static String INVAILED_TARGET_NAME = "INVAILED TARGET NAME";
    public final static String INVAILED_TARGET_KEY = "INVAILED TARGET KEY";
    public final static String INVAILED_REQUESTOR_KEY = "INVAILED REQUESTOR KEY";
    public final static String NOT_INVAILED = "NOT_INVAILED";

    public static String createOuterMessage(String password, IvParameterSpec iv, String aad, byte[] message){
        String ivString=Helper.bytesToString(iv.getIV());
        String msgEnc=Cryptography.encryptMessage(password,iv,aad,message);
        return ivString+" "+aad+" "+msgEnc;
    }

    public static String createToken(String password, IvParameterSpec iv, String aad, byte[] message){
        return Cryptography.encryptMessage(password,iv,aad,message);
    }

}
