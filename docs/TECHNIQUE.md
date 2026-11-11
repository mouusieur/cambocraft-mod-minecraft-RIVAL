# Technique — état actuel

Le projet contient un point d'entrée Fabric sans contenu jouable. Versions dans `gradle.properties`; Java 21 requis. `README.md` donne les commandes de construction et de lancement. Aucun réseau, stockage joueur, chargeur de données, datagen ou GameTest n'est implémenté. L'architecture cible et le principe d'autorité serveur sont décrits dans `DESIGN.md`.

## Outil autonome

`python3 tools/gacha.py --draws 5000000 --seed 20261111 --output rapport.json` génère le rapport des deux simulations ; `python3 -m unittest discover -s tests -v` lance ses tests. Les tickets et l'état sont définis dans `tools/gacha.py`. Un serveur Java devra avoir sa propre implémentation testée et synchronisée avec la spécification ; Python n'est pas embarqué dans Minecraft.

Les autres outils demandés (générateurs NBT/textures/JSON/sons/docs, validateurs de données/assets/traduction, calculateur d'équilibrage, bot et GameTests) restent à créer. `ROADMAP.md` indique les dépendances et estimations.

## Raretés Java

`Gacha.draw(ticket, pity)` est pur et retourne le résultat et le nouvel état ; `PityState` refuse les compteurs négatifs, au-delà des garanties et incohérents. Le futur serveur doit générer un ticket uniforme dans `[0, 10000)`, choisir une entrée de la rareté résultante et enregistrer l'état en même temps que la consommation du bol ; le client ne fournit jamais le ticket. Aucun de ces raccordements n'est implémenté.

Avec un JDK 21, `tools/test_java.sh --simulate` compile et teste ces seules classes Java et effectue 5 millions de tirages par scénario. Sans `--simulate`, il ne lance que les tests rapides. `ECJ_JAR` et `JAVA` permettent d'utiliser explicitement un compilateur Eclipse et un runtime externes.

## Modèle de carnet

`PowerCatalog` est un instantané immuable regroupant des identifiants uniques par rareté. `NenJournal.offer` reçoit une source de nombres aléatoires bornée ; il appelle le cœur de gacha puis sélectionne un pouvoir. `NenJournal.resolve` accepte ou rejette une offre, sans perdre les acquisitions précédentes ; `NenState.equip` valide jusqu'à trois pouvoirs possédés distincts. `PowerProgress` conserve exemplaires et maîtrise, mais les seuils de maîtrise sont à définir et équilibrer. L'absence d'offre en attente est requise avant tout nouveau tirage.

Ces types constituent un contrat de domaine sans catalogue JSON, synchronisation, UI, bol ni transaction de gameplay ; leur encodage est raccordé à un attachment Fabric expérimental décrit ci-dessous. Le futur adaptateur serveur doit sérialiser les accès concurrents au joueur et enregistrer consommation, offre et compteurs atomiquement, gérer rechargement et migration, et valider toutes les requêtes. Ne pas accepter une RNG, une offre ou une progression venant du client.

## Codec autonome du carnet

`NenStateCodec.encode/decode` utilise un format binaire versionné (magic `CBNN`, version 1, pity, collection, emplacements, offre, CRC32). Les identifiants sont triés à l'encodage et les noms des raretés sont stockés explicitement. `decode` contrôle les longueurs et invariants et refuse tout fichier d'une version inconnue ; il ne tente aucune migration automatique. La somme de contrôle n'est pas une protection contre un attaquant.

L'état de domaine n'a pas de limite logique de collection, mais le codec actuel refuse plus de 100 000 entrées ou 32 Mio : limite de sécurité **incompatible avec la promesse d'inventaire illimité à grande échelle**, à résoudre avant livraison. Ce codec n'écrit aucun fichier lui-même ; un attachment Fabric l'emploie désormais. La sauvegarde transactionnelle, la migration et le cycle de vie complet du joueur restent à implémenter et tester.

## Modèle de saisons

`SeasonClock.sample` calcule à partir d'un âge de monde monotone une saison, un jour, un tick et une fraction de niveau du lac. La durée est configurable au constructeur (huit jours par défaut). `lakeHeight(min,max)` arrondit cette fraction entre deux niveaux donnés ; il ne touche pas au monde. Le futur serveur devra fournir un compteur persistant (pas `timeOfDay`), synchroniser l'affichage et planifier toute modification de terrain avec un budget et une protection des constructions. Aucun de ces raccordements n'existe encore.

## Déclaration de dimension expérimentale

Les ressources `data/cambocraft/dimension/cambodia.json` et `dimension_type/cambodia.json` déclarent respectivement la dimension et son type. La génération emploie provisoirement `minecraft:overworld` et un biome fixe `minecraft:jungle` ; il n'existe ni portail ni bruit ou biomes personnalisés. Les tests Python valident quelques invariants statiques mais ne vérifient pas la désérialisation ou le chargement Minecraft. **Non prêt pour un monde de production.**

## Pierre du Lotus expérimentale

`ModBlocks` enregistre le bloc et son `BlockItem` sur les deux environnements, ainsi qu'une entrée créative. Ressources sous `assets/cambocraft/` et `data/cambocraft/` ; texture régénérable avec `python3 tools/generate_lotus_texture.py`. Une recette de test fabrique quatre pierres à partir de quatre grès et d'une teinture rose. Elle est provisoire, ne remplace pas la quête et ne crée pas de portail.

## Attachment Fabric du carnet

`ModAttachments.NEN` est persistant, copié à la mort et non synchronisé. `PlayerNenStorage` ne prend qu'un joueur serveur. Il décode les octets avant chaque lecture et refuse d'écraser une archive existante illisible. Le codec Fabric ne décode pas l'état métier : il conserve les octets inconnus pour éviter une réinitialisation silencieuse. Les GameTests couvrent l'encodage/décodage NBT sur une entité et la conservation d'octets invalides, pas encore le cycle de vie d'un vrai joueur.

L'attachment n'est utilisé par aucun tirage en jeu. La sauvegarde en mémoire, la consommation du bol et les compteurs ne sont pas transactionnels ; Fabric peut ignorer des données si le NBT est invalide avant le codec métier. Migration, sauvegardes de secours et tests de redémarrage sont indispensables. Les limites du codec sont toujours applicables.

## Horloge serveur expérimentale

`SeasonRuntime` avance un attachment `season_ticks` sur l'Overworld une fois par tick serveur et annonce les transitions aux joueurs présents dans `cambocraft:cambodia`. À la connexion dans cette dimension, la saison est annoncée. `-Dcambocraft.seasonDays=8` est le réglage par défaut (1 à 365) ; un changement reclassifie le compteur existant et n'est pas une migration de durée. Des données négatives ou saturées arrêtent l'horloge au lieu de les réinitialiser.

Le calcul ne modifie pas encore l'eau, la pluie, les cultures ou les constructions et n'offre pas de HUD. Vérifier redémarrage, changement de dimension, multi-joueurs et performances avant usage réel.

## Zones expérimentales et progression

Les douze fichiers `data/cambocraft/zones/*.json` sont des définitions bilingues rechargeables ; leur identifiant doit correspondre au chemin. Le chargeur exige exactement douze fichiers, une chaîne ordonnée de boss distincts, des niveaux valides et le drapeau de renaissance uniquement pour la dernière zone. Chaque fichier est limité à 16 384 caractères. Le candidat est préparé hors thread de jeu et la publication attend la réussite du rechargement global. Ce comportement avec des échecs de datapacks tiers n'a pas été vérifié en jeu ; aucun réseau ne synchronise encore ces données aux clients.

`PlayerLevel` applique la formule provisoire `ceil(120 × niveau^1,8)` (niveau maximal 200) mais n'est relié ni à des joueurs, ni à des récompenses. `ZoneCatalog.access` est une fonction pure : le monde ne bloque pas encore les accès et aucune victoire n'est persistée. Les boss référencés n'existent pas en tant qu'entités. Les données ne sont pas équilibrées.

`SafePortalLanding` teste un sol plein, non dangereux et sec ainsi que deux blocs d'air, puis peut chercher une surface existante dans un rayon de huit blocs. La recherche est bornée et ne construit rien, mais peut charger des chunks, ne protège pas des monstres et n'est pas reliée à une téléportation.

## Portail de test

En Overworld ou en Cambocraft, avec un bol vanilla en main principale, utiliser la pierre inférieure centrale d'un cadre vertical 3×4 vide : le serveur vérifie les conditions puis cherche une surface existante dans l'autre dimension près des mêmes coordonnées X/Z. Le bol n'est pas consommé et une temporisation de 100 ticks s'applique après succès. Le retour demande de construire un autre cadre, ne mémorise pas le cadre d'origine, et peut échouer faute de sol convenable. Pas de génération naturelle ni d'aspect portail ; arrivée exposée aux monstres et voyager avec un véhicule ou des passagers est refusé. À tester avec de vrais clients avant toute utilisation hors monde jetable.
