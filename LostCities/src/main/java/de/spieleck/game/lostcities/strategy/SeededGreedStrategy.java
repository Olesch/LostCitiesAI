package de.spieleck.game.lostcities.strategy;

import java.util.List;

import de.spieleck.game.lostcities.Strategy;
import de.spieleck.game.lostcities.Perspective;
import de.spieleck.game.lostcities.Move;

/**
 * Trial to force a failed GreedyStrategy to have something to be greedy about.
 */
public class SeededGreedStrategy
    implements Strategy
{
    public Move decide(Perspective p) {
        int sign = p.turnNo() < 4 ? -1 : +1;
        int scoreMax = -Integer.MAX_VALUE;
        Move mMax = null;
        for(Move m : p.legalMoves()) {
            Perspective p2 = p.turn(m);
            int score = sign * p2.myScore();
            if ( score > scoreMax ) {
                scoreMax = score;
                mMax = m;
            }
        }
        return mMax;
    }
}
