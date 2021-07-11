package sample.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private static final String FILENAME = "Log.txt";


    //write log to log txt file
    public   static void addLine(String type,String ipAndPort,String user,String haeder,String msg,String iv,String hmac,String validFlag) {

        if (haeder.equals(Message.MESSAGE_ACK_HELLO)){
            haeder=Message.MESSAGE_HELLO;
            msg=Message.MESSAGE_ACK_HELLO+"-"+user;
        }

        if (haeder.equals(Message.MESSAGE_MESSAGE))
            msg=removeLastChar(msg);

        if (!validFlag.equals("")){validFlag=" | " +validFlag;}
        if (!hmac.equals("")){hmac=" | " +hmac;}
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String line = currentTime + " | " + type + " | " + ipAndPort + " | " + user + " | " + haeder + " | " + msg + " | " + iv+hmac +validFlag;

        File file = new File(FILENAME);
        FileWriter fr = null;
        try {
            fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(line);
            br.newLine();
            br.close();
            fr.close();
        } catch (IOException e) {
            System.out.println("Erorr");
        }
    }

    //write bad message to log txt file
    public   static void addBadMessage(String type,String ipAndPort,String user,String haeder) {

        if (haeder.equals(Message.MESSAGE_ACK_HELLO)){
            haeder=Message.MESSAGE_HELLO;
        }

        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String line = currentTime + " | " + type + " | " + ipAndPort + " | " + user + " | [ BAD  " + haeder + " ] | " ;

        File file = new File(FILENAME);
        FileWriter fr = null;
        try {
            fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(line);
            br.newLine();
            br.close();
            fr.close();
        } catch (IOException e) {
            System.out.println("Erorr");
        }
    }

    //write  error to log txt file
    public   static void addError(String ipAndPort,String user,String haeder,String error) {



        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String line = currentTime + " | " +haeder + "  | "+ ipAndPort + " | " + user + " |  " + error;

        File file = new File(FILENAME);
        FileWriter fr = null;
        try {
            fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(line);
            br.newLine();
            br.close();
            fr.close();
        } catch (IOException e) {
            System.out.println("Erorr");
        }
    }

    //write Decreption fault to log txt file
    public   static void addDecreptionFault(String type,String ipAndPort) {

        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String line = currentTime + " | " + type + " | " + ipAndPort + " | [ Decreption Fault ] | " ;

        File file = new File(FILENAME);
        FileWriter fr = null;
        try {
            fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(line);
            br.newLine();
            br.close();
            fr.close();
        } catch (IOException e) {
            System.out.println("Erorr");
        }
    }

    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }

}



