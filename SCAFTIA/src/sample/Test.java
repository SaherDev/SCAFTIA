package sample;

import sample.Out.OutThreadToServer;
import sample.main.Cryptography;
import sample.main.Message;
import sample.main.Server;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        Server server =Server.getServer();
        String responceFromServer="";
        String msg= Message.createMessageToServer("client1","client2", "nonce");

        OutThreadToServer outThreadToServer=  new OutThreadToServer(server,"hello");

        outThreadToServer.start();


        outThreadToServer.join();

        responceFromServer=outThreadToServer.getResponce();
        System.out.println("rsponce "+responceFromServer);
    }
}
