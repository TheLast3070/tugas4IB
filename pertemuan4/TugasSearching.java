import java.util.*;

public class TugasSearching {

    static String[] namaSimpul = {"GU", "PB", "R", "GKU", "PR", "K", "M", "A", "AS", "IF", "SC", "LK"};
    static Map<String, Integer> indexSimpul = new HashMap<>();
    static int jumlahSimpul;
    static List<List<int[]>> graf;
    static int[] perkiraan;

    static void bobotsimpul(String a, String b, int bobot) {
        int nodeSekarang = indexSimpul.get(a);
        int nodeTetangga = indexSimpul.get(b);
        graf.get(nodeSekarang).add(new int[]{nodeTetangga, bobot});
        graf.get(nodeTetangga).add(new int[]{nodeSekarang, bobot});
    }

    static void setPerkiraan(String name, int val) {
        perkiraan[indexSimpul.get(name)] = val;
    }

    static void buatGraf() {
        jumlahSimpul = namaSimpul.length;
        for (int i = 0; i < jumlahSimpul; i++) indexSimpul.put(namaSimpul[i], i);
        graf = new ArrayList<>();
        for (int i = 0; i < jumlahSimpul; i++) graf.add(new ArrayList<>());

        bobotsimpul("GU", "PB", 3);
        bobotsimpul("GU", "R", 4);
        bobotsimpul("PB", "GKU", 4);
        bobotsimpul("PB", "K", 6);
        bobotsimpul("GKU", "PR", 2);
        bobotsimpul("GKU", "K", 3);
        bobotsimpul("GKU", "A", 7);
        bobotsimpul("R", "PR", 3);
        bobotsimpul("R", "M", 5);
        bobotsimpul("PR", "A", 6);
        bobotsimpul("PR", "IF", 4);
        bobotsimpul("K", "AS", 5);
        bobotsimpul("A", "M", 3);
        bobotsimpul("A", "LK", 5);
        bobotsimpul("M", "SC", 6);
        bobotsimpul("AS", "IF", 4);
        bobotsimpul("AS", "SC", 6);
        bobotsimpul("IF", "LK", 3);
        bobotsimpul("SC", "LK", 4);

        perkiraan = new int[jumlahSimpul];
        setPerkiraan("GU", 12); setPerkiraan("PB", 10); setPerkiraan("R", 9);  setPerkiraan("GKU", 7);
        setPerkiraan("PR", 6);  setPerkiraan("K", 9);   setPerkiraan("M", 7);  setPerkiraan("A", 4);
        setPerkiraan("AS", 5);  setPerkiraan("IF", 2);  setPerkiraan("SC", 3); setPerkiraan("LK", 0);
    }

    static String indexKeNama(List<Integer> rute) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rute.size(); i++) {
            sb.append(namaSimpul[rute.get(i)]);
            if (i + 1 < rute.size()) sb.append(" -> ");
        }
        return sb.toString();
    }

    static String indexdiKunjungi(List<Integer> urutan) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < urutan.size(); i++) {
            sb.append(namaSimpul[urutan.get(i)]);
            if (i + 1 < urutan.size()) sb.append(", ");
        }
        return sb.toString();
    }

    static int totalBobotRute(List<Integer> rute) {
        int total = 0;
        for (int i = 0; i + 1 < rute.size(); i++) {
            int u = rute.get(i), v = rute.get(i + 1);
            for (int[] edge : graf.get(u)) {
                if (edge[0] == v) { total += edge[1]; break; }
            }
        }
        return total;
    }

    static List<Integer> AkhirkeAwal(int[] sebelum, int tujuan) {
        List<Integer> rute = new ArrayList<>();
        for (int cur = tujuan; cur != -1; cur = sebelum[cur]) rute.add(cur);
        Collections.reverse(rute);
        return rute;
    }

    static void printHeader(String judul) {
        System.out.println("\n" + judul);
    }
    // ------------------------- 1. UCS -------------------------
    static void uniformCostSearch(int start, int goal) {
        printHeader("1. UNIFORM COST SEARCH (UCS)");

        final int INF = Integer.MAX_VALUE;
        int[] dist = new int[jumlahSimpul];
        int[] parent = new int[jumlahSimpul];
        boolean[] visited = new boolean[jumlahSimpul];
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);

        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
        dist[start] = 0;
        pq.add(new int[]{0, start});
        List<Integer> order = new ArrayList<>();

        while (!pq.isEmpty()) {
            int[] top = pq.poll();
            int u = top[1];
            if (visited[u]) continue;
            visited[u] = true;
            order.add(u);
            if (u == goal) break;

            for (int[] edge : graf.get(u)) {
                int v = edge[0], w = edge[1];
                if (!visited[v] && dist[u] != INF && dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    parent[v] = u;
                    pq.add(new int[]{dist[v], v});
                }
            }
        }

        System.out.println("Urutan simpul diekspansi : " + indexdiKunjungi(order));
        if (dist[goal] == INF) {
            System.out.println("Tidak ditemukan rute ke tujuan.");
            return;
        }
        List<Integer> path = AkhirkeAwal(parent, goal);
        System.out.println("Rute ditemukan           : " + indexKeNama(path));
        System.out.println("Total bobot (cost)       : " + dist[goal]);
    }

    // ------------------------- 2. IDS -------------------------
    static final long FOUND_SIGNAL = -1L;

    static long dfsBounded(List<Integer> path, long g, long limit, int goal,
                            List<Integer> order) {
        int node = path.get(path.size() - 1);
        order.add(node);

        if (g > limit) return g; // melebihi limit -> kandidat limit berikutnya
        if (node == goal) return FOUND_SIGNAL;

        long minExceed = Long.MAX_VALUE;
        for (int[] edge : graf.get(node)) {
            int v = edge[0], w = edge[1];
            if (path.contains(v)) continue; // hindari siklus

            path.add(v);
            long t = dfsBounded(path, g + w, limit, goal, order);
            if (t == FOUND_SIGNAL) return FOUND_SIGNAL;
            if (t < minExceed) minExceed = t;
            path.remove(path.size() - 1);
        }
        return minExceed;
    }

    static void iterativeDeepeningSearch(int start, int goal) {
        printHeader("2. ITERATIVE DEEPENING SEARCH (IDS) BERBASIS BOBOT");

        long limit = 0; // batas cost awal, dinaikkan bertahap
        List<Integer> path = new ArrayList<>();
        path.add(start);
        int iterasi = 0;

        while (true) {
            iterasi++;
            List<Integer> order = new ArrayList<>();
            long t = dfsBounded(path, 0, limit, goal, order);

            System.out.println("Iterasi ke-" + iterasi + " (limit cost = " + limit + ")");
            System.out.println("  Urutan simpul dikunjungi : " + indexdiKunjungi(order));

            if (t == FOUND_SIGNAL) {
                System.out.println("\nRute ditemukan pada iterasi ke-" + iterasi);
                System.out.println("Rute ditemukan           : " + indexKeNama(path));
                System.out.println("Total bobot (cost)       : " + totalBobotRute(path));
                return;
            }
            if (t == Long.MAX_VALUE) {
                System.out.println("Tidak ditemukan rute ke tujuan.");
                return;
            }
            limit = t; // naikkan batas ke nilai cost terkecil yang melebihi limit sebelumnya
        }
    }
    // ------------------------- 3. GBFS -------------------------
    static void greedyBestFirstSearch(int start, int goal) {
        printHeader("3. GREEDY BEST-FIRST SEARCH (GBFS)");

        boolean[] visited = new boolean[jumlahSimpul];
        int[] parent = new int[jumlahSimpul];
        Arrays.fill(parent, -1);

        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
        pq.add(new int[]{perkiraan[start], start});
        List<Integer> order = new ArrayList<>();

        while (!pq.isEmpty()) {
            int[] top = pq.poll();
            int u = top[1];
            if (visited[u]) continue;
            visited[u] = true;
            order.add(u);
            if (u == goal) break;

            for (int[] edge : graf.get(u)) {
                int v = edge[0];
                if (!visited[v]) {
                    if (parent[v] == -1 && v != start) parent[v] = u;
                    pq.add(new int[]{perkiraan[v], v});
                }
            }
        }

        System.out.println("Urutan simpul diekspansi : " + indexdiKunjungi(order));
        if (!visited[goal]) {
            System.out.println("Tidak ditemukan rute ke tujuan.");
            return;
        }
        List<Integer> path = AkhirkeAwal(parent, goal);
        System.out.println("Rute ditemukan           : " + indexKeNama(path));
        System.out.println("Total bobot (cost)       : " + totalBobotRute(path)
                + "  (GBFS tidak menjamin cost minimum)");
    }
    // ------------------------- 4. A* SEARCH -------------------------
    static void aStarSearch(int start, int goal) {
        printHeader("4. A* SEARCH");

        final int INF = Integer.MAX_VALUE;
        int[] g = new int[jumlahSimpul];
        int[] parent = new int[jumlahSimpul];
        boolean[] closed = new boolean[jumlahSimpul];
        Arrays.fill(g, INF);
        Arrays.fill(parent, -1);

        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
        g[start] = 0;
        pq.add(new int[]{perkiraan[start], start});
        List<Integer> order = new ArrayList<>();

        while (!pq.isEmpty()) {
            int[] top = pq.poll();
            int u = top[1];
            if (closed[u]) continue;
            closed[u] = true;
            order.add(u);
            if (u == goal) break;

            for (int[] edge : graf.get(u)) {
                int v = edge[0], w = edge[1];
                if (closed[v]) continue;
                int newG = g[u] + w;
                if (newG < g[v]) {
                    g[v] = newG;
                    parent[v] = u;
                    pq.add(new int[]{newG + perkiraan[v], v});
                }
            }
        }

        System.out.println("Urutan simpul diekspansi : " + indexdiKunjungi(order));
        if (g[goal] == INF) {
            System.out.println("Tidak ditemukan rute ke tujuan.");
            return;
        }
        List<Integer> path = AkhirkeAwal(parent, goal);
        System.out.println("Rute ditemukan           : " + indexKeNama(path));
        System.out.println("Total bobot (cost)       : " + g[goal]);
    }

    // ------------------------- MAIN -------------------------
    public static void main(String[] args) {
        buatGraf();
        int start = indexSimpul.get("GU");
        int goal = indexSimpul.get("LK");
        System.out.println("Kasus: Robot kurir kampus mencari rute dari GU (Gerbang Utama) " + "ke LK (Lab Komputasi)");

        uniformCostSearch(start, goal);
        iterativeDeepeningSearch(start, goal);
        greedyBestFirstSearch(start, goal);
        aStarSearch(start, goal);
    }
}