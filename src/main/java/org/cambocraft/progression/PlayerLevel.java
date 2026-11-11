package org.cambocraft.progression;

/** Courbe provisoire ; aucun revenu d'XP n'est accordé par Minecraft pour le moment. */
public record PlayerLevel(int level, long xp) {
    public static final int MAX_LEVEL = 200;
    public static final PlayerLevel INITIAL = new PlayerLevel(1, 0);

    public PlayerLevel {
        if (level < 1 || level > MAX_LEVEL || xp < 0
                || (level == MAX_LEVEL ? xp != 0 : xp >= requiredXp(level))) {
            throw new IllegalArgumentException("Niveau ou XP invalide");
        }
    }

    public static long requiredXp(int currentLevel) {
        if (currentLevel < 1 || currentLevel >= MAX_LEVEL) throw new IllegalArgumentException("Niveau invalide pour l'XP suivante");
        return (long) Math.ceil(120.0 * Math.pow(currentLevel, 1.8));
    }

    /** Application bornée à 199 montées maximum, même avec Long.MAX_VALUE XP. */
    public PlayerLevel gain(long amount) {
        if (amount < 0) throw new IllegalArgumentException("Gain d'XP négatif");
        int nextLevel = level;
        long currentXp = xp;
        while (nextLevel < MAX_LEVEL) {
            long remaining = requiredXp(nextLevel) - currentXp;
            if (amount < remaining) return new PlayerLevel(nextLevel, currentXp + amount);
            amount -= remaining;
            nextLevel++;
            currentXp = 0;
        }
        return new PlayerLevel(MAX_LEVEL, 0);
    }
}
