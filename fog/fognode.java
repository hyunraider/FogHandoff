package foghandoff.fog;

import java.net.*;
import java.io.*;

public class FogNode {
    private ServerSocket serverSocket;

    private static class Predictor {
        // TODO 
    }

    @Scope("prototype")
    // Runnable class to listen for new connections
    private static class ListenerRunnable implements Runnable{
        @Override
        public void run(){
            while(true){
                Socket clientSocket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(handler).start();
            }
        }
    }

    @Scope("prototype")
    // Runnable class to communicate with clients after connection
    private static class ClientHandler implements Runnable{
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket){
            this.clientSocket = clientSocket;
        }

        @Override
        public void run(){
            PrintWriter out = new PrintWriter(clientSocket.getOutStream(), true);
            Bufferedreader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while((inputLine = in.readLine()) != null){
                // TODO 
            }
        }

        @After
        public void tearDown(){
            clientSocket.stopConnection();
        }
    }

    public FogNode(int port){
        ServerSocket serverSocket = new ServerSocket(port);

        ListenerRunnable listener = new ListenerRunnable();
        Thread t = new Thread(listener).start();
    }
}