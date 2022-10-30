import com.opencsv.CSVReader;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


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

        Model model = ModelFactory.createDefaultModel();
        Property hasManufacturericao = model.createProperty("http://host/aircraft/"+"hasManufacturericao");
        Property hasTypecode = model.createProperty("http://host/aircraft/"+"hasTypecode");
        Property hasSerialnumber = model.createProperty("http://host/aircraft/"+"hasSerialnumber");

        for(Object o: staticData){
            JSONObject aircraft = (JSONObject) o;
            String aircraftURI = "http://host/aircraft/"+aircraft.get("icao24");
            String manufacturericao = aircraft.get("manufacturericao").toString();
            String typecode = aircraft.get("typecode").toString();
            String serialnumber = aircraft.get("serialnumber").toString();
            Resource toAdd = model.createResource(aircraftURI)
                    .addProperty(hasManufacturericao,manufacturericao)
                    .addProperty(hasTypecode, typecode)
                    .addProperty(hasSerialnumber, serialnumber);
        }
        model.write(System.out);

    }
}
