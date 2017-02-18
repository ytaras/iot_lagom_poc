package com.ss.ssstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.ss.ssstream.api.SsStreamService
import com.ss.ss.api.SsService
import com.softwaremill.macwire._

class SsStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SsStreamApplication(context) {
      override def serviceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SsStreamApplication(context) with LagomDevModeComponents

  override def describeServices = List(
    readDescriptor[SsStreamService]
  )
}

abstract class SsStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[SsStreamService].to(wire[SsStreamServiceImpl])
  )

  // Bind the SsService client
  lazy val ssService = serviceClient.implement[SsService]
}
