# Technique — état actuel

Le projet contient un point d'entrée Fabric sans contenu jouable. Versions dans `gradle.properties`; Java 21 requis. `README.md` donne les commandes de construction et de lancement. Aucun réseau, stockage joueur, chargeur de données, datagen ou GameTest n'est implémenté. L'architecture cible et le principe d'autorité serveur sont décrits dans `DESIGN.md`.

## Outil autonome

`python3 tools/gacha.py --draws 5000000 --seed 20261111 --output rapport.json` génère le rapport des deux simulations ; `python3 -m unittest discover -s tests -v` lance ses tests. Les tickets et l'état sont définis dans `tools/gacha.py`. Un serveur Java devra avoir sa propre implémentation testée et synchronisée avec la spécification ; Python n'est pas embarqué dans Minecraft.

Les autres outils demandés (générateurs NBT/textures/JSON/sons/docs, validateurs de données/assets/traduction, calculateur d'équilibrage, bot et GameTests) restent à créer. `ROADMAP.md` indique les dépendances et estimations.
