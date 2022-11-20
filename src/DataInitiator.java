import com.opencsv.CSVReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class DataInitiator {

    public List<String[]> getStaticData() {
        try {
            CSVReader reader = new CSVReader(new FileReader("aircraftDatabase_statisch.csv"));
            List<String[]> data = reader.readAll();
            /*data.forEach(x -> {
                for (String string : x) {
                    string.replaceAll(" ", "_");
                }
            });*/
            return data;

        } catch (Exception e) {
            System.out.println("Fehler beim laden der statischen Daten: ");
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

    public JSONObject getDynamicData() {
        try { //dynamische Daten
            //URL url = new URL("https://opensky-network.org/api/states/all?lamin=46.3688&lomin=9.4897&lamax=49.0067&lomax=17.0987");
            URL url = new URL("https://opensky-network.org/api/states/all?lamin=46.3688&lomin=5.4897&lamax=50.0067&lomax=17.0987");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect(); //mit API verbinden
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) throw new RuntimeException("HttpResponseCode: " + responseCode);
            StringBuilder data = new StringBuilder();
            Scanner scanner = new Scanner(url.openStream()); //Stream von API verarbeiten
            while (scanner.hasNext()) {
                data.append(scanner.nextLine());
            }
            scanner.close();

            JSONParser parser = new JSONParser(); //String zu JSON parsen
            JSONObject dataObject = (JSONObject) parser.parse(String.valueOf(data));

            //System.out.println(dataObject);
            return dataObject;
        } catch (Exception e) {
            System.out.println("Fehler beim laden der dynamischen Daten: ");
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

            return dataObject;
        } catch (Exception e) {
            System.out.println("Fehler beim laden der dynamischen Daten: ");
            e.printStackTrace();
            return null;
        }
    }


}
