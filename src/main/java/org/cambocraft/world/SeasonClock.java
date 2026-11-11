package org.cambocraft.world;

/** Calcul pur du cycle saisonnier ; aucun bloc ou météo Minecraft n'est modifié ici. */
public final class SeasonClock {
    public static final long TICKS_PER_DAY = 24_000L;
    public static final int DEFAULT_DAYS_PER_SEASON = 8;

    public enum Season { SECHE, PLUIES, FRAICHE }

    public record Snapshot(Season season, int dayInSeason, int tickOfDay,
                           long tickInSeason, long ticksPerSeason) {
        public Snapshot {
            if (season == null || dayInSeason < 0 || tickOfDay < 0 || tickOfDay >= TICKS_PER_DAY
                    || ticksPerSeason <= 0 || ticksPerSeason % TICKS_PER_DAY != 0
                    || tickInSeason < 0 || tickInSeason >= ticksPerSeason
                    || dayInSeason != tickInSeason / TICKS_PER_DAY
                    || tickOfDay != tickInSeason % TICKS_PER_DAY) {
                throw new IllegalArgumentException("Instant saisonnier invalide");
            }
        }

        /** Lac en décrue sèche, en crue de mousson et stable pendant la saison fraîche. */
        public double lakeFraction() {
            double progress = (double) tickInSeason / ticksPerSeason;
            return switch (season) {
                case SECHE -> 1.0 - progress;
                case PLUIES -> progress;
                case FRAICHE -> 1.0;
            };
        }

        /** Niveau cible indicatif ; aucune écriture de bloc n'est effectuée. */
        public int lakeHeight(int minimum, int maximum) {
            if (minimum > maximum) throw new IllegalArgumentException("Niveaux de lac inversés");
            long range = (long) maximum - minimum;
            return (int) ((long) minimum + Math.round(range * lakeFraction()));
        }
    }

    private final long ticksPerSeason;

    public SeasonClock(int daysPerSeason) {
        if (daysPerSeason <= 0) throw new IllegalArgumentException("Durée de saison non positive");
        ticksPerSeason = daysPerSeason * TICKS_PER_DAY;
    }

    /** elapsedTicks doit être un compteur de monde monotone sauvegardé, pas l'heure modifiable. */
    public Snapshot sample(long elapsedTicks) {
        if (elapsedTicks < 0) throw new IllegalArgumentException("Âge du monde négatif");
        long tick = elapsedTicks % ticksPerSeason;
        Season season = Season.values()[(int) ((elapsedTicks / ticksPerSeason) % Season.values().length)];
        return new Snapshot(season, (int) (tick / TICKS_PER_DAY), (int) (tick % TICKS_PER_DAY), tick, ticksPerSeason);
    }
}
