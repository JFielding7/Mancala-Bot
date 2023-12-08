package engine;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Engine {
    static int maxScore = 36;
    static int threshold = maxScore >>> 1;
    static int x;
    static int y;
    static int endgame = 16;
    static int midGame = maxScore - endgame - 1;
    static HashMap<Long, Integer> cache;
    static HashSet<Long> seen = new HashSet<>();
    public static void main(String[] args) throws IOException {
        //1 0 5 5 0 0   7
        //1 5 3 0 0 0   9
        long startTime = System.currentTimeMillis();
//        System.gc();
//        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//        System.out.println(memory);
         cache = Database.loadCache(endgame);
        System.out.println("Database load time: " + (System.currentTimeMillis() - startTime));
//        System.gc();
//        System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - memory);
        playGame();
//        int p2 = encode(new StringBuilder("4 4 4 0 0 0").reverse().toString());
//        int p1 = encode("4 4 0 1 1 1");
//        int score1 = 1;
//        int score2 = 12;
//
//        startTime = System.currentTimeMillis();
//        System.out.println("Pos Eval: " + evaluatePosition(p1, p2, score1, score2, -1, 1, true));
//        System.out.println("Pos Eval time: " + (System.currentTimeMillis() - startTime));
//        System.out.println(x);
//        System.out.println(seen.size());
//        System.out.println(y);
    }

    static void simulateGame(){
        int p2 = encode(new StringBuilder("3 3 3 3 3 3").reverse().toString());
        int p1 = encode("3 3 3 3 3 3");
        int score1 = 0;
        int score2 = 0;
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.println("Pos Eval: " + evaluatePosition(p1, p2, score1, score2, -1, 1, true));
            p1 = encode(new StringBuilder(scanner.nextLine()).reverse().toString());
            p2 = encode(scanner.nextLine());
            score2 = Integer.parseInt(scanner.nextLine());
            score1 = Integer.parseInt(scanner.nextLine());
        }
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

    static void playGame() {
        Scanner scan = new Scanner(System.in);
        int comScore = 0, playerScore = 0;
        int comPiles = encode("3 3 3 3 3 3"), playerPiles = comPiles;
        boolean comTurn = true;
        while(comPiles > 0 && playerPiles > 0 && comScore + playerScore < 36) {
            if(comTurn) {
                for (int[] move : nextMoves(comPiles, playerPiles, comScore)) {
                    int nextComPiles = move[0], nextPlayerPiles = move[1], nextComScore = move[2], freeTurn = move[3], pile = move[4];
                    int eval;
                    boolean turn = true;
                    if (freeTurn == 0) {
                        eval = -evaluatePosition(nextPlayerPiles, nextComPiles, playerScore, nextComScore, -1, 1, false);
                        turn = false;
                    }
                    else {
                        eval = evaluatePosition(nextComPiles, nextPlayerPiles, nextComScore, playerScore, -1, 1, false);
                    }
                    if (eval == 1) {
                        comScore = nextComScore;
                        comPiles = nextComPiles;
                        playerPiles = nextPlayerPiles;
                        comTurn = turn;
                        System.out.print(pile + " ");
                        break;
                    }
                }
            }
            else {
                System.out.println();
                System.out.println();
                System.out.println(new StringBuilder(decode(comPiles)).reverse());
                System.out.println(decode(playerPiles));
                System.out.println(playerScore + "-" + comScore);
                int pile = scan.nextInt() - 1;
                while(pile < 0 || pile > 5 || ((playerPiles >> pile * 5) & 31) == 0) {
                    System.out.println("Not a move!");
                    pile = scan.nextInt() - 1;
                }
                int[] move = nextMove(playerPiles, comPiles, playerScore, pile);
                comPiles = move[1];
                playerPiles = move[0];
                playerScore = move[2];
                comTurn = (move[3] == 0);
            }
        }
        if(comPiles == 0) playerScore = 36 - comScore;
        else comScore = 36 - playerScore;
        System.out.println();
        System.out.println("Final Score: ");
        System.out.println("You: " + playerScore);
        System.out.println("Computer: " + comScore);
    }

    static ArrayList<int[]> nextMoves(int piles1, int piles2, int score1) {
        ArrayList<int[]> moves = new ArrayList<>();
        for(int i = 0; i < 6; i++) {
            if(((piles1 >> i * 5) & 31) != 0) moves.add(nextMove(piles1, piles2, score1, i));
        }
        return moves;
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

//46228987
//27204867
    static int evaluatePosition(int piles1, int piles2, int score1, int score2, int bestEval1, int bestEval2, boolean curr){
        if(score1 + score2 < 21) {
            // seen.add(((long) piles1 << 30) + (long) piles2);
            x++;
        }
        else y++;
        if(score1 + score2 > midGame) {
            score1 += cache.get(((long) piles1 << 30) + (long) piles2);
            score2 = maxScore - score1;
        }
        else if(piles1 == 0) score2 = maxScore - score1;
        else if(piles2 == 0) score1 = maxScore - score2;
        if(score1 > threshold) return 1;
        if(score2 > threshold) return -1;
        if(score1 == threshold && score2 == threshold) return 0;
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
                    if(dest == 12){
                        System.out.println(new StringBuilder(decode(nextPiles1)).reverse());
                        System.out.println(decode(nextPiles2));
                        System.out.println(score2);
                        System.out.println(nextScore);
                        System.out.println("Eval: " + eval);
                        System.out.println("Free Turn");
                    }
                    else {
                        System.out.println(new StringBuilder(decode(nextPiles2)).reverse());
                        System.out.println(decode(nextPiles1));
                        System.out.println(nextScore);
                        System.out.println(score2);
                        System.out.println("Eval: " + eval);
                    }
                }
                if(eval > maxEval) maxEval = eval;
                if(eval > bestEval1) bestEval1 = eval;
                if (bestEval1 >= bestEval2) {
                    return maxEval;
                }
            }
        }
        return maxEval;
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
