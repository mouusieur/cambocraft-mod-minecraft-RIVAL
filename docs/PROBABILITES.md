# Gacha — modèle expérimental, non intégré au jeu

Le simulateur `tools/gacha.py` tire une rareté, pas un pouvoir. Il n'implémente ni sélection de pouvoir, ni bol, ni inventaire, ni persistance, ni réseau. Ses résultats ne prouvent donc pas le fonctionnement du futur mod.

| Rareté | Probabilité de base | Tickets sur 10 000 |
|---|---:|---:|
| Commun | 55 % | 0–5499 |
| Rare | 28 % | 5500–8299 |
| Épique | 12 % | 8300–9499 |
| Légendaire | 4,5 % | 9500–9949 |
| Mythique | 0,5 % | 9950–9999 |

Trois compteurs suivent les tirages consécutifs sans résultat de rareté **au moins** épique, légendaire et mythique respectivement. Après 30, 80 et 250 échecs respectifs, le tirage suivant (31e, 81e, 251e au plus tard) reçoit au minimum la rareté correspondante. Les garanties simultanées prennent le plancher le plus élevé ; un résultat naturel supérieur est conservé. Le résultat final remet à zéro tous les compteurs de rareté inférieure ou égale ; les autres augmentent de 1. Les garanties modifient les taux effectivement observés.

## Simulation reproductible

`python3 tools/gacha.py --draws 5000000 --seed 20261111 --output docs/simulation-gacha.json`

CPython 3.11.2 ; 5 000 000 tirages **pour chaque scénario**, avec la même graine pour comparer les tickets. Sans pity, le programme vérifie chaque fréquence avec une tolérance de 6 écarts-types binomiaux ; avec pity, il vérifie à chaque tirage que les limites ne sont pas dépassées. Une simulation donne une estimation, pas une preuve d'équilibrage ; les tests de seuils complètent ce contrôle.

| Rareté | Sans pity observé | Avec pity observé |
|---|---:|---:|
| Commun | 55,01572 % | 54,83504 % |
| Rare | 28,00050 % | 27,91054 % |
| Épique | 11,97540 % | 11,99134 % |
| Légendaire | 4,50762 % | 4,56446 % |
| Mythique | 0,50076 % | 0,69862 % |

Les plus longues suites observées sans épique+, légendaire+ et mythique sont respectivement 30, 80 et 250 avec pity. Les données détaillées sont dans `simulation-gacha.json`. La RNG Python sert uniquement à la simulation : une implémentation serveur devra employer sa propre RNG, conserver les compteurs et tester les migrations et transactions atomiques avant intégration.

## Cœur Java autonome

`src/main/java/org/cambocraft/power/Gacha.java` et `PityState.java` implémentent les mêmes règles en Java, sans dépendance à Minecraft. `tools/test_java.sh --simulate` compile ce code avec un JDK 21, exécute ses tests puis simule cinq millions de tirages dans chaque scénario en appelant **ce code Java**. La sortie et la graine sont archivées dans `simulation-gacha-java.txt`. La RNG Java utilise un algorithme différent de Python, donc les résultats diffèrent. Les fréquences sans pity sont vérifiées à six écarts-types et les limites sont vérifiées à chaque tirage avec pity.

Ce cœur n'est pas enregistré auprès de Fabric : aucun bol, inventaire, sauvegarde, paquet ou pouvoir jouable n'utilise encore ce code. Un futur appel serveur doit générer lui-même les tickets, appliquer la transition et enregistrer atomiquement le tirage et sa consommation.
