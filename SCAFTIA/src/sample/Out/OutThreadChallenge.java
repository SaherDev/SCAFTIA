package sample.Out;
import sample.main.Helper;
import sample.main.Log;
import sample.main.Message;
import sample.main.Neighbor;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class OutThreadChallenge  extends Thread{

    Neighbor neighbor;
    String header;
    String msgDec;
    String msg;
    String result;


    public OutThreadChallenge(Neighbor neighbor,String header,String msgDec,String msg){
        this.header=header;
        this.neighbor=neighbor;
        this.msgDec=msgDec;
        this.msg=msg;
        this.result ="";
    }

    @Override
    public void run() {
        Socket client = null;
        PrintWriter pw =  null;
        BufferedReader br = null;
        try {

            //send cjhallenge to user and get responce back (msg 7+9)
            InetAddress address = InetAddress.getByName(neighbor.getIp());
            client  = new Socket(address, Integer.parseInt(neighbor.getPort()));
            client.setSoTimeout(5*1000);
            pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            Log.addLine("SEND   ",neighbor.getIp() + ":" + neighbor.getPort(),neighbor.getName(),header,msgDec, Helper.convertBytesToHex(Helper.stringToBytes(msg.split(" ")[1])),"","");
            pw.write( msg+ "\n");
            pw.flush();
            this.result = br.readLine();


        }
        catch (UnknownHostException e) {
            System.out.println("UnknownHostException"+e.getMessage());
            Log.addError(neighbor.getIp() + ":" + neighbor.getPort(),neighbor.getName(),Message.ERROR_NEIGHBOR,Message.ERROR_NEIGHBOR_UNAVAILABLE);
        } catch (SocketException e) {
            System.out.println("SocketException "+e.getMessage());
            Log.addError(neighbor.getIp() + ":" + neighbor.getPort(),neighbor.getName(),Message.ERROR_NEIGHBOR,Message.ERROR_NEIGHBOR_UNAVAILABLE);
        } catch (IOException e) {
            System.out.println("SocketException "+e.getMessage());
            Log.addError(neighbor.getIp() + ":" + neighbor.getPort(),neighbor.getName(),Message.ERROR_NEIGHBOR,Message.ERROR_NEIGHBOR_UNAVAILABLE);
        }
    }

    public String getResult() {
        return result;
    }
}
