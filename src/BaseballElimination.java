import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * User: outzider
 * Date: 12/22/13
 * Time: 8:51 PM
 */
public class BaseballElimination {

    private class Team {
        private int position;
        private String name;
        private int wins;
        private int loses;
        private int totalRemainingGames;
        private int[] remainingGamesAgainstOtherTeams;
    }

    private class Game {
        private final String name;
        private final int remainingGames;

        private Game(String name, int remainingGames) {
            this.name = name;
            this.remainingGames = remainingGames;
        }
    }

    private static final int SOURCE = 0;

    private final Map<String, Team> teams;
    private final Map<Integer, Integer> vertexToTeamMap;
    private final Map<Integer, Integer> teamToVertexMap;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        teams = new LinkedHashMap<String, Team>();
        vertexToTeamMap = new HashMap<Integer, Integer>();
        teamToVertexMap = new HashMap<Integer, Integer>();

        In in = new In(filename);
        int numberOfTeams = Integer.parseInt(in.readLine());
        int position = 0;
        while (in.hasNextLine()) {
            String rawLine = in.readLine();
            String line = rawLine.trim();
            String[] entries = line.split("\\s+");
            Team team = new Team();
            team.position = position++;
            team.name = entries[0];
            team.wins = Integer.parseInt(entries[1]);
            team.loses = Integer.parseInt(entries[2]);
            team.totalRemainingGames = Integer.parseInt(entries[3]);
            int[] remainingGamesPerTeam = new int[numberOfTeams];
            int index = 0;
            for (int i = 4; i < entries.length; i++) {
                remainingGamesPerTeam[index++] = Integer.parseInt(entries[i]);
            }
            team.remainingGamesAgainstOtherTeams = remainingGamesPerTeam;

            teams.put(team.name, team);
        }
    }

    // number of teams
    public int numberOfTeams() {
        return teams.size();
    }

    // all teams
    public Iterable<String> teams() {
        return teams.keySet();
    }

    // number of wins for given team
    public int wins(String team) {
        checkIfInputIsValid(team);

        return teams.get(team).wins;
    }

    // number of losses for given team
    public int losses(String team) {
        checkIfInputIsValid(team);

        return teams.get(team).loses;
    }

    // number of remaining games for given team
    public int remaining(String team) {
        checkIfInputIsValid(team);

        return teams.get(team).totalRemainingGames;
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        checkIfInputIsValid(team1);
        checkIfInputIsValid(team2);

        Team firstTeam = teams.get(team1);
        Team secondTeam = teams.get(team2);

        return firstTeam.remainingGamesAgainstOtherTeams[secondTeam.position];
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        checkIfInputIsValid(team);

        Team chosenTeam = teams.get(team);

        List<String> eliminationTeams = getListOfTeamNamesThatTrivallyEliminateChoosenTeam(chosenTeam);

        if (eliminationTeams.size() > 0) {

            return true;
        } else {

            List<Game> gameList = getListOfAllUniqueGamesExcludingCurrentTeam(chosenTeam);

            Set<Integer> uniqueTeams = getAllUniqueTeamsFromGames(gameList);

            final int sink = (gameList.size() + uniqueTeams.size() + 2) - 1;

            FlowNetwork flowNetwork = createFlowNetwork(gameList, uniqueTeams, chosenTeam, sink);

            new FordFulkerson(flowNetwork, SOURCE, sink);
            Iterable<FlowEdge> edges = flowNetwork.edges();
            for (FlowEdge edge : edges) {
                if (edge.from() == 0 && edge.flow() < edge.capacity()) {
                    return true;
                }
            }
        }

        return false;
    }

    private void checkIfInputIsValid(String team) {
        if (!teams.containsKey(team)) {
            throw new IllegalArgumentException("Invalid team used");
        }
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        checkIfInputIsValid(team);

        Team currTeam = teams.get(team);

        List<String> eliminationTeams = getListOfTeamNamesThatTrivallyEliminateChoosenTeam(currTeam);

        if (eliminationTeams.size() < 1) {

            List<Game> gameList = getListOfAllUniqueGamesExcludingCurrentTeam(currTeam);

            Set<Integer> uniqueTeams = getAllUniqueTeamsFromGames(gameList);

            final int sink = (gameList.size() + uniqueTeams.size() + 2) - 1;

            FlowNetwork flowNetwork = createFlowNetwork(gameList, uniqueTeams, currTeam, sink);

            FordFulkerson fordFulkerson = new FordFulkerson(flowNetwork, SOURCE, sink);

            for (int i = 1 + gameList.size(); i < gameList.size() + uniqueTeams.size() + 1; i++) {
                boolean inTheCut = fordFulkerson.inCut(i);
                if (inTheCut) {
                    eliminationTeams.add(getTeamByPosition(vertexToTeamMap.get(i)).name);
                }
            }
        }

        return eliminationTeams.size() == 0 ? null : eliminationTeams;
    }

    private FlowNetwork createFlowNetwork(List<Game> gameList, Set<Integer> uniqueTeams, Team choosenTeam, final int sink) {
        final int totalVertexCount = gameList.size() + uniqueTeams.size() + 2;
        int gameVertex = 1;
        int teamVertex = gameVertex + gameList.size();

        clearAndPopulateVertexToTeamAndTeamToVertexMaps(uniqueTeams, teamVertex);

        FlowNetwork flowNetwork = new FlowNetwork(totalVertexCount);
        for (Game game : gameList) {
            FlowEdge gameEdge = new FlowEdge(SOURCE, gameVertex, game.remainingGames);
            flowNetwork.addEdge(gameEdge);
            String[] teamPosition = game.name.split("-");
            for (int i = 0; i < teamPosition.length; i++) {
                int team = Integer.parseInt(teamPosition[i]);
                FlowEdge teamEdge = new FlowEdge(gameVertex, teamToVertexMap.get(team), Double.POSITIVE_INFINITY);
                flowNetwork.addEdge(teamEdge);

                boolean edgeAlreadyExists = false;
                Iterable<FlowEdge> flowNetworkEdges = flowNetwork.edges();
                for (FlowEdge flowNetworkEdge : flowNetworkEdges) {
                    if (flowNetworkEdge.from() == teamToVertexMap.get(team) && flowNetworkEdge.to() == sink) {
                        edgeAlreadyExists = true;
                    }
                }
                FlowEdge sinkEdge = new FlowEdge(teamToVertexMap.get(team), sink, choosenTeam.wins + choosenTeam.totalRemainingGames - getTeamByPosition(team).wins);

                if (!edgeAlreadyExists) {
                    flowNetwork.addEdge(sinkEdge);
                }

            }
            gameVertex++;
        }

        return flowNetwork;
    }

    private Set<Integer> getAllUniqueTeamsFromGames(List<Game> gameList) {
        Set<Integer> uniqueTeams = new HashSet<Integer>();

        for (Game game : gameList) {
            String[] teamPositions = game.name.split("-");
            for (String teamPosition : teamPositions) {
                uniqueTeams.add(Integer.parseInt(teamPosition));
            }
        }

        return uniqueTeams;
    }

    private List<Game> getListOfAllUniqueGamesExcludingCurrentTeam(Team chosenTeam) {
        List<Game> gameList = new ArrayList<Game>();

        for (Team team : teams.values()) {
            if (!team.name.equals(chosenTeam.name)) { //skip the same team row
                int[] remainingGames = team.remainingGamesAgainstOtherTeams;
                for (int i = 0; i < remainingGames.length; i++) {
                    String gameName = team.position + "-" + i;
                    Game game = new Game(gameName, remainingGames[i]);
                    if (chosenTeam.position != i && !doesGameAlreadyExist(gameList, gameName) && remainingGames[i] > 0) { //skip the same column
                        gameList.add(game);
                    }
                }
            }
        }

        return gameList;
    }

    private Team getTeamByPosition(int position) {
        for (Team team : teams.values()) {
            if (team.position == position) {
                return team;
            }
        }

        return null;
    }

    private static boolean doesGameAlreadyExist(List<Game> gameList, String gameName) {
        String[] gameTeams = gameName.split("-");
        String reverseName = gameTeams[1] + "-" + gameTeams[0];
        for (Game game : gameList) {
            if (game.name.equals(gameName) || game.name.equals(reverseName)) {
                return true;
            }
        }

        return false;
    }

    private void clearAndPopulateVertexToTeamAndTeamToVertexMaps(Set<Integer> uniqueTeams, int teamVertexStart) {
        int teamVertex = teamVertexStart;
        teamToVertexMap.clear();
        vertexToTeamMap.clear();

        for (int uniqueTeam : uniqueTeams) {
            teamToVertexMap.put(uniqueTeam, teamVertex);
            vertexToTeamMap.put(teamVertex, uniqueTeam);
            teamVertex++;
        }
    }

    private List<String> getListOfTeamNamesThatTrivallyEliminateChoosenTeam(Team team) {
        List<String> teamNames = new ArrayList<String>();
        for (Team currentTeam : teams.values()) {
            if (team.wins + team.totalRemainingGames - currentTeam.wins < 0) {
                teamNames.add(currentTeam.name);
            }
        }

        return teamNames;
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team))
                    StdOut.print(t + " ");
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}