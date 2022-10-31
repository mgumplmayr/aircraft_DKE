import com.opencsv.CSVReader;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;




public class Main {

    public static void main(String[] args) {
        DataInitiator initiator = new DataInitiator();
        JSONArray staticData = initiator.getStaticDataJSON();
        JSONArray dynamicData = initiator.getDynamicData();
        //System.out.println(staticData);
        System.out.println("-------------------------------------------");
        //System.out.println(dynamicData);

        JSONObject x = (JSONObject) staticData.get(3);
        System.out.println(x.get("icao24"));

        //create RDF Model
        Model model = ModelFactory.createDefaultModel();

        //define general URIS
        String startURI = "http://host/";
        model.setNsPrefix("voc",VOC.getURI());
        model.setNsPrefix("rdf",RDF.getURI());


        //create static Aircraft Properties
        //TODO: delete special chacarters from URIs
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
            String thisManufacturerURI = manufacturerURI+aircraft.get("manufacturericao").toString().replace(" ","_");
            String thisManufacturer = aircraft.get("manufacturericao").toString(); //for aircraft
            String thisManufacturerName = aircraft.get("manufacturername").toString();

            //Model properties
            String thisModelURI = modelURI+aircraft.get("model").toString().replace(" ","_");;
            String thisModel = aircraft.get("model").toString();
            String thisTypecode = aircraft.get("typecode").toString();
            String thisEngines = aircraft.get("engines").toString();
            String thisIcaoAircraftType = aircraft.get("icaoaircrafttype").toString();

            //Operator properties
            String thisOperatorURI = operatorURI+aircraft.get("operatoricao").toString().replace(" ","_");;
            String thisOperatorIcao = aircraft.get("operatoricao").toString();
            String thisOperator = aircraft.get("operator").toString();
            String thisOperatorCallsign = aircraft.get("operatorcallsign").toString();
            String thisOperatorIata = aircraft.get("operatoriata").toString();

            //Owner properies
            String thisOwnerURI = ownerURI+aircraft.get("owner").toString().replace(" ","_");;
            String thisOwner = aircraft.get("owner").toString();

            //CategoryDescription properties
            String thisCategoryURI = categoryURI+aircraft.get("categoryDescription").toString().replace(" ","_");;
            String thisCategoryDescription = aircraft.get("categoryDescription").toString();


            Resource aircraftToAdd = model.createResource(thisAircraftURI)
                    .addProperty(VOC.icao24,thisIcao24)
                    .addProperty(VOC.registration,thisRegistration)
                    .addProperty(VOC.serialNumber,thisSerialNumber)
                    .addProperty(VOC.lineNumber,thisLineNumber)
                    .addProperty(VOC.builtDate,thisBuiltDate)
                    .addProperty(VOC.registeredDate,thisRegisteredDate)
                    .addProperty(VOC.firstFlightDate,thisFirstFlightDate)
                    .addProperty(RDF.type,"Aircraft");


            if(!thisManufacturer.isEmpty()) {
                aircraftToAdd.addProperty(VOC.manufacturerIcao,thisManufacturer);
                Resource manufacturerToAdd = model.createResource(thisManufacturerURI)
                        .addProperty(VOC.manufacturerIcao, thisManufacturer)
                        .addProperty(VOC.manufacturerName, thisManufacturerName)
                        .addProperty(RDF.type,"Manufacturer");
                //TODO: should we add property "hasAircraft"?
            }
            if (!thisModel.isEmpty()){
                aircraftToAdd.addProperty(VOC.modelName,thisModel);
                Resource modelToAdd = model.createResource(thisModelURI)
                        .addProperty(VOC.modelName,thisModel)
                        .addProperty(VOC.typecode,thisTypecode)
                        .addProperty(VOC.engines,thisEngines)
                        .addProperty(VOC.icaoAircraftType,thisIcaoAircraftType)
                        .addProperty(RDF.type,"Model");
            }
            if(!thisOperatorIcao.isEmpty()){
                aircraftToAdd.addProperty(VOC.operatorIcao,thisOperatorIcao);
                Resource operatorToAdd = model.createResource(thisOperatorURI)
                        .addProperty(VOC.operatorIcao,thisOperatorIcao)
                        .addProperty(VOC.operator,thisOperator)
                        .addProperty(VOC.operatorCallsign,thisOperatorCallsign)
                        .addProperty(VOC.operatorIata,thisOperatorIata)
                        .addProperty(RDF.type,"Operator");
            }
            if(!thisOwner.isEmpty()){
                aircraftToAdd.addProperty(VOC.owner,thisOwner);
                Resource ownerToAdd = model.createResource(thisOwnerURI)
                        .addProperty(VOC.owner,thisOwner)
                        .addProperty(RDF.type,"Owner");
            }
            if (!thisCategoryDescription.isEmpty()){
                aircraftToAdd.addProperty(VOC.categoryDescription,thisCategoryDescription);
                Resource categoryDescriptionToAdd = model.createResource(thisCategoryURI)
                        .addProperty(VOC.categoryDescription,thisCategoryDescription)
                        .addProperty(RDF.type,"Category");
            }
        }

        //Output RDF Data
        try {
            model.write(new FileOutputStream("staticRDF.ttl"),"TTL");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
