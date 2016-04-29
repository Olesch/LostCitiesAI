package de.spieleck.game.lostcities.strategy;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import de.spieleck.game.lostcities.Strategy;
import de.spieleck.game.lostcities.Perspective;
import de.spieleck.game.lostcities.Move;

/**
 * Plays random, but avoids discards, whenever possible.
 */
public class Random2Strategy
    implements Strategy
{ 
    private final static Random rand = new Random(42);

    public Move decide(Perspective p) {
        List<Move> legals = p.legalMoves();
        List<Move> cands = new ArrayList<>();
        for(Move m : legals) if ( !m.discard ) cands.add(m);
        if ( cands.size() == 0 ) for(Move m : legals) if ( m.card.number != 0 ) cands.add(m);
        if ( cands.size() == 0 ) cands = legals;
        return cands.get(rand.nextInt(cands.size()));
    }
}
