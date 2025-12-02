package com.riot.models

import zio.json.*

// Account & Summoner Models
case class Account(
  puuid: String,
  gameName: String,
  tagLine: String
)

object Account:
  given JsonDecoder[Account] = DeriveJsonDecoder.gen[Account]

case class Summoner(
  puuid: String,
  profileIconId: Int,
  revisionDate: Long,
  summonerLevel: Int
)

object Summoner:
  given JsonDecoder[Summoner] = DeriveJsonDecoder.gen[Summoner]

// Rank Models
case class RankEntry(
  queueType: String,
  tier: String,
  rank: String,
  leaguePoints: Int,
  wins: Int,
  losses: Int,
  puuid: String
)

object RankEntry:
  given JsonDecoder[RankEntry] = DeriveJsonDecoder.gen[RankEntry]

// Champion Mastery Models
case class ChampionMastery(
  puuid: String,
  championId: Int,
  championLevel: Int,
  championPoints: Int,
  lastPlayTime: Long,
  championPointsSinceLastLevel: Long,
  championPointsUntilNextLevel: Long,
  tokensEarned: Int
)

object ChampionMastery:
  given JsonDecoder[ChampionMastery] = DeriveJsonDecoder.gen[ChampionMastery]
  given JsonEncoder[ChampionMastery] = DeriveJsonEncoder.gen[ChampionMastery]

// Data Dragon Models
case class ChampionData(
  key: String,
  name: String,
  title: String,
  id: String
)

object ChampionData:
  given JsonDecoder[ChampionData] = DeriveJsonDecoder.gen[ChampionData]

case class ChampionsResponse(
  data: Map[String, ChampionData]
)

object ChampionsResponse:
  given JsonDecoder[ChampionsResponse] = DeriveJsonDecoder.gen[ChampionsResponse]

// Match Models
case class MatchParticipantChallenges(
  kda: Option[Double],
  killParticipation: Option[Double]
)

object MatchParticipantChallenges:
  given JsonDecoder[MatchParticipantChallenges] = DeriveJsonDecoder.gen[MatchParticipantChallenges]

case class MatchParticipant(
  puuid: String,
  championName: String,
  championId: Int,
  lane: String,
  role: String,
  teamPosition: Option[String],
  kills: Int,
  deaths: Int,
  assists: Int,
  totalDamageDealtToChampions: Int,
  visionScore: Int,
  wardsPlaced: Int,
  wardsKilled: Int,
  win: Boolean,
  timePlayed: Int,
  challenges: Option[MatchParticipantChallenges]
)

object MatchParticipant:
  given JsonDecoder[MatchParticipant] = DeriveJsonDecoder.gen[MatchParticipant]

case class MatchInfo(
  gameDuration: Int,
  queueId: Int,
  participants: List[MatchParticipant]
)

object MatchInfo:
  given JsonDecoder[MatchInfo] = DeriveJsonDecoder.gen[MatchInfo]

case class MatchDetail(
  info: MatchInfo
)

object MatchDetail:
  given JsonDecoder[MatchDetail] = DeriveJsonDecoder.gen[MatchDetail]

// Response Models for MCP Tools
case class ChampionMasteryResponse(
  gameName: String,
  tagLine: String,
  puuid: String,
  championName: String,
  championId: Int,
  championMastery: ChampionMastery
)

object ChampionMasteryResponse:
  given JsonEncoder[ChampionMasteryResponse] = DeriveJsonEncoder.gen[ChampionMasteryResponse]

case class MatchSummaryResponse(
  championName: String,
  lane: String,
  role: String,
  kills: Int,
  deaths: Int,
  assists: Int,
  kda: Option[Double],
  killParticipation: Option[Double],
  totalDamageDealtToChampions: Int,
  visionScore: Int,
  wardsPlaced: Int,
  wardsKilled: Int,
  win: Boolean,
  teamPosition: Option[String],
  timePlayed: Int,
  gameDuration: Int,
  queueId: Int
)

object MatchSummaryResponse:
  given JsonEncoder[MatchSummaryResponse] = DeriveJsonEncoder.gen[MatchSummaryResponse]
