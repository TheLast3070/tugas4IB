import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SolusiTugas6 {
    static final int N = 4;
    static final int B = 2;

    static final int[][] INITIAL = {
        {0, 0, 2, 4},
        {2, 0, 1, 3},
        {3, 0, 0, 2},
        {0, 0, 0, 0}
    };

    static final boolean[][] FIXED = new boolean[N][N];
    static Random rnd = new Random(42);
    static {
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                FIXED[r][c] = INITIAL[r][c] != 0;
            }
        }
    }

    static int[][] copy(int[][] grid) {
        int[][] result = new int[N][N];
        for (int r = 0; r < N; r++) {
            result[r] = grid[r].clone();
        }
        return result;
    }

    static void print(int[][] grid, String title) {
        if (title != null) {
            System.out.println(title);
        }

        if (grid == null) {
            System.out.println("Tidak ditemukan solusi.");
            return;
        }

        for (int r = 0; r < N; r++) {
            StringBuilder line = new StringBuilder("  ");
            for (int c = 0; c < N; c++) {
                line.append(grid[r][c] == 0 ? "." : Integer.toString(grid[r][c]));
                line.append(c == B - 1 ? " | " : " ");
            }
            System.out.println(line.toString().trim());

            if (r == B - 1) {
                System.out.println("  --------");
            }
        }
    }

    // Memeriksa apakah nilai v boleh ditempatkan di grid[r][c].
    static boolean consistent(int[][] grid, int r, int c, int v) {
        for (int k = 0; k < N; k++) {
            if (grid[r][k] == v || grid[k][c] == v) {
                return false;
            }
        }

        int boxRow = (r / B) * B;
        int boxCol = (c / B) * B;

        for (int i = boxRow; i < boxRow + B; i++) {
            for (int j = boxCol; j < boxCol + B; j++) {
                if (grid[i][j] == v) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean isValidSolution(int[][] grid) {
        if (grid == null) {
            return false;
        }

        // Pastikan semua angka awal tetap sama dan tidak ada kotak kosong.
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (grid[r][c] < 1 || grid[r][c] > N) {
                    return false;
                }
                if (INITIAL[r][c] != 0 && grid[r][c] != INITIAL[r][c]) {
                    return false;
                }
            }
        }

        // Memeriksa baris, kolom, dan kotak 2x2.
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                int value = grid[r][c];
                grid[r][c] = 0;
                boolean valid = consistent(grid, r, c, value);
                grid[r][c] = value;
                if (!valid) {
                    return false;
                }
            }
        }
        return true;
    }

    //DFS-BACKTRACKING

    static int assignCount;
    static int backtrackCount;
    static int candidateChecks;
    static int[][] btGrid;
    static boolean[][][] btDomain;

    static int domainSize(int r, int c) {
        int size = 0;
        for (int v = 1; v <= N; v++) {
            if (btDomain[r][c][v]) {
                size++;
            }
        }
        return size;
    }

    static boolean isPeer(int r, int c, int i, int j) {
        boolean sameUnit = i == r || j == c
                || (i / B == r / B && j / B == c / B);
        return sameUnit && !(i == r && j == c);
    }

    static boolean solveBT() {
        // MRV: pilih kotak kosong dengan pilihan nilai paling sedikit.
        int bestRow = -1;
        int bestCol = -1;
        int bestSize = Integer.MAX_VALUE;

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (btGrid[r][c] == 0) {
                    int size = domainSize(r, c);
                    if (size < bestSize) {
                        bestSize = size;
                        bestRow = r;
                        bestCol = c;
                    }
                }
            }
        }

        // Tidak ada kotak kosong lagi: solusi ditemukan.
        if (bestRow == -1) {
            return true;
        }

        // Jika domain kosong, cabang ini tidak mungkin menghasilkan solusi.
        if (bestSize == 0) {
            return false;
        }

        for (int value = 1; value <= N; value++) {
            candidateChecks++;
            if (!btDomain[bestRow][bestCol][value]
                    || !consistent(btGrid, bestRow, bestCol, value)) {
                continue;
            }

            btGrid[bestRow][bestCol] = value;
            assignCount++;

            // Forward checking: hapus value dari domain kotak-kotak tetangga.
            List<int[]> removed = new ArrayList<int[]>();
            boolean possible = true;

            for (int i = 0; i < N && possible; i++) {
                for (int j = 0; j < N; j++) {
                    if (btGrid[i][j] == 0
                            && isPeer(bestRow, bestCol, i, j)
                            && btDomain[i][j][value]) {
                        btDomain[i][j][value] = false;
                        removed.add(new int[] {i, j});

                        if (domainSize(i, j) == 0) {
                            possible = false;
                            break;
                        }
                    }
                }
            }

            if (possible && solveBT()) {
                return true;
            }

            // Backtrack: batalkan pilihan dan pulihkan domain yang diubah.
            btGrid[bestRow][bestCol] = 0;
            backtrackCount++;

            for (int[] cell : removed) {
                btDomain[cell[0]][cell[1]][value] = true;
            }
        }
        return false;
    }

    static int[][] backtracking() {
        btGrid = copy(INITIAL);
        btDomain = new boolean[N][N][N + 1];
        assignCount = 0;
        backtrackCount = 0;
        candidateChecks = 0;

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (btGrid[r][c] == 0) {
                    for (int v = 1; v <= N; v++) {
                        btDomain[r][c][v] = consistent(btGrid, r, c, v);
                    }
                }
            }
        }

        return solveBT() ? copy(btGrid) : null;
    }

    // ================================ 2. HILL CLIMBING

    /*
     * Kandidat lengkap dibuat dengan mengisi setiap baris menggunakan angka
     * yang belum ada di baris itu. Dengan demikian konflik baris = 0.
     * Angka awal yang diberikan dalam puzzle tidak diubah.
     */
    static int[][] randomState() {
        int[][] grid = copy(INITIAL);

        for (int r = 0; r < N; r++) {
            List<Integer> missing = new ArrayList<Integer>();

            for (int value = 1; value <= N; value++) {
                boolean found = false;
                for (int c = 0; c < N; c++) {
                    if (grid[r][c] == value) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    missing.add(value);
                }
            }

            Collections.shuffle(missing, rnd);
            int index = 0;

            for (int c = 0; c < N; c++) {
                if (grid[r][c] == 0) {
                    grid[r][c] = missing.get(index++);
                }
            }
        }
        return grid;
    }

    /*
     * Cost adalah jumlah pasangan angka sama pada kolom dan kotak 2x2.
     * Karena setiap baris sudah berisi permutasi 1..4, konflik baris = 0.
     * Cost 0 berarti seluruh aturan Sudoku terpenuhi.
     */
    static int cost(int[][] grid) {
        int conflicts = 0;

        // Konflik kolom.
        for (int c = 0; c < N; c++) {
            for (int i = 0; i < N; i++) {
                for (int j = i + 1; j < N; j++) {
                    if (grid[i][c] == grid[j][c]) {
                        conflicts++;
                    }
                }
            }
        }

        // Konflik kotak 2x2.
        for (int boxRow = 0; boxRow < N; boxRow += B) {
            for (int boxCol = 0; boxCol < N; boxCol += B) {
                int[] values = new int[N];
                int index = 0;

                for (int r = boxRow; r < boxRow + B; r++) {
                    for (int c = boxCol; c < boxCol + B; c++) {
                        values[index++] = grid[r][c];
                    }
                }

                for (int i = 0; i < N; i++) {
                    for (int j = i + 1; j < N; j++) {
                        if (values[i] == values[j]) {
                            conflicts++;
                        }
                    }
                }
            }
        }
        return conflicts;
    }

    // Neighbor dibentuk dengan menukar dua kotak non-given pada baris yang sama.
    static List<int[]> neighbors() {
        List<int[]> moves = new ArrayList<int[]>();

        for (int r = 0; r < N; r++) {
            List<Integer> free = new ArrayList<Integer>();

            for (int c = 0; c < N; c++) {
                if (!FIXED[r][c]) {
                    free.add(c);
                }
            }

            for (int i = 0; i < free.size(); i++) {
                for (int j = i + 1; j < free.size(); j++) {
                    moves.add(new int[] {r, free.get(i), free.get(j)});
                }
            }
        }
        return moves;
    }

    static int[][] swap(int[][] grid, int[] move) {
        int[][] next = copy(grid);
        int row = move[0];
        int colA = move[1];
        int colB = move[2];

        int temp = next[row][colA];
        next[row][colA] = next[row][colB];
        next[row][colB] = temp;
        return next;
    }

    static class Result {
        int[][] grid;
        int steps;
        int evaluations;
        boolean success;

        Result(int[][] grid, int steps, int evaluations, boolean success) {
            this.grid = grid;
            this.steps = steps;
            this.evaluations = evaluations;
            this.success = success;
        }
    }

    // Hill Climbing murni: pilih neighbor dengan cost terkecil.
    // Berhenti saat solusi ditemukan atau tidak ada perbaikan.
    static Result hillClimbing(int[][] start, boolean verbose) {
        int[][] current = copy(start);
        int currentCost = cost(current);
        List<int[]> moves = neighbors();
        int steps = 0;
        int evaluations = 1;

        while (currentCost > 0) {
            int[][] bestState = null;
            int bestCost = Integer.MAX_VALUE;
            int[] bestMove = null;

            for (int[] move : moves) {
                int[][] candidate = swap(current, move);
                int candidateCost = cost(candidate);
                evaluations++;

                if (candidateCost < bestCost) {
                    bestCost = candidateCost;
                    bestState = candidate;
                    bestMove = move;
                }
            }

            if (bestMove == null || bestCost >= currentCost) {
                if (verbose) {
                    System.out.println("Berhenti: tidak ada neighbor yang memperbaiki cost.");
                }
                return new Result(current, steps, evaluations, false);
            }

            if (verbose) {
                System.out.printf(
                    "  Langkah %d: cost %d -> %d; tukar baris %d kolom %d dengan kolom %d%n",
                    steps + 1, currentCost, bestCost,
                    bestMove[0] + 1, bestMove[1] + 1, bestMove[2] + 1
                );
            }

            current = bestState;
            currentCost = bestCost;
            steps++;
        }

        return new Result(current, steps, evaluations, isValidSolution(current));
    }

    // ================================ 3. PENGUJIAN DAN BENCHMARK

    static void runDemo() {
        System.out.println("=== INITIAL STATE DARI MODUL ===");
        print(INITIAL, null);
        System.out.println();

        int[][] solution = backtracking();
        System.out.println("=== HASIL DFS-BACKTRACKING (MRV + FORWARD CHECKING) ===");
        print(solution, null);
        System.out.println("Valid: " + isValidSolution(solution));
        System.out.println("Penugasan nilai: " + assignCount);
        System.out.println("Backtrack: " + backtrackCount);
        System.out.println("Pemeriksaan kandidat: " + candidateChecks);
        System.out.println("Cost: " + (solution == null ? "-" : cost(solution)));
        System.out.println();

        System.out.println("=== CONTOH SATU RUN HILL CLIMBING ===");
        rnd = new Random(7);
        int[][] start = randomState();
        print(start, "State awal kandidat lengkap:");
        System.out.println("Cost awal: " + cost(start));

        Result result = hillClimbing(start, true);
        print(result.grid, "Hasil Hill Climbing:");
        System.out.println("Berhasil: " + result.success);
        System.out.println("Valid: " + isValidSolution(result.grid));
        System.out.println("Cost akhir: " + cost(result.grid));
        System.out.println("Langkah diterima: " + result.steps);
        System.out.println("Evaluasi state: " + result.evaluations);
        System.out.println();
    }

    static void benchmark() {
        final int RUNS = 1000;
        final int WARMUP = 200;

        // Warm-up untuk mengurangi pengaruh pemanasan JVM terhadap pengukuran.
        for (int i = 0; i < WARMUP; i++) {
            backtracking();
            hillClimbing(randomState(), false);
        }

        // Benchmark DFS-Backtracking: hitung metrik dari semua run.
        int btSuccess = 0;
        long btAssignments = 0;
        long btBacktracks = 0;
        long btChecks = 0;
        long btStart = System.nanoTime();

        for (int i = 0; i < RUNS; i++) {
            int[][] solution = backtracking();
            if (isValidSolution(solution)) {
                btSuccess++;
            }
            btAssignments += assignCount;
            btBacktracks += backtrackCount;
            btChecks += candidateChecks;
        }

        long btElapsed = System.nanoTime() - btStart;
        double btTimeMs = btElapsed / (double) RUNS / 1_000_000.0;

        // Benchmark Hill Climbing: setiap run mendapat kandidat awal acak baru.
        int hcSuccess = 0;
        long hcSteps = 0;
        long hcEvaluations = 0;
        long hcStart = System.nanoTime();

        for (int i = 0; i < RUNS; i++) {
            Result result = hillClimbing(randomState(), false);
            if (result.success && isValidSolution(result.grid)) {
                hcSuccess++;
            }
            hcSteps += result.steps;
            hcEvaluations += result.evaluations;
        }

        long hcElapsed = System.nanoTime() - hcStart;
        double hcTimeMs = hcElapsed / (double) RUNS / 1_000_000.0;

        System.out.println("=== BENCHMARK (" + RUNS + " run per metode) ===");
        System.out.printf("%-26s %10s %16s %16s %18s%n",
                "Metode", "Sukses", "Waktu rata2 (ms)",
                "Langkah rata2", "Evaluasi rata2");

        System.out.printf("%-26s %9.1f%% %16.6f %16.2f %18.2f%n",
                "DFS-Backtracking",
                100.0 * btSuccess / RUNS,
                btTimeMs,
                btAssignments / (double) RUNS,
                btChecks / (double) RUNS);

        System.out.printf("%-26s %9.1f%% %16.6f %16.2f %18.2f%n",
                "Hill Climbing",
                100.0 * hcSuccess / RUNS,
                hcTimeMs,
                hcSteps / (double) RUNS,
                hcEvaluations / (double) RUNS);

        System.out.println();
        System.out.println("Catatan: langkah DFS adalah rata-rata penugasan nilai; evaluasi DFS adalah pemeriksaan kandidat.");
        System.out.println("Langkah Hill Climbing adalah jumlah perpindahan ke state yang lebih baik.");
        System.out.println("Evaluasi Hill Climbing menghitung berapa kandidat state yang dinilai.");
    }

    public static void main(String[] args) {
        runDemo();
        benchmark();
    }
}
