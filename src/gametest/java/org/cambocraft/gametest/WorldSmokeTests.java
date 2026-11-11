package org.cambocraft.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.cambocraft.registry.ModBlocks;

/** Tests réels du chargement et du premier bloc ; pas de test du portail absent. */
public final class WorldSmokeTests {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void pierreDuLotus(TestContext context) {
        Identifier id = Identifier.of("cambocraft", "lotus_stone");
        context.assertTrue(Registries.BLOCK.get(id) == ModBlocks.LOTUS_STONE, "Bloc non enregistré");
        context.assertTrue(Registries.ITEM.get(id) instanceof BlockItem, "BlockItem absent");
        context.setBlockState(1, 1, 1, ModBlocks.LOTUS_STONE);
        context.checkBlock(new BlockPos(1, 1, 1), block -> block == ModBlocks.LOTUS_STONE, "Bloc non placé");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void typeDimensionEtRessource(TestContext context) {
        Identifier id = Identifier.of("cambocraft", "cambodia");
        context.assertTrue(context.getWorld().getRegistryManager().get(RegistryKeys.DIMENSION_TYPE).containsId(id),
                "Type de dimension non chargé");
        context.assertTrue(context.getWorld().getServer().getResourceManager()
                .getResource(Identifier.of("cambocraft", "dimension/cambodia.json")).isPresent(),
                "Ressource de dimension absente");
        context.complete();
    }
}
