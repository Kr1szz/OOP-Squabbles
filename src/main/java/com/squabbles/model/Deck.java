package com.squabbles.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck<T extends Card> {
    private List<T> cards;

    public Deck(List<T> cards) {
        this.cards = new ArrayList<>(cards);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public T draw() {
        if (cards.isEmpty())
            return null;
        return cards.remove(0);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public List<T> getCards() {
        return cards;
    }
}
