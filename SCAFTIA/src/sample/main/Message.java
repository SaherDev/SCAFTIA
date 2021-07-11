package sample.main;

import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;

public class Message {
    public final static String MESSAGE_HELLO = "HELLO";
    public final static String MESSAGE_BYE = "BYE";
    public final static String MESSAGE_MESSAGE = "MESSAGE";
    public final static String MESSAGE_SENDFILE = "SENDFILE";
    public final static String MESSAGE_OK = "OK";
    public final static String MESSAGE_NO = "NO";
    public final static String MESSAGE_ACK="ACK";
    public final static String MESSAGE_FILE="FILE";

    public final static String INVAILED_SERVER_REQUESTOR_NAME = "SERVER REQUESTOR NAME";
    public final static String INVAILED_SERVER_RECEIVER_NAME = "SERVER RECEIVER NAME";
    public final static String INVAILED_TOKEN = "RANDOM TOKEN";
    public final static String INVAILED_CHALLENGE = "CHALLENGE ENC WITH WRONG SESS";
    public final static String INVAILED_RESPONCE_WRONG_KEY = "RESPONCE WRONG KEY";
    public final static String INVAILED_RESPONCE_WRONGE_NONCE = "RESPONCE WRONGE NONCE";
    public final static String INVAILED_ENCREPTED_FILE_WRONG_KEY = "ENCREPTED FILE WRONG KEY";


    public final static String MESSAGE_CHALLENGE_SESSION="SESSION";
    public final static String MESSAGE_CHALLENGE_NONCE="NONCE";
    public final static String MESSAGE_CHALLENGE_ACK_SESSION="ACK_SESSION";
    public final static String MESSAGE_CHALLENGE_ACK_NONCE="ACK_NONCE";

    public final static String MESSAGE_SESSION_KEY_REQUEST="SESSION_KEY_REQUEST";
    public final static String MESSAGE_ACK_SESSION_KEY_REQUEST="ACK_SESSION_KEY_REQUEST";
    public final static String MESSAGE_SESSION_KEY_REQUEST_USER_NOT_FOUND = "USER_NOT_FOUND";

    public final static String ERROR_SERVER="ERROR_SERVER";
    public final static String ERROR_NEIGHBOR ="ERROR_NEIGHBOR";

    public final static String ERROR_SERVER_UNAVAILABLE="authentication server isn't available";
    public final static String ERROR_SERVER_INVALID_RESPONCE="server gave an invalid response";

    public final static String ERROR_NEIGHBOR_UNAVAILABLE="neighbor isn't available";
    public final static String ERROR_NEIGHBOR_INVALID_RESPONCE="neighbor gave an invalid response";
    public final static String ERROR_NEIGHBOR_SESSION_KEY="token was invalid";
    public final static String ERROR_NEIGHBOR_NONCE_NOT_CORRECT="challenge response was invalid";

    public final static String MESSAGE_ACK_HELLO = "ACK_HELLO";
    public final static String MESSAGE_ACK_FILE_SUCCEED = "ACK_FILE_SUCCEED";
    public final static String MESSAGE_ACK_FILE_FAILED = "ACK_FILE_FAILED";

    public final static String MODE_MESSAGE_MODE = "MESSAGE_MODE";
    public final static String MODE_FILE_MODE = "FILE_MODE";



    //creating string message [encrypted header,iv,encrypted content]
    public static String createMessage(String header, String sharedPassword,String macPassword,IvParameterSpec iv, String message){
        byte[] headerEncByte =Cryptography.encryptMessage(sharedPassword,iv,header);
        byte[] msgEncByte=Cryptography.encryptMessage(sharedPassword,iv,message);
        byte[] combined=Helper.concat(headerEncByte,iv.getIV(),msgEncByte);

        String headerEnc= Helper.bytesToString(headerEncByte);
        String msgEnc=Helper.bytesToString(msgEncByte);
        String ivString=Helper.bytesToString(iv.getIV()) ;
        String  hmac=Helper.convertBytesToHex(Cryptography.createHmac(macPassword,combined));

      return headerEnc+" "+ivString+" "+msgEnc+" "+hmac;
    }


    //creating byte[] filedata [iv,header/fileName,encrypted filedata+tag]
    public static byte[] createFile(String sessionKey,IvParameterSpec iv,String fileName,byte[] file) {
        byte[] ivStringWithSpac=Helper.concat(iv.getIV()," ".getBytes());
        byte[] filNameWithSpace=Helper.concat(fileName.getBytes()," ".getBytes());
        byte[] fileEnc=Cryptography.encrypFile(sessionKey,iv,fileName,file);
        return Helper.concat(ivStringWithSpac,filNameWithSpace,fileEnc);
    }

    //creating byte[] [sender,receiver,nonce]
    public static String createMessageToServer(String sender,String receiver,String nonce) {
        return sender+" "+receiver+" "+nonce;
    }

    //create challenge message
    public static String createChallengeMessage(String password, IvParameterSpec iv, String header,String aad , String message){
        String ivString=Helper.bytesToString(iv.getIV());
        String msgEnc=Cryptography.encryptMessageGCM(password,iv,aad,message);
        String headerEnc=Cryptography.encryptMessageGCM(password,iv,aad,header);
        return headerEnc+" "+ivString+" "+aad+" "+msgEnc;
    }

    //create token message
    public static String createToken(String password, IvParameterSpec iv, String aad, byte[] message){
        String msg=new String (message, StandardCharsets.UTF_8);
        return Cryptography.encryptMessageGCM(password,iv,aad,msg);
    }

}
