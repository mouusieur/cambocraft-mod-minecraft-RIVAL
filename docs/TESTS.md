# Vérifications

## Exécutées le 2026-11-11

- `python3 -m unittest discover -s tests -v` : **5 tests réussis**, sur CPython 3.11.2. Vérification des bornes de tickets, compteurs et garanties, validation d'états invalides, reproductibilité et comparaison statistique.
- `python3 tools/gacha.py --draws 5000000 --seed 20261111 --output docs/simulation-gacha.json` : **réussi** pour 5 000 000 tirages sans pity et 5 000 000 avec pity ; bornes vérifiées et fréquences de base dans la tolérance de six écarts-types. Voir `PROBABILITES.md` et le JSON.
- `JAVA_HOME=/tmp/cambocraft-jdk/jdk4py/java-runtime ./gradlew build` : **échec avant configuration**, téléchargement de Gradle impossible (`SSLHandshakeException` vers services.gradle.org). Le runtime temporaire n'est pas un JDK : `javac` manque. Les accès à maven.fabricmc.net et Maven Central échouent aussi depuis cet environnement. Le build, les versions entre elles et le mod n'ont pas pu être vérifiés.

## Non réalisés

Client et serveur, GameTests, test du portail, intégration des systèmes en jeu, bot de progression, mesures à 1/4/8 joueurs et bêta interne. Il n'y a aucune preuve de fonctionnement ou de performance en jeu.
