package org.cambocraft.power;

/** Nombre d'exemplaires débloqués, plafonné à cinq ; maîtrise cumulée non équilibrée. */
public record PowerProgress(int copies, int mastery) {
    public static final PowerProgress FIRST = new PowerProgress(1, 0);

    public PowerProgress {
        if (copies < 1 || copies > 5 || mastery < 0) {
            throw new IllegalArgumentException("Progression de pouvoir invalide");
        }
    }

    public PowerProgress duplicate() { return new PowerProgress(Math.min(5, copies + 1), mastery); }

    /** À appeler seulement après une utilisation validée par le serveur. */
    public PowerProgress gainMastery(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Maîtrise négative");
        return new PowerProgress(copies, Math.addExact(mastery, amount));
    }

    /** Les seuils sont des paramètres de conception et ne sont pas encore équilibrés. */
    public int level(int fourthThreshold, int fifthThreshold) {
        if (fourthThreshold <= 0 || fifthThreshold < fourthThreshold) {
            throw new IllegalArgumentException("Seuils de maîtrise invalides");
        }
        if (copies >= 5 && mastery >= fifthThreshold) return 5;
        if (copies >= 4 && mastery >= fourthThreshold) return 4;
        return Math.min(copies, 3);
    }
}
