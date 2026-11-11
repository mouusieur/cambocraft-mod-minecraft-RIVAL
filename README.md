# Cambocraft

**État : fondations expérimentales, pas un mod jouable.** Aucun portail, dimension ou système de progression n'est encore intégré. Ne pas installer dans un monde auquel vous tenez.

## Développement

Prérequis : JDK 21, Python 3 et accès aux dépôts Fabric, Maven Central, Gradle et Minecraft. Depuis la racine :

```sh
./gradlew build
./gradlew runClient   # client de développement
./gradlew runServer   # serveur de développement séparé
python3 -m unittest discover -s tests -v
```

Le jar remappé est attendu dans `build/libs/` après une compilation réussie. Le socle n'a pas encore été compilé dans cet environnement. Pour une future installation, utiliser Minecraft 1.21.1 avec Fabric Loader et l'API Fabric indiqués dans `gradle.properties`, puis placer le jar et l'API Fabric dans `mods/`. Ce n'est pas une version distribuable aujourd'hui.

## Dépendances

Minecraft 1.21.1 (jeu), Fabric Loader (chargement), Fabric API (points d'intégration), Loom (construction/remappage), Yarn (noms de développement), Gradle (construction), Java 21 (exécution) ; versions exactes dans `gradle.properties` et le wrapper. Aucune dépendance d'animation à ce stade. Fabric Loader/API doivent être installés côté jeu. Aucun écran d'options n'est inclus.

Voir `docs/DECISIONS.md`, `docs/TESTS.md` et `CREDITS.md` pour les limites, vérifications et sources.

## Simulation expérimentale du gacha

`python3 tools/gacha.py --draws 5000000 --seed 20261111` exécute chaque scénario (avec et sans pity). La simulation et les conventions de garantie sont expliquées dans `docs/PROBABILITES.md`. Elle est indépendante du mod : il n'y a pas encore de gacha jouable.

Un cœur Java de rareté/garantie est également présent mais **pas raccordé au jeu**. Avec un JDK 21, exécuter `tools/test_java.sh --simulate` pour le compiler, le tester et simuler les tirages. Voir `docs/TESTS.md`.
