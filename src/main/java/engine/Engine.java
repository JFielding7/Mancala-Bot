package engine;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class Engine {

    static void loadBinaryArr(int endgame) {
        positions = new long[Database.combosSum(endgame, 12).intValue()];
        scores = new byte[positions.length];
        endScores = new byte[positions.length];
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

    public static void main2(String[] args) {
        int p2 = encode(new StringBuilder("4 4 4 4 4 4").reverse().toString());
        int p1 = encode("4 4 4 4 4 4");
        int score1 = 0;
        int score2 = 0;
        System.gc();
        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        loadBinaryArr(16);
        endGameCache = Database.loadBinaryCache(16);
        long start = System.currentTimeMillis();
        System.out.println(evaluatePosition(p1, p2, score1, score2, -1, 1));
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(x);
        System.out.println(winCache.size());
        System.gc();
        System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - memory) / 1000000000.0);
    }

    public static void main3(String[] args) {
        Random rand = new Random(42);
        int length = 10000000;
        int[] arr = new int[length];
        HashMap<Integer, Integer> map = new HashMap<>();
        for(int i = 0; i < length; i++) {
            int num = rand.nextInt(length);
            arr[i] = num;
            map.put(num, i);
        }
        Arrays.sort(arr);
//        System.out.println(Arrays.toString(Arrays.copyOfRange(arr, 0, 100000)));
        System.out.println("started");
        long start = System.currentTimeMillis();
        Integer index = 0;
        for(int i = 0; i < length; i++)
//            index = interpolationSearch(arr, rand.nextInt(length));
            // System.out.println(index);
        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        for(int i = 0; i < length; i++)
            index = map.get(rand.nextInt(length));
        System.out.println(index);
        System.out.println(System.currentTimeMillis() - start);
    }

    static int maxScore = 48;
    static int threshold = maxScore >>> 1;
    static int x;
    static int endgame = 16;
    static int midGame = maxScore - endgame - 1;
    static HashMap<Long, Integer> endGameCache;
    static HashMap<Long, Integer> winCache = new HashMap<>();

    static HashMap<Integer, HashSet<Integer>> seen = new HashMap<>();
    static long[] positions;
    static byte[] scores;
    static byte[] endScores;

    public static void main(String[] args) {
//        System.gc();
//        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//        byte[] x = new byte[1000000000];
//        System.gc();
//        System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - memory) / 1000000000.0);
        loadBinaryArr(endgame);
        int p2 = encode(new StringBuilder("4 4 4 4 4 4").reverse().toString());
        int p1 = encode("4 4 4 4 4 4");
        int score1 = 0;
        int score2 = 0;
        long startTime = System.currentTimeMillis();
//        System.out.println(new State(p1, p2, score1, score2, true, -1).nextMove(0));
        System.out.println("Eval: " + evaluatePosition(p1, p2, score1, score2, -1, 1, true));
        System.out.println("Time: " + (System.currentTimeMillis() - startTime));
        System.out.println(upsets.size());
        for(long pos : upsets) {
            System.out.println(decode((int) pos, true));
            System.out.println(decode((int) (pos >> 30), false));
        }
//        int matches = 0;
//        for(Integer piles : scoreCache.keySet()) {
//            Pair pair = scoreCache.get(piles);
//            if(!pair.losses.stream().allMatch(loss -> pair.wins.stream().allMatch(win -> win >= loss - 3))) {
////                System.out.println(pair.wins);
////                System.out.println(pair.losses);
//                System.out.println(decode(piles, false));
//            }
//            else matches++;
//        }
//        System.out.println(matches);
//        System.out.println(scoreCache.size());
        System.out.println(x);
    }

    static int getIndex(int piles) {
        int index = 0;
        for(int i = 0; i < 6; i++) {
            int stones = (piles >> (5 * i) & 31);
            if (stones > 19) return -1;
            index += (int) Math.pow(20, i) * stones;
        }
        return index;
    }

    static boolean[][] strongMoves = new boolean[1][6];

    static int[] moveOrder(int piles) {
        int index = getIndex(piles);
        if(index == -1) return new int[] {0, 1, 2, 3, 4, 5};
        boolean[] moves = strongMoves[index];
        int[] order = new int[6];
        int idx = 0;
        for(int i = 0; i < 6; i++) {
            if(moves[i]) order[idx++] = i;
        }
        for(int i = 0; i < 6; i++) {
            if(!moves[i]) order[idx++] = i;
        }
        return order;
    }

//    static HashMap<Integer, HashSet<Integer>> strongMoves = new HashMap<>();
//    static int[] moveOrder(int piles1, int piles2) {
//        int index = 0, lowIdx = 0;
//        int[] moves = new int[6], lowPriority = new int[6];
//        for(int i = 0; i < 6; i++) {
//            if((piles1 >> (i * 5) & 31) == i + 1) moves[index++] = i;
//            else lowPriority[lowIdx++] = i;
//        }
//        lowIdx = 0;
//        while(index < 6) moves[index++] = lowPriority[lowIdx++];
//        return moves;
//    }

//    static int[] moveOrder(int piles) {
//        int[] order = new int[6];
//        HashSet<Integer> moves = strongMoves.getOrDefault(piles, new HashSet<>());
//        int i = 0, j = moves.size();
//        for(int k = 0; k < 6; k++) {
//            if(moves.contains(k)) order[i++] = k;
//            else order[j++] = k;
//        }
//        return order;
//    }

    static HashMap<Integer, Pair> scoreCache = new HashMap<>();
    static HashSet<Long> upsets = new HashSet<>();

    static int evaluatePosition(int piles1, int piles2, int score1, int score2, int bestEval1, int bestEval2, boolean turn){
        if(score1 + score2 > midGame) {
            long posCode = ((long) piles1 << 30) + (long) piles2;
            score1 += scores[Arrays.binarySearch(positions, posCode)];
            score2 = maxScore - score1;
        }
        else if(piles1 == 0) score2 = maxScore - score1;
        else if(piles2 == 0) score1 = maxScore - score2;
        if(score1 > threshold) return 1;
        if(score2 > threshold) return -1;
        if(score1 == threshold && score2 == threshold) return 0;
        x++;
        if(score1 - score2 > 1) return 1;
        if(score2 - score1 > 1) return -1;
        int maxEval = -1;
        int empty = 0;
        for(int i = 0; i < 6; i++){
            int iShift = i * 5;
            int stones = (piles1 >> iShift) & 31;
            if(stones > 0) {
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
                int eval;
                if (dest == 12) eval = evaluatePosition(nextPiles1, nextPiles2, nextScore, score2, bestEval1, bestEval2, turn);
                else eval = -evaluatePosition(nextPiles2, nextPiles1, score2, nextScore, -bestEval2, -bestEval1, !turn);
                if(eval > maxEval) maxEval = eval;
                if(eval > bestEval1) bestEval1 = eval;
                if (bestEval1 >= bestEval2) {
                    // if(bestEval1 == 1) scoreCache.computeIfAbsent(piles1, k -> new Pair()).addWin(score1);
//                    if(bestEval1 == 1) strongMoves.computeIfAbsent(piles1, k -> new HashSet<>()).add(i);
//                    if(bestEval1 == 1) {
//                        int index = getIndex(piles1);
//                        if(index != -1) strongMoves[index][i] = true;
//                    }
                    return maxEval;
                }
            } else empty++;
        }
        // scoreCache.computeIfAbsent(piles1, k -> new Pair()).addLoss(score1);
//        if(score1 - score2 >= 11 && empty == 2) {
//            upsets.add(((long) piles1 << 30) + (long) piles2);
//        }
        return maxEval;
    }

    static int evaluatePosition(int piles1, int piles2, int score1, int score2, int bestEval1, int bestEval2){
        if(score1 + score2 > midGame) {
            long posCode = ((long) piles1 << 30) + (long) piles2;
            score1 += scores[Arrays.binarySearch(positions, posCode)];
            score2 = maxScore - score1;
        }
        else if(piles1 == 0) score2 = maxScore - score1;
        else if(piles2 == 0) score1 = maxScore - score2;
        if(score1 > threshold) return 1;
        if(score2 > threshold) return -1;
        if(score1 == threshold && score2 == threshold) return 0;
        x++;
        int maxEval = -1;
        int empty = 0;
        for(int i = 0; i < 6; i++){
            int iShift = i * 5;
            int stones = (piles1 >> iShift) & 31;
            if(stones > 0) {
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
                int eval;
                if (dest == 12) eval = evaluatePosition(nextPiles1, nextPiles2, nextScore, score2, bestEval1, bestEval2);
                else eval = -evaluatePosition(nextPiles2, nextPiles1, score2, nextScore, -bestEval2, -bestEval1);
                if(eval > maxEval) maxEval = eval;
                if(eval > bestEval1) bestEval1 = eval;
                if (bestEval1 >= bestEval2) {
                    // if(bestEval1 == 1) scoreCache.computeIfAbsent(piles1, k -> new Pair()).addWin(score1);
//                    if(bestEval1 == 1) strongMoves.computeIfAbsent(piles1, k -> new HashSet<>()).add(i);
//                    if(bestEval1 == 1) {
//                        int index = getIndex(piles1);
//                        if(index != -1) strongMoves[index][i] = true;
//                    }
                    return maxEval;
                }
            } else empty++;
        }
        // scoreCache.computeIfAbsent(piles1, k -> new Pair()).addLoss(score1);
//        if(score1 - score2 >= 11 && empty == 2) {
//            upsets.add(((long) piles1 << 30) + (long) piles2);
//        }
        return maxEval;
    }//31811745

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

    public static void main4(String[] args) {
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

    static int interpolationSearch(long[] arr, long target) {
        int start = 0, end = arr.length - 1;
        while(start <= end) {
            long startVal = arr[start], endVal = arr[end];
            System.out.println(start + " " + end);
            if(startVal == target) return start;
            if(startVal > target || endVal < target) return -1;
            int guess = BigInteger.valueOf(start).add(
                    new BigInteger(String.valueOf(end - start))
                            .multiply(BigInteger.valueOf(target - startVal))
                            .divide(BigInteger.valueOf(endVal - startVal))).intValue();
            long guessVal = arr[guess];
            if(guessVal == target) return guess;
            if(guessVal > target) end = guess - 1;
            else start = guess + 1;
        }
        return -1;
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

    static int[] nextMove(int piles1, int piles2, int score1, int pile) {
        int iShift = pile * 5;
        int stones = (piles1 >> iShift) & 31;
        int nextScore = score1;
        int nextPiles1 = piles1 - (stones << iShift);
        int nextPiles2 = piles2;
        for (int j = 0; j < 6; j++) {
            int jShift = j * 5;
            nextPiles1 += ((j - pile + 13) % 13 + stones) / 13 << jShift;
            nextPiles2 += ((j - pile + 6) % 13 + stones) / 13 << jShift;
        }
        int dest = (52 + pile - stones) % 13, destShift = dest * 5;
        if (stones > pile) nextScore += (12 - pile + stones) / 13;
        if (dest < 6 && stones < 14 && ((piles1 >> destShift) & 31) == 0) {
            int shift2 = (5 - dest) * 5;
            if (((nextPiles2 >> shift2) & 31) != 0) {
                nextPiles1 &= ~(31 << destShift);
                nextScore += ((nextPiles2 >> shift2) & 31) + 1;
                nextPiles2 &= ~(31 << shift2);
            }
        }
        return new int[]{nextPiles1, nextPiles2, nextScore, dest == 12 ? 1 : 0, pile + 1};
    }
}
