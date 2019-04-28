package foghandoff.fog;

import java.util.*;
import java.io.*;
import java.lang.Math;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

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
public class SmartPredictor extends Predictor {
    @Value("${mapsApiKey}")
    private String apiKey;

    // So spring shut ups
    public SmartPredictor() { super(); }

    public SmartPredictor(Location currentFogNode, HashMap<String, Location> nearbyFogNodes, @Value("${mapsApiKey}")String apiKey, @Value("${geonamesUsername}")String username, @Value("${signalRadius}")double radius){
        super(currentFogNode, nearbyFogNodes);
        this.apiKey = apiKey;
        this.username = username;
        this.radius = radius;
    }

    public List<String> getCandidateNodes(Location currentLocation, Velocity v){
        System.out.println("here");
        if(distanceBetween(currentLocation, currentFogNode) < (radius - 0.01)){
            System.out.println("Still in center of node radius");
            return new ArrayList<String>();
        }

        Location[] probePoints = getProbePoints(currentLocation, v);
        List<String> candidates = new ArrayList<String>();
        for (Location l : probePoints){
            System.out.printf("Probe point: %.2f, %.2f\n", l.getLatitude(), l.getLongitude());
            Location snappedEndpoint = getSnappedEndPoint(currentLocation, l);
            candidates.add(getNearestNeighbor(snappedEndpoint));
        }
        return candidates;
    }

    // Gets three points outside of the current node range: one directly ahead, one slightly to the right and one slightly to the left
    private Location[] getProbePoints(Location currentLocation, Velocity v){
        double distFromNode = distanceBetween(currentLocation, currentFogNode);
        double testDist = (radius - distFromNode) + radius/100; // should be further away

        double newLatitude = currentLocation.getLatitude() + testDist*v.getDeltaLatitude();
        double newLongitude = currentLocation.getLongitude() + testDist*v.getDeltaLongitude();
        Location directlyAhead = Location.newBuilder().setLatitude(newLatitude)
                                                        .setLongitude(newLongitude)
                                                        .build();

        double shiftAmount = 0.000483; // TODO change to configurable

        double perpendicularX = v.getDeltaLatitude();
        double perpendicularY = -v.getDeltaLongitude();
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
        JSONArray snappedPoints = (JSONArray) result.get("snappedPoints");
        JSONObject snappedEndpoint = (JSONObject) snappedPoints.get(1);

        JSONObject location = (JSONObject) snappedEndpoint.get("location");
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");
        Location snappedLocation = Location.newBuilder().setLongitude(longitude)
                                            .setLatitude(latitude)
                                            .build();

        return snappedLocation;
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
