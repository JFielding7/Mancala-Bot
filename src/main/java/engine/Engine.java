package engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Engine {
    public static void main(String[] args) throws FileNotFoundException {
        //1 0 5 5 0 0   7
        //1 5 3 0 0 0   9
        try(Scanner scan = new Scanner(new File("Positions" + endgame + ".txt"))){
            while (scan.hasNext()){
                cache.put(scan.nextLong(), scan.nextInt());
            }
        }
        System.out.println("done");
        System.out.println(cache.size());
        System.out.println(cache.get(1099511627786L));
        int p2 = encode(new StringBuilder("3 3 3 3 3 3").reverse().toString());
        int p1 = encode("3 3 3 0 4 4");
        long curr = System.currentTimeMillis();
//        System.out.println("Eval: " + evaluatePosition(p1, p2, 5, 10, -1, 1, true));
//        System.out.println(Arrays.toString(evals));
//        System.out.println(getOptimalScore(p1, p2, 14));
        System.out.println(evaluatePosition(p1, p2, 1, 0, -1, 1, true));
        System.out.println(System.currentTimeMillis() - curr);
        System.out.println(x);
//        System.out.println(y);
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

    static String decode(int piles){
        String decoded = "";
        for(int i = 5; i > -1; i--){
            decoded += ((piles >> (5 * i)) & 31) + " ";
        }
        return decoded.trim();
    }

    static int maxScore = 36;
    static int threshold = maxScore >>> 1;
    static int x;
    static int y;
    static int endgame = 15;

    static int evaluatePosition(int piles1, int piles2, int score1, int score2, int bestEval1, int bestEval2, boolean curr){
        x++;
        if(piles1 == 0) score2 = maxScore - score1;
        if(piles2 == 0) score1 = maxScore - score2;
        if(score1 > threshold) return 1;
        if(score2 > threshold) return -1;
        if(score1 + score2 >= maxScore - endgame) {
            score1 += getOptimalScore(piles1, piles2, maxScore - score1 - score2);
            if(score1 > threshold) return 1;
            if(score1 < threshold) return -1;
            return 0;
        }
        int maxEval = -1;
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
                if (dest == 12) eval = evaluatePosition(nextPiles1, nextPiles2, nextScore, score2, bestEval1, bestEval2, false);
                else eval = -evaluatePosition(nextPiles2, nextPiles1, score2, nextScore, -bestEval2, -bestEval1, false);
                if(curr){
                    System.out.println(new StringBuilder(decode(nextPiles2)).reverse());
                    System.out.println(decode(nextPiles1));
                    System.out.println(nextScore + " - " + score2);
                    System.out.println("Eval: " + eval);
                }
                if(eval > maxEval) maxEval = eval;
                if(eval > bestEval1) bestEval1 = eval;
                if (bestEval1 >= bestEval2) return maxEval;
            }
        }
        return maxEval;
    }

    static HashMap<Long, Integer> cache = new HashMap<>();

    static int getOptimalScore(int piles1, int piles2, int totalStones){
        if(piles1 == 0) return 0;
        if(piles2 == 0) return totalStones;
        long key = ((long) piles1 << 30) + (long) piles2;
        if(cache.containsKey(key)) return cache.get(key);
        int optimalScore = 0;
        for(int i = 0; i < 6; i++){
            int iShift = i * 5;
            int stones = (piles1 >> iShift) & 31;
            if(stones > 0) {
                int score = 0;
                int nextPiles1 = piles1 - (stones << iShift);
                int nextPiles2 = piles2;
                for (int j = 0; j < 6; j++) {
                    int jShift = j * 5;
                    nextPiles1 += ((j - i + 13) % 13 + stones) / 13 << jShift;
                    nextPiles2 += ((j - i + 6) % 13 + stones) / 13 << jShift;
                }
                int dest = (52 + i - stones) % 13, destShift = dest * 5;
                if (stones > i) score += (12 - i + stones) / 13;
                if (dest < 6 && stones < 14 && ((piles1 >> destShift) & 31) == 0) {
                    int shift2 = (5 - dest) * 5;
                    if (((nextPiles2 >> shift2) & 31) != 0) {
                        nextPiles1 &= ~(31 << destShift);
                        score += ((nextPiles2 >> shift2) & 31) + 1;
                        nextPiles2 &= ~(31 << shift2);
                    }
                }
                int stonesRemaining = totalStones - score;
                if (dest == 12) score += getOptimalScore(nextPiles1, nextPiles2, stonesRemaining);
                else score += stonesRemaining - getOptimalScore(nextPiles2, nextPiles1, stonesRemaining);
                optimalScore = Math.max(optimalScore, score);
            }
        }
        cache.put(key, optimalScore);
        return optimalScore;
    }

// 10193284
//    static int evaluatePosition(int piles1, int piles2, int score1, int score2, int bestEval1, int bestEval2, boolean curr){
//        if(score1 + score2 > 31) y++;
//        x++;
//        if(piles1 == 0) score2 = maxScore - score1;
//        if(piles2 == 0) score1 = maxScore - score2;
//        if(score1 > threshold) return 1;
//        if(score2 > threshold) return -1;
//        if(score1 + score2 == maxScore) return 0;
////        int freeTurns = 0;
////        for(int i = 0; i < 6; i++){
////            if(((piles1 >> i * 5) & 31) == i + 1) moves[freeTurns++] = i;
////        }
////        for(int i = 0; i < 6; i++){
////            if(((piles1 >> i * 5) & 31) != i + 1) moves[freeTurns++] = i;
////        }
//        int maxEval = -1;
//        for(int i = 0; i < 6; i++){
//            int iShift = i * 5;
//            int stones = (piles1 >> iShift) & 31;
//            if(stones > 0) {
//                int nextScore = score1;
//                int nextPiles1 = piles1 - (stones << iShift);
//                int nextPiles2 = piles2;
//                for (int j = 0; j < 6; j++) {
//                    int jShift = j * 5;
//                    nextPiles1 += ((j - i + 13) % 13 + stones) / 13 << jShift;
//                    nextPiles2 += ((j - i + 6) % 13 + stones) / 13 << jShift;
//                }
//                int dest = (52 + i - stones) % 13, destShift = dest * 5;
//                if (stones > i) nextScore += (12 - i + stones) / 13;
//                if (dest < 6 && stones < 14 && ((piles1 >> destShift) & 31) == 0) {
//                    int shift2 = (5 - dest) * 5;
//                    if (((nextPiles2 >> shift2) & 31) != 0) {
//                        nextPiles1 &= ~(31 << destShift);
//                        nextScore += ((nextPiles2 >> shift2) & 31) + 1;
//                        nextPiles2 &= ~(31 << shift2);
//                    }
//                }
//                int eval;
//                if (dest == 12) eval = evaluatePosition(nextPiles1, nextPiles2, nextScore, score2, bestEval1, bestEval2, false);
//                else eval = -evaluatePosition(nextPiles2, nextPiles1, score2, nextScore, -bestEval2, -bestEval1, false);
////                if(curr){
////                    System.out.println(new StringBuilder(decode(nextPiles2)).reverse());
////                    System.out.println(decode(nextPiles1));
////                    System.out.println(nextScore + " - " + score2);
////                    System.out.println("Eval: " + eval);
////                }
//                if(eval > maxEval) maxEval = eval;
//                if(eval > bestEval1) bestEval1 = eval;
//                if (bestEval1 >= bestEval2) return maxEval;
//            }
//        }
//        return maxEval;
//    }

//    static int evaluatePosition(int piles1, int piles2, int score1, int score2, int bestEval1, int bestEval2, boolean curr){
//        x++;
////        System.out.println(new StringBuilder(decode(piles2)).reverse());
////        System.out.println(decode(piles1));
////        System.out.println(score1 + " - " + score2);
////        System.out.println();
//        if(score1 > 18) return 2 * (score1 - 18);
//        if(score2 > 18) return -2 * (score2 - 18);
//        if(piles1 == 0) return 2 * score1 - 36;
//        if(piles2 == 0) return 36 - 2 * score2;
//        // long hash = ((long) piles1 << 30) + (long) piles2;
//        // if(cache.containsKey(hash)) return cache.get(hash);
//        int complete = 0;
//        int maxEval = minEval;
//        for(int i = 0; i < 6; i++){
//            int iShift = i * 5;
//            int stones = (piles1 >> iShift) & 31;
//            if(stones > 0) {
//                int nextScore = score1;
//                int nextPiles1 = piles1 - (stones << iShift);
//                int nextPiles2 = piles2;
//                for (int j = 0; j < 6; j++) {
//                    int jShift = j * 5;
//                    nextPiles1 += ((j - i + 13) % 13 + stones) / 13 << jShift;
//                    nextPiles2 += ((j - i + 6) % 13 + stones) / 13 << jShift;
//                }
//                int dest = (13 + i - stones + 39) % 13, destShift = dest * 5;
//                if (stones > i) {
//                    nextScore += ((13 - i - 1) + stones) / 13;
//                }
//                if (dest < 6 && stones < 14 && ((piles1 >> destShift) & 31) == 0) {
//                    nextPiles1 &= ~(31 << destShift);
//                    int shift2 = (5 - dest) * 5;
//                    nextScore += ((nextPiles2 >> shift2) & 31) + 1;
//                    nextPiles2 &= ~(31 << shift2);
//                }
//                int eval;
////                if(nextScore + score2 == 36) {
////                    System.out.println(nextPiles1 + " " + nextPiles2);
////                    return nextScore - 18;
////                }
//                if (dest == 12) eval = evaluatePosition(nextPiles1, nextPiles2, nextScore, score2, bestEval1, bestEval2, false);
//                else {
//                    eval = -evaluatePosition(nextPiles2, nextPiles1, score2, nextScore, -bestEval2, -bestEval1, false);
//                }
//                if(curr){
//                    System.out.println(new StringBuilder(decode(nextPiles2)).reverse());
//                    System.out.println(decode(nextPiles1));
//                    System.out.println(nextScore + " " + score2);
//                    System.out.println("Eval: " + eval);
//                    System.out.println(dest);
//                }
//                maxEval = Math.max(maxEval, eval);
//                bestEval1 = Math.max(bestEval1, eval);
//                if (bestEval1 >= bestEval2) {
//                    return maxEval;
//                }
//            }
//        }
//        // cache.put(hash, maxEval);
//        return maxEval;
//    }
}
