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
