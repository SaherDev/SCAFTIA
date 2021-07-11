package sample.main;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Neighbor {
    private String ip;
    private String port;
    private String name;
    private static final String FILENAME = "Neighbors.txt";

    public Neighbor(String ip,String port){
        this.ip=ip;
        this.port=port;
    }

    public Neighbor(String ip,String port,String name){
        this.ip=ip;
        this.port=port;
        this.name=name;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public   static void addNeighbor(String ip, String port){
        File file = new File(FILENAME);
        FileWriter fr = null;
        try {
            fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(ip+":"+port);
            br.newLine();
            br.close();
            fr.close();
        } catch (IOException e) {
            System.out.println("Erorr addNeighbor");
        }

    }

    //remove neighbor [ip:port] from Neighbors.txt file
    public  static void removeNeighbor(String ip, String port)
    {
        String neighbor=ip+":"+port;
        try {
            File file = new File(FILENAME);
            List<String> out = Files.lines(file.toPath()).filter(line -> !line.contains(neighbor)).collect(Collectors.toList());
            Files.write(file.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Erorr removeNeighbor");
        }
    }

    //remove neighbor [ip:port] from Neighbors.txt file
    public  static List<Neighbor> getAllNeighbors(){
        List<Neighbor> neighbors= new LinkedList<Neighbor>();
        try
        {
            FileInputStream fis=new FileInputStream(FILENAME);
            Scanner sc=new Scanner(fis);
            while(sc.hasNextLine())
            {
                String line[]=sc.nextLine().split(":");
                neighbors.add(new Neighbor(line[0],line[1]));
            }
            sc.close();
        }
        catch(IOException |ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Erorr getAllNeighbors");
            return null;
        }
        return neighbors;
    }

    //return  neighbor list  [ip:port] from Neighbors.txt file
    public  static List<String> getAllNeighborsStringList(){
        List<String> neighbors= new LinkedList<String>();
        try
        {
            FileInputStream fis=new FileInputStream(FILENAME);
            Scanner sc=new Scanner(fis);
            while(sc.hasNextLine())
            {

                neighbors.add(sc.nextLine());
            }
            sc.close();
        }
        catch(IOException e)
        {
            System.out.println("Erorr getAllNeighborsStringList");

        }
        return neighbors;
    }

    //return  neighbor  [ip:port] from Neighbors.txt file by ip
    public  static String getNeighbor(String ip){

        String neighbor ="";
        try
        {
            FileInputStream fis=new FileInputStream(FILENAME);
            Scanner sc=new Scanner(fis);
            String line="";
            while(sc.hasNextLine())
            {
                line=sc.nextLine();

                if(line.split(":")[0].equals(ip)){
                    neighbor=line;
                    break;
                }
            }
            sc.close();
        }
        catch(IOException e)
        {
            System.out.println("Erorr getNeighbor");
        }
        return neighbor;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Neighbor neighbor = (Neighbor) obj;
        return getIp().equals( neighbor.getIp()) &&
                getPort().equals(neighbor.getPort());

    }

    @Override
    public String toString() {
        return getIp() + ":" + getPort() + "   " + getName();
    }

}
