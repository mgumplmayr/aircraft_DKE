import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.rules.RuleUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PositionPredictor {

    static Model responseModel = ModelFactory.createDefaultModel();

    public static void predictPosition() {
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
                                GRAPH ?g {\s
                        	        ?s voc:time ?time.
                        	    }
                            } ORDER BY DESC(?time)
                            LIMIT 1"""
        );

        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            //getting Response from last 3 Graphs
            responseModel = conn.query(resultQuery).execConstruct();

            //getting the latest Graph for Upload
            QuerySolution q = conn.query(latestGraphQuery).execSelect().nextSolution();
            String graphURL = q.get("g").toString();

            //add rules to model
            Model shapesModel = JenaUtil.createMemoryModel();
            shapesModel.read("shacl/PositionPredictorRules.ttl");

            //infer Triples from rules
            Model result = RuleUtil.executeRules(responseModel, shapesModel, null, null);
            try {
                //write infered triples to file
                result.write(new FileOutputStream("out/result.ttl"), "TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            System.out.println("SHACL-Rule for Position Prediction executed");

            System.out.println("Uploading generated Position Prediction data to endpoint " + graphURL);
            conn.load(graphURL, result);
            System.out.println("Upload of Position Prediction data complete");

        }
    }

}
