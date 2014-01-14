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
        int level;
        char letter;

        private Position(int x, int y, int level) {
            this.x = x;
            this.y = y;
            this.level = level;
        }

        private Position(int x, int y, char letter) {
            this.x = x;
            this.y = y;
            this.letter = letter;
        }

        @Override
        public String toString() {
            return String.format("(%d,%d) %s@%d", x, y, letter, level);
        }
    }

    private final TrieST<Integer> trie;
    private final Set<String> validWords;

    // Initializes the data structure using the given array of strings as the dictionary.
    // (You can assume each word in the dictionary contains only the uppercase letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        Set<String> dict = new TreeSet<String>(Arrays.asList(dictionary));
        trie = new TrieST<Integer>();

        int value = 0;
        for (String word : dict) {
            trie.put(word, value++);
        }

        validWords = new TreeSet<String>();
    }

    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {

        for (int row = 0; row < board.rows(); row++) {
            for (int column = 0; column < board.cols(); column++) {
                boolean[][] marked = new boolean[board.rows()][board.cols()];
                StringBuilder word = new StringBuilder();
                char letter = board.getLetter(row, column);
                Stack<Position> history = new Stack<Position>();
                Stack<Position> nextPositions = new Stack<Position>();


                Position current = new Position(row, column, letter);
                nextPositions.push(current);
                int currentLevel = 0;

                while (nextPositions.size() > 0) {
                    Position next = nextPositions.pop();

                    boolean shouldRemoveLetter = next.level < currentLevel;
                    if (shouldRemoveLetter) {
                        int levelDiff = currentLevel - next.level;
                        for (int i = 1; i <= levelDiff; i++) {
                            word.deleteCharAt(word.length() - 1);
                            Position historyPosition = history.pop();
                            marked[historyPosition.x][historyPosition.y] = false;
                        }
                        currentLevel = currentLevel - levelDiff;
                    }

                    history.push(next);
                    marked[next.x][next.y] = true;
                    word.append(board.getLetter(next.x, next.y));

                    //not a word and not a prefix
                    if (!(trie.keysWithPrefix(word.toString()).iterator().hasNext()) && trie.get(word.toString()) == null) {
                        removeLastLetterFromWord(marked, word, history, next);
                    } //word but not a prefix
                    else if (!trie.keysWithPrefix(word.toString()).iterator().hasNext() && trie.get(word.toString()) != null) {
                        addWordToValidWordList(word.toString());
                        removeLastLetterFromWord(marked, word, history, next);
                    } //is a prefix, is a word
                    else if (trie.keysWithPrefix(word.toString()).iterator().hasNext() && trie.get(word.toString()) != null) {
                        addWordToValidWordList(word.toString());
                        currentLevel++;
                        addNextPositionsToStack(board, marked, nextPositions, next, currentLevel);
                    } //is a prefix but not a word
                    else if (trie.keysWithPrefix(word.toString()).iterator().hasNext() && trie.get(word.toString()) == null) {
                        currentLevel++;
                        addNextPositionsToStack(board, marked, nextPositions, next, currentLevel);
                    }
                }
            }
        }

        return validWords;
    }

    private void removeLastLetterFromWord(boolean[][] marked, StringBuilder word, Stack<Position> history, Position current) {
        word.deleteCharAt(word.length() - 1);
        marked[current.x][current.y] = false;
        history.pop();
    }

    private void addWordToValidWordList(String word) {
        if (word.length() >= 3) {
            validWords.add(word);
        }
    }

    private void addNextPositionsToStack(BoggleBoard board, boolean[][] marked, Stack<Position> stack, Position next, int currentLevel) {
        List<Position> positions = getNextPositions(board, next, board.rows(), board.cols());
        for (Position position : positions) {
            if (!marked[position.x][position.y]) {
                position.level = position.level++;
                Position newPosition = new Position(position.x, position.y, currentLevel);
                newPosition.letter = board.getLetter(position.x, position.y);
                stack.push(newPosition);
            }
        }
    }

    private List<Position> getNextPositions(BoggleBoard board, Position coordinates, int maxRows, int maxColumns) {
        List<Position> positions = new ArrayList<Position>();

        if (coordinates.x > 0) {
            positions.add(new Position(coordinates.x - 1, coordinates.y, board.getLetter(coordinates.x - 1, coordinates.y)));
        }

        if (coordinates.x > 0 && coordinates.y < maxColumns - 1) {
            positions.add(new Position(coordinates.x - 1, coordinates.y + 1, board.getLetter(coordinates.x - 1, coordinates.y + 1)));
        }

        if (coordinates.y < maxColumns - 1) {
            positions.add(new Position(coordinates.x, coordinates.y + 1, board.getLetter(coordinates.x, coordinates.y + 1)));
        }

        if (coordinates.y < maxColumns - 1 && coordinates.x < maxRows - 1) {
            positions.add(new Position(coordinates.x + 1, coordinates.y + 1, board.getLetter(coordinates.x + 1, coordinates.y + 1)));
        }

        if (coordinates.x < maxRows - 1) {
            positions.add(new Position(coordinates.x + 1, coordinates.y, board.getLetter(coordinates.x + 1, coordinates.y)));
        }

        if (coordinates.y > 0 && coordinates.x > 0) {
            positions.add(new Position(coordinates.x - 1, coordinates.y - 1, board.getLetter(coordinates.x - 1, coordinates.y - 1)));
        }

        if (coordinates.y > 0) {
            positions.add(new Position(coordinates.x, coordinates.y - 1, board.getLetter(coordinates.x, coordinates.y - 1)));
        }

        if (coordinates.x < maxRows - 1 && coordinates.y > 0) {
            positions.add(new Position(coordinates.x + 1, coordinates.y - 1, board.getLetter(coordinates.x + 1, coordinates.y - 1)));
        }

        return positions;
    }

    // Returns the score of the given word if it is in the dictionary, zero otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        if (word.length() > 2 && word.length() < 5) {
            return 1;
        }

        if (word.length() == 5) {
            return 2;
        }

        if (word.length() == 6) {
            return 3;
        }

        if (word.length() == 7) {
            return 5;
        }

        if (word.length() > 7) {
            return 11;
        }

        return 0;
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        for (String word : solver.getAllValidWords(board)) {
            StdOut.println(word);
            score += solver.scoreOf(word);
        }
        StdOut.println("Score = " + score);
    }
}
