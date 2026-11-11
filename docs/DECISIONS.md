# Décisions

## 2026-11-11 — Socle et limites
- **Choix :** Fabric 1.21.1, Yarn `1.21.1+build.3`, Loom 1.7.4, Loader 0.20.1, API 0.116.17+1.21.1 et Gradle 8.10.2. Versions fixées pour rendre la résolution reproductible. Le Loader et l'API suivent l'exemple Fabric de la branche 1.21.1 ; leur combinaison avec ce Loom reste à vérifier en compilation.
- **Alternatives :** mappings officiels et versions flottantes écartés pour respecter la demande Yarn et limiter les changements imprévus.
- **Animations :** aucune bibliothèque d'animation pour l'instant. Le choix GeckoLib ou animation native est reporté jusqu'au premier modèle animé et à un test d'intégration ; ne pas ajouter une dépendance invérifiée au socle.
- **Contrainte :** pas de JDK ni de Gradle installés initialement ; accès réseau refusé à services.gradle.org, maven.fabricmc.net et Maven Central. Un runtime Java 21 temporaire a été obtenu depuis PyPI, mais il ne contient pas `javac`. La compilation Fabric et les lancements restent non vérifiés.
- **Périmètre :** le dépôt était vide à l'exception du README. Ce socle ne constitue pas un mod jouable et ne valide aucun critère de jeu de la section 18.

## 2026-11-11 — Prototype du gacha
- **Choix :** « après N tirages sans » signifie garantie au tirage suivant : 31, 81 et 251 au plus tard. Une rareté supérieure remet à zéro les compteurs inférieurs. Lorsque plusieurs garanties sont dues, la plus élevée l'emporte ; un résultat naturel supérieur n'est jamais abaissé.
- **Alternative :** garantir dès le N-ième tirage a été écarté car cela signifierait après seulement N−1 échecs ; ces deux interprétations devront être annoncées explicitement aux joueurs.
- **Portée :** outil de conception autonome, non connecté au jeu. Aucun état joueur n'est sauvegardé. L'intégration serveur, les choix de relance et les pouvoirs restent P0.

## 2026-11-11 — Arbitrages de conception
- **Introduction :** un premier guide dans l'Overworld remettra les composants du portail ; une introduction exclusivement dans la dimension créerait une dépendance circulaire. Aucun accès par commande n'est envisagé comme parcours joueur.
- **Zones :** les limites communes 15, 30, etc. seront des prérequis minimaux ; une seule zone n'est pas attribuée exclusivement à chaque niveau. L'accès nécessitera aussi le boss précédent.
- **Catalogue de pouvoirs :** prévoir au moins 84 pouvoirs, plutôt que 80, si chaque pouvoir a un seul élément : sept éléments × douze pouvoirs. L'alternative de pouvoirs multi-éléments aurait aussi résolu la contrainte mais ajouterait des ambiguïtés d'équilibrage ; elle reste possible après conception détaillée.
- **Priorités contradictoires :** course de pirogues et Royaume des Esprits considérés P1 comme dans les critères de succès ; ils ne sont pas relégués au P2.
- **Culture :** les références documentaires sont un premier repérage et ne remplacent pas une consultation de personnes cambodgiennes. Aucune œuvre tierce n'est incorporée.

## 2026-11-11 — Communication de l'état
- **Choix :** conserver guide et page publique comme brouillons clairement non publiables, et suivre séparément chaque critère de la section 18.
- **Alternative écartée :** annoncer comme fonctionnalités les objectifs de conception ou les résultats d'une simulation non intégrée ; cela masquerait l'absence de jeu et de tests Minecraft.

## 2026-11-11 — Premier cœur P0 en Java
- **Choix :** séparer les règles pures de rareté et de garantie de l'adaptateur Fabric. Un état immuable validé est renvoyé avec chaque tirage ; aucune décision cliente ni état global. La simulation Java appelle directement ces règles, en plus du simulateur Python indépendant.
- **Alternative :** écrire des appels Fabric non vérifiables dans l'environnement actuel a été écarté pour ce lot ; la liaison au serveur reste indispensable, tout comme la consommation atomique des bols et la sauvegarde.
- **Test :** JRE Java 21 temporaire fourni par `jdk4py` depuis PyPI ; compilateur Eclipse 3.46.100 temporaire contenu dans le paquet npm `coc-java` 1.56.0. Ces outils de test ne sont pas inclus dans le dépôt ni requis en production ; un JDK 21 suffit normalement. La compilation Fabric complète reste bloquée.

## 2026-11-11 — Carnet de Nen : domaine sans intégration
- **Choix :** inventaire immuable de pouvoirs, trois emplacements distincts au maximum et offre en attente obligatoire avant un nouveau tirage. Accepter conserve les pouvoirs précédents ; rejeter conserve l'état pity après le tirage. Un doublon ajoute un exemplaire jusqu'à cinq, les niveaux 4 et 5 nécessitent des seuils de maîtrise fournis par la configuration future.
- **Alternatives :** supprimer l'ancien pouvoir ou permettre un deuxième tirage pendant qu'une offre est en attente ferait perdre des acquis ou faciliterait les abus ; ces comportements sont écartés. Aucune valeur de maîtrise n'est choisie comme équilibre final.
- **Rechargement :** un catalogue immuable est entièrement validé (identifiants uniques, au moins un pouvoir dans chaque rareté) avant utilisation. Une offre dont la définition a changé ne peut plus être acceptée mais peut être rejetée ; l'inventaire existant n'est pas effacé. La migration des pouvoirs supprimés ou modifiés reste à concevoir avant livraison.
- **Limite :** ce contrat ne gère pas encore le rechargement JSON, les 84 pouvoirs, la consommation des bols, la transaction de sauvegarde ni les paquets réseau. Il ne doit pas être exposé au client sans contrôles d'autorité.

## 2026-11-11 — Encodage d'état P0
- **Choix :** codec autonome binaire version 1, ordre d'écriture déterministe, noms explicites des raretés et CRC32. Les données invalides, tronquées, doublonnées et les versions inconnues sont rejetées avant de produire un état. Le CRC détecte des corruptions accidentelles, **pas** la falsification.
- **Alternative :** supposer que l'encodage constitue une sauvegarde Fabric a été écarté. Aucun branchement NBT/attachment, écriture durable atomique ou migration d'une version antérieure n'existe ; il n'y a pas encore de version antérieure du format à migrer. Une version ultérieure devra lire et migrer la version 1 explicitement et conserver une sauvegarde de secours.
- **Limite de sécurité :** 100 000 pouvoirs et 32 Mio par état encodé protègent le décodeur d'entrées excessives. Cette limite technique contredit l'inventaire théoriquement illimité demandé : elle est un compromis explicite du prototype, à remplacer par une persistance paginée ou une stratégie sûre équivalente avant validation du P0.

## 2026-11-11 — Modèle de saisons et lac P0
- **Choix :** une horloge pure à trois saisons ordonnées sèche → pluies → fraîche, huit jours de 24 000 ticks par défaut et durée configurable. Le temps d'entrée doit être un compteur de monde monotone persisté, indépendant de l'heure du jour modifiable. Les bornes de niveau du lac sont des paramètres et non des valeurs d'équilibrage.
- **Hydrologie simplifiée :** décrue linéaire pendant la saison sèche, crue linéaire pendant les pluies, plateau en saison fraîche ; continuité aux transitions. Cette abstraction ne prétend pas reproduire l'hydrologie réelle du Tonlé Sap. Alternative d'un lac statique écartée car elle ne modélise pas la crue demandée.
- **Limites :** aucun compteur de monde n'est encore enregistré ni avancé par Fabric. Le niveau calculé ne place ni ne retire de blocs, aucune météo ou rizière n'est modifiée ; protection des constructions, chunks non chargés, budgets par tick et reprise après crash restent à concevoir et vérifier avant activation.

## 2026-11-11 — Déclaration initiale de dimension
- **Choix :** déclarer `cambocraft:cambodia` et son type en ressources 1.21.1 ; utiliser provisoirement le générateur de bruit de l'Overworld et le biome jungle vanilla. Les valeurs de hauteur et les champs requis ont été comparés aux données de la version 1.21.1. L'identifiant et l'éclairage sont propres au projet.
- **Alternative :** inventer 14 biomes ou des réglages de terrain non vérifiés aurait donné une fausse impression de contenu complet. Cette dimension de travail est un seul biome et ne remplit pas le P0 ; elle devra être remplacée par la génération demandée et vérifiée en jeu.
- **Limites :** aucune validation par le chargeur de Minecraft, aucun portail naturel, retour ou garantie de compatibilité de sauvegarde. Les contrôles Python vérifient uniquement la structure et les valeurs attendues. Ne pas utiliser dans un monde de production.

## 2026-11-11 — Première pierre et vérification CI
- **Choix :** enregistrer la Pierre du Lotus côté commun, avec modèle, loot, texte fr/en et texture originale générée par script. Une recette provisoire de grès et de teinture rose permet d'en fabriquer pour les tests.
- **Alternative :** attendre le guide ou la génération des temples empêcherait d'essayer le bloc avant ces systèmes. La recette est provisoire et devra être remplacée ou rééquilibrée lors de l'implémentation de la quête d'introduction ; elle ne fait pas office de portail.
- **Build :** la CI GitHub, déclenchée depuis la branche de travail, a pu accéder aux dépendances : le build Fabric de `7865626` y a réussi. Le build local reste bloqué et aucun client/serveur Minecraft n'a été lancé. L'upload CI des jars est ajouté pour faciliter la vérification des futures révisions, mais ne transforme pas cette version en livraison.

## 2026-11-11 — Limite du serveur GameTest
- **Choix :** vérifier dans le serveur de test le registre du type de dimension et l'existence de la ressource de dimension, puis réserver la validation de création du monde à un serveur ordinaire.
- **Raison :** un premier test exigeant un `ServerWorld` personnalisé a échoué ; le serveur GameTest de Minecraft ne crée que ses mondes de test vanilla. Voir `RESEARCH.md`. La modification du test ne prouve donc pas que la dimension complète se charge sur un serveur normal.

## 2026-11-11 — Attachment de sauvegarde Nen
- **Choix :** enregistrer un attachment Fabric persistant, copié à la mort, non synchronisé, contenant une enveloppe d'octets immuable. L'adaptateur de stockage n'accepte qu'un `ServerPlayerEntity` et refuse de lire ou d'écraser une archive métier invalide ou d'une version inconnue.
- **Raison :** le codec Fabric stocke les octets même si le format métier est inconnu ; cela évite qu'une erreur de décodage métier soit automatiquement remplacée par un nouvel état vide. Alternatives écartées : synchronisation générale au client et décodage destructif directement dans le codec Fabric.
- **Limites :** aucune sauvegarde atomique avec la consommation d'un bol, migration, UI ou utilisation gameplay. Fabric peut encore ignorer un attachment si son propre décodage NBT échoue ; le stockage actuel n'est pas une sauvegarde de secours. Les limites de 100 000 pouvoirs/32 Mio persistent ; la reconnexion, la mort réelle d'un joueur, les redémarrages et les crashs n'ont pas été testés.

## 2026-11-11 — Première horloge serveur des saisons
- **Choix :** un compteur de ticks actif sur l'Overworld, conservé par un attachment Fabric et indépendant de l'heure modifiable du jour, pilote les trois saisons. Les joueurs présents dans la dimension reçoivent une annonce traduite à la connexion et aux transitions. La propriété JVM `-Dcambocraft.seasonDays=N` règle chaque saison de 1 à 365 jours, huit par défaut.
- **Raison :** un seul compteur par serveur évite des saisons différentes selon les dimensions ; en absence de joueurs il progresse tant que le serveur tourne. Changer la durée reclassifie l'âge déjà enregistré et peut donc changer immédiatement la saison ; une configuration en jours persistée par monde et une migration seront nécessaires avant livraison.
- **Limites :** météo, rendu, récolte, crue et HUD permanents non raccordés ; pas de vraie dimension accessible normalement. En cas de compteur négatif ou saturé, la progression est suspendue plutôt que réinitialisée. Le test de redémarrage réel reste à faire.

## 2026-11-11 — Référentiel des douze zones
- **Choix :** douze fichiers de données sous `data/cambocraft/zones/`, dont le Royaume des Esprits soumis à la renaissance ; les accès expérimentaux exigent le niveau minimal et la victoire sur le boss précédent. Noms et identifiants des boss sont des données de conception, pas des entités existantes.
- **Raison :** la progression cible devient vérifiable avant de construire les régions et combats. Les fichiers sont lus dans leur ensemble, bornés, validés et publiés après succès de l'ensemble du rechargement ; un échec doit conserver le catalogue précédent. Ce comportement de `/reload` reste à tester en serveur réel, notamment en présence d'erreurs dans d'autres datapacks.
- **Limites :** aucun contrôle d'accès dans le monde ni persistances de niveau/victoires/renaissance ; une courbe d'XP pure utilise `ceil(120 × niveau^1,8)` mais les revenus et son équilibrage ne sont pas définis. Après renaissance, les victoires antérieures devront être conservées pour l'accès au royaume, choix provisoire.

## 2026-11-11 — Destination de portail conservatrice
- **Choix :** rechercher une surface existante dans un rayon borné de huit blocs, sans créer de plateforme ni modifier de constructions. Un sol plein non dangereux et deux blocs d'air sont requis ; refuser si aucun emplacement ne convient.
- **Alternative :** construire systématiquement une plateforme ; rejetée car elle pourrait détruire des blocs de joueur. La recherche peut charger/générer quelques chunks proches et ne garantit pas l'absence de monstres ni de dangers voisins. Elle n'est pas encore reliée au portail ou à une téléportation ; il faudra tester le voyage et les permissions en multijoueur.

## 2026-11-11 — Prototype d'activation de portail
- **Choix provisoire :** clic avec un bol Minecraft en main principale sur la pierre inférieure centrale d'un cadre 3×4 vide ; le serveur contrôle joueur, dimension, distance, cadre et destination avant de voyager. Aucun bol n'est consommé ; délai de cinq secondes. Un cadre doit aussi être construit au retour et le lieu est choisi près des coordonnées du cadre de départ, sans souvenir du portail d'origine.
- **Raison :** rendre la mécanique expérimentable sans détruire le terrain ; le bol et la pierre sont accessibles sans dépendre d'un PNJ ou d'une dimension encore inexistants. Le portail naturel, son aspect visuel, la mise en scène et un retour fiable au cadre d'origine restent à construire.
- **Risques :** aucune épreuve de voyage avec joueur réel au moment de l'ajout ; pas de recherche de monstres, pas de preuve d'intégration aux protections tierces, arrivée possiblement sans matériaux pour reconstruire le retour. À réserver aux mondes de test jusqu'à validation de bout en bout ; la recherche peut refuser un voyage s'il n'y a pas de sol sûr proche.

## 2026-11-11 — Client headless pour le prototype de portail
- **Choix :** utiliser Mineflayer uniquement dans la CI et un mod auxiliaire non distribué pour créer un cadre dans un monde jetable, invoquer le callback de bloc sur un joueur réellement connecté, attendre le cooldown et tester l'aller-retour ainsi que les deux changements de dimension reçus par le client.
- **Limites :** le clic est invoqué par le mod de test, pas par un paquet client ; le monde est modifié volontairement pour préparer le scénario. Cela ne remplace ni un client Fabric graphique ni un playtest, ni les essais de sécurité des réseaux, du combat ou de la reconnexion. Résultat à confirmer en CI.
