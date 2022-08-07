import Jama.Matrix;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Class where bigger calculations occur. Seemed cleaner that way.
 */
public class Calculations {

    /**
     * All matches in a given event.
     */
    List<BlueAllianceAPI.Match> matches;
    /**
     * All relevant team numbers.
     */
    List<Integer> teamNames;

    /**
     * Sets all matches and team numbers
     * @param matches  List of all Match objects for event.
     * @param teamNames  List of all team numbers for event.
     */
    public Calculations(List<BlueAllianceAPI.Match> matches, List<Integer> teamNames){
        this.matches = matches;
        this.teamNames = teamNames;
    }

    /**
     * Calculates OPR for a given type of value
     * @param extractScore All of a certain type of data from AllianceScore to calculate OPR for (includes team numbers).
     * @return A Map of all team numbers and OPRs
     */
    public Map<Integer, Double> calculateOPR(ToIntFunction<BlueAllianceAPI.AllianceScore> extractScore)     {
        System.out.println("calculating OPR for " + teamNames.size() + " teams, " + matches.size() + " matches");

        double[][] teamPresence = new double[matches.size() * 2][teamNames.size()];
        double[][] scores = new double[matches.size() * 2][1];

        for (int i = 0; i < matches.size(); i++) {
            BlueAllianceAPI.Match match = matches.get(i);
            int redScore = extractScore.applyAsInt(match.red);
            int blueScore = extractScore.applyAsInt(match.blue);

            scores[2 * i + 0][0] = redScore;
            scores[2 * i + 1][0] = blueScore;

            // Mark red teams as having shown up
            for (BlueAllianceAPI.IndividualTeamInfo score : match.red.teams) {
                int teamIdx = teamNames.indexOf(score.teamId);
                teamPresence[2 * i + 0][teamIdx] = 1;
            }
            for (BlueAllianceAPI.IndividualTeamInfo score : match.blue.teams) {
                int teamIdx = teamNames.indexOf(score.teamId);
                teamPresence[2 * i + 1][teamIdx] = 1;
            }
        }

        try {
            Matrix A = new Matrix(teamPresence);
            Matrix b = new Matrix(scores);

            Matrix x = A.solve(b);
            Matrix r = A.times(x).minus(b);
            double rnorm = r.normInf();
            System.out.println("OPR error: " + rnorm);

            Map<Integer, Double> oprMap = new HashMap<>();
            for (int i = 0; i < teamNames.size(); i++) {
                int E = teamNames.get(i);
                double e = x.get(i, 0);
                oprMap.put(E, e);
            }
            return oprMap;

        } catch (Exception e) {
            e.printStackTrace();
            Map<Integer, Double> oprMap = new HashMap<>();
            return oprMap;
        }
    }

    /**
     * Calculates DPR for a given type of value
     * @param extractScore All of a certain type of data from AllianceScore to calculate DPR for (includes team numbers).
     * @return A Map of all team numbers and DPRs
     */
    public Map<Integer, Double> calculateDPR(ToIntFunction<BlueAllianceAPI.AllianceScore> extractScore) {
        System.out.println("calculating OPR for " + teamNames.size() + " teams, " + matches.size() + " matches");

        double[][] teamPresence = new double[matches.size() * 2][teamNames.size()];
        double[][] scores = new double[matches.size() * 2][1];

        for (int i = 0; i < matches.size(); i++) {
            BlueAllianceAPI.Match match = matches.get(i);
            int redScore = extractScore.applyAsInt(match.red);
            int blueScore = extractScore.applyAsInt(match.blue);

            scores[2 * i + 0][0] = blueScore;
            scores[2 * i + 1][0] = redScore;

            // Mark red teams as having shown up
            for (BlueAllianceAPI.IndividualTeamInfo score : match.red.teams) {
                int teamIdx = teamNames.indexOf(score.teamId);
                teamPresence[2 * i + 0][teamIdx] = 1;
            }
            for (BlueAllianceAPI.IndividualTeamInfo score : match.blue.teams) {
                int teamIdx = teamNames.indexOf(score.teamId);
                teamPresence[2 * i + 1][teamIdx] = 1;
            }
        }

        try {
            Matrix A = new Matrix(teamPresence);
            Matrix b = new Matrix(scores);

            Matrix x = A.solve(b);
            Matrix r = A.times(x).minus(b);
            double rnorm = r.normInf();
            System.out.println("OPR error: " + rnorm);

            Map<Integer, Double> oprMap = new HashMap<>();
            for (int i = 0; i < teamNames.size(); i++) {
                int E = teamNames.get(i);
                double e = x.get(i, 0);
                oprMap.put(E, e);
            }
            return oprMap;

        } catch (Exception e) {
            e.printStackTrace();
            Map<Integer, Double> oprMap = new HashMap<>();
            return oprMap;
        }

    }

    /**
     * Adjusts the Endgame OPR values for the teams own average hang contribution.
     * @param endgameOPRall The unadjusted Endgame OPR for all teams.
     * @param scoresByTeam All teams and all their corresponding IndividualTeamInfo objects across all matches.
     * @return A HashMap of all team numbers and Adjusted OPRs
     */
    public HashMap<Integer, Double> hangOPRAdjustedCalc(Map<Integer, Double> endgameOPRall,
        Map<Integer, List<BlueAllianceAPI.IndividualTeamInfo>> scoresByTeam){

        HashMap<Integer, Double> hangOprAdjusted = new HashMap<>();

        for (int teamName : teamNames) {
            double endgameOPReach = endgameOPRall.getOrDefault(teamName, Double.NaN);
            if (!Double.isNaN(endgameOPReach)) {
                double totalClimbPoints = 0;
                for (BlueAllianceAPI.IndividualTeamInfo E : scoresByTeam.get(teamName)) {
                    totalClimbPoints += E.getClimbPoints();
                }
                hangOprAdjusted.put(teamName, endgameOPReach - totalClimbPoints / scoresByTeam.get(teamName).size());

            }
        }

        return hangOprAdjusted;
    }

}
