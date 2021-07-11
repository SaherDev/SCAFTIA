package sample.Out;

import sample.main.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class OutThreadToServer extends Thread {

    Server server;
    String msg;
    String responce;

    public OutThreadToServer(Server server,String msg){
        this.server=server;
        this.msg=msg;
        this.responce="";
    }

    @Override
    public void run() {
        Socket client = null;
        PrintWriter pw =  null;
        BufferedReader br = null;
        try {


            //send message to server and get responce back (msg 3)
            InetAddress address = InetAddress.getByName(server.getIp());
            client  = new Socket(address, Integer.parseInt(server.getPort()));
            client.setSoTimeout(5*1000);
            pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            pw.write( msg+ "\n");
            Log.addLine("SEND   ",server.getIp() + ":" + server.getPort()," server",Message.MESSAGE_SESSION_KEY_REQUEST,msg,"","","");
            pw.flush();
            this.responce= br.readLine();

            //Log.addLine("RECEIVE",server.getIp() + ":" + server.getPort()," server",Message.MESSAGE_ACK_SESSION_KEY_REQUEST,responce,"","","");

            try { pw.close(); } catch (Exception ex) {}
            try { br.close(); } catch (Exception ex) {}
            try { client.close(); } catch (Exception ex) {}
        }
        catch (UnknownHostException e) {
            System.out.println("UnknownHostException"+e.getMessage());
        } catch (SocketException e) {
            System.out.println("SocketException "+e.getMessage());
        } catch (IOException e) {
            System.out.println("SocketException "+e.getMessage());

        }
    }

    public String getResponce() {
        return responce;
    }
}
