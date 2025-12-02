# MCP Riot Server - Scala Version

**MCP-Riot Scala**

> **Disclaimer:** Ceci est un projet open-source *non affilié ni approuvé par Riot Games.* League of Legends® est une marque déposée de Riot Games, Inc.


## Fonctionnalités

### Résumé du Joueur
Fournit le niveau du joueur, le rang Solo, les champions les plus maîtrisés et l'historique de matchs récents.

### Top Champions
Retourne les N meilleurs champions basés sur les points de maîtrise.

### Maîtrise de Champion (non implémenté)
Retourne les données détaillées de maîtrise pour un champion spécifique.

### Matchs Récents
Liste les matchs récents incluant le champion utilisé, K/D/A et le résultat.

### Résumé de Match (non implémenté)
Retourne les statistiques du joueur pour un match donné.


## Démarrage Rapide

### 1. Configuration de .env

Dans le point .env


RIOT_API_KEY=your_riot_api_key
PORT=port


Obtenez votre clé sur https://developer.riotgames.com/

### 2. Compilation et Exécution

#### Option 1: Utiliser le fichier .env (Recommandé)

```bash
# Compiler le projet
sbt compile

# Exécuter le serveur (lit automatiquement le .env)
sbt run
```

#### Option 2: Variables d'environnement explicites

**Bash/Linux/MacOS:**
```bash
export RIOT_API_KEY="RGAPI-6fc1c4ce-a756-428d-90c3-dab277ad812d"
export PORT="8081"
sbt run
```

**PowerShell (Windows):**
```powershell
$env:RIOT_API_KEY="api_key"
$env:PORT="8081"
sbt run
```

**CMD (Windows):**
```cmd
set RIOT_API_KEY=your_riot_api_key
set PORT=8081
sbt run
```

Le serveur démarrera sur le port 8081 (configurable via PORT dans .env).

#### Option 3: in the terminal
cd "mcp-riot-scala" ; $env:RIOT_API_KEY="" ; $env:PORT="" ; sbt run

### 3. Accéder à l'Interface Web

Une fois le serveur lancé, ouvrez votre navigateur sur:

```
http://localhost:port
```

Vous verrez une interface graphique moderne permettant de:
- Rechercher des joueurs par GameName et TagLine
- Consulter le résumé du joueur (niveau, ranks Solo/Flex)
- Voir les champions les plus maîtrisés
- Explorer l'historique des matchs récents
- etc

## Architecture

Le projet est organisé en plusieurs modules:

- **Models.scala**: Modèles de données pour les réponses API Riot
- **RiotConfig.scala**: Configuration (API key, régions, etc.)
- **RiotApiClient.scala**: Client HTTP pour communiquer avec l'API Riot
- **RiotService.scala**: Logique métier et opérations
- **McpServer.scala**: Serveur MCP exposant les outils
- **Main.scala**: Point d'entrée de l'application

---

## Technologies Utilisées

- **Scala 3.6.3**: Langage de programmation fonctionnel
- **ZIO 2.1.14**: Framework pour la programmation fonctionnelle et asynchrone
- **ZIO HTTP 3.0.1**: Serveur HTTP
- **STTP 3.10.2**: Client HTTP
- **ZIO JSON 0.7.3**: Sérialisation/désérialisation JSON
