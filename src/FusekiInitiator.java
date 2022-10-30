import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;

public class FusekiInitiator {

    public void start(){
        System.out.println("Starting Fuseki Server");
        Dataset ds = DatasetFactory.createTxnMem();
        FusekiServer server = FusekiServer.create()
                .port(3333)
                .add("/rdf", ds)
                .build() ;
        server.start() ;
        System.out.println("Fuseki Server started");
    }

}
