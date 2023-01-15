import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;

public class CategoryDataModel {
    static Model model = loadCategoryData();
    static final String startURI = "http://example.org/";
    static String categoryURI = startURI + "category/";
    public static Model loadCategoryData() { //could not retrieve category info from API
        model = ModelFactory.createDefaultModel();

        String thisCategoryURI = categoryURI + "0";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "No information at all");

        thisCategoryURI = categoryURI + "1";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "No ADS-B Emitter Category Information");

        thisCategoryURI = categoryURI + "2";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Light (< 15500 lbs)");

        thisCategoryURI = categoryURI + "3";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Small (15500 to 75000 lbs)");

        thisCategoryURI = categoryURI + "4";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Large (75000 to 300000 lbs)");

        thisCategoryURI = categoryURI + "5";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "High Vortex Large (aircraft such as B-757)");

        thisCategoryURI = categoryURI + "6";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Heavy (> 300000 lbs)");

        thisCategoryURI = categoryURI + "7";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "High Performance (> 5g acceleration and 400 kts)");

        thisCategoryURI = categoryURI + "8";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Rotorcraft");

        thisCategoryURI = categoryURI + "9";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Glider / sailplane");

        thisCategoryURI = categoryURI + "10";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Lighter-than-air");

        thisCategoryURI = categoryURI + "11";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Parachutist / Skydiver");

        thisCategoryURI = categoryURI + "12";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Ultralight / hang-glider / paraglider");

        thisCategoryURI = categoryURI + "13";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Reserved");

        thisCategoryURI = categoryURI + "14";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Unmanned Aerial Vehicle");

        thisCategoryURI = categoryURI + "15";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Space / Trans-atmospheric vehicle");

        thisCategoryURI = categoryURI + "16";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Surface Vehicle – Emergency Vehicle");

        thisCategoryURI = categoryURI + "17";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Surface Vehicle – Service Vehicle");

        thisCategoryURI = categoryURI + "18";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Point Obstacle (includes tethered balloons)");

        thisCategoryURI = categoryURI + "19";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Cluster Obstacle");

        thisCategoryURI = categoryURI + "20";
        model.createProperty(thisCategoryURI)
                .addProperty(RDF.type, VOC.category)
                .addProperty(VOC.categoryDescription, "Line Obstacle");

        return model;
    }
}
