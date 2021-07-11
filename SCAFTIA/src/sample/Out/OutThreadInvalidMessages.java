package sample.Out;

import sample.main.*;
import sun.font.DelegatingShape;

import javax.crypto.spec.IvParameterSpec;
import javax.lang.model.util.ElementScanner6;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class OutThreadInvalidMessages extends Thread {

    AppSetting appSetting;
    List<Neighbor> neighbors;
    String header;
    String msg;
    String password;
    String macPass;
    String mode;
    String invalidMsg;
    String customeFieldString;
    public OutThreadInvalidMessages(String mode ,Neighbor neighbor,String headerOrSessionKey,String msgOrFile ,String invalidMsg,String customeFieldString ){
        this.mode=mode;
        appSetting= new AppSetting();
        this.neighbors=new LinkedList<>();
        this.header=headerOrSessionKey;
        this.msg = msgOrFile;
        this.password =appSetting.getSharedPassword();
        this.macPass=appSetting.getMAcPassword();
        this.invalidMsg=invalidMsg;
        this.customeFieldString=customeFieldString;
        neighbors.add(neighbor);
    }



    @Override
    public void run() {

        Socket client = null;
        PrintWriter pw =  null;
        BufferedReader br = null;

        String filePathToSend=msg;
        String sessionKey=header;
        String portForFile;

        //here the app start to send file data [iv,filename,filedata]
        if (mode.equals(Message.MODE_FILE_MODE)){

            try {

                Neighbor neighbor=neighbors.get(0);
                InetAddress neighborAddress = InetAddress.getByName(neighbor.getIp());
                client = createClientSocket(neighborAddress,Integer.parseInt(neighbor.getPort())) ;

                //contact [iv,filename,filedata+tag]  encrypt
                OutputStream outsStream = client.getOutputStream();
                IvParameterSpec iv = Cryptography.generate12IV();
                byte[] fileByte=   Helper.readFileBytes(filePathToSend);
                byte[] dataTosend= Message.createFile(sessionKey,iv,Helper.getFileNameFromAllPath(filePathToSend),fileByte);
                int dataLength=dataTosend.length;

                //satrt sending data
                int count=0;
                byte[] fileChunk;
                while(count!=dataLength){
                    int size = 100;
                    if(dataLength - count >= size){count += size;}
                    else{size = (int)(dataLength - count); count = dataLength;}
                    fileChunk = new byte[size];
                    System.arraycopy(dataTosend,(count-size),fileChunk,0,size);
                    outsStream.write(fileChunk);
                }
                Log.addLine("SEND   ", neighbor.getIp() + ":" + neighbor.getPort(), neighbor.getName(), Message.MESSAGE_FILE, Helper.getFileNameFromAllPath(filePathToSend), Helper.convertBytesToHex(iv.getIV()),"","");

                //sending file is done
                if (client != null) {
                    client.close();
                    if (outsStream!=null){  outsStream.close();}
                }

            } catch (UnknownHostException e) {
                System.out.println(e.getMessage());
            } catch (SocketException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            return;
        }

        //trim file name if there is sending file
        if (header.equals(Message.MESSAGE_SENDFILE)){msg=Helper.getFileNameFromAllPath(msg); }


        //send message to list of neighbors
        //message[header,iv,content,hmac]
        if (!(neighbors==null)) {
            for (Neighbor n : neighbors) {

                try {
                    InetAddress neighborAddress = InetAddress.getByName(n.getIp());
                    client  = new Socket(neighborAddress, Integer.parseInt(n.getPort()));//createClientSocket(neighborAddress,Integer.parseInt(n.getPort())) ;
                    pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));

                    IvParameterSpec iv = Cryptography.generate16IV();
                    String ivString=Helper.bytesToString(iv.getIV());
                    String createdMessage=Message.createMessage(header, password,macPass, iv, msg);
                    String hmacHex=createdMessage.split(" ")[3];

                    pw.write( createdMessage+ "\n");
                    pw.flush();
                    Log.addLine("SEND   ", n.getIp() + ":" + n.getPort(), n.getName(), header, msg, Helper.convertBytesToHex(Helper.stringToBytes(ivString)),hmacHex,"");

                    //for sending file need port back to start sending
                    if (header.equals(Message.MESSAGE_SENDFILE)) {
                        try {
                            String IncomeString="";
                            String[] splitIncomeString;
                            String headerresponse="";
                            String ivresponse ="";
                            String valifFlag="Invalid";
                            Boolean validHmac=false;
                            String[] splitresponceFromServer;

                            br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            IncomeString= br.readLine();
                            splitIncomeString=IncomeString.split(" ");
                            ivresponse=splitIncomeString[1];

                            //generate hmac from incoming message
                            byte[] ivbytes =Helper.stringToBytes(ivresponse);
                            byte[] headerByte =Helper.stringToBytes(splitIncomeString[0]);
                            byte[] msgByte =Helper.stringToBytes(splitIncomeString[2]);
                            byte[] combined=Helper.concat(headerByte,ivbytes,msgByte);
                            byte[] generatedHmacByte=Cryptography.createHmac(appSetting.getMAcPassword(),combined);
                            String generatedHmacString=Helper.convertBytesToHex(generatedHmacByte);
                            headerresponse=Cryptography.decryptMessageString(appSetting.getSharedPassword(),ivresponse,splitIncomeString[0]) ;

                            if(splitIncomeString.length==4) {
                                hmacHex = splitIncomeString[3];
                                //hmac validation
                                if (hmacHex.equals(generatedHmacString)) {
                                    validHmac = true;
                                    valifFlag = "Valid";
                                }
                            }

                            if(!validHmac){//bad hmac
                                Log.addBadMessage("RECEIVE",n.getIp()+":"+n.getPort(),n.getName(),header);
                                try { br.close(); } catch (Exception ex) {}
                                try { pw.close(); } catch (Exception ex) {}
                                try { client.close(); } catch (Exception ex) {}
                                return;
                            }



                            portForFile=Cryptography.decryptMessageString( appSetting.getSharedPassword(),ivresponse,splitIncomeString[2]);

                            Log.addLine("RECEIVE", n.getIp() + ":" + n.getPort(), n.getName(), headerresponse, portForFile, Helper.convertBytesToHex(Helper.stringToBytes(ivresponse)),hmacHex,valifFlag);

                            if (headerresponse.equals(Message.MESSAGE_OK)){
                                byte[] header;
                                String headerDec;
                                String ivGcm;
                                String aadGcm;
                                byte[] outerMessageInc;
                                String outerMessageDec;
                                String[] splitOuterMessageDec;
                                sessionKey ="";
                                String nonce;
                                String receiver;
                                String token;
                                String inNonce;

                                Server server =Server.getServer();
                                String responceFromServer="";
                                String senderToServer=appSetting.getUserName().trim();
                                String receiverToServer=n.getName().trim();

                                //this is invalid msg  => (a) Invalid Server key request (Msg 3) - With an invalid requestor name (not the user id of the sender)
                                if (invalidMsg.equals(Message.INVAILED_SERVER_REQUESTOR_NAME)){
                                    if (!customeFieldString.equals("")){senderToServer=customeFieldString;}
                                    else {senderToServer="INVAILED_NAME";}
                                    }
                                //this is invalid msg  => (b) Invalid Server key request (Msg 3) - With an invalid receiver name (not the recipient the user chose)
                                if (invalidMsg.equals(Message.INVAILED_SERVER_RECEIVER_NAME)){
                                    if (!customeFieldString.equals("")){receiverToServer=customeFieldString;}
                                    else {receiverToServer="INVAILED_NAME";}

                                }
                                //sending message to server and get responce back
                                nonce=Cryptography.createNonce();
                                String msg=Message.createMessageToServer(senderToServer,receiverToServer.trim(),nonce);
                                OutThreadToServer outThreadToServer=  new OutThreadToServer(server,msg);
                                outThreadToServer.start();
                                outThreadToServer.join();
                                responceFromServer=outThreadToServer.getResponce();


                                splitresponceFromServer=responceFromServer.split(" ");

                                if (splitresponceFromServer.length==3){
                                    ivGcm=splitresponceFromServer[0];
                                    aadGcm=splitresponceFromServer[1];
                                    outerMessageInc=Helper.stringToBytes(splitresponceFromServer[2]) ;
                                    outerMessageDec=Cryptography.decryptMessageGCMString(appSetting.getNonSharedPassword(),ivGcm,aadGcm,outerMessageInc);

                                    if (outerMessageDec.equals("")){
                                        Log.addLine("RECEIVE",server.getIp() + ":" + server.getPort()," server",Message.MESSAGE_ACK_SESSION_KEY_REQUEST,"INVAILED_RESPONCE_WITH_WRONG_KEY",ivGcm,"","Invalid");
                                        ScaftMain.ShowDialog(Message.ERROR_SERVER,Message.ERROR_SERVER_INVALID_RESPONCE);
                                        try { br.close(); } catch (Exception ex) {}
                                        return;
                                    }
                                    splitOuterMessageDec=outerMessageDec.split(" ");

                                    sessionKey=new String (Helper.stringToBytes(splitOuterMessageDec[0]), StandardCharsets.UTF_8);
                                    inNonce=splitOuterMessageDec[1];
                                    receiver=splitOuterMessageDec[2];

                                    //this is invalid msg  => (c) Invalid token message (Msg 5) - Create and send a random token.
                                    if (invalidMsg.equals(Message.INVAILED_TOKEN)){
                                      token = Message.createToken(appSetting.getSharedPassword(), iv, aadGcm, Helper.concat(sessionKey.getBytes(), appSetting.getUserName().trim().getBytes()));
                                    }
                                    else {token=splitOuterMessageDec[3];}

                                    if (inNonce.equals(nonce)&&receiver.equals(n.getName().trim())){

                                        String firstChallengeMessage;
                                        String secondChallengeMessage;
                                        String[] firstChallengeMessageResponce;
                                        String[] secondChallengeMessageResponce;
                                        String secondChallengeMessageResponceDec;
//sending first challenge                to neighbor and get responce back
                                        firstChallengeMessage=Message.MESSAGE_CHALLENGE_SESSION+" "+ivGcm+" "+aadGcm +" "+token;
                                        OutThreadChallenge outThreadChallenge=  new OutThreadChallenge(new Neighbor(n.getIp(),portForFile,n.getName()), Message.MESSAGE_CHALLENGE_SESSION,token,firstChallengeMessage);
                                        outThreadChallenge.start();
                                        outThreadChallenge.join();

                                        firstChallengeMessageResponce=outThreadChallenge.getResult().split(" ");

                                        if (firstChallengeMessageResponce.length==4){
                                            byte[] nonceReceiver;
                                            byte[] okResponce;

                                            String nonceReceiverDec;
                                            header=Helper.stringToBytes(firstChallengeMessageResponce[0]);
                                            ivGcm=firstChallengeMessageResponce[1];
                                            aadGcm=firstChallengeMessageResponce[2];
                                            nonceReceiver=Helper.stringToBytes(firstChallengeMessageResponce[3]) ;
                                            headerDec=Cryptography.decryptMessageGCMString(sessionKey,ivGcm,aadGcm,header);

                                            if (headerDec.trim().equals(Message.MESSAGE_CHALLENGE_ACK_SESSION)) {

                                                nonceReceiverDec = Cryptography.decryptMessageGCMString(sessionKey, ivGcm, aadGcm, nonceReceiver);
                                                Log.addLine("RECEIVE",n.getIp()+":"+portForFile,n.getName(),headerDec,nonceReceiverDec,Helper.convertBytesToHex(Helper.stringToBytes(ivGcm)),"","Valid");

                                                if (!nonceReceiverDec.equals(Message.MESSAGE_NO)){
                                                    String sessionKeyTmp=sessionKey;
                                                    String noncechalenge = (Long.parseLong(nonceReceiverDec) - 1)+"";
                                                    iv = Cryptography.generate16IV();

                                                    //this is invalid msg  => (e) Invalid response message (Msg 7) - Send a response encrypted with the wrong key (not KS)
                                                    if (invalidMsg.equals(Message.INVAILED_RESPONCE_WRONG_KEY)){sessionKeyTmp="INVAILED_SESSION_KEY";}
                                                    //this is invalid msg  => (f) Invalid response message (Msg 7) - Send a response encrypted with the wrong numerical response (correct KS, but some value other than NB − 1 encrypted).
                                                    if (invalidMsg.equals(Message.INVAILED_RESPONCE_WRONGE_NONCE)){noncechalenge=Cryptography.createNonce();}

                                                    //sending second challenge  to neighbor and get responce back
                                                    secondChallengeMessage = Message.createChallengeMessage(sessionKeyTmp, iv, Message.MESSAGE_CHALLENGE_NONCE, appSetting.getUserName().trim(), noncechalenge );
                                                    OutThreadChallenge outThreadSecondChallenge = new OutThreadChallenge(new Neighbor(n.getIp(), portForFile, n.getName()),Message.MESSAGE_CHALLENGE_NONCE,noncechalenge,secondChallengeMessage);
                                                    outThreadSecondChallenge.start();
                                                    outThreadSecondChallenge.join();
                                                    secondChallengeMessageResponce = outThreadSecondChallenge.getResult().split(" ");

                                                    if (secondChallengeMessageResponce.length==4){
                                                        header = Helper.stringToBytes(secondChallengeMessageResponce[0]);
                                                        ivGcm = secondChallengeMessageResponce[1];
                                                        aadGcm = secondChallengeMessageResponce[2];
                                                        okResponce = Helper.stringToBytes(secondChallengeMessageResponce[3]);
                                                        headerDec = Cryptography.decryptMessageGCMString(sessionKey, ivGcm, aadGcm, header);

                                                        if (headerDec.trim().equals(Message.MESSAGE_CHALLENGE_ACK_NONCE)) {
                                                            secondChallengeMessageResponceDec = Cryptography.decryptMessageGCMString(sessionKey, ivGcm, aadGcm, okResponce);
                                                            Log.addLine("RECEIVE",n.getIp()+":"+portForFile,n.getName(),headerDec,secondChallengeMessageResponceDec,Helper.convertBytesToHex(Helper.stringToBytes(ivGcm)),"","Valid");
                                                            if (secondChallengeMessageResponceDec.equals(Message.MESSAGE_OK)) {

                                                                //this is invalid msg  =>    (g) Send the file (Msg 9) encrypted with the wrong key (not KS)
                                                                if (invalidMsg.equals(Message.INVAILED_ENCREPTED_FILE_WRONG_KEY)){sessionKey="INVAILED_SESSION_KEY";}
                                                                //everthing is ok start to send the file with session key
                                                                ScaftMain.startsendingFile(new Neighbor(n.getIp(), portForFile, n.getName()), sessionKey, filePathToSend);
                                                            }
                                                            else {//error chalenge reponce (msg 9)
                                                                Log.addError(n.getIp() + ":" + portForFile,n.getName(),Message.ERROR_NEIGHBOR,Message.ERROR_NEIGHBOR_NONCE_NOT_CORRECT);
                                                                ScaftMain.ShowDialog(Message.ERROR_NEIGHBOR,Message.ERROR_NEIGHBOR_NONCE_NOT_CORRECT);
                                                            }
                                                        }
                                                    }//end headerDec.trim().equals(Message.MESSAGE_CHALLENGE_ACK_NONCE
                                                    //error chalenge reponce (msg 9)
                                                    else { Log.addError(n.getIp() + ":" + portForFile,n.getName(),Message.ERROR_NEIGHBOR,Message.ERROR_NEIGHBOR_INVALID_RESPONCE);}

                                                }// end !nonceReceiverDec.equals(Message.MESSAGE_NO)

                                                else {///error chalenge reponce (msg 9)
                                                    Log.addError(n.getIp() + ":" + portForFile,n.getName(),Message.ERROR_NEIGHBOR,Message.ERROR_NEIGHBOR_NONCE_NOT_CORRECT);
                                                    ScaftMain.ShowDialog(Message.ERROR_NEIGHBOR,Message.ERROR_NEIGHBOR_NONCE_NOT_CORRECT);
                                                }

                                            }//end headerDec.trim().equals(Message.MESSAGE_CHALLENGE_ACK_SESSION)
                                            else {
                                                ///error chalenge reponce (msg 7)
                                                Log.addError(n.getIp() + ":" + portForFile,n.getName(),Message.ERROR_NEIGHBOR,Message.ERROR_NEIGHBOR_SESSION_KEY);
                                                ScaftMain.ShowDialog(Message.ERROR_NEIGHBOR,Message.ERROR_NEIGHBOR_SESSION_KEY);
                                            }

                                        }//end firstChallengeMessageResponce.length==4
                                        else {
                                            ///error chalenge reponce (msg 7)
                                            Log.addError(n.getIp() + ":" + portForFile,n.getName(),Message.ERROR_NEIGHBOR,Message.ERROR_NEIGHBOR_NONCE_NOT_CORRECT);
                                            ScaftMain.ShowDialog(Message.ERROR_NEIGHBOR,Message.ERROR_NEIGHBOR_NONCE_NOT_CORRECT);}

                                    }//end inNonce.equals(nonce)&&receiver.equals(n.getName().trim())
                                    else {
                                        ///error chalenge reponce (msg 7)
                                        Log.addLine("RECEIVE",server.getIp() + ":" + server.getPort()," server",Message.MESSAGE_ACK_SESSION_KEY_REQUEST,Message.MESSAGE_SESSION_KEY_REQUEST_USER_NOT_FOUND,"","","Valid");
                                        ScaftMain.ShowDialog(Message.ERROR_SERVER,Message.ERROR_SERVER_INVALID_RESPONCE);
                                    }

                                }//end if splitresponceFromServer.length==3
                                ///error server responce user not found
                                else if (Message.MESSAGE_SESSION_KEY_REQUEST_USER_NOT_FOUND.equals(responceFromServer.trim())){
                                    Log.addLine("RECEIVE",server.getIp() + ":" + server.getPort()," server",Message.MESSAGE_ACK_SESSION_KEY_REQUEST,Message.MESSAGE_SESSION_KEY_REQUEST_USER_NOT_FOUND,"","","Valid");
                                    ScaftMain.ShowDialog(Message.ERROR_SERVER,Message.ERROR_SERVER_INVALID_RESPONCE);
                                }
                                else if (responceFromServer.equals("")){
                                    ///error server responce
                                    Log.addError(server.getIp() + ":" + server.getPort(),"SERVER",Message.ERROR_SERVER,Message.ERROR_SERVER_UNAVAILABLE);
                                    ScaftMain.ShowDialog(Message.ERROR_SERVER,Message.ERROR_SERVER_UNAVAILABLE);
                                }
                                else {
                                    ///error server responce
                                    Log.addError(server.getIp() + ":" + server.getPort(),"SERVER",Message.ERROR_SERVER,Message.ERROR_SERVER_INVALID_RESPONCE);
                                    ScaftMain.ShowDialog(Message.ERROR_SERVER,Message.ERROR_SERVER_INVALID_RESPONCE);
                                }
                            }

                            if (headerresponse.equals(Message.MESSAGE_NO)){
                                ScaftMain.printRceiveeMessage(n,"[Not interested]");
                            }

                            try { br.close(); } catch (Exception ex) {}
                        }catch (SocketException ex){
                            System.out.println(ex.getMessage());} catch (InterruptedException e) {
                            System.out.println("join "+e.getMessage());
                        }


                    }

                    try { pw.close(); } catch (Exception ex) {}
                    try { client.close(); } catch (Exception ex) {}

                } catch (UnknownHostException e) {
                    System.out.println("UnknownHostException"+e.getMessage());
                } catch (SocketException e) {
                    System.out.println("SocketException "+e.getMessage());
                } catch (IOException e) {
                    System.out.println("SocketException "+e.getMessage());
                }
            }
        }
    }

    //try to create socket
    private Socket createClientSocket(InetAddress clientName, int port){

        boolean scanning = true;
        Socket socket = null;
        int numberOfTry = 0;

        while (scanning && numberOfTry < 1){
            numberOfTry++;
            try {
                socket = new Socket(clientName, port);
                scanning = false;
            } catch (IOException e) {
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException ie) {
//                    ie.printStackTrace();
//                }
            }

        }
        return socket;
    }
}
