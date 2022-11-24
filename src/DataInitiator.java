import com.opencsv.CSVReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class DataInitiator {
    Queue<String> testData = new PriorityQueue<>();
    String testData0 = "{\n" +
            "  \"time\": 1669043836,\n" +
            "  \"states\": [\n" +
            "    [\n" +
            "      \"4b1815\",\n" +
            "      \"SWR2SZ  \",\n" +
            "      \"Switzerland\",\n" +
            "      1669043826,\n" +
            "      1669043826,\n" +
            "      7.8876,\n" +
            "      47.2855,\n" +
            "      4213.86,\n" +
            "      false,\n" +
            "      195.12,\n" +
            "      25.62,\n" +
            "      -11.38,\n" +
            "      null,\n" +
            "      4160.52,\n" +
            "      \"5554\",\n" +
            "      false,\n" +
            "      0\n" +
            "    ]" +
            "]" +
            "}";

    public DataInitiator(){
        String testData1 = "{\n" +
                "  \"time\": 1669043816,\n" +
                "  \"states\": [\n" +
                "    [\n" +
                "      \"4b1815\",\n" +
                "      \"SWR2SZ  \",\n" +
                "      \"Switzerland\",\n" +
                "      1669043826,\n" +
                "      1669043826,\n" +
                "      7.8876,\n" +
                "      47.2855,\n" +
                "      4213.86,\n" +
                "      false,\n" +
                "      195.12,\n" +
                "      25.62,\n" +
                "      -11.38,\n" +
                "      null,\n" +
                "      4160.52,\n" +
                "      \"5554\",\n" +
                "      false,\n" +
                "      0\n" +
                "    ],\n" +
                "    [\n" +
                "      \"4b1816\",\n" +
                "      \"SWR63K  \",\n" +
                "      \"Switzerland\",\n" +
                "      1669043673,\n" +
                "      1669043676,\n" +
                "      8.5599,\n" +
                "      47.4515,\n" +
                "      null,\n" +
                "      true,\n" +
                "      0,\n" +
                "      185.62,\n" +
                "      null,\n" +
                "      null,\n" +
                "      null,\n" +
                "      \"1000\",\n" +
                "      false,\n" +
                "      0\n" +
                "    ],\n" +
                "    [\n" +
                "      \"4b1817\",\n" +
                "      \"SWR9JA  \",\n" +
                "      \"Switzerland\",\n" +
                "      1669043826,\n" +
                "      1669043826,\n" +
                "      6.5257,\n" +
                "      49.2063,\n" +
                "      10363.2,\n" +
                "      false,\n" +
                "      236.86,\n" +
                "      150.32,\n" +
                "      -5.85,\n" +
                "      null,\n" +
                "      10195.56,\n" +
                "      \"1166\",\n" +
                "      false,\n" +
                "      0\n" +
                "    ]]}";
        String testData2 = "{\n" +
                "  \"time\": 1669043826,\n" +
                "  \"states\": [\n" +
                "    [\n" +
                "      \"4b1815\",\n" +
                "      \"SWR2SZ  \",\n" +
                "      \"Switzerland\",\n" +
                "      1669043826,\n" +
                "      1669043826,\n" +
                "      7.8876,\n" +
                "      47.2855,\n" +
                "      4213.86,\n" +
                "      false,\n" +
                "      195.12,\n" +
                "      25.62,\n" +
                "      -11.38,\n" +
                "      null,\n" +
                "      4160.52,\n" +
                "      \"5554\",\n" +
                "      false,\n" +
                "      0\n" +
                "    ],\n" +
                "    [\n" +
                "      \"4b1816\",\n" +
                "      \"SWR63K  \",\n" +
                "      \"Switzerland\",\n" +
                "      1669043673,\n" +
                "      1669043676,\n" +
                "      8.5599,\n" +
                "      47.4515,\n" +
                "      null,\n" +
                "      true,\n" +
                "      0,\n" +
                "      185.62,\n" +
                "      null,\n" +
                "      null,\n" +
                "      null,\n" +
                "      \"1000\",\n" +
                "      false,\n" +
                "      0\n" +
                "    ],\n" +
                "    [\n" +
                "      \"4b1817\",\n" +
                "      \"SWR9JA  \",\n" +
                "      \"Switzerland\",\n" +
                "      1669043826,\n" +
                "      1669043826,\n" +
                "      6.5257,\n" +
                "      49.2063,\n" +
                "      10363.2,\n" +
                "      false,\n" +
                "      236.86,\n" +
                "      150.32,\n" +
                "      -5.85,\n" +
                "      null,\n" +
                "      10195.56,\n" +
                "      \"1166\",\n" +
                "      false,\n" +
                "      0\n" +
                "    ],\n" +
                "    [\n" +
                "      \"4b1812\",\n" +
                "      \"SWR6VU  \",\n" +
                "      \"Switzerland\",\n" +
                "      1669043826,\n" +
                "      1669043826,\n" +
                "      8.3314,\n" +
                "      47.6287,\n" +
                "      1722.12,\n" +
                "      false,\n" +
                "      104.02,\n" +
                "      124.32,\n" +
                "      -2.93,\n" +
                "      null,\n" +
                "      1653.54,\n" +
                "      \"1000\",\n" +
                "      false,\n" +
                "      0\n" +
                "    ],\n" +
                "    [\n" +
                "      \"4b1805\",\n" +
                "      \"SWR1SK  \",\n" +
                "      \"Switzerland\",\n" +
                "      1669043795,\n" +
                "      1669043808,\n" +
                "      8.5576,\n" +
                "      47.4535,\n" +
                "      533.4,\n" +
                "      true,\n" +
                "      0,\n" +
                "      5.62,\n" +
                "      null,\n" +
                "      null,\n" +
                "      null,\n" +
                "      \"2000\",\n" +
                "      false,\n" +
                "      0\n" +
                "    ]]}";
        testData.add(testData2);
        testData.add(testData1);
    }



    public List<String[]> getStaticData() {

        try {
            if(GUI.getChosenMode() == GUI.Mode.TEST) {
                CSVReader reader = new CSVReader(new FileReader("staticDataTest.csv"));
                List<String[]> data = reader.readAll();
                return data;
            }

            CSVReader reader = new CSVReader(new FileReader("aircraftDatabase_statisch.csv"));

            List<String[]> data = reader.readAll();

            return data;

        } catch (Exception e) {
            System.out.println("Fehler beim laden der statischen Daten: ");
            Main.log.append("Fehler beim laden der statischen Daten: ");
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
            String data = testData.poll();
            if(data==null) data = testData0;

            JSONParser parser = new JSONParser(); //String zu JSON parsen
            JSONObject dataObject = (JSONObject) parser.parse(data);
            System.out.println(dataObject);
            Main.log.append(dataObject);

            //System.out.println(dataObject);
            return dataObject;
        } catch (Exception e) {
            System.out.println("Fehler beim laden der dynamischen Daten: ");
            Main.log.append("Fehler beim laden der dynamischen Daten: ");
            e.printStackTrace();
            return null;
        }
    }
    public JSONObject getDynamicData2() {
        try { //dynamische Daten
            HttpClient client = HttpClient.newHttpClient();
            String url= "https://opensky-network.org/api/states/all?lamin=46.3688&lomin=5.4897&lamax=50.0067&lomax=17.0987";
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONParser parser = new JSONParser(); //String zu JSON parsen
            JSONObject dataObject = (JSONObject) parser.parse(response.body());

            //Patrick: für Testfile
            /*
            FileWriter file = new FileWriter("C:/Github/test.json");
            file.write(dataObject.toJSONString());
            file.close();
            */


            return dataObject;
        } catch (Exception e) {
            System.out.println("Fehler beim laden der dynamischen Daten: ");
            Main.log.append("Fehler beim laden der dynamischen Daten: ");
            e.printStackTrace();
            return null;
        }
    }


}
