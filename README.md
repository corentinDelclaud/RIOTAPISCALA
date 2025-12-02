# MCP Riot Server - Scala Version

**MCP-Riot Scala**

> **Disclaimer:** Ceci est un projet open-source *non affilié ni approuvé par Riot Games.* League of Legends® est une marque déposée de Riot Games, Inc.

# MCP Riot Server - Scala Version

![CI](https://github.com/corentinDelclaud/RIOTAPISCALA/actions/workflows/sbt.yml/badge.svg)

**MCP-Riot Scala**

> Disclaimer: Ce projet est open-source et n'est ni affilié ni approuvé par Riot Games. League of Legends® est une marque déposée de Riot Games, Inc.

Résumé
-------
Serveur backend écrit en Scala 3 (ZIO) exposant des endpoints pour interroger des données publiques liées aux joueurs et matchs League of Legends via l'API Riot.

Fonctionnalités principales
- Récupération du profil joueur (niveau, rang Solo/Flex)
- Top champions par maîtrise
- Historique de matchs récents (champion, K/D/A, résultat)
-  Maîtrise de Champion (non implémenté) (Retourne les données détaillées de maîtrise pour un champion spécifique.)
- Résumé de Match (non implémenté) (Retourne les statistiques du joueur pour un match donné.)

Prérequis
- Java 17+ (correspondant à Scala 3 et aux dépendances)
- sbt (ou utilisez les commandes sbt dans votre shell)


Configuration
-------------
Le projet attend une clé d'API Riot et éventuellement un port. Vous pouvez définir ces valeurs via un fichier `.env` ou des variables d'environnement.

Exemple `.env` (à créer à la racine):
```
RIOT_API_KEY=RGAPI-xxxx-your-key-xxxx
PORT=8081
```

Démarrage local
---------------
Ouvrez une console puis:
```powershell
A la racine de ce projet
# Compiler
sbt compile
# Lancer le serveur
sbt run
```

Sur Linux / macOS:
```bash
export RIOT_API_KEY="RGAPI-..."
export PORT=8081
sbt run
```

Tester
------
Exécutez les tests unitaires (s'il y en a):
```bash
sbt test
```

CI (GitHub Actions)
---------------------
Un workflow GitHub Actions est fourni (`.github/workflows/sbt.yml`) qui :
- installe Java 17
- installe/active sbt
- met en cache les dépendances (`.ivy2`, `.sbt`, `.coursier`)
- exécute `sbt clean compile test` sur chaque push et pull request

Voir le badge en haut pour l'état du build.

Structure du projet
-------------------
- `src/main/scala/com/...` : code source
- `src/main/resources` : ressources statiques et fichiers de config
- `build.sbt` : configuration sbt / dépendances

Contact
-------
Corentin Delclaud — `corentin.delclaud@gmail.com`

