import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DynamicDataModel {
    static DataInitiator initiator = new DataInitiator();
    static Model dynamicModel;
    static final String START_URI = "http://example.org/";
    static final String CATEGORY_URI = START_URI + "category/";

    static final String AIRCRAFT_URI = START_URI + "aircraft/";
    static final String TIME_URI = START_URI + "time/";
    static final String POSITION_URI = START_URI + "position/";
    static String responseTime;
    public static Model loadDynamicData() {
        dynamicModel = ModelFactory.createDefaultModel();

        //create vocabulary prefixes
        dynamicModel.setNsPrefix("voc", VOC.getURI());
        dynamicModel.setNsPrefix("rdf", RDF.getURI());
        dynamicModel.setNsPrefix("xsd", XSD.getURI());

        //create dynamic Aircraft Prefixes
        dynamicModel.setNsPrefix("time", TIME_URI);
        dynamicModel.setNsPrefix("position", POSITION_URI);
        dynamicModel.setNsPrefix("aircraft", AIRCRAFT_URI);
        dynamicModel.setNsPrefix("category", CATEGORY_URI);

        System.out.println("Loading Dynamic Data");
        JSONObject dynamicData;
        if (GUI.getChosenMode() == GUI.Mode.TEST) {
            dynamicData = initiator.getDynamicTestData();
        } else dynamicData = initiator.getDynamicData();

        JSONArray states = (JSONArray) dynamicData.get("states");
        responseTime = dynamicData.get("time").toString();

        Resource timeToAdd = dynamicModel.createResource(TIME_URI + responseTime)
                .addProperty(RDF.type, VOC.time)
                .addLiteral(VOC.timestamp, Integer.valueOf(responseTime));

        for (Object state : states) {
            JSONArray stateToAdd = (JSONArray) state;
            String icao24Pos = String.valueOf(stateToAdd.get(0));
            String callsign = String.valueOf(stateToAdd.get(1));
            String originCountry = String.valueOf(stateToAdd.get(2));
            String timePosition = String.valueOf(stateToAdd.get(3));
            String lastContact = String.valueOf(stateToAdd.get(4));
            String longitude = String.valueOf(stateToAdd.get(5));
            String latitude = String.valueOf(stateToAdd.get(6));
            String baroAltitude = String.valueOf(stateToAdd.get(7));
            String onGround = String.valueOf(stateToAdd.get(8));
            String velocity = String.valueOf(stateToAdd.get(9));
            String trueTrack = String.valueOf(stateToAdd.get(10));
            String verticalRate = String.valueOf(stateToAdd.get(11));
            //String sensors = String.valueOf(stateToAdd.get(12));
            String geoAltitude = String.valueOf(stateToAdd.get(13));
            String squawk = String.valueOf(stateToAdd.get(14));
            String spi = String.valueOf(stateToAdd.get(15));
            String positionSource = String.valueOf(stateToAdd.get(16));
            String category = String.valueOf(stateToAdd.get(17));

            String thisPositionURI = POSITION_URI + icao24Pos + "_" + responseTime;
            Resource positionToAdd = dynamicModel.createResource(thisPositionURI)
                    .addProperty(RDF.type, VOC.position)
                    .addProperty(VOC.hasTime, timeToAdd);

            if (!timePosition.equals("null")) positionToAdd.addLiteral(VOC.timePosition, Integer.valueOf(timePosition));
            if (!lastContact.equals("null")) positionToAdd.addLiteral(VOC.lastContact, Integer.valueOf(lastContact));
            if (!longitude.equals("null")) positionToAdd.addLiteral(VOC.longitude, Float.valueOf(longitude));
            if (!latitude.equals("null")) positionToAdd.addLiteral(VOC.latitude, Float.valueOf(latitude));
            if (!baroAltitude.equals("null")) positionToAdd.addLiteral(VOC.baroAltitude, Float.valueOf(baroAltitude));
            if (!onGround.equals("null")) positionToAdd.addLiteral(VOC.onGround, Boolean.valueOf(onGround));
            if (!velocity.equals("null")) positionToAdd.addLiteral(VOC.velocity, Float.valueOf(velocity));
            if (!trueTrack.equals("null")) positionToAdd.addLiteral(VOC.trueTrack, Float.valueOf(trueTrack));
            if (!verticalRate.equals("null")) positionToAdd.addLiteral(VOC.verticalRate, Float.valueOf(verticalRate));
            // if (!sensors.isEmpty()) positionToAdd.addProperty(VOC.sensors, sensors);
            if (!geoAltitude.equals("null")) positionToAdd.addLiteral(VOC.geoAltitude, Float.valueOf(geoAltitude));
            if (!squawk.equals("null")) positionToAdd.addLiteral(VOC.squawk, squawk);
            if (!spi.equals("null")) positionToAdd.addLiteral(VOC.spi, Boolean.valueOf(spi));
            if (!positionSource.equals("null"))
                positionToAdd.addLiteral(VOC.positionSource, Integer.valueOf(positionSource));


            //Create dynamic aircraft Resource;
            String thisAircraftURI = AIRCRAFT_URI + icao24Pos;
            Resource aircraftToAdd = dynamicModel.createResource(thisAircraftURI)
                    .addProperty(VOC.icao24, icao24Pos)
                    .addProperty(RDF.type, VOC.aircraft);

            if (!callsign.equals("null") && !callsign.isEmpty()) aircraftToAdd.addProperty(VOC.callsign, callsign);
            if (!originCountry.equals("null") && !originCountry.isEmpty())
                aircraftToAdd.addProperty(VOC.originCountry, originCountry);
            if (!category.equals("null"))
                aircraftToAdd.addProperty(VOC.hasCategory, CategoryDataModel.model.getResource(CATEGORY_URI + category));

            positionToAdd.addProperty(VOC.hasAircraft, aircraftToAdd);
        }
        System.out.println("Dynamic Data Loaded");

        return dynamicModel;
    }

    public static String getResponseTime() {
        return responseTime;
    }
}
