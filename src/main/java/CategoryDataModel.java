import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;

public class CategoryDataModel {
    static Model model = loadCategoryData();
    static final String START_URI = "http://example.org/";
    static final String CATEGORY_URI = START_URI + "category/";
    public static Model loadCategoryData() { //could not retrieve category info from API
        model = ModelFactory.createDefaultModel();

        String thisCategoryURI = CATEGORY_URI + "0";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "No information at all");

        thisCategoryURI = CATEGORY_URI + "1";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "No ADS-B Emitter Category Information");

        thisCategoryURI = CATEGORY_URI + "2";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Light (< 15500 lbs)");

        thisCategoryURI = CATEGORY_URI + "3";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Small (15500 to 75000 lbs)");

        thisCategoryURI = CATEGORY_URI + "4";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Large (75000 to 300000 lbs)");

        thisCategoryURI = CATEGORY_URI + "5";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "High Vortex Large (aircraft such as B-757)");

        thisCategoryURI = CATEGORY_URI + "6";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Heavy (> 300000 lbs)");

        thisCategoryURI = CATEGORY_URI + "7";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "High Performance (> 5g acceleration and 400 kts)");

        thisCategoryURI = CATEGORY_URI + "8";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Rotorcraft");

        thisCategoryURI = CATEGORY_URI + "9";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Glider / sailplane");

        thisCategoryURI = CATEGORY_URI + "10";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Lighter-than-air");

        thisCategoryURI = CATEGORY_URI + "11";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Parachutist / Skydiver");

        thisCategoryURI = CATEGORY_URI + "12";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Ultralight / hang-glider / paraglider");

        thisCategoryURI = CATEGORY_URI + "13";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Reserved");

        thisCategoryURI = CATEGORY_URI + "14";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Unmanned Aerial Vehicle");

        thisCategoryURI = CATEGORY_URI + "15";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Space / Trans-atmospheric vehicle");

        thisCategoryURI = CATEGORY_URI + "16";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Surface Vehicle – Emergency Vehicle");

        thisCategoryURI = CATEGORY_URI + "17";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Surface Vehicle – Service Vehicle");

        thisCategoryURI = CATEGORY_URI + "18";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Point Obstacle (includes tethered balloons)");

        thisCategoryURI = CATEGORY_URI + "19";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Cluster Obstacle");

        thisCategoryURI = CATEGORY_URI + "20";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Line Obstacle");

        return model;
    }
}
