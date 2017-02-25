package com.ss.mapping.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.api.{Descriptor, ServiceLocator}
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import com.ss.mapping.api.MappingService
import org.slf4j.LoggerFactory
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable.Seq

/**
  * Created by ytaras on 18.02.17.
  */
class MappingLoader extends LagomApplicationLoader {
  val logger = LoggerFactory.getLogger(getClass)
  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new MappingApplication(context) with LagomDevModeComponents

  override def load(context: LagomApplicationContext): LagomApplication =
    new MappingApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def describeServices: Seq[Descriptor] = Seq(
    readDescriptor[MappingService]
  )
}

abstract class MappingApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {
  override def jsonSerializerRegistry: JsonSerializerRegistry = {
    MappingSerializerRegistry
  }
  override def lagomServer: LagomServer = {
    LagomServer.forServices(
      bindService[MappingService].to(wire[MappingServiceImpl])
    )
  }
  readSide.register(wire[NodeReadEventsProcessor])
}

object MappingSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[RegisterMapping],
    JsonSerializer[MappingRegistered],
    JsonSerializer[LoadMapping.type],
    JsonSerializer[UnregisterMapping.type],
    JsonSerializer[MappingUnregistered.type]
  )
}
