# Feuille de route restante — 2026-11-11

Estimations grossières en **semaines-personne** (SP), non engagements calendaires. Les lots peuvent se chevaucher ; QA et itérations culturelles/artistiques peuvent multiplier ces estimations. Aucun lot ci-dessous n'est complet. L'environnement doit d'abord avoir un JDK 21 complet et accès aux dépôts Gradle, Fabric, Maven et Minecraft. Les besoins P0 ne sont pas rétrogradés.

## P0 d'abord

| Lot restant | Estimation | Dépendances et sortie vérifiable |
|---|---:|---|
| Build, CI, client et serveur dédiés | 1–2 SP | Réseau, JDK ; build et lancements documentés |
| Persistance versionnée et réseau serveur | 3–5 SP | Socle ; migrations, reconnexion, validation des paquets |
| Portail, dimension, quête d'accès | 4–8 SP | Socle et sauvegarde ; accès légitime et retour sûr |
| Biomes, terrain, saisons, lac, riz | 10–18 SP | Dimension ; test terrain et protection des constructions |
| 12 zones, structures, Jigsaw, 12 boss | 30–50 SP | Monde, combat ; arènes, accès, phases, progression vérifiée |
| Combat, ninjas et transformations | 10–18 SP | Entités et réseau ; IA, signaux, GameTests |
| Quêtes, dialogues et progression 1–200 | 12–20 SP | Sauvegarde, monde, PNJ ; 12 chapitres, 60 secondaires, fins |
| 84 pouvoirs, gacha jouable, énergie et carnet | 18–30 SP | Sauvegarde, réseau, contenu ; simulateur déjà prototypé, intégration absente |
| Assets P0, traductions fr/en et audio | 35–60 SP | Direction artistique, conception, licences ; production et revue en jeu |
| Outils, GameTests et validation | 8–14 SP | Schémas et intégration ; CI verte et tests réels |

## P1 ensuite

| Lot restant | Estimation | Dépendances |
|---|---:|---|
| PNJ, réputation, marchands et économie | 12–20 SP | Données, sauvegarde, quêtes, assets |
| Cuisine, pêche, soie, sculpture et forge | 12–20 SP | Économie, recettes, modèles et interface |
| Compagnon, monture et pirogue | 8–14 SP | Entités, IA, sauvegarde, animations |
| Événements saisonniers et Nuit des Ombres | 6–10 SP | Saisons, villages, IA, économie |
| Synergies et maîtrise | 8–12 SP | Pouvoirs, réseau, équilibrage |
| Pradal serey, danse, course de pirogues | 10–18 SP | Combat, animations, réseau, Grand Lac |
| Renaissance, royaume post-200 et raids | 10–18 SP | Progression, boss, persistance multijoueur |
| Accessibilité, bot, bêta, tests de charge | 12–20 SP | Systèmes jouables, infrastructure multijoueur |

## P2 non livrées

| Bonus | Estimation | Dépendances |
|---|---:|---|
| Fusion de pouvoirs | 3–5 SP | Pouvoirs de niveau 5, maîtrise, économie |
| Tuk-tuk | 3–5 SP | Véhicules, collisions, modèles et audio |
| Donjon roguelite | 8–14 SP | Structures, combat, génération par graine |
| Jeu de société traditionnel | 4–7 SP | Recherche culturelle, règles et IA, interface |
| Mode photo | 4–7 SP | Client, rendu, journal et stockage |
| Localisation khmère | 4–8 SP | Polices testées, traduction et relecture professionnelles |

Le cahier des charges classe parfois la course de pirogues et le Royaume des Esprits en P2 mais les exige ensuite comme P1 et critères de réussite : ils sont donc traités en **P1**, pas en bonus.
