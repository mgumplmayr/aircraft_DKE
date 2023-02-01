import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;

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

    static final String OUTPUT_NAME = "out/RDFData.ttl";
    static final String SHAPES_NAME = "shacl/shapes.ttl";
    static final String connectionURL = "http://localhost:3030/aircraft/";
    static String responseTime;
    public static StringBuilder log = new StringBuilder();
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
    }

    public static void executeRules(float velocityThreshold, float directionThreshold, float heightThreshold, float distanceThreshold) {
        System.out.println("\033[0;36m" + "Executing SHACL-Rules for current Graph" + "\033[0m");

        //Task 1
        System.out.println();
        PositionPredictor.executeRule();

        //Task 2
        System.out.println();
        CollisionControl.executeRule(distanceThreshold);

        //Task 3
        System.out.println();
        ChangeIdentifier.executeRule(velocityThreshold, directionThreshold, heightThreshold);

        System.out.println("\033[0;36m" + "SHACL-Rules Executed" + "\033[0m");
        System.out.println(DASHES);
    }

    public static void openDatasetQuery() {

        try {
            URI uri = new URI("http://localhost:3030/#/dataset/aircraft/query/");
            java.awt.Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initiateFuseki() {
        try {
            Runtime.getRuntime().exec(new String[]{"cmd.exe", "/k", "cd fuseki && start fuseki-server.bat"});
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Fuseki Server started");
        System.out.println(DASHES);
    }

    public static void uploadModel(Model model, String endpoint) {
        System.out.println("Uploading Graph data to endpoint " + endpoint);
        try (RDFConnection conn = RDFConnection.connect(connectionURL)) {
            conn.put(endpoint, model); // put -> set content, load -> add/append
        }
        System.out.println("Upload of Graph data complete");
        System.out.println(DASHES);
    }

    public static void uploadStaticGraph() {
        uploadModel(staticModel, connectionURL + "static/");
    }

    private static void uploadDynamicGraph() {
        String graphURL = connectionURL + "states/" + responseTime;
        uploadModel(dynamicModel, graphURL);

        dynamicModel.removeAll();
    }

    public static void validateStaticData() {
        validateModel(staticModel, "Static Model");
        RDFFileModel.add(staticModel);
    }

    public static void validateDynamicData() {
        dynamicModel.add(CategoryDataModel.model);
        validateModel(dynamicModel, "Dynamic Model");
        dynamicModel.remove(CategoryDataModel.model);

        RDFFileModel.add(dynamicModel);
    }

    public static void validateModel(Model model, String modelName) {
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
        System.out.println(DASHES);
    }

    public static void writeRDF() {
        System.out.println("Printing " + RDFFileModel.size() + " resources");
        try {
            RDFFileModel.write(new FileOutputStream(OUTPUT_NAME), "TTL");
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Printed to: " + OUTPUT_NAME);
        System.out.println(DASHES);
    }

}
