package org.cambocraft.gametest;

import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import org.cambocraft.progression.ZoneCatalog;
import org.cambocraft.registry.ZoneData;

public final class ZoneDataTests {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void zonesChargees(TestContext context) {
        ZoneCatalog catalog = ZoneData.current();
        context.assertTrue(catalog.zones().size() == 12, "Douze zones attendues");
        context.assertTrue(catalog.access("cambocraft:tonle", 1, 0, Set.of()) == ZoneCatalog.Access.ALLOWED, "Village inaccessible");
        context.assertTrue(catalog.access("cambocraft:ly", 15, 0, Set.of()) == ZoneCatalog.Access.BOSS_REQUIRED, "Boss précédent ignoré");
        context.assertTrue(catalog.access("cambocraft:ly", 15, 0, Set.of("cambocraft:river_chief")) == ZoneCatalog.Access.ALLOWED, "Accès refusé");
        context.assertTrue(catalog.access("cambocraft:royaume_esprits", 200, 0, Set.of()) == ZoneCatalog.Access.REBIRTH_REQUIRED, "Renaissance ignorée");
        context.complete();
    }
}
