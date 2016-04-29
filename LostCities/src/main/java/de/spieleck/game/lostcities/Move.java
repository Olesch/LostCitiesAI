package de.spieleck.game.lostcities;

public class Move
{
    public final Card card;
    public final boolean discard;
    public final Colour draw; // null means pile

    public Move(Card card, boolean discard, Colour draw) {
        this.card = card;
        this.discard = discard;
        this.draw = draw;
    }

    public String toString() {
        return "{"+(discard ? "d=" : "P:")+card+(draw == null ? "_" : draw.ch)+"}";
    }
}
