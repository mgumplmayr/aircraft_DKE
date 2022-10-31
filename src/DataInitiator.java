import com.opencsv.CSVReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    public JSONArray getDynamicData() {
        try { //dynamische Daten
            URL url = new URL("https://opensky-network.org/api/states/all?lamin=46.3688&lomin=9.4897&lamax=49.0067&lomax=17.0987");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect(); //mit API verbinden
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) throw new RuntimeException("HttpResponseCode: " + responseCode);
            String data = "";
            Scanner scanner = new Scanner(url.openStream()); //Stream von API verarbeiten
            while (scanner.hasNext()) {
                data += scanner.nextLine();
            }
            scanner.close();

            JSONParser parse = new JSONParser(); //String zu JSON parsen
            JSONObject data_obj = (JSONObject) parse.parse(data);

            JSONArray states = (JSONArray) data_obj.get("states"); //States der Flugzeuge ermitteln
            //System.out.println(states);
            return states;
        } catch (Exception e) {
            System.out.println("Fehler beim laden der dynamischen Daten: ");
            e.printStackTrace();
            return null;
        }
    }


}
