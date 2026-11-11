package org.cambocraft.power;

/** Compteurs persistables d'échecs consécutifs pour chaque seuil de rareté. */
public record PityState(int epic, int legendary, int mythic) {
    public static final int EPIC_LIMIT = 30;
    public static final int LEGENDARY_LIMIT = 80;
    public static final int MYTHIC_LIMIT = 250;
    public static final PityState INITIAL = new PityState(0, 0, 0);

    public PityState {
        if (epic < 0 || epic > EPIC_LIMIT || legendary < 0 || legendary > LEGENDARY_LIMIT
                || mythic < 0 || mythic > MYTHIC_LIMIT || epic > legendary || legendary > mythic) {
            throw new IllegalArgumentException("Compteurs de garantie invalides");
        }
    }

    /** Calcule l'état suivant sans modifier l'état existant. */
    PityState after(Rarity result) {
        return new PityState(result.atLeast(Rarity.EPIQUE) ? 0 : epic + 1,
                result.atLeast(Rarity.LEGENDAIRE) ? 0 : legendary + 1,
                result == Rarity.MYTHIQUE ? 0 : mythic + 1);
    }
}
