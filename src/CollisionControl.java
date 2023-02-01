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
            String Endpoint = q.get("g").toString()+"/1";


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
            System.out.println("threshold: " + distanceThreshold);
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

/*

    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        PREFIX aircraft: <http://example.org/aircraft/>
        PREFIX position: <http://example.org/position/>
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX time: <http://example.org/time/>
        PREFIX voc: <http://example.org/vocabulary#>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX math: <http://www.w3.org/2005/xpath-functions/math#>


        SELECT ?aircraft1 ?aircraft2 ?distance ?time ?distanceP ?distanceChange WHERE {
        GRAPH ?graph {
        ?position1 voc:hasTime ?time1.
        ?position1 voc:latitude ?latitude1.
        ?position1 voc:longitude ?longitude1.
        ?position1 voc:velocity ?velocity1.
        ?position1 voc:hasAircraft ?aircraft1.
        ?position1 voc:onGround ?onGround1.
        ?position1 voc:trueTrack ?true_track1.
        ?time1 voc:time ?time.
        ?position1 voc:lastContact ?time_position1
        FILTER(?time_position1<?time &&?onGround1 = false)
        ?position2 voc:hasTime ?time2.
        ?position2 voc:latitude ?latitude2.
        ?position2 voc:longitude ?longitude2.
        ?position2 voc:velocity ?velocity2.
        ?position2 voc:hasAircraft ?aircraft2.
        ?position2 voc:onGround ?onGround2.
        ?position2 voc:trueTrack ?true_track2.
        ?time2 voc:time ?time.
        ?position2 voc:lastContact ?time_position2.

        ?position2P voc:hasTime ?time2P.
        ?position2P voc:latitude ?latitude2P.
        ?position2P voc:longitude ?longitude2P.
        ?position2P voc:velocity ?velocity2P.
        ?position2P voc:hasAircraft ?aircraft2P.
        ?position2P voc:onGround ?onGround2P.
        ?position2P voc:trueTrack ?true_track2P.
        ?time2 voc:time ?timeP.
        ?position2P voc:lastContact ?time_position2P

        FILTER(?time_position2<?time &&?onGround2 = false)
        FILTER(?time_position2P<?time &&?onGround2P = false)
        FILTER(?aircraft1 != ?aircraft2)
        FILTER(?aircraft2 = ?aircraft2P)
        FILTER(?time_position2 > ?time_position2P)

        BIND ((?latitude2 - ?latitude1) as ?dLat)
        BIND ((?longitude2 - ?longitude1) as ?dLon)
        BIND (math:pow(math:sin(?dLat/2), 2) + math:pow(math:sin(?dLon/2), 2) * math:cos(?latitude1) * math:cos(?latitude2) as ?a)
        BIND (6378.388 * 2 * math:atan2(math:sqrt(?a), math:sqrt(1.0-?a)) as ?distance)
        FILTER(?distance < 200)

        BIND ((?latitude2P - ?latitude1) as ?dLatP)
        BIND ((?longitude2P - ?longitude1) as ?dLonP)
        BIND (math:pow(math:sin(?dLatP/2), 2) + math:pow(math:sin(?dLonP/2), 2) * math:cos(?latitude1) * math:cos(?latitude2P) as ?aP)
        BIND (6378.388 * 2 * math:atan2(math:sqrt(?a), math:sqrt(1.0-?a)) as ?distanceP)

        Bind((?distance - ?distanceP) as ?distanceChange)
        }


 */