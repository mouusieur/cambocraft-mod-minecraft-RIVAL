package org.cambocraft.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.cambocraft.registry.ModAttachments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Horloge serveur partagée, stockée sur l'Overworld ; météo et niveau d'eau non raccordés. */
@SuppressWarnings("UnstableApiUsage")
public final class SeasonRuntime {
    private static final Logger LOGGER = LoggerFactory.getLogger("cambocraft");
    private static final RegistryKey<World> CAMBODIA = RegistryKey.of(RegistryKeys.WORLD, Identifier.of("cambocraft", "cambodia"));
    private static final int DAYS = configuredDays();
    private static final SeasonClock CLOCK = new SeasonClock(DAYS);
    private static final long SEASON_LENGTH = DAYS * SeasonClock.TICKS_PER_DAY;
    private static boolean warnedInvalid;

    private SeasonRuntime() { }

    private static int configuredDays() {
        String value = System.getProperty("cambocraft.seasonDays", "8");
        try {
            int days = Integer.parseInt(value);
            if (days >= 1 && days <= 365) return days;
        } catch (NumberFormatException ignored) { }
        LOGGER.warn("cambocraft.seasonDays invalide : utilisation de 8 jours");
        return SeasonClock.DEFAULT_DAYS_PER_SEASON;
    }

    public static void initialize() {
        ServerTickEvents.END_WORLD_TICK.register(SeasonRuntime::advance);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            if (!player.getWorld().getRegistryKey().equals(CAMBODIA)) return;
            Long age = server.getOverworld().getAttached(ModAttachments.SEASON_TICKS);
            if (age == null || age >= 0) announce(player, CLOCK.sample(age == null ? 0 : age).season());
        });
    }

    /** À appeler sur le thread serveur ; une seule dimension fait avancer le compteur. */
    public static void advance(ServerWorld world) {
        if (!world.getRegistryKey().equals(World.OVERWORLD)) return;
        Long saved = world.getAttached(ModAttachments.SEASON_TICKS);
        long current = saved == null ? 0L : saved;
        if (current < 0 || current == Long.MAX_VALUE) {
            if (!warnedInvalid) {
                LOGGER.error("Horloge saisonnière invalide ou saturée ; aucune réinitialisation automatique");
                warnedInvalid = true;
            }
            return;
        }
        long next = current + 1;
        world.setAttached(ModAttachments.SEASON_TICKS, next);
        if (next % SEASON_LENGTH == 0) {
            SeasonClock.Season season = CLOCK.sample(next).season();
            for (ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList()) {
                if (player.getWorld().getRegistryKey().equals(CAMBODIA)) announce(player, season);
            }
        }
    }

    private static void announce(ServerPlayerEntity player, SeasonClock.Season season) {
        String key = switch (season) {
            case SECHE -> "season.cambocraft.dry";
            case PLUIES -> "season.cambocraft.rainy";
            case FRAICHE -> "season.cambocraft.cool";
        };
        player.sendMessage(Text.translatable("message.cambocraft.season", Text.translatable(key)), true);
    }
}
