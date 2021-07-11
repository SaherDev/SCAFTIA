package sample.In;

import java.io.IOException;
import java.net.ServerSocket;

public class InThread extends  Thread {
    private ServerSocket listen;
    private String mode;


    public InThread(ServerSocket listen,String mode ){
        this.listen=listen;
        this.mode=mode;
    }

    @Override
    public void run() {

        //accept sockets from senders and run clienthandler(socket)
        while (!this.isInterrupted()) {
            InClientHandler inClientHandler = null;

            try {
                inClientHandler = new InClientHandler(mode,listen.accept(),listen);
             //   System.out.println(clientHandler.sock);
                inClientHandler.start();
            } catch (IllegalStateException|IllegalArgumentException | IOException | SecurityException e) {
                if (this.isInterrupted()){  break;}
                System.out.println("Error accepting new connection: " + e.getMessage());
            }

        }
    }



}
