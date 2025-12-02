package com.riot.client

import com.riot.config.RiotConfig
import com.riot.models.*
import zio.*
import zio.json.*
import sttp.client3.*
import sttp.client3.ziojson.*
import sttp.client3.SttpBackend

trait RiotApiClient:
  def getAccount(gameName: String, tagLine: String): Task[Option[Account]]
  def getSummonerByPuuid(puuid: String, region: String): Task[Option[Summoner]]
  def getRankEntries(puuid: String, region: String): Task[List[RankEntry]]
  def getTopChampionMasteries(puuid: String, region: String, count: Int): Task[List[ChampionMastery]]
  def getChampionMastery(puuid: String, championId: Int, region: String): Task[Option[ChampionMastery]]
  def getMatchIds(puuid: String, count: Int): Task[List[String]]
  def getMatchDetail(matchId: String): Task[Option[MatchDetail]]
  def getChampionMap(language: String): Task[Map[Int, String]]

case class RiotApiClientLive(config: RiotConfig, backend: SttpBackend[Task, Any]) extends RiotApiClient:
  
  private val championMapCache: Ref[Map[String, Map[Int, String]]] = 
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.run(Ref.make(Map.empty[String, Map[Int, String]])).getOrThrow()
    }

  private def headers: Map[String, String] = Map(
    "Content-Type" -> "application/json"
  )

  override def getAccount(gameName: String, tagLine: String): Task[Option[Account]] =
    val url = uri"https://europe.api.riotgames.com/riot/account/v1/accounts/by-riot-id/$gameName/$tagLine?api_key=${config.apiKey}"
    basicRequest
      .get(url)
      .headers(headers)
      .response(asJson[Account])
      .send(backend)
      .map(_.body.toOption)
      .catchAll { error =>
        ZIO.logError(s"Error fetching account: $error") *> ZIO.succeed(None)
      }

  override def getSummonerByPuuid(puuid: String, region: String): Task[Option[Summoner]] =
    val url = uri"https://$region.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/$puuid?api_key=${config.apiKey}"
    basicRequest
      .get(url)
      .headers(headers)
      .response(asJson[Summoner])
      .send(backend)
      .map(_.body.toOption)
      .catchAll { error =>
        ZIO.logError(s"Error fetching summoner: $error") *> ZIO.succeed(None)
      }

  override def getRankEntries(puuid: String, region: String): Task[List[RankEntry]] =
    val url = uri"https://$region.api.riotgames.com/lol/league/v4/entries/by-puuid/$puuid?api_key=${config.apiKey}"
    basicRequest
      .get(url)
      .headers(headers)
      .response(asJson[List[RankEntry]])
      .send(backend)
      .map(_.body.getOrElse(List.empty))
      .catchAll { error =>
        ZIO.logError(s"Error fetching rank entries: $error") *> ZIO.succeed(List.empty)
      }

  override def getTopChampionMasteries(puuid: String, region: String, count: Int): Task[List[ChampionMastery]] =
    val url = uri"https://$region.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-puuid/$puuid/top?count=$count&api_key=${config.apiKey}"
    basicRequest
      .get(url)
      .headers(headers)
      .response(asJson[List[ChampionMastery]])
      .send(backend)
      .map(_.body.getOrElse(List.empty))
      .catchAll { error =>
        ZIO.logError(s"Error fetching champion masteries: $error") *> ZIO.succeed(List.empty)
      }

  override def getChampionMastery(puuid: String, championId: Int, region: String): Task[Option[ChampionMastery]] =
    val url = uri"https://$region.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-puuid/$puuid/by-champion/$championId?api_key=${config.apiKey}"
    basicRequest
      .get(url)
      .headers(headers)
      .response(asJson[ChampionMastery])
      .send(backend)
      .map(_.body.toOption)
      .catchAll { error =>
        ZIO.logError(s"Error fetching champion mastery: $error") *> ZIO.succeed(None)
      }

  override def getMatchIds(puuid: String, count: Int): Task[List[String]] =
    val url = uri"https://europe.api.riotgames.com/lol/match/v5/matches/by-puuid/$puuid/ids?count=$count&api_key=${config.apiKey}"
    basicRequest
      .get(url)
      .headers(headers)
      .response(asJson[List[String]])
      .send(backend)
      .map(_.body.getOrElse(List.empty))
      .catchAll { error =>
        ZIO.logError(s"Error fetching match IDs: $error") *> ZIO.succeed(List.empty)
      }

  override def getMatchDetail(matchId: String): Task[Option[MatchDetail]] =
    val url = uri"https://europe.api.riotgames.com/lol/match/v5/matches/$matchId?api_key=${config.apiKey}"
    basicRequest
      .get(url)
      .headers(headers)
      .response(asJson[MatchDetail])
      .send(backend)
      .map(_.body.toOption)
      .catchAll { error =>
        ZIO.logError(s"Error fetching match detail: $error") *> ZIO.succeed(None)
      }

  override def getChampionMap(language: String): Task[Map[Int, String]] =
    championMapCache.get.flatMap { cache =>
      cache.get(language) match
        case Some(map) => ZIO.succeed(map)
        case None =>
          for
            versionList <- fetchLatestVersion
            version = versionList.headOption.getOrElse("14.1.1")
            champMap <- fetchChampionData(version, language)
            _ <- championMapCache.update(_ + (language -> champMap))
          yield champMap
    }

  private def fetchLatestVersion: Task[List[String]] =
    val url = uri"https://ddragon.leagueoflegends.com/api/versions.json"
    basicRequest
      .get(url)
      .response(asJson[List[String]])
      .send(backend)
      .map(_.body.getOrElse(List("14.1.1")))
      .catchAll { error =>
        ZIO.logError(s"Error fetching version: $error") *> ZIO.succeed(List("14.1.1"))
      }

  private def fetchChampionData(version: String, language: String): Task[Map[Int, String]] =
    val url = uri"https://ddragon.leagueoflegends.com/cdn/$version/data/$language/champion.json"
    basicRequest
      .get(url)
      .response(asJson[ChampionsResponse])
      .send(backend)
      .map { response =>
        response.body match
          case Right(championsResponse) =>
            championsResponse.data.map { case (_, champData) =>
              champData.key.toInt -> champData.name
            }
          case Left(_) => Map.empty[Int, String]
      }
      .catchAll { error =>
        ZIO.logError(s"Error fetching champion data: $error") *> ZIO.succeed(Map.empty)
      }

object RiotApiClientLive:
  val layer: ZLayer[RiotConfig & SttpBackend[Task, Any], Nothing, RiotApiClient] =
    ZLayer {
      for
        config <- ZIO.service[RiotConfig]
        backend <- ZIO.service[SttpBackend[Task, Any]]
      yield RiotApiClientLive(config, backend)
    }
