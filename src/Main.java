import com.opencsv.CSVReader;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
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
        System.out.println("Test");
        Dataset ds = DatasetFactory.createTxnMem();
        FusekiServer server = FusekiServer.create()
                .port(3333)
                .add("/rdf", ds)
                .build() ;
        server.start() ;
        System.out.println("Started");


        try { //statische Daten
            CSVReader reader = new CSVReader(new FileReader("aircraftDatabase_statisch.csv"));
            List<String[]> r = reader.readAll();
            r.forEach(x -> System.out.println(Arrays.toString(x)));
        }
        catch(Exception e){
            System.out.println("Fehler beim laden der statischen Daten: ");
            e.printStackTrace();
        }

        System.out.println("-----------------------------------------------------");

        try{ //dynamische Daten
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
            System.out.println(states);
            System.out.println("test");

        }
        catch(Exception e){
            System.out.println("Fehler beim laden der dynamischen Daten: ");
            e.printStackTrace();
        }



    }
}
