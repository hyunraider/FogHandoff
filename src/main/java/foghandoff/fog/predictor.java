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

public abstract class Predictor {
    public Map<Integer, Location> nearbyFogNodes; // Map between Fog ID and Fog node location.
    public Location currentFogNode;

    public Predictor(Location currentFogNode, HashMap<Integer, Location> nearbyFogNodes){
        this.nearbyFogNodes = nearbyFogNodes;
        this.currentFogNode = currentFogNode;
    }

    // Returns an array of candidate fog node IDs 
    public abstract List<Integer> getCandidateNodes(Location currentLocation, Velocity v);

    // Helper functions 
    public double distanceBetween(Location loc1, Location loc2){
        double longitude = Math.abs(loc1.getLongitude() - loc2.getLongitude()); 
        double latitude = Math.abs(loc1.getLatitude() - loc2.getLatitude());
        return Math.hypot(longitude, latitude);
    }

    public Boolean inCorrectDirection(Location currentLocation, Location destination, Velocity v){
        double slope = (destination.getLongitude() - currentLocation.getLongitude())/(destination.getLatitude() - destination.getLatitude());
        return (slope <= ((v.getY()/v.getX()) + 0.01)) || (slope >= ((v.getY()/v.getX()) - 0.01));
    }

    public int getNearestNeighbor(Location target){
        double closestDist = -1.0;
        int closestNode = -1;
        for(Map.Entry<Integer, Location> entry : nearbyFogNodes.entrySet()){
            double dist = distanceBetween(entry.getValue(), target);
            if(closestDist < 0 || dist < closestDist){
                closestDist = dist;
                closestNode = entry.getKey();
            }
        }
        return closestNode;
    }
}