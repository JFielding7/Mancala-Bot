package engine;

public final class State {
    int piles1, piles2, score1, score2;

    public State(int piles1, int piles2, int score1, int score2){
        this.piles1 = piles1;
        this.piles2 = piles2;
        this.score1 = score1;
        this.score2 = score2;
    }
}
