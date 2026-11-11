package org.cambocraft.power;

import java.util.Objects;

/** Règles de tirage pures : le ticket doit être généré et validé côté serveur. */
public final class Gacha {
    public static final int TICKET_COUNT = 10_000;

    private Gacha() { }

    public record Draw(Rarity rarity, PityState pity) {
        public Draw {
            Objects.requireNonNull(rarity, "rarity");
            Objects.requireNonNull(pity, "pity");
        }
    }

    public static Rarity baseRarity(int ticket) {
        if (ticket < 0 || ticket >= TICKET_COUNT) {
            throw new IllegalArgumentException("Ticket hors limites : " + ticket);
        }
        if (ticket < 5500) return Rarity.COMMUN;
        if (ticket < 8300) return Rarity.RARE;
        if (ticket < 9500) return Rarity.EPIQUE;
        if (ticket < 9950) return Rarity.LEGENDAIRE;
        return Rarity.MYTHIQUE;
    }

    /** La garantie n'abaisse jamais un tirage naturel supérieur. */
    public static Draw draw(int ticket, PityState state) {
        Objects.requireNonNull(state, "state");
        Rarity natural = baseRarity(ticket);
        Rarity floor;
        if (state.mythic() == PityState.MYTHIC_LIMIT) floor = Rarity.MYTHIQUE;
        else if (state.legendary() == PityState.LEGENDARY_LIMIT) floor = Rarity.LEGENDAIRE;
        else if (state.epic() == PityState.EPIC_LIMIT) floor = Rarity.EPIQUE;
        else floor = Rarity.COMMUN;
        Rarity result = natural.atLeast(floor) ? natural : floor;
        return new Draw(result, state.after(result));
    }
}
