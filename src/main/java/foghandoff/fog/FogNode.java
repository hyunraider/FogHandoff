package foghandoff.fog;

import java.net.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
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
import static foghandoff.fog.FogMessages.AllocatedMessage;
import static foghandoff.fog.FogMessages.Velocity;
import static foghandoff.fog.FogMessages.CandidateNodes;
import static foghandoff.fog.FogMessages.Candidate;

@Component
@Getter
@Setter
@Slf4j
public class FogNode {
    private ServerSocket serverSocket;
    private Predictor predictor;
    @Autowired
    private MembershipList membershipList;
    private int lamPort;
    private int serverPort;
    private String fogId;
    private double longitude;
    private double latitude;

    @Scope("prototype")
    // Runnable class to communicate with clients after connection
    private static class ClientHandler implements Runnable{
        private Predictor predictor;
        private Socket clientSocket;
        private DataInputStream in;
        private DataOutputStream out;
    	private String edgeId;
        private int edgePort; // only relevant for prediction compoment
        private String fogId;
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
                this.out.flush();
    			this.out.write(messageBytes);
                System.out.println(messageBytes);
                System.out.println("Sent message back!");
                this.out.flush();
    		} catch(IOException e) {
    			e.printStackTrace();
    		}
    	}

        /**
        * In the case of a prepared component, we need to pre-establish our content and wait for a connection to come in on our job port
        */
        public ClientHandler(String id, int port, String fogId, Predictor predictor) {
            this.edgeId = id;
            this.active = false;
            this.edgePort = port;
            this.fogId = fogId;
            this.predictor = predictor;
        }

        /**
        * In the case of brand new connection with no preknowledge, we havee a socket already and we instantly become active
        */
        public ClientHandler(Socket clientSocket, String id, DataInputStream in, String fogId, Predictor predictor) throws IOException{
            this.clientSocket = clientSocket;
            this.edgeId = id;
            this.in = in;
            this.out = new DataOutputStream(clientSocket.getOutputStream());
            this.active = true;
            this.edgePort = -1;
            this.fogId = fogId;
            this.predictor = predictor;
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
                    ServerSocket serverSock = null;
                    try {
                        serverSock = new ServerSocket(this.edgePort);
                        serverSock.setSoTimeout(10000);
                        this.clientSocket = serverSock.accept();
                        this.in = new DataInputStream(new BufferedInputStream(this.clientSocket.getInputStream()));
                        this.out = new DataOutputStream(this.clientSocket.getOutputStream());
                        sendAcceptMessage();
                        this.active = true;
                    } catch(SocketTimeoutException e) {
                        // We timed out waiting for a predicted fog node. Kill ourselves...
                        if(serverSock != null) { serverSock.close(); }
                        return;
                    } catch(IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }

	            // Read in next message from the socket as a byte array
	            while(true) {
		            int length = in.readInt();
		            byte[] response  = new byte[length];
		            in.readFully(response);
		            TaskMessage msg = TaskMessage.parseFrom(response);

		            // Do some action based off of the message type
		            switch(msg.getType()) {
		            	// Kill Ourselves Immediately
		            	case KILL:
                            System.out.println("Received message to kill client handler of " + this.edgeId);
		            		out.writeInt(-1);
                            tearDown();
                            return;
			           	// Respond to a ping with some integer indicator
		            	case PING:
		            		out.writeInt(420);
                            break;
                        // Sending us Directional information
                        case INFO:
                            Velocity vel = msg.getVelocity();
                            List<String> candNodes = predictor.getCandidateNodes(vel.getLoc(), vel);

                            // Send out a message to the predicted nodes to allocate space
                            ArrayList<Candidate> candList = new ArrayList<>();
                            for(int i = 0; i < candNodes.size(); i++) {
                                var candBuilder = Candidate.newBuilder();
                                int port = requestAllocation(msg.getEdgeId(), candNodes.get(i));
                                candBuilder.setFogPort(port).setFogId(candNodes.get(i));
                                candList.add(candBuilder.build());
                            }

                            // Send a message back to the edge server
                            var msgBuilder = CandidateNodes.newBuilder()
                                .setExists(candNodes.size() == 0 ? 0 : 1)
                                .addAllCandidates(candList);
                            byte[] msgBytes = msgBuilder.build().toByteArray();
                            out.writeInt(msgBytes.length);
                            out.write(msgBytes);
                            
                            System.out.println("Got meta info:");
                            System.out.println("" + vel.getDeltaLatitude() + "," + vel.getDeltaLongitude());
                            break;
		            	default: throw new RuntimeException("Invalid Message Type");
		            }
		        }
	        } catch(Exception e) {
	        	// We assume that IOException means that the client is dead and clean up appropriately
	        	e.printStackTrace();
                System.out.println("ClienttHandler ran into trouble for client... " + this.edgeId);
                tearDown();
                return;
	        }
        }

        public void tearDown(){
            try {
            	this.out.close();
            	this.in.close();
                this.clientSocket.close();
            } catch(IOException e) {
                System.out.println("Error while tearing down structures...");
                e.printStackTrace();
            }
        }

        /**
        * Contact the other fog node to ask them to allocate bandwidtht for the edge. Wait for a job port assignment back
        * @param edgeId: string denoting the edge that we want to allocate bandwidtth for
        * @param fogId: string denoting hte fog node that we want to contact
        * @return port: the returned port, -1 for error
        */
        private int requestAllocation(String edgeId, String fogId) {
            try {
                Socket s = new Socket(InetAddress.getLocalHost(), Integer.parseInt(fogId));
                DataInputStream fIn = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                DataOutputStream fOut = new DataOutputStream(s.getOutputStream());

                // Send prepare requestt
                var msgBuilder = ConnectionMessage.newBuilder()
                    .setEdgeId(edgeId)
                    .setType(ConnectionMessage.OpType.PREPARE);
                byte[] msg = msgBuilder.build().toByteArray();
                out.writeInt(msg.length);
                out.write(msg);

                // Read back allocation request
                int length = in.readInt();
                byte[] response = new byte[length];
                in.readFully(response);
                AllocatedMessage allocMsg = AllocatedMessage.parseFrom(response);
                s.close();
                fIn.close();
                fOut.close();

                return allocMsg.getJobPort();
            } catch(IOException e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    /**
    * Start the server by waiting for connection requests and creating a ClientHandler thread
    */
    protected void startServer() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
            System.out.print("Listening for messages on: " + this.serverPort);
        } catch(IOException e) {
            e.printStackTrace();
        }

    	while(true) {
    		try {
	        	Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted Connection");
                DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

                int length = in.readInt();
                System.out.println(length);
                byte[] response = new byte[length];
                in.readFully(response);
                System.out.println("Hello");
                ConnectionMessage msg = ConnectionMessage.parseFrom(response);
                // Handle if it is a preparation request
                if(msg.getType() == ConnectionMessage.OpType.PREPARE) {
                    System.out.println("Received request to prepare bandwidth for " + msg.getEdgeId());
                    ClientHandler handler = new ClientHandler(msg.getEdgeId(), this.lamPort, this.fogId, this.predictor);
                    Thread handlerThread = new Thread(handler);
                    handlerThread.start();
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                    // Started our thread let the original fog node know
                    System.out.println("Allocated space on port " + this.lamPort);
                    byte[] msgBytes = AllocatedMessage.newBuilder().setEdgeId(msg.getEdgeId()).setJobPort(this.lamPort).build().toByteArray();
                    out.writeInt(msgBytes.length);
                    out.write(msgBytes);
                    this.lamPort = this.lamPort + 1;
                }
                // Handle if it is just a new connection request. Start up the client socket right away
                else if(msg.getType() == ConnectionMessage.OpType.NEW) {
                    System.out.println("Received request to connect from " + msg.getEdgeId());
    	        	ClientHandler handler = new ClientHandler(clientSocket, msg.getEdgeId(), in, this.fogId, this.predictor);
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

    // SHut up string
    public FogNode(){}
}
