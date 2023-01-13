import org.apache.jena.graph.Graph;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.sparql.resultset.RDFOutput;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.util.JenaUtil;
import org.apache.jena.shacl.Shapes;
import org.topbraid.shacl.engine.Shape;
import org.topbraid.shacl.engine.ShapesGraphFactory;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.model.impl.SHParameterImpl;
import org.topbraid.shacl.model.impl.SHParameterizableInstanceImpl;
import org.topbraid.shacl.rules.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ChangeIdentifier {

    static Model responseModel = ModelFactory.createDefaultModel();

    public static void IdentifyChanges() {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://localhost:3030/aircraft/");

        Query query = QueryFactory.create("SELECT * { BIND('Hello'as ?text) }");
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
            //getting Response from last 3 Graphs
            responseModel = conn.query(constructQuery).execConstruct();

            //getting the latest Graph for Upload
            QuerySolution q = conn.query(latestGraphQuery).execSelect().nextSolution();
            String graphURL = q.get("g").toString(); //todo add /3



            //print response to file
            try {
                responseModel.write(new FileOutputStream("out/response.ttl"), "TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            Model shapesModel = JenaUtil.createMemoryModel();
            shapesModel.read("shacl/ChangeIdentifierRules.ttl");

            //todo: add parameter to shacl shape / sparql query?

            /* add rules to model
            try {
                shapesModel.write(new FileOutputStream("out/testRules.ttl"), "TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }*/

            //infer Triples from rules
            Model result = RuleUtil.executeRules(responseModel, shapesModel, null, null);
            //result.add(responseModel);
            try { //write infered triples to file
                result.write(new FileOutputStream("out/changeIdentifier.ttl"), "TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            System.out.println("SHACL-Rule for Change Identification executed");
        }
    }
}
