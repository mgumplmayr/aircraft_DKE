import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;


public class VOC {
    public static final String VOCABULARY_URI = "http://host/vocabulary#";
    private static final Model MODEL = ModelFactory.createDefaultModel();

    public static final Property ICAO24;
    public static final Property REGISTRATION;
    public static final Property SERIALNUMBER;
    public static final Property LINENUMBER;
    public static final Property BUILT_DATE;
    public static final Property REGISTERED_DATE;
    public static final Property FIRST_FLIGHT_DATE;
    public static final Property MANUFACTURER_ICAO;
    public static final Property MANUFACTURER_NAME;
    public static final Property MODEL_NAME;
    public static final Property TYPECODE;
    public static final Property ENGINES;
    public static final Property ICAO_AIRCRAFT_TYPE;
    public static final Property OPERATOR_ICAO;
    public static final Property OPERATOR;
    public static final Property OPERATOR_CALLSIGN;
    public static final Property OPERATOR_IATA;
    public static final Property OWNER;
    public static final Property CATEGORY_DESCRIPTION;

    public VOC(){

    }

    public static String getURI() { return VOCABULARY_URI; }

    static {
        //Aircraft vocabulary
        ICAO24 = MODEL.createProperty(VOCABULARY_URI + "icao24");
        REGISTRATION = MODEL.createProperty(VOCABULARY_URI + "hasRegistration");
        SERIALNUMBER = MODEL.createProperty(VOCABULARY_URI + "serialNumber");
        LINENUMBER = MODEL.createProperty(VOCABULARY_URI + "lineNumber");
        BUILT_DATE = MODEL.createProperty(VOCABULARY_URI + "buildDate");
        REGISTERED_DATE = MODEL.createProperty(VOCABULARY_URI + "registeredDate");
        FIRST_FLIGHT_DATE = MODEL.createProperty(VOCABULARY_URI + "firstFlightDate");

        //Manufacturer vocabulary
        MANUFACTURER_ICAO = MODEL.createProperty(VOCABULARY_URI +"hasManufacturerIcao");
        MANUFACTURER_NAME = MODEL.createProperty(VOCABULARY_URI +"hasManufacturerName");

        //Model vocabulary
        MODEL_NAME = MODEL.createProperty(VOCABULARY_URI +"model");
        TYPECODE = MODEL.createProperty(VOCABULARY_URI +"hasTypecode");
        ENGINES = MODEL.createProperty(VOCABULARY_URI +"engines");
        ICAO_AIRCRAFT_TYPE = MODEL.createProperty(VOCABULARY_URI +"icaoAircraftType");

        //Operator Vocabulary
        OPERATOR_ICAO = MODEL.createProperty(VOCABULARY_URI +"operatorIcao");
        OPERATOR = MODEL.createProperty(VOCABULARY_URI +"operator");
        OPERATOR_CALLSIGN = MODEL.createProperty(VOCABULARY_URI +"operatorCallsign");
        OPERATOR_IATA = MODEL.createProperty(VOCABULARY_URI +"operatorIata");

        //Owner Vocabulary
        OWNER = MODEL.createProperty(VOCABULARY_URI +"owner");

        //Category Vocabulary
        CATEGORY_DESCRIPTION = MODEL.createProperty(VOCABULARY_URI +"category");

    }
}
