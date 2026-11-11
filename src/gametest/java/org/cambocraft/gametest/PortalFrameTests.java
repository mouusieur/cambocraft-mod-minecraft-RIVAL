package org.cambocraft.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.cambocraft.registry.ModBlocks;
import org.cambocraft.world.LotusPortalFrame;

/** Tests de cadre uniquement ; aucun voyage n'est encore implémenté. */
public final class PortalFrameTests {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void cadreOrientationsEtObstacles(TestContext context) {
        BlockPos center = context.getAbsolutePos(new BlockPos(3, 2, 3));
        var world = context.getWorld();
        context.assertTrue(!LotusPortalFrame.matches(world, center, Direction.Axis.X, Blocks.LANTERN), "Cadre absent accepté");
        for (int y = -1; y <= 2; y++) {
            for (int x = -1; x <= 1; x++) {
                if (y == -1 || y == 2 || x != 0) {
                    world.setBlockState(center.add(x, y, 0), ModBlocks.LOTUS_STONE.getDefaultState());
                }
            }
        }
        context.assertTrue(LotusPortalFrame.matches(world, center, Direction.Axis.X, Blocks.LANTERN), "Cadre X refusé");
        context.assertTrue(!LotusPortalFrame.matches(world, center, Direction.Axis.Z, Blocks.LANTERN), "Mauvais axe accepté");
        context.assertTrue(!LotusPortalFrame.matches(world, center, Direction.Axis.Y, Blocks.LANTERN), "Axe vertical accepté");
        world.setBlockState(center, Blocks.LANTERN.getDefaultState());
        context.assertTrue(LotusPortalFrame.matches(world, center, Direction.Axis.X, Blocks.LANTERN), "Bol au centre refusé");
        context.assertTrue(!LotusPortalFrame.matches(world, center, Direction.Axis.X, null), "Bloc étranger accepté");
        world.setBlockState(center.up(), Blocks.STONE.getDefaultState());
        context.assertTrue(!LotusPortalFrame.matches(world, center, Direction.Axis.X, Blocks.LANTERN), "Intérieur obstrué accepté");
        world.setBlockState(center.up(), Blocks.AIR.getDefaultState());
        world.setBlockState(center.add(1, 1, 0), Blocks.AIR.getDefaultState());
        context.assertTrue(!LotusPortalFrame.matches(world, center, Direction.Axis.X, Blocks.LANTERN), "Cadre cassé accepté");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void cadreAxeZ(TestContext context) {
        BlockPos center = context.getAbsolutePos(new BlockPos(3, 2, 3));
        var world = context.getWorld();
        for (int y = -1; y <= 2; y++) {
            for (int z = -1; z <= 1; z++) {
                if (y == -1 || y == 2 || z != 0) {
                    world.setBlockState(center.add(0, y, z), ModBlocks.LOTUS_STONE.getDefaultState());
                }
            }
        }
        context.assertTrue(LotusPortalFrame.matches(world, center, Direction.Axis.Z, null), "Cadre Z refusé");
        context.complete();
    }
}
