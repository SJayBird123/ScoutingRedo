import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class BlueAllianceAPI {

    private final String apiKey;

    private int year;

    public BlueAllianceAPI(String apiKey, int currentYear) {
        this.apiKey = apiKey;
        year = currentYear;
    }

    public JSONArray apiCall(String endpoint) throws Exception {
        URL matchesURL = new URL("https://www.thebluealliance.com/api/v3/" + endpoint);
        HttpsURLConnection con = (HttpsURLConnection) matchesURL.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "MOEScouting");
        con.setRequestProperty("X-TBA-Auth-Key", apiKey);

        int responseCode = con.getResponseCode();
        System.out.println("This is " + responseCode);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            JSONParser parser = new JSONParser();
            return (JSONArray) parser.parse(in);
        }
    }

    public ArrayList<String> returnCountries() throws Exception {
        JSONArray eventsRaw = apiCall("events/" + year + "/simple");
        ArrayList<String> countries = new ArrayList<String>();

        for (Object objMatch: eventsRaw) {
            JSONObject eventRawSpecific = (JSONObject) objMatch;
            String matchCountry = (String) eventRawSpecific.get("country");
            boolean isCountryPresent = false;

            for(String addedCountry : countries){
                if (matchCountry.equals(addedCountry)) {
                    isCountryPresent = true;
                    break;
                }
            }

            if(!isCountryPresent){
                countries.add(matchCountry);
            }
        }

        Collections.sort(countries);
        return countries;
    }

    public LinkedHashMap<String, String> returnAllEvents(String country) throws Exception {
        JSONArray eventsRaw = apiCall("events/" + year + "/simple");
        LinkedHashMap<String, String> events = new LinkedHashMap<String, String>();

        for (Object objMatch: eventsRaw) {
            JSONObject eventRawSpecific = (JSONObject) objMatch;

            if (!country.equals(eventRawSpecific.get("country"))) {
                continue;
            }
            events.put((String) eventRawSpecific.get("key"),(String) eventRawSpecific.get("name"));
        }

        return events;
    }



    public static class IndividualTeamScore {
        int teamId;
        String robotNumber;
        int matchNumber;

        //Year Specific Stuff
        String taxi;
        String endgame;

        public int getClimbPoints() {
            switch (endgame) {
                case "Low":
                    return 4;
                case "Mid":
                    return 6;
                case "High":
                    return 10;
                case "Traversal":
                    return 15;
                case "None":
                default:
                    return 0;
            }
        }
    }

    public static class AllianceScore {
        int matchNumber;

        /**
         * Alliance score at end of match (includes fouls)
         */
        int score;
        int autoScore;

        /**
         * Number of foul points from the OPPOSING alliance
         */
        int foulPoints;
        int teleopPoints;

        //Year Specific Stuff
        int teleopCargoLower;
        int teleopCargoUpper;
        int autoCargoScore;
        int endgamePoints;

        List<IndividualTeamScore> teams;
    }

    public static class Match{
        int matchNumber;
        AllianceScore red;
        AllianceScore blue;

        public List<IndividualTeamScore> getAllBreakdowns() {
            List<IndividualTeamScore> result = new ArrayList<>();
            result.addAll(red.teams);
            result.addAll(blue.teams);
            return result;
        }
    }
}
