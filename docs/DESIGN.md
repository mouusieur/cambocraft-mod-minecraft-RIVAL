# Conception de Cambocraft — document de travail

**Statut : conception, non fonctionnalités livrées.** Voir `TESTS.md` et `ROADMAP.md` pour l'état réel. Aucune estimation d'équilibrage n'est présentée comme un résultat de playtest.

## Architecture cible

```text
Client : HUD, écrans, rendu, audio  ── demandes non fiables ──▶ Serveur
                                                               │
Serveur : validation → quêtes/progression/pouvoirs/combat/économie/monde
                                                               │
                                    données joueur et monde versionnées
                                                               │
                    ressources JSON validées ── /reload atomique ──┘
```

Le serveur contrôlera possession, ressources, permissions, état, portée, cooldown et disponibilité avant toute mutation ; le client ne devra jamais transmettre le résultat d'un tirage ou des dégâts. Le chargement préparera et validera les données avant échange atomique ; en cas d'erreur, conserver l'ancienne version. Tester mort, déconnexion, respawn, dimension et migration avant livraison. Les effets visuels ne doivent jamais faire autorité.

## Boucle et progression visées

Le guide est rencontré dans l'Overworld afin d'obtenir la pierre et le bol nécessaires au portail : cela évite la contradiction d'une introduction située uniquement après le portail. Dans la dimension : exploration → objectif de quête → expérience/ressources → boss et déblocage → nouvelle zone ; les activités civiles et la réputation donnent des chemins parallèles. Le niveau et la victoire sur le boss précédent conditionnent les accès. Les limites de niveaux du cahier des charges se chevauchent : les niveaux indiqués ci-dessous sont des **prérequis minimaux**, pas des plages exclusives. Les conditions et niveaux sont modélisés et chargés depuis des JSON, mais ne bloquent aucun accès en jeu et ne sont pas équilibrés.

| # | Zone | Niveau minimal prévu | Boss prévu |
|---|---|---:|---|
| 1 | Village de Tonlé | 1 | Chef des bandits de la rivière |
| 2 | Rizières de Ly | 15 | Buffle d'Or |
| 3 | Bambouseraie de Srok | 30 | Maître des lames |
| 4 | Grand Lac | 50 | Crocodile des Profondeurs |
| 5 | Forêt de Kompong | 70 | Serpent de la forêt |
| 6 | Temples de Prasat Sovann | 90 | Gardien de pierre |
| 7 | Forteresse Ombre | 110 | Généralissime |
| 8 | Montagnes karstiques | 130 | Phénix de latérite |
| 9 | Labyrinthe des Visages | 150 | Éveil du Visage |
| 10 | Îles des Garudas | 165 | Garuda de la Tempête |
| 11 | Sanctuaire du Lotus | 185 | Gardien de l'Équilibre |
| 12 | Royaume des Esprits | Renaissance | Ombre Première |

La formule `120 × n^1,8` n'est qu'une hypothèse à simuler avec les récompenses et temps réels ; pas de promesse d'équilibrage avant bot et bêta. La renaissance conserve pouvoirs, maîtrise et réputation ; les bonus plafonnés restent à chiffrer. Les fins Lumière, Ombre et Équilibre nécessiteront des choix persistants et conséquences distinctes.

## Pouvoirs et économie

Le modèle expérimental des probabilités est dans `PROBABILITES.md`. La demande impose au moins 12 pouvoirs pour chacun des sept éléments : avec une seule résonance par pouvoir, cela nécessite **au moins 84 pouvoirs**, et non 80. Prévoir 84 pouvoirs distincts, au moins 20 communs, 18 rares, 18 épiques, 14 légendaires et 10 mythiques, et vérifier au moins six types par rareté. Leur catalogue, valeurs, effets, 30 synergies et maîtrise restent à produire et tester.

Les prix, revenus et taux de récolte ne sont pas fixés : ils doivent être établis ensemble et validés par simulation et temps de farm mesuré. Toute inflation saisonnière devra être bornée et éviter des fluctuations exploitables. Aucune économie ne fonctionne aujourd'hui.

## Saisons et monde

Cycle serveur cible : trois saisons de huit jours de jeu configurables, état sauvegardé à l'échelle du monde. Le niveau d'eau devra respecter les constructions du joueur : aucune modification destructive des blocs placés, reprise sûre après redémarrage et traitement limité par tick. Les règles de protection et la faisabilité de la génération restent à prouver. Les 14 biomes, 60 structures NBT avec variantes, Jigsaw et placement sont absents.

## Production et risques

Inventaire demandé : 200 blocs, 180 items, modèles/animations PNJ, ninjas, boss, animaux et montures, 60 structures avec variantes, 30 particules, 100 sons et six pistes au minimum ; aucune de ces quantités n'est atteinte. Les pistes et couches par zone exigent plus que six thèmes distincts si chaque zone doit avoir sa propre musique adaptative : prévoir au moins douze ensembles ou documenter explicitement une mutualisation après validation. Les assets doivent être originaux ou sous licence compatible. Une revue culturelle est indispensable.

Le planning, les dépendances, les estimations et le statut des P2 figurent dans `ROADMAP.md` ; la recherche culturelle et technique reste à approfondir dans `RESEARCH.md`.
