package org.cambocraft.world;

import java.util.Set;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.cambocraft.registry.ModBlocks;
import org.cambocraft.registry.ModAttachments;

/** Voyage expérimental par interaction ; aucune génération de cadre à destination. */
@SuppressWarnings("UnstableApiUsage")
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
        UseItemCallback.EVENT.register((player, world, hand) -> {
            var stack = player.getStackInHand(hand);
            if (hand != Hand.MAIN_HAND || player.isSpectator() || !player.isSneaking()
                    || !world.getRegistryKey().equals(CAMBODIA) || !stack.isOf(Items.BOWL)) {
                return TypedActionResult.pass(stack);
            }
            if (world.isClient) return TypedActionResult.success(stack);
            if (!(player instanceof ServerPlayerEntity serverPlayer) || !(world instanceof ServerWorld origin)) return TypedActionResult.fail(stack);
            return recall(serverPlayer, origin) == ActionResult.SUCCESS
                    ? TypedActionResult.success(stack) : TypedActionResult.fail(stack);
        });
    }

    private static boolean canTravel(ServerPlayerEntity player, ServerWorld origin) {
        return player.getServerWorld() == origin && !player.isSpectator() && player.isAlive()
                && !player.hasVehicle() && !player.hasPassengers() && player.getMainHandStack().isOf(Items.BOWL)
                && !player.getItemCooldownManager().isCoolingDown(Items.BOWL);
    }

    public static ActionResult travel(ServerPlayerEntity player, ServerWorld origin, BlockPos clicked) {
        if (!canTravel(player, origin) || !origin.getBlockState(clicked).isOf(ModBlocks.LOTUS_STONE)
                || player.squaredDistanceTo(clicked.getX() + 0.5, clicked.getY() + 0.5, clicked.getZ() + 0.5) > 64.0) {
            return ActionResult.FAIL;
        }
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
        BlockPos saved = player.getAttached(ModAttachments.PORTAL_RETURN);
        BlockPos anchor = null;
        if (destination.equals(CAMBODIA)) {
            BlockPos current = player.getBlockPos();
            anchor = SafePortalLanding.isSafe(origin, current) ? current.toImmutable()
                    : SafePortalLanding.find(origin, clicked.getX(), clicked.getZ()).orElse(null);
            if (anchor == null) {
                player.sendMessage(Text.translatable("message.cambocraft.portal_no_return"), true);
                return ActionResult.FAIL;
            }
        }
        var landing = destination.equals(World.OVERWORLD) && saved != null
                ? landingNear(target, saved) : SafePortalLanding.find(target, clicked.getX(), clicked.getZ());
        if (landing.isEmpty()) {
            player.sendMessage(Text.translatable("message.cambocraft.portal_unsafe"), true);
            return ActionResult.FAIL;
        }
        if (anchor != null) player.setAttached(ModAttachments.PORTAL_RETURN, anchor);
        if (!teleport(player, target, landing.get())) {
            if (anchor != null) player.setAttached(ModAttachments.PORTAL_RETURN, saved);
            return ActionResult.FAIL;
        }
        if (destination.equals(World.OVERWORLD)) player.setAttached(ModAttachments.PORTAL_RETURN, null);
        player.sendMessage(Text.translatable(destination.equals(CAMBODIA)
                ? "message.cambocraft.portal_arrival" : "message.cambocraft.portal_returned"), true);
        return ActionResult.SUCCESS;
    }

    private static java.util.Optional<BlockPos> landingNear(ServerWorld world, BlockPos anchor) {
        if (!world.isInBuildLimit(anchor) || !world.getWorldBorder().contains(anchor)) return java.util.Optional.empty();
        world.getChunk(anchor.getX() >> 4, anchor.getZ() >> 4);
        return SafePortalLanding.isSafe(world, anchor) ? java.util.Optional.of(anchor)
                : SafePortalLanding.find(world, anchor.getX(), anchor.getZ());
    }

    private static ActionResult recall(ServerPlayerEntity player, ServerWorld origin) {
        if (!canTravel(player, origin)) return ActionResult.FAIL;
        BlockPos anchor = player.getAttached(ModAttachments.PORTAL_RETURN);
        ServerWorld target = origin.getServer().getOverworld();
        if (anchor == null) {
            player.sendMessage(Text.translatable("message.cambocraft.portal_no_return"), true);
            return ActionResult.FAIL;
        }
        var landing = landingNear(target, anchor);
        if (landing.isEmpty()) {
            player.sendMessage(Text.translatable("message.cambocraft.portal_unsafe"), true);
            return ActionResult.FAIL;
        }
        if (!teleport(player, target, landing.get())) return ActionResult.FAIL;
        player.setAttached(ModAttachments.PORTAL_RETURN, null);
        player.sendMessage(Text.translatable("message.cambocraft.portal_returned"), true);
        return ActionResult.SUCCESS;
    }

    private static boolean teleport(ServerPlayerEntity player, ServerWorld target, BlockPos pos) {
        if (!player.teleport(target, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                Set.of(), player.getYaw(), player.getPitch())) {
            player.sendMessage(Text.translatable("message.cambocraft.portal_unavailable"), true);
            return false;
        }
        player.getItemCooldownManager().set(Items.BOWL, 100);
        return true;
    }
}
