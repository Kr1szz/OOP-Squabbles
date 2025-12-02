package com.squabbles.logic;

import com.squabbles.model.Card;
import com.squabbles.model.Icon;
import com.squabbles.model.StandardCard;

import java.util.ArrayList;
import java.util.List;

public class DeckGenerator {
    private static final int P = 7;

    // Icon index layout for P = 7:
    // Points: 1 .. P*P   (1-49)
    // Slopes: P*P+1 .. P*P+P (50-56)
    // Vertical: P*P+P+1 (57)

    public List<Card> generateDeck() {
        List<Card> deck = new ArrayList<>();
        int cardIdCounter = 1;

        // Family A: Slope-Intercept Lines (y = mx + b)
        // m in [0, P-1]
        for (int m = 0; m < P; m++) {
            for (int b = 0; b < P; b++) {
                List<Icon> cardIcons = new ArrayList<>();

                // 1. Slope Icon
                // Ensure we use the same slope IDs as in the special card (Family C):
                // P*P+1 .. P*P+P (50-56)
                int slopeIconId = P * P + 1 + m;
                cardIcons.add(new Icon(slopeIconId, "SLOPE", "m=" + m));

                // 2. Point Icons
                // y = mx + b (mod P)
                for (int x = 0; x < P; x++) {
                    int y = (m * x + b) % P;
                    int pointIconId = getPointIconId(x, y);
                    cardIcons.add(new Icon(pointIconId, "POINT", "(" + x + "," + y + ")"));
                }

                deck.add(new StandardCard(cardIdCounter++, cardIcons));
            }
        }

        // Family B: Vertical Lines (x = k)
        // k in [0, 4]
        for (int k = 0; k < P; k++) {
            List<Icon> cardIcons = new ArrayList<>();

            // 1. Vertical Icon
            // Index 31
            // Indices:
            // Points: 1 to P*P (1-49)
            // Slopes: P*P+1 to P*P+P (50-56)
            // Vertical: P*P+P+1 (57)

            int verticalIconIdVal = P * P + P + 1;
            cardIcons.add(new Icon(verticalIconIdVal, "VERTICAL", "inf slope"));

            // 2. Point Icons
            // x = k, iterate y
            for (int y = 0; y < P; y++) {
                int pointIconId = getPointIconId(k, y);
                cardIcons.add(new Icon(pointIconId, "POINT", "(" + k + "," + y + ")"));
            }

            deck.add(new StandardCard(cardIdCounter++, cardIcons));
        }

        // Family C: The Single Special Card
        List<Icon> specialCardIcons = new ArrayList<>();
        // P Slope Icons
        for (int m = 0; m < P; m++) {
            specialCardIcons.add(new Icon(P * P + 1 + m, "SLOPE", "m=" + m));
        }
        // 1 Vertical Icon
        specialCardIcons.add(new Icon(P * P + P + 1, "VERTICAL", "inf slope"));

        deck.add(new StandardCard(cardIdCounter++, specialCardIcons));

        return deck;
    }

    private int getPointIconId(int x, int y) {
        // Formula: (P * x) + y + 1
        return (P * x) + y + 1;
    }
}
