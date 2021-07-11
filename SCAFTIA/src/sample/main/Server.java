package sample.main;

import java.io.*;
import java.util.Scanner;

public class Server {
    private String ip;
    private String port;
    private static final String FILENAME = "Server.txt";
    public Server(String ip,String port){
        this.ip=ip;
        this.port=port;
    }
    public String getIp(){
        return ip;
    }
    public String getPort(){
        return port;
    }

    public static void updateServer(String ip, String port){
        File file = new File(FILENAME);
        FileWriter fr = null;
        try {
            fr = new FileWriter(file, false);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(ip+":"+port);
            br.newLine();
            br.close();
            fr.close();
        } catch (IOException e) {
            System.out.println("Erorr addNeighbor");
        }
    }

    public  static Server getServer(){
        Server  server=null;
        try
        {
            FileInputStream fis=new FileInputStream(FILENAME);
            Scanner sc=new Scanner(fis);
           String  s=sc.nextLine();
           server= new Server(s.split(":")[0],s.split(":")[1]);
            sc.close();
        }
        catch(IOException e)
        {
            System.out.println("Erorr getServer");

        }
        return server;
    }
}
