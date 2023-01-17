import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.progress.SimpleProgressMonitor;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.rules.RuleUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ChangeIdentifier {

    static final String START_URI = "http://example.org/";
    static final String OUTPUT_NAME = "out/changeIdentifier.ttl";
    static final String VELOCITY_EVENT_URI = START_URI + "velocityEvent/";
    static final String DIRECTION_EVENT_URI = START_URI + "directionEvent/";
    static final String HEIGHT_EVENT_URI = START_URI + "heightEvent/";
    static Model responseModel = ModelFactory.createDefaultModel();
    static Model resultModel = ModelFactory.createDefaultModel();
    static SimpleProgressMonitor monitor = new SimpleProgressMonitor("ChangeIdentifier");

    public static void executeRule(float velocityThreshold, float directionThreshold, float heightThreshold) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://localhost:3030/aircraft/");

        Query constructQuery = QueryFactory.create("""
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
                     		{?s a voc:Position} UNION {?s a voc:Time} UNION {?s a voc:Aircraft}
                             }
                             {
                                 SELECT ?graph ?time ?timestamp WHERE {
                                 GRAPH ?graph {
                                       ?time rdf:type voc:Time.
                                       ?time voc:time ?timestamp.
                                 }
                               }
                 
                           order by desc(?timestamp)
                           limit 2
                             }
                     }
                     """);

        Query latestGraphQuery = QueryFactory.create("""
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
                            LIMIT 1
                """);

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
            if (graphs < 2) {
                System.out.println("Not enough data to Identify Changes, only " + graphs + " Graph: need at least 2 Graphs to Identify Changes");
                return;
            }

            //getting Response from last 2 Graphs
            responseModel = conn.query(constructQuery).execConstruct();

            //create Objects for Threshold parameters
            responseModel.createResource(START_URI + "velocityThreshold").addLiteral(RDF.value, velocityThreshold);
            responseModel.createResource(START_URI + "directionThreshold").addLiteral(RDF.value, directionThreshold);
            responseModel.createResource(START_URI + "heightThreshold").addLiteral(RDF.value, heightThreshold);

            //getting the latest Graph for Upload
            QuerySolution q = conn.query(latestGraphQuery).execSelect().nextSolution();
            String endpoint = q.get("g").toString() + "/Task3";


            /* print response to file
            try {
                responseModel.write(new FileOutputStream("out/response.ttl"), "TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            */

            //create model for Rules
            Model shapesModel = JenaUtil.createMemoryModel();
            shapesModel.read("shacl/ChangeIdentifierRules.ttl");
            // add rules to model
            try {
                shapesModel.write(new FileOutputStream("out/testRules.ttl"), "TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            //infer Triples from rules
            resultModel = RuleUtil.executeRules(responseModel, shapesModel, null, monitor);
            resultModel.setNsPrefix("velocityEvent", VELOCITY_EVENT_URI);
            resultModel.setNsPrefix("directionEvent", DIRECTION_EVENT_URI);
            resultModel.setNsPrefix("heightEvent", HEIGHT_EVENT_URI);


            System.out.println("SHACL-Rule for Change Identification executed");
            Main.validateModel(resultModel, "PositionPredictor");
            Main.uploadModel(resultModel, endpoint);
            System.out.println("Upload of Change Identification data complete");
        }
    }

    public static void writeRDF() {
        try {
            System.out.println("Printing " + resultModel.size() + " resources");
            resultModel.write(new FileOutputStream(OUTPUT_NAME), "TTL");
            System.out.println("Printed to: " + OUTPUT_NAME);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
