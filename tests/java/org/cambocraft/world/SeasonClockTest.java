package org.cambocraft.world;

import static org.cambocraft.world.SeasonClock.Season.*;

public final class SeasonClockTest {
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static void invalid(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Exception attendue");
    }
    public static void main(String[] args) {
        long day = SeasonClock.TICKS_PER_DAY;
        long season = 8 * day;
        SeasonClock clock = new SeasonClock(SeasonClock.DEFAULT_DAYS_PER_SEASON);
        check(clock.sample(0).season() == SECHE && clock.sample(0).dayInSeason() == 0, "Début");
        check(clock.sample(day + 42).dayInSeason() == 1 && clock.sample(day + 42).tickOfDay() == 42, "Jour");
        check(clock.sample(season - 1).season() == SECHE && clock.sample(season).season() == PLUIES, "Mousson");
        check(clock.sample(2 * season).season() == FRAICHE && clock.sample(3 * season).season() == SECHE, "Cycle");
        check(clock.sample(0).lakeHeight(60, 68) == 68 && clock.sample(season).lakeHeight(60, 68) == 60, "Décrue");
        check(clock.sample(season + season / 2).lakeHeight(60, 68) == 64, "Crue");
        check(clock.sample(2 * season).lakeHeight(60, 68) == 68, "Fin de crue");
        check(clock.sample(3 * season).lakeHeight(60, 68) == 68, "Raccord du cycle");
        check(clock.sample(0).lakeHeight(Integer.MIN_VALUE, Integer.MAX_VALUE) == Integer.MAX_VALUE, "Bornes entières");
        check(new SeasonClock(1).sample(day).season() == PLUIES, "Durée configurable");
        check(new SeasonClock(Integer.MAX_VALUE).sample(Long.MAX_VALUE).dayInSeason() >= 0, "Grands temps");
        invalid(() -> new SeasonClock(0));
        invalid(() -> clock.sample(-1));
        invalid(() -> clock.sample(0).lakeHeight(2, 1));
        invalid(() -> new SeasonClock.Snapshot(SECHE, 0, 0, 1, day));
        invalid(() -> new SeasonClock.Snapshot(SECHE, 0, 0, 0, day + 1));
        // Chaque transition est continue à un tick près (avant arrondi en blocs).
        for (long boundary = season; boundary <= 3 * season; boundary += season) {
            check(Math.abs(clock.sample(boundary).lakeFraction() - clock.sample(boundary - 1).lakeFraction()) <= 1.0 / season + 1e-12, "Continuité");
        }
        System.out.println("SeasonClockTest : OK");
    }
}
