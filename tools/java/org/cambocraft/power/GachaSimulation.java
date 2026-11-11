package org.cambocraft.power;

import java.util.Locale;
import java.util.Random;

/** Simule le même code Java que le domaine, sans démontrer l'intégration Minecraft. */
public final class GachaSimulation {
    private static void run(int draws, long seed, boolean pity) {
        Random random = new Random(seed);
        int[] counts = new int[5];
        int[] misses = new int[3];
        int[] maxima = new int[3];
        int[] limits = {30, 80, 250};
        PityState state = PityState.INITIAL;
        for (int n = 0; n < draws; n++) {
            int ticket = random.nextInt(Gacha.TICKET_COUNT);
            Rarity result;
            if (pity) {
                Gacha.Draw draw = Gacha.draw(ticket, state);
                result = draw.rarity();
                state = draw.pity();
            } else result = Gacha.baseRarity(ticket);
            counts[result.ordinal()]++;
            for (int i = 0; i < 3; i++) {
                misses[i] = result.ordinal() >= i + 2 ? 0 : misses[i] + 1;
                maxima[i] = Math.max(maxima[i], misses[i]);
                if (pity && misses[i] > limits[i]) throw new AssertionError("Garantie dépassée");
            }
        }
        double[] probabilities = {0.55, 0.28, 0.12, 0.045, 0.005};
        System.out.printf(Locale.ROOT, "pity=%s tirages=%d seed=%d%n", pity, draws, seed);
        for (Rarity rarity : Rarity.values()) {
            int i = rarity.ordinal();
            if (!pity && Math.abs(counts[i] - draws * probabilities[i])
                    > 6 * Math.sqrt(draws * probabilities[i] * (1 - probabilities[i]))) {
                throw new AssertionError("Fréquence hors tolérance pour " + rarity);
            }
            System.out.printf(Locale.ROOT, "%s %d %.5f%%%n", rarity, counts[i], 100.0 * counts[i] / draws);
        }
        System.out.printf("Echecs consecutifs maximum : %d / %d / %d%n", maxima[0], maxima[1], maxima[2]);
    }

    public static void main(String[] args) {
        int draws = args.length > 0 ? Integer.parseInt(args[0]) : 5_000_000;
        long seed = args.length > 1 ? Long.parseLong(args[1]) : 20261111L;
        if (draws <= 0) throw new IllegalArgumentException("Nombre de tirages invalide");
        run(draws, seed, false);
        run(draws, seed, true);
    }
}
