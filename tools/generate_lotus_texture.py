#!/usr/bin/env python3
"""Texture originale déterministe 16×16, sans dépendance d'image externe."""
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/main/resources/assets/cambocraft/textures/block/lotus_stone.png"


def chunk(kind, data):
    return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xffffffff)


def image():
    petals = {
        (7, 3), (8, 3), (7, 4), (8, 4), (6, 5), (7, 5), (8, 5), (9, 5),
        (4, 5), (11, 5), (4, 6), (5, 6), (6, 6), (7, 6), (8, 6), (9, 6), (10, 6), (11, 6),
        (4, 7), (5, 7), (6, 7), (7, 7), (8, 7), (9, 7), (10, 7), (11, 7),
        (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8),
        (6, 9), (7, 9), (8, 9), (9, 9),
    }
    rows = []
    for y in range(16):
        row = bytearray([0])
        for x in range(16):
            n = ((x * 13 + y * 7 + x * y * 3) % 13) - 6
            color = (188 + n, 158 + n, 119 + n, 255)
            if x in (0, 15) or y in (0, 15):
                color = (150 + n, 120 + n, 87 + n, 255)
            if (x, y) in petals:
                color = (218 + n, 139 + n, 156 + n, 255)
            if (x, y) in {(7, 7), (8, 7), (7, 8), (8, 8)}:
                color = (225, 188, 102, 255)
            row.extend(color)
        rows.append(bytes(row))
    ihdr = struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0)
    return b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(b"".join(rows), 9)) + chunk(b"IEND", b"")


if __name__ == "__main__":
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_bytes(image())
