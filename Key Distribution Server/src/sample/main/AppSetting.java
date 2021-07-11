package sample.main;

import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;



public class AppSetting {


    private final String MY_PORT = "MyPort";

    enum  keys{ port,pass}
    Ini AppSetting;

    public AppSetting(){
        try {
            AppSetting= new Ini(new File("APP_SETTING.ini"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPort(String port){AppSetting.put(MY_PORT, keys.port.toString(),port);}

    public String getMyPort(){return AppSetting.get(MY_PORT, keys.port.toString()); }


    public void save(){
        try {
            AppSetting.store(AppSetting.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
