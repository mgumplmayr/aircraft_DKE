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
                PREFIX voc: <http://example.org/vocabulary#>
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                CONSTRUCT{
                  ?s ?p ?o}
                    WHERE {
                  Graph ?g{
                  ?s ?p ?o.
                    ?s rdf:type voc:Position.
                  }
                }""");

        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            /* Funktioniert
             Model fetch = conn.fetch("http://localhost:3030/aircraft/static/");
             System.out.println(fetch);*/

            responseModel = conn.query(constructQuery).execConstruct();

            try {
                responseModel.write(new FileOutputStream("out/response.ttl"),"TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            Model fetchModel = conn.fetch("http://localhost:3030/aircraft/static/");
            //Graph ruleGraph = RDFDataMgr.loadGraph("shacl/ChangeIdentifierRules.ttl");
            Model shapesModel= JenaUtil.createMemoryModel();

            try { //add rules to model
                shapesModel.read("shacl/ChangeIdentifierRules.ttl");
                shapesModel.write(new FileOutputStream("out/testRules.ttl"),"TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            //infer Triples from rules
            Model result = RuleUtil.executeRules(fetchModel,shapesModel,null,null);
            result.add(responseModel);
            try { //write infered triples to file
                result.write(new FileOutputStream("out/inference.ttl"),"TTL");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            System.out.println("end");
        }
    }
}
