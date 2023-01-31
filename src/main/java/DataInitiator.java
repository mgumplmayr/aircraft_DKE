import com.opencsv.CSVReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class DataInitiator {
    Queue<JSONObject> testData = new LinkedList<>();


    public DataInitiator() {
        List<String> testFiles = new LinkedList<>();
        testFiles.add("testData/1670000010.json");
        testFiles.add("testData/1670000020.json");
        testFiles.add("testData/1670000030.json");

        JSONParser parser = new JSONParser();
        for (String file : testFiles) {
            try (FileReader reader = new FileReader(file)) {
                JSONObject dataObject = (JSONObject) parser.parse(reader);
                testData.add(dataObject);
                System.out.println("Added " + file + " to testData");
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public List<String[]> getStaticData() {

        try {
            CSVReader reader = new CSVReader(new FileReader("aircraftDatabase_statisch.csv"));
            List<String[]> data = reader.readAll();
            return data;
        } catch (Exception e) {
            System.out.println("Error with static data: ");
            e.printStackTrace();
            return null;
        }
    }

    public JSONArray getStaticDataJSON() {
        List<String[]> data = getStaticData();
        JSONArray result = new JSONArray();
        String[] description = data.get(0);
        for (int i = 1; i < data.size(); i++) {
            JSONObject toAdd = new JSONObject();
            for (int j = 0; j < description.length; j++) {
                toAdd.put(description[j], data.get(i)[j]);
            }
            result.add(toAdd);
        }
        return result;
    }

    public JSONObject getDynamicTestData() {
        try { //dynamische Daten
            JSONObject dataObject = testData.poll();
            if(dataObject == null) throw new RuntimeException("No more Test data available!");
            System.out.println(dataObject);
            return dataObject;
        } catch (Exception e) {
            System.out.println("Error with test Data:");
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject getDynamicData() {
        try { //dynamische Daten
            HttpClient client = HttpClient.newHttpClient();
            String url = "https://opensky-network.org/api/states/all?&extended=1&lamin=45.5&lomin=5.5&lamax=50.07&lomax=17.0";
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONParser parser = new JSONParser(); //String zu JSON parsen
            JSONObject dataObject = (JSONObject) parser.parse(response.body());

            return dataObject;
        } catch (Exception e) {
            System.out.println("Fehler beim laden der dynamischen Daten: ");
            e.printStackTrace();
            return null;
        }
    }


}
