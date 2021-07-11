package sample.In;

import java.io.IOException;
import java.net.ServerSocket;

public class InThread extends  Thread {
    private ServerSocket listen;


    public InThread(ServerSocket listen ){
        this.listen=listen;
    }

    @Override
    public void run() {

        //accept sockets from senders and run clienthandler(socket)
        while (!this.isInterrupted()) {
            InClientHandler inClientHandler = null;

            try {
                inClientHandler = new InClientHandler(listen.accept());

                inClientHandler.start();
            } catch (IllegalStateException|IllegalArgumentException | IOException | SecurityException e) {
                if (this.isInterrupted()){  break;}
                System.out.println("Error accepting new connection: " + e.getMessage());
            }

        }
    }



}
