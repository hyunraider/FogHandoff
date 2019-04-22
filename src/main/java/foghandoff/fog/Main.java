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

import static foghandoff.fog.FogMessages.Location;

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
			JSONArray jsonArr = (JSONArray) parser.parse(new FileReader("../src/main/resources/fogTopo.json"));
			// Loop through list of fog nodes
			for(Object o : jsonArr) {
				JSONObject node = (JSONObject) o;
				String nodeId = (String)node.get("nodeId");
				double longitude = Double.parseDouble((String)node.get("longitude"));
				double latitude = Double.parseDouble((String)node.get("latitude"));

				// Create a member and add it to the membership list if it is not us
				if(!nodeId.equals(hostId)) {
					Location loc = Location.newBuilder().setLongitude(longitude).setLatitude(latitude).build();
					membershipList.add(new Member(nodeId, loc));
					predictor.setCurrentFogNode(loc);
				} else {
					fogNode.setLongitude(longitude);
					fogNode.setLatitude(latitude);
				}
			}
			// Set the predictor's list of membership nodes
			predictor.setNearbyFogNodes(membershipList.getAllEntriesMap());
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

		membershipList = ctx.getBean(MembershipList.class);

		final String predType = ctx.getEnvironment().getProperty("predictorType");
		if(predType == "cloud"){
            predictor = new CloudPredictor();
        } else if (predType == "smart"){
            predictor = new SmartPredictor();
        } else {
        	predictor = new DumbPredictor();
        }

		fogNode = ctx.getBean(FogNode.class);
		fogNode.setFogId(args[0]);
		fogNode.setLamPort(Integer.parseInt(args[0]) + 1);
		fogNode.setServerPort(Integer.parseInt(args[0]));
		fogNode.setPredictor(predictor);

        // Initialize our topology
        readInTopo(args[0]);

        // Start the fog node
		fogNode.startServer();
	}
}