import Jama.Matrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;


public class Calculations {

    List<BlueAllianceAPI.Match> matches;
    List<Integer> teamNames;

    public Calculations(List<BlueAllianceAPI.Match> matches, List<Integer> teamNames){
        this.matches = matches;
        this.teamNames = teamNames;
    }

    public Map<Integer, Double> calculateOPR(ToIntFunction<BlueAllianceAPI.AllianceScore> extractScore) {

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

}
