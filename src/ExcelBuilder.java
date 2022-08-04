import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExcelBuilder {
    Map<String, Double>[] OPRs;
    List<Integer> teamNames;
    List<BlueAllianceAPI.Match> matches;
    Map<Integer, List<BlueAllianceAPI.IndividualTeamInfo>> scoresByTeam;
    int year;
    int currentYear;

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

    public XSSFWorkbook build() {
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet summarySheet = workbook.createSheet("Summary");
        summarySheet(summarySheet,workbook);

        if(year == currentYear){
            for (int teamName : this.teamNames) {
                teamSheet(teamName,workbook);
            }
        }

        return workbook;
    }


    private void summarySheet(XSSFSheet sheet, XSSFWorkbook workbook) {

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

        for (int teamKey : teamNames) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(teamKey);

            for(int i =0; i<OPRs.length;i++){
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

        summarySheetStyling(sheet, workbook);
    }

    void summarySheetStyling(XSSFSheet sheet, XSSFWorkbook workbook){
        for(int i =0; i<sheet.getRow(0).getHeight();i++){
            sheet.autoSizeColumn(i);
        }
    }

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

        for(int i =0; i<sheet.getRow(0).getHeight();i++){
            sheet.autoSizeColumn(i);
        }
    }

}

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
