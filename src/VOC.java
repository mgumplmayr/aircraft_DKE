import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;


public class VOC {
    public static final String vocabularyURI = "http://host/vocabulary#";
    private static final Model model = ModelFactory.createDefaultModel();

    public static final Property icao24;
    public static final Property registration;
    public static final Property serialNumber;
    public static final Property lineNumber;
    public static final Property builtDate;
    public static final Property registeredDate;
    public static final Property firstFlightDate;
    public static final Property manufacturerIcao;
    public static final Property manufacturerName;
    public static final Property modelName;
    public static final Property typecode;
    public static final Property engines;
    public static final Property icaoAircraftType;
    public static final Property operatorIcao;
    public static final Property operator;
    public static final Property operatorCallsign;
    public static final Property operatorIata;
    public static final Property owner;
    public static final Property categoryDescription;

    public VOC(){

    }

    public static String getURI() { return vocabularyURI; }

    static {
        //Aircraft vocabulary
        icao24 = model.createProperty(vocabularyURI + "icao24");
        registration = model.createProperty(vocabularyURI + "hasRegistration");
        serialNumber = model.createProperty(vocabularyURI + "serialNumber");
        lineNumber = model.createProperty(vocabularyURI + "lineNumber");
        builtDate = model.createProperty(vocabularyURI + "buildDate");
        registeredDate = model.createProperty(vocabularyURI + "registeredDate");
        firstFlightDate = model.createProperty(vocabularyURI + "firstFlightDate");

        //Manufacturer vocabulary
        manufacturerIcao = model.createProperty(vocabularyURI+"hasManufacturerIcao");
        manufacturerName = model.createProperty(vocabularyURI+"hasManufacturerName");

        //Model vocabulary
        modelName = model.createProperty(vocabularyURI+"model");
        typecode = model.createProperty(vocabularyURI+"hasTypecode");
        engines = model.createProperty(vocabularyURI+"engines");
        icaoAircraftType = model.createProperty(vocabularyURI+"icaoAircraftType");

        //Operator Vocabulary
        operatorIcao = model.createProperty(vocabularyURI+"operatorIcao");
        operator = model.createProperty(vocabularyURI+"operator");
        operatorCallsign = model.createProperty(vocabularyURI+"operatorCallsign");
        operatorIata = model.createProperty(vocabularyURI+"operatorIata");

        //Owner Vocabulary
        owner = model.createProperty(vocabularyURI+"owner");

        //Category Vocabulary
        categoryDescription = model.createProperty(vocabularyURI+"category");

    }
}
