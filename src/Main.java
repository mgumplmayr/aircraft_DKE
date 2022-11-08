import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
import java.nio.file.Paths;


public class Main {

    public static void main(String[] args) {
        DataInitiator initiator = new DataInitiator();
        JSONArray staticData = initiator.getStaticDataJSON();
        JSONArray dynamicData = initiator.getDynamicData();
        //System.out.println(staticData);
        System.out.println("-------------------------------------------");
        //System.out.println(dynamicData);

        //JSONObject x = (JSONObject) staticData.get(3);
        //System.out.println(x.get("icao24"));

        //create RDF Model
        Model model = ModelFactory.createDefaultModel();

        //define general URIS
        String startURI = "http://example.org/";
        model.setNsPrefix("voc",VOC.getURI());
        model.setNsPrefix("rdf",RDF.getURI());


        //create static Aircraft Properties
        String aircraftURI = startURI + "aircraft/";
        model.setNsPrefix("aircraft",aircraftURI);
        String manufacturerURI = startURI + "manufacturer/";
        model.setNsPrefix("manufacturer",manufacturerURI);
        String modelURI = startURI + "model/";
        model.setNsPrefix("model",modelURI);
        String operatorURI = startURI + "operator/";
        model.setNsPrefix("operator",operatorURI);
        String ownerURI = startURI + "owner/";
        model.setNsPrefix("owner",ownerURI);
        String categoryURI = startURI + "Category/";
        model.setNsPrefix("category",categoryURI);


        for(Object o: staticData){
            JSONObject aircraft = (JSONObject) o;

            //Static Aircraft properties
            String thisAircraftURI = aircraftURI+aircraft.get("icao24");
            String thisIcao24 = aircraft.get("icao24").toString();
            String thisRegistration = aircraft.get("registration").toString();
            String thisSerialNumber = aircraft.get("serialnumber").toString();
            String thisLineNumber = aircraft.get("linenumber").toString();
            String thisBuiltDate = aircraft.get("built").toString();
            String thisRegisteredDate = aircraft.get("registered").toString();
            String thisFirstFlightDate = aircraft.get("firstflightdate").toString();

            //Manufacturer properties
            String thisManufacturerURI = manufacturerURI+aircraft.get("manufacturericao").toString().replaceAll("[^A-Za-z0-9]","");
            String thisManufacturer = aircraft.get("manufacturericao").toString(); //for aircraft
            String thisManufacturerName = aircraft.get("manufacturername").toString();

            //Model properties
            String thisModelURI = modelURI+aircraft.get("model").toString().replaceAll("[^A-Za-z0-9]","");
            String thisModel = aircraft.get("model").toString();
            String thisTypecode = aircraft.get("typecode").toString();
            String thisEngines = aircraft.get("engines").toString();
            String thisIcaoAircraftType = aircraft.get("icaoaircrafttype").toString();

            //Operator properties
            String thisOperatorURI = operatorURI+aircraft.get("operatoricao").toString().replaceAll("[^A-Za-z0-9]","");
            String thisOperatorIcao = aircraft.get("operatoricao").toString();
            String thisOperator = aircraft.get("operator").toString();
            String thisOperatorCallsign = aircraft.get("operatorcallsign").toString();
            String thisOperatorIata = aircraft.get("operatoriata").toString();

            //Owner properies
            String thisOwnerURI = ownerURI+aircraft.get("owner").toString().replaceAll("[^A-Za-z0-9]","");
            String thisOwner = aircraft.get("owner").toString();

            //CategoryDescription properties
            String thisCategoryURI = categoryURI+aircraft.get("categoryDescription").toString().replaceAll("[^A-Za-z0-9]","");
            String thisCategoryDescription = aircraft.get("categoryDescription").toString();


            Resource aircraftToAdd = model.createResource(thisAircraftURI)
                    .addProperty(VOC.icao24,thisIcao24)
                    .addProperty(RDF.type,VOC.aircraft);
            if(!thisRegistration.isEmpty()) aircraftToAdd.addProperty(VOC.registration,thisRegistration); //TODO: Hier addProperty oder addLiteral?
            if(!thisSerialNumber.isEmpty()) aircraftToAdd.addProperty(VOC.serialNumber,thisSerialNumber);
            if(!thisLineNumber.isEmpty()) aircraftToAdd.addProperty(VOC.lineNumber,thisLineNumber);
            if(!thisBuiltDate.isEmpty()) aircraftToAdd.addProperty(VOC.builtDate,thisBuiltDate);
            if(!thisRegisteredDate.isEmpty()) aircraftToAdd.addProperty(VOC.registeredDate,thisRegisteredDate);
            if(!thisFirstFlightDate.isEmpty()) aircraftToAdd.addProperty(VOC.firstFlightDate,thisFirstFlightDate);

            if(!thisManufacturer.isEmpty()) {
                Resource manufacturerToAdd = model.createResource(thisManufacturerURI)
                        .addProperty(VOC.manufacturerIcao, thisManufacturer)
                        .addProperty(RDF.type,VOC.manufacturer);
                if(!thisManufacturerName.isEmpty()) manufacturerToAdd.addProperty(VOC.manufacturerName, thisManufacturerName);

                aircraftToAdd.addProperty(VOC.hasManufacturer,manufacturerToAdd);
            }

            if (!thisModel.isEmpty()){
                Resource modelToAdd = model.createResource(thisModelURI)
                        .addProperty(VOC.modelName,thisModel)
                        .addProperty(RDF.type,VOC.model);
                if(!thisTypecode.isEmpty()) modelToAdd.addProperty(VOC.typecode,thisTypecode);
                if(!thisEngines.isEmpty()) modelToAdd.addProperty(VOC.engines,thisEngines);
                if(!thisIcaoAircraftType.isEmpty()) modelToAdd.addProperty(VOC.icaoAircraftType,thisIcaoAircraftType);

                aircraftToAdd.addProperty(VOC.hasModel,modelToAdd);
            }

            if(!thisOperatorIcao.isEmpty()){
                Resource operatorToAdd = model.createResource(thisOperatorURI)
                        .addProperty(VOC.operatorIcao,thisOperatorIcao)
                        .addProperty(RDF.type,VOC.operator);
                if(!thisOperator.isEmpty()) operatorToAdd.addProperty(VOC.operator,thisOperator);
                if(!thisOperatorCallsign.isEmpty()) operatorToAdd.addProperty(VOC.operatorCallsign,thisOperatorCallsign);
                if(!thisOperatorIata.isEmpty()) operatorToAdd.addProperty(VOC.operatorIata,thisOperatorIata);

                aircraftToAdd.addProperty(VOC.hasOperator,operatorToAdd);
            }

            if(!thisOwner.isEmpty()){
                Resource ownerToAdd = model.createResource(thisOwnerURI)
                        .addProperty(VOC.ownerName,thisOwner)
                        .addProperty(RDF.type,VOC.owner);

                aircraftToAdd.addProperty(VOC.hasOwner,ownerToAdd);
            }

            if (!thisCategoryDescription.isEmpty()){
                Resource categoryDescriptionToAdd = model.createResource(thisCategoryURI)
                        .addProperty(VOC.categoryDescriptionName,thisCategoryDescription)
                        .addProperty(RDF.type,VOC.categoryDescription);

                aircraftToAdd.addProperty(VOC.hasCategoryDescription,categoryDescriptionToAdd);
            }
        }
        //write RDF to file
        final String OUTPUT_NAME = "staticRDF.ttl";

        try {
            model.write(new FileOutputStream(OUTPUT_NAME),"TTL");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //validate with SHACL
        Graph staticDataGraph = RDFDataMgr.loadGraph(OUTPUT_NAME);
        Graph shapeGraph = RDFDataMgr.loadGraph("shacl.ttl");


        Shapes shape = Shapes.parse(shapeGraph);
        ValidationReport report = ShaclValidator.get().validate(shape, staticDataGraph);
        System.out.println("---------------------------------------");
        ShLib.printReport(report);
        RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);


    }
}
