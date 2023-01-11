import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.rules.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PositionPredictor {

    static Model responseModel = ModelFactory.createDefaultModel();

    public static void predictPosition() {
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
                                GRAPH ?g {\s
                        	        ?s voc:time ?time.
                        	    }
                            } ORDER BY DESC(?time)
                            LIMIT 1"""
        );

        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            /* Funktioniert
             Model fetch = conn.fetch("http://localhost:3030/aircraft/static/");
             System.out.println(fetch);*/

            responseModel = conn.query(resultQuery).execConstruct();
            QuerySolution q =conn.query(latestGraphQuery).execSelect().nextSolution();
            String graph = q.get("g").toString();
            System.out.println(graph);

            try {
                responseModel.write(new FileOutputStream("out/response.ttl"), "TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            Model shapesModel = JenaUtil.createMemoryModel();

            try { //add rules to model
                shapesModel.read("shacl/PositionPredictorRules.ttl");
                shapesModel.write(new FileOutputStream("out/testRules.ttl"), "TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            //infer Triples from rules
            Model result = RuleUtil.executeRules(responseModel, shapesModel, null, null);
            //result.add(responseModel);
            try { //write infered triples to file
                result.write(new FileOutputStream("out/inference.ttl"), "TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            System.out.println("SHACL-Rule for Position Prediction executed");

            /*String graphURL = connectionURL + "states/" + responseTime;
            System.out.println("Uploading dynamic Graph data to endpoint " + graphURL);
            log.append("Uploading dynamic Graph data to endpoint " + graphURL + "\n");

            try (RDFConnection conn = RDFConnection.connect(connectionURL)) {
                conn.put(graphURL, dynamicModel);
            }
            System.out.println("Upload of dynamic Graph data complete");
            log.append("Upload of dynamic Graph data complete\n");*/



        }
    }
}
