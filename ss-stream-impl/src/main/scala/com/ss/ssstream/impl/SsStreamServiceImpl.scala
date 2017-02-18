package com.ss.ssstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.ss.ssstream.api.SsStreamService
import com.ss.ss.api.SsService

import scala.concurrent.Future

/**
  * Implementation of the SsStreamService.
  */
class SsStreamServiceImpl(ssService: SsService) extends SsStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(ssService.hello(_).invoke()))
  }
}
