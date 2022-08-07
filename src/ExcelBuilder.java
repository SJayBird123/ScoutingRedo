import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Constructs the spreadsheet containing all the data.
 */
public class ExcelBuilder {
    /**
     * All OPR calculations and the corresponding teams
     */
    Map<String, Double>[] OPRs;
    /**
     * List of all team numbers in order
     */
    List<Integer> teamNames;
    /**
     * List of all Match objects for event
     */
    List<BlueAllianceAPI.Match> matches;
    /**
     * All teams and all their corresponding IndividualTeamInfo objects across all matches
     */
    Map<Integer, List<BlueAllianceAPI.IndividualTeamInfo>> scoresByTeam;
    /**
     * The year selected for scouting data.
     */
    int year;
    /**
     * The current year.
     */
    int currentYear;

    /**
     * Set all OPRs, team numbers, IndividualTeamInfo for all teams, matches, selected year, and current year
     * @param OPRs All OPR calculations and the corresponding teams.
     * @param teamNames List of all team numbers in order.
     * @param scoresByTeam All teams and all their corresponding IndividualTeamInfo objects across all matches.
     * @param matches List of all Match objects for event.
     * @param year The year selected for scouting data.
     * @param currentYear The current year.
     */
    public ExcelBuilder(Map<String, Double>[] OPRs, List<Integer> teamNames,
                        Map<Integer, List<BlueAllianceAPI.IndividualTeamInfo>> scoresByTeam,
                        List<BlueAllianceAPI.Match> matches, int year, int currentYear) {
        this.OPRs = OPRs;
        this.teamNames = teamNames;
        this.matches = matches;
        this.scoresByTeam = scoresByTeam;
        this.year = year;
        this.currentYear = currentYear;
    }

    /**
     * Builds the Workbook containing all of the individual sheets. Calls methods that create summary and team sheets.
     * @return XSSFWorkbook containing the spreadsheet.
     */
    public XSSFWorkbook build() {
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet summarySheet = workbook.createSheet("Summary");
        summarySheet(summarySheet);

        if(year == currentYear){
            for (int teamName : this.teamNames) {
                teamSheet(teamName,workbook);
            }
        }

        return workbook;
    }

    /**
     * Creates the Summary sheet
     * @param sheet The XSSFSheet being worked on.
     */
    private void summarySheet(XSSFSheet sheet) {
        //Sets the header
        {
            Row row = sheet.createRow(0);

            row.createCell(0).setCellValue("Team #");
            row.createCell(1).setCellValue("OPR");
            row.createCell(2).setCellValue("Auto OPR");
            row.createCell(3).setCellValue("Teleop OPR");
            row.createCell(4).setCellValue("Endgame OPR");
            row.createCell(5).setCellValue("DPR");
            row.createCell(6).setCellValue("penalty DPR");

            if(year == currentYear) {
                row.createCell(7).setCellValue("Low OPR");
                row.createCell(8).setCellValue("High OPR");
                row.createCell(9).setCellValue("Endgame OPR (hang adjusted)");

                row.createCell(10).setCellValue("Average Hang");
                row.createCell(11).setCellValue("None #");
                row.createCell(12).setCellValue("Low #");
                row.createCell(13).setCellValue("Mid #");
                row.createCell(14).setCellValue("High #");
                row.createCell(15).setCellValue("Traversal #");
            }
        }

        int rowNum = 1;

        //For every team
        for (int teamKey : teamNames) {
            //Create a new row with first cell being the team number
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(teamKey);

            //For every OPR type
            for(int i =0; i<OPRs.length;i++){
                //Fill each cell with the OPR for the team to the tenths place
                double opr = OPRs[i].getOrDefault(teamKey, Double.NaN);
                if(!Double.isNaN(opr))
                    row.createCell(i+1).setCellValue(Math.round(10*opr)/10.0);
            }

            if(year == currentYear){
                endGames currentTeamEndgames = new endGames(teamKey);

                for (BlueAllianceAPI.IndividualTeamInfo score : scoresByTeam.get(teamKey)) {
                    currentTeamEndgames.endgameAddMethod(score.endgame);
                }

                int endgamesTotal = 0;
                for (int value : currentTeamEndgames.endGameTotals.values()) {
                    endgamesTotal += value;
                }

                double averageHangScore = Math.round(
                        (currentTeamEndgames.endGameTotals.get("Low") * 4 +
                                currentTeamEndgames.endGameTotals.get("Mid") * 6 +
                                currentTeamEndgames.endGameTotals.get("High") * 10 +
                                currentTeamEndgames.endGameTotals.get("Traversal") * 15.0) /
                                (endgamesTotal) * 10
                ) / 10.0;

                row.createCell(OPRs.length+1).setCellValue(averageHangScore);
                row.createCell(OPRs.length+2).setCellValue(currentTeamEndgames.endGameTotals.get("None"));
                row.createCell(OPRs.length+3).setCellValue(currentTeamEndgames.endGameTotals.get("Low"));
                row.createCell(OPRs.length+4).setCellValue(currentTeamEndgames.endGameTotals.get("Mid"));
                row.createCell(OPRs.length+5).setCellValue(currentTeamEndgames.endGameTotals.get("High"));
                row.createCell(OPRs.length+6).setCellValue(currentTeamEndgames.endGameTotals.get("Traversal"));
            }
        }

        summarySheetStyling(sheet);
    }

    /**
     * Adds styling to the summary sheet
     * @param sheet The XSSFSheet being worked on.
     */
    void summarySheetStyling(XSSFSheet sheet){
        for(int i =0; i<sheet.getRow(0).getHeight();i++){
            //Resize all columns so they make sense
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Creates a sheet for the given team
     * @param teamName The number of the team the sheet corresponds to
     * @param workbook The workbook being operated in.
     */
    private void teamSheet(int teamName, XSSFWorkbook workbook){
        XSSFSheet sheet = workbook.createSheet(Integer.toString(teamName));
        List<BlueAllianceAPI.IndividualTeamInfo> teamMatches = scoresByTeam.get(teamName);

        teamMatches = teamMatches
                .stream()
                .sorted(Comparator.comparingInt(match -> match.matchNumber))
                .collect(Collectors.toList());

        // Add header
        int rowNum = 0;
        {
            Row header = sheet.createRow(rowNum++);

            header.createCell(0).setCellValue("Match #");
            header.createCell(1).setCellValue("Taxi");
            header.createCell(2).setCellValue("Endgame");
            header.createCell(3).setCellValue("Robot #");
        }

        for (BlueAllianceAPI.IndividualTeamInfo match : teamMatches) {
            Row row = sheet.createRow(rowNum);
            rowNum++;

            row.createCell(0).setCellValue(match.matchNumber);
            row.createCell(1).setCellValue(match.taxi);
            row.createCell(2).setCellValue(match.endgame);
            row.createCell(3).setCellValue(match.robotNumber);
        }

        teamSheetStyling(sheet);
    }

    /**
     * Adds styling to the team sheet
     * @param sheet The XSSFSheet being worked on.
     */
    void teamSheetStyling(XSSFSheet sheet){
        for(int i =0; i<sheet.getRow(0).getHeight();i++){
            sheet.autoSizeColumn(i);
        }
    }

}

/**
 * An object that records all hanging states (2022) for a given team.
 */
class endGames {
    int teamName;
    LinkedHashMap<String, Integer> endGameTotals = new LinkedHashMap<>();

    endGames(int teamName) {
        this.teamName = teamName;
        endGameTotals.put("None", 0);
        endGameTotals.put("Low", 0);
        endGameTotals.put("Mid", 0);
        endGameTotals.put("High", 0);
        endGameTotals.put("Traversal", 0);
        endGameTotals.put("Average", 0);
    }

    public void endgameAddMethod(String state) {
        endGameTotals.replace(state, endGameTotals.get(state)+1);
    }
}
