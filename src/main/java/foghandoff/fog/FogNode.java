package foghandoff.fog;

import java.net.*;
import java.io.*;
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

import static foghandoff.fog.FogMessages.AcceptMessage;
import static foghandoff.fog.FogMessages.JobMessage;

@Component()
@Getter
@Setter
@Slf4j
public class FogNode {
    private ServerSocket serverSocket;
    @Autowired
    private Predictor predictor;
    int lamportId;

    @Scope("prototype")
    // Runnable class to communicate with clients after connection
    private static class ClientHandler implements Runnable{
        private Socket clientSocket;
        private DataInputStream in;
        private DataOutputStream out;
    	private int lamportId;
    	private AtomicBoolean isProcessing;
    	private int currJobId;

    	/**
    	* Sends a AcceptMessage to the client to alert it of the job port and their assigned lamport ID
    	*/
    	protected void sendAcceptMessage() {
    		try {
    			var messageBuilder = AcceptMessage.newBuilder()
   					.setId()		/* TODO include this fog node's id */
    				.setAssignedId(this.lamportId);
    			byte[] messageBytes = messageBuilder.build().toByteArray();
    			this.out.writeInte(messageBytes.length);
    			this.out.write(messageBytes);
    		} catch(IOException e) {
    			e.printStackTrace();
    		}
    	}

        public ClientHandler(Socket clientSocket, int id){
            this.clientSocket = clientSocket;
            this.id = lamportId;
            this.isProcessing = new AtomicBoolean(false);
        }

        @Override
        public void run(){
        	try {
	            this.out = new DataOutputStream(clientSocket.getOutputStream());
	            this.in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

	            sendAcceptMessage();

	            // Read in next message from the socket as a byte array
	            while(true) {
		            int length = in.readInt();
		            byte[] response  = new byte[length];
		            in.readFully(response);
		            JobMessage msg = JobMessage.parseFrom(response)

		            /* TODO some action based off of the job type */
		            switch(msg.getType()) {
		            	// Kill Any Existing Job Threads Immediately and Grab their return value
		            	case KILL:
		            		// If there is a currently runnning job, kill it and send the results to the client
		            		if(isProcessing.get()) {
		            			var messageBuilder = ResultMessage.newBuilder()
		            				.setId() /* TODO */
		            				.setJobId(this.currJobId)
		            				.setTimeStep() /* TODO */
		            				.setAccelValues();
		            			byte[] resultMsg = messageBuilder.build().toByteArray();
		            			out.writeInt(resultMsg.length);
		            			out.write(resultMsg);
		            			return;
		            		}
		            		// Otherwise directly send an acknowledgement to the edge device and shut down
		            		else {
		            			out.sendInt(-1);
		            			return;
		            		}
		 
			           	// Create a new Job for processing and add its metadata
		            	case JOB:
		            		// Read in data for the job
		            		currJobId = msg.getJobId();
		            		/* TODO */

		            	default: throw new RuntimeException("Invalid Message Type");
		            }
		        }
	        } catch(IOException e) {
	        	// We assume that IOException means that the client is dead and clean up appropriately
	        	/* TODO */	
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
	        	ClientHandler handler = new ClientHandler(clientSocket, lamportId);
	        	lamportId = lamportId + 1;
	        } catch(IOException e) {
	        	e.printStackTrace();
	        }	
        }
    }

    public FogNode(@Value("${serverPort}")int port){
        ServerSocket serverSocket = new ServerSocket(port);
        lamportId = 0;
    }
}