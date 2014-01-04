import java.util.*;

/**
 * User: outzider
 * Date: 12/27/13
 * Time: 5:37 PM
 */
public class BoggleSolver {

    private static class Position {
        int x;
        int y;

        private Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    private final Set<String> dict;
    private final TrieST<Integer> trie;

    // Initializes the data structure using the given array of strings as the dictionary.
    // (You can assume each word in the dictionary contains only the uppercase letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        dict = new TreeSet<String>(Arrays.asList(dictionary));
        trie = new TrieST<Integer>();

        int value = 0;
        for (String word : dict) {
            trie.put(word, value++);
        }

    }

    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        boolean[][] marked = new boolean[board.rows()][board.cols()];
        List<String> validWords = new ArrayList<String>();


        for (int row = 0; row < board.rows(); row++) {
            columnLoop:
            for (int column = 0; column < board.cols(); column++) {
                Stack<Position> stack = new Stack<Position>();
                StringBuilder word = new StringBuilder();

                char letter = board.getLetter(row, column);
                word.append(letter);
                Position current = new Position(row, column);
                marked[row][column] = true;
                Stack<Position> nextPositions = getNextPositions(current, board.rows(), board.cols());
                for (Position position : nextPositions) {
                    stack.push(position);
                }

                while (stack.size() > 0) {
                    Position next = stack.pop();
                    marked[next.x][next.y] = true;
                    word.append(board.getLetter(next.x, next.y));

                    if (word.length() >= 3) {
                        if (dict.contains(word.toString())) {
                            validWords.add(word.toString());

                            clearMarkedArray(board, marked);

                            continue columnLoop;
                        } else {
                            word.deleteCharAt(word.length() - 1);
                            marked[next.x][next.y] = false;
                        }
                    } else {

                        addNextPositionsToStack(board, marked, stack, next);
                    }
                }
            }

            clearMarkedArray(board, marked);
        }

        return validWords;
    }

    private void addNextPositionsToStack(BoggleBoard board, boolean[][] marked, Stack<Position> stack, Position next) {
        Stack<Position> positions = getNextPositions(next, board.rows(), board.cols());
        for (Position position : positions) {
            if (!marked[position.x][position.y]) {
                stack.push(position);
            }
        }
    }

    private void clearMarkedArray(BoggleBoard board, boolean[][] marked) {
        for (int i = 0; i < board.rows(); i++) {
            for (int j = 0; j < board.cols(); j++) {
                marked[i][j] = false;
            }
        }
    }

    private Stack<Position> getNextPositions(Position coordinates, int maxRows, int maxColumns) {
        Stack<Position> positions = new Stack<Position>();

        if (coordinates.x > 0)
            positions.push(new Position(coordinates.x - 1, coordinates.y));

        if (coordinates.x > 0 && coordinates.y < maxColumns - 1)
            positions.push(new Position(coordinates.x - 1, coordinates.y + 1));

        if (coordinates.y < maxColumns - 1)
            positions.push(new Position(coordinates.x, coordinates.y + 1));

        if (coordinates.y < maxColumns - 1 && coordinates.x < maxRows - 1)
            positions.push(new Position(coordinates.x + 1, coordinates.y + 1));

        if (coordinates.x < maxRows - 1)
            positions.push(new Position(coordinates.x + 1, coordinates.y));

        if (coordinates.y > 0 && coordinates.x > 0)
            positions.push(new Position(coordinates.x - 1, coordinates.y - 1));

        if (coordinates.y > 0)
            positions.push(new Position(coordinates.x, coordinates.y - 1));

        if (coordinates.x < maxRows - 1 && coordinates.y > 0)
            positions.push(new Position(coordinates.x + 1, coordinates.y - 1));

        return positions;
    }

    // Returns the score of the given word if it is in the dictionary, zero otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        //TODO: Implement this

        return 0;
    }

    public static void main(String[] args)
    {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        for (String word : solver.getAllValidWords(board))
        {
            StdOut.println(word);
            score += solver.scoreOf(word);
        }
        StdOut.println("Score = " + score);
    }
}
