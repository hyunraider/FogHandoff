package foghandoff.fog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spring.SpringConfig;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import java.io.FileReader;

@Slf4j
public class Main {
	
	// Persistent Components for our Fog Node
	private static FogNode fogNode;
	private static MembershipList membershipList;
	private static Predictor predictor;

	/**
	* Read in topology of the fog network from a YAML config file
	*/
	private static void readInTopo(String hostId) {
		JSONParser parser = new JSONParser();
		try {
			JSONArray jsonArr = (JSONArray) parser.parse(new FileReader("../../resources/fogTopo.json"));
			// Loop through list of fog nodes
			for(Object o : jsonArr) {
				JSONObject node = (JSONObject) o;
				String nodeId = (String)node.get("nodeId");
				Double longitude = (Double)node.get("longitude");
				Double latitude = (Double)node.get("latitude");

				// Create a member and add it to the membership list if it is not us
				if(!nodeId.equals(hostId)) {
					membershipList.add(new Member(nodeId, longitude, latitude));
				}
			}
		} catch(Exception e) {
			System.out.println("Failed too parse the topology file...");
			e.printStackTrace();
		}
	}

	/**
	* Run as java Main <int fogId> 
	*/
	public static void main(String[] args) throws Exception {
		final ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);

		fogNode = ctx.getBean(FogNode.class);
		fogNode.setFogId(args[0]);

		membershipList = ctx.getBean(MembershipList.class);

		final String predType = ctx.getEnvironment().getProperty("predictorType");
		if(predType == "cloud"){
            predictor = ctx.getBean(CloudPredictor.class);
        } else if (predType == "smart"){
            predictor = ctx.getBean(SmartPredictor.class);
        } else {
        	predictor = ctx.getBean(DumbPredictor.class);
        }

        // Initialize our topology
        readInTopo(args[0]);

        // Start the fog node
		fogNode.startServer();
	}
}