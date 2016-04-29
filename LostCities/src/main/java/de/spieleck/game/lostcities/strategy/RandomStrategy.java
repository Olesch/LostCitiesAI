package de.spieleck.game.lostcities.strategy;

import java.util.Random;
import java.util.List;

import de.spieleck.game.lostcities.Strategy;
import de.spieleck.game.lostcities.Perspective;
import de.spieleck.game.lostcities.Move;

public class RandomStrategy
    implements Strategy
{ 
    private final static Random rand = new Random(42);

    public Move decide(Perspective p) {
        List<Move> legals = p.legalMoves();
        return legals.get(rand.nextInt(legals.size()));
    }
}
