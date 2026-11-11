package org.cambocraft.power;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/** Enveloppe immuable qui conserve aussi les versions ou octets encore illisibles. */
public final class NenArchive {
    private final byte[] bytes;

    private NenArchive(byte[] bytes) { this.bytes = bytes.clone(); }

    public static NenArchive ofState(NenState state) throws IOException {
        return new NenArchive(NenStateCodec.encode(state));
    }

    public static NenArchive fromBuffer(ByteBuffer buffer) {
        ByteBuffer copy = Objects.requireNonNull(buffer, "buffer").slice();
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return new NenArchive(bytes);
    }

    public ByteBuffer toBuffer() { return ByteBuffer.wrap(bytes.clone()); }
    public NenState read() throws IOException { return NenStateCodec.decode(bytes); }
}
