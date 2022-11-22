import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.util.concurrent.TimeUnit;


public class Main {
    static DataInitiator initiator = new DataInitiator();
    //create model
    static Model staticModel = ModelFactory.createDefaultModel();
    static Model dynamicModel = ModelFactory.createDefaultModel();

    //define general URIS
    static final String startURI = "http://example.org/";

    static String aircraftURI = startURI + "aircraft/";

    static String manufacturerURI = startURI + "manufacturer/";
    static String modelURI = startURI + "model/";
    static String operatorURI = startURI + "operator/";
    static String ownerURI = startURI + "owner/";
    static String categoryURI = startURI + "category/";
    static String positionURI = startURI + "position/";

    static final String OUTPUT_NAME = "RDFData.ttl";
    static final String connectionURL = "http://localhost:3030/aircraft/";
    static String dynamicModelTime;

    public static void main(String[] args) {
        //create GUI
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                GUI gui = new GUI();
                gui.setVisible(true);
            }
        });


        //create vocabulary prefixes
        staticModel.setNsPrefix("voc", VOC.getURI());
        staticModel.setNsPrefix("rdf", RDF.getURI());
        staticModel.setNsPrefix("xsd", XSD.getURI());

        dynamicModel.setNsPrefix("voc", VOC.getURI());
        dynamicModel.setNsPrefix("rdf", RDF.getURI());
        dynamicModel.setNsPrefix("xsd", XSD.getURI());

        //create static Aircraft Prefixes
        staticModel.setNsPrefix("aircraft", aircraftURI);
        staticModel.setNsPrefix("manufacturer", manufacturerURI);
        staticModel.setNsPrefix("model", modelURI);
        staticModel.setNsPrefix("operator", operatorURI);
        staticModel.setNsPrefix("owner", ownerURI);
        staticModel.setNsPrefix("category", categoryURI);

        //create dynamic Aircraft Prefixes
        dynamicModel.setNsPrefix("position", positionURI);
        dynamicModel.setNsPrefix("aircraft", aircraftURI);

        //load data into models

        System.out.println("--------------------------------------------");
        loadStaticData();

        //validate models
        System.out.println("--------------------------------------------");
        validateStaticData();

        //write RDF to file
        System.out.println("--------------------------------------------");
        writeRDF();

        //upload both Graphs to Fuseki
        System.out.println("--------------------------------------------");
        //uploadGraph();
    }

    public static void update(){
        if(GUI.getFirst()) loadStaticData();
        loadDynamicData();
        validateDynamicData();
        writeRDF(); //RDF write necessary?
        uploadDynamicGraph();
    }

    private static void openDatasetQuery() {
        URI uri = null;
        try {
            uri = new URI("http://localhost:3030/#/dataset/aircraft/query/");
            java.awt.Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initiateFuseki() { //fuseki in src? why 2 cmd windows?
        try {
            Runtime r = Runtime.getRuntime();
            r.getRuntime().exec("cmd /c start cmd.exe /K \"cd fuseki && start fuseki-server.bat\" ");
            TimeUnit.SECONDS.sleep(4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Fuseki Server started");
        uploadStaticGraph();
    }

    public static void uploadGraph() {
        //upload to Fuseki
        uploadStaticGraph();
        uploadDynamicGraph();
    }

    private static void uploadStaticGraph(){
        System.out.println("Uploading static Graph data to endpoint " + connectionURL);

        try (RDFConnection conn = RDFConnection.connect(connectionURL)) {
            conn.put(staticModel); // put -> set content, load -> add/append
        }
        System.out.println("Upload of static Graph data complete");
    }
    private static void uploadDynamicGraph(){
        System.out.println("Uploading dynamic Graph data to endpoint " + connectionURL+dynamicModelTime);

        try (RDFConnection conn = RDFConnection.connect(connectionURL)) {
            conn.put(connectionURL+dynamicModelTime, dynamicModel);
        }
        System.out.println("Upload of dynamic Graph data complete");
        dynamicModel.removeAll();
    }

    private static void validateData() {
        //validate with SHACL
        validateStaticData();
        validateDynamicData();
    }

    private static void validateStaticData(){
        System.out.println("Checking " + staticModel.size() + "  static model resources");
        Graph staticDataGraph = staticModel.getGraph();
        Graph shapeGraph = RDFDataMgr.loadGraph("shacl.ttl");

        Shapes shape = Shapes.parse(shapeGraph);
        ValidationReport report = ShaclValidator.get().validate(shape, staticDataGraph);
        ShLib.printReport(report);
        //report.getModel() select query -> get focus nodes with error (SPARQL)
        RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
    }
    private static void validateDynamicData(){
        System.out.println("Checking " + dynamicModel.size() + " dynamic model resources");
        Graph dynamicDataGraph = dynamicModel.getGraph();
        Graph shapeGraph = RDFDataMgr.loadGraph("shacl.ttl");

        Shapes shape = Shapes.parse(shapeGraph);
        ValidationReport report = ShaclValidator.get().validate(shape, dynamicDataGraph);
        ShLib.printReport(report);
        RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
    }

    private static void writeRDF() {
        Model model = ModelFactory.createDefaultModel();
        model.add(staticModel);
        model.add(dynamicModel);
        try {
            System.out.println("Printing " + model.size() + " resources");
            model.write(new FileOutputStream(OUTPUT_NAME), "TTL");
            System.out.println("Printed to: " + OUTPUT_NAME);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    private static void linkPositions() {
        System.out.println("Linking Positions");
        ResIterator positionIterator = staticModel.listSubjectsWithProperty(RDF.type, VOC.position);
        int positionCounter = 0;
        int linkedCounter = 0;
        while (positionIterator.hasNext()) {
            Resource pos = positionIterator.nextResource();

            ResIterator aircraftIterator = staticModel.listSubjectsWithProperty(RDF.type, VOC.aircraft);
            while (aircraftIterator.hasNext()) {
                Resource aircraft = aircraftIterator.nextResource();
                //System.out.println("pos: "+ pos.getProperty(VOC.icao24).getObject().toString() + "  Aircraft: " + aircraft.getProperty(VOC.icao24).getObject().toString());
                if (pos.getProperty(VOC.icao24).getObject().equals(aircraft.getProperty(VOC.icao24).getObject())) {
                    aircraft.addProperty(VOC.hasAircraft, pos);
                    System.out.println("Aircraft: " + aircraft.getURI() + " linked");
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
            String thisCategoryDescription = aircraft.get("categoryDescription").toString();

            //create aircraft resource
            Resource aircraftToAdd = staticModel.createResource(thisAircraftURI)
                    .addLiteral(VOC.icao24, thisIcao24)
                    .addProperty(RDF.type, VOC.aircraft);
            if (!thisRegistration.isEmpty())
                aircraftToAdd.addProperty(VOC.registration, thisRegistration);
            if (!thisSerialNumber.isEmpty()) aircraftToAdd.addProperty(VOC.serialNumber, thisSerialNumber);
            if (!thisLineNumber.isEmpty()) aircraftToAdd.addProperty(VOC.lineNumber, thisLineNumber);
            if (!thisBuiltDate.isEmpty()) aircraftToAdd.addProperty(VOC.builtDate, thisBuiltDate);
            if (!thisRegisteredDate.isEmpty()) aircraftToAdd.addProperty(VOC.registeredDate, thisRegisteredDate);
            if (!thisFirstFlightDate.isEmpty()) aircraftToAdd.addProperty(VOC.firstFlightDate, thisFirstFlightDate);

            //create manufacturer resource
            Resource manufacturerToAdd;
            if (!thisManufacturer.isEmpty()) {
                manufacturerToAdd = staticModel.createResource(thisManufacturerURI)
                        .addProperty(VOC.manufacturerIcao, thisManufacturer)
                        .addProperty(RDF.type, VOC.manufacturer);
                if (!thisManufacturerName.isEmpty())
                    manufacturerToAdd.addProperty(VOC.manufacturerName, thisManufacturerName);
                aircraftToAdd.addProperty(VOC.hasManufacturer, manufacturerToAdd);
            } else if (!thisManufacturerName.isEmpty()) {
                manufacturerToAdd = staticModel.createResource()
                        .addProperty(VOC.manufacturerName, thisManufacturerName)
                        .addProperty(RDF.type, VOC.manufacturer);
                aircraftToAdd.addProperty(VOC.hasManufacturer, manufacturerToAdd);
            }


            //create model resource
            Resource modelToAdd;
            if (!thisModel.isEmpty()) {
                modelToAdd = staticModel.createResource(thisModelURI)
                        .addProperty(VOC.modelName, thisModel)
                        .addProperty(RDF.type, VOC.model);
                if (!thisTypecode.isEmpty()) modelToAdd.addProperty(VOC.typecode, thisTypecode);
                if (!thisEngines.isEmpty()) modelToAdd.addProperty(VOC.engines, thisEngines);
                if (!thisIcaoAircraftType.isEmpty()) modelToAdd.addProperty(VOC.icaoAircraftType, thisIcaoAircraftType);

                aircraftToAdd.addProperty(VOC.hasModel, modelToAdd);
            } else if (!thisTypecode.isEmpty() || !thisEngines.isEmpty() || !thisIcaoAircraftType.isEmpty()) {
                modelToAdd = staticModel.createResource()
                        .addProperty(RDF.type, VOC.model);
                if (!thisTypecode.isEmpty()) modelToAdd.addProperty(VOC.typecode, thisTypecode);
                if (!thisEngines.isEmpty()) modelToAdd.addProperty(VOC.engines, thisEngines);
                if (!thisIcaoAircraftType.isEmpty()) modelToAdd.addProperty(VOC.icaoAircraftType, thisIcaoAircraftType);

                aircraftToAdd.addProperty(VOC.hasModel, modelToAdd);
            }


            //create operator resource
            Resource operatorToAdd;
            if (!thisOperatorIcao.isEmpty()) {
                operatorToAdd = staticModel.createResource(thisOperatorURI)
                        .addProperty(VOC.operatorIcao, thisOperatorIcao)
                        .addProperty(RDF.type, VOC.operator);
                if (!thisOperator.isEmpty()) operatorToAdd.addProperty(VOC.operatorName, thisOperator);
                if (!thisOperatorCallsign.isEmpty())
                    operatorToAdd.addProperty(VOC.operatorCallsign, thisOperatorCallsign);
                if (!thisOperatorIata.isEmpty()) operatorToAdd.addProperty(VOC.operatorIata, thisOperatorIata);

                aircraftToAdd.addProperty(VOC.hasOperator, operatorToAdd);
            } else if (!thisOperator.isEmpty() || !thisOperatorCallsign.isEmpty() || !thisOperatorIata.isEmpty()) {
                operatorToAdd = staticModel.createResource()
                        .addProperty(RDF.type, VOC.operator);
                if (!thisOperator.isEmpty()) operatorToAdd.addProperty(VOC.operatorName, thisOperator);
                if (!thisOperatorCallsign.isEmpty())
                    operatorToAdd.addProperty(VOC.operatorCallsign, thisOperatorCallsign);
                if (!thisOperatorIata.isEmpty()) operatorToAdd.addProperty(VOC.operatorIata, thisOperatorIata);

                aircraftToAdd.addProperty(VOC.hasOperator, operatorToAdd);
            }


            //create owner resource
            Resource ownerToAdd;
            if (!thisOwner.isEmpty()) {
                ownerToAdd = staticModel.createResource(thisOwnerURI)
                        .addProperty(VOC.ownerName, thisOwner)
                        .addProperty(RDF.type, VOC.owner);

                aircraftToAdd.addProperty(VOC.hasOwner, ownerToAdd);
            }
            //link categories
            loadCategoryData();
            if (!thisCategoryDescription.isEmpty()) {
                ResIterator categoryIterator = staticModel.listSubjectsWithProperty(RDF.type, VOC.category);
                boolean loop = true;
                while (categoryIterator.hasNext() && loop) {
                    Resource category = categoryIterator.nextResource();
                    if (thisCategoryDescription.equals(category.getProperty(VOC.categoryDescription).getObject().toString())) {
                        aircraftToAdd.addProperty(VOC.hasCategory, category);
                        loop = false;
                    }
                }
            } else {
                aircraftToAdd.addProperty(VOC.hasCategory, staticModel.getResource("http://example.org/category/0"));
            }

        }

        System.out.println("Static Data loaded");
    }

    private static void loadCategoryData() { //could not retrieve category info from API
        Resource categoryToAdd;

        String thisCategoryURI = categoryURI + "0";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "No information at all");

        thisCategoryURI = categoryURI + "1";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "No ADS-B Emitter Category Information");

        thisCategoryURI = categoryURI + "2";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Light (< 15500 lbs)");

        thisCategoryURI = categoryURI + "3";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Small (15500 to 75000 lbs)");

        thisCategoryURI = categoryURI + "4";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Large (75000 to 300000 lbs)");

        thisCategoryURI = categoryURI + "5";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "High Vortex Large (aircraft such as B-757)");

        thisCategoryURI = categoryURI + "6";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Heavy (> 300000 lbs)");

        thisCategoryURI = categoryURI + "7";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "High Performance (> 5g acceleration and 400 kts)");

        thisCategoryURI = categoryURI + "8";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Rotorcraft");

        thisCategoryURI = categoryURI + "9";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Glider / sailplane");

        thisCategoryURI = categoryURI + "10";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Lighter-than-air");

        thisCategoryURI = categoryURI + "11";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Parachutist / Skydiver");

        thisCategoryURI = categoryURI + "12";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Ultralight / hang-glider / paraglider");

        thisCategoryURI = categoryURI + "13";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Reserved");

        thisCategoryURI = categoryURI + "14";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Unmanned Aerial Vehicle");

        thisCategoryURI = categoryURI + "15";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Space / Trans-atmospheric vehicle");

        thisCategoryURI = categoryURI + "16";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Surface Vehicle – Emergency Vehicle");

        thisCategoryURI = categoryURI + "17";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Surface Vehicle – Service Vehicle");

        thisCategoryURI = categoryURI + "18";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Point Obstacle (includes tethered balloons)");

        thisCategoryURI = categoryURI + "19";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Cluster Obstacle");

        thisCategoryURI = categoryURI + "20";
        staticModel.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Line Obstacle");

    }

    private static void loadDynamicData() {
        System.out.println("Loading Dynamic Data");
        JSONObject dynamicData = null;
        if(GUI.getChosenMode() == GUI.Mode.TEST) {
            try {
                JSONParser parser = new JSONParser(); //String zu JSON parsen
                dynamicData = (JSONObject) parser.parse(new FileReader("dynamicDataTest.json"));
                System.out.println("hurray");
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        } else dynamicData = initiator.getDynamicData2();

        JSONArray states = (JSONArray) dynamicData.get("states");
        dynamicModelTime = dynamicData.get("time").toString();

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

            String thisPositionURI = positionURI + icao24Pos + "_" + dynamicModelTime;
            Resource positionToAdd = dynamicModel.createResource(thisPositionURI)
                    .addProperty(RDF.type, VOC.position)
                    .addLiteral(VOC.time, Integer.valueOf(dynamicModelTime));

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
            if (!positionSource.equals("null")) positionToAdd.addLiteral(VOC.positionSource, Integer.valueOf(positionSource));


            //Create dynamic aircraft Resource;
            String thisAircraftURI = aircraftURI + icao24Pos;
            Resource aircraftToAdd = dynamicModel.createResource(thisAircraftURI)
                    .addProperty(VOC.icao24, icao24Pos)
                    .addProperty(RDF.type, VOC.aircraft);

            if (!callsign.equals("null") && !callsign.isEmpty()) aircraftToAdd.addProperty(VOC.callsign, callsign);
            if (!originCountry.equals("null") && !originCountry.isEmpty()) aircraftToAdd.addProperty(VOC.originCountry, originCountry);

            positionToAdd.addProperty(VOC.hasAircraft,aircraftToAdd);
        }
        System.out.println("Dynamic Data Loaded");

    }

}
