package org.cambocraft.power;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** État métier immuable ; sa persistance et sa synchronisation sont à raccorder au serveur. */
public record NenState(PityState pity, Map<String, PowerProgress> owned,
                       List<String> equipped, Pending pending) {
    public static final int MAX_EQUIPPED = 3;

    public record Pending(String id, Rarity rarity) {
        public Pending {
            PowerCatalog.validateId(id);
            Objects.requireNonNull(rarity, "rarity");
        }
    }

    public NenState {
        Objects.requireNonNull(pity, "pity");
        owned = Map.copyOf(Objects.requireNonNull(owned, "owned"));
        equipped = List.copyOf(Objects.requireNonNull(equipped, "equipped"));
        for (String id : owned.keySet()) PowerCatalog.validateId(id);
        if (equipped.size() > MAX_EQUIPPED || new HashSet<>(equipped).size() != equipped.size()
                || !owned.keySet().containsAll(equipped)) {
            throw new IllegalArgumentException("Pouvoirs équipés invalides");
        }
    }

    public static NenState initial() { return new NenState(PityState.INITIAL, Map.of(), List.of(), null); }

    public NenState equip(List<String> ids) { return new NenState(pity, owned, ids, pending); }
}
