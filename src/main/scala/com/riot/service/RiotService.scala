package com.riot.service

import com.riot.client.RiotApiClient
import com.riot.models.*
import zio.*

trait RiotService:
  def getPlayerSummary(gameName: String, tagLine: String, language: String, region: String): Task[String]
  def getTopChampions(gameName: String, tagLine: String, language: String, region: String, count: Int): Task[String]
  def getChampionMasteryInfo(gameName: String, tagLine: String, championName: String, language: String, region: String): Task[Either[String, ChampionMasteryResponse]]
  def getRecentMatches(gameName: String, tagLine: String, count: Int): Task[String]
  def getMatchSummary(matchId: String, puuid: String): Task[Either[String, MatchSummaryResponse]]

case class RiotServiceLive(client: RiotApiClient) extends RiotService:

  override def getPlayerSummary(gameName: String, tagLine: String, language: String, region: String): Task[String] =
    for
      accountOpt <- client.getAccount(gameName, tagLine)
      account <- ZIO.fromOption(accountOpt).orElseFail(new Exception("Failed to find player"))
      puuid = account.puuid
      
      summonerOpt <- client.getSummonerByPuuid(puuid, region)
      summoner <- ZIO.fromOption(summonerOpt).orElseFail(new Exception("Failed to get summoner profile"))
      
      level = summoner.summonerLevel
      rank <- getRankString(puuid, region)
      champMap <- client.getChampionMap(language)
      topChamps <- getTopChampsString(puuid, champMap, region, 3)
      matches <- getRecentMatchesString(puuid, 3)
      
      summary = s"""
👤 $gameName (Level $level)

🏅 Rank: $rank

🔥 Top Champions:
$topChamps

🕹️ Recent Matches:
$matches
"""
    yield summary.trim

  override def getTopChampions(gameName: String, tagLine: String, language: String, region: String, count: Int): Task[String] =
    for
      accountOpt <- client.getAccount(gameName, tagLine)
      account <- ZIO.fromOption(accountOpt).orElseFail(new Exception("Failed to find player"))
      champMap <- client.getChampionMap(language)
      result <- getTopChampsString(account.puuid, champMap, region, count)
    yield result

  override def getChampionMasteryInfo(
    gameName: String,
    tagLine: String,
    championName: String,
    language: String,
    region: String
  ): Task[Either[String, ChampionMasteryResponse]] =
    (for
      accountOpt <- client.getAccount(gameName, tagLine)
      account <- ZIO.fromOption(accountOpt).orElseFail(new Exception("Failed to find player"))
      
      champMap <- client.getChampionMap(language)
      championIdOpt = champMap.find((_, name) => name.toLowerCase == championName.toLowerCase).map(_._1)
      championId <- ZIO.fromOption(championIdOpt).orElseFail(new Exception(s"Champion '$championName' not found"))
      
      masteryOpt <- client.getChampionMastery(account.puuid, championId, region)
      mastery <- ZIO.fromOption(masteryOpt).orElseFail(new Exception(s"Could not find mastery data for $championName"))
      
      response = ChampionMasteryResponse(
        gameName = gameName,
        tagLine = tagLine,
        puuid = account.puuid,
        championName = championName,
        championId = championId,
        championMastery = mastery
      )
    yield Right(response)).catchAll(ex => ZIO.succeed(Left(ex.getMessage)))

  override def getRecentMatches(gameName: String, tagLine: String, count: Int): Task[String] =
    for
      accountOpt <- client.getAccount(gameName, tagLine)
      account <- ZIO.fromOption(accountOpt).orElseFail(new Exception("Failed to find player"))
      result <- getRecentMatchesString(account.puuid, count)
    yield result

  override def getMatchSummary(matchId: String, puuid: String): Task[Either[String, MatchSummaryResponse]] =
    (for
      matchOpt <- client.getMatchDetail(matchId)
      matchDetail <- ZIO.fromOption(matchOpt).orElseFail(new Exception("Failed to load match data"))
      
      participantOpt = matchDetail.info.participants.find(_.puuid == puuid)
      participant <- ZIO.fromOption(participantOpt).orElseFail(new Exception(s"No participant found with puuid: $puuid"))
      
      response = MatchSummaryResponse(
        championName = participant.championName,
        lane = participant.lane,
        role = participant.role,
        kills = participant.kills,
        deaths = participant.deaths,
        assists = participant.assists,
        kda = participant.challenges.flatMap(_.kda),
        killParticipation = participant.challenges.flatMap(_.killParticipation),
        totalDamageDealtToChampions = participant.totalDamageDealtToChampions,
        visionScore = participant.visionScore,
        wardsPlaced = participant.wardsPlaced,
        wardsKilled = participant.wardsKilled,
        win = participant.win,
        teamPosition = participant.teamPosition,
        timePlayed = participant.timePlayed,
        gameDuration = matchDetail.info.gameDuration,
        queueId = matchDetail.info.queueId
      )
    yield Right(response)).catchAll(ex => ZIO.succeed(Left(ex.getMessage)))

  private def getRankString(puuid: String, region: String): Task[String] =
    client.getRankEntries(puuid, region).map { entries =>
      val soloRank = entries.find(_.queueType == "RANKED_SOLO_5x5").map { solo =>
        val winrate = math.round((solo.wins.toDouble / (solo.wins + solo.losses)) * 100)
        s"Solo: ${solo.tier} ${solo.rank} (${solo.leaguePoints} LP) - ${solo.wins}W ${solo.losses}L ($winrate% WR)"
      }.getOrElse("Solo: Unranked")
      
      val flexRank = entries.find(_.queueType == "RANKED_FLEX_SR").map { flex =>
        val winrate = math.round((flex.wins.toDouble / (flex.wins + flex.losses)) * 100)
        s"Flex: ${flex.tier} ${flex.rank} (${flex.leaguePoints} LP) - ${flex.wins}W ${flex.losses}L ($winrate% WR)"
      }.getOrElse("Flex: Unranked")
      
      s"$soloRank | $flexRank"
    }

  private def getTopChampsString(puuid: String, champMap: Map[Int, String], region: String, count: Int): Task[String] =
    client.getTopChampionMasteries(puuid, region, count).map { masteries =>
      if masteries.isEmpty then
        "No champion mastery data found."
      else
        masteries.map { m =>
          val champName = champMap.getOrElse(m.championId, s"ID(${m.championId})")
          s"- $champName: Level ${m.championLevel}, ${m.championPoints} pts"
        }.mkString("\n")
    }

  private def getRecentMatchesString(puuid: String, count: Int): Task[String] =
    for
      matchIds <- client.getMatchIds(puuid, count)
      matches <- ZIO.foreach(matchIds) { matchId =>
        client.getMatchDetail(matchId).map { matchOpt =>
          matchOpt.flatMap { matchDetail =>
            matchDetail.info.participants.find(_.puuid == puuid).map { p =>
              val result = if p.win then "Win" else "Loss"
              s"$matchId ${p.championName}: ${p.kills}/${p.deaths}/${p.assists} - $result"
            }
          }
        }
      }
      validMatches = matches.flatten
    yield
      if validMatches.isEmpty then "No recent matches found."
      else validMatches.mkString("\n")

object RiotServiceLive:
  val layer: ZLayer[RiotApiClient, Nothing, RiotService] =
    ZLayer {
      for client <- ZIO.service[RiotApiClient]
      yield RiotServiceLive(client)
    }
