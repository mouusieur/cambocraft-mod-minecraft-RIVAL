package org.cambocraft.power;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

/** Transitions métier pures ; le serveur doit sérialiser et valider chaque opération. */
public final class NenJournal {
    private NenJournal() { }

    private static int next(IntUnaryOperator random, int bound) {
        int value = random.applyAsInt(bound);
        if (value < 0 || value >= bound) throw new IllegalArgumentException("RNG hors limites");
        return value;
    }

    /** Une offre non résolue bloque les tirages suivants, même après sauvegarde future. */
    public static NenState offer(NenState state, PowerCatalog catalog, IntUnaryOperator random) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(catalog, "catalog");
        Objects.requireNonNull(random, "random");
        if (state.pending() != null) throw new IllegalStateException("Offre déjà en attente");
        Gacha.Draw draw = Gacha.draw(next(random, Gacha.TICKET_COUNT), state.pity());
        List<String> candidates = catalog.powersOf(draw.rarity());
        String id = candidates.get(next(random, candidates.size()));
        return new NenState(draw.pity(), state.owned(), state.equipped(), new NenState.Pending(id, draw.rarity()));
    }

    /** Accepter conserve l'inventaire précédent ; rejeter conserve les compteurs post-tirage. */
    public static NenState resolve(NenState state, PowerCatalog catalog, boolean accept) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(catalog, "catalog");
        NenState.Pending pending = state.pending();
        if (pending == null) throw new IllegalStateException("Aucune offre en attente");
        Map<String, PowerProgress> owned = state.owned();
        if (accept) {
            if (catalog.rarityOf(pending.id()) != pending.rarity()) {
                throw new IllegalStateException("Pouvoir retiré ou modifié ; rejeter l'offre");
            }
            owned = new HashMap<>(owned);
            owned.compute(pending.id(), (id, prior) -> prior == null ? PowerProgress.FIRST : prior.duplicate());
        }
        return new NenState(state.pity(), owned, state.equipped(), null);
    }
}
