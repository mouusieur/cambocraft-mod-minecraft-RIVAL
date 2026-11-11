"""Contrôles de cohérence ; ne remplacent pas un lancement de Minecraft."""
import json
import pathlib
import struct
import unittest
from tools.generate_lotus_texture import image

ROOT = pathlib.Path(__file__).resolve().parents[1] / "src/main/resources"
ASSETS = ROOT / "assets/cambocraft"
DATA = ROOT / "data"


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


class LotusStoneAssetsTest(unittest.TestCase):
    def test_block_models_and_texture(self):
        state = load(ASSETS / "blockstates/lotus_stone.json")
        model = load(ASSETS / "models/block/lotus_stone.json")
        item = load(ASSETS / "models/item/lotus_stone.json")
        self.assertEqual(state["variants"][""]["model"], "cambocraft:block/lotus_stone")
        self.assertEqual(model["parent"], "minecraft:block/cube_all")
        self.assertEqual(model["textures"]["all"], "cambocraft:block/lotus_stone")
        self.assertEqual(item["parent"], "cambocraft:block/lotus_stone")
        texture = (ASSETS / "textures/block/lotus_stone.png").read_bytes()
        self.assertEqual(texture, image())
        self.assertEqual(texture[:8], b"\x89PNG\r\n\x1a\n")
        self.assertEqual(struct.unpack(">II", texture[16:24]), (16, 16))

    def test_localization_loot_and_recipe(self):
        for lang in ("fr_fr", "en_us"):
            self.assertTrue(load(ASSETS / f"lang/{lang}.json")["block.cambocraft.lotus_stone"])
        loot = load(DATA / "cambocraft/loot_table/blocks/lotus_stone.json")
        self.assertEqual(loot["pools"][0]["entries"][0]["name"], "cambocraft:lotus_stone")
        recipe = load(DATA / "cambocraft/recipe/lotus_stone.json")
        self.assertEqual(recipe["result"], {"id": "cambocraft:lotus_stone", "count": 4})
        self.assertEqual(len(recipe["ingredients"]), 5)
        tag = load(DATA / "minecraft/tags/block/mineable/pickaxe.json")
        self.assertFalse(tag["replace"])
        self.assertIn("cambocraft:lotus_stone", tag["values"])


if __name__ == "__main__":
    unittest.main()
