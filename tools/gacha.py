#!/usr/bin/env python3
"""Prototype autonome de tirage de rareté ; non intégré au mod Minecraft."""
from dataclasses import dataclass
from enum import IntEnum
import argparse
import json
import math
import random
from pathlib import Path


class Rarity(IntEnum):
    COMMUN = 0
    RARE = 1
    EPIQUE = 2
    LEGENDAIRE = 3
    MYTHIQUE = 4


# Un ticket uniforme sur 10 000 ; bornes exclusives et probabilités exactes.
WEIGHTS = (5500, 2800, 1200, 450, 50)
LIMITS = (30, 80, 250)


def base_rarity(ticket: int) -> Rarity:
    if not 0 <= ticket < 10_000:
        raise ValueError("Le ticket doit être compris entre 0 et 9999")
    if ticket < 5500:
        return Rarity.COMMUN
    if ticket < 8300:
        return Rarity.RARE
    if ticket < 9500:
        return Rarity.EPIQUE
    if ticket < 9950:
        return Rarity.LEGENDAIRE
    return Rarity.MYTHIQUE


@dataclass
class Pity:
    """Nombres de tirages consécutifs sans résultat au moins de la rareté visée."""
    epic: int = 0
    legendary: int = 0
    mythic: int = 0

    def __post_init__(self):
        for value, limit in zip((self.epic, self.legendary, self.mythic), LIMITS):
            if type(value) is not int or not 0 <= value <= limit:
                raise ValueError("Compteur pity invalide")
        # Les absences d'une rareté supérieure incluent celles des raretés inférieures.
        if not self.epic <= self.legendary <= self.mythic:
            raise ValueError("Compteurs pity incohérents")

    def draw(self, ticket: int) -> Rarity:
        result = base_rarity(ticket)
        # Au tirage suivant 30/80/250 échecs, appliquer le plancher le plus haut.
        if self.mythic >= 250:
            floor = Rarity.MYTHIQUE
        elif self.legendary >= 80:
            floor = Rarity.LEGENDAIRE
        elif self.epic >= 30:
            floor = Rarity.EPIQUE
        else:
            floor = Rarity.COMMUN
        result = max(result, floor)
        self.epic = 0 if result >= Rarity.EPIQUE else self.epic + 1
        self.legendary = 0 if result >= Rarity.LEGENDAIRE else self.legendary + 1
        self.mythic = 0 if result >= Rarity.MYTHIQUE else self.mythic + 1
        return result


def simulate(n: int, seed: int, pity: bool) -> dict:
    if n <= 0:
        raise ValueError("Le nombre de tirages doit être positif")
    rng = random.Random(seed)
    state = Pity()
    counts = [0] * 5
    max_misses = [0, 0, 0]
    # Compteurs externes vérifiant les garanties indépendamment de l'état utilisé.
    misses = [0, 0, 0]
    for _ in range(n):
        ticket = rng.randrange(10_000)
        result = state.draw(ticket) if pity else base_rarity(ticket)
        counts[result] += 1
        for i, threshold in enumerate((Rarity.EPIQUE, Rarity.LEGENDAIRE, Rarity.MYTHIQUE)):
            misses[i] = 0 if result >= threshold else misses[i] + 1
            max_misses[i] = max(max_misses[i], misses[i])
        if pity and any(misses[i] > LIMITS[i] for i in range(3)):
            raise AssertionError("Une garantie pity a été dépassée")
    return {
        "draws": n, "seed": seed, "pity": pity,
        "counts": {r.name.lower(): counts[r] for r in Rarity},
        "rates": {r.name.lower(): counts[r] / n for r in Rarity},
        "max_consecutive_misses": dict(zip(("epique", "legendaire", "mythique"), max_misses)),
    }


def check_base(result: dict) -> None:
    """Tolérance de six écarts-types binomiaux par catégorie."""
    if result["pity"]:
        raise ValueError("Le test des probabilités de base exige pity désactivé")
    n = result["draws"]
    for rarity, weight in zip(Rarity, WEIGHTS):
        p = weight / 10_000
        observed = result["counts"][rarity.name.lower()]
        if abs(observed - n * p) > 6 * math.sqrt(n * p * (1 - p)):
            raise AssertionError(f"Fréquence hors tolérance pour {rarity.name}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--draws", type=int, default=5_000_000, help="tirages par scénario")
    parser.add_argument("--seed", type=int, default=20261111)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    if args.draws <= 0:
        parser.error("--draws doit être positif")
    base = simulate(args.draws, args.seed, False)
    check_base(base)
    with_pity = simulate(args.draws, args.seed, True)
    report = {"version": 1, "without_pity": base, "with_pity": with_pity}
    text = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.write_text(text, encoding="utf-8")
    print(text, end="")


if __name__ == "__main__":
    main()
