import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class Main {

    /**
     * Key to access the API
     */
    static String apiKey = "khASaff3i538qIxLZuxqC3g6dv55ZLHC8ztwsNfYPAHenKU6ymjwSfE8HqMvqNqL";
    /**
     * The year being scouted
     */
    private static int year;
    /**
     * The current year
     */
    private static int currentYear = 2022;

    /*
    *What the program does:
    *A year is selected. All events that occurred that year are parsed for all countries that hosted an event.
    * A country is selected. All events in that country during that year are displayed. An event is selected.
    * All matches are stored from that event. All matches are parsed for all team names. Matches are broken
    * down for all relevant data and stored in robot, alliance, and match data formats. scoresByTeam is made
    * and stores all individual robot data per match in a list and stores it as a value with each key being
    * the team number. Scoring data is used to determine the various OPRs and averages, data that is used as
    * a predictive/advisory metric. That data is organized in a summary sheet by team. There are individual
    * team pages that display individual match data. This is stored to a file in a location that is selected
    * in a prompt by the user. A rerun prompt is shown to ask whether or not the user wants to rerun the
    * program to select another competition.
    */

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
            List<Integer> teamNames = API.getTeamNames(matches);


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

            Map<String, Double>[] OPRs;
            if(year ==currentYear) {
                Map<Integer, Double> OPR = calc.calculateOPR(alliance -> alliance.score - alliance.foulPoints);
                Map<Integer, Double> autoOPR = calc.calculateOPR(alliance -> alliance.autoScore);
                Map<Integer, Double> teleopOPR = calc.calculateOPR(alliance -> alliance.teleopPoints);
                Map<Integer, Double> endgameOPR = calc.calculateOPR(alliance -> alliance.endgamePoints);
                Map<Integer, Double> DPR = calc.calculateDPR(alliance -> alliance.score - alliance.foulPoints);
                Map<Integer, Double> penaltyDPR = calc.calculateDPR(alliance -> alliance.foulPoints);

                Map<Integer, Double> highOpr = calc.calculateOPR(alliance -> alliance.teleopCargoUpper);
                Map<Integer, Double> lowOpr = calc.calculateOPR(alliance -> alliance.teleopCargoLower);
                Map<Integer, Double> hangOprAdjusted = calc.hangOPRAdjustedCalc(endgameOPR, scoresByTeam);

                OPRs = new Map[]{
                        OPR, autoOPR, teleopOPR, endgameOPR, DPR, penaltyDPR, lowOpr, highOpr, hangOprAdjusted
                };
            }else{
                Map<Integer, Double> OPR = calc.calculateOPR(alliance -> alliance.score - alliance.foulPoints);
                Map<Integer, Double> autoOPR = calc.calculateOPR(alliance -> alliance.autoScore);
                Map<Integer, Double> teleopOPR = calc.calculateOPR(alliance -> alliance.teleopPoints);
                Map<Integer, Double> endgameOPR = calc.calculateOPR(alliance -> alliance.endgamePoints);
                Map<Integer, Double> DPR = calc.calculateDPR(alliance -> alliance.score - alliance.foulPoints);
                Map<Integer, Double> penaltyDPR = calc.calculateDPR(alliance -> alliance.foulPoints);

                OPRs = new Map[]{
                        OPR, autoOPR, teleopOPR, endgameOPR, DPR, penaltyDPR
                };
            }

            ExcelBuilder ExcelBuilder = new ExcelBuilder(OPRs, teamNames, scoresByTeam, matches, year, currentYear);
            XSSFWorkbook workbook = ExcelBuilder.build();
            File saveFile = UI.compileFile(selectedEventKey);

            // Save workbook to file
            try (FileOutputStream outputStream = new FileOutputStream(saveFile)) {
                workbook.write(outputStream);
            }
            if(UI.reRun()){
                String[] E = new String[0];
                main(E);
            }

            System.exit(0);
        }catch (Throwable t) {
            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));
            JOptionPane.showMessageDialog(null, errors.getBuffer().toString(), "An error occurred", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        System.exit(0);
    }
}
