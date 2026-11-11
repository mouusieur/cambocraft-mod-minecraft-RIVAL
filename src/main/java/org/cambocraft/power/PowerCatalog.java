package org.cambocraft.power;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/** Instantané immuable : un futur chargeur JSON construira le catalogue complet avant échange. */
public final class PowerCatalog {
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");
    private final Map<String, Rarity> byId;
    private final Map<Rarity, List<String>> byRarity;

    public record Entry(String id, Rarity rarity) {
        public Entry {
            validateId(id);
            Objects.requireNonNull(rarity, "rarity");
        }
    }

    public PowerCatalog(Collection<Entry> entries) {
        Objects.requireNonNull(entries, "entries");
        Map<String, Rarity> ids = new HashMap<>();
        EnumMap<Rarity, List<String>> groups = new EnumMap<>(Rarity.class);
        for (Rarity rarity : Rarity.values()) groups.put(rarity, new ArrayList<>());
        for (Entry entry : entries) {
            Objects.requireNonNull(entry, "entry");
            if (ids.putIfAbsent(entry.id(), entry.rarity()) != null) {
                throw new IllegalArgumentException("Pouvoir dupliqué : " + entry.id());
            }
            groups.get(entry.rarity()).add(entry.id());
        }
        for (Rarity rarity : Rarity.values()) {
            List<String> list = groups.get(rarity);
            if (list.isEmpty()) throw new IllegalArgumentException("Rareté vide : " + rarity);
            list.sort(Comparator.naturalOrder());
            groups.put(rarity, List.copyOf(list));
        }
        byId = Map.copyOf(ids);
        byRarity = Map.copyOf(groups);
    }

    static void validateId(String id) {
        if (id == null || id.length() > 256 || !ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Identifiant de pouvoir invalide : " + id);
        }
    }

    public Rarity rarityOf(String id) { return byId.get(id); }
    public List<String> powersOf(Rarity rarity) { return byRarity.get(Objects.requireNonNull(rarity)); }
}
