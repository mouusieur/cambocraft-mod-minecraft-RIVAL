package org.cambocraft.progression;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ProgressionTest {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static void invalid(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Exception attendue");
    }
    private static ZoneCatalog catalog() {
        List<ZoneDefinition> zones = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            var name = new ZoneDefinition.LocalizedText("Zone " + i, "Zone " + i);
            zones.add(new ZoneDefinition("cambocraft:z" + i, i, name, i == 12 ? 1 : i * 10,
                    "cambocraft:b" + i, name, i == 1 ? null : "cambocraft:b" + (i - 1), i == 12));
        }
        return new ZoneCatalog(zones);
    }
    public static void main(String[] args) {
        check(PlayerLevel.INITIAL.gain(119).equals(new PlayerLevel(1, 119)), "XP restante");
        check(PlayerLevel.INITIAL.gain(120).equals(new PlayerLevel(2, 0)), "Premier niveau");
        long next = PlayerLevel.requiredXp(2);
        check(PlayerLevel.INITIAL.gain(120 + next + 4).equals(new PlayerLevel(3, 4)), "Plusieurs niveaux");
        check(PlayerLevel.INITIAL.gain(Long.MAX_VALUE).equals(new PlayerLevel(200, 0)), "Plafond");
        check(new PlayerLevel(200, 0).gain(1).equals(new PlayerLevel(200, 0)), "XP au plafond");
        for (int i = 1; i < 199; i++) check(PlayerLevel.requiredXp(i) < PlayerLevel.requiredXp(i + 1), "Courbe croissante");
        invalid(() -> PlayerLevel.INITIAL.gain(-1));
        invalid(() -> new PlayerLevel(1, 120));
        invalid(() -> new PlayerLevel(200, 1));
        invalid(() -> PlayerLevel.requiredXp(200));
        var catalog = catalog();
        check(catalog.access("cambocraft:z1", 10, 0, Set.of()) == ZoneCatalog.Access.ALLOWED, "Départ");
        check(catalog.access("cambocraft:z2", 19, 0, Set.of()) == ZoneCatalog.Access.LEVEL_TOO_LOW, "Niveau");
        check(catalog.access("cambocraft:z2", 20, 0, Set.of()) == ZoneCatalog.Access.BOSS_REQUIRED, "Boss");
        check(catalog.access("cambocraft:z2", 20, 0, Set.of("cambocraft:b1")) == ZoneCatalog.Access.ALLOWED, "Déblocage");
        check(catalog.access("cambocraft:z12", 200, 0, Set.of("cambocraft:b11")) == ZoneCatalog.Access.REBIRTH_REQUIRED, "Renaissance");
        check(catalog.access("cambocraft:z12", 1, 1, Set.of()) == ZoneCatalog.Access.BOSS_REQUIRED, "Boss après renaissance");
        check(catalog.access("cambocraft:z12", 1, 1, Set.of("cambocraft:b11")) == ZoneCatalog.Access.ALLOWED, "Renaissance et boss");
        invalid(() -> catalog.access("cambocraft:inconnue", 1, 0, Set.of()));
        invalid(() -> catalog.access("cambocraft:z1", 0, 0, Set.of()));
        invalid(() -> new ZoneCatalog(List.of()));
        try { catalog.zones().clear(); throw new AssertionError("Collection modifiable"); }
        catch (UnsupportedOperationException expected) { }
        var broken = new ArrayList<>(catalog.zones());
        broken.set(1, broken.get(0));
        invalid(() -> new ZoneCatalog(broken));
        System.out.println("ProgressionTest : OK");
    }
}
