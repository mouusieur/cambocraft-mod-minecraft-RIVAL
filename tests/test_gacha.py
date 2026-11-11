import unittest
from tools.gacha import Pity, Rarity, base_rarity, check_base, simulate


class GachaTest(unittest.TestCase):
    def test_ticket_boundaries(self):
        for ticket, rarity in [(0, 0), (5499, 0), (5500, 1), (8299, 1),
                               (8300, 2), (9499, 2), (9500, 3),
                               (9949, 3), (9950, 4), (9999, 4)]:
            self.assertEqual(base_rarity(ticket), rarity)
        for ticket in (-1, 10000, 1.5, True, "1"):
            with self.assertRaises(ValueError):
                base_rarity(ticket)

    def test_full_ticket_distribution(self):
        from collections import Counter
        from tools.gacha import WEIGHTS
        counts = Counter(base_rarity(ticket) for ticket in range(10_000))
        self.assertEqual([counts[rarity] for rarity in Rarity], list(WEIGHTS))

    def test_each_guarantee_after_exact_misses(self):
        for state, expected in [(Pity(30, 30, 30), Rarity.EPIQUE),
                                (Pity(0, 80, 80), Rarity.LEGENDAIRE),
                                (Pity(0, 0, 250), Rarity.MYTHIQUE)]:
            self.assertEqual(state.draw(0), expected)
        state = Pity(29, 29, 29)
        self.assertEqual(state.draw(0), Rarity.COMMUN)
        self.assertEqual(state.draw(0), Rarity.EPIQUE)

    def test_priority_natural_upgrade_and_resets(self):
        state = Pity(30, 80, 250)
        self.assertEqual(state.draw(0), Rarity.MYTHIQUE)
        self.assertEqual(state, Pity())
        state = Pity(30, 40, 60)
        self.assertEqual(state.draw(9950), Rarity.MYTHIQUE)
        self.assertEqual(state, Pity())
        state = Pity(0, 79, 240)
        self.assertEqual(state.draw(9500), Rarity.LEGENDAIRE)
        self.assertEqual(state, Pity(0, 0, 241))

    def test_invalid_states(self):
        for values in [(-1, 0, 0), (31, 31, 31), (0, 81, 81),
                       (0, 0, 251), (1, 0, 1), (0, 1, 0), (True, 0, 0)]:
            with self.assertRaises(ValueError):
                Pity(*values)
        with self.assertRaises(ValueError):
            simulate(0, 1, True)

    def test_reproducibility_and_simulation(self):
        a = simulate(100_000, 123, False)
        self.assertEqual(a, simulate(100_000, 123, False))
        check_base(a)
        b = simulate(100_000, 123, True)
        self.assertEqual(sum(b["counts"].values()), 100_000)
        for actual, limit in zip(b["max_consecutive_misses"].values(), (30, 80, 250)):
            self.assertLessEqual(actual, limit)
        with self.assertRaises(ValueError):
            check_base(b)


if __name__ == '__main__':
    unittest.main()
