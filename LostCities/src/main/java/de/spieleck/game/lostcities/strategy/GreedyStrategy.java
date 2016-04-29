package de.spieleck.game.lostcities.strategy;

import java.util.List;

import de.spieleck.game.lostcities.Strategy;
import de.spieleck.game.lostcities.Perspective;
import de.spieleck.game.lostcities.Move;

/**
 * Was meant to be smarter, ends up playing discards.
 */
public class GreedyStrategy
    implements Strategy
{
    public Move decide(Perspective p) {
        int scoreMax = -Integer.MAX_VALUE;
        Move mMax = null;
        for(Move m : p.legalMoves()) {
            Perspective p2 = p.turn(m);
            int score = p2.myScore();
            if ( score > scoreMax ) {
                scoreMax = score;
                mMax = m;
            }
        }
        return mMax;
    }
}
