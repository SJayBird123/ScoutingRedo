import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
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

    public ExcelBuilder(Map<String, Double>[] OPRs, List<Integer> teamNames,
                        Map<Integer, List<BlueAllianceAPI.IndividualTeamInfo>> scoresByTeam,List<BlueAllianceAPI.Match> matches) {

        this.OPRs = OPRs;
        this.teamNames = teamNames;
        this.matches = matches;
        this.scoresByTeam = scoresByTeam;
    }


    public XSSFWorkbook build() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet summarySheet = workbook.createSheet("Summary");
        summarySheet(summarySheet);

        for (int teamname : this.teamNames) {
            XSSFSheet sheet = workbook.createSheet(Integer.toString(teamname));
            List<BlueAllianceAPI.IndividualTeamInfo> teamMatches = scoresByTeam.get(teamname);
            teamMatches = teamMatches
                    .stream()
                    .sorted(Comparator.comparingInt(match -> match.matchNumber))
                    .collect(Collectors.toList());
            // Add header
            int rowNum = 0;
            {
                Row header = sheet.createRow(rowNum++);
                XSSFFont bold = workbook.createFont();
                bold.setBold(true);
                CellStyle style = workbook.createCellStyle();
                style.setFont(bold);
                header.setRowStyle(style);

                Cell cell1 = header.createCell(0);
                cell1.setCellValue("Match #");

                Cell cell2 = header.createCell(1);
                cell2.setCellValue("Taxi");

                Cell cell3 = header.createCell(2);
                cell3.setCellValue("Endgame");

                Cell cell4 = header.createCell(3);
                cell4.setCellValue("Robot #");

            }

            for (BlueAllianceAPI.IndividualTeamInfo match : teamMatches) {
                Row row = sheet.createRow(rowNum);
                rowNum++;

                Cell matchNumberCell = row.createCell(0);
                matchNumberCell.setCellValue(match.matchNumber);
                Cell taxiCell = row.createCell(1);
                taxiCell.setCellValue(match.taxi);
                Cell endgameCell = row.createCell(2);
                endgameCell.setCellValue(match.endgame);
                Cell robotNumberCell = row.createCell(3);
                robotNumberCell.setCellValue(match.robotNumber);
            }
        }

        return workbook;
    }

    void summarySheet(XSSFSheet sheet) {
        {
            Row row = sheet.createRow(0);

            row.createCell(0).setCellValue("Team #");
            row.createCell(1).setCellValue("None #");
            row.createCell(2).setCellValue("Low #");
            row.createCell(3).setCellValue("Mid #");
            row.createCell(4).setCellValue("High #");
            row.createCell(5).setCellValue("Traversal #");
            row.createCell(6).setCellValue("Average Hang");
            row.createCell(7).setCellValue("OPR");
            row.createCell(8).setCellValue("DPR");

            row.createCell(9).setCellValue("Auto OPR");
            row.createCell(10).setCellValue("Low OPR");
            row.createCell(11).setCellValue("High OPR");
            row.createCell(12).setCellValue("Teleop OPR");
            row.createCell(13).setCellValue("Endgame OPR (adjusted)");
            row.createCell(14).setCellValue("penaltyDPR");
        }

        int rowNum = 1;

        for (int teamKey : teamNames) {

            endGames currentTeamEndgames = new endGames(teamKey);

            for (BlueAllianceAPI.IndividualTeamInfo score : scoresByTeam.get(teamKey)) {
                currentTeamEndgames.endgameAddMethod(score.endgame);
            }

            int endgamesTotal = 0;
            for (int value : currentTeamEndgames.endGameTotals.values()) {
                endgamesTotal += value;
            }

            double averageHangScore = Math.round(
                    (currentTeamEndgames.endGameTotals.get("Low")*4+
                            currentTeamEndgames.endGameTotals.get("Mid")*6+
                            currentTeamEndgames.endGameTotals.get("High")*10+
                            currentTeamEndgames.endGameTotals.get("Traversal")*15.0)/
                            (endgamesTotal)*100
            )/100.0;

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(teamKey);
            row.createCell(1).setCellValue(currentTeamEndgames.endGameTotals.get("None"));
            row.createCell(2).setCellValue(currentTeamEndgames.endGameTotals.get("Low"));
            row.createCell(3).setCellValue(currentTeamEndgames.endGameTotals.get("Mid"));
            row.createCell(4).setCellValue(currentTeamEndgames.endGameTotals.get("High"));
            row.createCell(5).setCellValue(currentTeamEndgames.endGameTotals.get("Traversal"));
            row.createCell(6).setCellValue(averageHangScore);

            for(int i =0; i<OPRs.length;i++){
                double opr = OPRs[i].getOrDefault(teamKey, Double.NaN);
                if(!Double.isNaN(opr))
                    row.createCell(7+i).setCellValue(Math.round(10*opr)/10.0);
            }
        }


    }

}

class endGames {
    int teamName;
    LinkedHashMap<String, int> endGameTotals = new LinkedHashMap<String, int>();

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
