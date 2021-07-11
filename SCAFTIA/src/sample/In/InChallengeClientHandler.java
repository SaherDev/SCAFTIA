package sample.In;
import sample.main.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class InChallengeClientHandler extends Thread{

    Socket sock;
    ServerSocket listener;
    String result;
    AppSetting appSetting;
    String params="";
    public InChallengeClientHandler( Socket sock,ServerSocket listener) {
        this.sock = sock;
        this.listener=listener;
        result="";
        appSetting= new AppSetting();
    }

    public InChallengeClientHandler( Socket sock,ServerSocket listener,String params) {
        this.sock = sock;
        this.listener=listener;
        result="";
        this.params=params;
        appSetting= new AppSetting();
    }
    @Override
    public void run() {

        BufferedReader in= null;
        PrintWriter pw=null;
        String remoteSocAdd = "";
        String clientIp="";
        String IncomeString="";
        String[] splitIncomeString;

        String header="";
        String ivString="";
        String aad="";
        String msg="";
        String msgDec="";

        String messageBack="";
        try {
            //strating challenge
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            remoteSocAdd=  sock.getRemoteSocketAddress().toString().substring(1);

            clientIp= InetAddress.getByName(remoteSocAdd.split(":")[0].trim()).toString().replace("/", "");
            String neighIpPort=  Neighbor.getNeighbor(clientIp);
            String  ip=neighIpPort.split(":")[0];
            String port=neighIpPort.split(":")[1];
            String name=ScaftMain.returnUserName(new Neighbor(ip,port)).trim();

            IncomeString= in.readLine();

            splitIncomeString=IncomeString.split(" ");

            if (splitIncomeString.length==4) {


                ivString = splitIncomeString[1];
                aad = splitIncomeString[2];
                msg=splitIncomeString[3];
                if (params.equals(""))
                    header = splitIncomeString[0].trim();
                else
                    header=Cryptography.decryptMessageGCMString(params.split(" ")[0],ivString,aad,Helper.stringToBytes(splitIncomeString[0].trim()));
            }

            System.out.println(header);
            IvParameterSpec iv = Cryptography.generate16IV();

            switch (header) {
                case Message.MESSAGE_CHALLENGE_SESSION://this is fist challenge that sender send token that get from server (message 5)
                    byte[] dec=Cryptography.decrypttMessageGCM(appSetting.getNonSharedPassword(),ivString,aad,Helper.stringToBytes(msg));
                    String nonce;
                    String sessionKey="";
                    String sender="";
                    if(!Arrays.equals(dec,new  byte[0])){
                    List<byte[]> byteArrays = new LinkedList<byte[]>();
                    int index=Cryptography.getsessionKeyLenght();
                    byteArrays.add(Arrays.copyOfRange(dec, 0, index));
                    byteArrays.add(Arrays.copyOfRange(dec, index, dec.length));
                    sessionKey= new String (byteArrays.get(0), StandardCharsets.UTF_8);
                    sender= new String (byteArrays.get(1), StandardCharsets.UTF_8);
                    }
                    if (sender.equals(name)){
                        nonce=Cryptography.createNonce();
                        //the user create challenge to sender back (message 6)
                        Log.addLine("RECEIVE",ip+":"+listener.getLocalPort(),name,header,sessionKey+sender,Helper.convertBytesToHex(Helper.stringToBytes(ivString)),"","Valid");
                        this.result=sessionKey+" "+nonce;
                        messageBack=Message.createChallengeMessage(sessionKey,iv,Message.MESSAGE_CHALLENGE_ACK_SESSION,sender,nonce);
                    }
                    else {
                        //the token is invalid
                        messageBack=Message.createChallengeMessage(sessionKey,iv,Message.MESSAGE_CHALLENGE_ACK_SESSION,sender,Message.MESSAGE_NO);
                        this.result= Message.ERROR_NEIGHBOR_SESSION_KEY;
                    }


                    break;
                case Message.MESSAGE_CHALLENGE_NONCE://this is second challenge that sender (message 7)
                    String sessionK=params.split(" ")[0];
                    String nonc=params.split(" ")[1];
                    String  incomingNonceMinus=Cryptography.decryptMessageGCMString(sessionK,ivString,aad,Helper.stringToBytes(msg));

                    if (((Long.parseLong(nonc) - 1)+"").equals(incomingNonceMinus)){
                        //the user  send ok  tback back (message 8)
                        Log.addLine("RECEIVE",ip+":"+listener.getLocalPort(),name,header,incomingNonceMinus,Helper.convertBytesToHex(Helper.stringToBytes(ivString)),"","Valid");
                        messageBack=Message.createChallengeMessage(sessionK,iv,Message.MESSAGE_CHALLENGE_ACK_NONCE,name,Message.MESSAGE_OK);
                        this.result=Message.MESSAGE_OK;
                    }
                    else {
                        //send back error
                        messageBack=Message.createChallengeMessage(sessionK,iv,Message.MESSAGE_CHALLENGE_ACK_NONCE,name,Message.MESSAGE_NO);
                        this.result=Message.ERROR_NEIGHBOR_NONCE_NOT_CORRECT;
                    }

                    break;
            }
            pw.write( messageBack+ "\n");
            pw.flush();
            try { pw.close(); } catch (Exception ex) {}
            try { in.close(); } catch (Exception ex) {}
            try { sock.close(); } catch (Exception ex) {}

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public String getResult() {
        return result;
    }
}
