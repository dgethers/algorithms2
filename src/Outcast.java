import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: outzider
 * Date: 11/15/13
 * Time: 11:16 AM
 */
public class Outcast {

    private final WordNet wordNet;

    private final class NounDistance implements Comparable<NounDistance> {
        private final String noun;
        private final int distance;

        private NounDistance(String inNoun, int inDistance) {
            this.noun = inNoun;
            this.distance = inDistance;
        }

        private String getNoun() {
            return noun;
        }

        @Override
        public int compareTo(NounDistance nounDistance) {
            return Integer.valueOf(nounDistance.distance).compareTo(distance);
        }
    }

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        this.wordNet = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        List<NounDistance> nounDistances = new ArrayList<NounDistance>();
        for (int i = 0; i < nouns.length; i++) {
            int nounSum = 0;
            for (int j = 0; j < nouns.length; j++) {
                nounSum = nounSum + wordNet.distance(nouns[i], nouns[j]);
            }
            nounDistances.add(new NounDistance(nouns[i], nounSum));
        }
        Collections.sort(nounDistances);

        return nounDistances.get(0).getNoun();
    }

    // for unit testing of this class (such as the one below)
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            String[] nouns = In.readStrings(args[t]);
//            long startTime = System.currentTimeMillis();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
//            long endTime = System.currentTimeMillis();
//            StdOut.println("Runtime " + (endTime - startTime));
        }
    }
}
