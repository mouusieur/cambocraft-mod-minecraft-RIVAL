package org.cambocraft.world;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

/** Recherche bornée d'une surface non destructive. Ne téléporte ni ne crée de blocs. */
public final class SafePortalLanding {
    public static final int MAX_RADIUS = 8;
    private SafePortalLanding() { }

    /** Cherche la position la plus proche, sur la hauteur de surface, sans construction de plateforme. */
    public static Optional<BlockPos> find(ServerWorld world, int x, int z) {
        for (int radius = 0; radius <= MAX_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    // Les coordonnées du monde Minecraft sont bornées ; éviter le débordement d'entier.
                    long candidateX = (long) x + dx;
                    long candidateZ = (long) z + dz;
                    if (candidateX < -29_999_984 || candidateX > 29_999_984
                            || candidateZ < -29_999_984 || candidateZ > 29_999_984) continue;
                    BlockPos column = new BlockPos((int) candidateX, 0, (int) candidateZ);
                    if (!world.getWorldBorder().contains(column)) continue;
                    BlockPos feet = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, column);
                    if (isSafe(world, feet)) return Optional.of(feet.toImmutable());
                }
            }
        }
        return Optional.empty();
    }

    /** Vérification conservative sur deux blocs d'air et un sol plein non dangereux. */
    public static boolean isSafe(ServerWorld world, BlockPos feet) {
        BlockPos floor = feet.down();
        if (!world.isInBuildLimit(floor) || !world.isInBuildLimit(feet.up())
                || !world.getWorldBorder().contains(feet)) return false;
        BlockState ground = world.getBlockState(floor);
        if (!ground.isFullCube(world, floor) || !ground.getFluidState().isEmpty()
                || ground.isOf(Blocks.MAGMA_BLOCK) || ground.isOf(Blocks.CACTUS)
                || ground.isOf(Blocks.CAMPFIRE) || ground.isOf(Blocks.SOUL_CAMPFIRE)
                || ground.isOf(Blocks.POWDER_SNOW)) return false;
        return world.getBlockState(feet).isAir() && world.getBlockState(feet.up()).isAir();
    }
}
