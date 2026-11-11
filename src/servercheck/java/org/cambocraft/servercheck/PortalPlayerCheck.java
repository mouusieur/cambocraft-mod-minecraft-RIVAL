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
import org.cambocraft.registry.ModAttachments;
import org.cambocraft.world.LotusPortal;
import org.cambocraft.power.NenState;
import org.cambocraft.power.PityState;
import org.cambocraft.power.PlayerNenStorage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/** Scénario destructif uniquement dans le monde éphémère de la CI. */
@SuppressWarnings("UnstableApiUsage")
public final class PortalPlayerCheck {
    private ServerPlayerEntity player;
    private BlockPos clicked;
    private int phase;
    private int ticks;

    public void initialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (player != null || !handler.player.getGameProfile().getName().equals("CamboBot")) return;
            player = handler.player;
            if (phase == 4) {
                if (!player.getServerWorld().getRegistryKey().equals(LotusPortal.CAMBODIA)
                        || !new BlockPos(10, 101, 10).equals(player.getAttached(ModAttachments.PORTAL_RETURN)))
                    throw new AssertionError("Dimension ou point de retour perdu après reconnexion");
                try {
                    if (!PlayerNenStorage.read(player).equals(testNen())) throw new AssertionError("Nen perdu après reconnexion");
                } catch (IOException ex) { throw new AssertionError("Nen illisible après reconnexion", ex); }
                System.out.println("CAMBO_PLAYER_RECONNECT_OK");
                phase = 2;
                ticks = 0;
                return;
            }
            try { PlayerNenStorage.write(player, testNen()); }
            catch (IOException ex) { throw new AssertionError("Écriture Nen échouée", ex); }
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
                String message = phase == 1 ? "CAMBO_CLICK " + clicked.getX() + " " + clicked.getY() + " " + clicked.getZ() : "CAMBO_RECALL";
                player.sendMessage(Text.literal(message), false);
                System.out.println("CAMBO_WAIT_CLICK " + phase + " " + clicked);
                phase = phase == 1 ? 11 : 22;
                ticks = 0;
                return;
            }
            if (phase == 11 && player.getServerWorld().getRegistryKey().equals(LotusPortal.CAMBODIA)) {
                if (!player.getMainHandStack().isOf(Items.BOWL)) throw new AssertionError("Bol consommé");
                if (!new BlockPos(10, 101, 10).equals(player.getAttached(ModAttachments.PORTAL_RETURN)))
                    throw new AssertionError("Point de retour manquant");
                phase = 4;
                ticks = 0;
                player.networkHandler.disconnect(Text.literal("CAMBO_RECONNECT"));
                player = null;
                return;
            }
            if (phase == 22 && player.getServerWorld().getRegistryKey().equals(World.OVERWORLD)) {
                if (!player.getMainHandStack().isOf(Items.BOWL)) throw new AssertionError("Bol consommé");
                if (!player.getBlockPos().equals(new BlockPos(10, 101, 10))) throw new AssertionError("Retour éloigné du point sauvegardé");
                if (player.getAttached(ModAttachments.PORTAL_RETURN) != null) throw new AssertionError("Point de retour non effacé");
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

    private static NenState testNen() {
        return new NenState(new PityState(1, 2, 3), Map.of(), List.of(), null);
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
