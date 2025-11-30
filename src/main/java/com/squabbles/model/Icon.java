package com.squabbles.model;

import java.util.ArrayList;
import java.util.List;

public class Icon {
    private final int id;
    private final String type; // "POINT", "SLOPE", "VERTICAL"
    // Optional: coordinates or slope value for debugging
    private final String description;

    public Icon(int id, String type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Icon{" + "id=" + id + ", type='" + type + '\'' + '}';
    }
}
