package org.cambocraft.progression;

import java.util.Objects;
import java.util.regex.Pattern;

/** Données d'une zone ; ne crée ni terrain ni boss. */
public record ZoneDefinition(String id, int order, LocalizedText name, int minimumLevel,
                             String boss, LocalizedText bossName, String previousBoss,
                             boolean requiresRebirth) {
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");

    public record LocalizedText(String fr, String en) {
        public LocalizedText {
            if (fr == null || fr.isBlank() || en == null || en.isBlank()
                    || fr.length() > 200 || en.length() > 200) {
                throw new IllegalArgumentException("Traduction de zone absente ou trop longue");
            }
        }
    }

    public ZoneDefinition {
        validateId(id); validateId(boss);
        if (previousBoss != null) validateId(previousBoss);
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(bossName, "bossName");
        if (order < 1 || order > 12 || minimumLevel < 1 || minimumLevel > 200) {
            throw new IllegalArgumentException("Ordre ou niveau de zone invalide : " + id);
        }
    }

    private static void validateId(String id) {
        if (id == null || id.length() > 256 || !ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Identifiant invalide : " + id);
        }
    }
}
