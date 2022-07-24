import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    static String apiKey = "khASaff3i538qIxLZuxqC3g6dv55ZLHC8ztwsNfYPAHenKU6ymjwSfE8HqMvqNqL";
    private static int year;
    private static int currentYear = 2022;

    public static void main(String[] args) throws Exception {
        try {

            Prompt UI = new Prompt();
            Calculations calc = new Calculations();

            year = UI.selectYear();

            BlueAllianceAPI API = new BlueAllianceAPI(apiKey,currentYear, year);

            ArrayList<String> countries = API.returnCountries();
            String selectedCountry = UI.selectCountry(countries);


            LinkedHashMap<String,String> eventsInCountry= API.returnAllEvents(selectedCountry);
            String selectedEventKey = UI.selectEvent(eventsInCountry);
            System.out.println(selectedEventKey);

            List<BlueAllianceAPI.Match> matches = API.getMatchBreakdowns(selectedEventKey);
            List<Integer> teamNames = API.getTeamNames(selectedEventKey, matches);


            Map<Integer, Double> OPR = calc.calculateOPR(alliance -> alliance.score - alliance.foulPoints, false,
                   matches, teamNames);

            System.out.println("OPR");
            System.out.println(OPR);
            System.out.println(teamNames);

        }catch(Exception e){

        }
        System.exit(0);
    }
}
