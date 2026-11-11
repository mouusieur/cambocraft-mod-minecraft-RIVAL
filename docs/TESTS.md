# Vérifications

## Exécutées le 2026-11-11

- `python3 -m unittest discover -s tests -v` : **5 tests réussis**, sur CPython 3.11.2. Vérification des bornes de tickets, compteurs et garanties, validation d'états invalides, reproductibilité et comparaison statistique.
- `python3 tools/gacha.py --draws 5000000 --seed 20261111 --output docs/simulation-gacha.json` : **réussi** pour 5 000 000 tirages sans pity et 5 000 000 avec pity ; bornes vérifiées et fréquences de base dans la tolérance de six écarts-types. Voir `PROBABILITES.md` et le JSON.
- `JAVA_HOME=/tmp/cambocraft-jdk/jdk4py/java-runtime ./gradlew build` : **échec avant configuration**, téléchargement de Gradle impossible (`SSLHandshakeException` vers services.gradle.org). Le runtime temporaire n'est pas un JDK : `javac` manque. Les accès à maven.fabricmc.net et Maven Central échouent aussi depuis cet environnement. Le build, les versions entre elles et le mod n'ont pas pu être vérifiés.

## Non réalisés

Client et serveur, GameTests, test du portail, intégration des systèmes en jeu, bot de progression, mesures à 1/4/8 joueurs et bêta interne. Il n'y a aucune preuve de fonctionnement ou de performance en jeu.

## État des critères de la section 18

| Critère | État au 2026-11-11 |
|---|---|
| `./gradlew build` | Réussi en CI sur `7865626` ; téléchargement bloqué localement, mod non livrable |
| Dimension et portail naturels | Déclaration de dimension expérimentale ajoutée ; chargement non testé et portail absent |
| Saisons, lac, riz | Non implémentés |
| 12 zones et boss | Non implémentés |
| 80 pouvoirs jouables et persistants | Non implémentés |
| Gacha intégré et simulation | Simulation autonome exécutée ; intégration et persistance absentes, critère global non atteint |
| Synergies et maîtrise | Non implémentées |
| Ninjas | Non implémentés |
| Quête principale et trois issues | Non implémentées |
| Réputation | Non implémentée |
| Cuisine, pêche, soie, sculpture | Non implémentées |
| Compagnon, monture, pirogue | Non implémentés |
| Événements saisonniers et Nuit des Ombres | Non implémentés |
| Pradal serey et course | Non implémentés |
| Renaissance et Royaume des Esprits | Non implémentés |
| Tous les outils de 15.3 | Seul le simulateur existe ; reste à créer |
| Validateur sans erreurs | Pas de validateur |
| Tests et bot de playtest | Tests Python du simulateur réussis ; pas de GameTest ni de bot |
| Traductions fr/en | Non implémentées |
| Performances 1/4/8 joueurs | Non mesurées |
| Accessibilité | Non implémentée |
| Documentation de livraison complète | Incomplète ; documents de conception et de statut uniquement |
| P2 non livrées estimées | Listées avec dépendances dans `ROADMAP.md` |

La suite de tests a été étendue à six tests après la simulation : le balayage intégral des 10 000 tickets vérifie notamment la correspondance entre les bornes et les poids publiés.

## Suite P0 — cœur Java autonome, 2026-11-11

- `tools/test_java.sh --simulate` exécuté avec le runtime OpenJDK 21.0.8 de `jdk4py` et le compilateur Eclipse 3.46.100 du paquet npm `coc-java` : compilation Java 21 réussie, `GachaTest : OK`, puis 5 000 000 de tirages sans et 5 000 000 avec pity. Voir `simulation-gacha-java.txt`.
- Les 10 000 tickets, les états invalides, les garanties, la priorité et les remises à zéro sont testés ; chaque scénario de simulation vérifie respectivement les écarts statistiques ou les bornes de pity.
- `python3 -m unittest discover -s tests -v` : six tests Python réussis.
- **Ce n'est pas `./gradlew build`** : le compilateur autonome ne teste ni Minecraft, ni Fabric, ni leur intégration. Aucun test en jeu n'a été exécuté.

## Suite P0 — modèle autonome de carnet

- `tools/test_java.sh --simulate` avec Eclipse Compiler et JRE 21 : compilation des classes de domaine et des deux suites `GachaTest` et `NenJournalTest` réussie ; simulation Java reproductible de 5 millions de tirages par scénario également réussie.
- Vérifiés : validation du catalogue, tri des candidats, bornes de la RNG, verrou d'offre, acceptation/refus, doublons, plafonnement, seuils de maîtrise, trois emplacements, copies défensives et refus d'une offre rendue invalide par un changement de catalogue.
- Tests Python : six réussis. Aucun test Fabric, de sauvegarde Minecraft ou de réseau : le carnet n'est pas en jeu. Seuils de maîtrise utilisés dans les tests (100 et 250) arbitraires et non équilibrés.

## Suite P0 — codec de carnet

- `tools/test_java.sh --simulate` avec Eclipse Compiler et JRE 21 : les trois suites Java réussissent et les deux simulations de 5 millions de tirages passent.
- Codec : aller-retour d'état complet et vide, ordre déterministe, détection de corruption, fichier tronqué, taille excessive, version inconnue, format incorrect, compteur invalide, nombre excessif d'entrées, doublons et données surnuméraires.
- La sérialisation testée est en mémoire seulement ; pas de test de redémarrage d'un serveur, ni de branchement Minecraft, ni d'écriture atomique ou de migration de données existantes.

## Suite P0 — horloge saisonnière

- `tools/test_java.sh --simulate` : quatre suites Java réussies, dont `SeasonClockTest` ; compilation Eclipse en mode Java 21. Les tests couvrent durée par défaut et configurable, jours et transitions, continuité de la crue, niveaux et bornes, entrées invalides et très grands temps.
- Le modèle est purement numérique : pas de test Minecraft, modification de blocs, météo, temps persistant ou cycle du riz. Les critères de saisons et de Grand Lac restent non atteints.

## Suite P0 — ressources de dimension

- Huit tests Python réussis, dont deux contrôles statiques des JSON de dimension et du type (identifiants, géométrie, éclairage et paramètres de génération attendus).
- Quatre suites Java autonomes réussies. Le schéma des champs a été comparé aux données `1.21.1-data-json` de mcmeta ; ce n'est pas un test des codecs de Minecraft.
- `./gradlew build` reste bloqué au téléchargement de Gradle ; le chargement réel de la dimension, les biomes et les portails ne sont pas validés.

## Build distant et pierre du Lotus

- [CI GitHub n° 43106595371](https://github.com/mouusieur/cambocraft-mod-minecraft-RIVAL/actions/runs/43106595371) : `./gradlew build`, simulation Java et tests Python **réussis** sur le commit `7865626`. Ce build précède l'ajout de la pierre ; voir le prochain résultat CI pour le nouveau code.
- Dix tests Python réussis localement après ajout de la pierre : deux nouveaux contrôles statiques du modèle, de la texture, des traductions, de la recette et du loot. Quatre suites Java autonomes réussies.
- Aucune inspection visuelle, aucun lancement client ou serveur et aucun test en jeu. Les recettes et tables de loot n'ont pas été validées par Minecraft.

## Premier essai de GameTest

- Le build Fabric de [la révision `81f5cd7`](https://github.com/mouusieur/cambocraft-mod-minecraft-RIVAL/actions/runs/43107039443) a réussi et les jars ont été publiés comme artefact CI. Le bloc n'avait pas encore été testé dans Minecraft à cette révision.
- Un premier serveur GameTest a démarré en CI et deux tests ont été exécutés, mais celui qui exigeait un monde personnalisé a échoué. Ce serveur spécial ne crée que les trois dimensions vanilla ; le test est remplacé par une vérification plus limitée du type et de la présence de la ressource. La création de `cambocraft:cambodia` sur un serveur ordinaire reste à tester. La réussite du nouveau test ne doit pas être supposée avant exécution CI.

## GameTests et attachment Nen

- [CI n° 43108213003](https://github.com/mouusieur/cambocraft-mod-minecraft-RIVAL/actions/runs/43108213003) : build et deux GameTests réussis sur `48926b9` : enregistrement/placement de la pierre et chargement du type de dimension/présence de la ressource. Le serveur GameTest ne crée pas la dimension personnalisée ; ce test ne valide pas le voyage.
- [CI n° 43108547322](https://github.com/mouusieur/cambocraft-mod-minecraft-RIVAL/actions/runs/43108547322) : build, quatre GameTests et tests autonomes réussis sur `d500176`. Les deux nouveaux tests contrôlent un aller-retour NBT d'attachment sur un cochon et la conservation d'octets illisibles.
- Non testés : véritable joueur, mort et copie, déconnexion/reconnexion, redémarrage, transition de dimension et transactions de gameplay. Les flags d'attachment sont vérifiés, pas leurs effets sur le cycle de vie réel.

## Cadre de portail et horloge serveur

- [CI n° 43108991083](https://github.com/mouusieur/cambocraft-mod-minecraft-RIVAL/actions/runs/43108991083) : build et six GameTests réussis sur `015dd79`, dont deux tests de cadres 3×4 (axes, bloc manquant et intérieur obstrué). Le validateur de cadre n'est relié à aucun bol ni téléportation.
- Un test de compteur serveur est ajouté ensuite mais son résultat CI doit être vérifié séparément ; il ne prouve pas une persistance après redémarrage ni la météo, le lac ou le riz en jeu.

- [CI n° 43109521223](https://github.com/mouusieur/cambocraft-mod-minecraft-RIVAL/actions/runs/43109521223) : build et GameTest du compteur d'horloge réussis sur `e2bac76` ; aucun redémarrage testé à ce stade.
- [CI n° 43110142041](https://github.com/mouusieur/cambocraft-mod-minecraft-RIVAL/actions/runs/43110142041) : build et test du chargement des douze zones réussis sur `86f4cb1`. Le test ne vérifie pas le comportement de `/reload` ni des boss réels. Cinq suites Java et onze tests Python réussis.
- Un nouveau test de serveur dédié et redémarrage est ajouté ensuite ; son résultat doit être vérifié sur la CI correspondante. Il n'inclut aucun joueur réel.

- [CI n° 43116490122](https://github.com/mouusieur/cambocraft-mod-minecraft-RIVAL/actions/runs/43116490122) : serveur dédié ordinaire sur `3aed312`, monde `cambocraft:cambodia` créé, douze zones chargées et compteur saisonnier restauré après arrêt propre et redémarrage. Pas de joueur, voyage, plantage brutal, ni validation de génération personnalisée.
- Un GameTest de validation du sol d'arrivée est ajouté ensuite ; résultat CI à vérifier. La recherche reste non raccordée au portail.
