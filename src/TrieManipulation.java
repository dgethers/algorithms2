import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * User: outzider
 * Date: 1/5/14
 * Time: 1:57 PM
 */
public class TrieManipulation {

    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        Set<String> sortedDictionary = new TreeSet<String>(Arrays.asList(dictionary));
        TrieST<Integer> trie = new TrieST<Integer>();
        int value = 0;
        for (String dictWord : sortedDictionary) {
            trie.put(dictWord, value++);
        }
        /*
        DIFFER
        DIFFERENCE
        DIFFERENCES
        DIFFERENT
        */
        Integer nonExistentWord = trie.get("IRRIG");
        System.out.println("nonExistentWord = " + nonExistentWord);
        Integer existentWord = trie.get("DIFFERENT");
        System.out.println("existentWord = " + existentWord);
        Iterable<String> keysWithPrefix = trie.keysWithPrefix("DIFFER");
        System.out.println(keysWithPrefix);
        Iterable<String> nonExistentPrefix = trie.keysWithPrefix("TIX");
        System.out.println("nonExistentPrefix = " + nonExistentPrefix);
        System.out.println(nonExistentPrefix.iterator().hasNext());

    }
}
