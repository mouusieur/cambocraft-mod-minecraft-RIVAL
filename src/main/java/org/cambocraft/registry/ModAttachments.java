package org.cambocraft.registry;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.cambocraft.Cambocraft;
import org.cambocraft.power.NenArchive;

/** Stockage sans synchronisation cliente ; lecture et mutations doivent rester serveur. */
@SuppressWarnings("UnstableApiUsage")
public final class ModAttachments {
    private static final Codec<NenArchive> ARCHIVE_CODEC = Codec.BYTE_BUFFER.xmap(NenArchive::fromBuffer, NenArchive::toBuffer);
    public static final AttachmentType<NenArchive> NEN = AttachmentRegistry.create(
            Identifier.of(Cambocraft.ID, "nen"), builder -> builder.persistent(ARCHIVE_CODEC).copyOnDeath());

    public static final AttachmentType<BlockPos> PORTAL_RETURN = AttachmentRegistry.create(
            Identifier.of(Cambocraft.ID, "portal_return"), builder -> builder.persistent(BlockPos.CODEC).copyOnDeath());

    public static final AttachmentType<Long> SEASON_TICKS = AttachmentRegistry.createPersistent(
            Identifier.of(Cambocraft.ID, "season_ticks"), Codec.LONG);

    private ModAttachments() { }
    public static void initialize() { /* Déclenche l'enregistrement statique. */ }
}
