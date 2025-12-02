package com.riot.server

import com.riot.config.RiotConfig
import com.riot.service.RiotService
import zio.*
import zio.http.*
import zio.json.*

object McpServer:

  private def serveStaticFile(path: String): UIO[Response] =
    ZIO.succeed {
      val resourcePath = s"static/$path"
      val contentType = if path.endsWith(".html") then Header.ContentType(MediaType.text.html)
        else if path.endsWith(".css") then Header.ContentType(MediaType.text.css)
        else if path.endsWith(".js") then Header.ContentType(MediaType.application.javascript)
        else Header.ContentType(MediaType.application.`octet-stream`)
      
      Option(getClass.getClassLoader.getResourceAsStream(resourcePath)) match
        case Some(stream) =>
          val bytes = stream.readAllBytes()
          stream.close()
          Response(body = Body.fromArray(bytes), headers = Headers(contentType))
        case None =>
          Response.notFound
    }

  private def createRoutes(service: RiotService): Routes[Any, Response] =
    Routes(
      // Serve index page
      Method.GET / "" -> handler(serveStaticFile("index.html")),
      Method.GET / "static" / "index.html" -> handler(serveStaticFile("index.html")),
      Method.GET / "static" / "styles.css" -> handler(serveStaticFile("styles.css")),
      Method.GET / "static" / "app.js" -> handler(serveStaticFile("app.js")),
      
      // Health check
      Method.GET / "health" -> handler(Response.text("OK")),
      
      // Player Summary
      Method.GET / "api" / "player-summary" -> handler { (req: Request) =>
        val gameName = req.queryParamOrElse("gameName", "")
        val tagLine = req.queryParamOrElse("tagLine", "")
        val language = req.queryParamOrElse("language", "en_US")
        val region = req.queryParamOrElse("region", "kr")
        
        service.getPlayerSummary(gameName, tagLine, language, region)
          .map(summary => Response.text(summary))
          .catchAll(ex => ZIO.succeed(Response.text(s"Error: ${ex.getMessage}").status(Status.InternalServerError)))
      },
      
      // Top Champions
      Method.GET / "api" / "top-champions" -> handler { (req: Request) =>
        val gameName = req.queryParamOrElse("gameName", "")
        val tagLine = req.queryParamOrElse("tagLine", "")
        val language = req.queryParamOrElse("language", "en_US")
        val region = req.queryParamOrElse("region", "kr")
        val count = req.queryParamOrElse("count", "3").toInt
        
        service.getTopChampions(gameName, tagLine, language, region, count)
          .map(result => Response.text(result))
          .catchAll(ex => ZIO.succeed(Response.text(s"Error: ${ex.getMessage}").status(Status.InternalServerError)))
      },
      
      // Champion Mastery
      Method.GET / "api" / "champion-mastery" -> handler { (req: Request) =>
        val gameName = req.queryParamOrElse("gameName", "")
        val tagLine = req.queryParamOrElse("tagLine", "")
        val championName = req.queryParamOrElse("championName", "")
        val language = req.queryParamOrElse("language", "en_US")
        val region = req.queryParamOrElse("region", "kr")
        
        service.getChampionMasteryInfo(gameName, tagLine, championName, language, region)
          .map {
            case Right(response) => Response.json(response.toJson)
            case Left(error) => Response.text(error).status(Status.BadRequest)
          }
          .catchAll(ex => ZIO.succeed(Response.text(s"Error: ${ex.getMessage}").status(Status.InternalServerError)))
      },
      
      // Recent Matches
      Method.GET / "api" / "recent-matches" -> handler { (req: Request) =>
        val gameName = req.queryParamOrElse("gameName", "")
        val tagLine = req.queryParamOrElse("tagLine", "")
        val count = req.queryParamOrElse("count", "3").toInt
        
        service.getRecentMatches(gameName, tagLine, count)
          .map(result => Response.text(result))
          .catchAll(ex => ZIO.succeed(Response.text(s"Error: ${ex.getMessage}").status(Status.InternalServerError)))
      },
      
      // Match Summary
      Method.GET / "api" / "match-summary" -> handler { (req: Request) =>
        val matchId = req.queryParamOrElse("matchId", "")
        val puuid = req.queryParamOrElse("puuid", "")
        
        service.getMatchSummary(matchId, puuid)
          .map {
            case Right(response) => Response.json(response.toJson)
            case Left(error) => Response.text(error).status(Status.BadRequest)
          }
          .catchAll(ex => ZIO.succeed(Response.text(s"Error: ${ex.getMessage}").status(Status.InternalServerError)))
      }
    )

  def run: ZIO[RiotConfig & RiotService, Throwable, Unit] =
    for
      config <- ZIO.service[RiotConfig]
      service <- ZIO.service[RiotService]
      routes = createRoutes(service)
      _ <- ZIO.logInfo(s" MCP Riot Server starting on port ${config.port}")
      _ <- Server.serve(routes).provide(Server.defaultWithPort(config.port))
    yield ()
