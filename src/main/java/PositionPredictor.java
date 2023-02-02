import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.topbraid.jenax.progress.SimpleProgressMonitor;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.rules.RuleUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PositionPredictor {

    static Model responseModel = ModelFactory.createDefaultModel();
    static Model resultModel = ModelFactory.createDefaultModel();
    static String OUTPUT_NAME = "out/prediction_result.ttl";

    public static void executeRule() {
        System.out.println("Predicting position");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://localhost:3030/aircraft/");

        Query resultQuery = QueryFactory.create("""
                	PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                    PREFIX aircraft: <http://example.org/aircraft/>
                    PREFIX position: <http://example.org/position/>
                    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                    PREFIX time: <http://example.org/time/>
                    PREFIX voc: <http://example.org/vocabulary#>
                    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                    
                CONSTRUCT{
                     	?s ?p ?o}
                         WHERE {
                             Graph ?graph{
                             ?s ?p ?o.
                     		{?s a voc:Position} UNION {?s a voc:Time}
                             }
                             {
                                 SELECT ?graph ?time ?timestamp WHERE {
                                 GRAPH ?graph {
                                       ?time rdf:type voc:Time.
                                       ?time voc:time ?timestamp.
                                 }
                               }
                 
                           order by desc(?timestamp)
                           limit 3
                             }
                     }
                     """);

        Query latestGraphQuery = QueryFactory.create(
                """
                        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                        PREFIX aircraft: <http://example.org/aircraft/>
                        PREFIX position: <http://example.org/position/>
                        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                        PREFIX time: <http://example.org/time/>
                        PREFIX voc: <http://example.org/vocabulary#>
                        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                                           
                        SELECT DISTINCT ?g ?time
                            WHERE {
                                GRAPH ?g {
                        	        ?s voc:time ?time.
                        	    }
                            } ORDER BY DESC(?time)
                            LIMIT 1"""
        );

        Query enoughGraphsQuery = QueryFactory.create(
                """
                        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                        PREFIX aircraft: <http://example.org/aircraft/>
                        PREFIX position: <http://example.org/position/>
                        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                        PREFIX time: <http://example.org/time/>
                        PREFIX voc: <http://example.org/vocabulary#>
                        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                                                     
                        	SELECT (COUNT(?g) AS ?graphs)
                                       WHERE {
                                           GRAPH ?g {
                                   	        ?s voc:time ?time.
                                   	    }
                                       }
                        """

        );

        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            String graphCount = conn.query(enoughGraphsQuery).execSelect().nextSolution().get("graphs").toString();
            int graphs = Integer.parseInt(graphCount.substring(0, graphCount.indexOf("^")));
            if (graphs < 3) {
                System.out.println("\033[0;31m" + "Not enough data to predict position, only " +
                        graphs + " Graphs: need at least 3 Graphs to get a prediction" + "\033[0m");
                return;
            }

            //getting Response from last 3 Graphs
            responseModel = conn.query(resultQuery).execConstruct();

            //getting the latest Graph for Upload
            QuerySolution latestGraph = conn.query(latestGraphQuery).execSelect().nextSolution();
            String graphURL = latestGraph.get("g").toString();


            //add rules to model
            Model shapesModel = JenaUtil.createMemoryModel();
            shapesModel.read("shacl/PositionPredictorRules.ttl");

            SimpleProgressMonitor monitor = new SimpleProgressMonitor("Position Predictor");

            //infer Triples from rules
            resultModel = RuleUtil.executeRules(responseModel, shapesModel, null, monitor);


            System.out.println("SHACL-Rule for Position Prediction executed");

            if (resultModel.size() != 0) {
                Main.validateModel(resultModel, "PositionPredictor");
                Main.uploadModel(resultModel, graphURL + "/Task1");
            } else System.out.println("No Resulting Data to upload for Position Predictor!");


        }
    }

    public static void writeRDF() {
        try {
            System.out.println("Printing " + resultModel.size() + " resources");
            resultModel.write(new FileOutputStream(OUTPUT_NAME), "TTL");
            System.out.println("Printed to: " + OUTPUT_NAME);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
