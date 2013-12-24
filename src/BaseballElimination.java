import java.util.*;

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

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Team{");
            sb.append("position=").append(position);
            sb.append(", name='").append(name).append('\'');
            sb.append(", wins=").append(wins);
            sb.append(", loses=").append(loses);
            sb.append(", totalRemainingGames=").append(totalRemainingGames);
            sb.append(", remainingGamesAgainstOtherTeams=").append(Arrays.toString(remainingGamesAgainstOtherTeams));
            sb.append('}');
            return sb.toString();
        }
    }

    private class Game {
        private String name;
        private int remainingGames;

        private Game(String name, int remainingGames) {
            this.name = name;
            this.remainingGames = remainingGames;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Game{");
            sb.append("name='").append(name).append('\'');
            sb.append(", remainingGames=").append(remainingGames);
            sb.append('}');
            return sb.toString();
        }
    }

    private final Map<String, Team> teams;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        teams = new LinkedHashMap<String, Team>();
        In in = new In(filename);
        int numberOfTeams = Integer.parseInt(in.readLine());
        int position = 0;
        while (in.hasNextLine()) {
            String line = in.readLine();
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

//            System.out.println(team);
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
        return teams.get(team).wins;
    }

    // number of losses for given team
    public int losses(String team) {
        return teams.get(team).loses;
    }

    // number of remaining games for given team
    public int remaining(String team) {
        return teams.get(team).totalRemainingGames;
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        Team firstTeam = teams.get(team1);
        Team secondTeam = teams.get(team2);

        return firstTeam.remainingGamesAgainstOtherTeams[secondTeam.position];
    }

    // is given team eliminated?
    public boolean isEliminated(String teamName) {
        Team currTeam = teams.get(teamName);
        //trivally eliminate a team
        for (Team team : teams.values()) {
            if (currTeam.wins + currTeam.totalRemainingGames - team.wins <= 0) {
                return true;
            }
        }

        //untrivally eliminate a team
        List<Game> gameList = new ArrayList<Game>();

        //get all unique games
        for (Team team : teams.values()) {
            if (!team.name.equals(teamName)) { //skip the same team row
                int[] remainingGames = team.remainingGamesAgainstOtherTeams;
                for (int i = 0; i < remainingGames.length; i++) {
                    String gameName = team.position + "-" + i;
                    Game game = new Game(gameName, remainingGames[i]);
                    if (currTeam.position != i && !doesGameAlreadyExist(gameList, gameName) && remainingGames[i] > 0) { //skip the same column
                        gameList.add(game);
                    }
                }
            }

        }

//        System.out.println("gameList: " + gameList);


        //get all unique teams
        Set<Integer> uniqueTeams = new HashSet<Integer>();
        for (Game game : gameList) {
            String[] teamPositions = game.name.split("-");
            for (int i = 0; i < teamPositions.length; i++) {
                uniqueTeams.add(Integer.parseInt(teamPositions[i]));
            }
        }

//        System.out.println("uniqueTeams: " + uniqueTeams);

        int verticieCount = gameList.size() + uniqueTeams.size() + 2;
        final int SOURCE = 0;
        final int SINK = verticieCount - 1;
        int gameVertex = 1;
        int teamVertex = gameVertex + gameList.size();

        Map<Integer, Integer> teamToVerticieMap = new HashMap<Integer, Integer>();
        for (Integer uniqueTeam : uniqueTeams) {
            teamToVerticieMap.put(uniqueTeam, teamVertex++);
        }


        FlowNetwork flowNetwork = new FlowNetwork(verticieCount);
        for (Game game : gameList) {
            FlowEdge gameEdge = new FlowEdge(SOURCE, gameVertex, game.remainingGames);
            flowNetwork.addEdge(gameEdge);
            String[] teamPosition = game.name.split("-");
//            int teamOffset = gameList.size() + 1;
            for (int i = 0; i < teamPosition.length; i++) {
                Integer team = Integer.parseInt(teamPosition[i]);
//                    FlowEdge teamEdge = new FlowEdge(gameVertex, team + teamOffset, Double.POSITIVE_INFINITY);
                FlowEdge teamEdge = new FlowEdge(gameVertex, teamToVerticieMap.get(team), Double.POSITIVE_INFINITY);
//                boolean edgeAlreadyExists = false;
//                Iterable<FlowEdge> flowNetworkEdges = flowNetwork.edges();
//                for (FlowEdge flowNetworkEdge : flowNetworkEdges) {
//                    if (flowNetworkEdge.from() == gameVertex && flowNetworkEdge.to() == teamToVerticieMap.get(team)) {
//                        edgeAlreadyExists = true;
//                    }
//                }

//                if (!edgeAlreadyExists) {
                flowNetwork.addEdge(teamEdge);
//                }
//                    flowNetwork.addEdge(teamEdge);
//                    FlowEdge sinkEdge = new FlowEdge(team + teamOffset, SINK, currTeam.wins + currTeam.totalRemainingGames - getTeamByPosition(team).wins);
                boolean edgeAlreadyExists = false;
                Iterable<FlowEdge> flowNetworkEdges = flowNetwork.edges();
                for (FlowEdge flowNetworkEdge : flowNetworkEdges) {
                    if (flowNetworkEdge.from() == teamToVerticieMap.get(team) && flowNetworkEdge.to() == SINK) {
                        edgeAlreadyExists = true;
                    }
                }

                FlowEdge sinkEdge = new FlowEdge(teamToVerticieMap.get(team), SINK, currTeam.wins + currTeam.totalRemainingGames - getTeamByPosition(team).wins);
//                    FlowEdge sinkEdge = new FlowEdge(team + teamOffset, SINK, 99);

                if (!edgeAlreadyExists) {
                    flowNetwork.addEdge(sinkEdge);
                }
//                    teamVertex++;

            }
            gameVertex++;
        }


//        System.out.println(flowNetwork);
        FordFulkerson fordFulkerson = new FordFulkerson(flowNetwork, SOURCE, SINK);
//        System.out.println(flowNetwork);
        Iterable<FlowEdge> edges = flowNetwork.edges();
        for (FlowEdge edge : edges) {
            if (edge.from() == 0 && edge.flow() < edge.capacity()) {
                return true;
            }
        }

        return false;
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
        for (Game game : gameList) {
            if (game.name.equals(gameName) || game.name.equals(new StringBuilder(gameName).reverse().toString())) {
                return true;
            }
        }

        return false;
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String teamName) {
        Team currTeam = teams.get(teamName);

        //trivally eliminate a team
        List<String> result = new ArrayList<String>();
        boolean trivallyEliminated = false;

        for (Team team : teams.values()) {
            if (currTeam.wins + currTeam.totalRemainingGames - team.wins < 0) {
                result.add(team.name);
                trivallyEliminated = true;
            }
        }

        if (!trivallyEliminated) {

            List<Game> gameList = new ArrayList<Game>();

            //get all unique games
            for (Team team : teams.values()) {
                if (!team.name.equals(teamName)) { //skip the same team row
                    int[] remainingGames = team.remainingGamesAgainstOtherTeams;
                    for (int i = 0; i < remainingGames.length; i++) {
                        String gameName = team.position + "-" + i;
                        Game game = new Game(gameName, remainingGames[i]);
                        if (currTeam.position != i && !doesGameAlreadyExist(gameList, gameName) && remainingGames[i] > 0) { //skip the same column
                            gameList.add(game);
                        }
                    }
                }

            }

//        System.out.println("gameList: " + gameList);


            //get all unique teams
            Set<Integer> uniqueTeams = new HashSet<Integer>();
            for (Game game : gameList) {
                String[] teamPositions = game.name.split("-");
                for (int i = 0; i < teamPositions.length; i++) {
                    uniqueTeams.add(Integer.parseInt(teamPositions[i]));
                }
            }

//        System.out.println("uniqueTeams: " + uniqueTeams);

            int verticieCount = gameList.size() + uniqueTeams.size() + 2;
            final int SOURCE = 0;
            final int SINK = verticieCount - 1;
            int gameVertex = 1;
            int teamVertex = gameVertex + gameList.size();

            Map<Integer, Integer> teamToVerticieMap = new HashMap<Integer, Integer>();
            Map<Integer, Integer> verticieToTeamMap = new HashMap<Integer, Integer>();
            for (Integer uniqueTeam : uniqueTeams) {
                teamToVerticieMap.put(uniqueTeam, teamVertex);
                verticieToTeamMap.put(teamVertex, uniqueTeam);
                teamVertex++;
            }


            FlowNetwork flowNetwork = new FlowNetwork(verticieCount);
            for (Game game : gameList) {
                FlowEdge gameEdge = new FlowEdge(SOURCE, gameVertex, game.remainingGames);
                flowNetwork.addEdge(gameEdge);
                String[] teamPosition = game.name.split("-");
//            int teamOffset = gameList.size() + 1;
                for (int i = 0; i < teamPosition.length; i++) {
                    Integer team = Integer.parseInt(teamPosition[i]);
//                    FlowEdge teamEdge = new FlowEdge(gameVertex, team + teamOffset, Double.POSITIVE_INFINITY);
                    FlowEdge teamEdge = new FlowEdge(gameVertex, teamToVerticieMap.get(team), Double.POSITIVE_INFINITY);
//                boolean edgeAlreadyExists = false;
//                Iterable<FlowEdge> flowNetworkEdges = flowNetwork.edges();
//                for (FlowEdge flowNetworkEdge : flowNetworkEdges) {
//                    if (flowNetworkEdge.from() == gameVertex && flowNetworkEdge.to() == teamToVerticieMap.get(team)) {
//                        edgeAlreadyExists = true;
//                    }
//                }

//                if (!edgeAlreadyExists) {
                    flowNetwork.addEdge(teamEdge);
//                }
//                    FlowEdge sinkEdge = new FlowEdge(team + teamOffset, SINK, currTeam.wins + currTeam.totalRemainingGames - getTeamByPosition(team).wins);
                    boolean edgeAlreadyExists = false;
                    Iterable<FlowEdge> flowNetworkEdges = flowNetwork.edges();
                    for (FlowEdge flowNetworkEdge : flowNetworkEdges) {
                        if (flowNetworkEdge.from() == teamToVerticieMap.get(team) && flowNetworkEdge.to() == SINK) {
                            edgeAlreadyExists = true;
                        }
                    }

                    FlowEdge sinkEdge = new FlowEdge(teamToVerticieMap.get(team), SINK, currTeam.wins + currTeam.totalRemainingGames - getTeamByPosition(team).wins);
//                    FlowEdge sinkEdge = new FlowEdge(team + teamOffset, SINK, 99);

                    if (!edgeAlreadyExists) {
                        flowNetwork.addEdge(sinkEdge);
                    }
//                FlowEdge sinkEdge = new FlowEdge(teamToVerticieMap.get(team), SINK, currTeam.wins + currTeam.totalRemainingGames - getTeamByPosition(team).wins);
//                    FlowEdge sinkEdge = new FlowEdge(team + teamOffset, SINK, 99);
//                flowNetwork.addEdge(sinkEdge);
//                    teamVertex++;

                }
                gameVertex++;
            }

            teamVertex = gameVertex + gameList.size();
//        System.out.println(flowNetwork);
            FordFulkerson fordFulkerson = new FordFulkerson(flowNetwork, SOURCE, SINK);

//        List<String> result = new ArrayList<String>();
            for (int i = 1 + gameList.size(); i < gameList.size() + uniqueTeams.size() + 1; i++) {
                boolean inTheCut = fordFulkerson.inCut(i);
                if (inTheCut) {
//                System.out.println(i + " is in the mincut: " + getTeamByPosition(verticieToTeamMap.get(i)).name);
                    result.add(getTeamByPosition(verticieToTeamMap.get(i)).name);
                }
            }
        }

        return result;
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
//        System.out.println("number of teams: " + division.numberOfTeams());
//        System.out.println("wins for team Atlanta(83) is: " + division.wins("Atlanta"));
//        System.out.println("wins for team Philadelphia(80) is: " + division.wins("Philadelphia"));
//        System.out.println("loses for team Atlanta(71) is: " + division.losses("Atlanta"));
//        System.out.println("loses for team Philadelphia(79) is: " + division.losses("Philadelphia"));
//        System.out.println("remaining games for team Atlanta(8) is: " + division.remaining("Atlanta"));
//        System.out.println("loses for team Philadelphia(3) is: " + division.remaining("Philadelphia"));
//        System.out.println("remaining games of Atlanta vs Philadelphia(1) is: " + division.against("Atlanta", "Philadelphia"));
//        System.out.println("remaining games of Philadelphia vs Atlanta(1) is: " + division.against("Philadelphia", "Atlanta"));
//        System.out.println("remaining games of New York vs Atlanta(6) is: " + division.against("New_York", "Atlanta"));
//        System.out.println("Montreal is eliminated: " + division.isEliminated("Montreal"));
//        System.out.println("Philadelphia is eliminated: " + division.isEliminated("Philadelphia"));
//        System.out.println("New_York is eliminated: " + division.isEliminated("New_York"));
//        System.out.println("Atlanta is eliminated: " + division.isEliminated("Atlanta"));
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
