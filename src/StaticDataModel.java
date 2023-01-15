import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class StaticDataModel {
    static DataInitiator initiator = new DataInitiator();
    static Model staticModel = ModelFactory.createDefaultModel();
    static final String startURI = "http://example.org/";
    static String aircraftURI = startURI + "aircraft/";

    static String manufacturerURI = startURI + "manufacturer/";
    static String modelURI = startURI + "model/";
    static String operatorURI = startURI + "operator/";
    static String ownerURI = startURI + "owner/";
    static String categoryURI = startURI + "category/";

    public static Model loadModel() {
        //create vocabulary prefixes
        staticModel.setNsPrefix("voc", VOC.getURI());
        staticModel.setNsPrefix("rdf", RDF.getURI());
        staticModel.setNsPrefix("xsd", XSD.getURI());

        //create static Aircraft Prefixes
        staticModel.setNsPrefix("aircraft", aircraftURI);
        staticModel.setNsPrefix("manufacturer", manufacturerURI);
        staticModel.setNsPrefix("model", modelURI);
        staticModel.setNsPrefix("operator", operatorURI);
        staticModel.setNsPrefix("owner", ownerURI);
        staticModel.setNsPrefix("category", categoryURI);


        System.out.println("Loading Static Data");
        JSONArray staticData = initiator.getStaticDataJSON();
        for (Object o : staticData) {
            JSONObject aircraft = (JSONObject) o;

            //Static Aircraft properties
            String thisAircraftURI = aircraftURI + aircraft.get("icao24");
            String thisIcao24 = aircraft.get("icao24").toString();
            String thisRegistration = aircraft.get("registration").toString();
            String thisSerialNumber = aircraft.get("serialnumber").toString();
            String thisLineNumber = aircraft.get("linenumber").toString();
            String thisBuiltDate = aircraft.get("built").toString();
            String thisRegisteredDate = aircraft.get("registered").toString();
            String thisFirstFlightDate = aircraft.get("firstflightdate").toString();

            //Manufacturer properties
            String thisManufacturerURI = manufacturerURI + aircraft.get("manufacturericao").toString().replaceAll("[^A-Za-z0-9]", "");
            String thisManufacturer = aircraft.get("manufacturericao").toString();
            String thisManufacturerName = aircraft.get("manufacturername").toString();

            //Model properties
            String thisModelURI = modelURI + aircraft.get("model").toString().replaceAll("[^A-Za-z0-9]", "");
            String thisModel = aircraft.get("model").toString().trim();
            String thisTypecode = aircraft.get("typecode").toString();
            String thisEngines = aircraft.get("engines").toString();
            String thisIcaoAircraftType = aircraft.get("icaoaircrafttype").toString();

            //Operator properties
            String thisOperatorURI = operatorURI + aircraft.get("operatoricao").toString().replaceAll("[^A-Za-z0-9]", "");
            String thisOperatorIcao = aircraft.get("operatoricao").toString();
            String thisOperator = aircraft.get("operator").toString().trim();
            String thisOperatorCallsign = aircraft.get("operatorcallsign").toString();
            String thisOperatorIata = aircraft.get("operatoriata").toString();

            //Owner properies
            String thisOwnerURI = ownerURI + aircraft.get("owner").toString().replaceAll("[^A-Za-z0-9]", "");
            String thisOwner = aircraft.get("owner").toString().trim();

            //CategoryDescription properties
            String thisCategoryDescription = aircraft.get("categoryDescription").toString().trim();

            //create aircraft resource
            Resource aircraftToAdd = staticModel.createResource(thisAircraftURI)
                    .addLiteral(VOC.icao24, thisIcao24)
                    .addProperty(RDF.type, VOC.aircraft);
            if (!thisRegistration.isEmpty())
                aircraftToAdd.addProperty(VOC.registration, thisRegistration);
            if (!thisSerialNumber.isEmpty()) aircraftToAdd.addProperty(VOC.serialNumber, thisSerialNumber);
            if (!thisLineNumber.isEmpty()) aircraftToAdd.addProperty(VOC.lineNumber, thisLineNumber);
            if (!thisBuiltDate.isEmpty()) aircraftToAdd.addProperty(VOC.builtDate, thisBuiltDate);
            if (!thisRegisteredDate.isEmpty()) aircraftToAdd.addProperty(VOC.registeredDate, thisRegisteredDate);
            if (!thisFirstFlightDate.isEmpty()) aircraftToAdd.addProperty(VOC.firstFlightDate, thisFirstFlightDate);

            //create manufacturer resource
            Resource manufacturerToAdd;
            if (!thisManufacturer.isEmpty()) {
                manufacturerToAdd = staticModel.createResource(thisManufacturerURI)
                        .addProperty(VOC.manufacturerIcao, thisManufacturer)
                        .addProperty(RDF.type, VOC.manufacturer);
                if (!thisManufacturerName.isEmpty())
                    manufacturerToAdd.addProperty(VOC.manufacturerName, thisManufacturerName);
                aircraftToAdd.addProperty(VOC.hasManufacturer, manufacturerToAdd);
            } else if (!thisManufacturerName.isEmpty()) {
                manufacturerToAdd = staticModel.createResource()
                        .addProperty(VOC.manufacturerName, thisManufacturerName)
                        .addProperty(RDF.type, VOC.manufacturer);
                aircraftToAdd.addProperty(VOC.hasManufacturer, manufacturerToAdd);
            }


            //create model resource
            Resource modelToAdd;
            if (!thisModel.isEmpty()) {
                modelToAdd = staticModel.createResource(thisModelURI)
                        .addProperty(VOC.modelName, thisModel)
                        .addProperty(RDF.type, VOC.model);
                if (!thisTypecode.isEmpty()) modelToAdd.addProperty(VOC.typecode, thisTypecode);
                if (!thisEngines.isEmpty()) modelToAdd.addProperty(VOC.engines, thisEngines);
                if (!thisIcaoAircraftType.isEmpty()) modelToAdd.addProperty(VOC.icaoAircraftType, thisIcaoAircraftType);

                aircraftToAdd.addProperty(VOC.hasModel, modelToAdd);
            } else if (!thisTypecode.isEmpty() || !thisEngines.isEmpty() || !thisIcaoAircraftType.isEmpty()) {
                modelToAdd = staticModel.createResource()
                        .addProperty(RDF.type, VOC.model);
                if (!thisTypecode.isEmpty()) modelToAdd.addProperty(VOC.typecode, thisTypecode);
                if (!thisEngines.isEmpty()) modelToAdd.addProperty(VOC.engines, thisEngines);
                if (!thisIcaoAircraftType.isEmpty()) modelToAdd.addProperty(VOC.icaoAircraftType, thisIcaoAircraftType);

                aircraftToAdd.addProperty(VOC.hasModel, modelToAdd);
            }


            //create operator resource
            Resource operatorToAdd;
            if (!thisOperatorIcao.isEmpty()) {
                operatorToAdd = staticModel.createResource(thisOperatorURI)
                        .addProperty(VOC.operatorIcao, thisOperatorIcao)
                        .addProperty(RDF.type, VOC.operator);
                if (!thisOperator.isEmpty()) operatorToAdd.addProperty(VOC.operatorName, thisOperator);
                if (!thisOperatorCallsign.isEmpty())
                    operatorToAdd.addProperty(VOC.operatorCallsign, thisOperatorCallsign);
                if (!thisOperatorIata.isEmpty()) operatorToAdd.addProperty(VOC.operatorIata, thisOperatorIata);

                aircraftToAdd.addProperty(VOC.hasOperator, operatorToAdd);
            } else if (!thisOperator.isEmpty() || !thisOperatorCallsign.isEmpty() || !thisOperatorIata.isEmpty()) {
                operatorToAdd = staticModel.createResource()
                        .addProperty(RDF.type, VOC.operator);
                if (!thisOperator.isEmpty()) operatorToAdd.addProperty(VOC.operatorName, thisOperator);
                if (!thisOperatorCallsign.isEmpty())
                    operatorToAdd.addProperty(VOC.operatorCallsign, thisOperatorCallsign);
                if (!thisOperatorIata.isEmpty()) operatorToAdd.addProperty(VOC.operatorIata, thisOperatorIata);

                aircraftToAdd.addProperty(VOC.hasOperator, operatorToAdd);
            }


            //create owner resource
            Resource ownerToAdd;
            if (!thisOwner.isEmpty()) {
                ownerToAdd = staticModel.createResource(thisOwnerURI)
                        .addProperty(VOC.ownerName, thisOwner)
                        .addProperty(RDF.type, VOC.owner);

                aircraftToAdd.addProperty(VOC.hasOwner, ownerToAdd);
            }
            //link categories
            staticModel.add(CategoryDataModel.model);
            if (!thisCategoryDescription.isEmpty()) {
                ResIterator categoryIterator = staticModel.listSubjectsWithProperty(RDF.type, VOC.category);
                boolean loop = true;
                while (categoryIterator.hasNext() && loop) {
                    Resource category = categoryIterator.nextResource();
                    if (thisCategoryDescription.equals(category.getProperty(VOC.categoryDescription).getObject().toString())) {
                        aircraftToAdd.addProperty(VOC.hasCategory, category);
                        loop = false;
                    }
                }
            } else {
                aircraftToAdd.addProperty(VOC.hasCategory, staticModel.getResource("http://example.org/category/0"));
            }

        }

        System.out.println("Static Data loaded");

        return staticModel;
    }
}
