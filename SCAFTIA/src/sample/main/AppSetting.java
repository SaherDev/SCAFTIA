package sample.main;

import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;


//this class for app setting [username, port, shared password]
public class AppSetting {

    private final String USERNAME = "UserName";
    private final String MY_PORT = "MyPort";
    private final String SHAREDPASSWORD = "SharedPassword";
    private final String NONSHAREDPASSWORD = "NonSharedPassword";
    private final String MACPASSWORD = "MacPassword";
    enum  keys{ name,port,pass}
    Ini AppSetting;

    public AppSetting(){
        try {
            AppSetting= new Ini(new File("APP_SETTING.ini"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    
    public void setUserName(String userName){ AppSetting.put(USERNAME, keys.name.toString(),userName);}

    public String getUserName(){    return AppSetting.get(USERNAME, keys.name.toString());}

    public void setPort(String port){AppSetting.put(MY_PORT, keys.port.toString(),port);}

    public String getMyPort(){return AppSetting.get(MY_PORT, keys.port.toString()); }

    public void setSharedPassword(String password){  AppSetting.put(SHAREDPASSWORD, keys.pass.toString(),password); }

    public String getSharedPassword(){return AppSetting.get(SHAREDPASSWORD, keys.pass.toString());  }

    public void setNonSharedPassword(String password){  AppSetting.put(NONSHAREDPASSWORD, keys.pass.toString(),password); }

    public String getNonSharedPassword(){return AppSetting.get(NONSHAREDPASSWORD, keys.pass.toString());  }

    public void setMacPassword(String password){  AppSetting.put(MACPASSWORD, keys.pass.toString(),password); }

    public String getMAcPassword(){return AppSetting.get(MACPASSWORD, keys.pass.toString());  }

    public void save(){
        try {
            AppSetting.store(AppSetting.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
