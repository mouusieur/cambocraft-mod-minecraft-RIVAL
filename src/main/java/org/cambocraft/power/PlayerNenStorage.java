package org.cambocraft.power;

import java.io.IOException;
import java.util.Objects;
import net.minecraft.server.network.ServerPlayerEntity;
import org.cambocraft.registry.ModAttachments;

/** Accès à l'attachment côté serveur uniquement, sans communication avec le client. */
@SuppressWarnings("UnstableApiUsage")
public final class PlayerNenStorage {
    private PlayerNenStorage() { }

    public static NenState read(ServerPlayerEntity player) throws IOException {
        NenArchive archive = Objects.requireNonNull(player, "player").getAttached(ModAttachments.NEN);
        // Une archive présente mais invalide ne devient jamais silencieusement un nouvel état.
        return archive == null ? NenState.initial() : archive.read();
    }

    public static void write(ServerPlayerEntity player, NenState state) throws IOException {
        Objects.requireNonNull(player, "player");
        // Refuser l'écrasement d'une version future ou d'une archive endommagée.
        NenArchive existing = player.getAttached(ModAttachments.NEN);
        if (existing != null) existing.read();
        player.setAttached(ModAttachments.NEN, NenArchive.ofState(state));
    }
}
