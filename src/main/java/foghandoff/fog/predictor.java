package foghandoff.fog;

public class Predictor {
    
}

public class SmartPredictor extends Predictor {
    private class Map{
        private String apiKey;
        public Map(@Value("${mapsApiKey}")String apiKey){
            this.apiKey = apiKey;
        }
    }
}

public class CloudPredictor extends Predictor {

}

public class DumbPredictor extends Predictor {
    
}