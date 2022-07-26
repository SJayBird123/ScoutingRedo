import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
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
            Map<Integer, List<BlueAllianceAPI.IndividualTeamInfo>> scoresByTeam = new HashMap<>();

            // go through each match
            for(BlueAllianceAPI.Match match : matches){
                // go though each robot-specific score in the match
                for(BlueAllianceAPI.IndividualTeamInfo eachIndividualTeamInfoBreakDown : match.getAllBreakdowns()) {
                    //Take the match per team and add the score breakdown
                    List<BlueAllianceAPI.IndividualTeamInfo> scores = scoresByTeam.get(eachIndividualTeamInfoBreakDown.teamId);
                    if(scores == null){
                        scores = new ArrayList<>();
                        scoresByTeam.put(eachIndividualTeamInfoBreakDown.teamId,scores);
                    }

                    scores.add(eachIndividualTeamInfoBreakDown);
                }
            }

            Map<Integer, Double> OPR = calc.calculateOPR(alliance -> alliance.score - alliance.foulPoints);
            Map<Integer, Double> autoOPR = calc.calculateOPR(alliance -> alliance.autoScore);
            Map<Integer, Double> teleopOPR = calc.calculateOPR(alliance -> alliance.teleopPoints);
            Map<Integer, Double> endgameOPR = calc.calculateOPR(alliance -> alliance.endgamePoints);
            Map<Integer, Double> DPR = calc.calculateDPR(alliance -> alliance.score - alliance.foulPoints);
            Map<Integer, Double> penaltyDPR = calc.calculateDPR(alliance -> alliance.foulPoints);

            Map<Integer, Double> highOpr = calc.calculateOPR(alliance -> alliance.teleopCargoUpper);
            Map<Integer, Double> lowOpr = calc.calculateOPR(alliance -> alliance.teleopCargoLower);
            Map<Integer, Double> hangOprAdjusted = calc.hangOPRAdjustedCalc(endgameOPR,scoresByTeam);

            Map<String, Double>[] OPRs = new Map[] {
                    OPR, autoOPR, teleopOPR, endgameOPR, DPR, penaltyDPR, lowOpr, highOpr, hangOprAdjusted
            };


            ExcelBuilder ExcelBuilder = new ExcelBuilder(OPRs, teamNames, scoresByTeam, matches);
            XSSFWorkbook workbook = ExcelBuilder.build();
            File saveFile = UI.compileFile(selectedEventKey);

            // Save workbook to file
            try (FileOutputStream outputStream = new FileOutputStream(saveFile)) {
                workbook.write(outputStream);
            }

            System.exit(0);

        }catch(Exception e){

        }
        System.exit(0);
    }
}
