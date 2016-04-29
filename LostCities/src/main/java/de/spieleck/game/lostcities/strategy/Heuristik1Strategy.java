package de.spieleck.game.lostcities.strategy;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Arrays;

import de.spieleck.game.lostcities.Strategy;
import de.spieleck.game.lostcities.Perspective;
import de.spieleck.game.lostcities.Move;
import de.spieleck.game.lostcities.Card;
import de.spieleck.game.lostcities.Colour;

public class Heuristik1Strategy
    implements Strategy
{
    public Move decide(Perspective p) {
        int scoreMax = -Integer.MAX_VALUE;
        Move mMax = null;
        for(Move m : p.legalMoves()) {
            Perspective p2 = p.turn(m);
            int score = score(p2);
            if ( score > scoreMax ) {
                scoreMax = score;
                mMax = m;
            }
        }
        return mMax;
    }

    private int score(Perspective p2) {
        Set<Card> hand = p2.myHand();
        Set<Card> played = p2.myPlays();
        Set<Card> playable = new HashSet<Card>();
        //
        int[] playableScore = new int[Colour.values().length];
        for(Card c : hand) if ( p2.isCardPlayable(c) ) {
            playable.add(c);
            playableScore[c.colour.ordinal()] += c.number;
        }
        //
        int[] mul = new int[Colour.values().length];
        int[] sum = new int[Colour.values().length];
        for(Card c : played) {
            int colNo = c.colour.ordinal();
            if ( c.number == 0 ) mul[colNo]++; else sum[colNo] += c.number;
        }
        int res = 0; 
        for(int i = 0; i < mul.length; i++) if ( sum[i] + mul[i] > 0 ) res += (1+mul[i])*(sum[i] - 20);
        int score = res * 10;
        //

        int[] layedOut = colourCount(played);
        int scalProd = 0;
        for(int i = 0; i < playableScore.length; i++) scalProd += layedOut[i]*playableScore[i];
        score += scalProd * p2.pileSize() / 7;
        Arrays.sort(layedOut);
        int zeros = 0;
        while (zeros < layedOut.length && layedOut[zeros] == 0) zeros++;
        score -= 4 * zeros*zeros;
        score += 7 * valueSum(hand, p2.pileSize()/9);
        score -= 6 * valueSum(p2.discards(), 6);
        return score;
    }

    public static int valueSum(Collection<Card> cards, int zeroValue) {
        int res = 0;
        for(Card c : cards) {
            if ( c.number == 0 ) res += zeroValue;
            else res += c.number;
        }
        return res;
    }

    public static int[] colourCount(Collection<Card> cards) {
        int[] res = new int[Colour.values().length];
        for(Card c : cards) res[c.colour.ordinal()]++;
        return res;
    }
}
