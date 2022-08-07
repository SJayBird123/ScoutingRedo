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

/**
 * Class where the API is accessed and JSON data is parsed and sorted.
 */
public class BlueAllianceAPI {

    /**
     * The key used to access TBA.
     */
    private final String apiKey;
    /**
     * The current year.
     */
    private int year;
    /**
     * The year that was selected for scouting data.
     */
    private int currentYear;

    /**
     * Set the API key, current year, and selected year.
     * @param apiKey The key used to access TBA.
     * @param currentYear The current year.
     * @param selectedYear The year that was selected for scouting data.
     */
    public BlueAllianceAPI(String apiKey, int currentYear, int selectedYear) {
        this.apiKey = apiKey;
        year = selectedYear;
        this.currentYear = currentYear;
    }

    /**
     * Used to connect to and pull a raw JSONArray from a specific URL on TBA.
     * @param endpoint The specific URL ending we pull data from.
     * @return A JSONArray of all the data from a specific call.
     * @throws Exception
     */
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

    /**
     * Returns all countries that hosted an event by using apiCall() to get all events and then parsing through
     * them for all unique countries.
     * @return An ArrayList of all countries that hosted an event.
     * @throws Exception
     */
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

    /**
     * Returns all event keys and event names for a particular country, including "irrelevant" events.
     * @param country Accepts country name to return events from.
     * @return A LinkedHashMap of all event keys (Key) and the event names (Value).
     * @throws Exception
     */
    public LinkedHashMap<String, String> returnAllEvents(String country) throws Exception {
        JSONArray eventsRaw = apiCall("events/" + year);
        LinkedHashMap<String, String> events = new LinkedHashMap<>();

        for (Object objMatch: eventsRaw) {
            JSONObject eventRawSpecific = (JSONObject) objMatch;

            if (!country.equals(eventRawSpecific.get("country"))) {
                continue;
            }

            /*
             *So there are a million FRC events that are not actual calculatable competitions like one off stuff
             *or einstein field or covid events or a million other things. There is data for the type of event but none
             *of it is consistent. Because of this I can't effectively sort out which events are poopoo without
             *trying to run it and look for an error. I also have not spent much effort trying to solve this and maybe
             * if I used a brain cell or 2 I could find a value to sort by but as of right now I don't care enough to
             * fix it. (8-4-22)
             * */
//          For some reason events that are perfectly normal have playoff_types of null. !?!?!??!?!?!
//          Also it won't let me compare the JSONobject to an Int no matter what I try, but I'm also dumb so ye
//          if(eventRawSpecific.get("playoff_type") == null){
//              continue;
//          }

            events.put((String) eventRawSpecific.get("key"),(String) eventRawSpecific.get("name"));
        }

        return events;
    }

    /**
     * Returns list of match objects by parsing an apiCall() for matches from a specific event. Matches are sorted for
     * quals and broken down by alliance data.
     * @param eventId The unique key for the event.
     * @return A List of type Match.
     * @throws Exception
     */
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

            //Fun comment written in 2022 I decided to keep in the redo lol
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

    /**
     * Returns alliance data for a given match and color.
     * @param rawMatch The raw JSONObject containing a data regard a specific match.
     * @param allianceColor The alliance color to break down.
     * @return An AllianceScore object containing information for the chosen alliance.
     * @throws Exception
     */
    private AllianceScore extractBreakdowns(JSONObject rawMatch, String allianceColor) throws Exception{
        JSONObject alliances = (JSONObject) rawMatch.get("alliances");
        JSONObject currentAlliance = (JSONObject) alliances.get(allianceColor);
        JSONArray teamKeys = (JSONArray) currentAlliance.get("team_keys");

        JSONObject scoreBreakdownsRaw = (JSONObject) rawMatch.get("score_breakdown");
        JSONObject scoreBreakdownByAlliance = (JSONObject) scoreBreakdownsRaw.get(allianceColor);

        AllianceScore alliance = new AllianceScore();

        alliance.teams = new ArrayList<>();
        alliance.autoScore = ((Number) scoreBreakdownByAlliance.get("autoPoints")).intValue();
        alliance.teleopPoints = ((Number) scoreBreakdownByAlliance.get("teleopPoints")).intValue();
        alliance.endgamePoints = ((Number) scoreBreakdownByAlliance.get("endgamePoints")).intValue();
        alliance.foulPoints = ((Number) scoreBreakdownByAlliance.get("foulPoints")).intValue();
        alliance.matchNumber = ((Number) rawMatch.get("match_number")).intValue();
        alliance.score = ((Number) scoreBreakdownByAlliance.get("totalPoints")).intValue();

        if(year == currentYear) {
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
            alliance.autoCargoScore = ((Number) scoreBreakdownByAlliance.get("autoCargoPoints")).intValue();
        }

        for(int i = 1; i<=3;i++){
            String team = (String) teamKeys.get(i-1);

            IndividualTeamInfo teamScore = new IndividualTeamInfo();
            teamScore.teamId = Integer.parseInt(team.substring(3));
            teamScore.robotNumber = "Robot "+i;
            teamScore.matchNumber = ((Number) rawMatch.get("match_number")).intValue();

            if(year == currentYear) {
                teamScore.taxi = (String) scoreBreakdownByAlliance.get("taxiRobot" + i);
                teamScore.endgame = (String) scoreBreakdownByAlliance.get("endgameRobot" + i);
            }
            alliance.teams.add(teamScore);
        }

        return alliance;
    }

    /**
     * Meant to return all team numbers in order but it turned out sometimes teams participate without playing in
     * the quals and get included in the teams call. Funny thing is I think we ran into this exact problem in the
     * beginning of build season 2022 and I completely forgot when remaking lol. Don't use this method.
     * @param eventId The unique key for the event.
     * @return Nothing don't use this method
     * @throws Exception
     */
    public List<Integer> getTeamNamesBad(String eventId) throws Exception {
        /* Weird problem where for 2022week0 there were teams in the finals that didnt play any quals !?!?!?
        JSONArray teamNamesRaw = apiCall("event/" + eventId + "/teams/keys");
        List<Integer> teamNames= new ArrayList<>();

        for(Object name : teamNamesRaw){
            teamNames.add (Integer.parseInt(((String) name).substring(3)));
        }

        Collections.sort(teamNames);
        return teamNames;

    */
        return null;
    }

    /**
     * Returns all team numbers in order by parsing the List of Match objects for all unique team numbers
     * @param matches List of all Match objects for selected event
     * @return List of team numbers in order
     * @throws Exception
     */
    public List<Integer> getTeamNames(List<Match> matches) throws Exception {
        List<Integer> teamNames= new ArrayList<>();

        for(Match match : matches){
            List<IndividualTeamInfo> resultsRaw = match.getAllBreakdowns();

            for(IndividualTeamInfo teamInfo : resultsRaw){
                int teamName = teamInfo.teamId;
                boolean isIn = false;

                for(Integer E: teamNames){
                    if(E == teamName){
                        isIn = true;
                        break;
                    }
                }

                if(!isIn || teamNames.size()==0) {
                    teamNames.add(teamName);
                }
            }
        }

        Collections.sort(teamNames);
        return teamNames;
    }

    /**
     * Each team's individual data per alliance.
     */
    public static class IndividualTeamInfo {
        int teamId;
        String robotNumber;
        int matchNumber;

        //Year Specific Stuff
        String taxi;
        String endgame;

        /**
         * Returns the number of points earned in hang state (2022). Also fancy switch statement. :)
         * @return int of endgame's corresponding value
         */
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

    /**
     * Each alliance's data per match.
     */
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

    /**
     * Each match's data.
     */
    public static class Match{
        int matchNumber;
        /**
         * Red alliance breakdown, including individual team data.
         */
        AllianceScore red;
        /**
         * Blue alliance breakdown, including individual team data.
         */
        AllianceScore blue;

        /**
         * Returns all individual team data for every team in the match, for both alliances.
         * @return List of IndividualTeamInfo
         */
        public List<IndividualTeamInfo> getAllBreakdowns() {
            List<IndividualTeamInfo> result = new ArrayList<>();
            result.addAll(red.teams);
            result.addAll(blue.teams);
            return result;
        }
    }
}
