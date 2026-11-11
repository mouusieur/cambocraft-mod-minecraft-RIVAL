# Décisions

## 2026-11-11 — Socle et limites
- **Choix :** Fabric 1.21.1, Yarn `1.21.1+build.3`, Loom 1.7.4, Loader 0.20.1, API 0.116.17+1.21.1 et Gradle 8.10.2. Versions fixées pour rendre la résolution reproductible. Le Loader et l'API suivent l'exemple Fabric de la branche 1.21.1 ; leur combinaison avec ce Loom reste à vérifier en compilation.
- **Alternatives :** mappings officiels et versions flottantes écartés pour respecter la demande Yarn et limiter les changements imprévus.
- **Animations :** aucune bibliothèque d'animation pour l'instant. Le choix GeckoLib ou animation native est reporté jusqu'au premier modèle animé et à un test d'intégration ; ne pas ajouter une dépendance invérifiée au socle.
- **Contrainte :** pas de JDK ni de Gradle installés initialement ; accès réseau refusé à services.gradle.org, maven.fabricmc.net et Maven Central. Un runtime Java 21 temporaire a été obtenu depuis PyPI, mais il ne contient pas `javac`. La compilation Fabric et les lancements restent non vérifiés.
- **Périmètre :** le dépôt était vide à l'exception du README. Ce socle ne constitue pas un mod jouable et ne valide aucun critère de jeu de la section 18.
