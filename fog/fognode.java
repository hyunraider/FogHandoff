package foghandoff.fog;

import com.beust.jcommander.Parameter;

public class FogNode {
    private int port;

    private static class Predictor {
        // TODO 
    }

    @Scope("prototype")
    // Runnable class to listen for new connections
    private static class ListenerRunnable implements Runnable{
        public MyRunnable(){
            
        }

        @Override
        public void run(){

        }
    }

    @Scope("prototype")
    // Runnable class to communicate with clients after connection
    private static class ServerRunnable implements Runnable{
        public MyRunnable(Object parameter){
        
        }

        @Override
        public void run(){

        }
    }

    public FogNode(int port){
        this.port = port;
    }
}