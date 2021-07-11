package sample.In;
import sample.main.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class InClientHandler extends Thread{

    Socket socket;
    public InClientHandler(Socket socket){
        this.socket=socket;
    }
    @Override
    public void run() {

        BufferedReader in= null;
        PrintWriter pw=null;
        String IncomeString="";
        String[] splitIncomeString;

        String sender="";
        String receiver="";
        String nonceSender="";
        boolean isFoundedUsers=false;
        String intentionallyIncorrect="";
        String outerMessage="";

        try {

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            String remoteSocAdd=  socket.getRemoteSocketAddress().toString().substring(1);
             String clientIp= InetAddress.getByName(remoteSocAdd.split(":")[0].trim()).toString().replace("/", "");

            IncomeString= in.readLine();
            splitIncomeString=IncomeString.split(" ");

            //the server check if the message is valid
            if (splitIncomeString.length==3) {
                sender=splitIncomeString[0].trim();
                receiver=splitIncomeString[1].trim();
                nonceSender=splitIncomeString[2].trim();
            }

            IvParameterSpec iv = Cryptography.generate16IV();
            byte[] sessionKey=Cryptography.generateSessionkey();
            String senderPass="";
            String receiverPass="";
            String aad="";

            //the server find the userspassword
            if (!User.getUserPassword(sender).equals("")){
                if (!User.getUserPassword(receiver).equals("")){
                    isFoundedUsers=true;
                    senderPass=User.getUserPassword(sender).trim();
                    receiverPass=User.getUserPassword(receiver).trim();
                }
            }

            if (isFoundedUsers) {//there is no problem with users the server have passwords
                String invalidMsg=ServerMain.getInvalidResponce();
                if (invalidMsg.equals(Message.INVAILED_NONCE)){nonceSender=Cryptography.createNonce(); intentionallyIncorrect=Message.INVAILED_NONCE;}// Response with an invalid nonce (not the one the user sent)
                if (invalidMsg.equals(Message.INVAILED_TARGET_NAME)){receiver=User.getRandomUserName(sender,receiver);intentionallyIncorrect=Message.INVAILED_TARGET_NAME;}//Response with an invalid target name (not the one the user sent)
                if (invalidMsg.equals(Message.INVAILED_REQUESTOR_KEY)){senderPass="INVAILED_REQUESTOR_KEY";intentionallyIncorrect=Message.INVAILED_REQUESTOR_KEY;}//Response that is encrypted with the wrong requestor key (using a random key)
                if (invalidMsg.equals(Message.INVAILED_TARGET_KEY)){receiverPass="INVAILED_TARGET_KEY";intentionallyIncorrect=Message.INVAILED_TARGET_KEY;}///Response that is encrypted with the wrong target key (using a random key)
                aad = receiver;
                String token = Message.createToken(receiverPass, iv, aad, Helper.concat(sessionKey, sender.getBytes()));
                outerMessage = Helper.bytesToString(sessionKey) + " " + nonceSender + " " + receiver + " " + token;
                outerMessage = Message.createOuterMessage(senderPass, iv, aad, outerMessage.getBytes());
                Log.addLine(remoteSocAdd,sender,receiver,nonceSender,true,"",true,intentionallyIncorrect);
                ServerMain.printMessage(clientIp,sender,receiver,nonceSender,true,"",true,intentionallyIncorrect);
            }
            else {
                outerMessage=Message.MESSAGE_USER_NOT_FOUND;//user not found rsponce back
                Log.addLine(remoteSocAdd,sender,receiver,nonceSender,false,Message.MESSAGE_USER_NOT_FOUND,true,intentionallyIncorrect);
                ServerMain.printMessage(clientIp,sender,receiver,nonceSender,false,Message.MESSAGE_USER_NOT_FOUND,true,intentionallyIncorrect);
            }

            //send the message back
            pw.write( outerMessage+ "\n");
            pw.flush();
            try { pw.close(); } catch (Exception ex) {}
            try { in.close(); } catch (Exception ex) {}
            try { socket.close(); } catch (Exception ex) {}

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}