package com.ss.ss.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.ss.ss.api.SsService
import com.softwaremill.macwire._

class SsLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SsApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SsApplication(context) with LagomDevModeComponents

  override def describeServices = List(
    readDescriptor[SsService]
  )
}

abstract class SsApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[SsService].to(wire[SsServiceImpl])
  )

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = SsSerializerRegistry

  // Register the ss persistent entity
  persistentEntityRegistry.register(wire[SsEntity])
}
