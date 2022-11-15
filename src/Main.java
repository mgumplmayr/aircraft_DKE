import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.vocabulary.RDF;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class Main {
    static DataInitiator initiator = new DataInitiator();
    //create model
    static Model model = ModelFactory.createDefaultModel();

    //define general URIS
    static final String startURI = "http://example.org/";


    static         String aircraftURI = startURI + "aircraft/";

    static String manufacturerURI = startURI + "manufacturer/";
    static String modelURI = startURI + "model/";
    static String operatorURI = startURI + "operator/";
    static String ownerURI = startURI + "owner/";
    static String categoryURI = startURI + "category/";
    static String positionURI = startURI + "position/";


    public static void main(String[] args) {

        //create vocabulary prefixes
        model.setNsPrefix("voc", VOC.getURI());
        model.setNsPrefix("rdf", RDF.getURI());

        //create static Aircraft Prefixes
        model.setNsPrefix("aircraft", aircraftURI);
        model.setNsPrefix("manufacturer", manufacturerURI);
        model.setNsPrefix("model", modelURI);
        model.setNsPrefix("operator", operatorURI);
        model.setNsPrefix("owner", ownerURI);
        model.setNsPrefix("category", categoryURI);

        //create dynamic Aircraft Prefixes
        model.setNsPrefix("position", positionURI);

        loadDynamicData();
        loadStaticData();
        linkPositions();


        //todo link positions (dynamic first, then static)

        //write RDF to file
        final String OUTPUT_NAME = "staticRDF.ttl";

        try {
            System.out.println("Printing " + model.size() + " resources");
            model.write(new FileOutputStream(OUTPUT_NAME), "TTL");
            System.out.println("Printed to: " + OUTPUT_NAME);
            System.out.println("---------------------------------------");
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }


        //validate with SHACL
        System.out.println("Checking " + model.size() + " resources");
        Graph staticDataGraph = RDFDataMgr.loadGraph(OUTPUT_NAME);
        Graph shapeGraph = RDFDataMgr.loadGraph("shacl.ttl");


        Shapes shape = Shapes.parse(shapeGraph);
        ValidationReport report = ShaclValidator.get().validate(shape, staticDataGraph);
        System.out.println("---------------------------------------");
        ShLib.printReport(report);
        RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);


    }

    private static void linkPositions() {
        System.out.println("Linking Positions");
        ResIterator positionIterator = model.listSubjectsWithProperty(RDF.type, VOC.position);
        int positionCounter = 0;
        int linkedCounter = 0;
        while (positionIterator.hasNext()) {
            Resource pos = positionIterator.nextResource();

            ResIterator aircraftIterator = model.listSubjectsWithProperty(RDF.type, VOC.aircraft);
            while (aircraftIterator.hasNext()) {
                Resource aircraft = aircraftIterator.nextResource();
                if (pos.getProperty(VOC.icao24).toString().equals(aircraft.getProperty(VOC.icao24).toString())) {
                    aircraft.addProperty(VOC.hasPosition, pos);
                    System.out.println("Aircraft: " + aircraft.getURI() + "linked");
                    linkedCounter++;

                }
            }
            positionCounter++;
        }
        System.out.println(positionCounter + " Positions available");
        System.out.println(linkedCounter + " Positions linked");
    }

    private static void loadStaticData() {
        System.out.println("Loading Static Data");
        JSONArray staticData = initiator.getStaticDataJSON();
        for (Object o : staticData) {
            JSONObject aircraft = (JSONObject) o;

            //Static Aircraft properties
            String thisAircraftURI = aircraftURI + aircraft.get("icao24");
            String thisIcao24 = aircraft.get("icao24").toString();
            String thisRegistration = aircraft.get("registration").toString();
            String thisSerialNumber = aircraft.get("serialnumber").toString();
            String thisLineNumber = aircraft.get("linenumber").toString();
            String thisBuiltDate = aircraft.get("built").toString();
            String thisRegisteredDate = aircraft.get("registered").toString();
            String thisFirstFlightDate = aircraft.get("firstflightdate").toString();

            //Manufacturer properties
            String thisManufacturerURI = manufacturerURI + aircraft.get("manufacturericao").toString().replaceAll("[^A-Za-z0-9]", "");
            String thisManufacturer = aircraft.get("manufacturericao").toString(); //for aircraft
            String thisManufacturerName = aircraft.get("manufacturername").toString();

            //Model properties
            String thisModelURI = modelURI + aircraft.get("model").toString().replaceAll("[^A-Za-z0-9]", "");
            String thisModel = aircraft.get("model").toString();
            String thisTypecode = aircraft.get("typecode").toString();
            String thisEngines = aircraft.get("engines").toString();
            String thisIcaoAircraftType = aircraft.get("icaoaircrafttype").toString();

            //Operator properties
            String thisOperatorURI = operatorURI + aircraft.get("operatoricao").toString().replaceAll("[^A-Za-z0-9]", "");
            String thisOperatorIcao = aircraft.get("operatoricao").toString();
            String thisOperator = aircraft.get("operator").toString();
            String thisOperatorCallsign = aircraft.get("operatorcallsign").toString();
            String thisOperatorIata = aircraft.get("operatoriata").toString();

            //Owner properies
            String thisOwnerURI = ownerURI + aircraft.get("owner").toString().replaceAll("[^A-Za-z0-9]", "");
            String thisOwner = aircraft.get("owner").toString();

            //CategoryDescription properties
            String thisCategoryURI = categoryURI + aircraft.get("categoryDescription").toString().replaceAll("[^A-Za-z0-9]", "");
            String thisCategoryDescription = aircraft.get("categoryDescription").toString();

            //create aircraft resource
            Resource aircraftToAdd = model.createResource(thisAircraftURI)
                    .addProperty(VOC.icao24, thisIcao24)
                    .addProperty(RDF.type, VOC.aircraft);
            if (!thisRegistration.isEmpty())
                aircraftToAdd.addProperty(VOC.registration, thisRegistration); //TODO: Hier addProperty oder addLiteral?
            if (!thisSerialNumber.isEmpty()) aircraftToAdd.addProperty(VOC.serialNumber, thisSerialNumber);
            if (!thisLineNumber.isEmpty()) aircraftToAdd.addProperty(VOC.lineNumber, thisLineNumber);
            if (!thisBuiltDate.isEmpty()) aircraftToAdd.addProperty(VOC.builtDate, thisBuiltDate);
            if (!thisRegisteredDate.isEmpty()) aircraftToAdd.addProperty(VOC.registeredDate, thisRegisteredDate);
            if (!thisFirstFlightDate.isEmpty()) aircraftToAdd.addProperty(VOC.firstFlightDate, thisFirstFlightDate);

            //create manufacturer resource
            Resource manufacturerToAdd;
            if (!thisManufacturer.isEmpty()) manufacturerToAdd = model.createResource(thisManufacturerURI)
                    .addProperty(VOC.manufacturerIcao, thisManufacturer);
            else manufacturerToAdd = model.createResource();

            manufacturerToAdd.addProperty(RDF.type, VOC.manufacturer);
            if (!thisManufacturerName.isEmpty())
                manufacturerToAdd.addProperty(VOC.manufacturerName, thisManufacturerName);

            aircraftToAdd.addProperty(VOC.hasManufacturer, manufacturerToAdd);

            //create model resource
            Resource modelToAdd;
            if (!thisModel.isEmpty()) modelToAdd = model.createResource(thisModelURI)
                    .addProperty(VOC.modelName, thisModel);
            else modelToAdd = model.createResource();

            modelToAdd.addProperty(RDF.type, VOC.model);
            if (!thisTypecode.isEmpty()) modelToAdd.addProperty(VOC.typecode, thisTypecode);
            if (!thisEngines.isEmpty()) modelToAdd.addProperty(VOC.engines, thisEngines);
            if (!thisIcaoAircraftType.isEmpty()) modelToAdd.addProperty(VOC.icaoAircraftType, thisIcaoAircraftType);

            aircraftToAdd.addProperty(VOC.hasModel, modelToAdd);

            //create operator resource
            Resource operatorToAdd;
            if (!thisOperatorIcao.isEmpty()) operatorToAdd = model.createResource(thisOperatorURI)
                    .addProperty(VOC.operatorIcao, thisOperatorIcao);
            else operatorToAdd = model.createResource();

            operatorToAdd.addProperty(RDF.type, VOC.operator);
            if (!thisOperator.isEmpty()) operatorToAdd.addProperty(VOC.operator, thisOperator);
            if (!thisOperatorCallsign.isEmpty())
                operatorToAdd.addProperty(VOC.operatorCallsign, thisOperatorCallsign);
            if (!thisOperatorIata.isEmpty()) operatorToAdd.addProperty(VOC.operatorIata, thisOperatorIata);

            aircraftToAdd.addProperty(VOC.hasOperator, operatorToAdd);

            //create owner resource
            Resource ownerToAdd;
            if (!thisOwner.isEmpty()) ownerToAdd = model.createResource(thisOwnerURI)
                    .addProperty(VOC.ownerName, thisOwner);
            else ownerToAdd = model.createResource();

            ownerToAdd.addProperty(RDF.type, VOC.owner);

            aircraftToAdd.addProperty(VOC.hasOwner, ownerToAdd);

            //create category resource
            Resource categoryDescriptionToAdd;
            if (!thisCategoryDescription.isEmpty()) categoryDescriptionToAdd = model.createResource(thisCategoryURI)
                    .addProperty(VOC.categoryDescriptionName, thisCategoryDescription);
            else categoryDescriptionToAdd = model.createResource();

            categoryDescriptionToAdd.addProperty(RDF.type, VOC.categoryDescription);

            aircraftToAdd.addProperty(VOC.hasCategoryDescription, categoryDescriptionToAdd);


        }
        System.out.println("Static Data loaded");
    }

    private static void loadDynamicData() {
        System.out.println("Loading Dynamic Data");
        JSONObject dynamicData = initiator.getDynamicData();
        JSONArray states = (JSONArray) dynamicData.get("states");
        String time = dynamicData.get("time").toString();

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
            String sensors = String.valueOf(stateToAdd.get(12));
            String geoAltitude = String.valueOf(stateToAdd.get(13));
            String squawk = String.valueOf(stateToAdd.get(14));
            String spi = String.valueOf(stateToAdd.get(15));
            String positionSource = String.valueOf(stateToAdd.get(16));

            String thisPositionURI = positionURI + icao24Pos + "_" + time;
            Resource positionToAdd = model.createResource(thisPositionURI)
                    .addProperty(VOC.icao24, icao24Pos)
                    .addProperty(RDF.type, VOC.position)
                    .addProperty(VOC.time, time);

            //todo check for "null"
            if (!callsign.isEmpty()) positionToAdd.addProperty(VOC.callsign, callsign);
            if (!originCountry.isEmpty()) positionToAdd.addProperty(VOC.originCountry, originCountry);
            if (!timePosition.isEmpty()) positionToAdd.addProperty(VOC.timePosition, timePosition);
            if (!lastContact.isEmpty()) positionToAdd.addProperty(VOC.lastContact, lastContact);
            if (!longitude.isEmpty()) positionToAdd.addProperty(VOC.longitude, longitude);
            if (!latitude.isEmpty()) positionToAdd.addProperty(VOC.latitude, latitude);
            if (!baroAltitude.isEmpty()) positionToAdd.addProperty(VOC.baroAltitude, baroAltitude);
            if (!onGround.isEmpty()) positionToAdd.addProperty(VOC.onGround, onGround);
            if (!velocity.isEmpty()) positionToAdd.addProperty(VOC.velocity, velocity);
            if (!trueTrack.isEmpty()) positionToAdd.addProperty(VOC.trueTrack, trueTrack);
            if (!verticalRate.isEmpty()) positionToAdd.addProperty(VOC.verticalRate, verticalRate);
            if (!sensors.isEmpty()) positionToAdd.addProperty(VOC.sensors, sensors);
            if (!geoAltitude.isEmpty()) positionToAdd.addProperty(VOC.geoAltitude, geoAltitude);
            if (!squawk.isEmpty()) positionToAdd.addProperty(VOC.squawk, squawk);
            if (!spi.isEmpty()) positionToAdd.addProperty(VOC.spi, spi);
            if (!positionSource.isEmpty()) positionToAdd.addProperty(VOC.positionSource, positionSource);
        }
        System.out.println("Dynamic Data Loaded");
    }

}
