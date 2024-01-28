package engine;

import java.util.ArrayList;

public final class State {
    int piles1, piles2, score1, score2, pile;
    boolean comTurn;

    public State(int piles1, int piles2, int score1, int score2, boolean comTurn, int pile){
        this.piles1 = piles1;
        this.piles2 = piles2;
        this.score1 = score1;
        this.score2 = score2;
        this.comTurn = comTurn;
        this.pile = pile;
    }

    public State(int piles1, int piles2, int score1, int score2) {
        this.piles1 = piles1;
        this.piles2 = piles2;
        this.score1 = score1;
        this.score2 = score2;
    }

    public State(int piles1, int piles2) {
        this.piles1 = piles1;
        this.piles2 = piles2;
    }

    public ArrayList<State> nextStates() {
        ArrayList<State> moves = new ArrayList<>();
        for(int i = 0; i < 6; i++) {
            if(((piles1 >> i * 5) & 31) != 0) moves.add(nextMove(i));
        }
        return moves;
    }

    public State nextMove(int pile) {
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
        return dest == 12 ? new State(nextPiles1, nextPiles2, nextScore, score2, comTurn, pile) : new State(nextPiles2, nextPiles1, score2, nextScore, !comTurn, pile);
    }

    public long code() {
        return ((long) piles2 << 30) + (long) piles1;
    }

    @Override
    public String toString() {
        return Engine.decode(piles2, true) + "\n" + Engine.decode(piles1, false) + "\n" + score1 + "-" + score2;
    }
}
