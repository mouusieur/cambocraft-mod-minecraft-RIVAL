"""Contrôles statiques des ressources : ne remplacent pas un chargement Minecraft."""
import json
import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data/cambocraft"


class DimensionDataTest(unittest.TestCase):
    def test_dimension_reference_and_generator(self):
        dimension = json.loads((DATA / "dimension/cambodia.json").read_text(encoding="utf-8"))
        self.assertEqual(dimension["type"], "cambocraft:cambodia")
        self.assertTrue((DATA / "dimension_type/cambodia.json").is_file())
        generator = dimension["generator"]
        self.assertEqual(generator["type"], "minecraft:noise")
        self.assertEqual(generator["settings"], "minecraft:overworld")
        self.assertEqual(generator["biome_source"], {"type": "minecraft:fixed", "biome": "minecraft:jungle"})

    def test_dimension_type_geometry_and_safety(self):
        dimension_type = json.loads((DATA / "dimension_type/cambodia.json").read_text(encoding="utf-8"))
        self.assertEqual(dimension_type["min_y"], -64)
        self.assertEqual(dimension_type["height"], 384)
        self.assertEqual(dimension_type["logical_height"], 384)
        self.assertEqual(dimension_type["min_y"] % 16, 0)
        self.assertEqual(dimension_type["height"] % 16, 0)
        self.assertEqual(dimension_type["coordinate_scale"], 1)
        self.assertTrue(dimension_type["has_skylight"])
        self.assertTrue(dimension_type["bed_works"])
        self.assertFalse(dimension_type["ultrawarm"])
        self.assertEqual(dimension_type["effects"], "minecraft:overworld")
        self.assertEqual(dimension_type["infiniburn"], "#minecraft:infiniburn_overworld")
        self.assertEqual(dimension_type["monster_spawn_light_level"], {
            "type": "minecraft:uniform", "min_inclusive": 0, "max_inclusive": 7,
        })
        self.assertEqual(dimension_type["monster_spawn_block_light_limit"], 0)
        self.assertGreaterEqual(dimension_type["ambient_light"], 0)
        self.assertLessEqual(dimension_type["ambient_light"], 1)
        for name in ("has_ceiling", "has_raids", "natural", "piglin_safe", "respawn_anchor_works"):
            self.assertIsInstance(dimension_type[name], bool)


if __name__ == "__main__":
    unittest.main()
