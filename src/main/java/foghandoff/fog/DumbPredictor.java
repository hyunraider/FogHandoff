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

import static foghandoff.fog.FogMessages.Location;
import static foghandoff.fog.FogMessages.Velocity;

@Component
@Getter
@Setter
@Slf4j
public class DumbPredictor extends Predictor {

	// Shut up spring
	public DumbPredictor() { super(); }

    public DumbPredictor(Location currentFogNode, HashMap<String, Location> nearbyFogNodes){
        super(currentFogNode, nearbyFogNodes);
    }
    
    public List<String> getCandidateNodes(Location currentLocation, Velocity v){
        // TODO 
        return new ArrayList<String>();
    }
}
