import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;

public class Test {

    static String apiKey = "khASaff3i538qIxLZuxqC3g6dv55ZLHC8ztwsNfYPAHenKU6ymjwSfE8HqMvqNqL";

    public static void main(String[] args){
        JSONArray eventsRaw = null;
        System.out.println("hello");

        ArrayList<String> keys = new ArrayList<String>();
        ArrayList<String> names = new ArrayList<String>();
        LinkedHashMap<String, String> events = new LinkedHashMap<String, String>();

        try {
            URL matchesURL = new URL("https://www.thebluealliance.com/api/v3/events/2022/simple");
            HttpsURLConnection con = (HttpsURLConnection) matchesURL.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "MOEScouting");
            con.setRequestProperty("X-TBA-Auth-Key", apiKey);

            int responseCode = con.getResponseCode();
            System.out.println("This is " + responseCode);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                JSONParser parser = new JSONParser();
                eventsRaw = (JSONArray) parser.parse(in);
            }
        }catch(Exception e){
        }


        HashMap<String,Integer> wordCounts = new HashMap<String,Integer>(50,10);

        for (Object objMatch: eventsRaw) {
            JSONObject eventRawSpecific = (JSONObject) objMatch;

            String w = (String) eventRawSpecific.get("country");
            Integer i = wordCounts.get(w);
            if(i == null) wordCounts.put(w, 1);
            else wordCounts.put(w, i + 1);

            if (!"USA".equals(eventRawSpecific.get("country"))) {
                continue;
            }

            //keys.add((String) eventRawSpecific.get("key"));
            //names.add((String) eventRawSpecific.get("name"));

            events.put((String) eventRawSpecific.get("key"),(String) eventRawSpecific.get("name"));
        }
        for (String i : events.keySet()) {
            System.out.print(i);
            System.out.print(" : ");
            System.out.println(events.get(i));
        }

        System.out.println(wordCounts);

    }
}
