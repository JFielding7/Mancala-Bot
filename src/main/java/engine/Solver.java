package engine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static engine.Engine.*;

public class Solver {
    static int totalStones = 48;
    static int threshold = totalStones >>> 1;
    static long positionsEvaluated;
    static int endgame = 24;
    static int midGame = totalStones - endgame - 1;
    static byte[] optimalScores = loadEndgameCache(endgame);
    static int SIZE = 30_000_001;
    static long[] heuristicCache = new long[SIZE];
    static int[] heuristicValues = new int[SIZE];
    static long[] lower = new long[SIZE], upper = new long[SIZE];
    static int[] lowerValues = new int[SIZE], upperValues = new int[SIZE];
    static int[] l = new int[SIZE], u = new int[SIZE];
    static HashMap<Long, Integer> cutoffMoves = new HashMap<>();
//    static int[] cutoffMoves = new int[SIZE];
    static int[] frequency = new int[7];
//    static long[] lower = new long[SIZE], upper = new long[SIZE];
//    static int[] lowerVals = new int[SIZE], upperVals = new int[SIZE];
    static HashSet<Long> seen = new HashSet<>();
    static HashSet<State> positions = new HashSet<>();

    public static void main(String[] args) {
        int p2 = encode(new StringBuilder("4 4 4 4 4 4").reverse().toString());
        int p1 = encode("4 4 4 4 4 4");
        int score1 = 0;
        int score2 = 0;
        Engine.calculateHeuristics(p1, p2, 6);
        for (State s : new HashSet<>(positions)) {
            heuristics = new HashMap<>();
            Engine.calculateHeuristics(s.piles1, s.piles2, 6);
            System.out.println(decode(s.piles2, true));
            System.out.println(decode(s.piles1, false));
            System.out.println(s.score1);
            System.out.println(s.score2);
            System.out.println("Eval: " + evaluatePosition(s.piles1, s.piles2, s.score1, s.score2, -totalStones, totalStones, 6));
        }
        System.out.println(heuristics.size());
//        System.out.println(evaluatePosition(p1, p2, score1, score2, 0, 1, 6));
        System.out.println(positionsEvaluated);
        positionsEvaluated = 0;
        frequency = new int[7];
        seen = new HashSet<>();
        long start = System.currentTimeMillis();
//        System.out.println("Eval: " + mtd(p1, p2, score1, score2));
        System.out.println("Eval: " + evaluatePosition(p1, p2, score1, score2, 4, 6, 6));
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(positionsEvaluated);
        System.out.println(seen.size());
        System.out.println(Arrays.toString(frequency));
        System.out.println(total / count);
    }

    static double total;
    static int count;

    static int evaluatePosition(int piles1, int piles2, int score1, int score2, int alpha, int beta, int depth) {
        if(score1 + score2 > midGame) {
            total += 6 - depth;
            count++;
            score1 += optimalScores[Database.positionIndex(((long) piles2 << 30) + (long) piles1, totalStones - score1 - score2)];
            score2 = totalStones - score1;
        }
        else if(piles1 == 0) score2 = totalStones - score1;
        else if(piles2 == 0) score1 = totalStones - score2;
        if(score1 + score2 == totalStones) return score1 - score2;
        positionsEvaluated++;
//        if (depth == -4) return score1 - score2;
        long code = ((long) piles2 << 30) + (long) piles1;
        int moveOrder = (1 << 3) + (2 << 6) + (3 << 9) + (4 << 12) + (5 << 15);
        if (depth > 0) moveOrder = Engine.heuristics.get(code);
        int m = -1;
        for(int x = 0; x < 6; x++) {
            int i = moveOrder >> x * 3 & 0b111;
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
                if (dest == 12) {
                    if (m == 0) eval = evaluatePosition(nextPiles1, nextPiles2, nextScore, score2, alpha, beta, depth);
                    else {
                        eval = evaluatePosition(nextPiles1, nextPiles2, nextScore, score2, alpha, alpha + 2, depth);
                        if (eval > alpha && eval < beta) eval = evaluatePosition(nextPiles1, nextPiles2, nextScore, score2, alpha, beta, depth);
                    }
                }
                else {
                    if (m == 0) eval = -evaluatePosition(nextPiles2, nextPiles1, score2, nextScore, -beta, -alpha, depth - 1);
                    else {
                        eval = -evaluatePosition(nextPiles2, nextPiles1, score2, nextScore, -alpha - 2, -alpha, depth - 1);
                        if (eval > alpha && eval < beta) eval = -evaluatePosition(nextPiles2, nextPiles1, score2, nextScore, -beta, -alpha, depth - 1);
                    }
                }
                if (depth == 6) {
                    System.out.println(m);
                    System.out.println(decode(nextPiles2, true));
                    System.out.println(decode(nextPiles1, false));
                    System.out.println(nextScore + " - " + score2);
                    System.out.println(eval);
                    System.out.println(alpha + " " + beta);
                }
                if(eval > alpha) alpha = eval;
                if (alpha >= beta) {
                    if (depth < -10) frequency[m]++;
                    return alpha;
                }
            }
        }
        return alpha;
    }

    static int mtd(int piles1, int piles2, int score1, int score2) {
        int guess = 0;
        int lower = -totalStones, upper = totalStones;
        while (lower < upper) {
            guess = Math.max(guess, lower + 2);
            System.out.println(guess);
            int eval = evaluatePosition(piles1, piles2, score1, score2, guess - 2, guess, 6);
            if (eval == guess) lower = guess;
            else {
                upper = guess - 2;
                guess = upper;
            }
        }
        return lower;
    }

    static int[] moveOrder(int piles1, int piles2) {
        int[] order = {0, 1, 2, 3, 4, 5};
        int[] heuristicValues = new int[6];
        State curr = new State(piles1, piles2);
        for (int i = 0; i < 6; i++) {
            long code = curr.nextMove(i).code();
            int index = (int) (code % SIZE);
            if (heuristicCache[index] == code) heuristicValues[i] = Solver.heuristicValues[index];
        }
        for (int i = 1; i < 6; i++) {
            for (int j = i; j > 0 && heuristicValues[j] > heuristicValues[j - 1]; j--) {
                int temp = order[j];
                order[j] = order[j - 1];
                order[j - 1] = temp;
            }
        }
        return order;
    }

}
