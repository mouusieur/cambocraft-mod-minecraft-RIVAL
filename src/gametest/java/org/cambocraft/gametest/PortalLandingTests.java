package org.cambocraft.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import org.cambocraft.world.SafePortalLanding;

public final class PortalLandingTests {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void solEtObstacles(TestContext context) {
        BlockPos feet = context.getAbsolutePos(new BlockPos(3, 3, 3));
        var world = context.getWorld();
        world.setBlockState(feet.down(), Blocks.STONE.getDefaultState());
        world.setBlockState(feet, Blocks.AIR.getDefaultState());
        world.setBlockState(feet.up(), Blocks.AIR.getDefaultState());
        context.assertTrue(SafePortalLanding.isSafe(world, feet), "Sol solide et air refusés");
        var top = world.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, feet);
        context.assertTrue(SafePortalLanding.find(world, feet.getX(), feet.getZ()).isPresent(),
                "Recherche de surface échouée ; hauteur=" + top + ", pied attendu=" + feet);
        world.setBlockState(feet.up(), Blocks.STONE.getDefaultState());
        context.assertTrue(!SafePortalLanding.isSafe(world, feet), "Tête obstruée acceptée");
        world.setBlockState(feet.up(), Blocks.AIR.getDefaultState());
        world.setBlockState(feet, Blocks.WATER.getDefaultState());
        context.assertTrue(!SafePortalLanding.isSafe(world, feet), "Eau acceptée");
        world.setBlockState(feet, Blocks.AIR.getDefaultState());
        world.setBlockState(feet.down(), Blocks.MAGMA_BLOCK.getDefaultState());
        context.assertTrue(!SafePortalLanding.isSafe(world, feet), "Magma accepté");
        world.setBlockState(feet.down(), Blocks.AIR.getDefaultState());
        context.assertTrue(!SafePortalLanding.isSafe(world, feet), "Vide accepté");
        context.assertTrue(!SafePortalLanding.isSafe(world, new BlockPos(0, world.getBottomY(), 0)), "Sous le monde accepté");
        context.complete();
    }
}
