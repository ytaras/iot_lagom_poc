package com.ss.mapping.impl

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.UnhandledCommandException
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.ss.mapping.api.{GlobalSensorId, MappingService, SensorMapping}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by ytaras on 18.02.17.
  */
class MappingServiceImpl(persistentEntityRegistry: PersistentEntityRegistry)(implicit ec: ExecutionContext) extends MappingService {

  persistentEntityRegistry.register(new SensorMappingPersistentEntity)

  override def sensorMapping(nodeId: Long, sensorId: Int): ServiceCall[NotUsed, SensorMapping] = ServiceCall { _ =>
    mappingRef(nodeId, sensorId)
      .ask(LoadMapping)
  }

  private def entityId(nodeId: Long, sensorId: Int) = s"$nodeId:$sensorId"
  private def mappingRef(nodeId: Long, sensorId: Int) =
    persistentEntityRegistry.refFor[SensorMappingPersistentEntity](entityId(nodeId, sensorId))

  override def registerMapping(nodeId: Long, sensorId: Int): ServiceCall[SensorMapping, NotUsed] = ServiceCall { sm =>
    mappingRef(nodeId, sensorId)
      .ask(RegisterMapping(sm)).map(_ => NotUsed)
  }
}
