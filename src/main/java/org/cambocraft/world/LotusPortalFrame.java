package org.cambocraft.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.cambocraft.registry.ModBlocks;

/** Validation d'un cadre vertical 3×4. Ne crée pas encore de portail ni de téléportation. */
public final class LotusPortalFrame {
    private LotusPortalFrame() { }

    /**
     * centre est l'emplacement intérieur inférieur du bol. Un bol déjà placé est toléré ;
     * le reste de l'intérieur doit être vide. Ne modifie ni ne charge volontairement le monde.
     */
    public static boolean matches(BlockView world, BlockPos centre, Direction.Axis axis, Block bowl) {
        if (axis != Direction.Axis.X && axis != Direction.Axis.Z) return false;
        for (int y = -1; y <= 2; y++) {
            for (int horizontal = -1; horizontal <= 1; horizontal++) {
                BlockPos pos = centre.add(axis == Direction.Axis.X ? horizontal : 0, y,
                        axis == Direction.Axis.Z ? horizontal : 0);
                BlockState state = world.getBlockState(pos);
                boolean border = y == -1 || y == 2 || horizontal != 0;
                if (border) {
                    if (!state.isOf(ModBlocks.LOTUS_STONE)) return false;
                } else if (!state.isAir() && !(y == 0 && bowl != null && state.isOf(bowl))) {
                    return false;
                }
            }
        }
        return true;
    }
}
