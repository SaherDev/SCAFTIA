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
    public   static void addLine(String ipAndPort,String sender,String recipient,String nonce,Boolean isEncrypted,String error,Boolean isSentBack,String intentionallyIncorrect) {

        if (!error.equals("")){error=" | " +error;}

        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String line = currentTime + " | " + ipAndPort + " | " + sender + " | " + recipient + " | " + nonce + " | " + (isEncrypted ?"encrypted":"not encrypted" )+ error+" | " + (isSentBack ?"sent back":"Not sent back" )+" | " +(intentionallyIncorrect=="" ?"NO":intentionallyIncorrect   );

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





}



