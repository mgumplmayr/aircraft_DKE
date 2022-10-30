import com.opencsv.CSVReader;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.VCARD;
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
        System.out.println(staticData);
        System.out.println("-------------------------------------------");
        System.out.println(dynamicData);

        JSONObject x = (JSONObject) staticData.get(3);
        System.out.println(x.get("icao24"));

        //create RDF Model
        Model model = ModelFactory.createDefaultModel();

        //define general URIS
        String startURI = "http://host/";
        String vocabularyURI = startURI + "vocabulary#";

        //create static Aircraft Properties
        String aircraftURI = startURI + "aircraft/";
        String manufacturerURI = startURI + "manufacturer/";
        String modelURI = startURI + "model/";
        String operatorURI = startURI + "operator/";
        String ownerURI = startURI + "owner/";
        String categoryURI = startURI + "Category/";

        //Aircraft vocabulary
        Property icao24 = model.createProperty(vocabularyURI + "icao24");
        Property registration = model.createProperty(vocabularyURI + "hasRegistration");
        Property serialNumber = model.createProperty(vocabularyURI + "serialNumber");
        Property lineNumber = model.createProperty(vocabularyURI + "lineNumber");
        Property builtDate = model.createProperty(vocabularyURI + "buildDate");
        Property registeredDate = model.createProperty(vocabularyURI + "registeredDate");
        Property firstFlightDate = model.createProperty(vocabularyURI + "firstFlightDate");

        //Manufacturer vocabulary
        Property manufacturerIcao = model.createProperty(vocabularyURI+"hasManufacturerIcao");
        Property manufacturerName = model.createProperty(vocabularyURI+"hasManufacturerName");

        //Model vocabulary
        Property modelName = model.createProperty(vocabularyURI+"model");
        Property typecode = model.createProperty(vocabularyURI+"hasTypecode");
        Property engines = model.createProperty(vocabularyURI+"engines");
        Property icaoAircraftType = model.createProperty(vocabularyURI+"icaoAircraftType");

        //Operator Vocabulary
        Property operatorIcao = model.createProperty(vocabularyURI+"operatorIcao");
        Property operator = model.createProperty(vocabularyURI+"operator");
        Property operatorCallsign = model.createProperty(vocabularyURI+"operatorCallsign");
        Property operatorIata = model.createProperty(vocabularyURI+"operatorIata");

        //Owner Vocabulary
        Property owner = model.createProperty(vocabularyURI+"owner");

        //Category Vocabulary
        Property categoryDescription = model.createProperty(vocabularyURI+"category");


        for(Object o: staticData){
            JSONObject aircraft = (JSONObject) o;

            //TODO: add custom prefix for URIS (ex: voc = vocabulary)
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
            String thisManufacturerURI = manufacturerURI+aircraft.get("manufacturericao");
            String thisManufacturer = aircraft.get("manufacturericao").toString(); //for aircraft
            String thisManufacturerName = aircraft.get("manufacturername").toString();

            //Model properties
            String thisModelURI = modelURI+aircraft.get("model");
            String thisModel = aircraft.get("model").toString();
            String thisTypecode = aircraft.get("typecode").toString();
            String thisEngines = aircraft.get("engines").toString();
            String thisIcaoAircraftType = aircraft.get("icaoaircrafttype").toString();

            //Operator properties
            String thisOperatorURI = operatorURI+aircraft.get("operatoricao");
            String thisOperatorIcao = aircraft.get("operatoricao").toString();
            String thisOperator = aircraft.get("operator").toString();
            String thisOperatorCallsign = aircraft.get("operatorcallsign").toString();
            String thisOperatorIata = aircraft.get("operatoriata").toString();

            //Owner properies
            String thisOwnerURI = ownerURI+aircraft.get("owner");
            String thisOwner = aircraft.get("owner").toString();

            //CategoryDescription properties
            String thisCategoryURI = categoryURI+aircraft.get("categoryDescription");
            String thisCategoryDescription = aircraft.get("categoryDescription").toString();


            Resource aircraftToAdd = model.createResource(thisAircraftURI)
                    .addProperty(icao24,thisIcao24)
                    .addProperty(registration,thisRegistration)
                    .addProperty(serialNumber,thisSerialNumber)
                    .addProperty(lineNumber,thisLineNumber)
                    .addProperty(builtDate,thisBuiltDate)
                    .addProperty(registeredDate,thisRegisteredDate)
                    .addProperty(firstFlightDate,thisFirstFlightDate)
                    .addProperty(manufacturerIcao,thisManufacturer)
                    .addProperty(modelName,thisModel)
                    .addProperty(operatorIcao,thisOperatorIcao)
                    .addProperty(owner,thisOwner)
                    .addProperty(categoryDescription,thisCategoryDescription);

            Resource manufacturerToAdd = model.createResource(thisManufacturerURI)
                    .addProperty(manufacturerIcao,thisManufacturer)
                    .addProperty(manufacturerName,thisManufacturerName);

            //TODO: Rework JSON to Java objects? Manufacturers would be multiples in JSON format. -> Delete duplicates
        }

        //Output RDF Data
        try {
            model.write(new FileOutputStream("staticRDF.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
