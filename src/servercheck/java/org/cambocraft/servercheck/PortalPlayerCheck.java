package org.cambocraft.servercheck;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.cambocraft.registry.ModBlocks;
import org.cambocraft.world.LotusPortal;
import org.cambocraft.world.LotusPortalFrame;
import org.cambocraft.world.SafePortalLanding;

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
            phase = 1;
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (player == null || phase == 0 || ++ticks < (phase == 2 ? 120 : 30)) return;
            ticks = 0;
            if (phase == 3) {
                System.out.println("CAMBO_PLAYER_ROUNDTRIP_OK");
                phase = 0;
                server.stop(false);
                return;
            }
            ServerWorld origin = player.getServerWorld();
            var hit = new BlockHitResult(Vec3d.ofCenter(clicked), Direction.UP, clicked, false);
            ActionResult result = UseBlockCallback.EVENT.invoker().interact(player, origin, Hand.MAIN_HAND, hit);
            if (result != ActionResult.SUCCESS) {
                System.out.println("CAMBO_PORTAL_DIAG pos=" + player.getPos() + " clicked=" + clicked
                        + " hand=" + player.getMainHandStack() + " alive=" + player.isAlive()
                        + " spectator=" + player.isSpectator() + " vehicle=" + player.hasVehicle()
                        + " distance=" + player.squaredDistanceTo(Vec3d.ofCenter(clicked))
                        + " cooldown=" + player.getItemCooldownManager().isCoolingDown(Items.BOWL)
                        + " frameX=" + LotusPortalFrame.matches(origin, clicked.up(), Direction.Axis.X, null)
                        + " frameZ=" + LotusPortalFrame.matches(origin, clicked.up(), Direction.Axis.Z, null)
                        + " target=" + SafePortalLanding.find(server.getWorld(LotusPortal.CAMBODIA), clicked.getX(), clicked.getZ()));
                throw new AssertionError("Interaction portail refusée : " + result);
            }
            if (phase == 1) {
                if (!player.getServerWorld().getRegistryKey().equals(LotusPortal.CAMBODIA)) throw new AssertionError("Aller échoué");
                if (!player.getMainHandStack().isOf(Items.BOWL)) throw new AssertionError("Bol consommé");
                BlockPos pos = player.getBlockPos();
                clicked = makeFrame(player.getServerWorld(), pos.down().south(2));
                phase = 2;
            } else {
                if (!player.getServerWorld().getRegistryKey().equals(World.OVERWORLD)) throw new AssertionError("Retour échoué");
                if (!player.getMainHandStack().isOf(Items.BOWL)) throw new AssertionError("Bol consommé");
                phase = 3;
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
