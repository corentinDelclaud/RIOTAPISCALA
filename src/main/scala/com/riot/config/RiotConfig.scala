package com.riot.config

import zio.*

case class RiotConfig(
  apiKey: String,
  port: Int
)

object RiotConfig:
  val layer: ZLayer[Any, SecurityException, RiotConfig] =
    ZLayer.fromZIO {
      for
        apiKey <- System.env("RIOT_API_KEY").flatMap {
          case Some(key) => ZIO.succeed(key)
          case None => ZIO.fail(new SecurityException("RIOT_API_KEY environment variable not set"))
        }
        port <- System.env("PORT")
          .map(_.flatMap(_.toIntOption).getOrElse(9999))
          .orDie
      yield RiotConfig(apiKey, port)
    }
