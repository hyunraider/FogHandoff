package foghandoff.fog;

import java.net.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import static foghandoff.fog.FogMessages.ConnectionMessage;
import static foghandoff.fog.FogMessages.AcceptMessage;
import static foghandoff.fog.FogMessages.TaskMessage;

@Component
@Getter
@Setter
@Slf4j
public class FogNode {
    private ServerSocket serverSocket;
    @Autowired
    private Predictor predictor;
    @Autowired
    private MembershipList membershipList;
    private int lamPort;
    @Value("${fogNodeId}")
    private int fogId;
    private double longitude;
    private double latitude;

    @Scope("prototype")
    // Runnable class to communicate with clients after connection
    private static class ClientHandler implements Runnable{
        private Socket clientSocket;
        private DataInputStream in;
        private DataOutputStream out;
    	private int edgeId;
        private int edgePort; // only relevant for prediction compoment
        private int fogId;
        private boolean active;

    	/**
    	* Sends a AcceptMessage to the client to alert it of the job port and their assigned lamport ID
    	*/
    	protected void sendAcceptMessage() {
    		try {
    			var messageBuilder = AcceptMessage.newBuilder()
   					.setFogId(this.fogId)
                    .setJobPort(edgePort);
    			byte[] messageBytes = messageBuilder.build().toByteArray();
    			this.out.writeInt(messageBytes.length);
    			this.out.write(messageBytes);
    		} catch(IOException e) {
    			e.printStackTrace();
    		}
    	}

        /**
        * In the case of a prepared component, we need to pre-establish our content and wait for a connection to come in on our job port
        */
        public ClientHandler(int id, int port) {
            this.edgeId = id;
            this.active = false;
            this.edgePort = port;
        }

        /**
        * In the case of brand new connection with no preknowledge, we havee a socket already and we instantly become active
        */
        public ClientHandler(Socket clientSocket, int id, DataInputStream in){
            this.clientSocket = clientSocket;
            this.edgeId = id;
            this.in = in;
            this.out = new DataOutputStream(clientSocket.getOutputStream());
            this.active = true;
            this.edgePort = -1;
        }

        @Override
        public void run(){
        	try {
                // Emulate authentication and overhead setup (just sleep for a bit)
                TimeUnit.SECONDS.sleep(3);

                // Case where we did not do a prediction and are just plugging straight ahead.
                if(this.active) {
                    sendAcceptMessage();
                }
                // Case where we did prediction and thus need to wait for a job task to come in
                else {
                    /* TODO for prediction */
                    sendAcceptMessage();
                    this.acive = true;
                }

	            // Read in next message from the socket as a byte array
	            while(true) {
		            int length = in.readInt();
		            byte[] response  = new byte[length];
		            in.readFully(response);
		            TaskMessage msg = TaskMessage.parseFrom(response)

		            // Do some action based off of the message type
		            switch(msg.getType()) {
		            	// Kill Ourselves Immediately
		            	case KILL:
		            		out.sendInt(-1);
                            return;
			           	// Respond to a ping with some integer indicator
		            	case PING:
		            		out.sendInt(420);
		            	default: throw new RuntimeException("Invalid Message Type");
		            }
		        }
	        } catch(IOException e) {
	        	// We assume that IOException means that the client is dead and clean up appropriately
	        	e.printStackTrace();
                System.out.println("ClienttHandler ran into trouble for client... " + this.edgeId);
                return;
	        }
        }

        @After
        public void tearDown(){
        	this.out.close();
        	this.in.close();
            this.clientSocket.close();
        }
    }

    /**
    * Start the server by waiting for connection requests and creating a ClientHandler thread
    */
    protected void startServer() {
    	while(true) {
    		try {
	        	Socket clientSocket = serverSocket.accept();
                DataInputStream in = new BufferedInputStream(clientSocket.getInputStream());

                int length = in.readInt();
                byte[] response = new byte[length];
                in.readFully(response);
                ConnectionMessage msg = ConnectionMessage.parseFrom(response);

                // Handle if it is a preparation request
                if(msg.getType() == PREPARE) {
                    /* TODO PREDICTION CASE */
                    this.lamPort = this.lamPort + 1;
                }
                // Handle if it is just a new connection request. Start up the client socket right away
                else if(msg.getType() == NEW) {
    	        	ClientHandler handler = new ClientHandler(clientSocket, msg.getEdgeId(), in);
                    Thread handlerThread = new Thread(handler);
                    handlerThread.start();
                }
                // Invalid message type
                else {
                    throw new RuntimeException("Invalid Message Type");
                }
	        } catch(IOException e) {
	        	e.printStackTrace();
	        }	
        }
    }

    public FogNode(@Value("${serverPort}")int port, @Value("$serverLat")double latitude, @Value("serverLong")double longitude
            @Value("${basePort}")int basePort) {
        ServerSocket serverSocket = new ServerSocket(port);
        this.latitude = latitude;
        this.longitude = longitude;
        this.lamPort = basePort;
    }
}