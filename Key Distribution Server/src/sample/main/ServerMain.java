package sample.main;
import sample.Controller;
import sample.In.InThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerMain {

    static InThread inMessagethraed;
    static ServerSocket listenerMessageServer;
    static String invalidResponce=Message.NOT_INVAILED;
    static Controller controller;

    public ServerMain(){}

    public ServerMain(Controller controller) {this.controller=controller;}

    //start receive messages from [port]
    public void startIn(int port){
        try {
            System.out.println(port);
            listenerMessageServer = new   ServerSocket(port);
        } catch (IOException e) {System.out.println("Error  startIn "+ e.getMessage());    }
        inMessagethraed =new InThread(listenerMessageServer);
        inMessagethraed.start();
    }

    //stop receive messages at all
    public void stopIn(){

        try {
            inMessagethraed.interrupt();
           listenerMessageServer.close();
        } catch (IOException |NullPointerException e) {
            System.out.println("Error  stopIn"+ e.getMessage());
        }
    }

    //print messages in texbox
    public  static void printMessage(String ipAndPort,String sender,String recipient,String nonce,Boolean isEncrypted,String error,Boolean isSentBack,String intentionallyIncorrect) {
        String msg=
        "Date Time: "+    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"\n"+
        "sender’s IP and port: "+ipAndPort+ "\n"+
        "sender’s name: "+sender+ "\n"+
        "recipient’s name: "+recipient+ "\n"+
        "nonce (NA) sent: "+nonce+ "\n"+
        "was encrypted: "+(isEncrypted ?"encrypted":"not encrypted" )+ "\n"+
        "error: "+error+ "\n"+
        "sent back: "+(isSentBack ?"YES":"NO" )+ "\n"+
         "intentionally Incorrect: "+(intentionallyIncorrect=="" ?"NO":intentionallyIncorrect )+ "\n"+
         "            ---------------------"+ "\n";

      controller.printMessage(msg);
    }

    public static String getInvalidResponce(){return invalidResponce;}
    public static void setInvalidResponce(String invalid){ invalidResponce=invalid;}

}
