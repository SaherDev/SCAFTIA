package sample.main;
import java.util.LinkedList;
import java.util.List;

public class Challenge {

    String sender;
    String sessionKey;
    String nonce;
    Boolean isReady;

    private static List<Challenge> challenges=new LinkedList<>();

    public Challenge(String sender, String sessionKey, String nonce){
            this.sender=sender;
            this.sessionKey=sessionKey;
            this.nonce=nonce;
            this.isReady=false;
    }
    public Challenge(String sender, String sessionKey, String nonce, boolean isReady){
        this.sender=sender;
        this.sessionKey=sessionKey;
        this.nonce=nonce;
        this.isReady=isReady;
    }

    public Boolean getIsReady(){return isReady;}
    public String getSender() {
        return sender;
    }
    public String getSessionKey(){return sessionKey;}
    public String getNonce(){return nonce;}

    public static void addChhallenge(Challenge challenge){
        challenges.add(challenge);
    }

    public static void removeChhallenge(String sessionKey){
        for (Challenge c :challenges){
            if (c.getSessionKey().equals(sessionKey))
                challenges.remove(c);
        }
    }
    public static Boolean isChallengeExist(String sender){
        Boolean exist=false;
        for (Challenge c :challenges){
            if (c.getSender().equals(sender))
                exist=true;
        }
        return exist;
    }

    public static String getChalengeParams(String sender){
        String params="";
        for (Challenge c :challenges){
            if (c.getSender().equals(sender))
               params=c.getSessionKey() +" "+c.getNonce();
        }
        return params;
    }
    public static Boolean isChallengeReady(String sender){
        Boolean ready=false;
        for (Challenge c :challenges){
            if (c.getSender().equals(sender))
            {
                if (c.getIsReady()==true){
                    ready=true;
                }
            }
        }
        return ready;
    }
    public static void changeReadyStaus(String sender,boolean status){
       Challenge challenge;
        for (Challenge c :challenges){
            if (c.getSender().equals(sender))
            {
                challenges.remove(c);
                challenges.add(new Challenge(c.sender,c.sessionKey,c.nonce,status));

            }
        }

    }
}
