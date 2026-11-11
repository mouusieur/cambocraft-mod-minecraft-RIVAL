# Recherche initiale — 2026-11-11

Ce dossier est un point de départ, pas une validation culturelle ou technique exhaustive. Les liens servent de sources documentaires ; aucune image, musique ni texte de ces pages n'est incorporé au mod.

## Technique

- [Fabric Example Mod, branche 1.21.1](https://github.com/FabricMC/fabric-example-mod/tree/1.21.1) : modèle de configuration et wrapper, consulté en lecture seule. Le dépôt utilise désormais les mappings officiels ; Cambocraft choisit Yarn. Vérifier les versions ensemble lorsque les dépôts seront accessibles.
- [Fabric Loom](https://wiki.fabricmc.net/documentation:fabric_loom) : séparation des sources client/communes et remappage. La séparation est prévue mais pas configurée tant qu'aucun code client n'existe.
- [Mappings Fabric](https://wiki.fabricmc.net/tutorial:mappings) : choix de Yarn plutôt que des mappings officiels.
- [Index des événements Fabric](https://wiki.fabricmc.net/tutorial:event_index) : cycle de vie et rechargement des données. Vérifier les signatures correspondant exactement à Fabric API 1.21.1 avant implémentation.

Recherche encore nécessaire : Jigsaw et placement des structures en 1.21.1, génération de dimension, compatibilité précise de GeckoLib, attachments et migration, GameTest. Aucun mod tiers de gameplay n'a encore été audité.

## Culture et environnement

- [UNESCO — Angkor](https://whc.unesco.org/en/list/668/) : temples, réseaux hydrauliques et site habité. L'architecture est liée à des valeurs religieuses et symboliques ; éviter de présenter un temple réel comme un simple décor d'ennemis.
- [UNESCO — patrimoine culturel immatériel du Cambodge](https://ich.unesco.org/en/state/cambodia-KH) : ballet royal et pratiques du krama, entre autres. Ces traditions sont vivantes ; ne pas prétendre qu'un emote fictif en est une représentation fidèle.
- [Étude sur le Tonlé Sap, ScienceDirect](https://www.sciencedirect.com/science/article/abs/pii/S0277379119308807) : importance de la pulsation annuelle de crue. Le Grand Lac envisagé sera une interprétation ludique, non une simulation hydrologique scientifique.
- [Original Travel — cuisine cambodgienne](https://www.originaltravel.co.uk/travel-blog/traditional-food-in-cambodia) : exemples d'amok, de bai sach chrouk et de nom banh chok. Les recettes futures devront respecter les variations locales et être revues avec des personnes concernées.
- [Présentation de Bon Om Touk](https://www.mototomcambodia.com/newsvideos/cambodias-water-festival-a-celebration-of-nature-and-history) : courses et bateaux illuminés ; proposition de fête fictive inspirée, et non reproduction d'un rite.

À approfondir avant production : consultation rémunérée de personnes cambodgiennes, toponymie, folklore des Neak Ta, musique, fêtes, artisanat et histoire ; langues et variantes régionales. Les sources touristiques ne suffisent pas à établir une représentation complète ou authentique.

### Référence de schéma 1.21.1

- [mcmeta — données extraites 1.21.1](https://github.com/misode/mcmeta/tree/1.21.1-data-json/data/minecraft) : comparaison des champs de type de dimension et de générateur aux valeurs vanilla de cette version. Aucun test de chargement n'a pu être fait ; le terrain et le biome utilisés sont des références vanilla provisoires, pas des assets importés.
