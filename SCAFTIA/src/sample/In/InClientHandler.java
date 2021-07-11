package sample.In;

import sample.Out.OutThread;
import sample.Out.OutThreadChallenge;
import sample.main.*;
import javafx.application.Platform;

import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class InClientHandler extends Thread{
    Socket sock;
    AppSetting appSetting;
    String mode;
    ServerSocket listener;
    String sessionkey="";



    public InClientHandler(String mode, Socket sock,ServerSocket listener) {
        this.mode=mode;
        this.sock = sock;
        this.listener=listener;
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
        String iv ="";
        String msg="";
        String hmacHex="";
        String ivHex="";
        boolean validHmac=false;
        String valifFlag="Invalid";

        //here the app start to rec file data [iv,filename/header,filedata+tag]
        if (mode.equals(Message.MODE_FILE_MODE)){ //start challenge before sending the file (msges 5 > 8 )

            try {
                remoteSocAdd=  sock.getRemoteSocketAddress().toString().substring(1);
                clientIp= InetAddress.getByName(remoteSocAdd.split(":")[0].trim()).toString().replace("/", "");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            String neighIpPort=  Neighbor.getNeighbor(clientIp);
            String  ip=neighIpPort.split(":")[0];
            String port=neighIpPort.split(":")[1];
            String name=ScaftMain.returnUserName(new Neighbor(ip,port)).trim()+"/"+listener.getLocalPort();

            if (!Challenge.isChallengeExist(name)){//the challenge is not exist

                //send responce back
                InChallengeClientHandler challenge = new InChallengeClientHandler(sock,listener);
                challenge.start();
                try {
                    challenge.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String fistResult=challenge.getResult();
                if (fistResult.split(" ").length==2){
                    Challenge.addChhallenge(new Challenge(name,fistResult.split(" ")[0],fistResult.split(" ")[1]));
                }
                //there is error with the error
                else {Log.addError(ip+":"+port,name.split("/")[0],Message.ERROR_NEIGHBOR,fistResult);}
                return;
            }
            else {//the challenge is  exist
                if (!Challenge.isChallengeReady(name)){
                    InChallengeClientHandler challenge2 = new InChallengeClientHandler(sock,listener,Challenge.getChalengeParams(name));
                    challenge2.start();
                    try {
                        challenge2.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String SeconResult=challenge2.getResult();

                    if (SeconResult.equals(Message.MESSAGE_OK)){
                        Challenge.changeReadyStaus(name,true);//the challenge is done you can send file to me now
                    }
                    //there is error with the error
                    else {Log.addError(ip+":"+port,name.split("/")[0],Message.ERROR_NEIGHBOR,SeconResult);}
                    return;
                }

            }


            //  System.out.println("start rec");
            try {
                sessionkey=Challenge.getChalengeParams(name).split(" ")[0];
                BufferedInputStream fileReader = new BufferedInputStream(sock.getInputStream());
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                String FileNameHeader;
                byte[] data = new byte[1];
                int nRead;
                boolean save=true;
                //reading all the data
                while ((nRead = fileReader.read(data, 0, data.length)) != -1) {buffer.write(data, 0, nRead);   }

                buffer.flush();
                byte[] byteArray = buffer.toByteArray();
                fileReader.close();

                //split the data and decrypt the file
                ivHex=Helper.convertBytesToHex(Helper.splitByteWithFirstDelimiter(byteArray," ".getBytes()).get(0));
                iv= Helper.bytesToString(Helper.splitByteWithFirstDelimiter(byteArray," ".getBytes()).get(0));
                byte[] fileNameAndData=Helper.splitByteWithFirstDelimiter(byteArray," ".getBytes()).get(1);
                FileNameHeader=new String(Helper.splitByteWithFirstDelimiter(fileNameAndData," ".getBytes()).get(0));
                byte[] fileEnc=Helper.splitByteWithFirstDelimiter(fileNameAndData," ".getBytes()).get(1);
                byte[] fileDec=Cryptography.decryptFile(sessionkey,iv,FileNameHeader,fileEnc);
                String directory = null;

                //get neighbor data
                remoteSocAdd=  sock.getRemoteSocketAddress().toString().substring(1);
                clientIp= InetAddress.getByName(remoteSocAdd.split(":")[0].trim()).toString().replace("/", "");
                neighIpPort=  Neighbor.getNeighbor(clientIp);
                ip=neighIpPort.split(":")[0];
                port=neighIpPort.split(":")[1];
                name=ScaftMain.returnUserName(new Neighbor(ip,port));
                Neighbor neighbor= new Neighbor(ip,port,name);

                //the gcm decryption  is succed
                if (!Arrays.equals(fileDec,new  byte[0])){
                    JFrame parentFrame = new JFrame();
                    JFileChooser fc = new JFileChooser();
                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int returnVal = fc.showSaveDialog(parentFrame);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        directory  = fc.getSelectedFile().toString();

                        File file = new File(directory+"\\"+FileNameHeader);
                        if (file.exists()){//the file is already exist
                            int reply = JOptionPane.showConfirmDialog(null, FileNameHeader +" is already exist  do you want to overwrite the file", "save file", JOptionPane.YES_NO_OPTION);
                            if (reply == JOptionPane.NO_OPTION) {save=false;}
                        }
                        //write the file and save it
                        if (save){ Files.write(file.toPath(), fileDec);}

                        //stop receivng data from this port, print message that file arrive, send ack-succed back for sender, add to log file
                        Challenge.removeChhallenge(sessionkey);
                        Platform.runLater(()->{ScaftMain.stopInFile();});
                        Platform.runLater(()->{ScaftMain.printRceiveeMessage(neighbor,FileNameHeader);});
                        Platform.runLater(()->{ScaftMain.sendMessagetoallOnlineNeighbor(neighbor,Message.MESSAGE_ACK,Message.MESSAGE_ACK_FILE_SUCCEED +"-"+FileNameHeader); });
                        Log.addLine("RECEIVE", neighbor.getIp() + ":" + listener.getLocalPort()+"", neighbor.getName(), Message.MESSAGE_FILE, FileNameHeader, ivHex,"","");

                    }
                }
                //the gcm decryption  is failed
                else {
                    //stop receivng data from this port, print arrive failed message , send ack-failed back for sender, add to log file
                    Platform.runLater(()->{ScaftMain.stopInFile();});
                    Platform.runLater(()->{ScaftMain.printFailedRceiveeMessage(neighbor,FileNameHeader);});
                    Platform.runLater(()->{ScaftMain.sendMessagetoallOnlineNeighbor(neighbor,Message.MESSAGE_ACK,Message.MESSAGE_ACK_FILE_FAILED +"-"+FileNameHeader); });
                    Log.addDecreptionFault("RECEIVE",ip+":"+listener.getLocalPort()+"");

                }



                try { sock.close(); } catch (Exception ex) {}
            } catch (UnknownHostException e) {
                System.out.println(e.getMessage());
            } catch (SocketException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            return;
        }

        try {
            //rceive message [header,iv,contenet,hmac]
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            remoteSocAdd=  sock.getRemoteSocketAddress().toString().substring(1);
            clientIp= InetAddress.getByName(remoteSocAdd.split(":")[0].trim()).toString().replace("/", "");

            //decrept the message by iv
            IncomeString= in.readLine();
            splitIncomeString=IncomeString.split(" ");

            //generate hmac from incoming message
            iv=splitIncomeString[1];
            byte[] ivbytes =Helper.stringToBytes(iv);
            byte[] headerByte =Helper.stringToBytes(splitIncomeString[0]);
            byte[] msgByte =Helper.stringToBytes(splitIncomeString[2]);
            byte[] combined=Helper.concat(headerByte,ivbytes,msgByte);
            byte[] generatedHmacByte=Cryptography.createHmac(appSetting.getMAcPassword(),combined);
            String generatedHmacString=Helper.convertBytesToHex(generatedHmacByte);
            if(splitIncomeString.length==4) {
                hmacHex = splitIncomeString[3];
                //hmac validation
                if (hmacHex.equals(generatedHmacString)) {
                    validHmac = true;
                    valifFlag = "Valid";
                }
            }


            ivHex=Helper.convertBytesToHex(ivbytes);
            header=Cryptography.decryptMessageString(appSetting.getSharedPassword(),iv,splitIncomeString[0]) ;
            msg=Cryptography.decryptMessageString( appSetting.getSharedPassword(),iv,splitIncomeString[2]);

            String neighIpPort=  Neighbor.getNeighbor(clientIp);
            String ip;
            String port;
            String name;
            Neighbor neighbor;

            if(!validHmac){//bad hmac
                boolean isHelloOrHelloBack=(header.trim().equals(Message.MESSAGE_HELLO)||header.trim().equals(Message.MESSAGE_ACK_HELLO));
                ip=neighIpPort.split(":")[0];
                port=neighIpPort.split(":")[1];

                if(isHelloOrHelloBack){name=msg;}
                else{name=ScaftMain.returnUserName(new Neighbor(ip,port)).trim();}

                neighbor= new Neighbor(ip,port,name);

                if (header.trim().equals(Message.MESSAGE_HELLO)){
                    new OutThread(Message.MODE_MESSAGE_MODE,neighbor,Message.MESSAGE_ACK_HELLO,appSetting.getUserName()).start();
                }
                if (header.trim().equals(Message.MESSAGE_ACK_HELLO)){
                    if (!ScaftMain.checkSuspiciousNeighbors(neighbor)){  Platform.runLater(()->{ScaftMain.addSuspiciousNeighbors(neighbor); });}
                }
                if (ScaftMain.checkIfOnline(neighbor)){  Platform.runLater(()->{ScaftMain.removeOnlineNeighbor(neighbor); });}

                Log.addBadMessage("RECEIVE",ip+":"+port,name,header);
                try { in.close(); } catch (Exception ex) {}
                try { sock.close(); } catch (Exception ex) {}
                return;
            }

            //making action by the header
            switch (header.trim()){
                case Message.MESSAGE_HELLO://first message from sender
                    if(!neighIpPort.equals("")) {
                        ip=neighIpPort.split(":")[0];
                        port=neighIpPort.split(":")[1];
                        name=msg;
                        neighbor= new Neighbor(ip,port,name);
                        //     System.out.print("message from "+neighIpPort);
                        if (!ScaftMain.checkIfOnline(neighbor)){  Platform.runLater(()->{ScaftMain.addOnlineNeighbor(neighbor); });}
                        Log.addLine("RECEIVE",ip+":"+port," "+name,header,msg,ivHex,hmacHex,valifFlag);
                        new OutThread(Message.MODE_MESSAGE_MODE,new Neighbor(ip,port,name),Message.MESSAGE_ACK_HELLO,appSetting.getUserName()).start();
                    }
                    break;

                case Message.MESSAGE_ACK_HELLO://sender send ackhello back to me
                    ip=neighIpPort.split(":")[0];
                    port=neighIpPort.split(":")[1];
                    name=msg;
                    neighbor= new Neighbor(ip,port,name);
                    //   System.out.println("checkIfOnline "+ip+":"+port+name+"    "+ScaftMain.checkIfOnline(neighbor));
                    if (!ScaftMain.checkIfOnline(neighbor)){Platform.runLater(()->{ScaftMain.addOnlineNeighbor(neighbor); });}
                    Log.addLine("RECEIVE",ip+":"+port," "+name,Message.MESSAGE_HELLO,Message.MESSAGE_ACK_HELLO+"-"+name,ivHex,hmacHex,valifFlag);
                    break;

                case Message.MESSAGE_MESSAGE://message from sender and print it on chat
                    ip=neighIpPort.split(":")[0];
                    port=neighIpPort.split(":")[1];
                    name=ScaftMain.returnUserName(new Neighbor(ip,port));
                    neighbor= new Neighbor(ip,port,name);
                    if (ScaftMain.checkSuspiciousNeighbors(neighbor)){
                        Platform.runLater(()->{ScaftMain.removeSuspiciousNeighbors(neighbor); });// remove neighbor from  SuspiciousNeighbors list
                        Platform.runLater(()->{ScaftMain.addOnlineNeighbor(neighbor); });//add it back to noline neighbors
                    }
                    ScaftMain.printRceiveeMessage(neighbor,msg);
                    Log.addLine("RECEIVE",ip+":"+port,name,header,msg,ivHex,hmacHex,valifFlag);
                    break;

                case Message.MESSAGE_BYE://sender send bye message for remove it from online list
                    //  System.out.println("rec bye");
                    ip=neighIpPort.split(":")[0];
                    port=neighIpPort.split(":")[1];
                    name=msg;
                    neighbor= new Neighbor(ip,port,name);
                    Platform.runLater(()->{ScaftMain.removeOnlineNeighbor(neighbor); });
                    Log.addLine("RECEIVE",ip+":"+port,name,header,msg,ivHex,hmacHex,valifFlag);
                    break;

                case Message.MESSAGE_SENDFILE://sender want to send file
                    String headerBack;
                    String contentBack;
                    Boolean isChoose=false;
                    ip=neighIpPort.split(":")[0];
                    port=neighIpPort.split(":")[1];
                    name=ScaftMain.returnUserName(new Neighbor(ip,port));
                    String filename=msg;
                    Log.addLine("RECEIVE",ip+":"+port,name,header,msg,ivHex,hmacHex,valifFlag);

                    //yes no dialog for receiving file or not
                    int reply = JOptionPane.showConfirmDialog(null, name +" want to send "+" "+filename, "receive file", JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.YES_OPTION) {//generate port to send
                        headerBack=Message.MESSAGE_OK;
                        contentBack=String.valueOf(Helper.genRandomPort());
                        isChoose=true;
                    } else {
                        headerBack=Message.MESSAGE_NO;
                        contentBack=Message.MESSAGE_NO;
                        isChoose=true;
                    }

                    //choose yes or no for receing file
                    if (isChoose){

                        AppSetting appSetting= new AppSetting();
                        pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));

                        IvParameterSpec ivSend = Cryptography.generate16IV();
                        String ivString=Helper.bytesToString(ivSend.getIV());
                        String createdMessage=Message.createMessage(headerBack, appSetting.getSharedPassword(),appSetting.getMAcPassword(), ivSend, contentBack);
                        hmacHex=createdMessage.split(" ")[3];

                        //send back answer
                        //for yes [ok,iv,portTosend,hmac]
                        //for no [no,iv,no,hmac]
                        pw.write( createdMessage+ "\n");
                        pw.flush();
                        if (pw!=null){  pw.close();}
                        Log.addLine("SEND   ", ip + ":" + port, name, headerBack, contentBack, Helper.convertBytesToHex(Helper.stringToBytes(ivString)),hmacHex,"");


                        //open channel to start receiving the file if send Ok
                        if (  headerBack==Message.MESSAGE_OK){
                            Platform.runLater(()->{
                                ScaftMain.startInFile(Integer.parseInt(contentBack)); });
                        }}
                    break;

                case Message.MESSAGE_ACK://the file is arrive show message in chat the message arrive
                    String ackMsg=msg.split("-")[0];
                    String fileName=msg.split("-")[1];
                    ip=neighIpPort.split(":")[0];
                    port=neighIpPort.split(":")[1];
                    name=ScaftMain.returnUserName(new Neighbor(ip,port));
                    neighbor= new Neighbor(ip,port,name);
                    if(ackMsg.equals(Message.MESSAGE_ACK_FILE_SUCCEED)){ ScaftMain.printSendFile(neighbor,fileName);}//the file arrived successfully
                    else{ScaftMain.printFailedSendFile(neighbor,fileName);}//the file failed
                    Log.addLine("RECEIVE",ip+":"+port,name,header,msg,ivHex,hmacHex,valifFlag);
                    break;

                default:
                    ip=neighIpPort.split(":")[0];
                    port=neighIpPort.split(":")[1];
                    Log.addDecreptionFault("RECEIVE",ip+":"+port);
                    break;
            }
            try { in.close(); } catch (Exception ex) {}
            try { sock.close(); } catch (Exception ex) {}

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
