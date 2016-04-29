package de.spieleck.game.lostcities;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Collection;
import java.util.Comparator;

public class GameState
{
    public final static int CARDS_PER_HAND = 8;

    private final Set<Card> handA, handB, allDiscards, playsA, playsB;
    /** List, cause it is ordered! */
    private final LinkedList<Card> pile, discards;
    private int turn;


    private abstract class BasePerspective implements Perspective {
        private final GameState gs() { return GameState.this; }
        public List<Card> discards() { return Collections.unmodifiableList(discards); }
        public Set<Card> allDiscards() { return Collections.unmodifiableSet(allDiscards); }
        public boolean isEnded() { return isEnded(); }
        public Perspective turn(Move m) {
            GameState s2 = gs().turn(m);
            // The turn is still that of the current state!!
            return s2.perspective(isPlayerA());
        }
        public int turnNo() { return GameState.this.turnNo(); }
        public int myScore() { return isPlayerA() ? gs().getScoreA() : gs().getScoreB(); }
        public int hisScore() { return isPlayerA() ? gs().getScoreB() : gs().getScoreA(); }
        public int pileSize() { return pile.size(); }

    }

    /** Set up a new game. */
    public GameState() {
        turn = 0;
        pile = new LinkedList<>(Card.deck());
        Collections.shuffle(pile); // XXX doys if we want to be sure
        handA = new HashSet<Card>();
        handB = new HashSet<Card>();
        discards = new LinkedList<Card>();
        allDiscards = new HashSet<Card>();
        playsA = new HashSet<Card>();
        playsB = new HashSet<Card>();
        for(int i = 0; i < CARDS_PER_HAND; i++) {
            handA.add(pile.remove());
            handB.add(pile.remove());
        }
    }

    /** Internal construction, essentially for doing moves. */
    private GameState(int turn, LinkedList<Card> pile, Set<Card> handA, Set<Card> handB,
           LinkedList<Card> discards, Set<Card> playsA, Set<Card> playsB, Set<Card> allDiscards) {
        this.turn = turn;
        this.pile = pile;
        this.handA = handA;
        this.handB = handB;
        this.discards = discards;
        this.playsA = playsA;
        this.playsB = playsB;
        this.allDiscards = allDiscards;
    }

    public boolean isEnded() { return pile.size() == 0; }
    public boolean isEnded(int limit) { return pile.size() == 0 || turn >= limit; }
    public int turnNo() { return turn; }

    public GameState turn(Move m) {
        if ( !isAllowed(m) ) return null;
        return uncheckedTurn(m);
    }

    public boolean isAllowed(Move m) {
        if ( isEnded() ) return false;
        Perspective p = perspective();
        if ( !p.myHand().contains(m.card) ) return false;
        if ( !m.discard && !p.isCardPlayable(m.card) ) return false;
        // Fortunately the "null move", drop a card and immediately retake it, is forbidden.
        if ( m.discard && m.card.colour == m.draw ) return false;
        if ( !p.isColourDrawable(m.draw) ) return false;
        return true;
    }

    /** Try to avoid some reallocation as far as easily possible. */
    public GameState uncheckedTurn(Move m) {
        LinkedList<Card> newDiscards;
        Set<Card> newAllDiscards;
        if ( m.discard ) {
            newDiscards = new LinkedList<>(discards);
            newDiscards.add(m.card);
            newAllDiscards = new HashSet<>(allDiscards);
            newAllDiscards.add(m.card);
        } else {
            newDiscards = discards;
            newAllDiscards = allDiscards;
        }
        LinkedList<Card> newPile;
        Card drawnCard;
        if ( m.draw == null ) {
            newPile = new LinkedList<>(pile);
            drawnCard = newPile.remove();
        } else {
            newPile = pile;
            drawnCard = draw(m.draw);
            if ( newDiscards == discards ) newDiscards = new LinkedList<>(discards);
            newDiscards.remove(drawnCard);
        }
        if ( turn % 2 == 0 ) {
            Set<Card> myPlays;
            if ( m.discard ) myPlays = playsA;
            else {
                myPlays = new HashSet<>(playsA);
                myPlays.add(m.card);
            }
            Set<Card> myHand = new HashSet<>(handA);
            myHand.remove(m.card);
            myHand.add(drawnCard);
            return new GameState(turn+1, newPile, myHand, handB, newDiscards, myPlays, playsB, newAllDiscards);
        } else {
            Set<Card> myPlays;
            if ( m.discard ) myPlays = playsB;
            else {
                myPlays = new HashSet<>(playsB);
                myPlays.add(m.card);
            }
            Set<Card> myHand = new HashSet<>(handB);
            myHand.remove(m.card);
            myHand.add(drawnCard);
            return new GameState(turn+1, newPile, handA, myHand, newDiscards, playsA, myPlays, newAllDiscards);
        }
    }

    private Card draw(Colour c) {
        Card res = null;
        for(Card c2 : discards) {
            if ( c2.colour == c ) res = c2; // keep and draw the last one!
        }
        return res;
    }

    public Perspective perspective() {
        return perspective(turn % 2 == 0);
    }

    public Perspective perspective(boolean playerA) {
        if ( playerA ) {
            return new BasePerspective() {
                public boolean isPlayerA() { return true; }
                public Set<Card> myHand() { return Collections.unmodifiableSet(handA); }
                public Set<Card> myPlays() { return Collections.unmodifiableSet(playsA); }
                public Set<Card> hisPlays() { return Collections.unmodifiableSet(playsB); }
            };
        } else {
            return new BasePerspective() {
                public boolean isPlayerA() { return false; }
                public Set<Card> myHand() { return Collections.unmodifiableSet(handB); }
                public Set<Card> myPlays() { return Collections.unmodifiableSet(playsB); }
                public Set<Card> hisPlays() { return Collections.unmodifiableSet(playsA); }
            };
        }
    }

    public int getScoreA() { return score(playsA); }
    public int getScoreB() { return score(playsB); }

    public String toString() {
        return niceString(playsA, discards, playsB);
    }

    public static boolean isCardDominant(Card c, Collection<Card> cards) {
       for(Card c2 : cards) {
            if ( c2.colour == c.colour && c2.number > c.number ) return false;
        }
        return true;
    }

    public static boolean containsColour(Colour c, Collection<Card> cards) {
        if ( c == null ) return true;
        for(Card c2 : cards) if ( c2.colour == c ) return true;
        return false;
    }

    public static int score(Collection<Card> played) {
        int[] mul = new int[Colour.values().length];
        int[] sum = new int[Colour.values().length];
        int[] cnt = new int[Colour.values().length];
        for(Card c : played) {
            int colNo = c.colour.ordinal();
            if ( c.number == 0 ) mul[colNo]++; else sum[colNo] += c.number;
            cnt[colNo]++;
        }
        int res = 0; 
        for(int i = 0; i < mul.length; i++) if ( cnt[i] > 0 ) {
            res += (1+mul[i])*(sum[i] - 20);
            if ( cnt[i] >= 8 ) res += 20;
        }
        return res;
    }

    public static String niceString(Collection<Card>... cardColls) {
        return niceString(true, cardColls);
    }
    public static String niceString(boolean sort, Collection<Card>... cardColls) {
        StringBuilder res = new StringBuilder();
        boolean first = true;
        for(Collection<Card> coll : cardColls) {
            if ( sort ) {
                List<Card> coll2 = new ArrayList<Card>();
                for(Card c : coll) coll2.add(c);
                Collections.sort(coll2, new Comparator<Card>() {
                    public int compare(Card c1, Card c2) {
                        int h = c1.colour.ordinal() - c2.colour.ordinal();
                        if ( h == 0 ) h = c1.number - c2.number;
                        return h;
                    }
                });
                coll = coll2;
            }
            if ( first ) {
                res.append('[');
                first = false;
            } else { 
                res.append(" - [");
            }
            Card lc = null;
            for(Card c : coll) {
                if ( lc != null ) res.append(sort && lc.colour == c.colour ? "-" : " ");
                res.append(c);
                lc = c;
            }
            res.append(']');
        }
        return res.toString();
    }

}
