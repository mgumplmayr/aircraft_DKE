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


public class CollisionControl {

    static final String START_URI = "http://example.org/";
    static final String OUTPUT_NAME = "out/collisionControl.ttl";
    static final String COLLISION_EVENT_URI = START_URI + "collisionEvent/";
    //static final String DIRECTION_EVENT_URI = START_URI + "directionEvent/";
    //static final String HEIGHT_EVENT_URI = START_URI + "heightEvent/";
    static Model responseModel = ModelFactory.createDefaultModel();
    static Model resultModel = ModelFactory.createDefaultModel();
    static SimpleProgressMonitor monitor = new SimpleProgressMonitor("CollisionControl");

    public static void executeRule(float distanceThreshold) {
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

        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            //getting Response from last 2 Graphs
            responseModel = conn.query(constructQuery).execConstruct();

            //create Objects for Threshold parameters
            responseModel.createResource(START_URI + "distanceThreshold").addLiteral(RDF.value, distanceThreshold);

            //getting the latest Graph for Upload
            QuerySolution q = conn.query(latestGraphQuery).execSelect().nextSolution();
            String Endpoint = q.get("g").toString()+"/3";


            /* print response to file
            try {
                responseModel.write(new FileOutputStream("out/response.ttl"), "TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            */

            //create model for Rules
            Model shapesModel = JenaUtil.createMemoryModel();
            shapesModel.read("shacl/CollisionControlRules.ttl");
            // add rules to model
            try {
                shapesModel.write(new FileOutputStream("out/testRules.ttl"), "TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            //infer Triples from rules
            resultModel = RuleUtil.executeRules(responseModel, shapesModel, null, monitor);
            resultModel.setNsPrefix("collisionEvent", COLLISION_EVENT_URI);


            System.out.println("SHACL-Rule for Collision Control executed");
            Main.uploadModel(resultModel,Endpoint);
            System.out.println("Upload of Collision Control data complete");
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
