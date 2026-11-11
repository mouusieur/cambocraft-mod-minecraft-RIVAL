package org.cambocraft.world;

import java.util.Set;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.cambocraft.registry.ModBlocks;

/** Voyage expérimental par interaction ; aucune génération de cadre à destination. */
public final class LotusPortal {
    public static final RegistryKey<World> CAMBODIA = RegistryKey.of(RegistryKeys.WORLD, Identifier.of("cambocraft", "cambodia"));
    private LotusPortal() { }

    public static void initialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (hand != Hand.MAIN_HAND || player.isSpectator()
                    || !player.getStackInHand(hand).isOf(Items.BOWL)
                    || !world.getBlockState(hit.getBlockPos()).isOf(ModBlocks.LOTUS_STONE)) return ActionResult.PASS;
            // Ne donne jamais l'autorité au client ; SUCCESS transmet la requête d'interaction au serveur.
            if (world.isClient) return ActionResult.SUCCESS;
            if (!(player instanceof ServerPlayerEntity serverPlayer) || !(world instanceof ServerWorld serverWorld)) return ActionResult.FAIL;
            return travel(serverPlayer, serverWorld, hit.getBlockPos());
        });
    }

    public static ActionResult travel(ServerPlayerEntity player, ServerWorld origin, BlockPos clicked) {
        if (player.getServerWorld() != origin || player.isSpectator() || !player.isAlive()
                || player.hasVehicle() || player.hasPassengers() || !player.getMainHandStack().isOf(Items.BOWL)
                || !origin.getBlockState(clicked).isOf(ModBlocks.LOTUS_STONE)
                || player.squaredDistanceTo(clicked.getX() + 0.5, clicked.getY() + 0.5, clicked.getZ() + 0.5) > 64.0) {
            return ActionResult.FAIL;
        }
        if (player.getItemCooldownManager().isCoolingDown(Items.BOWL)) return ActionResult.FAIL;
        BlockPos center = clicked.up();
        if (!LotusPortalFrame.matches(origin, center, Direction.Axis.X, null)
                && !LotusPortalFrame.matches(origin, center, Direction.Axis.Z, null)) {
            player.sendMessage(Text.translatable("message.cambocraft.portal_frame"), true);
            return ActionResult.FAIL;
        }
        RegistryKey<World> destination;
        if (origin.getRegistryKey().equals(World.OVERWORLD)) destination = CAMBODIA;
        else if (origin.getRegistryKey().equals(CAMBODIA)) destination = World.OVERWORLD;
        else return ActionResult.FAIL;
        ServerWorld target = origin.getServer().getWorld(destination);
        if (target == null) {
            player.sendMessage(Text.translatable("message.cambocraft.portal_unavailable"), true);
            return ActionResult.FAIL;
        }
        var landing = SafePortalLanding.find(target, clicked.getX(), clicked.getZ());
        if (landing.isEmpty()) {
            player.sendMessage(Text.translatable("message.cambocraft.portal_unsafe"), true);
            return ActionResult.FAIL;
        }
        BlockPos pos = landing.get();
        if (!player.teleport(target, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                Set.of(), player.getYaw(), player.getPitch())) {
            player.sendMessage(Text.translatable("message.cambocraft.portal_unavailable"), true);
            return ActionResult.FAIL;
        }
        player.getItemCooldownManager().set(Items.BOWL, 100);
        player.sendMessage(Text.translatable("message.cambocraft.portal_arrival"), true);
        return ActionResult.SUCCESS;
    }
}
