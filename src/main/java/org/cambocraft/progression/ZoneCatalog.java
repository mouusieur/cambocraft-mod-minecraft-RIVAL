package org.cambocraft.progression;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Instantané de progression ; le serveur doit fournir des victoires et niveaux fiables. */
public final class ZoneCatalog {
    public enum Access { ALLOWED, LEVEL_TOO_LOW, BOSS_REQUIRED, REBIRTH_REQUIRED }
    private final List<ZoneDefinition> ordered;
    private final Map<String, ZoneDefinition> byId;

    public ZoneCatalog(List<ZoneDefinition> zones) {
        Objects.requireNonNull(zones, "zones");
        if (zones.size() != 12) throw new IllegalArgumentException("Douze zones sont requises");
        var sorted = new ArrayList<>(zones);
        sorted.sort(Comparator.comparingInt(ZoneDefinition::order));
        var ids = new HashMap<String, ZoneDefinition>();
        var bosses = new HashSet<String>();
        ZoneDefinition previous = null;
        for (int i = 0; i < sorted.size(); i++) {
            ZoneDefinition zone = Objects.requireNonNull(sorted.get(i));
            if (zone.order() != i + 1 || ids.putIfAbsent(zone.id(), zone) != null || !bosses.add(zone.boss())) {
                throw new IllegalArgumentException("Ordre ou identifiant de zone/boss dupliqué");
            }
            if (!Objects.equals(zone.previousBoss(), previous == null ? null : previous.boss())) {
                throw new IllegalArgumentException("Boss précédent incohérent : " + zone.id());
            }
            if ((i < 11 && zone.requiresRebirth()) || (i == 11 && !zone.requiresRebirth())) {
                throw new IllegalArgumentException("Condition de renaissance incohérente : " + zone.id());
            }
            if (previous != null && i < 11 && zone.minimumLevel() < previous.minimumLevel()) {
                throw new IllegalArgumentException("Niveaux de zone non croissants");
            }
            previous = zone;
        }
        ordered = List.copyOf(sorted);
        byId = Map.copyOf(ids);
    }

    public List<ZoneDefinition> zones() { return ordered; }

    public Access access(String id, int level, int rebirths, Set<String> defeatedBosses) {
        ZoneDefinition zone = byId.get(Objects.requireNonNull(id));
        if (zone == null) throw new IllegalArgumentException("Zone inconnue : " + id);
        if (level < 1 || level > 200 || rebirths < 0) throw new IllegalArgumentException("Progression invalide");
        Objects.requireNonNull(defeatedBosses, "defeatedBosses");
        if (zone.requiresRebirth() && rebirths == 0) return Access.REBIRTH_REQUIRED;
        if (level < zone.minimumLevel()) return Access.LEVEL_TOO_LOW;
        if (zone.previousBoss() != null && !defeatedBosses.contains(zone.previousBoss())) return Access.BOSS_REQUIRED;
        return Access.ALLOWED;
    }
}
