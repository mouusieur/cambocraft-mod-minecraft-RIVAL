package org.cambocraft.power;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

public final class NenStateCodecTest {
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    @FunctionalInterface private interface IoRunnable { void run() throws IOException; }
    private static void invalid(IoRunnable action) throws IOException {
        try { action.run(); } catch (IOException expected) { return; }
        throw new AssertionError("IOException attendue");
    }
    // Recalculer le CRC pour tester les validations sémantiques, pas seulement le checksum.
    private static byte[] fixCrc(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data, 0, data.length - 4);
        int value = (int) crc.getValue();
        for (int i = 0; i < 4; i++) data[data.length - 4 + i] = (byte) (value >>> (24 - i * 8));
        return data;
    }
    private static byte[] withOwnedDuplicate() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(buffer)) {
            out.writeInt(0x43424e4e); out.writeInt(1);
            out.writeInt(0); out.writeInt(0); out.writeInt(0); out.writeInt(2);
            for (int i = 0; i < 2; i++) { out.writeUTF("cambocraft:a"); out.writeInt(1); out.writeInt(0); }
            out.writeInt(0); out.writeByte(0); out.writeInt(0);
        }
        return fixCrc(buffer.toByteArray());
    }
    public static void main(String[] args) throws Exception {
        NenState state = new NenState(new PityState(3, 50, 220),
                Map.of("cambocraft:feu", new PowerProgress(5, 300), "cambocraft:eau", new PowerProgress(2, 4)),
                List.of("cambocraft:eau", "cambocraft:feu"), new NenState.Pending("cambocraft:ombre", Rarity.MYTHIQUE));
        byte[] data = NenStateCodec.encode(state);
        check(NenStateCodec.decode(data).equals(state), "Aller-retour complet");
        check(NenStateCodec.decode(NenStateCodec.encode(NenState.initial())).equals(NenState.initial()), "État vide");
        NenState reordered = new NenState(state.pity(), Map.of("cambocraft:eau", new PowerProgress(2, 4),
                "cambocraft:feu", new PowerProgress(5, 300)), state.equipped(), state.pending());
        check(Arrays.equals(data, NenStateCodec.encode(reordered)), "Encodage déterministe");
        byte[] damaged = data.clone(); damaged[10] ^= 1;
        invalid(() -> NenStateCodec.decode(damaged));
        invalid(() -> NenStateCodec.decode(Arrays.copyOf(data, data.length - 1)));
        invalid(() -> NenStateCodec.decode(new byte[0]));
        invalid(() -> NenStateCodec.decode(new byte[NenStateCodec.MAX_BYTES + 1]));
        byte[] version = data.clone(); version[7] = 2; fixCrc(version);
        invalid(() -> NenStateCodec.decode(version));
        byte[] magic = data.clone(); magic[0] ^= 1; fixCrc(magic);
        invalid(() -> NenStateCodec.decode(magic));
        byte[] pity = data.clone(); pity[8] = -1; fixCrc(pity);
        invalid(() -> NenStateCodec.decode(pity));
        byte[] count = data.clone(); count[20] = 127; fixCrc(count);
        invalid(() -> NenStateCodec.decode(count));
        invalid(() -> NenStateCodec.decode(withOwnedDuplicate()));
        byte[] trailing = Arrays.copyOf(data, data.length + 1); fixCrc(trailing);
        invalid(() -> NenStateCodec.decode(trailing));
        System.out.println("NenStateCodecTest : OK");
    }
}
