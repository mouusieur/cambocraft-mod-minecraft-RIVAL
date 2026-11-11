package org.cambocraft.servercheck;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import org.cambocraft.registry.ModBlocks;
import org.cambocraft.world.LotusPortal;

/** Scénario destructif uniquement dans le monde éphémère de la CI. */
public final class PortalPlayerCheck {
    private ServerPlayerEntity player;
    private BlockPos clicked;
    private int phase;
    private int ticks;

    public void initialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (player != null || !handler.player.getGameProfile().getName().equals("CamboBot")) return;
            player = handler.player;
            player.getInventory().setStack(0, new ItemStack(Items.BOWL));
            ServerWorld world = server.getOverworld();
            player.teleport(world, 10.5, 101, 10.5, 0, 0);
            clicked = makeFrame(world, new BlockPos(10, 100, 12));
            // La jungle peut ne présenter aucune clairière dans le rayon de recherche.
            // Une plateforme d'essai uniquement prépare un point d'arrivée déterministe.
            ServerWorld target = server.getWorld(LotusPortal.CAMBODIA);
            if (target == null) throw new AssertionError("Dimension absente");
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
                BlockPos floor = new BlockPos(10 + x, 200, 12 + z);
                target.setBlockState(floor, Blocks.STONE.getDefaultState());
                target.setBlockState(floor.up(), Blocks.AIR.getDefaultState());
                target.setBlockState(floor.up(2), Blocks.AIR.getDefaultState());
            }
            phase = 1;
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (player == null || phase == 0) return;
            ticks++;
            if ((phase == 1 && ticks >= 30) || (phase == 2 && ticks >= 120)) {
                player.sendMessage(Text.literal("CAMBO_CLICK " + clicked.getX() + " " + clicked.getY() + " " + clicked.getZ()), false);
                System.out.println("CAMBO_WAIT_CLICK " + phase + " " + clicked);
                phase = phase == 1 ? 11 : 22;
                ticks = 0;
                return;
            }
            if (phase == 11 && player.getServerWorld().getRegistryKey().equals(LotusPortal.CAMBODIA)) {
                if (!player.getMainHandStack().isOf(Items.BOWL)) throw new AssertionError("Bol consommé");
                clicked = makeFrame(player.getServerWorld(), player.getBlockPos().down().south(2));
                phase = 2;
                ticks = 0;
                return;
            }
            if (phase == 22 && player.getServerWorld().getRegistryKey().equals(World.OVERWORLD)) {
                if (!player.getMainHandStack().isOf(Items.BOWL)) throw new AssertionError("Bol consommé");
                phase = 3;
                ticks = 0;
                return;
            }
            if ((phase == 11 || phase == 22) && ticks > 200) {
                throw new AssertionError("Aucun voyage après le clic réseau, étape " + phase);
            }
            if (phase == 3 && ticks >= 30) {
                System.out.println("CAMBO_PLAYER_ROUNDTRIP_OK");
                phase = 0;
                server.stop(false);
            }
        });
    }

    private static BlockPos makeFrame(ServerWorld world, BlockPos base) {
        // Plateforme temporaire évitant que le bot tombe pendant le test.
        for (int x = -3; x <= 3; x++) for (int z = -4; z <= 2; z++) {
            BlockPos floor = base.add(x, 0, z);
            world.setBlockState(floor, Blocks.STONE.getDefaultState());
            world.setBlockState(floor.up(), Blocks.AIR.getDefaultState());
            world.setBlockState(floor.up(2), Blocks.AIR.getDefaultState());
            world.setBlockState(floor.up(3), Blocks.AIR.getDefaultState());
        }
        for (int y = 0; y < 4; y++) for (int x = -1; x <= 1; x++) {
            if (y == 0 || y == 3 || x != 0) world.setBlockState(base.add(x, y, 0), ModBlocks.LOTUS_STONE.getDefaultState());
        }
        return base;
    }
}
