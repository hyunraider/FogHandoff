package foghandoff.fog;

import java.net.*;
import java.io.*;

public class FogNode {
    private ServerSocket serverSocket;
    private Predictor predictor;

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

    public FogNode(@Value("${serverPort}")int port, @Value("${predictorType}")String predictorType){
        ServerSocket serverSocket = new ServerSocket(port);
        if(predictorType == "cloud"){
            predictor = new CloudPredictor();
        } else if (predictorType == "smart"){
            predictor = new SmartPredictor();
        } else if (predictorType == "dumb"){
            predictor = new DumbPredictor();
        }

        ListenerRunnable listener = new ListenerRunnable();
        Thread t = new Thread(listener).start();
    }
}