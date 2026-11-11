# Journal des changements

## 2026-11-11 — Socle expérimental
- Initialisation de la configuration Fabric et du point d'entrée commun.
- Compilation et lancement non vérifiés : dépôts externes inaccessibles et pas de JDK complet dans l'environnement.

## 2026-11-11 — Simulation du gacha
- Modèle autonome des cinq raretés et des garanties ; tests des seuils et des remises à zéro.
- Simulation reproductible de 5 millions de tirages avec et sans pity, résultats archivés.
- Aucun gacha jouable ou système de sauvegarde n'est intégré.

## 2026-11-11 — Conception et feuille de route
- Recherche initiale sourcée, plan technique, limites et propositions de palette et de recettes.
- Tableau d'avancement restant, dépendances et estimations P0/P1/P2 ; aucun contenu associé n'est revendiqué comme livré.

## 2026-11-11 — Revue de la simulation et état de livraison
- Contrôle exhaustif des 10 000 tickets et rejet des types non entiers.
- Tableau de suivi des critères de livraison ; brouillons de guide et de page publique explicitement non publiables.

## 2026-11-11 — Cœur de rareté P0
- Implémentation Java autonome des probabilités et des garanties avec état immuable validé.
- Tests Java et simulation de cinq millions de tirages par scénario sur le code Java lui-même ; aucune intégration en jeu ni persistance.

## 2026-11-11 — Modèle du carnet P0
- Catalogue immuable par rareté et transitions d'offre/choix ; conservation de la collection, équipement de trois pouvoirs, doublons et seuils de maîtrise paramétrés.
- Tests de limites, copie défensive, catalogue modifié et état d'offre en attente. Pas d'interface, contenu jouable, sauvegarde Minecraft ni réseau.

## 2026-11-11 — Encodage du carnet
- Codec autonome versionné avec CRC32, bornes défensives et tests de sauvegardes invalides.
- Ce codec n'est pas branché aux sauvegardes Minecraft ; pas de migration réelle ni de persistance durable, et une limite technique à résoudre avant livraison.

## 2026-11-11 — Modèle du cycle saisonnier P0
- Horloge Java pure : trois saisons configurables et niveau cible du lac continu, avec tests de transitions et de bornes.
- Aucune modification du monde, météo, rizière ou persistance Fabric : le cycle n'est pas visible en jeu.

## 2026-11-11 — Déclaration initiale de dimension
- Ressources expérimentales pour `cambocraft:cambodia` et son type, avec terrain et biome vanilla provisoires ; contrôles statiques Python.
- Pas de lancement Minecraft, portail, biomes personnalisés ou génération finale : la dimension n'est pas jouable normalement.

## 2026-11-11 — Première pierre et build CI
- Premier bloc Pierre du Lotus enregistré, traduit et muni d'une texture originale, d'un loot et d'une recette de test provisoire.
- Build Fabric de la révision `7865626` réussi en CI GitHub ; la vérification de la révision incluant le bloc reste à faire. Aucun lancement en jeu ni portail.

## 2026-11-11 — GameTests
- Début des GameTests Fabric séparés du jar distribué. Premier lancement : échec du test exigeant une dimension complète dans le serveur GameTest ; ce serveur ne crée pas les mondes personnalisés. Test réorienté vers le type et la ressource, sans prétendre tester le monde complet.

## 2026-11-11 — Persistance Fabric du carnet
- Attachment Nen côté serveur, persistant et copié à la mort, avec conservation des octets de versions métier inconnues.
- GameTests d'aller-retour NBT sur entité et d'archive invalide ; pas encore de test de cycle de vie joueur ou de transaction avec un bol.

## 2026-11-11 — Horloge serveur saisonnière
- Horloge sur l'Overworld, attachment persistant et notifications traduites dans la dimension ; durée configurable au démarrage.
- Météo, niveau d'eau, riz et cycle de vie après redémarrage ne sont pas encore validés ; la CI de cette révision reste à exécuter.

## 2026-11-11 — Définitions des zones et progression expérimentale
- Douze définitions JSON bilingues, chargement par datapack et validation du chaînage ; calcul d'XP et des conditions d'accès dans un modèle pur.
- Ces données ne créent ni zone, ni boss, ni restrictions réelles. Le rechargement avec erreur et le cycle joueur n'ont pas encore été testés.
