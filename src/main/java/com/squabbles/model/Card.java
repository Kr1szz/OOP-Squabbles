package com.squabbles.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Card {
    protected final int id;
    protected final List<Icon> icons;

    public Card(int id, List<Icon> icons) {
        this.id = id;
        this.icons = new ArrayList<>(icons);
    }

    public int getId() {
        return id;
    }

    public List<Icon> getIcons() {
        return icons;
    }

    public void shuffleIcons() {
        Collections.shuffle(icons);
    }

    @Override
    public String toString() {
        return "Card{" + "id=" + id + ", icons=" + icons + '}';
    }
}
