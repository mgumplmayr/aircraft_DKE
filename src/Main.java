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

   //create model
    static Model staticModel;
    static Model dynamicModel;
    //used for saving of all graphs in RDF File
    static Model RDFFileModel = ModelFactory.createDefaultModel();
    //used to store Category Data
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
    }

    public static void loadStaticData() {
        staticModel = StaticDataModel.loadModel();
    }

    public static void loadDynamicData() {
        dynamicModel = DynamicDataModel.loadDynamicData();
        responseTime = DynamicDataModel.getResponseTime();
    }

    public static void update() {
        loadDynamicData();
        validateDynamicData();
        uploadDynamicGraph();

        ChangeIdentifier.executeRule();
    }

    public static void openDatasetQuery() {
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

    public static void uploadModel(Model model, String endpoint){
        try (RDFConnection conn = RDFConnection.connect(connectionURL)) {
            conn.put(endpoint, model); // put -> set content, load -> add/append
        }
    }

    public static void uploadStaticGraph() {
        System.out.println("Uploading static Graph data to endpoint " + connectionURL + "static/");
        log.append("Uploading static Graph data to endpoint " + connectionURL + "static/" + "\n");

        uploadModel(staticModel,connectionURL+"static/");

        System.out.println("Upload of static Graph data complete");
        log.append("Upload of static Graph data complete\n");
    }

    private static void uploadDynamicGraph() {
        String graphURL = connectionURL + "states/" + responseTime;
        System.out.println("Uploading dynamic Graph data to endpoint " + graphURL);
        log.append("Uploading dynamic Graph data to endpoint " + graphURL + "\n");

        uploadModel(dynamicModel,graphURL);

        System.out.println("Upload of dynamic Graph data complete");
        log.append("Upload of dynamic Graph data complete\n");

        dynamicModel.removeAll();
    }

    public static void validateStaticData() {
        validateModel(staticModel, "Static Model");
        RDFFileModel.add(staticModel);
    }

    public static void validateDynamicData() {
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
        try {
            System.out.println("Printing " + RDFFileModel.size() + " resources");
            log.append("Printing " + RDFFileModel.size() + " resources\n");
            RDFFileModel.write(new FileOutputStream(OUTPUT_NAME), "TTL");
            System.out.println("Printed to: " + OUTPUT_NAME);
            log.append("Printed to: " + OUTPUT_NAME + "\n");
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
