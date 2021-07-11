package sample.main;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class User {

    private String name;
    private String password;
    private static final String FILENAME = "User.txt";

    public User(String name, String password){
        this.name=name;
        this.password=password;
    }


    public String getPassword(){ return password; }
    public String getName() {
        return name;
    }


    public   static void addUser(String name, String password){
        File file = new File(FILENAME);
        FileWriter fr = null;
        try {
            fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(name+" = "+password);
            br.newLine();
            br.close();
            fr.close();
        } catch (IOException e) {
            System.out.println("Erorr addUser");
        }

    }


    public  static List<User> getAllUsers(){
        List<User> users= new LinkedList<User>();
        try
        {
            FileInputStream fis=new FileInputStream(FILENAME);
            Scanner sc=new Scanner(fis);
            while(sc.hasNextLine())
            {
                String line[]=sc.nextLine().split(" = ");
                users.add(new User(line[0],line[1]));
            }
            sc.close();
        }
        catch(IOException |ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Erorr getAllNeighbors");
            return null;
        }
        return users;
    }





    public  static String getRandomUserName(String name1,String name2){
        String user ="user";
        try
        {
            FileInputStream fis=new FileInputStream(FILENAME);
            Scanner sc=new Scanner(fis);
            String line="";
            while(sc.hasNextLine())
            {
                line=sc.nextLine();

                if((!line.split(" = ")[0].equals(name1))&&(!line.split(" = ")[0].equals(name2))){
                    user=line.split(" = ")[0];
                    break;
                }
            }
            sc.close();
        }
        catch(IOException e)
        {
            System.out.println("Erorr getNeighbor");
        }
        return user;
    }



    public  static String getUserPassword(String name){


        String password ="";
        try
        {
            FileInputStream fis=new FileInputStream(FILENAME);
            Scanner sc=new Scanner(fis);
            String line="";
            while(sc.hasNextLine())
            {
                line=sc.nextLine();

                if(line.split(" = ")[0].equals(name)){
                    password=line.split(" = ")[1];
                    break;
                }
            }
            sc.close();
        }
        catch(IOException e)
        {
            System.out.println("Erorr getUserPassword");
        }
        return password;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        User user = (User) obj;
        return getName() .equals( user.getName()) &&
                getPassword().equals(user.getPassword());

    }

    @Override
    public String toString() {
        return getName() + " = " +getPassword();
    }

}
