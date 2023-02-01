import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;

public class FusekiInitiator {

    public void start(Dataset ds){
        System.out.println("Starting Fuseki Server");
                FusekiServer server = FusekiServer.create()
                .port(3030)
                .add("/aircraft", ds)
                .build() ;
        server.start() ;
        System.out.println("Fuseki Server started");
    }

}
