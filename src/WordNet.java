import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * User: outzider
 * Date: 11/9/13
 * Time: 6:20 PM
 */
public class WordNet {

    private Map<Integer, SynonymSet> synonymSets;
    private final Map<String, List<Integer>> allNouns;
    private final Digraph wordNet;
    private final SAP sap;

    private class SynonymSet {
        private Set<String> nouns;
        private List<Integer> hypernyms;

        private SynonymSet() {
            hypernyms = new ArrayList<Integer>();
        }

        private Set<String> getNouns() {
            return nouns;
        }

        private void setNouns(Set<String> inNouns) {
            this.nouns = inNouns;
        }

        private List<Integer> getHypernyms() {
            return hypernyms;
        }

        private void addHypernyms(List<Integer> inHypernyms) {
            this.hypernyms.addAll(inHypernyms);
        }
    }

    // constructor takes the name of the two input files
    /* DAG (Direct Acyclic Graph) - directed graph with no cycles. A rooted DAG means there is 1 vertex
    for which there are no incoming edges (indegree = 0) */

    public WordNet(String synsets, String hypernyms) {
        synonymSets = new LinkedHashMap<Integer, SynonymSet>();
        allNouns = new TreeMap<String, List<Integer>>();

        readAndParseSynsetsInputFile(synsets);
        readAndParseHypernymsInputFile(hypernyms);

        wordNet = new Digraph(synonymSets.size());
        for (int vertexId : synonymSets.keySet()) {
            for (int hypernymsId : synonymSets.get(vertexId).getHypernyms()) {
                wordNet.addEdge(vertexId, hypernymsId);
            }
        }

        checkForInvalidCycleAndMultipleRoots();

        sap = new SAP(wordNet);
    }

    private void checkForInvalidCycleAndMultipleRoots() {
        DirectedCycle directedCycle = new DirectedCycle(wordNet);
        if (directedCycle.hasCycle()) {
            throw new IllegalArgumentException("Wordnet contains a cycle");
        }

        int rootCount = 0;
        for (int i = 0; i < wordNet.V(); i++) {
            if (!wordNet.adj(i).iterator().hasNext()) {
                rootCount++;

                if (rootCount > 1) {
                    throw new IllegalArgumentException("Wordnet has more than one root");
                }
            }
        }
    }

    private void readAndParseSynsetsInputFile(String synsets) {
        In in = new In(synsets);
        String line = in.readLine();
        while (line != null) {
            SynonymSet synonymSet = new SynonymSet();

            Set nounSet = new LinkedHashSet();
            String[] parts = line.split(",");
            String[] nouns = parts[1].split(" ");
            nounSet.addAll(Arrays.asList(nouns));
            synonymSet.setNouns(nounSet);

            int id = Integer.parseInt(parts[0]);

            for (String noun : nouns) {
                if (allNouns.containsKey(noun)) {
                    List<Integer> ids = allNouns.get(noun);
                    ids.add(id);
                } else {
                    List<Integer> ids = new ArrayList<Integer>();
                    ids.add(id);
                    allNouns.put(noun, ids);
                }
            }

            synonymSets.put(id, synonymSet);

            line = in.readLine();
        }
    }

    private void readAndParseHypernymsInputFile(String hypernyms) {
        In in = new In(hypernyms);
        String line = in.readLine();
        while (line != null) {
            String[] parts = line.split(",");

            SynonymSet synonymSet = synonymSets.get(Integer.parseInt(parts[0]));

            String[] hypernymIds = Arrays.copyOfRange(parts, 1, parts.length);
            List<Integer> synsetHypernyms = new ArrayList<Integer>(hypernymIds.length);

            for (String id : hypernymIds) {
                synsetHypernyms.add(Integer.parseInt(id));
            }
            synonymSet.addHypernyms(synsetHypernyms);

            line = in.readLine();
        }
    }

    // the set of nouns (no duplicates), returned as an Iterable
    public Iterable<String> nouns() {
        return new HashSet<String>(allNouns.keySet());
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        return allNouns.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (isNoun(nounA) && isNoun(nounB)) {

            return sap.length(allNouns.get(nounA), allNouns.get(nounB));
        } else {
            throw new IllegalArgumentException();
        }
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (isNoun(nounA) && isNoun(nounB)) {

            int ancestor = sap.ancestor(allNouns.get(nounA), allNouns.get(nounB));

            Set<String> nouns = synonymSets.get(ancestor).getNouns();
            StringBuilder builder = new StringBuilder();
            Iterator<String> iterator = nouns.iterator();
            while (iterator.hasNext()) {
                String noun = iterator.next();
                builder.append(noun);
                if (iterator.hasNext()) {
                    builder.append(" ");
                }
            }

            return builder.toString();
        } else {
            throw new IllegalArgumentException();
        }
    }

    // for unit testing of this class
    public static void main(String[] args) {
//        long startTime = System.currentTimeMillis();
        WordNet wordNet = new WordNet(args[0], args[1]);
//        long endTime = System.currentTimeMillis();
//        System.out.println("Total time to create wordNet is: " + (endTime - startTime));
        System.out.printf("wordnet distance() for Black_Plague & black_marlin is %d\n", wordNet.distance("Black_Plague",
                "black_marlin"));
        System.out.printf("wordnet distance() for American_water_spaniel & histology is %d\n", wordNet.distance("American_water_spaniel",
                "histology"));
        System.out.printf("wordnet distance() for Brown_Swiss & barrel_roll is %d\n", wordNet.distance("Brown_Swiss", "barrel_roll"));
        System.out.printf("wordnet sap() for bicycle_clip & American_red_squirrel is %s\n", wordNet.sap("bicycle_clip", "American_red_squirrel"));
        System.out.printf("wordnet distance() for bicycle_clip & American_red_squirrel is %d\n", wordNet.distance("bicycle_clip", "American_red_squirrel"));
    }
}
