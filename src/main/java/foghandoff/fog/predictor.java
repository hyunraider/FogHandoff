package foghandoff.fog;

import java.io.*; 
import java.lang.Math;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import foghandoff.Fog.Velocity;
import foghandoff.Fog.Location;

public class Predictor {
    private Location[] nearbyFogNodes;
    private Location currentFogNode;
    public Predictor(Location currentFogNode, Location[] nearbyFogNodes){
        this.nearbyFogNodes = nearbyFogNodes;
        this.currentFogNode = currentFogNode;
    }

    // Helper functions 
    private double distanceBetween(Location loc1, Location loc2){
        double longitude = Math.abs(loc1.getLongitude() - loc2.getLongitude()); 
        double latitude = Math.abs(loc1.getLatitude() - loc2.getLatitude());
        return Math.hypot(longitude, latitude);
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

        private Boolean inCorrectDirection(Location currentLocation, Location destination, Velocity v){
            double slope = (destination.getLongitude() - currentLocation.getLongitude())/(destination.getLatitude() - destination.getLatitude());
            return (slope <= ((v.getY()/v.getX()) + 0.01)) || (slope >= ((v.getY()/v.getX()) - 0.01));
        }

        private Location[] getNearestIntersection(Location currentLocation){
            // Build request
            StringBuffer request = new StringBuffer("http://api.geonames.org/findNearestAddressJSON?");
            request.append("lat=" + currentLocation.getLatitude());
            request.append("&lng=" + currentLocation.getLongitude());
            request.append("&username=" + username);
            
            JSONObject jsonObject = getJsonObjectFrom(request.toString());
            JSONObject address = (JSONObject) jsonObject.get("address");
            double distance = Double.parseDouble((String)address.get("distance"));
            // TODO handle
        }

        // TODO check logic LOL. Assuming velocity is in long/latitude 
        private Location[] getTestPoints(Location currentLocation, Velocity v){
            double distFromNode = distanceBetween(currentLocation, currentFogNode);
            double testDist = (radius - distFromNode) + radius/100 // should be further away
            
            double newLatitude = currentLocation.getLatitude() + testDist*v.getX();
            double newLongitude = currentLocation.getLongitude() + testDist*v.getY();
            Location directlyAhead = Location.newBuilder().setLatitude(newLatitude)
                                                          .setLongitude(newLongitude)
                                                          .build();
            
            double perpendicularX = v.getX()
            double perpendicularY = -v.getY()
            double mag = Math.sqrt(perpendicularX*perpendicularX + perpendicularY*perpendicularY);
            perpendicularX /= mag;
            perpendicularY /= mag;

            double leftLatitude = newLatitude + perpendicularX;
            double leftLongitude = newLatitude + perpendicularY;
            Location shiftLeft = Location.newBuilder().setLatitude(leftLatitude)
                                                      .setLongitude(leftLongitude)
                                                      .build();

            double leftLatitude = newLatitude - perpendicularX;
            double leftLongitude = newLatitude - perpendicularY;
            Location shiftRight = Location.newBuilder().setLatitude(rightLatitude)
                                                       .setLongitude(rightLongitude)
                                                       .build();
            
            return {directlyAhead, shiftLeft, shiftRight}
        }

        // Returns nearest roads (longitude, latitude) based on current location and velocity
        public Location[] getNearestRoads(Location currentLocation, Velocity v){
            Location[] testPoints = getTestPoints(currentLocation, v);
            
            for(int i = 0; i < 3; i++){
                StringBuffer request = new StringBuffer("https://roads.googleapis.com/v1/nearestRoads?path=");
                request.append(currentLocation.getLongitude() + "," + currentLocation.getLatitude() + "|");
                request.append(testPoints[i].getLongitude() + "," + testPoints[i].getLatitude());
                // TODO finish
                getJsonObjectFrom(request.toString());
            }
        }

        private JSONObject getJsonObjectFrom(String request){
            HttpURLConnection conn = null;
            int responseCode = 0;
            try{
                // Send request to Geonames API
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
}

public class CloudPredictor extends Predictor {

}

public class DumbPredictor extends Predictor {
    
}