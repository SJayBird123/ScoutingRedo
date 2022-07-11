import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class BlueAllianceAPI {

    private final String apiKey;

    private int year = 2022;

    public BlueAllianceAPI(String apiKey) {
        this.apiKey = apiKey;
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
                if(matchCountry.equals(addedCountry)){
                    isCountryPresent = true;
                }
            }

            if(isCountryPresent == false){
                countries.add(matchCountry);
            }
        }

        Collections.sort(countries);
        return countries;
    }


}
