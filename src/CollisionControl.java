public class CollisionControl {

    private static int distance = 100;

    private static final String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX aircraft: <http://example.org/aircraft/> \n" +
            "PREFIX position: <http://example.org/position/> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX time: <http://example.org/time/> \n" +
            "PREFIX voc: <http://example.org/vocabulary#> \n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "PREFIX math: <http://www.w3.org/2005/xpath-functions/math#>\n" +
            "\n" +
            "\n" +
            "SELECT ?aircraft1 ?aircraft2 ?distance ?time WHERE {\n" +
            "  GRAPH ?graph {\n" +
            "    ?position1 voc:hasTime ?time1.\n" +
            "    ?position1 voc:latitude ?latitude1.\n" +
            "    ?position1 voc:longitude ?longitude1.\n" +
            "    ?position1 voc:velocity ?velocity1.\n" +
            "    ?position1 voc:hasAircraft ?aircraft1.\n" +
            "    ?position1 voc:onGround ?onGround1.\n" +
            "    ?position1 voc:trueTrack ?true_track1.\n" +
            "    ?time1 voc:time ?time.\n" +
            "    ?position1 voc:lastContact ?time_position1\n" +
            "    FILTER(?time_position1<?time &&?onGround1 = false)\n" +
            "    ?position2 voc:hasTime ?time2.\n" +
            "    ?position2 voc:latitude ?latitude2.\n" +
            "    ?position2 voc:longitude ?longitude2.\n" +
            "    ?position2 voc:velocity ?velocity2.\n" +
            "    ?position2 voc:hasAircraft ?aircraft2.\n" +
            "    ?position2 voc:onGround ?onGround2.\n" +
            "    ?position2 voc:trueTrack ?true_track2.\n" +
            "    ?time2 voc:time ?time.\n" +
            "    ?position2 voc:lastContact ?time_position2\n" +
            "    FILTER(?time_position2<?time &&?onGround2 = false)\n" +
            "    FILTER(?aircraft1 != ?aircraft2)\n" +
            "    BIND ((?latitude2 - ?latitude1) as ?dLat)\n" +
            "    BIND ((?longitude2 - ?longitude1) as ?dLon)\n" +
            "    BIND (math:pow(math:sin(?dLat/2), 2) + math:pow(math:sin(?dLon/2), 2) * math:cos(?latitude1) * math:cos(?latitude2) as ?a)\n" +
            "    BIND (6378.388 * 2 * math:atan2(math:sqrt(?a), math:sqrt(1.0-?a)) as ?distance)\n" +
            "    FILTER(?distance < 200)\n" +
            "  }\n" +
            "}";
}

