"""Vérifie les données déclarées, pas l'existence physique des zones ou boss."""
import json
import pathlib
import re
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1] / "src/main/resources/data/cambocraft/zones"


class ZoneDataTest(unittest.TestCase):
    def test_zone_chain(self):
        files = sorted(ROOT.glob("*.json"))
        self.assertEqual(len(files), 12)
        zones = sorted((json.loads(p.read_text(encoding="utf-8")) for p in files), key=lambda x: x["order"])
        ids, bosses = set(), set()
        previous, last_level = None, 0
        for order, zone in enumerate(zones, 1):
            self.assertEqual(zone["order"], order)
            self.assertRegex(zone["id"], r"^cambocraft:[a-z0-9_]+$")
            self.assertTrue((ROOT / (zone["id"].split(":")[1] + ".json")).exists())
            self.assertNotIn(zone["id"], ids)
            self.assertNotIn(zone["boss"], bosses)
            ids.add(zone["id"]); bosses.add(zone["boss"])
            self.assertEqual(zone["previous_boss"], previous)
            previous = zone["boss"]
            self.assertEqual(zone["requires_rebirth"], order == 12)
            self.assertTrue(1 <= zone["minimum_level"] <= 200)
            if order < 12:
                self.assertGreaterEqual(zone["minimum_level"], last_level)
                last_level = zone["minimum_level"]
            for field in ("name", "boss_name"):
                self.assertTrue(zone[field]["fr"].strip())
                self.assertTrue(zone[field]["en"].strip())


if __name__ == "__main__":
    unittest.main()
