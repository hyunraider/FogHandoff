package foghandoff.fog;

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
    private Map <Integer, Location> nearbyFogNodes; // Map between Fog ID and Fog node location.
    private Location currentFogNode;
    public Predictor(Location currentFogNode, Location[] nearbyFogNodes){
        this.nearbyFogNodes = nearbyFogNodes;
        this.currentFogNode = currentFogNode;
    }

    // Returns an array of candidate fog node IDs 
    public abstract int[] getCandidateNodes(Location currentLocation, Velocity v);

    // Helper functions 
    private double distanceBetween(Location loc1, Location loc2){
        double longitude = Math.abs(loc1.getLongitude() - loc2.getLongitude()); 
        double latitude = Math.abs(loc1.getLatitude() - loc2.getLatitude());
        return Math.hypot(longitude, latitude);
    }

    private Boolean inCorrectDirection(Location currentLocation, Location destination, Velocity v){
        double slope = (destination.getLongitude() - currentLocation.getLongitude())/(destination.getLatitude() - destination.getLatitude());
        return (slope <= ((v.getY()/v.getX()) + 0.01)) || (slope >= ((v.getY()/v.getX()) - 0.01));
    }

    private int getNearestNeighbor(Location target){
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

public class SmartPredictor extends Predictor {
    private class Map{
        private String apiKey;
        private String username;
        private double radius;

        public Map(@Value("${mapsApiKey}")String apiKey, @Value("${geonamesUsername}")String username, @Value("${signalRadius}")double radius){
            this.apiKey = apiKey;
            this.username = username;
            this.radius = radius;
        }
    }

    public SmartPredictor(Location currentFogNode, Location[] nearbyFogNodes){
        super(currentFogNode, nearbyFogNodes);
    }

    public int[] getCandidateNodes(Location currentLocation, Velocity v){
        Location[] probePoints = getProbePoints(currentLocation, v);
        List<Integer> candidates = new ArrayList<Integer>();
        for (Location l : probePoints){
            Location snappedEndpoint = getSnappedEndPoint(currentLocation, l);
            candidates.add(getNearestNeighbor(snappedEndpoint));
        }
        return candidates.toArray();
    }

    // Gets three points outside of the current node range: one directly ahead, one slightly to the right and one slightly to the left
    private Location[] getProbePoints(Location currentLocation, Velocity v){
        double distFromNode = distanceBetween(currentLocation, currentFogNode);
        double testDist = (radius - distFromNode) + radius/100; // should be further away
        
        double newLatitude = currentLocation.getLatitude() + testDist*v.getX();
        double newLongitude = currentLocation.getLongitude() + testDist*v.getY();
        Location directlyAhead = Location.newBuilder().setLatitude(newLatitude)
                                                        .setLongitude(newLongitude)
                                                        .build();
        
        double shiftAmount = 0.000483; // TODO change to configurable

        double perpendicularX = v.getX();
        double perpendicularY = -v.getY();
        double mag = Math.sqrt(perpendicularX*perpendicularX + perpendicularY*perpendicularY);
        perpendicularX /= mag;
        perpendicularY /= mag;
        
        double leftLatitude = newLatitude + shiftAmount*perpendicularX;
        double leftLongitude = newLongitude + shiftAmount*perpendicularY;
        Location shiftLeft = Location.newBuilder().setLatitude(leftLatitude)
                                                    .setLongitude(leftLongitude)
                                                    .build();

        double rightLatitude = newLatitude - shiftAmount*perpendicularX;
        double rightLongitude = newLongitude - shiftAmount*perpendicularY;
        Location shiftRight = Location.newBuilder().setLatitude(rightLatitude)
                                                    .setLongitude(rightLongitude)
                                                    .build();
        
        return new Location[] {directlyAhead, shiftLeft, shiftRight};
    }

    private Location getSnappedEndPoint(Location startLocation, Location endLocation){
        StringBuffer request = new StringBuffer("https://roads.googleapis.com/v1/snapToRoads?path=");
        request.append(startLocation.getLatitude() + "," + startLocation.getLongitude() + "|");
        request.append(endLocation.getLatitude() + "," + endLocation.getLongitude());
        request.append("&interpolate=false");
        request.append("&key=" + apiKey);

        JSONObject result = getJsonObjectFrom(request.toString());
        JSONArray snappedPoints = (JSONArray) result.get("snappedPoints")[0];
        JSONObject snappedEndpoint = snappedPoints.getJSONObject(1);

        double latitude = (double) snappedEndpoint.get("location").get("latitude");
        double longitude = (double) snappedEndpoint.get("location").get("longitude");
        Location loc = Location.newBuilder().setLongitude(longitude)
                                            .setLatitude(latitude)
                                            .build();
    
        return loc;
    }
        

    private JSONObject getJsonObjectFrom(String request){
        HttpURLConnection conn = null;
        int responseCode = 0;
        try{
            URL reqUrl = new URL(request);
            conn = (HttpURLConnection) reqUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            
            responseCode = conn.getResponseCode();
            if(responseCode != 200){
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            }

            StringBuilder content;
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }
            System.out.println("Request response: " + content.toString());

            // Parse JSON response
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(content.toString());
            return jsonObject;
        } catch (Exception e){
            System.out.println("Exception: " + e);
        } finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return null;
    }
}

public class CloudPredictor extends Predictor {

}

public class DumbPredictor extends Predictor {
    
}
