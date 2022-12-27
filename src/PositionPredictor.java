import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.sparql.resultset.RDFOutput;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.util.ModelPrinter;

import java.io.*;

public class PositionPredictor {
    private static final String OUTPUT_NAME = "outputPred.ttl";
    private static String query = "SELECT ?x ?y ?z WHERE {\n" + "  GRAPH ?graph{\n" +
            "    ?x ?y ?z\n" +
            "  }\n" +
            "}\n"+
            "LIMIT 25";
    private static String query1 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "prefix aircraft: <http://example.org/aircraft/> \n" +
            "prefix position: <http://example.org/position/> \n" +
            "prefix rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "prefix time:     <http://example.org/time/> \n" +
            "prefix voc:      <http://example.org/vocabulary#> \n" +
            "prefix xsd:      <http://www.w3.org/2001/XMLSchema#> \n" +
            "\n" +
            "SELECT ?aircraft ?time ?velocity ?true_track ?latitude ?longitude WHERE {\n" +
            "  GRAPH ?graph{\n" +
            "    ?thisPosition voc:hasTime ?time_object.\n" +
            "    ?thisPosition voc:latitude ?latitude.\n" +
            "    ?thisPosition voc:longitude ?longitude.\n" +
            "    ?thisPosition voc:velocity ?velocity.\n" +
            "    ?thisPosition voc:hasAircraft ?aircraft.\n" +
            "    ?thisPosition voc:onGround ?onGround.\n" +
            "    ?thisPosition voc:trueTrack ?true_track.\n" +
            "    ?time_object voc:time ?time.\n" +
            "    ?thisPosition voc:lastContact ?time_position\n" +
            "    FILTER(?time_position<?time &&?onGround = false)\n" +
            "  }\n" +
            "}";


    public static void predictPosition(){
        try{
            RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("http://localhost:3030/aircraft/");
            RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build();
            final Model[] m = new Model[1];

            conn.queryResultSet(query1, (rs) -> {
                try {
                    m[0] = RDFOutput.encodeAsModel(rs);
                    m[0].write(new FileOutputStream(OUTPUT_NAME), "TTL");;
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });

            Model mod = m[0];
            mod.write(System.out, "TTL");




        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
