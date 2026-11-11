package org.cambocraft.gametest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import org.cambocraft.power.NenArchive;
import org.cambocraft.power.NenState;
import org.cambocraft.power.PityState;
import org.cambocraft.power.PowerProgress;
import org.cambocraft.power.Rarity;
import org.cambocraft.registry.ModAttachments;

/** La sérialisation est testée sur entité ; mort joueur et redémarrage restent à tester. */
@SuppressWarnings("UnstableApiUsage")
public final class NenAttachmentTests {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void sauvegardeNBT(TestContext context) throws IOException {
        NenState state = new NenState(new PityState(2, 20, 100),
                Map.of("cambocraft:test", new PowerProgress(2, 50)), List.of("cambocraft:test"),
                new NenState.Pending("cambocraft:offre", Rarity.EPIQUE));
        PigEntity first = Objects.requireNonNull(EntityType.PIG.create(context.getWorld()));
        first.setAttached(ModAttachments.NEN, NenArchive.ofState(state));
        NbtCompound nbt = first.writeNbt(new NbtCompound());
        PigEntity second = Objects.requireNonNull(EntityType.PIG.create(context.getWorld()));
        second.readNbt(nbt);
        NenArchive loaded = second.getAttached(ModAttachments.NEN);
        context.assertTrue(loaded != null && loaded.read().equals(state), "État Nen perdu dans NBT");
        context.assertTrue(ModAttachments.NEN.isPersistent() && ModAttachments.NEN.copyOnDeath(), "Flags de persistance absents");
        first.discard();
        second.discard();
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void pointRetourNBT(TestContext context) {
        BlockPos anchor = new BlockPos(-400, 71, 300);
        PigEntity first = Objects.requireNonNull(EntityType.PIG.create(context.getWorld()));
        first.setAttached(ModAttachments.PORTAL_RETURN, anchor);
        NbtCompound nbt = first.writeNbt(new NbtCompound());
        PigEntity second = Objects.requireNonNull(EntityType.PIG.create(context.getWorld()));
        second.readNbt(nbt);
        context.assertTrue(anchor.equals(second.getAttached(ModAttachments.PORTAL_RETURN)), "Point de retour perdu en NBT");
        context.assertTrue(ModAttachments.PORTAL_RETURN.isPersistent() && ModAttachments.PORTAL_RETURN.copyOnDeath(), "Flags de retour absents");
        first.discard();
        second.discard();
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void archiveInconnueConservee(TestContext context) throws IOException {
        byte[] bytes = {1, 2, 3, 4};
        PigEntity first = Objects.requireNonNull(EntityType.PIG.create(context.getWorld()));
        first.setAttached(ModAttachments.NEN, NenArchive.fromBuffer(ByteBuffer.wrap(bytes)));
        NbtCompound nbt = first.writeNbt(new NbtCompound());
        PigEntity second = Objects.requireNonNull(EntityType.PIG.create(context.getWorld()));
        second.readNbt(nbt);
        NenArchive loaded = Objects.requireNonNull(second.getAttached(ModAttachments.NEN));
        ByteBuffer saved = loaded.toBuffer();
        context.assertTrue(saved.remaining() == bytes.length && saved.get() == 1 && saved.get() == 2
                && saved.get() == 3 && saved.get() == 4, "Archive inconnue perdue");
        boolean rejected = false;
        try { loaded.read(); } catch (IOException expected) { rejected = true; }
        context.assertTrue(rejected, "Une archive invalide ne doit pas être initialisée à vide");
        first.discard();
        second.discard();
        context.complete();
    }
}
