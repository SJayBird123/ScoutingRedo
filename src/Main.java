import java.util.*;

public class Main {

    static String apiKey = "khASaff3i538qIxLZuxqC3g6dv55ZLHC8ztwsNfYPAHenKU6ymjwSfE8HqMvqNqL";
    private static int year;
    private static int currentYear = 2022;

    public static void main(String[] args) throws Exception {
        try {

            Prompt UI = new Prompt();
            year = UI.selectYear();

            BlueAllianceAPI API = new BlueAllianceAPI(apiKey, currentYear, year);

            ArrayList<String> countries = API.returnCountries();
            String selectedCountry = UI.selectCountry(countries);

            LinkedHashMap<String,String> eventsInCountry= API.returnAllEvents(selectedCountry);
            String selectedEventKey = UI.selectEvent(eventsInCountry);
            System.out.println(selectedEventKey);

            List<BlueAllianceAPI.Match> matches = API.getMatchBreakdowns(selectedEventKey);
            List<Integer> teamNames = API.getTeamNames(selectedEventKey, matches);


            Calculations calc = new Calculations(matches,teamNames);

            Map<Integer, Double> OPR = calc.calculateOPR(alliance -> alliance.score - alliance.foulPoints);
            Map<Integer, Double> autoOPR = calc.calculateOPR(alliance -> alliance.autoScore);
            Map<Integer, Double> teleopOPR = calc.calculateOPR(alliance -> alliance.teleopPoints);
            Map<Integer, Double> endgameOPR = calc.calculateOPR(alliance -> alliance.endgamePoints);
            Map<Integer, Double> DPR = calc.calculateDPR(alliance -> alliance.score - alliance.foulPoints);
            Map<Integer, Double> penaltyDPR = calc.calculateOPR(alliance -> alliance.foulPoints);

            Map<Integer, Double> highOpr = calc.calculateOPR(alliance -> alliance.teleopCargoUpper);
            Map<Integer, Double> lowOpr = calc.calculateOPR(alliance -> alliance.teleopCargoLower);

            Map<Integer, Double> hangOprAdjusted = new HashMap<Integer, Double>();


            System.out.println(OPR);
            System.out.println(autoOPR);
            System.out.println(teleopOPR);
            System.out.println(endgameOPR);
            System.out.println(DPR);
            System.out.println(penaltyDPR);
            System.out.println(highOpr);
            System.out.println(lowOpr);

        }catch(Exception e){

        }
        System.exit(0);
    }
}
