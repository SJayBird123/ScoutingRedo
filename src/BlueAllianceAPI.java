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
    private int currentYear;

    public BlueAllianceAPI(String apiKey, int currentYear, int selectedYear) {
        this.apiKey = apiKey;
        year = selectedYear;
        this.currentYear = currentYear;
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
        ArrayList<String> countries = new ArrayList<>();

        for (Object objMatch: eventsRaw) {
            JSONObject eventRawSpecific = (JSONObject) objMatch;
            String matchCountry = (String) eventRawSpecific.get("country");
            boolean isCountryPresent = false;

            if(matchCountry == null){
                continue;
            }

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
        LinkedHashMap<String, String> events = new LinkedHashMap<>();

        for (Object objMatch: eventsRaw) {
            JSONObject eventRawSpecific = (JSONObject) objMatch;

            if (!country.equals(eventRawSpecific.get("country"))) {
                continue;
            }
            events.put((String) eventRawSpecific.get("key"),(String) eventRawSpecific.get("name"));
        }

        return events;
    }


    public List<Match> getMatchBreakdowns(String eventId) throws Exception {
        JSONArray matches = apiCall("event/" + eventId + "/matches");

        List<Match> results = new ArrayList<>();

        for (Object objMatch: matches){
            JSONObject rawMatch = (JSONObject) objMatch;
            // Only parse qualifier matches
            if(!"qm".equals(rawMatch.get("comp_level") )){
                continue;
            }

            Match match = new Match();

            //This is a yikes added 940am mar 5 to bypass null exception thrown cuz no one has scores yet
            try {
                match.matchNumber = ((Number) rawMatch.get("match_number")).intValue();
                match.red = extractBreakdowns(rawMatch, "red");
                match.blue = extractBreakdowns(rawMatch, "blue");
            }catch(Exception e){
                System.err.println("Error parsing match");
                continue;
            }

            results.add(match);
        }

        return results;
    }

    private AllianceScore extractBreakdowns(JSONObject rawMatch, String allianceColor) throws Exception{
        JSONObject alliances = (JSONObject) rawMatch.get("alliances");
        JSONObject currentAlliance = (JSONObject) alliances.get(allianceColor);
        JSONArray teamKeys = (JSONArray) currentAlliance.get("team_keys");

        JSONObject scoreBreakdownsRaw = (JSONObject) rawMatch.get("score_breakdown");
        JSONObject scoreBreakdownByAlliance = (JSONObject) scoreBreakdownsRaw.get(allianceColor);

        AllianceScore alliance = new AllianceScore();


        alliance.teams = new ArrayList<>();
        alliance.autoCargoScore = ((Number) scoreBreakdownByAlliance.get("autoCargoPoints")).intValue();
        alliance.autoScore = ((Number) scoreBreakdownByAlliance.get("autoPoints")).intValue();
        alliance.endgamePoints = ((Number) scoreBreakdownByAlliance.get("endgamePoints")).intValue();
        alliance.foulPoints = ((Number) scoreBreakdownByAlliance.get("foulPoints")).intValue();
        alliance.matchNumber = ((Number) rawMatch.get("match_number")).intValue();
        alliance.score = ((Number) scoreBreakdownByAlliance.get("totalPoints")).intValue();
        alliance.teleopCargoLower =
                ((Number) scoreBreakdownByAlliance.get("teleopCargoLowerBlue")).intValue() +
                        ((Number) scoreBreakdownByAlliance.get("teleopCargoLowerFar")).intValue() +
                        ((Number) scoreBreakdownByAlliance.get("teleopCargoLowerNear")).intValue() +
                        ((Number) scoreBreakdownByAlliance.get("teleopCargoLowerRed")).intValue();
        alliance.teleopCargoUpper =
                ((Number) scoreBreakdownByAlliance.get("teleopCargoUpperBlue")).intValue() +
                        ((Number) scoreBreakdownByAlliance.get("teleopCargoUpperFar")).intValue() +
                        ((Number) scoreBreakdownByAlliance.get("teleopCargoUpperNear")).intValue() +
                        ((Number) scoreBreakdownByAlliance.get("teleopCargoUpperRed")).intValue();
        alliance.teleopPoints = ((Number) scoreBreakdownByAlliance.get("teleopPoints")).intValue();


        for(int i = 1; i<=3;i++){
            String team = (String) teamKeys.get(i-1);

            IndividualTeamInfo teamScore = new IndividualTeamInfo();
            teamScore.teamId = Integer.parseInt(team.substring(3));
            teamScore.robotNumber = "Robot "+i;
            teamScore.matchNumber = ((Number) rawMatch.get("match_number")).intValue();

            //Year Specific
            teamScore.taxi = (String) scoreBreakdownByAlliance.get("taxiRobot"+i);
            teamScore.endgame = (String) scoreBreakdownByAlliance.get("endgameRobot"+i);
            alliance.teams.add(teamScore);
        }

        return alliance;
    }


    public static class IndividualTeamInfo {
        int teamId;
        String robotNumber;
        int matchNumber;

        //Year Specific Stuff
        String taxi;
        String endgame;

        public int getClimbPoints() {
            return switch (endgame) {
                case "Low" -> 4;
                case "Mid" -> 6;
                case "High" -> 10;
                case "Traversal" -> 15;
                default -> 0;
            };
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

        List<IndividualTeamInfo> teams;

        //Year Specific Stuff
        int teleopCargoLower;
        int teleopCargoUpper;
        int autoCargoScore;
        int endgamePoints;
    }

    public static class Match{
        int matchNumber;
        AllianceScore red;
        AllianceScore blue;

        public List<IndividualTeamInfo> getAllBreakdowns() {
            List<IndividualTeamInfo> result = new ArrayList<>();
            result.addAll(red.teams);
            result.addAll(blue.teams);
            return result;
        }
    }
}
