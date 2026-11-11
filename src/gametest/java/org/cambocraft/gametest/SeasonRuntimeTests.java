package org.cambocraft.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import org.cambocraft.registry.ModAttachments;
import org.cambocraft.world.SeasonRuntime;

/** Contrôle les transitions de l'attachment ; aucun redémarrage de serveur n'est effectué. */
@SuppressWarnings("UnstableApiUsage")
public final class SeasonRuntimeTests {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void compteurServeur(TestContext context) {
        var world = context.getWorld();
        Long old = world.getAttached(ModAttachments.SEASON_TICKS);
        try {
            world.setAttached(ModAttachments.SEASON_TICKS, 123L);
            SeasonRuntime.advance(world);
            context.assertTrue(Long.valueOf(124L).equals(world.getAttached(ModAttachments.SEASON_TICKS)), "Horloge non avancée");
            world.setAttached(ModAttachments.SEASON_TICKS, -1L);
            SeasonRuntime.advance(world);
            context.assertTrue(Long.valueOf(-1L).equals(world.getAttached(ModAttachments.SEASON_TICKS)), "Horloge invalide réinitialisée");
            context.assertTrue(ModAttachments.SEASON_TICKS.isPersistent(), "Horloge non persistante");
        } finally {
            world.setAttached(ModAttachments.SEASON_TICKS, old);
        }
        context.complete();
    }
}
