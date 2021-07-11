package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.main.*;
import sample.main.Neighbor;
import sample.main.ScaftMain;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import sample.main.Validation;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Controller {

    public ListView lvOnlineNeighbors;
    public Button btnSendFile;
    public TextArea taChat;
    public TextArea taMessage;
    public TextField txNeighborIp;
    public TextField tfPort;
    public TextField tfServerIp;
    public TextField tfServerPort;
    public TextField tfNeighborPort;
    public Button btnAddNeighbor;
    public ListView lvnNighbousList;
    public Button btnRemove;
    public TextField tfUserName;
    public TextField tfMacPassword;
    public TextField tfSharedPassword;
    public TextField tfNonSharedPassword;
    public Button btnEdit;
    public Button btnSave;
    public Button btnConnect;
    public Button btnSendIvalidMessage;
    public ComboBox cbInvalidMessages;
    public ComboBox cbCstmInvalidMessages;
    public Button btnSendCustomeIvalidMessage;
    public TextField tfCustomeFieldString;

    private List<String> suspiciousNeighbors;
    private AppSetting appSetting;
    private ScaftMain scaftMain;
    private Boolean notReady =false;

    public void initialize(){
        ObservableList<String> invalidMessagesFunctions = FXCollections.observableArrayList(Message.INVAILED_SERVER_REQUESTOR_NAME, Message.INVAILED_SERVER_RECEIVER_NAME , Message.INVAILED_TOKEN,Message.INVAILED_CHALLENGE,Message.INVAILED_RESPONCE_WRONG_KEY,Message.INVAILED_RESPONCE_WRONGE_NONCE,Message.INVAILED_ENCREPTED_FILE_WRONG_KEY);
        cbInvalidMessages.setItems(invalidMessagesFunctions);
        ObservableList<String> invalidCustomeMessages = FXCollections.observableArrayList(Message.INVAILED_SERVER_REQUESTOR_NAME, Message.INVAILED_SERVER_RECEIVER_NAME );
        cbCstmInvalidMessages.setItems(invalidCustomeMessages);
        appSetting = new AppSetting();
        scaftMain=new ScaftMain(this);
        settingSetDisable(true);
        SendingSetDisable(true);
        getSettingData();
        updateNeighborsList();
        suspiciousNeighbors= new LinkedList<>();
        taChat.clear();
    }

    //send file event
    public void sendFile(ActionEvent actionEvent) {

        try{
            if (lvnNighbousList.getItems().size()==0) return;

            int index=lvOnlineNeighbors.getSelectionModel().getSelectedIndex();
            String neighbor=lvOnlineNeighbors.getItems().get(index).toString();
            String ip=neighbor.split(":")[0];
            String port =neighbor.split(":")[1].split("  ")[0];
          //  System.out.println("port is "+port);
            String name =neighbor.split(":")[1].split("  ")[1];

            Stage theStage = null;
            FileChooser  fileChooser = new FileChooser();
            fileChooser.setTitle("Choose File To send");
            fileChooser.setInitialDirectory(new java.io.File("."));
            File chosenFile = fileChooser.showOpenDialog(theStage);

            if ( chosenFile != null){ scaftMain.sendMessagetoallOnlineNeighbor(new Neighbor(ip,port,name),Message.MESSAGE_SENDFILE,chosenFile.toString());}


        }
        catch (IndexOutOfBoundsException ex){
            lvOnlineNeighbors.requestFocus();
        }

    }

    //send message event
    public void sendMessage(KeyEvent e) {
        if(e.getCode().equals(KeyCode.ENTER)){
            String msg=taMessage.getText().replace("\n","");
            scaftMain.sendMessagetoallOnline(getOnlineNeighbors(),msg);
            scaftMain.sendMessagetoallOnline(getSuspiciousNeighbor(),msg);
            printSendeMessage(msg);
            taMessage.clear();


        }

    }

    //add neighbor to list
    public void addNeighbor(ActionEvent actionEvent) {
        String ip=txNeighborIp.getText();
        String port=tfNeighborPort.getText();
        if (!Validation.validIP(ip)) {
            txNeighborIp.requestFocus();
            return;
        }
        if (!Validation.validPort(port)) {
            tfNeighborPort.requestFocus();
            return;
        }

        Neighbor.addNeighbor(ip,port);
        lvnNighbousList.getItems().add(ip+":"+port);
        txNeighborIp.clear();tfNeighborPort.clear();
    }

    //remove neighbor
    public void RemoveNeighbor(ActionEvent actionEvent) {
        try{
            int inddex=lvnNighbousList.getSelectionModel().getSelectedIndex();
            String[] neighbor=lvnNighbousList.getItems().get(inddex).toString().split(":");
            Neighbor.removeNeighbor(neighbor[0],neighbor[1]);

            lvnNighbousList.getItems().remove(inddex);}
        catch (ArrayIndexOutOfBoundsException ex){

        }
    }

    //connect and disconnect
    public void connect(ActionEvent actionEvent) {

        if(btnConnect.getText().equals("connect")) {
            if (!Validation.validPort(tfPort.getText())) { tfPort.requestFocus(); return;}
            if (!Validation.validuser(tfUserName.getText())){ tfUserName.requestFocus(); return;}
            if (notReady){ btnEdit.requestFocus();return;}
            SendingSetDisable(false);
            setSettingData();
            btnConnect.setText("disconnect");
            scaftMain.SendtOutHello();
            scaftMain.startIn(Integer.parseInt(appSetting.getMyPort()));
        }
        else {
            SendingSetDisable(true);
            btnConnect.setText("connect");
            scaftMain.SendtOutBye();
            scaftMain.stopIn();
            clearOnlinelist();
            clearSuspiciousNeighbors();

        }
    }

    public void EditSetting(ActionEvent actionEvent) { settingSetDisable(false);}

    public void saveSetting(ActionEvent actionEvent) {setSettingData();}


    //get neighbors data from Neighbors.txt
    private void updateNeighborsList(){

        List<String> neighborsList=Neighbor.getAllNeighborsStringList();
        lvnNighbousList.getItems().clear();

        for(String n:neighborsList){
            lvnNighbousList.getItems().add(n);
        }

    }

    //get neighbors data from APP_SETTING.ini
    private void getSettingData(){
        try {
            tfUserName.setText(appSetting.getUserName());
            tfSharedPassword.setText(appSetting.getSharedPassword());
            tfNonSharedPassword.setText(appSetting.getNonSharedPassword());
            tfMacPassword.setText(appSetting.getMAcPassword());
            tfPort.setText(appSetting.getMyPort());
            tfServerIp.setText(Server.getServer().getIp());
            tfServerPort.setText(Server.getServer().getPort());
            if (appSetting.getUserName().equals("")||appSetting.getMyPort().equals("")||appSetting.getSharedPassword().equals("")||appSetting.getMAcPassword().equals("")
             ||appSetting.getNonSharedPassword().equals("")){   notReady =true;}
        }catch (Exception ex){System.out.print(ex.getMessage());}

    }

    //save setting data
    private void setSettingData(){

        if (!Validation.validPort(tfPort.getText())) { tfPort.requestFocus(); return;}
        if (!Validation.validuser(tfUserName.getText())){ tfUserName.requestFocus(); return;}

        if (!Validation.validIP(tfServerIp.getText())) {tfServerIp.requestFocus();  return;   }
        if (!Validation.validPort(tfServerPort.getText())) { tfServerPort.requestFocus(); return;}

        appSetting.setUserName(tfUserName.getText());
        appSetting.setSharedPassword(tfSharedPassword.getText());
        appSetting.setNonSharedPassword(tfNonSharedPassword.getText());
        appSetting.setMacPassword(tfMacPassword.getText());
        appSetting.setPort(tfPort.getText());
        Server.updateServer(tfServerIp.getText(),tfServerPort.getText());
        //save server data

        appSetting.save();
        settingSetDisable(true);
    }

    private void settingSetDisable(boolean value){
        if(btnConnect.getText().equals("connect")){
            tfUserName.setDisable(value);
            tfPort.setDisable(value);
        }
        tfSharedPassword.setDisable(value);
        tfNonSharedPassword.setDisable(value);
        tfMacPassword.setDisable(value);
        tfServerIp.setDisable(value);
        tfServerPort.setDisable(value);
        btnSave.setDisable(value);
        btnEdit.setDisable(!value);
    }

    private void SendingSetDisable(boolean value){
        btnSendFile.setDisable(value);
        taMessage.setDisable(value);
        tfUserName.setDisable(!value);
        tfPort.setDisable(!value);
        //btnEdit.setDisable(!value);
    }


    public void printRceiveeMessage(String from,String msg){
        String messagePrint=from +"  >    "+msg;
        String emptyright="              ";
        int size=61;
        List<String> splited =Helper.splitEqually(messagePrint,size);
        for (int index = 0; index < splited.size() ; index++) {  taChat.appendText(  splited.get(index) +emptyright+"\n");}

    }


    public void printSendeMessage(String msg){
        String emptyLeft="                    ";
        int size=61;
        List<String> splited =Helper.splitEqually(msg,size);
        for (int index = 0; index < splited.size() ; index++) {  taChat.appendText(emptyLeft + splited.get(index)+ "\n" );}

    }



    ///////////////   Suspicious   Neighbor


    public List<Neighbor> getSuspiciousNeighbor() {
        int size=suspiciousNeighbors.size();
        List<Neighbor> neighbors= new LinkedList<>();
        for(int index=0;index<size;index++){
            String neighbor=suspiciousNeighbors.get(index);
            String ip=neighbor.split(":")[0];
            String port =neighbor.split(":")[1].split("  ")[0];
            String name =neighbor.split(":")[1].split("  ")[1];
            neighbors.add(new Neighbor(ip,port,name));
        }
        return   neighbors;
    }

    public void addSuspiciousNeighbor(Neighbor neighbor) {
        suspiciousNeighbors.add(neighbor.toString());
    }

    public void removeSuspiciousNeighbor(Neighbor neighbor) {
        int size=suspiciousNeighbors.size();
        for(int index=0;index<size;index++){
            try{
          if(  suspiciousNeighbors.get(index).trim().equals(neighbor.toString())){ suspiciousNeighbors.remove(index);}
            }catch (Exception ex){System.out.println(ex.getMessage());}

        }
    }

    public void clearSuspiciousNeighbors() {
        suspiciousNeighbors.clear();
    }

    public Boolean checkSuspiciousNeighbor(Neighbor neighbor) {
        int size=suspiciousNeighbors.size();
        for(int index=0;index<size;index++){
            if(  suspiciousNeighbors.get(index).trim().equals(neighbor.toString())){ return true;}
        }
        return false;
    }





//////////////////////  online neighbors


    //get list of online neighbors
    public List<Neighbor> getOnlineNeighbors() {
        int size=lvOnlineNeighbors.getItems().size();
        List<Neighbor> neighbors= new LinkedList<>();
        for(int index=0;index<size;index++){
            String neighbor=lvOnlineNeighbors.getItems().get(index).toString();
            String ip=neighbor.split(":")[0];
            String port =neighbor.split(":")[1].split("  ")[0];
            String name =neighbor.split(":")[1].split("  ")[1];
            neighbors.add(new Neighbor(ip,port,name));
        }
        return   neighbors;
    }


    public void addOnlineNeigfhbor(Neighbor neighbor) {
        lvOnlineNeighbors.getItems().add(neighbor.toString());
    }

    public void removeOnlineNeigfhbor(Neighbor neighbor) {
        int size=lvOnlineNeighbors.getItems().size();
        for(int index=0;index<size;index++){
            try{
                if(  lvOnlineNeighbors.getItems().get(index).toString().trim().equals(neighbor.toString())){ lvOnlineNeighbors.getItems().remove(index);}
            }catch (Exception ex){System.out.println(ex.getMessage());}

        }
    }

    public void clearOnlinelist() {
        lvOnlineNeighbors.getItems().clear();
    }

    public Boolean checkIfOnline(Neighbor neighbor) {
        int size=lvOnlineNeighbors.getItems().size();
        for(int index=0;index<size;index++){
            if(  lvOnlineNeighbors.getItems().get(index).toString().trim().equals(neighbor.toString())){ return true;}
        }
        return false;
    }

    //send custome invalid messages (messages a > g)
    public void SendIvalidMessage(ActionEvent actionEvent) {

        try{
            if (lvnNighbousList.getItems().size()==0) return;

            int index=lvOnlineNeighbors.getSelectionModel().getSelectedIndex();
            String neighbor=lvOnlineNeighbors.getItems().get(index).toString();
            String ip=neighbor.split(":")[0];
            String port =neighbor.split(":")[1].split("  ")[0];
            //  System.out.println("port is "+port);
            String name =neighbor.split(":")[1].split("  ")[1];

            Stage theStage = null;
            FileChooser  fileChooser = new FileChooser();
            fileChooser.setTitle("Choose File To send");
            fileChooser.setInitialDirectory(new java.io.File("."));
            File chosenFile = fileChooser.showOpenDialog(theStage);

            if ( chosenFile != null){
                String invalidMessage="";
                int indexcb=cbInvalidMessages.getSelectionModel().getSelectedIndex();
                if (indexcb!=-1){
                    invalidMessage=cbInvalidMessages.getSelectionModel().getSelectedItem().toString();
                    scaftMain.sendInvailedMessagetoallOnlineNeighbor(new Neighbor(ip,port,name),Message.MESSAGE_SENDFILE,chosenFile.toString(),invalidMessage,"");
                }
                else {cbInvalidMessages.requestFocus();}
                }

        }
        catch (IndexOutOfBoundsException ex){
            lvOnlineNeighbors.requestFocus();
        }

    }

    //send custome invalid sender or receiver name
    public void SendCustomeIvalidMessage(ActionEvent actionEvent) {
        try{
            if (lvnNighbousList.getItems().size()==0) return;
            if (tfCustomeFieldString.getText().equals("")) {tfCustomeFieldString.requestFocus();return;}

            int index=lvOnlineNeighbors.getSelectionModel().getSelectedIndex();
            String neighbor=lvOnlineNeighbors.getItems().get(index).toString();
            String ip=neighbor.split(":")[0];
            String port =neighbor.split(":")[1].split("  ")[0];
            //  System.out.println("port is "+port);
            String name =neighbor.split(":")[1].split("  ")[1];

            Stage theStage = null;
            FileChooser  fileChooser = new FileChooser();
            fileChooser.setTitle("Choose File To send");
            fileChooser.setInitialDirectory(new java.io.File("."));
            File chosenFile = fileChooser.showOpenDialog(theStage);

            if ( chosenFile != null){
                String invalidMessage="";
                int indexcb=cbCstmInvalidMessages.getSelectionModel().getSelectedIndex();
                if (indexcb!=-1){
                    invalidMessage = cbCstmInvalidMessages.getSelectionModel().getSelectedItem().toString();
                    scaftMain.sendInvailedMessagetoallOnlineNeighbor(new Neighbor(ip, port, name), Message.MESSAGE_SENDFILE, chosenFile.toString(), invalidMessage, tfCustomeFieldString.getText());
                    tfCustomeFieldString.clear();
                }
                else {cbCstmInvalidMessages.requestFocus();}
            }

        }
        catch (IndexOutOfBoundsException ex){
            lvOnlineNeighbors.requestFocus();
        }
    }
}
