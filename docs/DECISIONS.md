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
