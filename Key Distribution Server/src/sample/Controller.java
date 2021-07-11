package sample;

import com.sun.security.ntlm.Server;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import sample.main.*;

import java.util.List;

public class Controller {


    public TextArea taChat;

    public TextField tfPort;

    public TextField txUserName;
    public TextField tfUserPassword;
    public Button btnAddUser;

    public Button btnStart;
    public ComboBox cbInvalidMessages;
    public Button btnInvalidMsgs;

    private AppSetting appSetting;
    private ServerMain serverMain;

    public void initialize(){
        ObservableList<String> invalidMessagesFunctions = FXCollections.observableArrayList(Message.INVAILED_NONCE,Message.INVAILED_TARGET_NAME,Message.INVAILED_REQUESTOR_KEY,Message.INVAILED_TARGET_KEY);
        cbInvalidMessages.setItems(invalidMessagesFunctions);
        appSetting = new AppSetting();
        serverMain= new ServerMain(this);
        getSettingData();

        taChat.clear();
    }

    public void addUser(ActionEvent actionEvent) {
        String name=txUserName.getText();
        String password=tfUserPassword.getText();
        User.addUser(name,password);
        txUserName.clear();txUserName.clear();
    }



    public void strat(ActionEvent actionEvent) {

        if(btnStart.getText().equals("start")) {
            if (!Validation.validPort(tfPort.getText())) { tfPort.requestFocus(); return;}
            setSettingData();
            btnStart.setText("stop");
            serverMain.startIn(Integer.parseInt(appSetting.getMyPort()));
            tfPort.setDisable(true);
        }
        else {
            btnStart.setText("start");
            serverMain.stopIn();
            tfPort.setDisable(false);

        }

    }

    //get neighbors data from APP_SETTING.ini
    private void getSettingData(){
        try {
            tfPort.setText(appSetting.getMyPort());

        }catch (Exception ex){System.out.print(ex.getMessage());}

    }

    //save setting data
    private void setSettingData(){

        if (!Validation.validPort(tfPort.getText())) { tfPort.requestFocus(); return;}

        appSetting.setPort(tfPort.getText());
        appSetting.save();

    }

    public void printMessage(String msg){
           taChat.appendText(msg);
    }

    public void enableIvalidMessage(ActionEvent actionEvent) {
        if(btnInvalidMsgs.getText().equals("enable")) {

            String invalidMessage="";
            int indexcb=cbInvalidMessages.getSelectionModel().getSelectedIndex();
            if (indexcb!=-1){
                invalidMessage=cbInvalidMessages.getSelectionModel().getSelectedItem().toString();
                ServerMain.setInvalidResponce(invalidMessage);
                btnInvalidMsgs.setText("disable");
                cbInvalidMessages.setDisable(true);
            }
            else {cbInvalidMessages.requestFocus();}

        }
        else {
            ServerMain.setInvalidResponce(Message.NOT_INVAILED);
            btnInvalidMsgs.setText("enable");
            cbInvalidMessages.setDisable(false);
        }

    }
}

