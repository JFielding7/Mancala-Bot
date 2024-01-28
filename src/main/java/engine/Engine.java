package engine;

import java.io.*;
import java.util.*;

public class Engine {

    static void loadBinaryArr(int endgame) {
        positions = new long[Database.combosSum(endgame, 12).intValue()];
        scores = new byte[positions.length];
        int curr = 0;
        try(FileInputStream in = new FileInputStream("C:\\Users\\josep\\IdeaProjects\\Mancala-Bot\\Positions16.bin")){
            byte[] bytes = new byte[9 * Database.combosSum(endgame, 12).intValue()];
            in.read(bytes, 0, bytes.length);
            for(int i = 0; i < bytes.length; i+=9) {
                long pos = 0;
                for(int j = i; j < i + 8; j++) {
                    pos += (long) ((bytes[j] + 256) & 255) << j % 9 * 8;
                }
                positions[curr] = pos;
                scores[curr++] = bytes[i + 8];
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    static byte[] loadEndgameCache(int size) {
        byte[] optimalScores = new byte[Database.combosSum(size, 12).intValue()];
        try(FileInputStream in = new FileInputStream("C:\\Users\\josep\\IdeaProjects\\Mancala-Bot\\Pos" + size + ".bin")){
            in.read(optimalScores);
        }
        catch (IOException e) { throw new RuntimeException(e); }
        return optimalScores;
    }

    static int maxScore = 48;
    static int threshold = maxScore >>> 1;
    static long x;
    static int draws;
    static int endgame = 24;
    static int midGame = maxScore - endgame - 1;
    static HashMap<Integer, HashSet<Integer>> seen = new HashMap<>();
    static long[] positions;
    static byte[] scores;


    public static void main(String[] args) {
        // loadBinaryArr(endgame);
        optimalScores = loadEndgameCache(endgame);
        int p2 = encode(new StringBuilder("4 4 4 4 4 4").reverse().toString());
        int p1 = encode("4 4 4 4 4 4");
        int score1 = 0;
        int score2 = 0;
        System.gc();
        calculateHeuristics(p1, p2, 6);
        System.out.println("Done");
        long startTime = System.currentTimeMillis();
        System.out.println("Eval: " + evaluatePosition(p1, p2, score1, score2, -1, 1));
        System.out.println("Time: " + (System.currentTimeMillis() - startTime));
        System.out.println(x);
        Arrays.fill(frequency, 0);
        startTime = System.currentTimeMillis();
        System.out.println("Eval: " + evaluatePosition(p1, p2, score1, score2, -1, 1));
        System.out.println("Time: " + (System.currentTimeMillis() - startTime));
        System.out.println(x);
        System.out.println(p.size());
        System.out.println(Arrays.toString(frequency));
        int total = 0;
        for (long pos : cutoffMoves.keySet()) {
            total += cutoffMoves.get(pos).size();
        }
        System.out.println(total);
        System.out.println(cutoffMoves.size());
    }

    // 881930
    // 450007

    static byte[] optimalScores;

    static int[] frequency = new int[7];
    static HashSet<Long> p = new HashSet<>();
    static HashMap<Long, HashSet<Integer>> cutoffMoves = new HashMap<>();

    static int evaluatePosition(int piles1, int piles2, int score1, int score2, int bestEval1, int bestEval2) {
        x++;
        if(score1 + score2 > midGame) {
            score1 += optimalScores[Database.positionIndex(((long) piles2 << 30) + (long) piles1, maxScore - score1 - score2)];
            score2 = maxScore - score1;
        }
        else if(piles1 == 0) score2 = maxScore - score1;
        else if(piles2 == 0) score1 = maxScore - score2;
//        if(score1 - score2 > 2) return 1;
//        if(score2 - score1 > 5) return -1;
        if(score1 > threshold) return 1;
        if(score2 > threshold) return -1;
        if(score1 == threshold && score2 == threshold) return 0;
        int maxEval = -1;
//        int moveOrder = heuristics.get(((long) piles2 << 30) + (long) piles1);
//        if(moveOrder == null) moveOrder = new int[]{0, 1, 2, 3, 4, 5};
//        long code = ((long) piles2 << 30) + (long) piles1 + ((long) (score1 & 15) << 60);
//        if (cutoffMoves.containsKey(code)) {
//            moveOrder = new int[]{cutoffMoves.get(code).stream().toList().get(0), 0, 1, 2, 3, 4, 5};
//        }
        int m = -1;
        for(int i = 0; i < 6; i++) {
//        for (int m = 0; m < 18; m+=3) {
//            int i = order >> m & 0b111;
            int iShift = i * 5;
            int stones = (piles1 >> iShift) & 0b11111;
            if(stones > 0) {
                m++;
                int nextScore = score1;
                int nextPiles1 = piles1 - (stones << iShift);
                int nextPiles2 = piles2;
                for (int j = 0; j < 6; j++) {
                    int jShift = j * 5;
                    nextPiles1 += ((j - i + 13) % 13 + stones) / 13 << jShift;
                    nextPiles2 += ((j - i + 6) % 13 + stones) / 13 << jShift;
                }
                int dest = (52 + i - stones) % 13, destShift = dest * 5;
                if (stones > i) nextScore += (12 - i + stones) / 13;
                if (dest < 6 && stones < 14 && ((piles1 >> destShift) & 0b11111) == 0) {
                    int shift2 = (5 - dest) * 5;
                    if (((nextPiles2 >> shift2) & 0b11111) != 0) {
                        nextPiles1 &= ~(0b11111 << destShift);
                        nextScore += ((nextPiles2 >> shift2) & 0b11111) + 1;
                        nextPiles2 &= ~(0b11111 << shift2);
                    }
                }
                int eval;
                if (dest == 12) eval = evaluatePosition(nextPiles1, nextPiles2, nextScore, score2, bestEval1, bestEval2);
                else eval = -evaluatePosition(nextPiles2, nextPiles1, score2, nextScore, -bestEval2, -bestEval1);
                if(eval > maxEval) maxEval = eval;
                if(eval > bestEval1) bestEval1 = eval;
                if (bestEval1 >= bestEval2) {
                    long posCode = ((long) piles2 << 30) + (long) piles1 + ((long) (score1 & 15) << 60);
                    cutoffMoves.computeIfAbsent(posCode, k -> new HashSet<>()).add(i);
                    frequency[m]++;
                    return maxEval;
                }
            }
        }
        return maxEval;
    }

    static HashMap<Long, Integer> heuristics = new HashMap<>();
    static HashMap<Long, Integer> heuristicScores = new HashMap<>();

    static void calculateHeuristics(int piles1, int piles2, int depth) {
        calculateMaxScore(piles1, piles2, 0, 0, depth);
    }

    static int calculateMaxScore(int piles1, int piles2, int score1, int score2, int depth) {
        if(depth == 0) return score1 - score2;
        long positionCode = ((long) piles2 << 30) + (long) piles1;
        if(heuristicScores.containsKey(positionCode)) return heuristicScores.get(positionCode);
        // x++;
        int[][] moveHeuristics = {{0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0}, {5, 0}};
        int maxScore = -Engine.maxScore;
        for(int i = 0; i < 6; i++) {
            int iShift = i * 5;
            int stones = (piles1 >> iShift) & 31;
            if (stones > 0) {
                int nextScore = score1;
                int nextPiles1 = piles1 - (stones << iShift);
                int nextPiles2 = piles2;
                for (int j = 0; j < 6; j++) {
                    int jShift = j * 5;
                    nextPiles1 += ((j - i + 13) % 13 + stones) / 13 << jShift;
                    nextPiles2 += ((j - i + 6) % 13 + stones) / 13 << jShift;
                }
                int dest = (52 + i - stones) % 13, destShift = dest * 5;
                if (stones > i) nextScore += (12 - i + stones) / 13;
                if (dest < 6 && stones < 14 && ((piles1 >> destShift) & 31) == 0) {
                    int shift2 = (5 - dest) * 5;
                    if (((nextPiles2 >> shift2) & 31) != 0) {
                        nextPiles1 &= ~(31 << destShift);
                        nextScore += ((nextPiles2 >> shift2) & 31) + 1;
                        nextPiles2 &= ~(31 << shift2);
                    }
                }
                int score;
                if (dest == 12) score = calculateMaxScore(nextPiles1, nextPiles2, nextScore, score2, depth);
                else score = -calculateMaxScore(nextPiles2, nextPiles1, score2, nextScore, depth - 1);
                moveHeuristics[i][1] = score;
                maxScore = Math.max(maxScore, score);
            }
        }
        Arrays.sort(moveHeuristics, (a, b) -> a[1] == b[1] ? a[0] - b[0] : b[1] - a[1]);
        int[] moveOrder = new int[6];
        int i = 0;
        int order = 0;
        for(int[] move : moveHeuristics) order += move[0] << 3 * i++;
        heuristics.put(positionCode, order);
        if (depth == 4) Solver.positions.add(new State(piles1, piles2, score1, score2));
        return maxScore;
    }

    static int[] strongMoves = generateDefaultMoves();
    static int DEFAULT_ORDER = (5 << 15) + (4 << 12) + (3 << 9) + (2 << 6) + (1 << 3);

    static int moveOrder(int piles) {
        int stones = 0;
        for(int i = 0; i < 30; i+=5) stones += piles >> i & 0b11111;
        int index = positionIndex(piles, stones);
        if(index < strongMoves.length) return strongMoves[index];
        return DEFAULT_ORDER;
    }

    static int[] generateDefaultMoves() {
        int[] strongMoves = new int[Database.comboSums[40][6]];
        Arrays.fill(strongMoves, 181896);
        return strongMoves;
    }

    static void updateOrder(int move, int piles) {
        int stones = 0;
        for(int i = 0; i < 30; i+=5) stones += piles >> i & 0b11111;
        int index = positionIndex(piles, stones);
        int order = strongMoves[index];
        int length = (order >> 18 & 0b111) * 3;
        for(int i = length; i < 18; i+=3) {
            int currMove = (order >> i) & 0b111;
            if(currMove == move) {
                order = (order & (1 << length) - 1) + (move << length) +
                        (((order >> length) & (1 << i - length) - 1) << length + 3) +
                        (order >> i + 3 << i + 3) + (1 << 18);
            }
        }
        strongMoves[index] = order;
    }

    static int positionIndex(int piles, int stones) {
        int index = 0;
        for(int i = 0; i < 6 && stones > 0; i++) {
            index += Database.comboSums[stones - 1][6 - i];
            stones -= piles >> i * 5 & 0b11111;
        }
        return index;
    }

    static class Pair {
        HashSet<Integer> wins = new HashSet<>();
        HashSet<Integer> losses = new HashSet<>();
        void addWin(int score) {
            wins.add(score);
        }
        void addLoss(int score) {
            losses.add(score);
        }
    }

    public static void main2(String[] args) {
        //1 0 5 5 0 0   7
        //1 5 3 0 0 0   9
        long startTime = System.currentTimeMillis();
//        System.gc();
//        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//        System.out.println(memory);
        loadBinaryArr(endgame);
        int p2 = encode(new StringBuilder("5 0 0 3 3 3").reverse().toString());
        int p1 = encode("4 4 3 3 3 3");
        int score1 = 0;
        int score2 = 2;
//        System.out.println(evaluatePosition(p1, p2, score1, score2, -1, 1));
//        getGameData(new State(p1, p2, score1, score2, true, -1), 50000);

        System.out.println("Eval: " + evaluatePosition(p1, p2, score1, score2, -1, 1));
        System.out.println("Time: " + (System.currentTimeMillis() - startTime));
//        System.out.println("Database load time: " + (System.currentTimeMillis() - startTime));
        // System.out.println(evaluatePosition(p1, p2, score1, score2, -1, 1));
        System.out.println(seen.size());
        int sum = 0;
        int spotSum = 0;
        for(Integer state : seen.keySet()) {
            sum += seen.get(state).size();
            for(Integer pile : seen.get(state)) {
                spotSum += pile + 1;
            }
        }
        System.out.println(sum);
        System.out.println(spotSum);
        System.out.println(x);
//        System.exit(0);
//        System.gc();
//        System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - memory);
        Scanner scan = new Scanner(System.in);
        System.out.print("Press 1 to Quit, anything else to Play: ");
        while(!scan.nextLine().trim().equals("1")) {
            System.out.print("\nEnter 1 to go first, anything else to go second: ");
            playGame(!scan.nextLine().equals("1"));
            System.out.print("\nPress 1 to Quit, anything else to Play: ");
        }
//        int p2 = encode(new StringBuilder("4 4 4 0 0 0").reverse().toString());
//        int p1 = encode("4 4 0 1 1 1");
//        int score1 = 1;
//        int score2 = 12;
//        System.out.println(getValidPile(new State(p1, p2, score1, score2, false, -1)));
//        startTime = System.currentTimeMillis();
//        System.out.println("Pos Eval: " + evaluatePosition(p1, p2, score1, score2, -1, 1, true));
//        System.out.println("Pos Eval time: " + (System.currentTimeMillis() - startTime));
//        System.out.println(x);
//        System.out.println(seen.size());
//        System.out.println(y);
    }

    static void getGameData(State start, int games) {
        double[] winPercent = new double[6];
        for(State next : start.nextStates()) {
            int target = next.comTurn ? 1 : -1;
            // System.out.println(target * evaluatePosition(next.piles1, next.piles2, next.score1, next.score2, -1, 1));
            System.out.println(next);
            for(int j = 0; j < games; j++) {
                if (simulateGame(next) == 1) winPercent[next.pile] += 1;
            }
        }
        for(int i = 0; i < winPercent.length; i++) {
            winPercent[i] /= games;
        }
        System.out.println(Arrays.toString(winPercent));
    }

    static Random rand = new Random();

    static int simulateGame(State curr){
        int score1 = curr.score1, score2 = curr.score2;
        if(maxScore - curr.score1 - curr.score2 <= endgame) {
            long posCode = ((long) curr.piles1 << 30) + (long) curr.piles2;
            score1 += scores[Arrays.binarySearch(positions, posCode)];
            score2 = maxScore - curr.score1;
        }
        if(curr.piles1 == 0) score2 = maxScore - curr.score1;
        if(curr.piles2 == 0) score1 = maxScore - curr.score2;
        if(score1 > threshold) return curr.comTurn ? 1 : -1;
        if(score2 > threshold) return curr.comTurn ? -1 : 1;
        if(score1 + score2 == maxScore) return 0;
        ArrayList<State> n = curr.nextStates();
        State next = n.get(rand.nextInt(n.size()));
        return simulateGame(next);
    }

    static int encode(String piles){
        int encoded = 0;
        int i = 5;
        for(String pile : piles.split(" ")){
            encoded += Integer.parseInt(pile) << 5 * i;
            i--;
        }
        return encoded;
    }

    static String decode(int piles, boolean reverse){
        String decoded = "";
        for(int i = 0; i < 6; i++){
            decoded += ((piles >> (5 * (reverse ? i : 5 - i))) & 31) + " ";
        }
        return decoded.trim();
    }

    static void playGame(boolean comStart) {
        int piles = encode("3 3 3 3 3 3");
        State state = new State(piles, piles, 0, 0, comStart, -1);
        while(state.piles1 > 0 && state.piles2 > 0) {
            if(state.comTurn) {
                long start = System.currentTimeMillis();
                ArrayList<Integer> draws = new ArrayList<>();
                int optimalLoss = 0;
                boolean forcedWin = false;
                for (State move : state.nextStates()) {
                    int co = move.comTurn ? 1 : -1;
                    int eval = co * evaluatePosition(move.piles1, move.piles2, move.score1, move.score2, -1, 1);
                    System.out.println(move);
                    if (eval == 1) {
                        state = move;
                        System.out.print(move.pile + 1 + " ");
                        forcedWin = true;
                        break;
                    }
                    if (eval == 0) draws.add(move.pile);
                    else if (eval == -1) {
                        if(evaluatePosition(move.piles1, move.piles2, move.score1 + co, move.score2 - co, -1, 1) == 0) {
                            optimalLoss = move.pile;
                        }
                    }
                }
                if(!forcedWin) {
                    if(!draws.isEmpty()) {
                        int movedPile = new Random().nextInt(draws.size());
                        state = state.nextMove(draws.get(movedPile));
                        System.out.print(movedPile + 1 + " ");
                    }
                    else {
                        state = state.nextMove(optimalLoss);
                        System.out.print(optimalLoss + 1 + " ");
                    }
                }
                System.out.println("Time: " + (System.currentTimeMillis() - start));
            }
            else {
                System.out.println();
                System.out.println();
                System.out.println(decode(state.piles2, true));
                System.out.println(decode(state.piles1, false));
                System.out.println(state.score1 + "-" + state.score2);
                state = state.nextMove(getValidPile(state));
            }
        }
        int comPiles = state.piles2, comScore = state.score2, playerScore = state.score1;
        if(state.comTurn) {
            comPiles = state.piles1;
            comScore = state.score1;
            playerScore = state.score2;
        }
        if(comPiles == 0) playerScore = 36 - comScore;
        else comScore = 36 - playerScore;
        System.out.println();
        System.out.println("Final Score: ");
        System.out.println("You: " + playerScore);
        System.out.println("Computer: " + comScore);
    }

    static int getValidPile(State state) {
        Scanner scan = new Scanner(System.in);
        int pile = -1;
        while(true) {
            String input = scan.nextLine().trim();
            try { pile = Integer.parseInt(input) - 1; }
            catch (Exception ignored) {}
            if(pile > -1 && pile < 6 && ((state.piles1 >> pile * 5) & 31) != 0) break;
            else System.out.println("Not a move!");
        }
        return pile;
    }
}
