package de.spieleck.game.lostcities;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * The part one can player from a complete GameState.
 */
public interface Perspective {
    Set<Card> myHand();
    List<Card> discards();
    Set<Card> myPlays();
    Set<Card> hisPlays();
    Set<Card> allDiscards();
    boolean isEnded();
    boolean isPlayerA();
    int turnNo();
    int pileSize();
    Perspective turn(Move m);

    default int myScore() { return GameState.score(myPlays()); }
    default int hisScore() { return GameState.score(hisPlays()); }

    /** Check if a certain card can be played. */
    default boolean isCardPlayable(Card c) {
        return GameState.isCardDominant(c, myPlays());
    }

    /** Check if a certain colour can be drawn. */
    default boolean isColourDrawable(Colour c) {
        return GameState.containsColour(c, discards());
    }

    default String niceString() {
        HashSet<Card> hisHand = new HashSet<>(allDiscards());
        hisHand.removeAll(discards());
        hisHand.removeAll(hisPlays());
        hisHand.removeAll(myPlays());
        hisHand.removeAll(myHand());
        // return GameState.niceString(hisHand, hisPlays(), discards(), myPlays(), myHand());
        return GameState.niceString(false, hisHand)
              +" - "+hisScore()+"="+GameState.niceString(hisPlays())
              +" |"+GameState.niceString(discards())
              +"| "+myScore()+"="+GameState.niceString(myPlays())
              +" - "+GameState.niceString(false, myHand());
    }

    default List<Move> legalMoves() {
        List<Move> res = new ArrayList<>();
        for(Card card : myHand()) {
            res.add(new Move(card, true, null));
            for(Colour col : Colour.values()) {
                if ( col != card.colour && isColourDrawable(col) ) res.add(new Move(card, true, col));
            }
            if ( isCardPlayable(card) ) {
                res.add(new Move(card, false, null));
                for(Colour col : Colour.values()) {
                    if ( isColourDrawable(col) ) res.add(new Move(card, false, col));
                }
            }
        }
        return res;
    }
}
