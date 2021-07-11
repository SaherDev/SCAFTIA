package sample.main;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import sample.Controller;
import sample.In.InThread;
import sample.Out.OutThread;
import sample.Out.OutThreadInvalidMessages;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

public class ScaftMain {

    static InThread inMessagethraed;
    static ServerSocket listenerMessageServer;

    static InThread inFileThread;
    static ServerSocket listenerFileServer;

    static Controller controller;

    public ScaftMain(){}

    public ScaftMain(Controller controller) {this.controller=controller;}

    //start receive messages from [port]
    public void startIn(int port){
        try {
            listenerMessageServer = new   ServerSocket(port);
        } catch (IOException e) {System.out.println("Error  startIn"+ e.getMessage());    }
        inMessagethraed =new InThread(listenerMessageServer,Message.MODE_MESSAGE_MODE);
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


    //start receive file from [port]
    public static void startInFile(int port){
        try {
            listenerFileServer = new   ServerSocket(port);
        } catch (IOException e) {System.out.println("Error  startInFile"+ e.getMessage());    }
        inFileThread =new InThread(listenerFileServer,Message.MODE_FILE_MODE);
        inFileThread.start();
    }

    //stop receive files at all
    public static void stopInFile(){

        try {
            inFileThread.interrupt();
            listenerFileServer.close();
        } catch (IOException |NullPointerException e) {
            System.out.println("Error  stopInFile"+ e.getMessage());
        }
    }

    //check if  listenerMessageServer is closed or null
    public static Boolean isMessageServerClosedOrNull(){
        if(listenerMessageServer==null) return true;
        return  listenerMessageServer.isClosed();}

    //check if  listenerFileServer is closed or null
    public static Boolean isFileServerClosedOrNull(){
        if(listenerFileServer==null) return true;
        return  listenerFileServer.isClosed();}


    //send hello message for other neighbors [hello,username]
    public void SendtOutHello(){
        AppSetting appSetting= new AppSetting();
        new OutThread(Message.MODE_MESSAGE_MODE,Neighbor.getAllNeighbors(),Message.MESSAGE_HELLO,appSetting.getUserName()).start();
    }

    //send hello message for obline neighbors [bye,username]
    public void SendtOutBye(){
        AppSetting appSetting= new AppSetting();
        new OutThread(Message.MODE_MESSAGE_MODE,getOnlineNeighbors(),Message.MESSAGE_BYE,appSetting.getUserName()).start();
    }

    //send message to online neighbors list
    public void sendMessagetoallOnline(List<Neighbor> neighbors,String msg){
        new OutThread(Message.MODE_MESSAGE_MODE,neighbors,Message.MESSAGE_MESSAGE,msg).start();
    }


    //send message to online neighbor list
    public static void sendMessagetoallOnlineNeighbor(Neighbor neighbor,String header,String msg) {
        new OutThread(Message.MODE_MESSAGE_MODE,neighbor,header,msg).start();
    }


    //send invailed message to online neighbor list
    public static void sendInvailedMessagetoallOnlineNeighbor(Neighbor neighbor,String header,String msg,String invalidMsg,String customeFieldString) {
        new OutThreadInvalidMessages(Message.MODE_MESSAGE_MODE,neighbor,header,msg,invalidMsg,customeFieldString).start();
    }

    //start sending file after get ok message from receiver
    public static void startsendingFile(Neighbor neighbor,String sessionKeys,String filePath) {
       // System.out.println("start out thread");
        new OutThread(Message.MODE_FILE_MODE,neighbor,sessionKeys,filePath).start();
    }

    //online neighbors

    public  static List<Neighbor> getOnlineNeighbors(){ return controller.getOnlineNeighbors( );}

    public  static void addOnlineNeighbor(Neighbor neighbor){controller.addOnlineNeigfhbor( neighbor);   }

    public   static void removeOnlineNeighbor(Neighbor neighbor){controller.removeOnlineNeigfhbor( neighbor);   }

    public  static Boolean checkIfOnline(Neighbor neighbor){ return   controller.checkIfOnline(neighbor);  }


    //suspicious Neighbors
    public  static void addSuspiciousNeighbors(Neighbor neighbor){controller.addSuspiciousNeighbor( neighbor);   }

    public   static void removeSuspiciousNeighbors(Neighbor neighbor){controller.removeSuspiciousNeighbor( neighbor);   }

    public  static Boolean checkSuspiciousNeighbors(Neighbor neighbor){ return   controller.checkSuspiciousNeighbor(neighbor);  }



    public  static void printRceiveeMessage(Neighbor from,String msg) {
        controller.printRceiveeMessage(from.getName(), msg);
    }
    public  static void printFailedRceiveeMessage(Neighbor from,String msg) {
        controller.printRceiveeMessage(from.getName(), msg+"  Failed!! ");
    }
    public  static void printSendFile(Neighbor to,String fileName) {
        controller.printSendeMessage(fileName +"  To > "+to.getName());
    }
    public  static void printFailedSendFile(Neighbor to,String fileName) {
        controller.printSendeMessage(fileName +"  To > "+to.getName()+"  Failed!! ");
    }

    public  static String  returnUserName(Neighbor neighbor){
        String name="";
        for (Neighbor n:getOnlineNeighbors()){
            if (n.equals(neighbor)) {
                name=n.getName();   break;
            }
        }
        return name;
    }

    //show message to user
    public static  void ShowDialog(String title,String msg){
        Platform.runLater(()->{
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
        });
    }



}
