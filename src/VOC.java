import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;


public class VOC {
    public static final String vocabularyURI = "http://example.org/vocabulary#";
    private static final Model m = ModelFactory.createDefaultModel();

    public static final Property aircraft;
    public static final Property icao24;
    public static final Property registration;
    public static final Property serialNumber;
    public static final Property lineNumber;
    public static final Property builtDate;
    public static final Property registeredDate;
    public static final Property firstFlightDate;
    public static final Property hasManufacturer;
    public static final Property hasModel;
    public static final Property hasOperator;
    public static final Property hasOwner;
    public static final Property hasCategoryDescription;

    public static final Property manufacturer;
    public static final Property manufacturerIcao;
    public static final Property manufacturerName;

    public static final Property model;
    public static final Property modelName;
    public static final Property typecode;
    public static final Property engines;
    public static final Property icaoAircraftType;

    public static final Property operatorIcao;
    public static final Property operator;
    public static final Property operatorName;
    public static final Property operatorCallsign;
    public static final Property operatorIata;
    public static final Property owner;
    public static final Property ownerName;
    public static final Property categoryDescription;
    public static final Property categoryDescriptionName;

    public VOC(){

    }

    public static String getURI() { return vocabularyURI; }

    static {
        //Aircraft vocabulary
        aircraft = m.createProperty(vocabularyURI+ "Aircraft");
        icao24 = m.createProperty(vocabularyURI + "icao24");
        registration = m.createProperty(vocabularyURI + "hasRegistration");
        serialNumber = m.createProperty(vocabularyURI + "serialNumber");
        lineNumber = m.createProperty(vocabularyURI + "lineNumber");
        builtDate = m.createProperty(vocabularyURI + "buildDate");
        registeredDate = m.createProperty(vocabularyURI + "registeredDate");
        firstFlightDate = m.createProperty(vocabularyURI + "firstFlightDate");
        hasManufacturer = m.createProperty(vocabularyURI + "hasManufacturer");
        hasModel = m.createProperty(vocabularyURI + "hasModel");
        hasOperator = m.createProperty(vocabularyURI + "hasOperator");
        hasOwner = m.createProperty(vocabularyURI + "hasOwner");
        hasCategoryDescription = m.createProperty(vocabularyURI + "hasCategoryDescription");

        //Manufacturer vocabulary
        manufacturer = m.createProperty(vocabularyURI+"Manufacturer");
        manufacturerIcao = m.createProperty(vocabularyURI+"ManufacturerIcao");
        manufacturerName = m.createProperty(vocabularyURI+"ManufacturerName");

        //Model vocabulary
        model = m.createProperty(vocabularyURI+"Model");
        modelName = m.createProperty(vocabularyURI+"model");
        typecode = m.createProperty(vocabularyURI+"typecode");
        engines = m.createProperty(vocabularyURI+"engines");
        icaoAircraftType = m.createProperty(vocabularyURI+"icaoAircraftType");

        //Operator Vocabulary
        operator = m.createProperty(vocabularyURI+"Operator");
        operatorIcao = m.createProperty(vocabularyURI+"operatorIcao");
        operatorName = m.createProperty(vocabularyURI+"operator");
        operatorCallsign = m.createProperty(vocabularyURI+"operatorCallsign");
        operatorIata = m.createProperty(vocabularyURI+"operatorIata");

        //Owner Vocabulary
        owner = m.createProperty(vocabularyURI+"Owner");
        ownerName = m.createProperty(vocabularyURI+"owner");

        //Category Vocabulary
        categoryDescription = m.createProperty(vocabularyURI+"Category");
        categoryDescriptionName = m.createProperty(vocabularyURI+"category");

    }
}
