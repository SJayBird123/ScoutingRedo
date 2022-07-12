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

public class Main {

    static String apiKey = "khASaff3i538qIxLZuxqC3g6dv55ZLHC8ztwsNfYPAHenKU6ymjwSfE8HqMvqNqL";
    private static int year;

    public static void main(String[] args) throws Exception {
        try {

            Prompt UI = new Prompt();

            year = UI.selectYear();

            BlueAllianceAPI API = new BlueAllianceAPI(apiKey,year);

            ArrayList<String> countries = API.returnCountries();
            String selectedCountry = UI.selectCountry(countries);

            LinkedHashMap<String,String> eventsInCountry= API.returnAllEvents(selectedCountry);
            String selectedEventKey = UI.selectEvent(eventsInCountry);

            System.out.println(selectedEventKey);

        }catch(Exception e){

        }
        System.exit(0);
    }
}
