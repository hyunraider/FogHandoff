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
public abstract class Predictor {
    public Map<String, Location> nearbyFogNodes; // Map between Fog ID and Fog node location.
    public Location currentFogNode;

    // Shut up spring
    public Predictor() {}

    public Predictor(Location currentFogNode, HashMap<String, Location> nearbyFogNodes){
        this.nearbyFogNodes = nearbyFogNodes;
        this.currentFogNode = currentFogNode;
    }

    // Returns an array of candidate fog node IDs
    public abstract List<String> getCandidateNodes(Location currentLocation, Velocity v);

    // Helper functions
    public double distanceBetween(Location loc1, Location loc2){
        double longitude = Math.abs(loc1.getLongitude() - loc2.getLongitude());
        double latitude = Math.abs(loc1.getLatitude() - loc2.getLatitude());
        return Math.hypot(longitude, latitude);
    }

    public Boolean inCorrectDirection(Location currentLocation, Location destination, Velocity v){
        double slope = (destination.getLongitude() - currentLocation.getLongitude())/(destination.getLatitude() - destination.getLatitude());
        return (slope <= ((v.getDeltaLongitude()/v.getDeltaLatitude()) + 0.01)) || (slope >= ((v.getDeltaLongitude()/v.getDeltaLatitude()) - 0.01));
    }

    public int getNearestNeighbor(Location target){
        double closestDist = -1.0;
        int closestNode = -1;
        for(Map.Entry<String, Location> entry : nearbyFogNodes.entrySet()){
            double dist = distanceBetween(entry.getValue(), target);
            if(closestDist < 0 || dist < closestDist){
                closestDist = dist;
                closestNode = entry.getKey();
            }
        }
        return closestNode;
    }
}
