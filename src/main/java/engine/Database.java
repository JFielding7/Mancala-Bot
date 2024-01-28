package engine;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import static engine.Engine.encode;

public class Database {
    public static void main(String[] args) {
//        System.gc();
//        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//        int[] x = new int[2000000000];
        long start = System.currentTimeMillis();
        System.out.println(combosSum(24, 12).longValue() - 2 * combosSum(24, 12).longValue());
        System.out.println(System.currentTimeMillis() - start);
//        System.gc();
//        System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - memory) / 1_000_000_000.0);
//        System.out.println(Runtime.getRuntime().freeMemory() / 1_000_000_000.0);
//        System.out.println(System.currentTimeMillis() - start);
    }

    static int positionIndex(long position, int stones) {
        int index = 0;
        for(int i = 0; i < 12 && stones > 0; i++) {
            index += comboSums[stones - 1][12 - i];
            stones -= (int) (position >> i * 5 & 0b11111);
        }
        return index;
    }

    static long getPosition(int index, int stones) {
        long position = 0;
        for(int i = 12; i > 0 && index > 0; i--) {
            index -= comboSums[stones - 1][i];
            int j = 0;
            while(comboSums[j][i - 1] <= index) j++;
            position += (long) (stones - j) << (12 - i) * 5;
            stones = j;
        }
        return position;
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
    static int[][] comboSums = generateComboSums(43);

    static int[][] generateComboSums(int size) {
        int[][] comboSums = new int[size][];
        for(int i = 0; i < size; i++) {
            comboSums[i] = new int[13];
            for (int j = 0; j < 13; j++) {
                comboSums[i][j] = combosSum(i, j).intValue();
            }
        }
        return comboSums;
    }

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

    static BigInteger combosSum(int n, int k) {
        BigInteger total = BigInteger.ZERO;
        for(int i = 0; i <= n; i++) total = total.add(combos(i, k));
        return total;
    }

    static HashMap<Long, Integer> loadBinaryCache(int endgame) {
        HashMap<Long, Integer> cache = new HashMap<>();
        long[] arr = new long[combosSum(endgame, 12).intValue()];
        try(FileInputStream in = new FileInputStream("Positions" + endgame + ".bin")){
            byte[] bytes = new byte[9 * combosSum(endgame, 12).intValue()];
            in.read(bytes, 0, bytes.length);
            for(int i = 0; i < bytes.length; i+=9) {
                long pos = 0;
                for(int j = i; j < i + 8; j++) {
                    pos += (long) ((bytes[j] + 256) & 255) << j % 9 * 8;
                }
                arr[i / 9] = pos;
                cache.put(pos, (int) bytes[i + 8]);
            }
        } catch (IOException e) { throw new RuntimeException(e); }
        System.out.println(cache.size());
        return cache;
    }

    static byte[] optimalScores;
    static boolean[] scoredCached;

    static void generateDatabase(int size) {
        int positions = comboSums[size][12];
        optimalScores = new byte[positions];
        scoredCached = new boolean[positions];
        int totalStones = 0;
        int threshold = comboSums[totalStones][12];
        for(int i = 0; i < positions; i++) {
            if (i == threshold) threshold = comboSums[++totalStones][12];
            long position = getPosition(i, totalStones);
            int piles1 = (int) (position & (1 << 30) - 1), piles2 = (int) (position >> 30);
            getOptimalScore(piles1, piles2, totalStones);
        }
        try (FileOutputStream out = new FileOutputStream("Pos" + size + ".bin")) {
            out.write(optimalScores);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static int getOptimalScore(int piles1, int piles2, int totalStones) {
        int index = positionIndex(((long) piles2 << 30) + (long) piles1, totalStones);
        if (piles1 == 0) {
            optimalScores[index] = 0;
            return 0;
        }
        if (piles2 == 0) {
            optimalScores[index] = (byte) totalStones;
            return totalStones;
        }
        if (scoredCached[index]) return optimalScores[index];
        int optimalScore = 0;
        for (int i = 0; i < 6; i++) {
            int iShift = i * 5;
            int stones = (piles1 >> iShift) & 31;
            if (stones > 0) {
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
                int remaining = totalStones - score;
                if (dest == 12) optimalScore = Math.max(optimalScore, score + getOptimalScore(nextPiles1, nextPiles2, remaining));
                else optimalScore = Math.max(optimalScore, totalStones - getOptimalScore(nextPiles2, nextPiles1, remaining));
            }
        }
        scoredCached[index] = true;
        optimalScores[index] = (byte) optimalScore;
        return optimalScore;
    }

    static void sortDatabase() {
        ArrayList<Pos> arr = new ArrayList<>();
        try(BufferedReader input = new BufferedReader(new FileReader("Positions" + 16 + ".txt"));
            PrintWriter pw = new PrintWriter("Pos16.txt")){
            String line = input.readLine();
            while (line != null){
                String[] values = line.split(" ");
                arr.add(new Pos(Long.parseLong(values[0]), Byte.parseByte(values[1])));
                line = input.readLine();
            }
            arr.sort(Comparator.comparingLong((Pos a) -> a.pos));
            for(Pos position : arr){
                pw.println(position.pos + " " + position.score);
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    static HashMap<Long, Integer> loadCache(int endgame) {
        HashMap<Long, Integer> cache = new HashMap<>();
        int match = 0;
        try(BufferedReader input = new BufferedReader(new FileReader("Positions" + endgame + ".txt"))){
            String line = input.readLine();
            while (line != null){
                String[] values = line.split(" ");
                cache.put(Long.parseLong(values[0]), Integer.parseInt(values[1]));
                line = input.readLine();
            }
        } catch (IOException e) { throw new RuntimeException(e); }
        System.out.println(match);
        System.out.println(combosSum(endgame, 12));
        return cache;
    }

    static void convertToBinaryFile(int endgame) {
        byte[] bytes = new byte[9 * combosSum(endgame, 12).intValue()];
        int currByte = 0;
        try (FileOutputStream out = new FileOutputStream("Positions" + endgame + ".bin");
             BufferedReader input = new BufferedReader(new FileReader("Pos" + endgame + ".txt"))) {
            String line = input.readLine();
            while(line != null) {
                String[] values = line.split(" ");
                long pos = Long.parseLong(values[0]);
                byte score = Byte.parseByte(values[1]);
                for(int i = 0; i < 64; i+=8) {
                    bytes[currByte++] = (byte) (pos >> i & 255);
                }
                bytes[currByte++] = score;
                line = input.readLine();
            }
            out.write(bytes, 0, bytes.length);
            out.flush();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class Pos {
        long pos;
        byte score;
        Pos(long pos, byte score) {
            this.pos = pos;
            this.score = score;
        }
    }
}
