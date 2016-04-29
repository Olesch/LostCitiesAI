package de.spieleck.game.lostcities;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

public class Card
{
    public final Colour colour;
    public final int number; // Semantinc == 0 is double, > 1 is value
    private final int hash; // to create 3 different zeros
    public final String str;

    private Card(Colour c, int n, int hash) {
        this.colour = c;
        this.number = n;
        this.hash = 0x10001 * hash;
        str = colour.ch + (number == 0 ? 'x' : (number < 10 ? Integer.toString(number) : "Z"));
    }

    public static final Set<Card> CARDS;
    static {
        List<Card> cards = new ArrayList<>();
        for(Colour c : Colour.values()) {
            for(int i = 0; i < 3; i++) cards.add(new Card(c, 0, cards.size()));
            for(int i = 2; i < 11; i++) cards.add(new Card(c, i, cards.size()));
        }
        CARDS = Collections.unmodifiableSet(new LinkedHashSet<Card>(cards));
    }

    public static Set<Card> deck() { return CARDS; }

    public String toString() { return str; }

    @Override
    public int hashCode() { return hash; }
}
