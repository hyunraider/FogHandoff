package foghandoff.fog;

import java.util.*;
import java.io.*; 
import java.lang.Math;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import static foghandoff.fog.FogMessages.Location;
import static foghandoff.fog.FogMessages.Velocity;

public class CloudPredictor extends Predictor {

	// Shut up spring
	public CloudPredictor() { super(); }

    public CloudPredictor(Location currentFogNode, HashMap<Integer, Location> nearbyFogNodes){
        super(currentFogNode, nearbyFogNodes);
    }

    public List<Integer> getCandidateNodes(Location currentLocation, Velocity v){
        // TODO 
        return new ArrayList<Integer>();
    }
}