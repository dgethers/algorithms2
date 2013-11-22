import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: outzider
 * Date: 11/9/13
 * Time: 6:28 PM
 */
public class SAP {

    private final Digraph digraph;

    private final class Ancestor implements Comparable<Ancestor> {
        private final int vertex;
        private final int totalDistance;

        private Ancestor(int inVertex, int vDistance, int wDistance) {
            this.vertex = inVertex;
            this.totalDistance = vDistance + wDistance;
        }

        private int getVertex() {
            return vertex;
        }

        @Override
        public int compareTo(Ancestor o) {
            return Integer.valueOf(totalDistance).compareTo(o.totalDistance);
        }
    }

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        digraph = new Digraph(G);
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        illegalInputParameterCheck(v, w);

        int ancestor = ancestor(v, w);

        if (ancestor == -1) {
            return -1;
        } else {
            BreadthFirstDirectedPaths vBreadthFirst = new BreadthFirstDirectedPaths(digraph, v);
            BreadthFirstDirectedPaths wBreadthFirst = new BreadthFirstDirectedPaths(digraph, w);

            return vBreadthFirst.distTo(ancestor) + wBreadthFirst.distTo(ancestor);
        }
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        illegalInputParameterCheck(v, w);

        List<Ancestor> ancestors = new ArrayList<Ancestor>();
        BreadthFirstDirectedPaths vBreadthFirst = new BreadthFirstDirectedPaths(digraph, v);
        BreadthFirstDirectedPaths wBreadthFirst = new BreadthFirstDirectedPaths(digraph, w);

        for (int i = 0; i < digraph.V(); i++) {
            if (vBreadthFirst.hasPathTo(i) && wBreadthFirst.hasPathTo(i)) {
                ancestors.add(new Ancestor(i, vBreadthFirst.distTo(i), wBreadthFirst.distTo(i)));
            }
        }

        if (ancestors.size() < 1) {
            return -1;
        } else {
            Collections.sort(ancestors);
            return ancestors.get(0).getVertex();
        }

    }

    private void illegalInputParameterCheck(int v, int w) {
        checkRange(v);

        checkRange(w);
    }

    private void illegalInputParameterCheck(Iterable<Integer> v, Iterable<Integer> w) {
        for (int vertex : v) {
            checkRange(vertex);
        }

        for (int vertex : w) {
            checkRange(vertex);
        }
    }

    private void checkRange(int vertex) {
        if (vertex < 0 || vertex > digraph.V() - 1) {
            throw new IndexOutOfBoundsException();
        }
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        illegalInputParameterCheck(v, w);

        int ancestor = ancestor(v, w);

        if (ancestor == -1) {
            return -1;
        } else {
            BreadthFirstDirectedPaths vBreadthFirst = new BreadthFirstDirectedPaths(digraph, v);
            BreadthFirstDirectedPaths wBreadthFirst = new BreadthFirstDirectedPaths(digraph, w);

            return vBreadthFirst.distTo(ancestor) + wBreadthFirst.distTo(ancestor);
        }
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        illegalInputParameterCheck(v, w);

        List<Ancestor> ancestors = new ArrayList<Ancestor>();
        BreadthFirstDirectedPaths vBreadthFirst = new BreadthFirstDirectedPaths(digraph, v);
        BreadthFirstDirectedPaths wBreadthFirst = new BreadthFirstDirectedPaths(digraph, w);

        for (int i = 0; i < digraph.V(); i++) {
            if (vBreadthFirst.hasPathTo(i) && wBreadthFirst.hasPathTo(i)) {
                ancestors.add(new Ancestor(i, vBreadthFirst.distTo(i), wBreadthFirst.distTo(i)));
            }
        }

        if (ancestors.size() < 1) {
            return -1;
        } else {
            Collections.sort(ancestors);
            return ancestors.get(0).getVertex();
        }
    }

    // for unit testing of this class (such as the one below)
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}