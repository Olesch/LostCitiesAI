package de.spieleck.game.lostcities.strategy;


import java.util.List;

import de.spieleck.game.lostcities.Strategy;
import de.spieleck.game.lostcities.Perspective;
import de.spieleck.game.lostcities.Move;

/**
 * Discard low and draw from pile.
 */
public class BaselineStrategy
    implements Strategy
{
    public Move decide(Perspective p) {
        int minLevel = Integer.MAX_VALUE;
        Move mMax = null;
        for(Move m : p.legalMoves()) {
            if (m.draw != null || !m.discard) continue;
            if ( m.card.number < minLevel ) {
                minLevel = m.card.number;
                mMax = m;
            } 
        }
        return mMax;
    }
}
