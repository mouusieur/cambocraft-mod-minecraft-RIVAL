package org.cambocraft.power;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.zip.CRC32;

/** Codec autonome versionné ; il ne remplace ni le stockage Minecraft ni une transaction disque. */
public final class NenStateCodec {
    private static final int MAGIC = 0x43424e4e; // CBNN
    private static final int VERSION = 1;
    // Bornes défensives du format actuel, pas un équilibrage du nombre de pouvoirs.
    public static final int MAX_ENTRIES = 100_000;
    public static final int MAX_BYTES = 32 * 1024 * 1024;

    private NenStateCodec() { }

    public static byte[] encode(NenState state) throws IOException {
        Objects.requireNonNull(state, "state");
        if (state.owned().size() > MAX_ENTRIES) throw new IOException("Carnet trop volumineux");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(buffer)) {
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(state.pity().epic());
            out.writeInt(state.pity().legendary());
            out.writeInt(state.pity().mythic());
            out.writeInt(state.owned().size());
            // Écriture déterministe pour faciliter les sauvegardes et les tests.
            for (Map.Entry<String, PowerProgress> entry : new TreeMap<>(state.owned()).entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeInt(entry.getValue().copies());
                out.writeInt(entry.getValue().mastery());
            }
            out.writeInt(state.equipped().size());
            for (String id : state.equipped()) out.writeUTF(id);
            out.writeByte(state.pending() == null ? 0 : 1);
            if (state.pending() != null) {
                out.writeUTF(state.pending().id());
                out.writeUTF(state.pending().rarity().name());
            }
        }
        byte[] body = buffer.toByteArray();
        if (body.length + Integer.BYTES > MAX_BYTES) throw new IOException("Sauvegarde trop volumineuse");
        CRC32 crc = new CRC32();
        crc.update(body);
        try (DataOutputStream out = new DataOutputStream(buffer)) { out.writeInt((int) crc.getValue()); }
        return buffer.toByteArray();
    }

    public static NenState decode(byte[] data) throws IOException {
        Objects.requireNonNull(data, "data");
        if (data.length < 4 * Integer.BYTES || data.length > MAX_BYTES) throw new IOException("Taille de sauvegarde invalide");
        int bodyLength = data.length - Integer.BYTES;
        CRC32 crc = new CRC32();
        crc.update(data, 0, bodyLength);
        try (DataInputStream checksum = new DataInputStream(new ByteArrayInputStream(data, bodyLength, Integer.BYTES))) {
            if (crc.getValue() != Integer.toUnsignedLong(checksum.readInt())) throw new IOException("Somme de contrôle invalide");
        }
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data, 0, bodyLength))) {
            if (in.readInt() != MAGIC) throw new IOException("Format de carnet inconnu");
            int version = in.readInt();
            if (version != VERSION) throw new IOException("Version de carnet non prise en charge : " + version);
            PityState pity = new PityState(in.readInt(), in.readInt(), in.readInt());
            int count = in.readInt();
            if (count < 0 || count > MAX_ENTRIES) throw new IOException("Nombre de pouvoirs invalide");
            Map<String, PowerProgress> owned = new HashMap<>();
            for (int i = 0; i < count; i++) {
                String id = in.readUTF();
                PowerCatalog.validateId(id);
                PowerProgress progress = new PowerProgress(in.readInt(), in.readInt());
                if (owned.putIfAbsent(id, progress) != null) throw new IOException("Pouvoir dupliqué : " + id);
            }
            int equippedCount = in.readInt();
            if (equippedCount < 0 || equippedCount > NenState.MAX_EQUIPPED) throw new IOException("Nombre d'emplacements invalide");
            ArrayList<String> equipped = new ArrayList<>(equippedCount);
            for (int i = 0; i < equippedCount; i++) equipped.add(in.readUTF());
            int pendingFlag = in.readUnsignedByte();
            NenState.Pending pending;
            if (pendingFlag == 0) pending = null;
            else if (pendingFlag == 1) pending = new NenState.Pending(in.readUTF(), Rarity.valueOf(in.readUTF()));
            else throw new IOException("Indicateur d'offre invalide");
            if (in.available() != 0) throw new IOException("Données surnuméraires");
            return new NenState(pity, owned, equipped, pending);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IOException("État de carnet invalide", e);
        }
    }
}
