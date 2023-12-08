package engine;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;

public class Database {
    public static void main2(String[] args) throws FileNotFoundException {
        new PrintWriter("Positions16.txt").close();
    }
    public static void main(String[] args) throws FileNotFoundException {
        int depth = 16;

        BigInteger total = BigInteger.ZERO;
        for(int i = 0; i <= depth; i++) total = total.add(combos(i, 12));
        System.out.println(total);

        long start = System.currentTimeMillis();
        for(int i = 0; i <= depth; i++){
            for(long position : allCombos(i, 12)){
                getOptimalScore((int) (position >> 30), (int) (position & (1 << 30) - 1), i);
            }
        }
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(cache.size());

        try (PrintWriter pw = new PrintWriter("Positions" + depth + ".txt")) {
            for(long position : cache.keySet()){
                pw.println(position + " " + cache.get(position));
            }
        }
    }

    static long[] allCombos(int n, int k){
        if(n == 0) return new long[]{0};
        if(k == 1) return new long[]{n};
        long[] combos = new long[combos(n, k).intValue()];
        int j = 0;
        for(int i = 0; i <= n; i++){
            long[] subCombos = allCombos(n - i, k - 1);
            for(long subCombo : subCombos){
                combos[j++] = (subCombo << 5) + i;
            }
        }
        return combos;
    }

    static HashMap<String, BigInteger> combosCache = new HashMap<>();

    static BigInteger combos(int n, int k){
        if(n == 0) return BigInteger.ONE;
        if(k == 0) return BigInteger.ZERO;
        String key = n + " " + k;
        if(combosCache.containsKey(key)) return combosCache.get(key);
        BigInteger total = BigInteger.ZERO;
        for(int i = 0; i <= n; i++)
            total = total.add(combos(i, k - 1));
        combosCache.put(key, total);
        return total;
    }


    static HashMap<Long, Integer> loadCache(int endgame) {
        HashMap<Long, Integer> cache = new HashMap<>();
        try(BufferedReader input = new BufferedReader(new FileReader("Positions" + endgame + ".txt"))){
            String line = input.readLine();
            while (line != null){
                String[] values = line.split(" ");
                cache.put(Long.parseLong(values[0]), Integer.parseInt(values[1]));
                line = input.readLine();
            }
        } catch (IOException e) { throw new RuntimeException(e); }
        return cache;
    }

    static HashMap<Long, Integer> cache = new HashMap<>();

    static int getOptimalScore(int piles1, int piles2, int totalStones){
        long key = ((long) piles1 << 30) + (long) piles2;
        if(piles1 == 0) {
            cache.put(key, 0);
            return 0;
        }
        if(piles2 == 0) {
            cache.put(key, totalStones);
            return totalStones;
        }
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

}
