package org.cambocraft.servercheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.cambocraft.registry.ModAttachments;
import org.cambocraft.registry.ZoneData;

/** Mod auxiliaire exclusif à la CI, jamais distribué. */
@SuppressWarnings("UnstableApiUsage")
public final class ServerCheck implements ModInitializer {
    private static final Path EXPECTED = Path.of("cambocraft-expected-ticks.txt");
    private boolean ready;
    private int ticks;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, Identifier.of("cambocraft", "cambodia"));
            if (server.getWorld(key) == null) throw new AssertionError("Dimension Cambocraft non créée");
            if (ZoneData.current().zones().size() != 12) throw new AssertionError("Zones non chargées");
            boolean restart = "1".equals(System.getenv("CAMBO_RESTART_CHECK"));
            if (restart) {
                try {
                    long expected = Long.parseLong(Files.readString(EXPECTED).trim());
                    Long actual = server.getOverworld().getAttached(ModAttachments.SEASON_TICKS);
                    if (actual == null || actual != expected || actual <= 0) {
                        throw new AssertionError("Horloge non restaurée : " + actual + " attendu " + expected);
                    }
                    System.out.println("CAMBO_SERVER_RESTART_OK " + actual);
                } catch (IOException | NumberFormatException ex) {
                    throw new AssertionError("Témoin de redémarrage illisible", ex);
                }
            } else {
                System.out.println("CAMBO_SERVER_WORLD_OK");
            }
            ready = true;
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!ready || ++ticks < 40) return;
            Long actual = server.getOverworld().getAttached(ModAttachments.SEASON_TICKS);
            if (actual == null || actual <= 0) throw new AssertionError("Horloge non avancée");
            if (!"1".equals(System.getenv("CAMBO_RESTART_CHECK"))) {
                try { Files.writeString(EXPECTED, Long.toString(actual)); }
                catch (IOException ex) { throw new AssertionError("Témoin non enregistré", ex); }
            }
            ready = false;
            System.out.println("CAMBO_SERVER_STOP_OK " + actual);
            server.stop(false);
        });
    }
}
