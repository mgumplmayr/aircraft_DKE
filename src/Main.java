import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.util.concurrent.TimeUnit;


public class Main {

    static DataInitiator initiator = new DataInitiator();
    //create model
    static Model staticModel = ModelFactory.createDefaultModel();
    static Model dynamicModel = ModelFactory.createDefaultModel();
    //used for saving of all graphs in RDF File
    static Model RDFFileModel = ModelFactory.createDefaultModel();
    //used to store Category Data
    static Model categoryModel = ModelFactory.createDefaultModel();

    //define general URIS
    static final String startURI = "http://example.org/";

    static String aircraftURI = startURI + "aircraft/";

    static String manufacturerURI = startURI + "manufacturer/";
    static String modelURI = startURI + "model/";
    static String operatorURI = startURI + "operator/";
    static String ownerURI = startURI + "owner/";
    static String categoryURI = startURI + "category/";
    static String timeURI = startURI + "time/";
    static String positionURI = startURI + "position/";

    static final String OUTPUT_NAME = "out/RDFData.ttl";
    static final String SHAPES_NAME = "shacl/shapes.ttl";
    static final String connectionURL = "http://localhost:3030/aircraft/";

    static String responseTime;
    public static StringBuilder log = new StringBuilder(); //todo? https://stackoverflow.com/questions/14534767/how-to-append-a-newline-to-stringbuilder
    public static final String DASHES = "--------------------------------------------";

    public static void main(String[] args) {
        //create GUI
        EventQueue.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });


        //create vocabulary prefixes
        staticModel.setNsPrefix("voc", VOC.getURI());
        staticModel.setNsPrefix("rdf", RDF.getURI());
        staticModel.setNsPrefix("xsd", XSD.getURI());

        dynamicModel.setNsPrefix("voc", VOC.getURI());
        dynamicModel.setNsPrefix("rdf", RDF.getURI());
        dynamicModel.setNsPrefix("xsd", XSD.getURI());

        loadCategoryData(categoryModel);

        //create static Aircraft Prefixes
        staticModel.setNsPrefix("aircraft", aircraftURI);
        staticModel.setNsPrefix("manufacturer", manufacturerURI);
        staticModel.setNsPrefix("model", modelURI);
        staticModel.setNsPrefix("operator", operatorURI);
        staticModel.setNsPrefix("owner", ownerURI);
        staticModel.setNsPrefix("category", categoryURI);

        //create dynamic Aircraft Prefixes
        dynamicModel.setNsPrefix("time", timeURI);
        dynamicModel.setNsPrefix("position", positionURI);
        dynamicModel.setNsPrefix("aircraft", aircraftURI);

        //load data into model
        /*
        System.out.println(DASHES);
        log.append("--------------------------------------------\n");
        System.out.println(log);
        loadStaticData();

        //validate models
        System.out.println(DASHES);
        log.append("--------------------------------------------\n");
        validateStaticData();

        //write RDF to file
        System.out.println(DASHES);
        log.append("--------------------------------------------\n");
        writeRDF();
        */
        //upload both Graphs to Fuseki
        System.out.println(DASHES);
        log.append("--------------------------------------------\n");
        //uploadGraph();

    }

    public static void update() {
        //if(GUI.getFirst()) loadStaticData();
        loadDynamicData();
        validateDynamicData();
        if (GUI.getCreateFile()) writeRDF(); //RDF write necessary?
        uploadDynamicGraph();
    }

    private static void openDatasetQuery() {
        URI uri;
        try {
            uri = new URI("http://localhost:3030/#/dataset/aircraft/query/");
            java.awt.Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initiateFuseki() {
        try {
            Runtime.getRuntime().exec(new String[]{"cmd.exe", "/k", "cd fuseki && start fuseki-server.bat"});
            TimeUnit.SECONDS.sleep(4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Fuseki Server started");
        log.append("Fuseki Server started\n");
    }

    public static void uploadGraph() {
        //upload to Fuseki
        uploadStaticGraph();
        uploadDynamicGraph();
    }

    public static void uploadStaticGraph() {
        System.out.println("Uploading static Graph data to endpoint " + connectionURL + "static/");
        log.append("Uploading static Graph data to endpoint " + connectionURL + "static/" + "\n");

        try (RDFConnection conn = RDFConnection.connect(connectionURL)) {
            conn.put(connectionURL + "static/", staticModel); // put -> set content, load -> add/append
        }
        System.out.println("Upload of static Graph data complete");
        log.append("Upload of static Graph data complete\n");
    }

    private static void uploadDynamicGraph() {
        String graphURL = connectionURL + "states/" + responseTime;
        System.out.println("Uploading dynamic Graph data to endpoint " + graphURL);
        log.append("Uploading dynamic Graph data to endpoint " + graphURL + "\n");

        try (RDFConnection conn = RDFConnection.connect(connectionURL)) {
            conn.put(graphURL, dynamicModel);
        }
        System.out.println("Upload of dynamic Graph data complete");
        log.append("Upload of dynamic Graph data complete\n");

        dynamicModel.removeAll();
    }

    private static void validateData() {
        //validate with SHACL
        validateStaticData();
        validateDynamicData();
    }

    public static void validateStaticData() {
        validateModel(staticModel, "Static Model");
    }

    private static void validateDynamicData() {
        validateModel(dynamicModel, "Dynamic Model");
        RDFFileModel.add(dynamicModel);
    }

    private static void validateModel(Model model, String modelName) {
        System.out.println("Checking " + model.size() + " " + modelName + " resources");
        Graph modelGraph = model.getGraph();
        Graph shapeGraph = RDFDataMgr.loadGraph(SHAPES_NAME);

        Shapes shape = Shapes.parse(shapeGraph);
        ValidationReport report = ShaclValidator.get().validate(shape, modelGraph);
        //RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
        if (report.conforms()) {
            System.out.println(modelName + " Data Conforms");
        } else {
            report.getEntries().forEach((e) -> {
                System.out.println("Removing: " + e.focusNode() + " Reason: " + e.message());
                model.removeAll(model.getResource(e.focusNode().toString()), null, null);
                model.removeAll(null, null, model.getResource(e.focusNode().toString()));
            });
        }
    }

    public static void writeRDF() {
        Model model = ModelFactory.createDefaultModel();
        model.add(staticModel);
        model.add(RDFFileModel);
        try {
            System.out.println("Printing " + model.size() + " resources");
            log.append("Printing " + model.size() + " resources\n");
            model.write(new FileOutputStream(OUTPUT_NAME), "TTL");
            System.out.println("Printed to: " + OUTPUT_NAME);
            log.append("Printed to: " + OUTPUT_NAME + "\n");
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    private static void linkPositions() {
        System.out.println("Linking Positions");
        log.append("Linking Positions\n");
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
                    log.append("Aircraft: " + aircraft.getURI() + " linked\n");
                    linkedCounter++;
                }
            }
            positionCounter++;
        }
        System.out.println(positionCounter + " Positions available");
        log.append(positionCounter + " Positions available\n");
        System.out.println(linkedCounter + " Positions linked");
        log.append(linkedCounter + " Positions linked\n");
    }

    public static void loadStaticData() {
        System.out.println("Loading Static Data");
        log.append("Loading Static Data\n");
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
            String thisManufacturer = aircraft.get("manufacturericao").toString();
            String thisManufacturerName = aircraft.get("manufacturername").toString();

            //Model properties
            String thisModelURI = modelURI + aircraft.get("model").toString().replaceAll("[^A-Za-z0-9]", "");
            String thisModel = aircraft.get("model").toString().trim();
            String thisTypecode = aircraft.get("typecode").toString();
            String thisEngines = aircraft.get("engines").toString();
            String thisIcaoAircraftType = aircraft.get("icaoaircrafttype").toString();

            //Operator properties
            String thisOperatorURI = operatorURI + aircraft.get("operatoricao").toString().replaceAll("[^A-Za-z0-9]", "");
            String thisOperatorIcao = aircraft.get("operatoricao").toString();
            String thisOperator = aircraft.get("operator").toString().trim();
            String thisOperatorCallsign = aircraft.get("operatorcallsign").toString();
            String thisOperatorIata = aircraft.get("operatoriata").toString();

            //Owner properies
            String thisOwnerURI = ownerURI + aircraft.get("owner").toString().replaceAll("[^A-Za-z0-9]", "");
            String thisOwner = aircraft.get("owner").toString().trim();

            //CategoryDescription properties
            String thisCategoryDescription = aircraft.get("categoryDescription").toString().trim();

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
            loadCategoryData(staticModel);
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
        log.append("Static Data loaded\n");
    }

    private static void loadCategoryData(Model model) { //could not retrieve category info from API

        String thisCategoryURI = categoryURI + "0";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "No information at all");

        thisCategoryURI = categoryURI + "1";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "No ADS-B Emitter Category Information");

        thisCategoryURI = categoryURI + "2";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Light (< 15500 lbs)");

        thisCategoryURI = categoryURI + "3";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Small (15500 to 75000 lbs)");

        thisCategoryURI = categoryURI + "4";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Large (75000 to 300000 lbs)");

        thisCategoryURI = categoryURI + "5";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "High Vortex Large (aircraft such as B-757)");

        thisCategoryURI = categoryURI + "6";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Heavy (> 300000 lbs)");

        thisCategoryURI = categoryURI + "7";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "High Performance (> 5g acceleration and 400 kts)");

        thisCategoryURI = categoryURI + "8";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Rotorcraft");

        thisCategoryURI = categoryURI + "9";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Glider / sailplane");

        thisCategoryURI = categoryURI + "10";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Lighter-than-air");

        thisCategoryURI = categoryURI + "11";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Parachutist / Skydiver");

        thisCategoryURI = categoryURI + "12";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Ultralight / hang-glider / paraglider");

        thisCategoryURI = categoryURI + "13";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Reserved");

        thisCategoryURI = categoryURI + "14";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Unmanned Aerial Vehicle");

        thisCategoryURI = categoryURI + "15";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Space / Trans-atmospheric vehicle");

        thisCategoryURI = categoryURI + "16";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Surface Vehicle ??? Emergency Vehicle");

        thisCategoryURI = categoryURI + "17";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Surface Vehicle ??? Service Vehicle");

        thisCategoryURI = categoryURI + "18";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Point Obstacle (includes tethered balloons)");

        thisCategoryURI = categoryURI + "19";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Cluster Obstacle");

        thisCategoryURI = categoryURI + "20";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Line Obstacle");

    }

    private static void loadDynamicData() {
        System.out.println("Loading Dynamic Data");
        log.append("Loading Dynamic Data\n");
        JSONObject dynamicData;
        if (GUI.getChosenMode() == GUI.Mode.TEST) {
            dynamicData = initiator.getDynamicTestData();
        } else dynamicData = initiator.getDynamicData();

        JSONArray states = (JSONArray) dynamicData.get("states");
        responseTime = dynamicData.get("time").toString();

        Resource timeToAdd = dynamicModel.createResource(timeURI + responseTime)
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

            String thisPositionURI = positionURI + icao24Pos + "_" + responseTime;
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
            String thisAircraftURI = aircraftURI + icao24Pos;
            Resource aircraftToAdd = dynamicModel.createResource(thisAircraftURI)
                    .addProperty(VOC.icao24, icao24Pos)
                    .addProperty(RDF.type, VOC.aircraft);

            if (!callsign.equals("null") && !callsign.isEmpty()) aircraftToAdd.addProperty(VOC.callsign, callsign);
            if (!originCountry.equals("null") && !originCountry.isEmpty())
                aircraftToAdd.addProperty(VOC.originCountry, originCountry);
            if (!category.equals("null"))
                aircraftToAdd.addProperty(VOC.hasCategory, categoryModel.getResource(categoryURI + category));

            positionToAdd.addProperty(VOC.hasAircraft, aircraftToAdd);
        }
        System.out.println("Dynamic Data Loaded");
        log.append("Dynamic Data Loaded\n");

    }
}
