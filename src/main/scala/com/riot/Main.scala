package com.riot

import com.riot.client.RiotApiClientLive
import com.riot.config.RiotConfig
import com.riot.server.McpServer
import com.riot.service.RiotServiceLive
import zio.*
import sttp.client3.httpclient.zio.HttpClientZioBackend

object Main extends ZIOAppDefault:

  override def run: ZIO[Any, Any, Any] =
    McpServer.run
      .provide(
        RiotConfig.layer,
        HttpClientZioBackend.layer(),
        RiotApiClientLive.layer,
        RiotServiceLive.layer
      )
