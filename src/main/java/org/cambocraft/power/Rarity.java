package org.cambocraft.power;

/** Ordre croissant utilisé pour les garanties ; ne pas modifier sans migration. */
public enum Rarity {
    COMMUN, RARE, EPIQUE, LEGENDAIRE, MYTHIQUE;

    public boolean atLeast(Rarity other) {
        return ordinal() >= other.ordinal();
    }
}
