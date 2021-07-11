package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.main.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("SCAFTIA");
        primaryStage.getIcons().add(new Image("file:./ScaftiaIcon.png"));
        primaryStage.setScene(new Scene(root, 856  , 617));
        primaryStage.show();

        // close event
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            ScaftMain scaftMain;
            if(!ScaftMain.isMessageServerClosedOrNull()){
                scaftMain=new ScaftMain();
                scaftMain.SendtOutBye();
                scaftMain.stopIn();
            }
            if(!ScaftMain.isFileServerClosedOrNull()){
                scaftMain=new ScaftMain();
                scaftMain.stopInFile();
            }

        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
