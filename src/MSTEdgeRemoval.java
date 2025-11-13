import java.util.*;
import java.util.stream.Collectors;

public class MSTEdgeRemoval {

    static class Edge implements Comparable<Edge> {
        int src, dest, weight;

        Edge(int src, int dest, int weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }

        @Override
        public int compareTo(Edge other) {
            return Integer.compare(this.weight, other.weight);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Edge edge = (Edge) obj;
            return src == edge.src && dest == edge.dest && weight == edge.weight;
        }

        @Override
        public int hashCode() {
            return Objects.hash(src, dest, weight);
        }

        @Override
        public String toString() {
            return String.format("(%d-%d: %d)", src, dest, weight);
        }
    }

    static class Graph {
        int vertices;
        List<Edge> edges;

        Graph(int vertices) {
            this.vertices = vertices;
            this.edges = new ArrayList<>();
        }

        void addEdge(int src, int dest, int weight) {
            edges.add(new Edge(src, dest, weight));
        }
    }

    static class UnionFind {
        int[] parent, rank;

        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        boolean union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);

            if (rootX == rootY) return false;

            if (rank[rootX] < rank[rootY]) {
                parent[rootX] = rootY;
            } else if (rank[rootX] > rank[rootY]) {
                parent[rootY] = rootX;
            } else {
                parent[rootY] = rootX;
                rank[rootX]++;
            }
            return true;
        }
    }

    public static List<Edge> kruskalMST(Graph graph) {
        List<Edge> mst = new ArrayList<>();
        List<Edge> edges = new ArrayList<>(graph.edges);
        Collections.sort(edges);

        UnionFind uf = new UnionFind(graph.vertices);

        for (Edge edge : edges) {
            if (uf.union(edge.src, edge.dest)) {
                mst.add(edge);
            }
        }

        return mst;
    }

    public static void printMST(List<Edge> mst) {
        int totalWeight = 0;
        System.out.println("MST Edges:");
        for (Edge edge : mst) {
            System.out.println(edge);
            totalWeight += edge.weight;
        }
        System.out.println("Total MST Weight: " + totalWeight);
        System.out.println();
    }

    public static Map<Integer, List<Integer>> findComponents(List<Edge> mst, int vertices, Edge removedEdge) {
        List<Edge> remainingEdges = mst.stream()
                .filter(edge -> !edge.equals(removedEdge))
                .collect(Collectors.toList());

        UnionFind uf = new UnionFind(vertices);
        for (Edge edge : remainingEdges) {
            uf.union(edge.src, edge.dest);
        }

        Map<Integer, List<Integer>> components = new HashMap<>();
        for (int i = 0; i < vertices; i++) {
            int root = uf.find(i);
            components.computeIfAbsent(root, k -> new ArrayList<>()).add(i);
        }

        return components;
    }

    public static Edge findReplacementEdge(Graph graph, List<Edge> mst, Edge removedEdge,
                                           Map<Integer, List<Integer>> components) {
        // Get the roots of the two components
        List<Integer> roots = new ArrayList<>(components.keySet());
        if (roots.size() != 2) {
            throw new IllegalStateException("Removing an edge should create exactly 2 components");
        }

        int root1 = roots.get(0);
        int root2 = roots.get(1);
        List<Integer> component1 = components.get(root1);
        List<Integer> component2 = components.get(root2);

        // Find all edges that connect the two components
        List<Edge> candidateEdges = new ArrayList<>();

        for (Edge edge : graph.edges) {
            // Skip edges that are already in MST (except the removed one)
            if (mst.contains(edge) && !edge.equals(removedEdge)) {
                continue;
            }

            boolean inComponent1 = component1.contains(edge.src) || component1.contains(edge.dest);
            boolean inComponent2 = component2.contains(edge.src) || component2.contains(edge.dest);

            if (inComponent1 && inComponent2) {
                candidateEdges.add(edge);
            }
        }

        if (candidateEdges.isEmpty()) {
            return null; // No replacement edge found
        }

        // Return the minimum weight edge
        return Collections.min(candidateEdges, Comparator.comparingInt(e -> e.weight));
    }

    public static void main(String[] args) {
        // Create a sample graph
        Graph graph = new Graph(6);

        // Add edges (src, dest, weight)
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 4);
        graph.addEdge(1, 2, 2);
        graph.addEdge(1, 0, 4);
        graph.addEdge(2, 0, 4);
        graph.addEdge(2, 1, 2);
        graph.addEdge(2, 3, 3);
        graph.addEdge(2, 5, 2);
        graph.addEdge(2, 4, 4);
        graph.addEdge(3, 2, 3);
        graph.addEdge(3, 4, 3);
        graph.addEdge(4, 2, 4);
        graph.addEdge(4, 3, 3);
        graph.addEdge(5, 2, 2);
        graph.addEdge(5, 4, 3);

        System.out.println("=== MST Edge Removal Demonstration ===\n");

        // Step 1: Build MST
        List<Edge> mst = kruskalMST(graph);
        System.out.println("1. Original MST:");
        printMST(mst);

        // Step 2: Remove an edge (choose one from MST)
        Edge edgeToRemove = mst.get(2); // Remove the third edge
        System.out.println("2. Removing edge: " + edgeToRemove);

        // Step 3: Show components after removal
        Map<Integer, List<Integer>> components = findComponents(mst, graph.vertices, edgeToRemove);
        System.out.println("3. Components after removal:");
        int componentNum = 1;
        for (List<Integer> component : components.values()) {
            System.out.println("   Component " + componentNum + ": " + component);
            componentNum++;
        }

        // Step 4: Find replacement edge
        System.out.println("\n4. Finding replacement edge...");
        Edge replacementEdge = findReplacementEdge(graph, mst, edgeToRemove, components);

        if (replacementEdge != null) {
            System.out.println("   Replacement edge found: " + replacementEdge);

            // Create new MST
            List<Edge> newMST = new ArrayList<>(mst);
            newMST.remove(edgeToRemove);
            newMST.add(replacementEdge);

            System.out.println("\n5. New MST after replacement:");
            printMST(newMST);
        } else {
            System.out.println("   No replacement edge found! Graph remains disconnected.");
        }

        // Additional demonstration: Show all possible edge removals
        System.out.println("\n=== Testing All MST Edges ===");
        for (int i = 0; i < mst.size(); i++) {
            Edge currentEdge = mst.get(i);
            System.out.println("\nRemoving edge " + currentEdge + ":");

            Map<Integer, List<Integer>> comps = findComponents(mst, graph.vertices, currentEdge);
            Edge replEdge = findReplacementEdge(graph, mst, currentEdge, comps);

            if (replEdge != null) {
                System.out.println("  Replacement: " + replEdge);
                List<Edge> testMST = new ArrayList<>(mst);
                testMST.remove(currentEdge);
                testMST.add(replEdge);
                int newWeight = testMST.stream().mapToInt(e -> e.weight).sum();
                System.out.println("  New MST weight: " + newWeight);
            } else {
                System.out.println("  No replacement available");
            }
        }
    }
}